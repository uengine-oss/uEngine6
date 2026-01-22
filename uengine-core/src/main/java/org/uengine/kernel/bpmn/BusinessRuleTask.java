package org.uengine.kernel.bpmn;

import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAlias;

import org.uengine.kernel.ParameterContext;
import org.uengine.kernel.ProcessInstance;
import org.uengine.kernel.ResultPayload;
import org.uengine.kernel.ReceiveActivity;

/**
 * BPMN Business Rule Task.
 *
 * <p>
 * This class mainly exists to support BPMN parsing/serialization where
 * {@code <bpmn:businessRuleTask/>} is mapped to
 * {@code org.uengine.kernel.bpmn.BusinessRuleTask}.
 * Runtime behavior (rule evaluation) is handled by upper layers/services.
 * </p>
 */
public class BusinessRuleTask extends ReceiveActivity {

    public BusinessRuleTask() {
        super();
        // ReceiveActivity 기본 생성자는 Activity name을 "Receive"로 잡습니다.
        // BusinessRuleTask는 BPMN 표준 요소 이름으로 맞춥니다.
        setName("Business Rule");
    }

    /**
     * Prefer this field for rule identification (current BPMN format).
     * Example JSON:
     * {"businessRuleId":"...","_type":"org.uengine.kernel.bpmn.BusinessRuleTask"}
     */
    @JsonAlias({ "businessRuleId", "business_rule_id", "businessRuleID" })
    private String businessRuleId;

    public String getBusinessRuleId() {
        String id = normalizeId(businessRuleId);
        if (id != null) {
            return id;
        }

        // Last fallback: infer from mappingElements like
        // [businessRule].r_<id>.in_... or [businessRule].r_<id>.out_...
        return inferRuleIdFromMappingElements();
    }

    public void setBusinessRuleId(String businessRuleId) {
        this.businessRuleId = businessRuleId;
    }

    private static String normalizeId(String raw) {
        if (raw == null) {
            return null;
        }
        String s = raw.trim();
        return s.isEmpty() ? null : s;
    }

    private String inferRuleIdFromMappingElements() {
        if (getEventSynchronization() == null || getEventSynchronization().getMappingContext() == null) {
            return null;
        }
        ParameterContext[] params = getEventSynchronization().getMappingContext().getMappingElements();
        if (params == null || params.length == 0) {
            return null;
        }

        for (ParameterContext p : params) {
            if (p == null || p.getArgument() == null) {
                continue;
            }
            String arg = p.getArgument().getText();
            if (arg == null) {
                continue;
            }
            // Expected pattern contains ".r_<id>."
            int rIdx = arg.indexOf(".r_");
            if (rIdx < 0) {
                continue;
            }
            int idStart = rIdx + ".r_".length();
            int dotAfter = arg.indexOf('.', idStart);
            if (dotAfter <= idStart) {
                continue;
            }
            String inferred = arg.substring(idStart, dotAfter);
            inferred = normalizeId(inferred);
            if (inferred != null) {
                return inferred;
            }
        }

        return null;
    }

    /**
     * BusinessRuleTask는 ReceiveActivity 흐름(메시지 리스너 등록 → 메시지 수신 시 완료)을 그대로 사용합니다.
     *
     * 메시지 이름은 designer가 세팅하는 message가 아니라, task 자체에서 결정합니다.
     */
    @Override
    public void setMessage(String value) {
        // ignore designer-provided message
    }

    @Override
    public String getMessage() {
        // Prefer businessRuleId if present (stable across re-tagging)
        String businessRuleId = getBusinessRuleId();
        if (businessRuleId != null) {
            return "businessRule:" + businessRuleId;
        }

        // Fallback to tracingTag (unique within definition)
        String tag = getTracingTag();
        if (tag != null && !tag.trim().isEmpty()) {
            return "businessRule:" + tag.trim();
        }

        // Last resort: use name (avoid collisions from constant topic)
        String name = getName();
        if (name != null && !name.trim().isEmpty()) {
            return "businessRule:" + name.trim();
        }

        return "businessRule:" + Integer.toHexString(System.identityHashCode(this));
    }

    public Map<String, Object> buildRuleInputs(ProcessInstance instance) throws Exception {
        Map<String, Object> inputs = new LinkedHashMap<>();
        if (instance == null || getEventSynchronization() == null
                || getEventSynchronization().getMappingContext() == null) {
            return inputs;
        }

        ParameterContext[] params = getEventSynchronization().getMappingContext().getMappingElements();
        if (params == null || params.length == 0) {
            return inputs;
        }

        for (ParameterContext p : params) {
            if (p == null || p.getArgument() == null || p.getVariable() == null) {
                continue;
            }
            String arg = p.getArgument().getText();
            String src = p.getVariable().getName();
            if (arg == null || src == null) {
                continue;
            }

            int inIdx = arg.indexOf(".in_");
            if (inIdx < 0) {
                continue;
            }
            String key = arg.substring(inIdx + ".in_".length());
            if (key.isEmpty()) {
                continue;
            }

            Object val = instance.getBeanProperty(src);
            inputs.put(key, val);
        }

        return inputs;
    }

    /**
     * Apply business-rule outputs to process variables, based on mappingElements.
     *
     * @return true if at least one output was applied
     */
    public boolean applyRuleOutputs(ProcessInstance instance, Map<String, ?> outputs) throws Exception {
        if (instance == null || outputs == null || outputs.isEmpty()) {
            return false;
        }
        if (getEventSynchronization() == null || getEventSynchronization().getMappingContext() == null) {
            return false;
        }

        ParameterContext[] params = getEventSynchronization().getMappingContext().getMappingElements();
        if (params == null || params.length == 0) {
            return false;
        }

        boolean applied = false;
        for (ParameterContext p : params) {
            if (p == null || p.getArgument() == null || p.getVariable() == null) {
                continue;
            }
            String targetVar = p.getArgument().getText();
            String srcVar = p.getVariable().getName();
            if (targetVar == null || srcVar == null) {
                continue;
            }
            if (targetVar.startsWith("[")) {
                continue;
            }

            int outIdx = srcVar.indexOf(".out_");
            if (outIdx < 0) {
                continue;
            }
            String outKey = srcVar.substring(outIdx + ".out_".length());
            if (outKey.isEmpty()) {
                continue;
            }

            Object v = outputs.get(outKey);
            if (v == null) {
                continue; // don't overwrite with null
            }

            instance.setBeanProperty(targetVar, String.valueOf(v));
            applied = true;
        }

        return applied;
    }

    @Override
    public Map<String, Object> getMappingInValues(ProcessInstance instance) throws Exception {
        // BusinessRuleTask는 ReceiveActivity의 attributes 기반 매핑을 따르지 않고,
        // mappingElements(.in_ 규칙) 기반으로 입력을 구성합니다. (Null-safe)
        return buildRuleInputs(instance);
    }

    /**
     * ReceiveActivity는 payload + mappingElements 조건이 안 맞으면 listener 제거가 누락될 수 있습니다.
     * BusinessRuleTask는 service에서 Map payload를 보내는 전제를 두고,
     * payload 타입에 맞춰 저장 후 반드시 listener 제거 + 완료 처리를 합니다.
     */
    @Override
    protected void onReceive(ProcessInstance instance, Object payload) throws Exception {
        instance.addDebugInfo(this);

        if (payload instanceof ResultPayload) {
            savePayload(instance, (ResultPayload) payload);
        } else if (payload instanceof java.util.Map) {
            @SuppressWarnings("unchecked")
            Map<String, ?> mapPayload = (Map<String, ?>) payload;
            applyRuleOutputs(instance, mapPayload);
        }

        // 항상 listener 제거
        try {
            instance.removeMessageListener(getMessage(), getTracingTag());
        } catch (Exception ignore) {
        }

        fireComplete(instance);
    }
}
