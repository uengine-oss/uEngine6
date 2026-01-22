package org.uengine.five.businessrule;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.camunda.bpm.dmn.engine.DmnDecision;
import org.camunda.bpm.dmn.engine.DmnDecisionRuleResult;
import org.camunda.bpm.dmn.engine.DmnDecisionTableResult;
import org.camunda.bpm.dmn.engine.DmnEngine;
import org.camunda.bpm.dmn.engine.DmnEngineConfiguration;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Evaluates business rule JSON stored under ruleJson.rules (hitPolicy=FIRST).
 */
@Component
public class BusinessRuleEvaluator {

    private static final DmnEngine DMN_ENGINE = DmnEngineConfiguration.createDefaultDmnEngineConfiguration()
            .buildEngine();

    public Map<String, Object> evaluate(JsonNode ruleJson, Map<String, Object> inputs) {
        if (ruleJson == null) {
            throw new IllegalArgumentException("ruleJson is required");
        }
        JsonNode rules = ruleJson.get("rules");
        if (rules != null && rules.isArray()) {
            return evaluateJsonRules(rules, inputs);
        }

        JsonNode dmnXmlNode = ruleJson.get("dmnXml");
        if (dmnXmlNode != null && dmnXmlNode.isTextual() && !dmnXmlNode.asText().isBlank()) {
            return evaluateDmnXml(dmnXmlNode.asText(), inputs);
        }

        throw new IllegalArgumentException("ruleJson must have either rules(array) or dmnXml(string)");
    }

    private Map<String, Object> evaluateJsonRules(JsonNode rules, Map<String, Object> inputs) {

        for (JsonNode rule : rules) {
            JsonNode conditions = rule.get("conditions");
            if (conditions == null || !conditions.isArray()) {
                continue;
            }
            if (matchesAll(conditions, inputs)) {
                JsonNode result = rule.get("result");
                return toMap(result);
            }
        }

        return new LinkedHashMap<>();
    }

    private Map<String, Object> evaluateDmnXml(String dmnXml, Map<String, Object> inputs) {
        String normalized = normalizeDmnXmlForCamunda(dmnXml);

        // convention: decision id stored as "decision_<ruleIdWithUnderscores>"
        String decisionId = extractDecisionId(normalized);
        if (decisionId == null || decisionId.isBlank()) {
            throw new IllegalArgumentException("DMN decision id not found");
        }

        DmnDecision decision;
        try (InputStream in = new ByteArrayInputStream(normalized.getBytes(StandardCharsets.UTF_8))) {
            decision = DMN_ENGINE.parseDecision(decisionId, in);
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to parse DMN XML", e);
        }

        Map<String, Object> coercedInputs = coerceInputsForDmn(normalized, decisionId, inputs);

        VariableMap vars = Variables.createVariables();
        if (coercedInputs != null) {
            for (Map.Entry<String, Object> e : coercedInputs.entrySet()) {
                vars.putValue(e.getKey(), e.getValue());
            }
        }

        DmnDecisionTableResult result = DMN_ENGINE.evaluateDecisionTable(decision, vars);
        if (result == null || result.isEmpty()) {
            return new LinkedHashMap<>();
        }

        DmnDecisionRuleResult single = result.getSingleResult();
        Map<String, Object> out = new LinkedHashMap<>();
        // DmnDecisionRuleResult#getEntry(...) may throw NPE when the key is missing in
        // some versions,
        // so access via Map view first.
        if (single instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> m = (Map<String, Object>) single;

            if ("true".equalsIgnoreCase(System.getProperty("uengine.businessrule.debug"))) {
                System.out.println("[BusinessRuleEvaluator] DMN result keys=" + m.keySet());
            }

            Object outcome = m.get("outcome");
            if (outcome == null) {
                outcome = m.get("Outcome"); // fallback to label (some DMN exporters)
            }
            if (outcome != null) {
                out.put("outcome", String.valueOf(outcome));
            }

            Object note = m.get("note");
            if (note == null) {
                note = m.get("Note");
            }
            if (note != null) {
                out.put("note", String.valueOf(note));
            }
        } else {
            // fallback (best-effort)
            try {
                Object outcome = single.getEntry("outcome");
                if (outcome != null) {
                    out.put("outcome", String.valueOf(outcome));
                }
            } catch (Exception ignore) {
            }
        }

        // matchedRuleIndex 계산: DMN XML과 입력값을 사용하여 정확한 인덱스 계산
        int matchedRuleIndex = calculateMatchedRuleIndex(normalized, result, coercedInputs);
        out.put("matchedRuleIndex", matchedRuleIndex);

        return out;
    }

    private String extractDecisionId(String dmnXml) {
        // lightweight extraction without full XML parser: find '<decision id="...">'
        // (works for our stored DMN XML format)
        int idx = dmnXml.indexOf("<decision ");
        if (idx < 0) {
            return null;
        }
        int idIdx = dmnXml.indexOf("id=\"", idx);
        if (idIdx < 0) {
            return null;
        }
        int start = idIdx + "id=\"".length();
        int end = dmnXml.indexOf("\"", start);
        if (end < 0) {
            return null;
        }
        return dmnXml.substring(start, end);
    }

    private Map<String, Object> coerceInputsForDmn(String dmnXml, String decisionId, Map<String, Object> inputs) {
        if (inputs == null || inputs.isEmpty()) {
            return inputs;
        }

        Map<String, String> expectedTypes = extractInputTypeRefs(dmnXml);
        if (expectedTypes.isEmpty()) {
            return inputs; // no typing info, keep legacy behavior
        }

        Map<String, Object> out = new LinkedHashMap<>();
        for (Map.Entry<String, Object> e : inputs.entrySet()) {
            String key = e.getKey();
            Object raw = e.getValue();

            String typeRef = expectedTypes.get(key);
            if (typeRef == null) {
                out.put(key, raw);
                continue;
            }

            out.put(key, coerceValue(decisionId, key, typeRef, raw));
        }

        if ("true".equalsIgnoreCase(System.getProperty("uengine.businessrule.debug"))) {
            System.out.println("[BusinessRuleEvaluator] DMN input typeRefs=" + expectedTypes);
            System.out.println("[BusinessRuleEvaluator] DMN inputs(before)=" + inputs);
            System.out.println("[BusinessRuleEvaluator] DMN inputs(after)=" + out);
        }

        return out;
    }

    private Map<String, String> extractInputTypeRefs(String dmnXml) {
        Map<String, String> map = new HashMap<>();
        if (dmnXml == null || dmnXml.isBlank()) {
            return map;
        }

        // inputExpression contains the DMN input key in <text> and typeRef attribute
        // for expected type.
        // Example:
        // <inputExpression ... typeRef="number"><text>key_123</text></inputExpression>
        Pattern p = Pattern.compile(
                "<inputExpression\\b[^>]*?\\btypeRef\\s*=\\s*\"([^\"]+)\"[^>]*>\\s*<text>\\s*([^<\\s]+)\\s*</text>",
                Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(dmnXml);
        while (m.find()) {
            String typeRef = m.group(1);
            String key = m.group(2);
            if (typeRef == null || key == null) {
                continue;
            }
            typeRef = typeRef.trim().toLowerCase();
            key = key.trim();
            if (!typeRef.isEmpty() && !key.isEmpty()) {
                map.put(key, typeRef);
            }
        }

        return map;
    }

    private Object coerceValue(String decisionId, String inputKey, String typeRef, Object raw) {
        if (raw == null) {
            return null;
        }

        switch (typeRef) {
            case "number":
                if (raw instanceof Number) {
                    return raw;
                }
                if (raw instanceof BigDecimal) {
                    return raw;
                }
                if (raw instanceof String) {
                    String s = ((String) raw).trim();
                    if (s.isEmpty()) {
                        throw dmnTypeMismatch(decisionId, inputKey, typeRef, raw);
                    }
                    try {
                        return new BigDecimal(s);
                    } catch (Exception ex) {
                        throw dmnTypeMismatch(decisionId, inputKey, typeRef, raw);
                    }
                }
                throw dmnTypeMismatch(decisionId, inputKey, typeRef, raw);
            case "string":
                if (raw instanceof String) {
                    return raw;
                }
                return String.valueOf(raw);
            case "boolean":
                if (raw instanceof Boolean) {
                    return raw;
                }
                if (raw instanceof String) {
                    String s = ((String) raw).trim().toLowerCase();
                    if ("true".equals(s)) {
                        return Boolean.TRUE;
                    }
                    if ("false".equals(s)) {
                        return Boolean.FALSE;
                    }
                    throw dmnTypeMismatch(decisionId, inputKey, typeRef, raw);
                }
                throw dmnTypeMismatch(decisionId, inputKey, typeRef, raw);
            default:
                // unknown typeRef -> keep raw (compat)
                return raw;
        }
    }

    private ResponseStatusException dmnTypeMismatch(String decisionId, String inputKey, String expectedTypeRef,
            Object raw) {
        String rawType = raw != null ? raw.getClass().getName() : "null";
        String rawValue = String.valueOf(raw);
        String msg = "DMN_INPUT_TYPE_MISMATCH decisionId=" + decisionId
                + " inputKey=" + inputKey
                + " expectedType=" + expectedTypeRef
                + " rawType=" + rawType
                + " rawValue=" + rawValue;
        return new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, msg);
    }

    private String normalizeDmnXmlForCamunda(String xml) {
        if (xml == null) {
            return null;
        }
        // Camunda DMN model parser rejects id attribute on annotation/annotationEntry
        // for our stored DMN
        String normalized = xml;
        normalized = normalized.replaceAll("(<annotation\\b[^>]*?)\\s+id=\"[^\"]*\"([^>]*>)", "$1$2");
        normalized = normalized.replaceAll("(<annotationEntry\\b[^>]*?)\\s+id=\"[^\"]*\"([^>]*>)", "$1$2");
        return normalized;
    }

    private boolean matchesAll(JsonNode conditions, Map<String, Object> inputs) {
        for (JsonNode cond : conditions) {
            String key = text(cond.get("key"));
            if (key == null) {
                key = text(cond.get("item"));
            }
            String op = text(cond.get("operator"));
            JsonNode condValue = cond.get("value");

            Object inputValue = inputs != null ? inputs.get(key) : null;
            if (!match(op, inputValue, condValue)) {
                return false;
            }
        }
        return true;
    }

    private boolean match(String operator, Object inputValue, JsonNode condValue) {
        if (operator == null) {
            return false;
        }
        if ("-".equals(operator)) {
            return true; // wildcard
        }

        if (inputValue == null) {
            return false;
        }

        // numeric compare only (current rules use number)
        BigDecimal left = toBigDecimal(inputValue);
        BigDecimal right = condValue != null && condValue.isNumber() ? condValue.decimalValue() : null;
        if (right == null) {
            return false;
        }

        int cmp = left.compareTo(right);
        switch (operator) {
            case "gte":
                return cmp >= 0;
            case "lte":
                return cmp <= 0;
            case "gt":
                return cmp > 0;
            case "lt":
                return cmp < 0;
            case "eq":
                return cmp == 0;
            case "neq":
                return cmp != 0;
            default:
                return false;
        }
    }

    private BigDecimal toBigDecimal(Object v) {
        if (v instanceof BigDecimal) {
            return (BigDecimal) v;
        }
        if (v instanceof Number) {
            return new BigDecimal(((Number) v).toString());
        }
        try {
            return new BigDecimal(v.toString());
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    private Map<String, Object> toMap(JsonNode node) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (node == null || node.isNull()) {
            return map;
        }
        if (!node.isObject()) {
            map.put("value", node.asText());
            return map;
        }
        node.fields().forEachRemaining(e -> {
            JsonNode v = e.getValue();
            if (v == null || v.isNull()) {
                map.put(e.getKey(), null);
            } else if (v.isNumber()) {
                map.put(e.getKey(), v.numberValue());
            } else if (v.isBoolean()) {
                map.put(e.getKey(), v.booleanValue());
            } else {
                map.put(e.getKey(), v.asText());
            }
        });
        return map;
    }

    private String text(JsonNode node) {
        return node != null && node.isTextual() ? node.asText() : null;
    }

    /**
     * Calculate the index of the matched rule in DMN XML.
     * For FIRST hit policy, returns the index of the first matching rule (0-based).
     * 
     * This implementation parses DMN XML to extract rule conditions and evaluates
     * each rule in order to find the first matching rule.
     * 
     * @param dmnXml DMN XML string
     * @param result DMN evaluation result
     * @param inputs Input values used for evaluation
     * @return 0-based index of the matched rule, or 0 if cannot be determined
     */
    private int calculateMatchedRuleIndex(String dmnXml, DmnDecisionTableResult result, Map<String, Object> inputs) {
        if (dmnXml == null || result == null || result.isEmpty() || inputs == null) {
            return 0;
        }

        try {
            // DMN XML에서 규칙들을 순서대로 추출
            List<DmnRuleCondition> rules = extractRuleConditions(dmnXml);

            if (rules.isEmpty()) {
                return 0;
            }

            // 각 규칙을 순서대로 평가하여 첫 번째 매칭 규칙 찾기
            for (int i = 0; i < rules.size(); i++) {
                DmnRuleCondition rule = rules.get(i);
                if (evaluateRuleCondition(rule, inputs)) {
                    return i;
                }
            }

            // 매칭되는 규칙이 없는 경우 (이론적으로는 발생하지 않아야 함)
            return 0;
        } catch (Exception e) {
            // 에러 발생 시 기본값 0 반환
            if ("true".equalsIgnoreCase(System.getProperty("uengine.businessrule.debug"))) {
                System.out.println("[BusinessRuleEvaluator] Failed to calculate matchedRuleIndex: " + e.getMessage());
                e.printStackTrace();
            }
            return 0;
        }
    }

    /**
     * Extract rule IDs from DMN XML in order.
     * 
     * @param dmnXml DMN XML string
     * @return List of rule IDs in order
     */
    private List<String> extractRuleIds(String dmnXml) {
        List<String> ruleIds = new ArrayList<>();
        if (dmnXml == null || dmnXml.isBlank()) {
            return ruleIds;
        }

        // <decisionTable> 내부의 <rule> 요소 찾기
        int decisionTableStart = dmnXml.indexOf("<decisionTable");
        if (decisionTableStart < 0) {
            return ruleIds;
        }

        // <decisionTable> 태그 종료 위치 찾기
        int decisionTableEnd = dmnXml.indexOf(">", decisionTableStart);
        if (decisionTableEnd < 0) {
            return ruleIds;
        }

        // <decisionTable> 내부에서 <rule id="..."> 패턴 찾기
        String decisionTableContent = dmnXml.substring(decisionTableEnd + 1);
        Pattern rulePattern = Pattern.compile("<rule\\b[^>]*?\\bid\\s*=\\s*\"([^\"]+)\"", Pattern.CASE_INSENSITIVE);
        Matcher matcher = rulePattern.matcher(decisionTableContent);

        while (matcher.find()) {
            String ruleId = matcher.group(1);
            if (ruleId != null && !ruleId.trim().isEmpty()) {
                ruleIds.add(ruleId.trim());
            }
        }

        return ruleIds;
    }

    /**
     * Extract rule conditions from DMN XML.
     * Each rule contains input entries (conditions) and output entries (results).
     */
    private List<DmnRuleCondition> extractRuleConditions(String dmnXml) {
        List<DmnRuleCondition> rules = new ArrayList<>();
        if (dmnXml == null || dmnXml.isBlank()) {
            return rules;
        }

        // <decisionTable> 내부의 <rule> 요소 찾기
        int decisionTableStart = dmnXml.indexOf("<decisionTable");
        if (decisionTableStart < 0) {
            return rules;
        }

        // <decisionTable> 태그 종료 위치 찾기
        int decisionTableEnd = dmnXml.indexOf(">", decisionTableStart);
        if (decisionTableEnd < 0) {
            return rules;
        }

        // input 정보 추출 (inputExpression의 text 값이 입력 키)
        Map<Integer, String> inputKeys = extractInputKeys(dmnXml);

        // <decisionTable> 내부에서 <rule> 요소 찾기
        String decisionTableContent = dmnXml.substring(decisionTableEnd + 1);
        Pattern rulePattern = Pattern.compile("<rule\\b[^>]*?id\\s*=\\s*\"([^\"]+)\"", Pattern.CASE_INSENSITIVE);
        Matcher ruleMatcher = rulePattern.matcher(decisionTableContent);

        int ruleIndex = 0;
        while (ruleMatcher.find()) {
            String ruleId = ruleMatcher.group(1);
            int ruleStart = ruleMatcher.start();

            // rule 태그 종료 위치 찾기
            int ruleEnd = findRuleEnd(decisionTableContent, ruleStart);
            if (ruleEnd < 0) {
                continue;
            }

            String ruleContent = decisionTableContent.substring(ruleStart, ruleEnd);

            // inputEntry 추출
            List<String> inputEntries = extractInputEntries(ruleContent);

            // outputEntry 추출
            String outputValue = extractOutputEntry(ruleContent);

            DmnRuleCondition rule = new DmnRuleCondition(ruleId, ruleIndex, inputKeys, inputEntries, outputValue);
            rules.add(rule);
            ruleIndex++;
        }

        return rules;
    }

    /**
     * Extract input keys from DMN XML.
     * Returns map of input index -> input key (from inputExpression text).
     */
    private Map<Integer, String> extractInputKeys(String dmnXml) {
        Map<Integer, String> inputKeys = new LinkedHashMap<>();
        if (dmnXml == null || dmnXml.isBlank()) {
            return inputKeys;
        }

        // <input> 요소 찾기
        Pattern inputPattern = Pattern.compile(
                "<input\\b[^>]*?id\\s*=\\s*\"input_(\\d+)\"[^>]*>.*?<inputExpression[^>]*>\\s*<text>\\s*([^<\\s]+)\\s*</text>",
                Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
        Matcher matcher = inputPattern.matcher(dmnXml);

        while (matcher.find()) {
            try {
                int inputIndex = Integer.parseInt(matcher.group(1));
                String inputKey = matcher.group(2).trim();
                inputKeys.put(inputIndex, inputKey);
            } catch (Exception e) {
                // ignore
            }
        }

        return inputKeys;
    }

    /**
     * Extract input entries from a rule element.
     */
    private List<String> extractInputEntries(String ruleContent) {
        List<String> entries = new ArrayList<>();
        if (ruleContent == null || ruleContent.isBlank()) {
            return entries;
        }

        Pattern entryPattern = Pattern.compile("<inputEntry[^>]*>\\s*<text>\\s*(.*?)\\s*</text>",
                Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
        Matcher matcher = entryPattern.matcher(ruleContent);

        while (matcher.find()) {
            String entry = matcher.group(1).trim();
            // XML 엔티티 디코딩
            entry = entry.replace("&gt;", ">").replace("&lt;", "<").replace("&quot;", "\"").replace("&amp;", "&");
            entries.add(entry);
        }

        return entries;
    }

    /**
     * Extract output entry from a rule element.
     */
    private String extractOutputEntry(String ruleContent) {
        if (ruleContent == null || ruleContent.isBlank()) {
            return null;
        }

        Pattern entryPattern = Pattern.compile("<outputEntry[^>]*>\\s*<text>\\s*(.*?)\\s*</text>",
                Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
        Matcher matcher = entryPattern.matcher(ruleContent);

        if (matcher.find()) {
            String entry = matcher.group(1).trim();
            // XML 엔티티 디코딩 및 따옴표 제거
            entry = entry.replace("&gt;", ">").replace("&lt;", "<").replace("&quot;", "\"").replace("&amp;", "&");
            // 따옴표로 감싸진 문자열에서 따옴표 제거
            if (entry.startsWith("\"") && entry.endsWith("\"")) {
                entry = entry.substring(1, entry.length() - 1);
            }
            return entry;
        }

        return null;
    }

    /**
     * Find the end position of a rule element.
     */
    private int findRuleEnd(String content, int ruleStart) {
        int depth = 0;
        int pos = ruleStart;
        boolean inTag = false;

        while (pos < content.length()) {
            char c = content.charAt(pos);

            if (c == '<') {
                if (pos + 1 < content.length() && content.charAt(pos + 1) == '/') {
                    // closing tag
                    depth--;
                    if (depth == 0
                            && content.substring(pos, Math.min(pos + 6, content.length())).startsWith("</rule")) {
                        // find end of closing tag
                        int endPos = content.indexOf(">", pos);
                        return endPos >= 0 ? endPos + 1 : -1;
                    }
                } else {
                    // opening tag
                    if (pos == ruleStart || depth > 0) {
                        depth++;
                    }
                }
                inTag = true;
            } else if (c == '>') {
                inTag = false;
            }

            pos++;
        }

        return -1;
    }

    /**
     * Evaluate a single rule condition against input values.
     */
    private boolean evaluateRuleCondition(DmnRuleCondition rule, Map<String, Object> inputs) {
        if (rule == null || inputs == null) {
            return false;
        }

        List<String> inputEntries = rule.getInputEntries();
        Map<Integer, String> inputKeys = rule.getInputKeys();

        if (inputEntries.size() != inputKeys.size()) {
            return false;
        }

        // 각 inputEntry를 평가
        int inputIndex = 0;
        for (String inputEntry : inputEntries) {
            String inputKey = inputKeys.get(inputIndex + 1); // input index는 1부터 시작
            if (inputKey == null) {
                return false;
            }

            Object inputValue = inputs.get(inputKey);
            if (!evaluateInputEntry(inputEntry, inputValue)) {
                return false;
            }

            inputIndex++;
        }

        return true;
    }

    /**
     * Evaluate a single input entry condition.
     * Supports: ">=", "<=", ">", "<", "=", "-" (wildcard)
     */
    private boolean evaluateInputEntry(String entry, Object inputValue) {
        if (entry == null) {
            return false;
        }

        entry = entry.trim();

        // 와일드카드 "-"는 항상 매칭
        if ("-".equals(entry)) {
            return true;
        }

        if (inputValue == null) {
            return false;
        }

        // 숫자 비교
        BigDecimal inputNum = toBigDecimal(inputValue);

        // 패턴 매칭: ">= 600", "<= 599", "> 100", "< 50", "= 100"
        Pattern comparisonPattern = Pattern.compile("(>=|<=|>|<|=)\\s*([+-]?\\d+(?:\\.\\d+)?)");
        Matcher matcher = comparisonPattern.matcher(entry);

        if (matcher.find()) {
            String operator = matcher.group(1);
            BigDecimal compareValue = new BigDecimal(matcher.group(2));

            int cmp = inputNum.compareTo(compareValue);

            switch (operator) {
                case ">=":
                    return cmp >= 0;
                case "<=":
                    return cmp <= 0;
                case ">":
                    return cmp > 0;
                case "<":
                    return cmp < 0;
                case "=":
                    return cmp == 0;
                default:
                    return false;
            }
        }

        // 문자열 비교 (fallback)
        String inputStr = String.valueOf(inputValue);
        return entry.equals(inputStr);
    }

    /**
     * Inner class to represent a DMN rule condition.
     */
    private static class DmnRuleCondition {
        private final String ruleId;
        private final int index;
        private final Map<Integer, String> inputKeys;
        private final List<String> inputEntries;
        private final String outputValue;

        public DmnRuleCondition(String ruleId, int index, Map<Integer, String> inputKeys,
                List<String> inputEntries, String outputValue) {
            this.ruleId = ruleId;
            this.index = index;
            this.inputKeys = inputKeys;
            this.inputEntries = inputEntries;
            this.outputValue = outputValue;
        }

        public String getRuleId() {
            return ruleId;
        }

        public int getIndex() {
            return index;
        }

        public Map<Integer, String> getInputKeys() {
            return inputKeys;
        }

        public List<String> getInputEntries() {
            return inputEntries;
        }

        public String getOutputValue() {
            return outputValue;
        }
    }
}
