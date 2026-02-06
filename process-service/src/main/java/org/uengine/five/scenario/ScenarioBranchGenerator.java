package org.uengine.five.scenario;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.uengine.contexts.EventSynchronization;
import org.uengine.five.businessrule.BusinessRuleEvaluator;
import org.uengine.five.businessrule.BusinessRuleStore;
import org.uengine.five.service.DefinitionServiceUtil;
import org.uengine.kernel.Activity;
import org.uengine.kernel.Condition;
import org.uengine.kernel.Evaluate;
import org.uengine.kernel.ParameterContext;
import org.uengine.kernel.ProcessDefinition;
import org.uengine.kernel.bpmn.BusinessRuleTask;
import org.uengine.kernel.bpmn.Event;
import org.uengine.kernel.bpmn.Gateway;
import org.uengine.kernel.bpmn.SequenceFlow;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 4단계: DMN 규칙별 분기 시나리오 생성.
 * BusinessRuleTask + DMN 규칙 목록으로 규칙별 시나리오 생성.
 * 다음 액티비티가 HumanActivity일 때만 시나리오 생성.
 */
@Component
public class ScenarioBranchGenerator {

    private final DefinitionServiceUtil definitionServiceUtil;
    private final BusinessRuleStore businessRuleStore;
    private final BusinessRuleEvaluator businessRuleEvaluator;

    private static final Pattern COMPARISON_PATTERN = Pattern.compile("(>=|<=|>|<|=)\\s*([+-]?\\d+(?:\\.\\d+)?)");

    public ScenarioBranchGenerator(
            @Autowired DefinitionServiceUtil definitionServiceUtil,
            @Autowired BusinessRuleStore businessRuleStore,
            @Autowired BusinessRuleEvaluator businessRuleEvaluator) {
        this.definitionServiceUtil = definitionServiceUtil;
        this.businessRuleStore = businessRuleStore;
        this.businessRuleEvaluator = businessRuleEvaluator;
    }

    /**
     * 해당 프로세스 내 BusinessRuleTask에 대해 DMN 규칙별 분기 시나리오 생성.
     *
     * @param processDefinitionId 프로세스 정의 ID
     * @return DMN 규칙별 시나리오 목록
     */
    public List<Scenario> generateDmnBranchScenarios(String processDefinitionId) throws Exception {
        Object defObj = definitionServiceUtil.getDefinition(processDefinitionId, null);
        if (!(defObj instanceof ProcessDefinition)) {
            throw new IllegalArgumentException("ProcessDefinition을 찾을 수 없습니다: " + processDefinitionId);
        }
        ProcessDefinition processDefinition = (ProcessDefinition) defObj;

        List<Scenario> scenarios = new ArrayList<>();

        List<Activity> childActivities = processDefinition.getChildActivities();
        if (childActivities == null) {
            return scenarios;
        }

        for (Activity activity : childActivities) {
            if (!(activity instanceof BusinessRuleTask)) {
                continue;
            }
            BusinessRuleTask brTask = (BusinessRuleTask) activity;
            String ruleId = brTask.getBusinessRuleId();
            if (ruleId == null || ruleId.isEmpty()) {
                continue;
            }

            BusinessRuleStore.BusinessRuleFile file;
            try {
                file = businessRuleStore.loadOrThrow(ruleId);
            } catch (Exception e) {
                continue;
            }

            List<BusinessRuleEvaluator.DmnRuleCondition> rules = businessRuleEvaluator
                    .getRuleConditions(file.getRuleJson());
            if (rules.isEmpty()) {
                continue;
            }

            for (int i = 0; i < rules.size(); i++) {
                BusinessRuleEvaluator.DmnRuleCondition rule = rules.get(i);
                Scenario scenario = buildScenarioForRule(processDefinition, processDefinitionId, brTask, rule, i);
                if (scenario != null) {
                    scenarios.add(scenario);
                }
            }
        }

        return scenarios;
    }

    /**
     * Given은 when.activityId(현재 태스크)의 입력만 반영.
     * 이 시나리오의 when은 HumanActivity(승인/거절)를 가리키며, 해당 태스크에는 매핑이 없으므로 given은 비운다.
     */
    private Scenario buildScenarioForRule(ProcessDefinition processDefinition, String processDefinitionId,
            BusinessRuleTask brTask, BusinessRuleEvaluator.DmnRuleCondition rule, int ruleIndex) {
        String outputValue = rule.getOutputValue();
        if (outputValue == null)
            outputValue = "";
        String bpmnValue = mapDmnOutcomeToBpmn(outputValue);

        List<SequenceFlow> outgoing = brTask.getOutgoingSequenceFlows();
        if (outgoing == null || outgoing.isEmpty()) {
            return null;
        }
        SequenceFlow firstFlow = outgoing.get(0);
        Activity nextActivity = firstFlow.getTargetActivity();
        if (nextActivity == null && firstFlow.getTargetRef() != null) {
            nextActivity = processDefinition.getActivity(firstFlow.getTargetRef());
        }
        if (!(nextActivity instanceof Gateway)) {
            return null;
        }
        Gateway gateway = (Gateway) nextActivity;
        List<SequenceFlow> gatewayFlows = gateway.getOutgoingSequenceFlows();
        if (gatewayFlows == null) {
            return null;
        }

        Activity targetActivity = null;
        for (SequenceFlow flow : gatewayFlows) {
            Condition cond = flow.getCondition();
            if (cond instanceof Evaluate) {
                Evaluate ev = (Evaluate) cond;
                Object evVal = ev.getValue();
                String evValStr = evVal != null ? evVal.toString() : "";
                if (evValStr.equals(bpmnValue)) {
                    Activity t = flow.getTargetActivity();
                    if (t == null && flow.getTargetRef() != null) {
                        t = processDefinition.getActivity(flow.getTargetRef());
                    }
                    if (t != null) {
                        targetActivity = t;
                        break;
                    }
                }
            }
        }
        if (targetActivity == null) {
            return null;
        }
        // Running 시나리오는 해당 태스크에 바운더리 이벤트가 붙어 있을 때만 추가
        if (!hasBoundaryEventAttached(processDefinition, targetActivity)) {
            return null;
        }

        Map<String, Object> when = new HashMap<>();
        when.put("status", "Running");
        when.put("activityId", targetActivity.getTracingTag());
        when.put("activityName",
                targetActivity.getName() != null ? targetActivity.getName() : targetActivity.getTracingTag());

        Scenario scenario = new Scenario();
        scenario.setProcessDefinitionId(processDefinitionId);
        String brName = brTask.getName() != null ? brTask.getName() : brTask.getTracingTag();
        scenario.setName("DMN_" + brName.replaceAll("[^a-zA-Z0-9가-힣_]", "_") + "_rule_" + ruleIndex + "_"
                + (bpmnValue != null ? bpmnValue : "out"));
        scenario.setGiven(new HashMap<>());
        scenario.setWhen(when);
        return scenario;
    }

    /**
     * 해당 액티비티에 바운더리 이벤트가 붙어 있는지 여부.
     * 프로세스 정의의 자식 중 Event이며 attachedToRef가 주어진 액티비티를 가리키면 true.
     */
    private boolean hasBoundaryEventAttached(ProcessDefinition processDefinition, Activity activity) {
        if (activity == null || activity.getTracingTag() == null) {
            return false;
        }
        List<Activity> children = processDefinition.getChildActivities();
        if (children == null) {
            return false;
        }
        String targetTag = activity.getTracingTag();
        for (Activity child : children) {
            if (child instanceof Event) {
                String attachedTo = ((Event) child).getAttachedToRef();
                if (targetTag.equals(attachedTo)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * DMN input key(예: key_1768...) → 프로세스 변수명(예: 신용도) 매핑. BR의 mappingContext에서 추출.
     */
    private Map<String, String> buildDmnInputKeyToProcessVar(BusinessRuleTask brTask) {
        Map<String, String> out = new LinkedHashMap<>();
        EventSynchronization sync = brTask.getEventSynchronization();
        if (sync == null || sync.getMappingContext() == null || sync.getMappingContext().getMappingElements() == null) {
            return out;
        }
        for (ParameterContext pc : sync.getMappingContext().getMappingElements()) {
            if (pc == null || !ParameterContext.DIRECTION_OUT.equals(pc.getDirection())) {
                continue;
            }
            String processVar = null;
            if (pc.getVariable() != null && pc.getVariable().getName() != null) {
                String vn = pc.getVariable().getName();
                if (!vn.contains("[businessRule]") && !vn.contains(".r_")) {
                    processVar = vn;
                }
            }
            if (processVar == null && pc.getArgument() != null && pc.getArgument().getText() != null) {
                processVar = pc.getArgument().getText();
            }
            if (processVar == null || processVar.isEmpty()) {
                continue;
            }
            String arg = pc.getArgument() != null ? pc.getArgument().getText() : null;
            if (arg != null && arg.contains("in_key_")) {
                int i = arg.indexOf("in_key_");
                String suffix = arg.substring(i + 7);
                if (suffix.contains(".")) {
                    suffix = suffix.substring(0, suffix.indexOf("."));
                }
                String dmnKey = "key_" + suffix;
                out.put(dmnKey, processVar);
            }
        }
        return out;
    }

    /** DMN outcome(approve/reject) → BPMN Gateway 조건값(승인/거절) */
    private static String mapDmnOutcomeToBpmn(String outputValue) {
        if (outputValue == null)
            return null;
        String s = outputValue.trim();
        if ("approve".equalsIgnoreCase(s))
            return "승인";
        if ("reject".equalsIgnoreCase(s))
            return "거절";
        return s;
    }

    /** inputEntry(예: ">=700", "-")에서 해당 규칙이 매칭되도록 하는 샘플 값 반환 */
    private static Object sampleValueFromInputEntry(String entry) {
        if (entry == null)
            return null;
        entry = entry.trim();
        if ("-".equals(entry))
            return null;
        Matcher m = COMPARISON_PATTERN.matcher(entry);
        if (!m.find())
            return entry;
        String op = m.group(1);
        BigDecimal num = new BigDecimal(m.group(2));
        switch (op) {
            case ">=":
            case "=":
                return num.intValue() == num.doubleValue() ? num.intValue() : num.doubleValue();
            case "<=":
                return num.intValue() == num.doubleValue() ? num.intValue() : num.doubleValue();
            case ">":
                BigDecimal plus = num.add(BigDecimal.ONE);
                return plus.intValue() == plus.doubleValue() ? plus.intValue() : plus.doubleValue();
            case "<":
                BigDecimal minus = num.subtract(BigDecimal.ONE);
                return minus.intValue() == minus.doubleValue() ? minus.intValue() : minus.doubleValue();
            default:
                return num.intValue() == num.doubleValue() ? num.intValue() : num.doubleValue();
        }
    }
}
