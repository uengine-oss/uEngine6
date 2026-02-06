package org.uengine.five.scenario;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.uengine.five.dto.InstanceResource;
import org.uengine.five.dto.Message;
import org.uengine.five.dto.ProcessExecutionCommand;
import org.uengine.five.dto.ProcessVariableValue;
import org.uengine.five.entity.WorklistEntity;
import org.uengine.five.repository.WorklistRepository;
import org.uengine.five.service.InstanceServiceImpl;
import org.uengine.kernel.Activity;
import org.uengine.kernel.HumanActivity;
import org.uengine.kernel.ProcessDefinition;
import org.uengine.kernel.ProcessInstance;
import org.uengine.kernel.ReceiveActivity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

/**
 * 시나리오 실행 및 결과 반환 (Given-When만 사용).
 * <p>
 * Given → When 실행 후, RunResult에 결과값(현재 액티비티/경로/변수)만 채워 반환. 자동 판정(Then) 없음.
 * When = 현재 태스크. when.activityId가 가리키는 태스크가 현재 태스크이고, status에 따라 "해당 태스크까지 도달(Running)" 또는 "이 태스크 완료(Completed)". payloadMapping은 이 태스크 Complete 시 주입. when.steps면 여러 현재 태스크를 순서대로 처리.
 * when.activityName은 확인용. when.event 있으면 fireMessage.
 */
@Component
public class ScenarioRunner {

    /** 내부 분기용: status "Running" 또는 미지정 시 */
    private static final String BEHAVIOR_RUN_UNTIL = "runUntil";
    private static final String BEHAVIOR_COMPLETE = "Complete";
    private static final String BEHAVIOR_FIRE_MESSAGE = "fireMessage";
    /**
     * 최대 폴링 시도 횟수 (실제 wall-clock 초 단위가 아님).
     * runUntilTarget에서 목표 액티비티 도달까지 최대 이 횟수만큼 루프를 돌며,
     * 각 루프마다 Thread.sleep(300~500ms)가 있으므로 실제 대기 시간은 약 (N * 0.3~0.5)초 구간.
     */
    private static final int DEFAULT_MAX_ATTEMPTS = 30;

    @Autowired
    private InstanceServiceImpl instanceService;

    @Autowired
    private WorklistRepository worklistRepository;

    /**
     * 시나리오 실행
     *
     * @return true if PASS, false if FAIL
     */
    public boolean run(Scenario scenario) {
        return run(scenario, null);
    }

    /**
     * 시나리오 실행 후 RunResult 반환 (1단계 API용).
     */
    public RunResult runWithResult(Scenario scenario) {
        RunResult result = new RunResult();
        run(scenario, result);
        return result;
    }

    /**
     * 시나리오 실행. runResult가 non-null이면 실행 결과를 채움.
     *
     * @return true if PASS, false if FAIL
     */
    public boolean run(Scenario scenario, RunResult runResult) {
        System.out.println("\n========================================");
        System.out.println("시나리오 실행: " + scenario.getName());
        System.out.println("========================================");

        try {
            // --- Given ---
            System.out.println("\n[Given]");
            logGiven(scenario);
            ProcessExecutionCommand command = buildCommand(scenario);

            // --- When ---
            System.out.println("\n[When]");
            InstanceResource instanceRes = instanceService.start(command);
            if (instanceRes == null) {
                System.out.println("\n[FAIL] 프로세스 시작 실패");
                if (runResult != null) {
                    runResult.setSuccess(false);
                    runResult.setMessage("프로세스 시작 실패");
                }
                return false;
            }
            String instanceId = instanceRes.getInstanceId();
            if (runResult != null) {
                runResult.setInstanceId(instanceId);
            }
            System.out.println("  process started, instanceId=" + instanceId);

            ProcessInstance processInstance = instanceService.getProcessInstanceLocal(instanceId);
            ProcessDefinition def = processInstance.getProcessDefinition();

            String behavior = resolveWhenBehavior(scenario);

            // When.instanceStatus: (선택) 액션 전 인스턴스 상태 전제
            Map<String, Object> whenMap = scenario.getWhen();
            if (whenMap != null && whenMap.get("instanceStatus") != null) {
                String expectedStatus = whenMap.get("instanceStatus").toString();
                String actualStatus = processInstance.getStatus("");
                if (!expectedStatus.equals(actualStatus)) {
                    System.out.println("\n[FAIL] when.instanceStatus 전제 불일치: 기대=" + expectedStatus + ", 실제=" + actualStatus);
                    if (runResult != null) {
                        runResult.setSuccess(false);
                        runResult.setMessage("when.instanceStatus 전제 불일치");
                        runResult.setInstanceId(instanceId);
                    }
                    return false;
                }
                System.out.println("  when.instanceStatus 전제 충족: " + actualStatus);
            }

            String startFromId = resolveStartFromActivityId(processInstance, scenario);
            if (startFromId != null) {
                System.out.println("  startFrom: " + startFromId);
                setVariables(processInstance, scenario.getGiven());
                processInstance.execute(startFromId);
                Activity startAct = def.getActivity(startFromId);
                System.out.println("  실행 완료: " + (startAct != null ? startAct.getName() : startFromId));
            }

            List<Activity> reached;
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> steps = (whenMap != null && whenMap.get("steps") instanceof List)
                    ? (List<Map<String, Object>>) whenMap.get("steps")
                    : null;
            if (steps != null && !steps.isEmpty()) {
                reached = runWhenSteps(processInstance, instanceId, def, scenario, steps, DEFAULT_MAX_ATTEMPTS);
            } else if (BEHAVIOR_COMPLETE.equals(behavior)) {
                String completeTargetId = getWhenCompleteActivityId(scenario, def);
                if (completeTargetId == null || completeTargetId.isEmpty()) {
                    System.out.println("\n[FAIL] status=Completed 시 when.activityId가 필요합니다.");
                    if (runResult != null) {
                        runResult.setSuccess(false);
                        runResult.setMessage("status=Completed 시 when.activityId가 필요합니다.");
                        runResult.setInstanceId(instanceId);
                    }
                    return false;
                }
                System.out.println("  status: Completed, completeTargetId: " + completeTargetId);
                List<Activity> atTarget = runUntilTarget(processInstance, instanceId, def, completeTargetId, false,
                        scenario, null, DEFAULT_MAX_ATTEMPTS);
                if (atTarget.isEmpty()) {
                    System.out.println("  ⚠ Complete 타깃 도달 전 프로세스 종료 또는 타임아웃");
                    reached = atTarget;
                } else {
                    Activity toComplete = atTarget.get(0);
                    if (toComplete instanceof HumanActivity) {
                        Map<String, Object> payload = buildPayloadForActivity(scenario, toComplete.getTracingTag(), null);
                        ((HumanActivity) toComplete).fireReceived(processInstance, payload);
                        System.out.println("  ✓ Complete 실행(HumanActivity): " + toComplete.getTracingTag());
                    } else if (toComplete instanceof ReceiveActivity) {
                        processInstance.execute(toComplete.getTracingTag());
                        System.out.println("  ✓ Complete 실행(ReceiveActivity): " + toComplete.getTracingTag());
                    } else {
                        processInstance.execute(toComplete.getTracingTag());
                        System.out.println("  ✓ Complete 실행(execute): " + toComplete.getTracingTag());
                    }
                    try { Thread.sleep(500); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
                    reached = processInstance.getCurrentRunningActivities();
                }
            } else if (BEHAVIOR_FIRE_MESSAGE.equals(behavior)) {
                Object eventObj = whenMap != null ? whenMap.get("event") : null;
                if (eventObj == null || eventObj.toString().isEmpty()) {
                    System.out.println("\n[FAIL] fireMessage 시 when.event가 필요합니다.");
                    if (runResult != null) {
                        runResult.setSuccess(false);
                        runResult.setMessage("fireMessage 시 when.event가 필요합니다.");
                        runResult.setInstanceId(instanceId);
                    }
                    return false;
                }
                String eventType = eventObj.toString();
                @SuppressWarnings("unchecked")
                Map<String, Object> payloadMap = (whenMap != null && whenMap.get("payload") != null
                        && whenMap.get("payload") instanceof Map)
                                ? (Map<String, Object>) whenMap.get("payload")
                                : new HashMap<>();
                Serializable payload = (Serializable) (payloadMap.isEmpty() ? null : payloadMap);
                Message message = new Message(eventType, payload);
                instanceService.postMessage(instanceId, message);
                System.out.println("  ✓ fireMessage: event=" + eventType);
                String runUntilId = resolveRunUntilTarget(scenario, def);
                if (runUntilId != null && !runUntilId.isEmpty())
                    reached = runUntilTarget(processInstance, instanceId, def, runUntilId, true, scenario, null,
                            DEFAULT_MAX_ATTEMPTS);
                else
                    reached = processInstance.getCurrentRunningActivities();
            } else {
                String runUntilId = resolveRunUntilTarget(scenario, def);
                if (runUntilId == null || runUntilId.isEmpty()) {
                    System.out.println("\n[FAIL] when.activityId가 필요합니다.(status=Running)");
                    if (runResult != null) {
                        runResult.setSuccess(false);
                        runResult.setMessage("when.activityId가 필요합니다.");
                        runResult.setInstanceId(instanceId);
                    }
                    return false;
                }
                System.out.println("  status: Running, runUntilId: " + runUntilId);
                reached = runUntilTarget(processInstance, instanceId, def, runUntilId, false,
                        scenario, null, DEFAULT_MAX_ATTEMPTS);
            }

            // --- 결과 반환 (판정 없음) ---
            System.out.println("\n[결과]");
            String actualResult = reached.stream().map(Activity::getName).filter(Objects::nonNull)
                    .collect(Collectors.joining(", "));
            if (actualResult.isEmpty())
                actualResult = "COMPLETED";
            System.out.println("  현재: " + actualResult);
            System.out.println("========================================\n");
            if (runResult != null) {
                runResult.setSuccess(true);
                runResult.setMessage("OK");
                runResult.setInstanceId(instanceId);
                Map<String, Object> actualMap = new HashMap<>();
                actualMap.put("currentActivityNames", actualResult);
                actualMap.put("currentActivityIds", reached.stream().map(Activity::getTracingTag).filter(Objects::nonNull).collect(Collectors.joining(",")));
                runResult.setActual(actualMap);
            }
            return true;

        } catch (TimeoutException e) {
            System.out.println("\n[FAIL] 타임아웃: " + e.getMessage());
            System.out.println("========================================\n");
            if (runResult != null) {
                runResult.setSuccess(false);
                runResult.setMessage("타임아웃: " + e.getMessage());
            }
            return false;
        } catch (Exception e) {
            System.out.println("\n[FAIL] " + e.getMessage());
            System.out.println("========================================");
            e.printStackTrace();
            System.out.println("========================================\n");
            if (runResult != null) {
                runResult.setSuccess(false);
                runResult.setMessage(e.getMessage());
            }
            return false;
        }
    }

    private void logGiven(Scenario scenario) {
        if (scenario.getGiven() != null && !scenario.getGiven().isEmpty()) {
            scenario.getGiven().forEach((k, v) -> System.out.println("  " + k + " = " + v));
        } else {
            System.out.println("  (없음)");
        }
    }

    private ProcessExecutionCommand buildCommand(Scenario scenario) {
        ProcessExecutionCommand cmd = new ProcessExecutionCommand();
        cmd.setProcessDefinitionId(scenario.getProcessDefinitionId());
        cmd.setSimulation(true);
        if (scenario.getGiven() != null && !scenario.getGiven().isEmpty()) {
            cmd.setProcessVariableValues(createProcessVariables(scenario.getGiven()));
        }
        return cmd;
    }

    /** when.status 값. */
    private static final String TASK_STATUS_RUNNING = "Running";
    private static final String TASK_STATUS_COMPLETED = "Completed";

    /**
     * When 실행 동작 결정. when.status만 사용(신규). status 없을 때만 when.action 폴백(하위호환).
     * "Completed" → Complete, "Running" 또는 미지정 → runUntil(activityId 있으면 그곳까지), when.event 있으면 fireMessage.
     */
    private String resolveWhenBehavior(Scenario scenario) {
        Map<String, Object> when = scenario.getWhen();
        if (when == null)
            return BEHAVIOR_RUN_UNTIL;
        if (when.get("event") != null && !when.get("event").toString().isEmpty())
            return BEHAVIOR_FIRE_MESSAGE;
        Object statusObj = when.get("status");
        if (statusObj != null && !statusObj.toString().isEmpty()) {
            String s = statusObj.toString();
            if (TASK_STATUS_COMPLETED.equals(s))
                return BEHAVIOR_COMPLETE;
            if (TASK_STATUS_RUNNING.equals(s))
                return BEHAVIOR_RUN_UNTIL;
        }
        Object actionObj = when.get("action");
        if (actionObj != null && !actionObj.toString().isEmpty()) {
            String a = actionObj.toString();
            if ("Complete".equals(a)) return BEHAVIOR_COMPLETE;
            if ("fireMessage".equals(a)) return BEHAVIOR_FIRE_MESSAGE;
        }
        return BEHAVIOR_RUN_UNTIL;
    }

    /**
     * When에서 목표 액티비티 ID(tracingTag) 반환.
     * when.activityId만 사용(activityName은 확인용, 미사용).
     */
    private String resolveRunUntilTarget(Scenario scenario, ProcessDefinition def) {
        Map<String, Object> when = scenario.getWhen();
        if (when == null) return null;
        Object idObj = when.get("activityId");
        if (idObj != null && !idObj.toString().isEmpty())
            return idObj.toString();
        return null;
    }

    /** 대기 상태(현재 액티비티 존재 또는 프로세스 완료)까지 진행. */
    private List<Activity> runUntilWaiting(ProcessInstance instance, String instanceId, int maxAttempts)
            throws Exception {
        int attempts = 0;
        while (attempts < maxAttempts) {
            if (instance.getStatus("").equals(Activity.STATUS_COMPLETED)) {
                return new ArrayList<>();
            }
            List<Activity> activities = instance.getCurrentRunningActivities();
            if (!activities.isEmpty()) {
                return activities;
            }
            Thread.sleep(500);
            attempts++;
        }
        return instance.getCurrentRunningActivities();
    }

    /**
     * when.steps 다단계 실행. 각 step: runUntil 해당 activityId → Complete(ReceiveActivity 기준, HumanActivity면 payload 주입).
     */
    private List<Activity> runWhenSteps(ProcessInstance processInstance, String instanceId, ProcessDefinition def,
            Scenario scenario, List<Map<String, Object>> steps, int maxAttempts) throws Exception, TimeoutException {
        List<Activity> reached = new ArrayList<>();
        for (int i = 0; i < steps.size(); i++) {
            Map<String, Object> step = steps.get(i);
            Object aidObj = step.get("activityId");
            if (aidObj == null || aidObj.toString().isEmpty())
                continue;
            String stepActivityId = aidObj.toString();
            System.out.println("  [step " + (i + 1) + "/" + steps.size() + "] activityId=" + stepActivityId);
            List<Activity> atTarget = runUntilTarget(processInstance, instanceId, def, stepActivityId, false,
                    scenario, step, maxAttempts);
            if (atTarget.isEmpty()) {
                System.out.println("  ⚠ step 도달 전 프로세스 종료 또는 타임아웃");
                return atTarget;
            }
            Activity toComplete = atTarget.get(0);
            if (!stepActivityId.equals(toComplete.getTracingTag())) {
                System.out.println("  ⚠ step 목표 불일치");
                return atTarget;
            }
            if (toComplete instanceof HumanActivity) {
                Map<String, Object> payload = buildPayloadForActivity(scenario, stepActivityId, step);
                ((HumanActivity) toComplete).fireReceived(processInstance, payload);
                System.out.println("  ✓ step Complete(HumanActivity): " + stepActivityId);
            } else if (toComplete instanceof ReceiveActivity) {
                processInstance.execute(stepActivityId);
                System.out.println("  ✓ step Complete(ReceiveActivity): " + stepActivityId);
            } else {
                processInstance.execute(stepActivityId);
                System.out.println("  ✓ step execute: " + stepActivityId);
            }
            try { Thread.sleep(500); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
            reached = processInstance.getCurrentRunningActivities();
        }
        return reached;
    }

    /** When.status "Completed" 시 완료할 액티비티 ID. when.activityId만 사용(activityName 미사용). */
    private String getWhenCompleteActivityId(Scenario scenario, ProcessDefinition def) {
        Map<String, Object> when = scenario.getWhen();
        if (when == null)
            return null;
        Object idObj = when.get("activityId");
        if (idObj != null && !idObj.toString().isEmpty())
            return idObj.toString();
        return null;
    }

    /** 시작 위치: when.startFromActivityId만 사용(조건·계산). 루트 startFromActivityId/Name은 사용 안 함. */
    private String resolveStartFromActivityId(ProcessInstance instance, Scenario scenario) throws Exception {
        Map<String, Object> when = scenario.getWhen();
        if (when == null) return null;
        Object idObj = when.get("startFromActivityId");
        if (idObj != null && !idObj.toString().isEmpty())
            return idObj.toString();
        return null;
    }

    /**
     * 목표 액티비티(tracingTag=targetActivityId)에 도달할 때까지 진행.
     * 타깃 식별은 activityId(tracingTag)만 사용(activityName 미사용).
     *
     * @param targetActivityId 목표 액티비티 ID(tracingTag)
     * @param scenario         시나리오. when.payloadMapping 키는 activityId 기준.
     */
    private List<Activity> runUntilTarget(ProcessInstance instance, String instanceId, ProcessDefinition def,
            String targetActivityId, boolean completeTarget, Scenario scenario, Map<String, Object> stepForPayload, int maxAttempts)
            throws Exception, TimeoutException {
        int attempts = 0;
        String lastLogged = null;

        // [디버깅] 무한반복 추적: 목표와 최대 시도 횟수
        System.out.println("  [runUntilTarget] 목표=" + targetActivityId + ", completeTarget=" + completeTarget + ", maxAttempts=" + maxAttempts);

        while (attempts < maxAttempts) {
            // [디버깅] 매 시도마다 attempts 출력 → 30까지 올라가면 정상 종료, 같은 숫자에서 멈추면 다른 원인
            System.out.println("  [runUntilTarget] attempts=" + (attempts + 1) + "/" + maxAttempts);
            List<Activity> activities = instance.getCurrentRunningActivities();

            if (instance.getStatus("").equals(Activity.STATUS_COMPLETED)) {
                System.out.println("  프로세스 종료.");
                return new ArrayList<>();
            }

            if (!activities.isEmpty()) {
                Activity current = activities.get(0);
                String currentId = current.getTracingTag();
                String currentName = current.getName();
                if (currentId != null && currentId.equals(targetActivityId)) {
                    if (!completeTarget) {
                        System.out.println("  ✓ runUntilWaiting 도달: " + currentId + " " + currentName + " (대기 유지)");
                        return activities;
                    }
                    System.out.println("  ✓ 목표 도달: " + currentId + " " + currentName);
                    return activities;
                }

                if (!currentId.equals(lastLogged)) {
                    System.out.println("  → 현재: " + currentId + " " + currentName);
                    lastLogged = currentId;
                }

                if (current instanceof ReceiveActivity) {
                    if (current instanceof HumanActivity) {
                        Map<String, Object> payload = buildPayloadForActivity(scenario, currentId, stepForPayload);
                        try {
                            ((HumanActivity) current).fireReceived(instance, payload);
                            System.out.println("  ✓ ReceiveActivity(Human) 완료: " + currentId + " " + currentName);
                        } catch (Exception ex) {
                            System.out.println("  ✗ HumanActivity 완료 실패: " + currentId + " - " + ex.getMessage());
                        }
                    } else {
                        try {
                            instance.execute(currentId);
                            System.out.println("  ✓ ReceiveActivity 실행: " + currentId + " " + currentName);
                        } catch (Exception ex) {
                            System.out.println("  → 대기 중: " + currentId + " - " + ex.getMessage());
                        }
                    }
                    Thread.sleep(500);
                } else {
                    try {
                        instance.execute(currentId);
                        System.out.println("  ✓ 비-ReceiveActivity 실행: " + currentId + " " + currentName);
                    } catch (Exception ex) {
                        System.out.println("  → 대기 중: " + currentId + " - " + ex.getMessage());
                    }
                    Thread.sleep(500);
                }
            } else {
                // [디버깅] 현재 실행 중인 액티비티 없음(activities 비어 있음) → 대기
                if (attempts % 5 == 0) {
                    System.out.println("  [runUntilTarget] 현재 액티비티 없음, 대기 중... attempts=" + (attempts + 1));
                }
                Thread.sleep(500);
            }
            attempts++;
        }

        List<Activity> finalList = instance.getCurrentRunningActivities();
        System.out.println("  ⚠ 타임아웃. 최종: "
                + (finalList.isEmpty() ? "없음" : finalList.get(0).getTracingTag() + " " + finalList.get(0).getName()));
        return finalList;
    }

    private ProcessVariableValue[] createProcessVariables(Map<String, Object> given) {
        if (given == null || given.isEmpty())
            return new ProcessVariableValue[0];
        List<ProcessVariableValue> list = new ArrayList<>();
        for (Map.Entry<String, Object> e : given.entrySet()) {
            ProcessVariableValue pv = new ProcessVariableValue();
            pv.setName(e.getKey());
            Object v = e.getValue();
            if (v instanceof List) {
                List<?> l = (List<?>) v;
                Serializable[] arr = new Serializable[l.size()];
                for (int i = 0; i < l.size(); i++)
                    arr[i] = (Serializable) l.get(i);
                pv.setValues(arr);
            } else {
                pv.setValues(new Serializable[] { (Serializable) v });
            }
            list.add(pv);
        }
        return list.toArray(new ProcessVariableValue[0]);
    }

    /**
     * activityId에 넣을 payload. payloadMapping은 activityId 키 없이 payload 맵만 저장(when/step에 activityId 있음).
     * 하위호환: payloadMapping이 activityId 키로 감싼 경우도 처리.
     */
    private Map<String, Object> buildPayloadForActivity(Scenario scenario, String activityId, Map<String, Object> stepForPayload) {
        if (stepForPayload != null && activityId.equals(stepForPayload.get("activityId"))) {
            Object pm = stepForPayload.get("payloadMapping");
            if (pm instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> m = (Map<String, Object>) pm;
                Object nested = m.get(activityId);
                if (nested instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> map = (Map<String, Object>) nested;
                    return new HashMap<>(map);
                }
                return new HashMap<>(m);
            }
        }
        Map<String, Object> when = scenario.getWhen();
        if (when != null) {
            Object pm = when.get("payloadMapping");
            if (pm instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> payloadMapping = (Map<String, Object>) pm;
                if (payloadMapping != null) {
                    Object activityPayload = payloadMapping.get(activityId);
                    if (activityPayload instanceof Map) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> map = (Map<String, Object>) activityPayload;
                        return new HashMap<>(map);
                    }
                    return new HashMap<>(payloadMapping);
                }
            }
        }
        return new HashMap<>();
    }

    private void setVariables(ProcessInstance instance, Map<String, Object> given) throws Exception {
        if (given == null || given.isEmpty())
            return;
        for (Map.Entry<String, Object> e : given.entrySet()) {
            instance.set("", e.getKey(), (Serializable) e.getValue());
        }
    }
}
