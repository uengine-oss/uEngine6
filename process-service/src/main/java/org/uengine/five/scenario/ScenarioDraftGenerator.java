package org.uengine.five.scenario;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.uengine.contexts.EventSynchronization;
import org.uengine.contexts.MappingContext;
import org.uengine.five.businessrule.BusinessRuleEvaluator;
import org.uengine.five.businessrule.BusinessRuleStore;
import org.uengine.five.service.DefinitionServiceUtil;
import org.uengine.kernel.Activity;
import org.uengine.kernel.Condition;
import org.uengine.kernel.Evaluate;
import org.uengine.kernel.Otherwise;
import org.uengine.kernel.HumanActivity;
import org.uengine.kernel.ParameterContext;
import org.uengine.kernel.ProcessDefinition;
import org.uengine.kernel.ProcessVariable;
import org.uengine.kernel.bpmn.BusinessRuleTask;
import org.uengine.kernel.bpmn.Gateway;
import org.uengine.kernel.bpmn.SequenceFlow;
import org.uengine.kernel.bpmn.StartEvent;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 시나리오 초안 자동 생성 (Given/When만).
 * BPMN에서 UserTask별 시나리오 초안 생성.
 * <ul>
 *   <li><b>Given</b>: 직접 입력(arguments) 또는 form 타입 변수 중 매핑에서 찾을 수 없는 경우만 포함.</li>
 *   <li><b>When.payload</b>: 직접 입력(arguments) 또는 form 타입 변수가 있을 때만 payloadMapping 포함.</li>
 * </ul>
 * HumanActivity에 mapping이 없거나 해당 항목이 없으면 payloadMapping을 when에 넣지 않음 → Runner가 빈 payload로 Complete 처리.
 * 트랜스포머/스크립트 구간은 정적 분석하지 않음.
 */
@Component
public class ScenarioDraftGenerator {

    private static final Pattern COMPARISON_PATTERN = Pattern.compile("(>=|<=|>|<|=)\\s*([+-]?\\d+(?:\\.\\d+)?)");

    private final DefinitionServiceUtil definitionServiceUtil;
    private final BusinessRuleStore businessRuleStore;
    private final BusinessRuleEvaluator businessRuleEvaluator;

    @Autowired
    public ScenarioDraftGenerator(
            DefinitionServiceUtil definitionServiceUtil,
            @Autowired(required = false) BusinessRuleStore businessRuleStore,
            @Autowired(required = false) BusinessRuleEvaluator businessRuleEvaluator) {
        this.definitionServiceUtil = definitionServiceUtil;
        this.businessRuleStore = businessRuleStore;
        this.businessRuleEvaluator = businessRuleEvaluator;
    }

    /**
     * 해당 프로세스의 UserTask별 시나리오 초안 생성 (2단계).
     * 옵션으로 일반 Gateway 분기 시나리오 포함 (3단계).
     *
     * @param processDefinitionId 프로세스 정의 ID
     * @param includeGatewayBranches true면 Gateway 조건별 분기 시나리오 추가
     * @return 생성된 시나리오 초안 목록
     */
    public List<Scenario> generateDrafts(String processDefinitionId, boolean includeGatewayBranches) throws Exception {
        // BPMN 로드
        Object defObj = definitionServiceUtil.getDefinition(processDefinitionId, null);
        if (!(defObj instanceof ProcessDefinition)) {
            throw new IllegalArgumentException("ProcessDefinition을 찾을 수 없습니다: " + processDefinitionId);
        }
        ProcessDefinition processDefinition = (ProcessDefinition) defObj;

        List<Scenario> scenarios = new ArrayList<>();

        // HumanActivity 필터 (무한/과도 반복 방지: 활동 수 상한)
        List<Activity> childActivities = processDefinition.getChildActivities();
        if (childActivities == null) {
            return scenarios;
        }
        // ProcessDefinition.getActivity() 첫 호출 시 registerToProcessDefinition()이 트리거됨.
        // 미리 한 번 호출해 두면 이후 getActivity()는 O(1)로 동작해 순환/과도 순회를 막을 수 있음.
        if (!childActivities.isEmpty()) {
            Activity first = childActivities.get(0);
            if (first != null && first.getTracingTag() != null) {
                processDefinition.getActivity(first.getTracingTag());
            }
        }
        final int maxActivities = 500;
        final int size = Math.min(childActivities.size(), maxActivities);

        // 블록 경계 = HumanActivity → HumanActivity. 경로 탐색 후 블록별 시나리오 생성.
        Activity startActivity = findStartActivity(processDefinition, childActivities, size);
        if (startActivity != null) {
            List<List<HumanActivity>> paths = enumeratePathsFromStart(processDefinition, startActivity, new ArrayList<>(), new java.util.HashSet<>(), maxActivities);
            for (List<HumanActivity> path : paths) {
                if (path.isEmpty()) continue;
                List<Scenario> forPath = createScenariosForHumanActivityPath(processDefinition, processDefinitionId, path);
                if (forPath != null) scenarios.addAll(forPath);
            }
        } else {
            // StartEvent 미발견 시 fallback: HumanActivity별 시나리오 생성
            for (int i = 0; i < size; i++) {
                Activity activity = childActivities.get(i);
                if (activity instanceof HumanActivity) {
                    List<Scenario> forTask = createScenariosForUserTask(processDefinition, processDefinitionId, (HumanActivity) activity);
                    if (forTask != null) scenarios.addAll(forTask);
                }
            }
        }

        // 3단계: 일반 Gateway 분기 시나리오 (해당 flow 조건 변수만 given에 포함)
        if (includeGatewayBranches) {
            List<Scenario> gatewayScenarios = generateGatewayBranchScenarios(processDefinition, processDefinitionId);
            scenarios.addAll(gatewayScenarios);
        }

        return scenarios;
    }

    /**
     * 2단계 호환: Gateway 분기 없이 UserTask별 시나리오만 생성
     */
    public List<Scenario> generateDrafts(String processDefinitionId) throws Exception {
        return generateDrafts(processDefinitionId, false);
    }

    /** StartEvent의 첫 outgoing flow 대상 액티비티 반환. childActivities 전체에서 StartEvent 탐색. */
    private Activity findStartActivity(ProcessDefinition processDefinition, List<Activity> childActivities, int size) {
        int limit = Math.min(size, childActivities != null ? childActivities.size() : 0);
        for (int i = 0; i < limit; i++) {
            Activity a = childActivities.get(i);
            if (a instanceof StartEvent) {
                List<SequenceFlow> out = a.getOutgoingSequenceFlows();
                if (out != null && !out.isEmpty()) {
                    SequenceFlow first = out.get(0);
                    Activity target = first.getTargetActivity();
                    if (target == null && first.getTargetRef() != null)
                        target = processDefinition.getActivity(first.getTargetRef());
                    return target;
                }
                return null;
            }
        }
        return null;
    }

    /**
     * 시작 액티비티부터 DFS로 HumanActivity 경로 수집. 블록 = HumanActivity → HumanActivity.
     * 경로에 HumanActivity 추가 시 path에 넣고, Gateway 분기마다 새 경로로 재귀.
     */
    private List<List<HumanActivity>> enumeratePathsFromStart(ProcessDefinition processDefinition, Activity activity,
            List<HumanActivity> currentPath, java.util.Set<String> visitedInPath, int maxDepth) {
        List<List<HumanActivity>> result = new ArrayList<>();
        if (maxDepth <= 0 || activity == null) return result;
        String tag = activity.getTracingTag();
        if (tag != null && visitedInPath.contains(tag)) return result; // cycle

        boolean isHuman = activity instanceof HumanActivity;
        if (isHuman) {
            currentPath.add((HumanActivity) activity);
            if (tag != null) visitedInPath.add(tag);
            result.add(new ArrayList<>(currentPath));
        }

        List<SequenceFlow> out = activity.getOutgoingSequenceFlows();
        if (out != null && !out.isEmpty()) {
            for (SequenceFlow flow : out) {
                Activity next = flow.getTargetActivity();
                if (next == null && flow.getTargetRef() != null)
                    next = processDefinition.getActivity(flow.getTargetRef());
                if (next != null)
                    result.addAll(enumeratePathsFromStart(processDefinition, next, currentPath, visitedInPath, maxDepth - 1));
            }
        }

        if (isHuman && tag != null) {
            visitedInPath.remove(tag);
            currentPath.remove(currentPath.size() - 1);
        }
        return result;
    }

    /**
     * HumanActivity 경로에 대해 시나리오 생성. 경로 1개면 createScenariosForUserTask, 경로 2개 이상이면 when.steps로 한 시나리오 생성.
     */
    private List<Scenario> createScenariosForHumanActivityPath(ProcessDefinition processDefinition, String processDefinitionId, List<HumanActivity> path) {
        if (path == null || path.isEmpty()) return null;
        if (path.size() == 1) {
            return createScenariosForUserTask(processDefinition, processDefinitionId, path.get(0));
        }
        // 경로 2개 이상: when.steps
        Scenario scenario = new Scenario();
        scenario.setProcessDefinitionId(processDefinitionId);
        List<Map<String, Object>> steps = new ArrayList<>();
        StringBuilder nameBuilder = new StringBuilder("Block");
        Map<String, Object> given = new HashMap<>();
        for (HumanActivity ha : path) {
            Map<String, Object> step = new HashMap<>();
            step.put("activityId", ha.getTracingTag());
            step.put("activityName", ha.getName() != null ? ha.getName() : ha.getTracingTag());
            step.put("status", "Completed");
            Map<String, Object> payload = buildPayloadFromHumanActivity(processDefinition, ha);
            if (!payload.isEmpty()) {
                step.put("payloadMapping", payload);
            }
            steps.add(step);
            nameBuilder.append("_").append(ha.getName() != null ? ha.getName().replaceAll("[^a-zA-Z0-9가-힣_]", "_") : ha.getTracingTag());
        }
        scenario.setName(nameBuilder.toString());
        scenario.setGiven(given);
        Map<String, Object> when = new HashMap<>();
        when.put("steps", steps);
        scenario.setWhen(when);
        return java.util.Collections.singletonList(scenario);
    }

    /**
     * 3단계: 일반 Gateway(비-DMN) 조건별 분기 시나리오 생성.
     * 해당 flow의 Evaluate 조건 key/value만 given에 포함 (전체 프로세스 변수 나열 안 함).
     */
    private List<Scenario> generateGatewayBranchScenarios(ProcessDefinition processDefinition, String processDefinitionId) {
        List<Scenario> scenarios = new ArrayList<>();
        List<Activity> childActivities = processDefinition.getChildActivities();
        if (childActivities == null) {
            return scenarios;
        }

        for (Activity activity : childActivities) {
            if (!(activity instanceof Gateway)) {
                continue;
            }
            Gateway gateway = (Gateway) activity;
            List<SequenceFlow> flows = gateway.getOutgoingSequenceFlows();
            if (flows == null || flows.isEmpty()) {
                continue;
            }

            String gatewayName = gateway.getName() != null ? gateway.getName() : gateway.getTracingTag();
            int flowIndex = 0;
            for (SequenceFlow flow : flows) {
                Condition cond = flow.getCondition();
                Activity targetActivity = flow.getTargetActivity();
                if (targetActivity == null && flow.getTargetRef() != null) {
                    targetActivity = processDefinition.getActivity(flow.getTargetRef());
                }
                if (targetActivity == null) {
                    flowIndex++;
                    continue;
                }

                Map<String, Object> given = new HashMap<>();
                String conditionLabel;
                if (cond instanceof Evaluate) {
                    Evaluate ev = (Evaluate) cond;
                    if (ev.getKey() != null && ev.getValue() != null) {
                        given.put(ev.getKey(), ev.getValue());
                    }
                    conditionLabel = (ev.getKey() != null ? ev.getKey() : "") + "_" + (ev.getValue() != null ? ev.getValue() : "");
                } else if (cond instanceof Otherwise) {
                    conditionLabel = "Otherwise";
                } else {
                    conditionLabel = "flow_" + flowIndex;
                }

                Map<String, Object> when = new HashMap<>();
                when.put("status", "Running");
                when.put("activityId", targetActivity.getTracingTag());
                when.put("activityName", targetActivity.getName() != null ? targetActivity.getName() : targetActivity.getTracingTag());

                Scenario scenario = new Scenario();
                scenario.setProcessDefinitionId(processDefinitionId);
                scenario.setName("Gateway_" + gatewayName.replaceAll("[^a-zA-Z0-9가-힣_]", "_") + "_" + flowIndex + "_" + conditionLabel);
                scenario.setGiven(given);
                scenario.setWhen(when);
                scenarios.add(scenario);
                flowIndex++;
            }
        }
        return scenarios;
    }

    /**
     * UserTask에 대한 시나리오 생성. 다음 액티비티가 DMN(BusinessRuleTask)이면 해당 DMN의 모든 규칙을 통과하는 시나리오를 규칙 수만큼 생성.
     * Given은 해당 흐름에서만 쓰는 최소 변수(해당 UserTask payload 변수 + DMN 규칙 입력 변수).
     */
    private List<Scenario> createScenariosForUserTask(ProcessDefinition processDefinition, String processDefinitionId, HumanActivity userTask) {
        List<SequenceFlow> outgoingFlows = userTask.getOutgoingSequenceFlows();
        if (outgoingFlows == null || outgoingFlows.isEmpty()) {
            Scenario one = createScenarioForUserTaskSingle(processDefinition, processDefinitionId, userTask, null, null);
            return one != null ? java.util.Collections.singletonList(one) : new ArrayList<>();
        }
        SequenceFlow firstFlow = outgoingFlows.get(0);
        Activity targetActivity = firstFlow.getTargetActivity();
        if (targetActivity == null && firstFlow.getTargetRef() != null) {
            targetActivity = processDefinition.getActivity(firstFlow.getTargetRef());
        }
        if (!(targetActivity instanceof BusinessRuleTask) || businessRuleStore == null || businessRuleEvaluator == null) {
            Scenario one = createScenarioForUserTaskSingle(processDefinition, processDefinitionId, userTask, targetActivity, firstFlow);
            return one != null ? java.util.Collections.singletonList(one) : new ArrayList<>();
        }
        BusinessRuleTask brTask = (BusinessRuleTask) targetActivity;
        String ruleId = brTask.getBusinessRuleId();
        if (ruleId == null || ruleId.isEmpty()) {
            Scenario one = createScenarioForUserTaskSingle(processDefinition, processDefinitionId, userTask, targetActivity, firstFlow);
            return one != null ? java.util.Collections.singletonList(one) : new ArrayList<>();
        }
        List<BusinessRuleEvaluator.DmnRuleCondition> rules;
        try {
            rules = businessRuleEvaluator.getRuleConditions(businessRuleStore.loadOrThrow(ruleId).getRuleJson());
        } catch (Exception e) {
            Scenario one = createScenarioForUserTaskSingle(processDefinition, processDefinitionId, userTask, targetActivity, firstFlow);
            return one != null ? java.util.Collections.singletonList(one) : new ArrayList<>();
        }
        if (rules.isEmpty()) {
            Scenario one = createScenarioForUserTaskSingle(processDefinition, processDefinitionId, userTask, targetActivity, firstFlow);
            return one != null ? java.util.Collections.singletonList(one) : new ArrayList<>();
        }
        List<SequenceFlow> brOutgoing = brTask.getOutgoingSequenceFlows();
        if (brOutgoing == null || brOutgoing.isEmpty()) {
            Scenario one = createScenarioForUserTaskSingle(processDefinition, processDefinitionId, userTask, targetActivity, firstFlow);
            return one != null ? java.util.Collections.singletonList(one) : new ArrayList<>();
        }
        Activity gatewayActivity = brOutgoing.get(0).getTargetActivity();
        if (gatewayActivity == null && brOutgoing.get(0).getTargetRef() != null) {
            gatewayActivity = processDefinition.getActivity(brOutgoing.get(0).getTargetRef());
        }
        if (!(gatewayActivity instanceof Gateway)) {
            Scenario one = createScenarioForUserTaskSingle(processDefinition, processDefinitionId, userTask, targetActivity, firstFlow);
            return one != null ? java.util.Collections.singletonList(one) : new ArrayList<>();
        }
        Gateway gateway = (Gateway) gatewayActivity;
        Map<String, String> dmnKeyToProcessVar = buildDmnInputKeyToProcessVar(brTask);
        String activityName = userTask.getName();
        String activityId = userTask.getTracingTag();
        String baseName = (activityName != null && !activityName.isEmpty())
                ? "UserTask_" + activityName.replaceAll("[^a-zA-Z0-9가-힣_]", "_")
                : (activityId != null ? "UserTask_" + activityId : "UserTask_" + System.currentTimeMillis());

        List<Scenario> result = new ArrayList<>();
        Map<String, Object> taskPayloadVars = buildPayloadFromHumanActivity(processDefinition, userTask);
        for (int ruleIndex = 0; ruleIndex < rules.size(); ruleIndex++) {
            BusinessRuleEvaluator.DmnRuleCondition rule = rules.get(ruleIndex);
            Map<Integer, String> inputKeys = rule.getInputKeys();
            List<String> inputEntries = rule.getInputEntries();
            Map<String, Object> given = new HashMap<>();
            if (inputKeys != null && inputEntries != null) {
                for (int idx = 0; idx < inputEntries.size(); idx++) {
                    String inputKey = inputKeys.get(idx + 1);
                    if (inputKey != null) {
                        String processVar = dmnKeyToProcessVar.get(inputKey);
                        String key = processVar != null ? processVar : inputKey;
                        if (!taskPayloadVars.containsKey(key)) {
                            Object value = sampleValueFromInputEntry(inputEntries.get(idx));
                            if (value != null) {
                                given.put(key, value);
                            }
                        }
                    }
                }
            }
            String outputValue = rule.getOutputValue();
            String bpmnValue = mapDmnOutcomeToBpmn(outputValue);
            Activity thenTarget = null;
            String conditionKey = null;
            String conditionValue = null;
            List<SequenceFlow> gatewayFlows = gateway.getOutgoingSequenceFlows();
            if (gatewayFlows != null) {
                for (SequenceFlow flow : gatewayFlows) {
                    Condition cond = flow.getCondition();
                    if (cond instanceof Evaluate) {
                        Evaluate ev = (Evaluate) cond;
                        if (bpmnValue != null && bpmnValue.equals(ev.getValue() != null ? ev.getValue().toString() : "")) {
                            thenTarget = flow.getTargetActivity();
                            if (thenTarget == null && flow.getTargetRef() != null) {
                                thenTarget = processDefinition.getActivity(flow.getTargetRef());
                            }
                            if (thenTarget != null) {
                                conditionKey = ev.getKey();
                                conditionValue = ev.getValue() != null ? ev.getValue().toString() : null;
                                break;
                            }
                        }
                    }
                }
            }
            if (thenTarget == null) {
                continue;
            }
            Map<String, Object> payloadForActivity = new HashMap<>();
            if (inputKeys != null && inputEntries != null && !taskPayloadVars.isEmpty()) {
                for (int idx = 0; idx < inputEntries.size(); idx++) {
                    String inputKey = inputKeys.get(idx + 1);
                    if (inputKey != null) {
                        String processVar = dmnKeyToProcessVar.get(inputKey);
                        String key = processVar != null ? processVar : inputKey;
                        if (taskPayloadVars.containsKey(key)) {
                            Object value = sampleValueFromInputEntry(inputEntries.get(idx));
                            if (value != null) {
                                payloadForActivity.put(key, value);
                            }
                        }
                    }
                }
            }
            Map<String, Object> when = new HashMap<>();
            when.put("activityId", activityId);
            when.put("activityName", activityName != null ? activityName : activityId);
            when.put("status", "Completed");
            if (!payloadForActivity.isEmpty()) {
                when.put("payloadMapping", payloadForActivity);
            }

            Scenario scenario = new Scenario();
            scenario.setProcessDefinitionId(processDefinitionId);
            scenario.setName(baseName + "_DMN규칙_" + ruleIndex + "_" + (bpmnValue != null ? bpmnValue : "out"));
            scenario.setGiven(given);
            scenario.setWhen(when);
            result.add(scenario);
        }
        return result;
    }

    /** DMN input key(예: key_1768...) → 프로세스 변수명(예: 신용도) 매핑. BR의 mappingContext에서 추출. */
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
            // argument 예: "[businessRule].r_xxx.in_key_1768362157935" → DMN input key "key_1768362157935"
            if (arg != null && arg.contains("in_key_")) {
                int i = arg.indexOf("in_key_");
                String suffix = arg.substring(i + 7); // "1768362157935" or "1768362157935.xxx"
                if (suffix.contains(".")) {
                    suffix = suffix.substring(0, suffix.indexOf("."));
                }
                String dmnKey = "key_" + suffix;
                out.put(dmnKey, processVar);
            }
        }
        return out;
    }

    private static String mapDmnOutcomeToBpmn(String outputValue) {
        if (outputValue == null) return null;
        String s = outputValue.trim();
        if ("approve".equalsIgnoreCase(s)) return "승인";
        if ("reject".equalsIgnoreCase(s)) return "거절";
        return s;
    }

    private static Object sampleValueFromInputEntry(String entry) {
        if (entry == null) return null;
        entry = entry.trim();
        if ("-".equals(entry)) return null;
        Matcher m = COMPARISON_PATTERN.matcher(entry);
        if (!m.find()) return entry;
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

    /**
     * UserTask에 대한 시나리오 1개 생성 (다음 액티비티가 BR이 아니거나 DMN 규칙 미적용 시).
     * Given은 해당 UserTask payload 변수만 (해당 흐름에서 쓰는 최소 변수).
     */
    private Scenario createScenarioForUserTaskSingle(ProcessDefinition processDefinition, String processDefinitionId, HumanActivity userTask, Activity targetActivity, SequenceFlow firstFlow) {
        Scenario scenario = new Scenario();
        scenario.setProcessDefinitionId(processDefinitionId);

        String activityName = userTask.getName();
        String activityId = userTask.getTracingTag();
        if (activityName != null && !activityName.isEmpty()) {
            scenario.setName("UserTask_" + activityName.replaceAll("[^a-zA-Z0-9가-힣_]", "_"));
        } else if (activityId != null) {
            scenario.setName("UserTask_" + activityId);
        } else {
            scenario.setName("UserTask_" + System.currentTimeMillis());
        }

        Map<String, Object> payloadForActivity = buildPayloadFromHumanActivity(processDefinition, userTask);
        scenario.setGiven(new HashMap<>());

        Map<String, Object> when = new HashMap<>();
        when.put("activityId", activityId);
        when.put("activityName", activityName != null ? activityName : activityId);
        when.put("status", "Completed");
        if (!payloadForActivity.isEmpty()) {
            when.put("payloadMapping", payloadForActivity);
        }
        scenario.setWhen(when);

        return scenario;
    }

    /**
     * 직접 입력(arguments) 또는 form 타입(ProcessVariable)인지 여부.
     * When.payload에는 이 조건을 만족하는 변수만 넣고, 이들이 있을 때만 payload를 넣는다.
     */
    private static boolean isDirectInputOrFormType(ParameterContext pc) {
        if (pc == null) return false;
        if (pc.getVariable() != null) return true; // form 타입(프로세스 변수)
        if (pc.getArgument() != null && pc.getArgument().getText() != null
                && pc.getArgument().getText().startsWith("[Arguments].")) return true; // 직접 입력
        return false;
    }

    /**
     * HumanActivity의 eventSynchronization.mappingContext에서 direction=out 중
     * 직접 입력(arguments) 또는 form 타입 변수만 추려 기본값으로 payload 맵 생성.
     * when에는 이 payload가 비어 있지 않을 때만 payloadMapping을 넣는다.
     */
    private Map<String, Object> buildPayloadFromHumanActivity(ProcessDefinition processDefinition, HumanActivity userTask) {
        Map<String, Object> payload = new HashMap<>();
        EventSynchronization sync = userTask.getEventSynchronization();
        if (sync == null) {
            return payload;
        }
        MappingContext mappingContext = sync.getMappingContext();
        if (mappingContext == null || mappingContext.getMappingElements() == null) {
            return payload;
        }
        for (ParameterContext pc : mappingContext.getMappingElements()) {
            if (pc == null || !ParameterContext.DIRECTION_OUT.equals(pc.getDirection())) {
                continue;
            }
            if (!isDirectInputOrFormType(pc)) {
                continue;
            }
            String varName = null;
            if (pc.getVariable() != null && pc.getVariable().getName() != null) {
                varName = pc.getVariable().getName();
                if (varName.startsWith("[Arguments].")) {
                    varName = varName.substring("[Arguments].".length());
                }
            } else if (pc.getArgument() != null && pc.getArgument().getText() != null) {
                String arg = pc.getArgument().getText();
                if (arg.startsWith("[Arguments].")) {
                    varName = arg.substring("[Arguments].".length());
                }
            }
            if (varName == null || varName.isEmpty()) {
                continue;
            }
            Object defaultValue = null;
            ProcessVariable pv = processDefinition.getProcessVariable(varName);
            if (pv != null && pv.getDefaultValue() != null && (!(pv.getDefaultValue() instanceof String) || !((String) pv.getDefaultValue()).isEmpty())) {
                defaultValue = pv.getDefaultValue();
            } else if (pv != null) {
                String typeName = pv.getTypeClassName();
                if (typeName == null && pv.getType() != null) {
                    typeName = pv.getType().getName();
                }
                if (typeName != null) {
                    if (typeName.contains("String") || typeName.contains("Text")) {
                        defaultValue = "";
                    } else if (typeName.contains("Number") || typeName.contains("Integer") || typeName.contains("Long") || typeName.contains("Double") || typeName.contains("int") || typeName.contains("double")) {
                        defaultValue = 0;
                    } else if (typeName.contains("Boolean") || typeName.contains("boolean")) {
                        defaultValue = false;
                    }
                }
            }
            if (defaultValue == null) {
                defaultValue = "";
            }
            payload.put(varName, defaultValue);
        }
        return payload;
    }

}
