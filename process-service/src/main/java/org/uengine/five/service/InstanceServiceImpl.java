package org.uengine.five.service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.QueryParam;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.HandlerMapping;
import org.uengine.five.ProcessServiceApplication;
import org.uengine.five.dto.InstanceResource;
import org.uengine.five.dto.Message;
import org.uengine.five.dto.ProcessExecutionCommand;
import org.uengine.five.dto.StartAndCompleteCommand;
import org.uengine.five.dto.TaskReturnAvailability;
import org.uengine.five.dto.TaskReturnCandidate;
import org.uengine.five.dto.TaskReturnCommand;
import org.uengine.five.dto.TaskReturnResult;
import org.uengine.five.dto.TaskSkipAvailability;
import org.uengine.five.dto.TaskSkipCommand;
import org.uengine.five.dto.TaskSkipResult;
import org.uengine.five.dto.WorkItemResource;
import org.uengine.five.dto.RoleMappingCommand;
import org.uengine.five.entity.ProcessInstanceEntity;
import org.uengine.five.entity.ServiceEndpointEntity;
import org.uengine.five.entity.WorklistEntity;
import org.uengine.five.framework.ProcessTransactionContext;
import org.uengine.five.framework.ProcessTransactional;
import org.uengine.five.overriding.JPAProcessInstance;
import org.uengine.five.repository.ProcessInstanceRepository;
import org.uengine.five.repository.ServiceEndpointRepository;
import org.uengine.five.repository.WorklistRepository;
import org.uengine.five.businessrule.BusinessRuleStore;
import org.uengine.five.businessrule.BusinessRuleEvaluator;
import org.uengine.five.scenario.RunResult;
import org.uengine.five.scenario.Scenario;
import org.uengine.five.scenario.ScenarioController;
import org.uengine.five.serializers.BpmnXMLParser;
import org.uengine.five.spring.SecurityAwareServletFilter;
import org.uengine.kernel.AbstractProcessInstance;
import org.uengine.kernel.Activity;
import org.uengine.kernel.ActivityInstanceContext;
import org.uengine.kernel.DefaultProcessInstance;
import org.uengine.kernel.DeployFilter;
import org.uengine.kernel.ExecutionScopeContext;
import org.uengine.kernel.GlobalContext;
import org.uengine.kernel.HumanActivity;
import org.uengine.kernel.ParameterContext;
import org.uengine.kernel.ProcessDefinition;
import org.uengine.kernel.ProcessInstance;
import org.uengine.kernel.ProcessVariable;
import org.uengine.kernel.ReceiveActivity;
import org.uengine.kernel.RoleMapping;
import org.uengine.kernel.TaskSkipAnalyzer;
import org.uengine.kernel.UEngineException;
import org.uengine.kernel.ValidationContext;
import org.uengine.contexts.UserContext;
import org.uengine.kernel.Condition;
import org.uengine.kernel.Evaluate;
import org.uengine.kernel.bpmn.CatchingRestMessageEvent;
import org.uengine.kernel.bpmn.Event;
import org.uengine.kernel.bpmn.Gateway;
import org.uengine.kernel.bpmn.SendTask;
import org.uengine.kernel.bpmn.SequenceFlow;
import org.uengine.kernel.bpmn.SignalEventInstance;
import org.uengine.kernel.bpmn.SignalIntermediateCatchEvent;
import org.uengine.kernel.bpmn.SubProcess;
import org.uengine.modeling.resource.ContainerResource;
import org.uengine.modeling.resource.DefaultResource;
import org.uengine.modeling.resource.IContainer;
import org.uengine.modeling.resource.IResource;
import org.uengine.modeling.resource.ResourceManager;
import org.uengine.util.UEngineUtil;
import org.uengine.webservices.worklist.DefaultWorkList;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.TextNode;
import com.fasterxml.jackson.databind.node.ValueNode;

/**
 * Created by uengine on 2017. 8. 9..
 *
 * Implementation Principles:
 * - REST Maturity Level : 2
 * - Not using old uEngine ProcessManagerBean, this replaces the
 * ProcessManagerBean
 * - ResourceManager and CachedResourceManager will be used for definition
 * caching (Not to use the old DefinitionFactory)
 * - json must be Typed JSON to enable object polymorphism - need to change the
 * jackson engine. TODO: accept? typed json is sometimes hard to read
 */
@RestController
@CrossOrigin(origins = "*")
@Service
public class InstanceServiceImpl implements InstanceService {

    @Autowired
    DefinitionServiceUtil definitionService;

    @Autowired
    ResourceManager resourceManager;

    @Autowired
    ApplicationContext applicationContext;

    @Autowired
    WorklistRepository worklistRepository;

    @Autowired
    private ApplicationContext context;

    @Autowired
    BusinessRuleStore businessRuleStore;

    @Autowired
    BusinessRuleEvaluator businessRuleEvaluator;

    @Autowired
    ScenarioController scenarioController;

    static ObjectMapper objectMapper = BpmnXMLParser.createTypedJsonObjectMapper();
    static ObjectMapper arrayObjectMapper = BpmnXMLParser.createTypedJsonArrayObjectMapper();
    static ObjectMapper plainObjectMapper = new ObjectMapper();

    // ----------------- execution services -------------------- //
    @RequestMapping(value = "/instance", consumes = "application/json;charset=UTF-8", method = { RequestMethod.POST,
            RequestMethod.PUT }, produces = "application/json;charset=UTF-8")
    @Transactional(rollbackFor = { Exception.class })
    @ProcessTransactional
    public InstanceResource start(@RequestBody ProcessExecutionCommand command) throws Exception {

        // FIXME: remove me
        String userId = SecurityAwareServletFilter.getUserId();
        GlobalContext.setUserId(userId);

        boolean simulation = command.getSimulation();
        String filePath = command.getProcessDefinitionId();
        String corrKeyValue = command.getCorrelationKeyValue();
        String groups = command.getGroups();

        Object definition;
        try {
            String defPath = java.net.URLDecoder.decode(filePath, "UTF-8");
            if (simulation) {
                definition = definitionService.getDefinition(defPath, null); // if simulation time, use the version
            } else {
                String version = findHighestNumberedFileName(defPath);
                definition = definitionService.getDefinition(defPath, version);
            }
            // under construction
        } catch (ClassNotFoundException cnfe) {
            // ClassNotFoundException을 처리하고, 500 Internal Server Error 반환
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Class not found", cnfe);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getCause().toString(), e);
        }

        if (definition instanceof ProcessDefinition) {
            ProcessDefinition processDefinition = (ProcessDefinition) definition;

            try {
                // org.uengine.kernel.ProcessInstance instance =
                // AbstractProcessInstance.create(processDefinition, command.getInstanceName(),
                // null);

                org.uengine.kernel.ProcessInstance instance = AbstractProcessInstance.create(processDefinition,
                        processDefinition.getName(), null);
                // invokeActivityFilters(null, instance);

                org.uengine.five.dto.RoleMapping[] roleMappings = command.getRoleMappings();
                if (roleMappings != null) {
                    for (org.uengine.five.dto.RoleMapping roleMapping : roleMappings) {
                        instance.putRoleMapping(roleMapping.getName(), roleMapping.toKernelRoleMapping());
                    }
                }

                if (corrKeyValue != null) {
                    ((JPAProcessInstance) instance).getProcessInstanceEntity().setCorrKey(corrKeyValue);
                }

                if (groups != null) {
                    instance.setGroups(groups);
                }

                org.uengine.five.dto.ProcessVariableValue[] processVariableValues = command.getProcessVariableValues();
                if (processVariableValues != null) {
                    for (org.uengine.five.dto.ProcessVariableValue pv : processVariableValues) {
                        if (pv.getName() == null)
                            continue;
                        java.io.Serializable val = (pv.getValues() != null && pv.getValues().length > 0)
                                ? (java.io.Serializable) pv.getValues()[0]
                                : null;
                        instance.set("", pv.getName(), val);
                    }
                }

                ((JPAProcessInstance) instance).getProcessInstanceEntity().setDefVerId(processDefinition.getVersion());
                // instance.setDefinitionVersionId(processDefinition.getVersion());
                instance.execute();
                try {
                    return new InstanceResource(instance);
                } catch (Exception linkEx) {
                    InstanceResource minimal = new InstanceResource();
                    minimal.setInstanceId(instance.getInstanceId());
                    minimal.setName(instance.getName());
                    minimal.setStatus(instance.getStatus());
                    return minimal;
                }
            } catch (Exception e) {
                e.printStackTrace();
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "Error executing process instance: " + e.getMessage(), e);
            }
        }
        return null;
    }

    private String findHighestNumberedFileName(String defPath) {
        if (!defPath.endsWith(".bpmn")) {
            defPath = defPath + ".bpmn";
        }
        File dir = new File("archive/" + defPath);
        if (!dir.exists() || !dir.isDirectory()) {
            return null;
        }

        File[] files = dir.listFiles();
        if (files == null || files.length == 0) {
            return null;
        }
        Arrays.sort(files, (f1, f2) -> {
            String name1 = f1.getName().replaceFirst("\\.bpmn$", "");
            String name2 = f2.getName().replaceFirst("\\.bpmn$", "");

            boolean isName1Version = isDotSeparatedNumericVersion(name1);
            boolean isName2Version = isDotSeparatedNumericVersion(name2);

            // Prefer version-like names, and sort them in descending order:
            // 0.11 > 0.10 > 0.9 > 0.3 ...
            if (isName1Version && isName2Version) {
                return compareDotSeparatedNumericVersions(name2, name1);
            } else if (isName1Version) {
                return -1;
            } else if (isName2Version) {
                return 1;
            } else {
                return name1.compareTo(name2);
            }
        });

        return files[0].getName().replaceFirst("\\.bpmn$", "");
    }

    private boolean isDotSeparatedNumericVersion(String str) {
        // e.g. "0.9", "0.10", "1", "1.0.3"
        return str != null && str.matches("\\d+(?:\\.\\d+)*");
    }

    private int compareDotSeparatedNumericVersions(String v1, String v2) {
        String[] p1 = v1.split("\\.");
        String[] p2 = v2.split("\\.");

        int max = Math.max(p1.length, p2.length);
        for (int i = 0; i < max; i++) {
            java.math.BigInteger n1 = i < p1.length ? new java.math.BigInteger(p1[i]) : java.math.BigInteger.ZERO;
            java.math.BigInteger n2 = i < p2.length ? new java.math.BigInteger(p2[i]) : java.math.BigInteger.ZERO;

            int cmp = n1.compareTo(n2);
            if (cmp != 0) {
                return cmp;
            }
        }

        // Same numeric version
        return 0;
    }

    @RequestMapping(value = "/instance/{instanceId}/stop", method = RequestMethod.POST)
    @ProcessTransactional
    public InstanceResource stop(@PathVariable("instanceId") String instanceId) throws Exception {

        ProcessInstance instance = getProcessInstanceLocal(instanceId);

        if (instance.isRunning(""))
            instance.stop();

        return new InstanceResource(instance);
    }

    @RequestMapping(value = "/instance/{instanceId}/suspend", method = RequestMethod.POST)
    @ProcessTransactional
    public InstanceResource suspend(@PathVariable("instanceId") String instanceId) throws Exception {

        ProcessInstance instance = getProcessInstanceLocal(instanceId);

        if (instance.isRunning("")) {
            List<ActivityInstanceContext> runningContexts = instance.getCurrentRunningActivitiesDeeply();

            for (ActivityInstanceContext runningContext : runningContexts) {

                runningContext.getActivity().suspend(runningContext.getInstance());

            }
        }

        return new InstanceResource(instance);
    }

    @RequestMapping(value = "/instance/{instanceId}/resume", method = RequestMethod.POST)
    @ProcessTransactional
    public InstanceResource resume(@PathVariable("instanceId") String instanceId) throws Exception {

        ProcessInstance instance = getProcessInstanceLocal(instanceId);

        if (instance.isRunning("")) {
            List<ActivityInstanceContext> suspendedContexts = instance.getActivitiesDeeply(Activity.STATUS_SUSPENDED);

            for (ActivityInstanceContext runningContext : suspendedContexts) {

                runningContext.getActivity().resume(runningContext.getInstance());

            }
        }

        return new InstanceResource(instance);
    }

    @RequestMapping(value = "/instance/{instanceId}", method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
    @ProcessTransactional(readOnly = true)
    public InstanceResource getInstance(@PathVariable("instanceId") String instanceId) throws Exception {

        ProcessInstance instance = getProcessInstanceLocal(instanceId);

        if (instance == null)
            throw new ResourceNotFoundException(); // make 404 error
        InstanceResource ir = new InstanceResource(instance);
        ir.setDefVer(instance.getProcessDefinition().getVersion());
        return ir;
    }

    @RequestMapping(value = "/instance/{instanceId}/eventList")
    @ProcessTransactional(readOnly = true)
    public Vector getEventList(@PathVariable("instanceId") String instanceId) throws Exception {
        ProcessInstance instance = getProcessInstanceLocal(instanceId);
        Vector messageListener = (Vector) instance.getMessageListeners("event");
        Vector<Map<String, String>> eventList = new Vector<>();
        for (Object listener : messageListener) {
            String eventListener = (String) listener;
            Activity act = instance.getProcessDefinition().getActivity(eventListener);
            if (!instance.getStatus(eventListener).equals("Running"))
                continue;
            String name = act.getName();
            Map<String, String> eventMap = new HashMap<>();
            eventMap.put("tracingTag", eventListener);
            eventMap.put("name", name);
            if (act instanceof Event) {
                Event event = (Event) act;
                eventMap.put("type", event.getClass().getSimpleName().replace("Event", ""));
            }
            eventList.add(eventMap);
        }

        return eventList;
    }

    /**
     * 프로세스를 특정 액티비티(태스크)까지 진행시켜, 해당 태스크가 Running 상태가 되도록 한다.
     * 목표 태스크가 Running이 되면 InstanceResource를 반환한다. 도달하지 못하면(프로세스 완료/타임아웃) 예외를 던진다.
     * ScenarioRunner.runUntilTarget과 동일한 방식으로 현재 액티비티를 완료/실행하며 목표에 도달할 때까지 진행한다.
     *
     * @param instanceId  프로세스 인스턴스 ID
     * @param tracingTag  목표 액티비티의 tracingTag (예: Activity_xxx)
     * @param body        (선택) payloadMapping(액티비티별 완료 시 넣을 값), maxAttempts(기본 30)
     * @return 목표 태스크가 Running 상태일 때만 InstanceResource 반환
     */
    @RequestMapping(value = "/instance/{instanceId}/advance-to-activity/{tracingTag}", method = RequestMethod.POST, consumes = "application/json;charset=UTF-8", produces = "application/json;charset=UTF-8")
    @ProcessTransactional
    @Transactional(rollbackFor = { Exception.class })
    public InstanceResource advanceToActivity(@PathVariable("instanceId") String instanceId,
            @PathVariable("tracingTag") String tracingTag,
            @RequestBody(required = false) Map<String, Object> body) throws Exception {

        ProcessInstance instance = getProcessInstanceLocal(instanceId);
        if (instance == null)
            throw new ResourceNotFoundException();
        if (instance.getStatus("").equals(Activity.STATUS_COMPLETED))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Process already completed; cannot advance to activity " + tracingTag);

        @SuppressWarnings("unchecked")
        Map<String, Object> payloadMapping = (body != null && body.get("payloadMapping") instanceof Map)
                ? (Map<String, Object>) body.get("payloadMapping")
                : new HashMap<>();
        int maxAttempts = (body != null && body.get("maxAttempts") != null)
                ? ((Number) body.get("maxAttempts")).intValue()
                : 30;

        ProcessDefinition def = instance.getProcessDefinition();
        if (def.getActivity(tracingTag) == null)
            throw new IllegalArgumentException("Activity not found: " + tracingTag);

        int attempts = 0;
        while (attempts < maxAttempts) {
            if (instance.getStatus("").equals(Activity.STATUS_COMPLETED))
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Process completed before reaching target activity " + tracingTag + "; target is not Running.");

            List<Activity> activities = instance.getCurrentRunningActivities();
            if (activities != null && !activities.isEmpty()) {
                Activity current = activities.get(0);
                String currentId = current.getTracingTag();
                if (currentId != null && currentId.equals(tracingTag)) {
                    if (Activity.STATUS_RUNNING.equals(instance.getStatus(tracingTag)))
                        return new InstanceResource(instance);
                }

                if (current instanceof ReceiveActivity) {
                    if (current instanceof HumanActivity) {
                        Map<String, Object> payload = getPayloadForActivity(payloadMapping, currentId);
                        try {
                            ((HumanActivity) current).fireReceived(instance, payload);
                        } catch (Exception ex) {
                            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                    "HumanActivity complete failed: " + currentId + " - " + ex.getMessage(), ex);
                        }
                    } else {
                        try {
                            instance.execute(currentId);
                        } catch (Exception ex) {
                            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                    "Execute failed: " + currentId + " - " + ex.getMessage(), ex);
                        }
                    }
                } else if (current instanceof Gateway) {
                    // 분기(Gateway)는 조건 평가 없이 목표 액티비티로 가는 플로우의 조건 변수만 설정 후 execute
                    setGatewayConditionForTarget(instance, current, tracingTag, def);
                    try {
                        instance.execute(currentId);
                    } catch (Exception ex) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                "Gateway execute failed: " + currentId + " - " + ex.getMessage(), ex);
                    }
                } else {
                    try {
                        instance.execute(currentId);
                    } catch (Exception ex) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                "Execute failed: " + currentId + " - " + ex.getMessage(), ex);
                    }
                }
                try {
                    Thread.sleep(300);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Interrupted");
                }
            } else {
                // 현재 실행 중인 액티비티 없음 + 프로세스 Running → Gateway 완료 후 조건 미충족으로 다음이 안 떠 있는 경우.
                // 목표로 가는 모든 Gateway의 조건 변수 설정 후 목표 액티비티를 직접 execute.
                setAnyGatewayConditionForTarget(instance, def, tracingTag);
                try {
                    instance.execute(tracingTag);
                } catch (Exception ex) {
                    // execute 실패 시(이미 완료 등) 다음 루프에서 재시도
                }
                try {
                    Thread.sleep(300);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Interrupted");
                }
            }
            attempts++;
        }

        throw new ResponseStatusException(HttpStatus.REQUEST_TIMEOUT,
                "Timeout: could not advance to activity " + tracingTag + "; target is not in Running state.");
    }

    /**
     * 정의 내 모든 Gateway 중 목표 액티비티로 가는 경로에 있는 것에 대해 조건 변수 설정.
     * getCurrentRunningActivities()가 비어 있을 때(Gateway 완료 후 다음 미시작) 사용.
     */
    @SuppressWarnings("unchecked")
    private void setAnyGatewayConditionForTarget(ProcessInstance instance, ProcessDefinition def, String targetTracingTag)
            throws Exception {
        java.util.Hashtable<String, Activity> whole = def.getWholeChildActivities();
        if (whole == null) return;
        for (Activity act : whole.values()) {
            if (act instanceof Gateway)
                setGatewayConditionForTarget(instance, act, targetTracingTag, def);
        }
    }

    /**
     * Gateway에서 목표 액티비티(tracingTag)로 가는 아웃고잉 플로우를 찾아,
     * 해당 플로우의 조건(Evaluate)이 있으면 조건 변수를 설정해 둔다. 조건을 무시하지 않고 목표 쪽으로 가도록 변수만 맞춘다.
     */
    private void setGatewayConditionForTarget(ProcessInstance instance, Activity gateway, String targetTracingTag,
            ProcessDefinition def) throws Exception {
        if (gateway.getOutgoingSequenceFlows() == null)
            return;
        for (SequenceFlow flow : gateway.getOutgoingSequenceFlows()) {
            if (flow.getTargetRef() == null)
                continue;
            if (!flowLeadsToTarget(def, flow.getTargetRef(), targetTracingTag, new HashSet<>()))
                continue;
            Condition cond = flow.getCondition();
            if (cond instanceof Evaluate) {
                Evaluate e = (Evaluate) cond;
                if (e.getKey() != null && !e.getKey().isEmpty())
                    instance.set("", e.getKey(), (Serializable) e.getValue());
            }
            return;
        }
    }

    private boolean flowLeadsToTarget(ProcessDefinition def, String activityId, String targetTracingTag, Set<String> visited) {
        if (activityId == null || activityId.equals(targetTracingTag))
            return true;
        if (visited.contains(activityId))
            return false;
        visited.add(activityId);
        Activity act = def.getActivity(activityId);
        if (act == null || act.getOutgoingSequenceFlows() == null)
            return false;
        for (SequenceFlow flow : act.getOutgoingSequenceFlows()) {
            if (flow.getTargetRef() != null && flowLeadsToTarget(def, flow.getTargetRef(), targetTracingTag, visited))
                return true;
        }
        return false;
    }

    private Map<String, Object> getPayloadForActivity(Map<String, Object> payloadMapping, String activityId) {
        if (payloadMapping == null || payloadMapping.isEmpty())
            return new HashMap<>();
        Object val = payloadMapping.get(activityId);
        if (val instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> m = (Map<String, Object>) val;
            return new HashMap<>(m);
        }
        return new HashMap<>(payloadMapping);
    }

    /**
     * 프로세스를 특정 액티비티(태스크)에서부터 실행한다.
     * ScenarioRunner의 startFromActivityId + execute(startFromId)와 동일. 변수 설정 후 해당 액티비티를 실행한다.
     *
     * @param instanceId 프로세스 인스턴스 ID
     * @param tracingTag 시작할 액티비티의 tracingTag
     * @param body       (선택) variables: 프로세스 변수 맵 (Given과 동일)
     */
    @RequestMapping(value = "/instance/{instanceId}/state/start-from-activity/{tracingTag}", method = RequestMethod.POST, consumes = "application/json;charset=UTF-8", produces = "application/json;charset=UTF-8")
    @ProcessTransactional
    @Transactional(rollbackFor = { Exception.class })
    public InstanceResource startFromActivity(@PathVariable("instanceId") String instanceId,
            @PathVariable("tracingTag") String tracingTag,
            @RequestBody(required = false) Map<String, Object> body) throws Exception {

        ProcessInstance instance = getProcessInstanceLocal(instanceId);
        if (instance == null)
            throw new ResourceNotFoundException();
        ProcessDefinition def = instance.getProcessDefinition();
        Activity activity = def.getActivity(tracingTag);
        if (activity == null)
            throw new IllegalArgumentException("Activity not found: " + tracingTag);

        if (body != null && body.get("variables") instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> variables = (Map<String, Object>) body.get("variables");
            for (Map.Entry<String, Object> e : variables.entrySet())
                instance.set("", e.getKey(), (Serializable) e.getValue());
        }

        instance.execute(tracingTag);
        return new InstanceResource(instance);
    }

    @RequestMapping(value = "/instance/{instanceId}/activity/{tracingTag}/backToHere", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    @ProcessTransactional
    @Transactional(rollbackFor = { Exception.class })
    public InstanceResource backToHere(@PathVariable("instanceId") String instanceId,
            @PathVariable("tracingTag") String tracingTag) throws Exception {

        ProcessInstance instance = getProcessInstanceLocal(instanceId);
        String execScope = null;
        if (tracingTag.contains(":")) {
            execScope = tracingTag.split(":")[1];
            tracingTag = tracingTag.split(":")[0];
        }
        if (execScope != null) {
            instance.setExecutionScope(execScope);
        }

        instance.getProcessDefinition().getActivity(tracingTag).backToHere(instance);
        // System.out.println("**********************");
        // System.out.println("getInstanceId : " + instance.getInstanceId());
        // System.out.println("getExecutionScopeContext : " +
        // instance.getExecutionScopeContext());
        // System.out.println("**********************");
        // ProcessDefinition definition = instance.getProcessDefinition();
        // List<Activity> list = new ArrayList<Activity>();

        // Activity returningActivity = definition.getActivity(tracingTag);

        // // returningActivity.compensateToThis(instance);

        // definition.gatherPropagatedActivitiesOf(instance, returningActivity, list);

        // Activity proActiviy;
        // for (int i = list.size() - 1; i >= 0; i--) {
        // proActiviy = list.get(i);
        // // compensate
        // proActiviy.compensate(instance);
        // }

        // returningActivity.resume(instance);
        // /*
        // * ProcessDefinition extends FlowActivity 상속하고 있기 때문에,
        // * List list = new ArrayList();
        // * definition.gatherPropagatedActivitiesOf(instance,
        // * definition.getWholeChildActivity(tracingTag), list);
        // *
        // * list 를 역순으로 하여 발견된 각 activity 들에 대해 compensate() 호출
        // */

        return new InstanceResource(instance);
    }

    @RequestMapping(value = "/instance/{instanceId}/variables", method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
    @ProcessTransactional(readOnly = true)
    public Map getProcessVariables(@PathVariable("instanceId") String instanceId) throws Exception {

        ProcessInstance instance = getProcessInstanceLocal(instanceId);

        // 여기서도 롤매핑이 들어가면 시리얼라이즈 에러가 나옴.
        Map variables = ((DefaultProcessInstance) instance).getVariables();

        // 저장 시점에 DB 상태가 인스턴스 파일에 동기화되므로, 여기서는 별도 동기화 불필요
        return variables;
    }

    @RequestMapping(value = "/instance/{instanceId}/status/{executionScope}", method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
    @ProcessTransactional(readOnly = true)
    public Map getActivitiesStatus(@PathVariable("instanceId") String instanceId,
            @PathVariable(value = "executionScope", required = false) String executionScope)
            throws Exception {
        ProcessInstance instance = getProcessInstanceLocal(instanceId);

        Map variables = ((DefaultProcessInstance) instance).getVariables();
        Map<String, Object> filteredVariables = new HashMap<>();

        String execScope = (executionScope == null) ? "0" : executionScope;

        for (Object key : variables.keySet()) {
            if (key instanceof String) {
                String keyStr = (String) key;
                if (keyStr.matches("Activity_\\w+:" + execScope + ":_status:prop")
                        || keyStr.matches("Gateway_\\w+:" + execScope + ":_status:prop")
                        || keyStr.matches("Event_\\w+:" + execScope + ":_status:prop")
                        || keyStr.matches("Flow_\\w+:" + execScope + ":_status:prop")) {
                    String newKey = keyStr.replace(":" + execScope + ":_status:prop", "");
                    filteredVariables.put(newKey, variables.get(key));

                    Activity activity = instance.getProcessDefinition().getActivity(newKey);
                    if (activity != null) {
                        System.out.println("Activity Name: " + activity.getName() + ", New Key: " + newKey
                                + ", Status: " + variables.get(key));
                    }
                } else if (keyStr.matches("Activity_\\w+:_status:prop")
                        || keyStr.matches("Gateway_\\w+:_status:prop")
                        || keyStr.matches("Event_\\w+:_status:prop")
                        || keyStr.matches("Flow_\\w+:_status:prop")) {
                    String newKey = keyStr.replace(":_status:prop", "");
                    if (!filteredVariables.containsKey(newKey)) {
                        filteredVariables.put(newKey, variables.get(key));
                    }
                    Activity activity = instance.getProcessDefinition().getActivity(newKey);
                    if (activity != null) {
                        System.out.println("Activity Name: " + activity.getName() + ", New Key: " + newKey
                                + ", Status: " + variables.get(key));
                    }
                }
            }
        }
        variables = filteredVariables;

        return variables;

    }

    @RequestMapping(value = "/instance/{instanceId}/running", method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
    @ProcessTransactional(readOnly = true)
    public ResponseEntity<List<WorklistEntity>> getRunningTaskId(@PathVariable("instanceId") String instanceId)
            throws Exception {

        List<WorklistEntity> worklistEntity = worklistRepository
                .findCurrentWorkItemByInstId(Long.parseLong(instanceId));
        return ResponseEntity.ok(worklistEntity);
    }

    @RequestMapping(value = "/instance/{instanceId}/completed", method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
    @ProcessTransactional(readOnly = true)
    public ResponseEntity<List<WorklistEntity>> getCompletedTaskId(@PathVariable("instanceId") String instanceId)
            throws Exception {

        List<WorklistEntity> worklistEntity = worklistRepository
                .findWorkListByInstId(Long.parseLong(instanceId));
        return ResponseEntity.ok(worklistEntity);
    }

    /**
     * 1) 인스턴스ID(루트 인스턴스ID)로 모든 태스크를 조회 (히스토리 표기 용도)
     * - 서브프로세스 태스크 포함(rootInstId 기준)
     */
    @RequestMapping(value = "/instance/{instanceId}/worklists", method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
    @ProcessTransactional(readOnly = true)
    public ResponseEntity<List<WorklistEntity>> getAllTasksByInstanceId(@PathVariable("instanceId") String instanceId) {
        Long rootInstId = Long.parseLong(instanceId);
        List<WorklistEntity> tasks = processInstanceRepository.findAllWorklistsByRootInstId(rootInstId);
        return ResponseEntity.ok(tasks);
    }


    @RequestMapping(value = "/instance/{instId}/variable/{varName}", method = RequestMethod.GET)
    @ProcessTransactional(readOnly = true)
    public Serializable getVariable(@PathVariable("instId") String instId, @PathVariable("varName") String varName)
            throws Exception {
        ProcessInstance instance = getProcessInstanceLocal(instId);
        return instance.get("", varName);
    }

    @RequestMapping(value = "/instance/{instId}/task/{taskId}/variable/{varName}", method = RequestMethod.GET)
    @ProcessTransactional(readOnly = true)
    public Serializable getVariableWithTaskId(@PathVariable("instId") String instId,
            @PathVariable("taskId") String taskId,
            @PathVariable("varName") String varName)
            throws Exception {
        ProcessInstance instance = getProcessInstanceLocal(instId);
        Serializable result = null;
        WorkItemResource workItem = getWorkItem(taskId);
        ExecutionScopeContext oldExecutionScopeContext = instance.getExecutionScopeContext();

        if (workItem.getWorklist().getExecScope() != null) {
            if (instance.getExecutionScopeContext() == null) {
                ExecutionScopeContext executionScopeContext = new ExecutionScopeContext();
                executionScopeContext.setExecutionScope(workItem.getWorklist().getExecScope());
                instance.setExecutionScopeContext(executionScopeContext);
            }
        }
        result = instance.get("", varName);
        instance.setExecutionScopeContext(oldExecutionScopeContext);
        return result;
    }

    @RequestMapping(value = "/instance/{instanceId}/variable/{varName}", method = RequestMethod.POST, produces = "application/json; charset=UTF-8")
    @ProcessTransactional
    public void setVariable(@PathVariable("instanceId") String instanceId, @PathVariable("varName") String varName,
            @RequestBody String json) throws Exception {
        ProcessInstance instance = getProcessInstanceLocal(instanceId);
        Serializable value = arrayObjectMapper.readValue(json, Serializable.class);
        instance.set("", varName, value);
    }

    @RequestMapping(value = "/instance/{instanceId}/task/{taskId}/variable/{varName}", method = RequestMethod.POST, produces = "application/json; charset=UTF-8")
    @ProcessTransactional
    public void setVariableWithTaskId(@PathVariable("instanceId") String instanceId,
            @PathVariable("taskId") String taskId, @PathVariable("varName") String varName,
            @RequestBody String json) throws Exception {
        setVariableWithTaskId(instanceId, taskId, varName, json, null);
    }

    public void setVariableWithTaskId(String instanceId,
            String taskId, String varName,
            @RequestBody String json, WorkItemResource workItemResource) throws Exception {
        ProcessInstance instance = getProcessInstanceLocal(instanceId);
        WorkItemResource workItem = workItemResource != null ? workItemResource : getWorkItem(taskId);
        ExecutionScopeContext oldExecutionScopeContext = instance.getExecutionScopeContext();

        if (workItem.getWorklist() != null && workItem.getWorklist().getExecScope() != null) {
            if (instance.getExecutionScopeContext() == null) {
                ExecutionScopeContext executionScopeContext = new ExecutionScopeContext();
                executionScopeContext.setExecutionScope(workItem.getWorklist().getExecScope());
                instance.setExecutionScopeContext(executionScopeContext);
            }
        }
        Serializable value = arrayObjectMapper.readValue(json, Serializable.class);
        instance.set("", varName, value);

        instance.setExecutionScopeContext(oldExecutionScopeContext);
    }

    @RequestMapping(value = "/instance/{instId}/role-mapping/{roleName}", method = RequestMethod.GET)
    public RoleMapping getRoleMapping(@PathVariable("instId") String instId, @PathVariable("roleName") String roleName)
            throws Exception {

        ProcessInstance instance = applicationContext.getBean(
                ProcessInstance.class,
                new Object[] {
                        null,
                        instId,
                        null
                });

        return instance.getRoleMapping(roleName);
    }

    // Spring Data rest 에서는 자동객체를 JSON으로 바인딩 해주지만, 원래 스프링에서는 리스폰스에 대해 스프링 프레임웤이 해석할
    // 수 있는 미디어타입을 xml 에 일일히 설정했었음.
    // produces 의 의미는. 리스폰스 헤더에 콘텐트타입을 설정해줌. 그래야 브라우저가 json 객체로 받아들인다.
    @RequestMapping(value = "/instance/{instanceId}/role-mapping/{roleName}", method = RequestMethod.POST, produces = "application/json; charset=UTF-8")
    public Object setRoleMapping(@PathVariable("instanceId") String instanceId,
            @PathVariable("roleName") String roleName, @RequestBody RoleMappingCommand roleMapping) throws Exception {

        ProcessInstance instance = applicationContext.getBean(
                ProcessInstance.class,
                new Object[] {
                        null,
                        instanceId,
                        null
                });
        RoleMapping rm = RoleMapping.create();
        if (rm != null && roleMapping != null) {
            if (roleMapping.getEndpoint() != null) rm.setEndpoint(roleMapping.getEndpoint());
            if (roleMapping.getResourceName() != null) rm.setResourceName(roleMapping.getResourceName());
            if (roleMapping.getScope() != null) rm.setScope(roleMapping.getScope());
            if (roleMapping.getAssignType() != null) rm.setAssignType(roleMapping.getAssignType());
        }

        instance.putRoleMapping(roleName, rm);
        rm.setName(roleName);
        return rm;
    }

    @RequestMapping(value = "/instance/{instanceId}/role-mapping/{roleName}", method = RequestMethod.PUT, produces = "application/json; charset=UTF-8")
    public Object putRoleMapping(@PathVariable("instanceId") String instanceId,
            @PathVariable("roleName") String roleName, @RequestBody RoleMappingCommand roleMapping) throws Exception {

        ProcessInstance instance = applicationContext.getBean(
                ProcessInstance.class,
                new Object[] {
                        null,
                        instanceId,
                        null
                });
       
        RoleMapping currentMapping = instance.getRoleMapping(roleName);
        if (currentMapping != null && roleMapping != null) {
            if (roleMapping.getEndpoint() != null) currentMapping.setEndpoint(roleMapping.getEndpoint());
            if (roleMapping.getResourceName() != null) currentMapping.setResourceName(roleMapping.getResourceName());
            if (roleMapping.getScope() != null) currentMapping.setScope(roleMapping.getScope());
            if (roleMapping.getAssignType() != null) currentMapping.setAssignType(roleMapping.getAssignType());
        }

        return currentMapping;
    }

    /**
     * use this rather ProcessManagerRemote.getProcessInstance() method instead
     * 
     * @param instanceId
     * @return
     */

    @GetMapping("/instance/{instanceId}")
    public ProcessInstance getProcessInstanceLocal(@PathVariable("instanceId") String instanceId) {

        ProcessInstance instance = ProcessTransactionContext.getThreadLocalInstance()
                .getProcessInstanceInTransaction(instanceId);
        if (instance != null) {
            return instance;
        }
        instance = applicationContext.getBean(
                ProcessInstance.class,
                new Object[] { null, instanceId, null });
        return instance;

    }

    final static String SERVICES_ROOT = "services";

    @Autowired
    ServiceEndpointRepository serviceEndpointRepository;

    @Autowired
    ProcessInstanceRepository processInstanceRepository;

    @ProcessTransactional
    @RequestMapping(value = "/instance/{instanceId}/signal/{signal}", method = {
            RequestMethod.POST }, produces = "application/json;charset=UTF-8")
    public Object signal(@PathVariable("instanceId") String instanceId, @PathVariable("signal") String signal)
            throws Exception {

        ProcessInstance instance = getProcessInstanceLocal(instanceId);
        Map<String, SignalEventInstance> signalEventInstanceMap = SignalIntermediateCatchEvent
                .getSignalEvents(instance);

        SignalEventInstance signalEventInstance = signalEventInstanceMap.get(signal);

        Activity activity = instance.getProcessDefinition().getActivity(signalEventInstance.getActivityRef());

        if (activity instanceof SignalIntermediateCatchEvent) {
            ((SignalIntermediateCatchEvent) activity).onMessage(instance, null);
        }

        return null;
    }

    // @ProcessTransactional
    // @RequestMapping(value = SERVICES_ROOT + "/**", method = { RequestMethod.GET,
    // RequestMethod.POST }, produces = "application/json;charset=UTF-8")
    // public Object serviceMessage(HttpServletRequest request) throws Exception {

    // String path = (String)
    // request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);

    // if (path == null || path.length() == 0)
    // throw new ResourceNotFoundException();

    // ServiceEndpointEntity serviceEndpointEntity = serviceEndpointRepository
    // .findById(path.substring(SERVICES_ROOT.length() + 2)).get();

    // if (serviceEndpointEntity == null)
    // throw new ResourceNotFoundException();

    // // find the correlated instance:
    // List<ProcessInstanceEntity> correlatedProcessInstanceEntities = null;
    // Object correlationData = null;
    // // ObjectInstance objectInstance = new ObjectInstance();

    // if ("POST".equals(request.getMethod())) {

    // ByteArrayOutputStream bao = new ByteArrayOutputStream();
    // UEngineUtil.copyStream(request.getInputStream(), bao);

    // JsonNode jsonNode = objectMapper.readTree(bao.toByteArray());

    // // convert jsonNode to object instance.
    // Iterator<String> fieldNames = jsonNode.fieldNames();
    // while (fieldNames.hasNext()) {
    // String fieldName = fieldNames.next();

    // Object childNode = jsonNode.get(fieldName);
    // Object converted = null;

    // if (childNode instanceof TextNode) {
    // converted = ((TextNode) childNode).textValue();
    // } else if (childNode instanceof ValueNode) {
    // converted = ((ValueNode) childNode).textValue();
    // } else
    // converted = childNode;

    // // objectInstance.setBeanProperty(fieldName, converted);
    // }

    // correlationData =
    // jsonNode.get(serviceEndpointEntity.getEvents().get(0).getCorrelationKey()).asText();

    // if (correlationData != null)
    // correlatedProcessInstanceEntities = processInstanceRepository
    // .findByCorrKeyAndStatus(correlationData.toString(), Activity.STATUS_RUNNING);
    // }

    // ProcessInstanceEntity processInstanceEntity;
    // if (correlatedProcessInstanceEntities == null ||
    // correlatedProcessInstanceEntities.size() == 0)
    // processInstanceEntity = null;
    // else {
    // processInstanceEntity = correlatedProcessInstanceEntities.get(0);
    // if (correlatedProcessInstanceEntities.size() > 1)
    // System.err.println("More than one correlated process instance found!");
    // }

    // JPAProcessInstance instance = null;

    // // case that correlation instance exists and is running:
    // if (processInstanceEntity != null) {
    // instance = (JPAProcessInstance)
    // getProcessInstanceLocal(String.valueOf(processInstanceEntity.getInstId()));

    // } else { // if no instances running, create new instance:
    // Object definition =
    // definitionService.getDefinition(serviceEndpointEntity.getEvents().get(0).getDefId(),
    // true);

    // ProcessDefinition processDefinition = (ProcessDefinition) definition;

    // instance = (JPAProcessInstance) applicationContext.getBean(
    // ProcessInstance.class,
    // // new Object[]{
    // processDefinition,
    // null,
    // null
    // // }
    // );

    // instance.execute();
    // }

    // // trigger the start or intermediate message catch events:
    // List<ActivityInstanceContext> runningActivities =
    // instance.getCurrentRunningActivitiesDeeply();

    // boolean neverTreated = true;

    // if (runningActivities != null) {
    // for (ActivityInstanceContext activityInstanceContext : runningActivities) {
    // Activity activity = activityInstanceContext.getActivity();

    // if (activity instanceof CatchingRestMessageEvent) {
    // CatchingMessageEvent catchingMessageEvent = (CatchingMessageEvent) activity;

    // boolean treated =
    // catchingMessageEvent.onMessage(activityInstanceContext.getInstance(), null);
    // if (treated)
    // neverTreated = false;
    // }
    // }
    // }

    // if (neverTreated) {
    // instance.stop();

    // return "문제가 발생하여 처음으로 돌아갑니다.";
    // }

    // // set correlation key so that this instance could be re-visited by the
    // // recurring requester.
    // if (instance.isNewInstance() && correlationData != null)
    // instance.getProcessInstanceEntity().setCorrKey(correlationData.toString());

    // // List<String> history = instance.getActivityCompletionHistory();
    // // if(history!=null){
    // // for(String tracingTag : history){
    // //
    // // Activity activityDone =
    // // instance.getProcessDefinition().getActivity(tracingTag);
    // //
    // // if(activityDone instanceof SendTask){
    // // SendTask sendTask = (SendTask) activityDone;
    // //
    // // if(sendTask.getDataInput() != null && sendTask.getDataInput().getName() !=
    // // null)
    // // return sendTask.getDataInput().get(instance, "");
    // // else {
    // // return sendTask.getInputPayloadTemplate();
    // // }
    // // }
    // //
    // // }
    // //
    // // }
    // List<String> messageQueue = SendTask.getMessageQueue(instance);

    // if (messageQueue != null && messageQueue.size() > 0) {

    // // StringBuffer fullMessage = new StringBuffer();
    // //
    // // for(String message : messageQueue){
    // // fullMessage.append(message);
    // // }

    // return messageQueue.get(messageQueue.size() - 1).toString().replace("\n",
    // "").replace("\r", "");

    // }

    // return null;
    // }

    @ProcessTransactional
    @RequestMapping(value = SERVICES_ROOT + "/**", method = { RequestMethod.GET,
            RequestMethod.POST }, produces = "application/json;charset=UTF-8")
    public Object serviceMessage(HttpServletRequest request,
            @QueryParam("correlationValue") String correlationValue,
            @QueryParam("correlationKey") String correlationKey) throws Exception {

        String path = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);

        if (path == null || path.length() == 0)
            throw new ResourceNotFoundException();

        ServiceEndpointEntity serviceEndpointEntity = serviceEndpointRepository
                .findById(path.substring(SERVICES_ROOT.length() + 2)).get();

        if (serviceEndpointEntity == null)
            throw new ResourceNotFoundException();

        // find the correlated instance:
        List<ProcessInstanceEntity> correlatedProcessInstanceEntities = null;
        // Object correlationData = null;
        // ObjectInstance objectInstance = new ObjectInstance();

        if ("POST".equals(request.getMethod())) {

            ByteArrayOutputStream bao = new ByteArrayOutputStream();
            UEngineUtil.copyStream(request.getInputStream(), bao);

            JsonNode jsonNode = objectMapper.readTree(bao.toByteArray());

            // convert jsonNode to object instance.
            Iterator<String> fieldNames = jsonNode.fieldNames();
            while (fieldNames.hasNext()) {
                String fieldName = fieldNames.next();

                Object childNode = jsonNode.get(fieldName);
                Object converted = null;

                if (childNode instanceof TextNode) {
                    converted = ((TextNode) childNode).textValue();
                } else if (childNode instanceof ValueNode) {
                    converted = ((ValueNode) childNode).textValue();
                } else
                    converted = childNode;

                // objectInstance.setBeanProperty(fieldName, converted);
            }

            // correlationData = correlationKey;

            if (correlationValue != null)
                correlatedProcessInstanceEntities = processInstanceRepository
                        .findByCorrKeyAndStatus(correlationValue, Activity.STATUS_RUNNING);
        }

        ProcessInstanceEntity processInstanceEntity;
        if (correlatedProcessInstanceEntities == null ||
                correlatedProcessInstanceEntities.size() == 0)
            processInstanceEntity = null;
        else {
            processInstanceEntity = correlatedProcessInstanceEntities.get(0);
            if (correlatedProcessInstanceEntities.size() > 1)
                System.err.println("More than one correlated process instance found!");
        }

        JPAProcessInstance instance = null;

        // case that correlation instance exists and is running:
        if (processInstanceEntity != null) {
            instance = (JPAProcessInstance) getProcessInstanceLocal(String.valueOf(processInstanceEntity.getInstId()));

        } else { // if no instances running, create new instance:
            Object definition = definitionService.getDefinition(serviceEndpointEntity.getEvents().get(0).getDefId(),
                    null);

            ProcessDefinition processDefinition = (ProcessDefinition) definition;

            instance = (JPAProcessInstance) applicationContext.getBean(
                    ProcessInstance.class,
                    // new Object[]{
                    processDefinition,
                    null,
                    null
            // }
            );

            instance.execute();
        }

        // trigger the start or intermediate message catch events:
        List<ActivityInstanceContext> runningActivities = instance.getCurrentRunningActivitiesDeeply();// TODO 확인 필

        boolean neverTreated = true;

        if (runningActivities != null) {
            for (ActivityInstanceContext activityInstanceContext : runningActivities) {
                Activity activity = activityInstanceContext.getActivity();

                if (activity instanceof CatchingRestMessageEvent) {
                    CatchingRestMessageEvent catchingMessageEvent = (CatchingRestMessageEvent) activity;
                    if (correlationKey.equals(catchingMessageEvent.getCorrelationKey())) {
                        boolean treated = catchingMessageEvent.onMessage(activityInstanceContext.getInstance(), null);

                        if (treated)
                            neverTreated = false;
                    }
                }
            }
        }

        if (neverTreated) {
            instance.stop();

            return "문제가 발생하여 처음으로 돌아갑니다.";
        }

        // set correlation key so that this instance could be re-visited by the
        // recurring requester.
        if (instance.isNewInstance() && correlationValue != null)
            instance.getProcessInstanceEntity().setCorrKey(correlationValue);

        // List<String> history = instance.getActivityCompletionHistory();
        // if(history!=null){
        // for(String tracingTag : history){
        //
        // Activity activityDone =
        // instance.getProcessDefinition().getActivity(tracingTag);
        //
        // if(activityDone instanceof SendTask){
        // SendTask sendTask = (SendTask) activityDone;
        //
        // if(sendTask.getDataInput() != null && sendTask.getDataInput().getName() !=
        // null)
        // return sendTask.getDataInput().get(instance, "");
        // else {
        // return sendTask.getInputPayloadTemplate();
        // }
        // }
        //
        // }
        //
        // }
        List<String> messageQueue = SendTask.getMessageQueue(instance);

        if (messageQueue != null && messageQueue.size() > 0) {

            // StringBuffer fullMessage = new StringBuffer();
            //
            // for(String message : messageQueue){
            // fullMessage.append(message);
            // }

            return messageQueue.get(messageQueue.size() - 1).toString().replace("\n",
                    "").replace("\r", "");

        }

        return null;
    }

    @RequestMapping(value = "/work-item/{taskId}", method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
    public WorkItemResource getWorkItem(@PathVariable("taskId") String taskId) throws Exception {
        if (taskId == null || taskId.equals("null"))
            return null;
        WorklistEntity worklistEntity = worklistRepository.findById(new Long(taskId)).get();
        if (worklistEntity == null) {
            throw new Exception("No such work item where taskId = " + taskId);
        }

        String defId = worklistEntity.getDefId();
        ProcessDefinition definition = (ProcessDefinition) definitionService.getDefinition(defId,
                worklistEntity.getDefVerId());
        HumanActivity activity = (HumanActivity) definition.getActivity(worklistEntity.getTrcTag());

        WorkItemResource workItem = new WorkItemResource();
        workItem.setActivity(activity); // defaultHandler
        workItem.setWorklist(worklistEntity); // handler:http/

        String instanceId = worklistEntity.getInstId().toString();
        ProcessInstance instance = getProcessInstanceLocal(instanceId);

        // get the parameter values and set them to the "workItem.parameterValues" so
        // that WorkItemHandler.vue can insert the default values
        Map parameterValues = new HashMap<String, Object>();
        if (activity.getParameters() != null) {
            for (ParameterContext parameterContext : activity.getParameters()) {
                if (parameterContext.getVariable() != null && parameterContext.getDirection().indexOf("in") == 0) {
                    parameterValues.put(parameterContext.getArgument().getText(),
                            parameterContext.getVariable().get(instance, "", ""));
                }
            }
        }

        if (workItem.getWorklist().getExecScope() != null) {
            if (instance.getExecutionScopeContext() == null) {
                ExecutionScopeContext executionScopeContext = new ExecutionScopeContext();
                executionScopeContext.setExecutionScope(workItem.getWorklist().getExecScope());
                instance.setExecutionScopeContext(executionScopeContext);
            }
        }

        if (activity instanceof ReceiveActivity) {
            Map<String, Object> mappingInValues = activity.getMappingInValues(instance);
            if (mappingInValues.size() > 0) {
                for (Map.Entry<String, Object> entry : mappingInValues.entrySet()) {
                    parameterValues.put(entry.getKey(), entry.getValue());
                }
            }
        }

        if (parameterValues.size() > 0) {
            workItem.setParameterValues(parameterValues);
        }

        if (activity.getStatus(instance).equals(Activity.STATUS_COMPLETED)
                && instance instanceof JPAProcessInstance) {
            Map<String, Object> payloadValues = getPayloadValues((JPAProcessInstance) instance, activity);
            if (payloadValues != null) {
                workItem.setParameterValues(payloadValues);
            }
        }

        workItem.getWorklist().setProcessInstance(null); // disconnect recursive json path

        return workItem;
    }

    private Map<String, Object> getPayloadValues(JPAProcessInstance instance, Activity activity) throws Exception {
        Date date = instance.getProcessInstanceEntity().getStartedDate();
        String currentYear = String.valueOf(date.getYear() + 1900);
        String currentMonth = String.format("%02d", date.getMonth() + 1);
        String currentDay = String.format("%02d", date.getDate());
        IResource resource = new DefaultResource(
                "payloads/" + currentYear + "/" + currentMonth + "/" + currentDay + "/" + instance.getInstanceId());

        boolean resourceExists = resourceManager.exists(resource);
        if (resourceExists) {
            Map<String, String> payloadMap = (Map) resourceManager.getObject(resource);
            String payloadKey = activity.getTracingTag() + "_payload@" + instance.getInstanceId() + ":";
            if (payloadMap.containsKey(payloadKey)) {
                String payload = payloadMap.get(payloadKey);
                ObjectMapper objectMapper = new ObjectMapper();
                return objectMapper.readValue(payload, new TypeReference<Map<String, Object>>() {
                });
            }
        }
        return null;
    }

    @RequestMapping(value = "/work-item/{taskId}/save", method = RequestMethod.POST)
    @org.springframework.transaction.annotation.Transactional
    @ProcessTransactional // important!
    public void putWorkItem(@PathVariable("taskId") String taskId, @RequestBody WorkItemResource workItem)
            throws Exception {

        WorklistEntity worklistEntity = worklistRepository.findById(new Long(taskId)).get();

        String instanceId = worklistEntity.getInstId().toString();
        ProcessInstance instance = getProcessInstanceLocal(instanceId);

        HumanActivity humanActivity = ((HumanActivity) instance.getProcessDefinition()
                .getActivity(worklistEntity.getTrcTag()));

        if (!instance.isRunning(humanActivity.getTracingTag()) && !humanActivity.isNotificationWorkitem()) {
            throw new UEngineException("Illegal completion for workitem [" + humanActivity + ":"
                    + humanActivity.getStatus(instance) + "]: Already closed or illegal status.");
        }

        // map the argument list to variables change list
        Map variableChanges = new HashMap<String, Object>();

        if (workItem.getParameterValues() != null
                && humanActivity.getParameters() != null) {
            for (ParameterContext parameterContext : humanActivity.getParameters()) {
                if (parameterContext.getDirection().indexOf("out") >= 0
                        && workItem.getParameterValues().containsKey(parameterContext.getArgument().getText())) {

                    Serializable data = (Serializable) workItem.getParameterValues()
                            .get(parameterContext.getArgument().getText());
                    // if("REST".equals(parameterContext.getVariable().getPersistOption())){
                    // RestResourceProcessVariableValue restResourceProcessVariableValue = new
                    // RestResourceProcessVariableValue();
                    // data = restResourceProcessVariableValue.lightweight(data,
                    // parameterContext.getVariable(), instance);
                    // }

                    if (data instanceof Map && ((Map) data).containsKey("_type")) {
                        String typeName = null;
                        try {
                            typeName = (String) ((Map) data).get("_type");
                            Class classType = Thread.currentThread().getContextClassLoader().loadClass(typeName);
                            data = (Serializable) ProcessServiceApplication.objectMapper.convertValue(data, classType);
                        } catch (Exception e) {
                            throw new Exception("Error while convert map to type: " + typeName, e);
                        }
                    }

                    variableChanges.put(parameterContext.getVariable().getName(), data);
                }
            }
        }
    }

    public void writeToFile(String filePath, WorkItemResource workItem) throws Exception {
        IResource resource = new DefaultResource("test/" + filePath);
        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(resourceManager.getOutputStream(resource), StandardCharsets.UTF_8))) {
            if (!resourceManager.exists(resource)) {
                String workItemJson = objectMapper.writeValueAsString(workItem);
                writer.write(workItemJson);
            } else {

                Set<Object> existObj = readFromFile(filePath);
                existObj.removeIf(obj -> obj instanceof Map && ((Map) obj).containsKey("_type"));
                // Ensure no duplicate data
                existObj.add(workItem.getParameterValues());
                Set<Object> uniqueValues = new LinkedHashSet<>(existObj);

                // Clear the existing set and add back the unique values
                existObj.clear();
                existObj.addAll(uniqueValues);
                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.writerWithDefaultPrettyPrinter().withoutAttribute("_type").writeValue(
                        resourceManager.getOutputStream(resource), existObj);

                // "errorList" : [ "java.util.ArrayList", [ "aa", "bb" ] ]

                // objectMapper.writeValue(file, existObj);
            }
        }
    }

    public void writeToFileRecord(String filePath, String trcTag, String name, WorkItemResource workItem)
            throws Exception {

        IResource resource = new DefaultResource("test/" + filePath);

        // Read existing data from the file
        List<Object> existingData = readExistingData(filePath);
        HashMap<String, Object> newData = new HashMap<>();
        newData.put("name", name);
        newData.put("tracingTag", trcTag);
        newData.put("workItem", workItem.getParameterValues());

        existingData.add(newData);

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.writeValue(resourceManager.getOutputStream(resource), existingData);
    }

    //
    private List<Object> readExistingData(String filePath) throws Exception {
        IResource resource = new DefaultResource("test/" + filePath);
        if (!resourceManager.exists(resource)) {
            return new ArrayList<>();
        }
        StringBuilder contentBuilder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(resourceManager.getInputStream(resource), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                contentBuilder.append(line).append(System.lineSeparator());
            }
        }

        String fileContent = contentBuilder.toString();
        ObjectMapper objectMapper = new ObjectMapper();
        if (fileContent.trim().isEmpty()) {
            return new ArrayList<>();
        } else {
            return objectMapper.readValue(fileContent, new TypeReference<List<Object>>() {
            });
        }
    }

    public Set<Object> readFromFile(String filePath) throws Exception {
        IResource resource = new DefaultResource("test/" + filePath);
        if (!resourceManager.exists(resource)) {
            throw new FileNotFoundException("File not found: " + filePath);
        }
        StringBuilder contentBuilder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(resourceManager.getInputStream(resource), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                contentBuilder.append(line).append(System.lineSeparator());
            }
        }
        String fileContent = contentBuilder.toString();
        ObjectMapper objectMapper = new ObjectMapper();
        if (fileContent.length() == 0) {
            Set<Object> result = new HashSet<>();
            return result;
        } else {
            return objectMapper.readValue(fileContent, new TypeReference<Set<Object>>() {
            });
        }

    }

    @RequestMapping(value = "/test/**", method = RequestMethod.DELETE, produces = "application/json;charset=UTF-8")
    public void deleteTest(HttpServletRequest request, @RequestBody Map<String, Object> testData) throws Exception {
        System.out.println(testData);
        String folderPath = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);

        folderPath = folderPath.substring("/test/".length());

        // Check if the folderPath ends with "/record"
        if (folderPath.endsWith("/record")) {
            deleteRecordTest(folderPath, testData);
            return;
        }

        String filePath = folderPath + "/" + testData.get("tracingTag") + ".json";
        Set<Object> tmp = readFromFile(filePath);

        int indexToRemove = (int) testData.get("idx");
        if (indexToRemove >= 0 && indexToRemove < tmp.size()) {
            Iterator<Object> iterator = tmp.iterator();
            int currentIndex = 0;
            while (iterator.hasNext()) {
                iterator.next();
                if (currentIndex == indexToRemove) {
                    iterator.remove();
                    break;
                }
                currentIndex++;
            }
        }

        IResource resource = new DefaultResource("test/" + filePath);
        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(resourceManager.getOutputStream(resource), StandardCharsets.UTF_8))) {
            if (resourceManager.exists(resource)) {
                String workItemJson = objectMapper.writeValueAsString(tmp);
                writer.write(workItemJson);
            }
        }

        System.out.println(tmp);
    }

    public void deleteRecordTest(
            @PathVariable("recordPath") String recordPath,
            @RequestBody Map<String, Object> requestData) throws Exception {
        Object index = requestData.get("idx");
        System.out.println("Record Path: " + recordPath);
        System.out.println("Index: " + index);

        IResource resource = new DefaultResource("test/" + recordPath + "/" + index + ".json");
        if (resourceManager.exists(resource)) {
            resourceManager.delete(resource);
        }
    }

    @RequestMapping(value = "/test/**", method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
    public Map<String, Object> testList(HttpServletRequest request) throws Exception {
        String folderPath = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        folderPath = folderPath.substring("/test/".length());

        Map<String, Object> result = new HashMap<>();

        IContainer container = new ContainerResource();
        container.setPath("test/" + folderPath);
        if (!resourceManager.exists(container)) {
            throw new FileNotFoundException("Folder not found: " + folderPath);
        }
        List<IResource> files = resourceManager.listFiles(container);
        if (files != null) {
            for (IResource file : files) {
                if (!file.isContainer()) {
                    IResource resource = new DefaultResource(file.getPath());
                    InputStream inputStream = resourceManager.getInputStream(resource);
                    StringBuilder contentBuilder = new StringBuilder();
                    try (BufferedReader reader = new BufferedReader(
                            new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            contentBuilder.append(line).append(System.lineSeparator());
                        }
                    }

                    String fileNameWithoutExtension = resource.getName().substring(0, file.getName().lastIndexOf('.'));
                    result.put(fileNameWithoutExtension, contentBuilder.toString());
                }
            }
        }
        return result;
    }

    /**
     * 워크아이템 완료 핵심 로직 (내부 메서드).
     * REST API와 시나리오 테스트에서 공통으로 사용.
     * 
     * @param taskId     워크아이템 taskId
     * @param workItem   WorkItemResource (parameterValues, execScope 포함)
     * @param isSimulate "true"면 시뮬레이션 모드 (파일 기록)
     * @throws Exception
     */
    @ProcessTransactional
    @Transactional(rollbackFor = { Exception.class })
    void completeWorkItemInternal(String taskId, WorkItemResource workItem, String isSimulate) throws Exception {
        System.out.println("[InstanceServiceImpl] completeWorkItemInternal: starting for taskId=" + taskId);
        WorklistEntity worklistEntity = worklistRepository.findById(new Long(taskId)).get();

        // 완료/스킵 시점에도 사용자 정보(endpoint/resName)가 비어있는 케이스가 있어 보정
        try {
            String actorEndpoint = UserContext.getThreadLocalInstance().getUserId();
            if (actorEndpoint == null || actorEndpoint.trim().isEmpty()) {
                actorEndpoint = SecurityAwareServletFilter.getUserId();
            }
            if (actorEndpoint != null && actorEndpoint.trim().length() > 0) {
                GlobalContext.setUserId(actorEndpoint);
                applyActorToWorklistIfEmpty(worklistEntity, actorEndpoint);
                worklistRepository.save(worklistEntity);
            }
        } catch (Exception ignore) {
        }

        String instanceId = worklistEntity.getInstId().toString();
        ProcessInstance instance = getProcessInstanceLocal(instanceId);
        System.out.println("[InstanceServiceImpl] completeWorkItemInternal: loaded instance " + instanceId + ", trcTag="
                + worklistEntity.getTrcTag());

        instance.setExecutionScope(workItem.getExecScope());
        HumanActivity humanActivity = ((HumanActivity) instance.getProcessDefinition()
                .getActivity(worklistEntity.getTrcTag()));

        if (!instance.isRunning(humanActivity.getTracingTag()) && !humanActivity.isNotificationWorkitem()) {
            throw new UEngineException("Illegal completion for workitem [" + humanActivity + ":"
                    + humanActivity.getStatus(instance) + "]: Already closed or illegal status.");
        }
        // ObjectMapper objectMapper = new ObjectMapper();
        // String workItemJson = objectMapper.writeValueAsString(workItem);
        writeToFile(instance.getProcessDefinition().getId() + "/" + humanActivity.getTracingTag() + ".json",
                workItem);

        if ("true".equals(isSimulate)) {
            writeToFileRecord(instance.getProcessDefinition().getId() + "/record/" + instance.getInstanceId() + ".json",
                    humanActivity.getTracingTag(), humanActivity.getName(), workItem);
        }

        // map the argument list to variables change list
        Map<String, Object> parameterValues = workItem.getParameterValues();

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String payload = objectMapper.writeValueAsString(parameterValues);
            ProcessTransactionContext tc = ProcessTransactionContext.getThreadLocalInstance();
            tc.setSharedContext(humanActivity.getTracingTag() + "_payload@" + instance.getInstanceId() + ":", payload);

            System.out.println("[InstanceServiceImpl] completeWorkItemInternal: calling fireReceived for activity="
                    + humanActivity.getName() + ", trcTag=" + humanActivity.getTracingTag());
            humanActivity.fireReceived(instance, parameterValues);
            System.out.println("[InstanceServiceImpl] completeWorkItemInternal: fireReceived completed successfully");
        } catch (Exception e) {
            humanActivity.fireFault(instance, e);

            // Preserve HTTP-friendly error for frontend (e.g. 422 on DMN input mismatch)
            if (e instanceof org.springframework.web.server.ResponseStatusException) {
                throw (org.springframework.web.server.ResponseStatusException) e;
            }
            if (e.getCause() instanceof org.springframework.web.server.ResponseStatusException) {
                throw (org.springframework.web.server.ResponseStatusException) e.getCause();
            }

            throw new UEngineException(e.getMessage(), null, new UEngineException(e.getMessage(), e), instance,
                    humanActivity);
        }
    }

    /**
     * 워크아이템 완료.
     * isSimulate가 "true"이면 완료 후 실행 결과(instanceId, processStatus, currentActivityNames, currentActivityIds, changedProcessVariables)를 반환한다.
     * changedProcessVariables는 정의에 선언된 프로세스 변수 중 완료로 인해 값이 변경된 것만 포함한다.
     */
    @RequestMapping(value = "/work-item/{taskId}/complete", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    @ProcessTransactional // important!
    @Transactional(rollbackFor = { Exception.class })
    public Object putWorkItemComplete(@PathVariable("taskId") String taskId, @RequestBody WorkItemResource workItem,
            @RequestHeader(value = "isSimulate", required = false) String isSimulate)

            throws Exception {
        if (!"true".equals(isSimulate)) {
            completeWorkItemInternal(taskId, workItem, isSimulate != null ? isSimulate : "false");
            return null;
        }
        WorklistEntity we = worklistRepository.findById(Long.valueOf(taskId)).orElse(null);
        if (we == null)
            return null;
        String instanceId = we.getInstId().toString();
        ProcessInstance instanceBefore = getProcessInstanceLocal(instanceId);
        if (instanceBefore == null)
            return null;
        ProcessDefinition def = instanceBefore.getProcessDefinition();
        Map<String, Object> beforeVars = getProcessVariableSnapshot(instanceBefore, def);

        completeWorkItemInternal(taskId, workItem, "true");

        ProcessInstance instance = getProcessInstanceLocal(instanceId);
        if (instance == null)
            return null;
        String processStatus = instance.getStatus("");
        List<Activity> running = instance.getCurrentRunningActivities();
        String currentActivityNames = "COMPLETED";
        String currentActivityIds = "";
        if (running != null && !running.isEmpty()) {
            StringBuilder names = new StringBuilder();
            StringBuilder ids = new StringBuilder();
            for (int i = 0; i < running.size(); i++) {
                Activity a = running.get(i);
                if (i > 0) {
                    names.append(", ");
                    ids.append(",");
                }
                if (a.getName() != null)
                    names.append(a.getName());
                if (a.getTracingTag() != null)
                    ids.append(a.getTracingTag());
            }
            currentActivityNames = names.toString();
            currentActivityIds = ids.toString();
        }
        Map<String, Object> changedProcessVariables = getChangedProcessVariables(def, beforeVars, instance);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("instanceId", instanceId);
        result.put("processStatus", processStatus);
        result.put("currentActivityNames", currentActivityNames);
        result.put("currentActivityIds", currentActivityIds);
        result.put("changedProcessVariables", changedProcessVariables);
        return result;
    }

    /** 정의에 선언된 프로세스 변수만 현재 값으로 스냅샷. */
    private Map<String, Object> getProcessVariableSnapshot(ProcessInstance instance, ProcessDefinition def) {
        Map<String, Object> snap = new LinkedHashMap<>();
        ProcessVariable[] pvs = def.getProcessVariables();
        if (pvs == null) return snap;
        for (ProcessVariable pv : pvs) {
            String name = pv.getName();
            if (name == null) continue;
            try {
                Object val = instance.get("", name);
                snap.put(name, val);
            } catch (Exception ignored) { }
        }
        return snap;
    }

    /** 정의에 선언된 프로세스 변수 중 완료 전후로 값이 변경된 것만 반환. 각 항목은 before/after 모두 포함. */
    private Map<String, Object> getChangedProcessVariables(ProcessDefinition def, Map<String, Object> beforeVars,
            ProcessInstance instance) {
        Map<String, Object> changed = new LinkedHashMap<>();
        ProcessVariable[] pvs = def.getProcessVariables();
        if (pvs == null) return changed;
        for (ProcessVariable pv : pvs) {
            String name = pv.getName();
            if (name == null) continue;
            try {
                Object afterVal = instance.get("", name);
                Object beforeVal = beforeVars.get(name);
                if (!objectsEqual(beforeVal, afterVal)) {
                    Map<String, Object> beforeAfter = new LinkedHashMap<>();
                    beforeAfter.put("before", beforeVal);
                    beforeAfter.put("after", afterVal);
                    changed.put(name, beforeAfter);
                }
            } catch (Exception ignored) { }
        }
        return changed;
    }

    private static boolean objectsEqual(Object a, Object b) {
        if (a == b) return true;
        if (a == null || b == null) return false;
        return a.equals(b);
    }

    /**
     * 액티비티 이름으로 현재 워크아이템을 찾아 완료한다.
     * 시나리오 테스트에서 사용.
     * 
     * @param instanceId      프로세스 인스턴스 ID
     * @param activityName    완료할 액티비티 이름 (BPMN 표시명)
     * @param parameterValues 워크아이템 완료 시 전달할 파라미터 (null이면 빈 Map 사용)
     * @throws IllegalArgumentException 지정한 이름의 현재 워크아이템이 없을 때
     * @throws Exception
     */
    @ProcessTransactional
    @Transactional(rollbackFor = { Exception.class })
    public void completeWorkItemByActivityName(String instanceId, String activityName,
            Map<String, Object> parameterValues) throws Exception {
        if (instanceId == null || instanceId.trim().isEmpty()) {
            throw new IllegalArgumentException("instanceId cannot be null or empty");
        }
        if (activityName == null || activityName.trim().isEmpty()) {
            throw new IllegalArgumentException("activityName cannot be null or empty");
        }

        long rootInstId;
        try {
            rootInstId = Long.parseLong(instanceId);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid instanceId format: " + instanceId, e);
        }

        List<WorklistEntity> worklistEntities = worklistRepository.findCurrentWorkItemByInstId(rootInstId);
        if (worklistEntities == null || worklistEntities.isEmpty()) {
            throw new IllegalArgumentException("No current work items found for instanceId: " + instanceId);
        }
        System.out.println("[InstanceServiceImpl] completeWorkItemByActivityName: found " + worklistEntities.size()
                + " work items for instanceId=" + instanceId + ", looking for activityName=" + activityName);

        ProcessInstance instance = getProcessInstanceLocal(instanceId);
        ProcessDefinition definition = instance.getProcessDefinition();

        WorklistEntity targetEntity = null;
        for (WorklistEntity we : worklistEntities) {
            if (we.getTrcTag() == null) {
                System.out.println(
                        "[InstanceServiceImpl] completeWorkItemByActivityName: skipping work item with null trcTag, taskId="
                                + we.getTaskId());
                continue;
            }
            Activity activity = definition.getActivity(we.getTrcTag());
            if (activity != null) {
                String actName = activity.getName();
                System.out.println("[InstanceServiceImpl] completeWorkItemByActivityName: checking work item taskId="
                        + we.getTaskId() + ", trcTag=" + we.getTrcTag() + ", activityName=" + actName + " (expected="
                        + activityName + ")");
                if (activityName.equals(actName)) {
                    targetEntity = we;
                    System.out.println(
                            "[InstanceServiceImpl] completeWorkItemByActivityName: matched! taskId=" + we.getTaskId());
                    break;
                }
            } else {
                System.out.println("[InstanceServiceImpl] completeWorkItemByActivityName: activity is null for trcTag="
                        + we.getTrcTag());
            }
        }

        if (targetEntity == null) {
            StringBuilder sb = new StringBuilder();
            sb.append("No work item found for activity name '").append(activityName).append("' in instance ")
                    .append(instanceId);
            sb.append(". Current work items: [");
            for (WorklistEntity we : worklistEntities) {
                if (we.getTrcTag() != null) {
                    Activity act = definition.getActivity(we.getTrcTag());
                    if (act != null) {
                        sb.append(act.getName()).append(",");
                    }
                }
            }
            sb.append("]");
            throw new IllegalArgumentException(sb.toString());
        }

        String taskId = targetEntity.getTaskId().toString();
        WorkItemResource workItem = new WorkItemResource();
        workItem.setParameterValues(parameterValues != null ? parameterValues : new HashMap<String, Object>());
        workItem.setExecScope(null); // 기본값

        System.out.println(
                "[InstanceServiceImpl] completeWorkItemByActivityName: calling completeWorkItemInternal for taskId="
                        + taskId);
        completeWorkItemInternal(taskId, workItem, "true");
        System.out.println(
                "[InstanceServiceImpl] completeWorkItemByActivityName: successfully completed taskId=" + taskId);
    }

    @RequestMapping(value = "/test/{recordPath}/record", method = RequestMethod.GET)
    @ProcessTransactional
    public List<String> testRecordList(@PathVariable("recordPath") String recordPath) throws Exception {
        List<String> fileContents = new ArrayList<>();

        try {
            IResource resource = new DefaultResource("test/" + recordPath);
            if (resourceManager.exists(resource)) {
                InputStream inputStream = resourceManager.getInputStream(resource);
                String content = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
                fileContents.add(content);
            }
        } catch (IOException e) {
            throw new UEngineException("Error reading record file from path: " + recordPath, e);
        }

        return fileContents;
    }

    @RequestMapping(value = "/instance/{instanceId}/fire-message", method = RequestMethod.POST)
    @ProcessTransactional
    public void fireMessage(@PathVariable("instanceId") String instanceId, @RequestBody Message message)
            throws Exception {
        ProcessInstance instance = getProcessInstanceLocal(instanceId);
        if (instance != null) {
            instance.getProcessDefinition().fireMessage(message.getEvent(), instance, message.getPayload());
        } else {
            throw new ResourceNotFoundException("Instance not found for ID: " + instanceId);
        }
    }

    @PostMapping("/instance/shutdown")
    public void shutdownContext() {
        int exitCode = 0; // 정상 종료 코드
        // 애플리케이션 종료
        SpringApplication.exit(context, () -> exitCode);
    }

    @Override
    public void postMessage(String instanceId, Message message) throws Exception {
        // Boundary Event 또는 Signal Event 발생 시 호출
        ProcessInstance instance = getProcessInstanceLocal(instanceId);
        if (instance != null) {
            instance.getProcessDefinition().fireMessage(message.getEvent(), instance, message.getPayload());
        } else {
            throw new ResourceNotFoundException("Instance not found for ID: " + instanceId);
        }
    }
    // "RESTFUl API PRINCIPLES"
    // "defintion-chages" > POST > "definition-changes/${defPath}"

    @ProcessTransactional
    @RequestMapping(value = "/definition-changes", method = RequestMethod.POST)
    public void postCreatedRawDefinition(@RequestBody String defPath) throws Exception {
        try {
            if (defPath == null) {
                return;
            }

            String p = defPath.trim().replace("\\", "/");
            if (p.startsWith("/")) {
                p = p.substring(1);
            }

            // Not a BPMN definition: ignore raw changes for business rules / json / rule
            // files
            if (p.startsWith("businessRules/")
                    || p.endsWith(".rule")
                    || p.startsWith("buisnessRules/")
                    || p.endsWith(".json")
                    || "map.json".equals(p)) {
                return;
            }

            if (p.endsWith("form"))
                return;

            ProcessDefinition definition = (ProcessDefinition) definitionService.getDefinition(p);
            definition.setId(p);

            if (definition != null && definition instanceof ProcessDefinition) {
                invokeDeployFilters(definition, p);
            }
        } catch (Exception e) {
            // invokeDeployFilters(DeployFilter.beforeDeploy)에서 발생한 예외(UEngineException 포함)를
            // 응답 메시지에 최대한 "그대로"(원인 + stacktrace) 담아서 디버깅 가능하게 한다.
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Post CreatedRawDefinition : " + buildThrowableDetail(e),
                    e);

        }

    }

    private void invokeDeployFilters(ProcessDefinition definitionDeployed, String path) throws UEngineException {

        Map<String, DeployFilter> filters = GlobalContext.getComponents(DeployFilter.class);
        if (filters != null && filters.size() > 0) {
            for (DeployFilter theFilter : filters.values()) {
                try {
                    theFilter.beforeDeploy(definitionDeployed, null, path, true);
                } catch (Exception e) {
                    throw new UEngineException("Error when to invoke DeployFilter: " + theFilter.getClass().getName(),
                            e);
                }
            }
        }
    }

    private String buildThrowableDetail(Throwable t) {
        if (t == null)
            return "(null)";

        Throwable root = t;
        while (root.getCause() != null && root.getCause() != root) {
            root = root.getCause();
        }

        String msg = t.getMessage();
        if (msg == null || msg.isBlank())
            msg = t.toString();

        String rootMsg = root.getMessage();
        if (rootMsg == null || rootMsg.isBlank())
            rootMsg = root.toString();

        StringWriter sw = new StringWriter();
        t.printStackTrace(new PrintWriter(sw));

        String stack = sw.toString();
        // 너무 길면(Feign/프론트에서 보기 어려움) 잘라서 내려준다.
        // 전체 스택은 서버 로그에서 확인 가능하고, 필요하면 server.error.include-stacktrace=on_param + ?trace=true로도 확인 가능.
        int limit = 8000;
        if (stack.length() > limit) {
            stack = stack.substring(0, limit) + "\n...(truncated " + (stack.length() - limit) + " chars)";
        }

        return msg + "\nRootCause: " + rootMsg + "\nStacktrace:\n" + stack;
    }

    // @ProcessTransactional(readOnly = true)
    // @RequestMapping(value = "/dry-run/**", method = RequestMethod.POST, produces
    // = "application/json;charset=UTF-8")
    // public Object dryRun(HttpServletRequest request, ProcessExecutionCommand
    // command) throws Exception {
    // String path = (String)
    // request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
    // String definitionPath = path.substring("/dry-run".length() + 1);

    // return dryRun(definitionPath, request, command);
    // }

    //

    @ProcessTransactional(readOnly = true)
    @RequestMapping(value = "/dry-run", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    public Object dryRun(@RequestBody ProcessExecutionCommand command) throws Exception {
        ProcessExecutionCommand processCommand = new ProcessExecutionCommand();
        processCommand.setProcessDefinitionId(command.getProcessDefinitionId());

        if (command.getRoleMappings() != null) {
            processCommand.setRoleMappings(command.getRoleMappings());
        }

        Object definition;
        try {
            String version = findHighestNumberedFileName(processCommand.getProcessDefinitionId());
            definition = definitionService.getDefinition(processCommand.getProcessDefinitionId(), version);
        } catch (ClassNotFoundException cnfe) {
            // ClassNotFoundException을 처리하고, 500 Internal Server Error 반환
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Class not found", cnfe);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
        }

        if (definition instanceof ProcessDefinition) {
            ProcessDefinition processDefinition = (ProcessDefinition) definition;
            // return processDefinition.getFirstHumanActivity();

            try {
                org.uengine.kernel.ProcessInstance instance = AbstractProcessInstance.create(processDefinition,
                        processCommand.getInstanceName(), null);

                org.uengine.five.dto.RoleMapping[] roleMappings = processCommand.getRoleMappings();
                if (roleMappings != null) {
                    for (org.uengine.five.dto.RoleMapping roleMapping : roleMappings) {
                        instance.putRoleMapping(roleMapping.getName(), roleMapping.toKernelRoleMapping());
                    }
                }

                if (processCommand.getCorrelationKeyValue() != null) {
                    ((JPAProcessInstance) instance).getProcessInstanceEntity()
                            .setCorrKey(processCommand.getCorrelationKeyValue());
                }

                instance.execute();
                // new InstanceResource(instance); // TODO: returns HATEOAS _self link instead.

                WorkItemResource workItem = new WorkItemResource();
                if (instance.getCurrentRunningActivity() != null) {
                    Activity activity = instance.getCurrentRunningActivity().getActivity();

                    if (activity instanceof org.uengine.kernel.FormActivity) {
                        String tool = ((org.uengine.kernel.FormActivity) activity).getTool(instance);
                        ((org.uengine.kernel.FormActivity) activity).setTool(tool);
                    } else if (activity instanceof org.uengine.kernel.URLActivity) {
                        String urlTool = ((org.uengine.kernel.URLActivity) activity).getTool(instance);
                        ((org.uengine.kernel.URLActivity) activity).setTool(urlTool);
                    } else {
                        String tool = ((org.uengine.kernel.HumanActivity) activity).getTool(instance);
                        ((org.uengine.kernel.HumanActivity) activity).setTool(tool);
                    }

                    Map<String, Object> parameterValues = new HashMap<String, Object>();

                    if (activity instanceof ReceiveActivity) {
                        Map<String, Object> mappingInValues = ((ReceiveActivity) activity).getMappingInValues(instance);
                        if (mappingInValues.size() > 0) {
                            for (Map.Entry<String, Object> entry : mappingInValues.entrySet()) {
                                parameterValues.put(entry.getKey(), entry.getValue());
                            }
                        }
                    }

                    if (parameterValues.size() > 0) {
                        workItem.setParameterValues(parameterValues);
                    }
                    workItem.setActivity(activity);
                }

                return workItem;
            } catch (Exception e) {
                e.printStackTrace();
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "Error get dry-run process instance: " + e.getMessage(), e);
            }

        }

        return null;
    }

    @RequestMapping(value = "/start-and-complete", consumes = "application/json;charset=UTF-8", method = {
            RequestMethod.POST,
            RequestMethod.PUT }, produces = "application/json;charset=UTF-8")
    @Transactional(rollbackFor = { Exception.class })
    @ProcessTransactional
    public InstanceResource startAndComplete(@RequestBody StartAndCompleteCommand command,
            @RequestHeader("isSimulate") String isSimulate) throws Exception {
        try {
            ProcessExecutionCommand processExecutionCommand = command.getProcessExecutionCommand();
            processExecutionCommand.setSimulation(false);
            InstanceResource instance = start(processExecutionCommand);

            if (instance == null)
                return null;
            String instId = instance.getInstanceId();
            List<WorklistEntity> worklistEntity = worklistRepository
                    .findCurrentWorkItemByInstId(Long.parseLong(instId));

            if (worklistEntity == null || worklistEntity.isEmpty())
                return null;

            String taskId = worklistEntity.get(0).getTaskId().toString();

            if (command.getVariables() != null) {
                for (String varName : command.getVariables().keySet()) {
                    String json = command.getVariables().get(varName);
                    setVariableWithTaskId(instId, taskId, varName, json, command.getWorkItem());
                }
            }

            putWorkItemComplete(taskId, command.getWorkItem(), isSimulate);
            return instance;
        } catch (Exception e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error executing dry-run process instance: " + e.getMessage(), e);
        }
    }

    @RequestMapping(value = "/validate", method = RequestMethod.POST, consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    @ProcessTransactional
    public Serializable validate(@RequestBody String xml)
            throws Exception {
        String decodedXml = URLDecoder.decode(xml, StandardCharsets.UTF_8.name());
        Serializable result = null;
        BpmnXMLParser parser = new BpmnXMLParser();
        ProcessDefinition processDefinition = parser.parse(decodedXml);
        HashMap<String, ValidationContext> validationMessages = new HashMap<>();

        ConcurrentHashMap<String, Activity> childActivities = new ConcurrentHashMap<>(
                processDefinition.getWholeChildActivities());

        for (Map.Entry<String, Activity> entry : childActivities.entrySet()) {
            Activity childActivity = entry.getValue();
            Map<String, Object> options = new HashMap<>();
            options.put(ValidationContext.OPTIONKEY_DISABLE_REPLICATION, true);
            ValidationContext validationContext = childActivity.validate(options);

            if (childActivity instanceof SubProcess) {
                SubProcess subProcess = (SubProcess) childActivity;
                validationContext = subProcess.validate(options);
                validateSequenceFlow(subProcess.getSequenceFlows(), validationMessages);
            }

            if (validationContext != null
                    && validationContext.size() > 0) {
                validationMessages.put(childActivity.getTracingTag(), validationContext);
            }
        }

        ArrayList<SequenceFlow> sequenceFlows = processDefinition.getSequenceFlows();
        validateSequenceFlow(sequenceFlows, validationMessages);

        result = validationMessages;
        return result;
    }

    void validateSequenceFlow(ArrayList<SequenceFlow> sequenceFlows,
            HashMap<String, ValidationContext> validationMessage) {
        for (SequenceFlow sequenceFlow : sequenceFlows) {
            Map<String, Object> options = new HashMap<>();
            options.put(ValidationContext.OPTIONKEY_DISABLE_REPLICATION, true);
            ValidationContext validationContext = sequenceFlow.validate(options);
            if (validationContext != null
                    && validationContext.size() > 0) {
                validationMessage.put(sequenceFlow.getTracingTag(), validationContext);
            }
        }
    }

    @RequestMapping(value = "/work-item", method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
    @ProcessTransactional(readOnly = true)
    public List<WorkItemResource> getCurrentWorkItemByCorrKey(@RequestParam("corrKey") String corrKey)
            throws Exception {
        if (corrKey == null)
            return null;

        List<ProcessInstanceEntity> processInstanceList = processInstanceRepository.findByCorrKeyAndStatus(corrKey,
                "Running");
        for (ProcessInstanceEntity processInstanceEntity : processInstanceList) {
            List<WorklistEntity> worklistEntity = worklistRepository
                    .findCurrentWorkItemByInstId(processInstanceEntity.getRootInstId());

            if (worklistEntity != null) {
                List<WorkItemResource> result = new ArrayList<>();
                for (WorklistEntity entity : worklistEntity) {
                    ProcessDefinition definition = (ProcessDefinition) definitionService
                            .getDefinition(entity.getDefId());
                    HumanActivity activity = (HumanActivity) definition.getActivity(entity.getTrcTag());

                    WorkItemResource workItem = new WorkItemResource();
                    workItem.setActivity(activity);
                    workItem.setWorklist(entity);
                    result.add(workItem);
                }

                return result;
            }
        }
        return null;
    }

    @RequestMapping(value = "/work-item/{taskId}/claim", method = RequestMethod.POST)
    @org.springframework.transaction.annotation.Transactional
    @ProcessTransactional // important!
    public void claimWorkItem(@PathVariable("taskId") String taskId, @RequestBody RoleMappingCommand roleMapping)
            throws Exception {

        if (taskId == null || taskId.equals("null")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "taskId is required");
        }

        WorklistEntity worklistEntity = worklistRepository.findById(new Long(taskId)).orElse(null);
        if (worklistEntity == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No such work item where taskId = " + taskId);
        }

        // endpoint가 비어있으면 "선점 해제(unclaim)"로 처리한다.
        boolean unclaim = (roleMapping == null || roleMapping.getEndpoint() == null
                || roleMapping.getEndpoint().trim().isEmpty());

        // unclaim은 반드시 로그인 사용자 컨텍스트가 필요
        String actorEndpoint = null;
        if (unclaim) {
            actorEndpoint = SecurityAwareServletFilter.getUserId();
            if (actorEndpoint != null) actorEndpoint = actorEndpoint.trim();
            if (actorEndpoint == null || actorEndpoint.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "login user is required for unclaim");
            }
        } else {
            actorEndpoint = roleMapping.getEndpoint().trim();
        }

        GlobalContext.setUserId(actorEndpoint);

        Long rootInstId = worklistEntity.getRootInstId() == null ? worklistEntity.getInstId() : worklistEntity.getRootInstId().longValue();
        String roleName = worklistEntity.getRoleName();
        String scope = worklistEntity.getScope();
        Integer assignType = worklistEntity.getAssignType();

        if (unclaim) {
            // 본인 소유의 건만 해제 허용
            if (UEngineUtil.isNotEmpty(worklistEntity.getEndpoint()) && !actorEndpoint.equals(worklistEntity.getEndpoint())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "No permission to unclaim this task. endpoint=" + worklistEntity.getEndpoint() + ", userId=" + actorEndpoint);
            }

            worklistEntity.setEndpoint(null);
            worklistEntity.setResName(null);
            worklistRepository.save(worklistEntity);

            if (rootInstId != null && roleName != null && scope != null && assignType != null) {
                List<WorklistEntity> siblings = worklistRepository.findSiblingsForClaimState(rootInstId, roleName, scope, assignType, actorEndpoint);
                if (siblings != null) {
                    for (WorklistEntity wl : siblings) {
                        if (wl == null) continue;
                        wl.setEndpoint(null);
                        wl.setResName(null);
                        worklistRepository.save(wl);
                    }
                }
            }
        } else {
            // 1) 현재 taskId의 endpoint/resName 보강
            applyActorToWorklistIfEmpty(worklistEntity, actorEndpoint);
            worklistRepository.save(worklistEntity);

            // 2) 동일 역할 + 동일 scope/assignType 그룹의 다른 workitem들도 함께 소유자 세팅
            if (rootInstId != null && roleName != null && scope != null && assignType != null) {
                List<WorklistEntity> siblings = worklistRepository.findSiblingsForClaimState(rootInstId, roleName, scope, assignType, null);
                if (siblings != null) {
                    for (WorklistEntity wl : siblings) {
                        if (wl == null) continue;
                        applyActorToWorklistIfEmpty(wl, actorEndpoint);
                        worklistRepository.save(wl);
                    }
                }
            }
        }
    }

    private void applyActorToWorklistIfEmpty(WorklistEntity wl, String actorEndpoint) {
        if (wl == null || actorEndpoint == null || actorEndpoint.trim().isEmpty()) return;

        if (!UEngineUtil.isNotEmpty(wl.getEndpoint())) {
            wl.setEndpoint(actorEndpoint);
        }

        if (!UEngineUtil.isNotEmpty(wl.getResName()) || (UEngineUtil.isNotEmpty(wl.getEndpoint()) && wl.getResName().equals(wl.getEndpoint()))) {
            try {
                RoleMapping rm = RoleMapping.create();
                if (rm != null) {
                    rm.setEndpoint(wl.getEndpoint());
                    if (UEngineUtil.isNotEmpty(wl.getScope())) rm.setScope(wl.getScope());
                    rm.setAssignType(wl.getAssignType());
                    rm.fill();
                    String filled = rm.getResourceName();
                    if (UEngineUtil.isNotEmpty(filled)) wl.setResName(filled);
                }
            } catch (Exception ignore) {
            }
        }
    }

    /**
     * WorkItem 위임(Delegation)
     *
     * - delegateOnlyForWorkitem=false(기본): 완전 이관(인스턴스 레벨 RoleMapping 변경 + 새 workitem 생성)
     * - delegateOnlyForWorkitem=true: 원소유 유지(workitem만 위임, 인스턴스 레벨 RoleMapping 유지)
     *
     * 권한 체크(기본): 현재 task의 endpoint(담당자)와 로그인 사용자ID가 일치해야 위임 가능.
     */
    @RequestMapping(value = "/work-item/{taskId}/delegate", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    @org.springframework.transaction.annotation.Transactional
    @ProcessTransactional // important!
    public WorkItemResource delegateWorkItem(
            @PathVariable("taskId") String taskId,
            @RequestBody RoleMappingCommand delegatedRoleMapping,
            @RequestParam(value = "delegateOnlyForWorkitem", required = false, defaultValue = "false") boolean delegateOnlyForWorkitem)
            throws Exception {

        if (taskId == null || taskId.equals("null")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "taskId is required");
        }

        if (delegatedRoleMapping == null || delegatedRoleMapping.getEndpoint() == null
                || delegatedRoleMapping.getEndpoint().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "delegatedRoleMapping.endpoint is required");
        }

        // 로그인 사용자 컨텍스트(권한/로깅/RoleResolution 등) 설정
        String userId = SecurityAwareServletFilter.getUserId();
        if (userId != null) {
            GlobalContext.setUserId(userId);
        }

        WorklistEntity worklistEntity = worklistRepository.findById(new Long(taskId)).orElse(null);
        if (worklistEntity == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No such work item where taskId = " + taskId);
        }

        String instanceId = worklistEntity.getInstId().toString();
        ProcessInstance instance = getProcessInstanceLocal(instanceId);

        HumanActivity humanActivity = ((HumanActivity) instance.getProcessDefinition()
                .getActivity(worklistEntity.getTrcTag()));

        // 현재 담당자(Worklist 기준 우선, 없으면 ActualMapping fallback)
        String currentOwner = worklistEntity.getEndpoint();
        try {
            if ((currentOwner == null || currentOwner.trim().isEmpty()) && humanActivity != null) {
                RoleMapping actual = humanActivity.getActualMapping(instance);
                if (actual != null) {
                    currentOwner = actual.getEndpoint();
                }
            }
        } catch (Exception ignore) {
        }

        // 권한 체크: 로그인 사용자ID가 있고, 현재 담당자가 확인되면 일치해야 함
        if (userId != null && currentOwner != null && !userId.equals(currentOwner)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "No permission to delegate this task. currentOwner=" + currentOwner + ", userId=" + userId);
        }

        // 실행 중이 아니면 위임 불가(알림 workitem은 정책에 따라 허용할 수 있으나, 기본은 차단)
        if (!instance.isRunning(humanActivity.getTracingTag()) && !humanActivity.isNotificationWorkitem()) {
            throw new UEngineException("Illegal delegation for workitem [" + humanActivity + ":"
                    + humanActivity.getStatus(instance) + "]: Already closed or illegal status.");
        }

        // DelegateTest의 동작과 동일하게 kernel 메서드 호출
        String laneScope = worklistEntity.getScope();
        int laneAssignType = worklistEntity.getAssignType();

        RoleMapping delegated = RoleMapping.create();
        if (delegated != null) {
            String targetEndpoint = delegatedRoleMapping.getEndpoint() != null ? delegatedRoleMapping.getEndpoint().trim() : null;
            delegated.setEndpoint(targetEndpoint);

            // delegate 요청에 scope/assignType이 없더라도, Lane(=roleName) 기반 태스크는 기존 worklist의 값을 유지해야 함
            // 그래야 위임 후 새로 생성되는 workitem(재실행)이 기존 Lane 정책(scope/assignType)을 그대로 가진다.
            if (delegatedRoleMapping.getScope() != null) {
                delegated.setScope(delegatedRoleMapping.getScope());
            } else if (UEngineUtil.isNotEmpty(laneScope) && !"null".equalsIgnoreCase(laneScope)) {
                delegated.setScope(laneScope);
            }

            if (delegatedRoleMapping.getAssignType() != null) {
                delegated.setAssignType(delegatedRoleMapping.getAssignType());
            } else {
                delegated.setAssignType(laneAssignType);
            }

            if (delegatedRoleMapping.getResourceName() != null) {
                delegated.setResourceName(delegatedRoleMapping.getResourceName());
            }

            // resName이 없으면 IAM 기반으로 채움 (Flyweight + isFilled로 중복 최소화)
            try {
                delegated.fill();
            } catch (Exception ignore) {
            }
        }
        humanActivity.delegate(instance, delegated, delegateOnlyForWorkitem);

        // 같은 Lane(roleName)의 병렬 태스크들도 동일 사용자로 재할당(NEW/RUNNING만)
        try {
            Long rootInstId = worklistEntity.getRootInstId() == null ? worklistEntity.getInstId() : worklistEntity.getRootInstId().longValue();
            if (rootInstId != null) {
                List<WorklistEntity> currents = worklistRepository.findCurrentWorkItemByInstId(rootInstId);
                if (currents != null) {
                    String laneRoleName = worklistEntity.getRoleName();
                    String targetEndpoint = delegated != null ? delegated.getEndpoint() : null;
                    String targetResName = delegated != null ? delegated.getResourceName() : null;

                    for (WorklistEntity wl : currents) {
                        if (wl == null) continue;
                        if (laneRoleName == null || !laneRoleName.equals(wl.getRoleName())) continue;
                        if (!UEngineUtil.isNotEmpty(targetEndpoint)) continue;

                        // 기존 Lane 속성 유지(위임 후 생성된 신규 workitem이 scope/assignType을 잃는 문제 보정 포함)
                        if (!UEngineUtil.isNotEmpty(wl.getScope()) || "null".equalsIgnoreCase(wl.getScope())) {
                            if (UEngineUtil.isNotEmpty(laneScope) && !"null".equalsIgnoreCase(laneScope)) wl.setScope(laneScope);
                        }
                        if (wl.getAssignType() == 0 && laneAssignType != 0) {
                            wl.setAssignType(laneAssignType);
                        }

                        wl.setEndpoint(targetEndpoint);
                        if (UEngineUtil.isNotEmpty(targetResName)) wl.setResName(targetResName);

                        // 혹시 resName이 비어있으면 endpoint 기반 fill로 보강
                        applyActorToWorklistIfEmpty(wl, targetEndpoint);
                        worklistRepository.save(wl);
                    }
                }
            }
        } catch (Exception ignore) {
        }

        // 완전 이관이면 새 taskId가 생길 수 있으므로, 현재 taskIds 기준으로 리턴
        String resultTaskId = taskId;
        try {
            String[] taskIds = humanActivity.getTaskIds(instance);
            if (taskIds != null && taskIds.length > 0 && taskIds[0] != null && taskIds[0].trim().length() > 0) {
                resultTaskId = taskIds[0];
            }
        } catch (Exception ignore) {
        }

        return getWorkItem(resultTaskId);
    }

    /**
     * 태스크 반송 가능여부 + 후보 목록
     *
     * - 정책: "실행 전(previous) 태스크"만 후보로 노출 (Activity.getPreviousActivities() 기반)
     * - 이전 태스크가 복수(병렬/분기 등)인 경우, previousActivities의 모든 HumanActivity를 후보로 제공
     * - 반송 시 기존 태스크 삭제 금지: return 실행에서는 현재 workitem을 SUSPENDED로 업데이트하고,
     */
    @RequestMapping(value = "/work-item/{taskId}/return/availability", method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
    @ProcessTransactional(readOnly = true)
    public TaskReturnAvailability getTaskReturnAvailability(@PathVariable("taskId") String taskId) throws Exception {
        if (taskId == null || taskId.equals("null")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "taskId is required");
        }

        WorklistEntity current = worklistRepository.findById(new Long(taskId)).orElse(null);
        if (current == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No such work item where taskId = " + taskId);
        }

        // 권한/컨텍스트 설정(가능여부 판단에 사용될 수 있음)
        String userId = SecurityAwareServletFilter.getUserId();
        if (userId != null) {
            GlobalContext.setUserId(userId);
        }

        String instanceId = String.valueOf(current.getInstId());
        ProcessInstance instance = getProcessInstanceLocal(instanceId);
        if (instance == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Instance not found for taskId=" + taskId);
        }

        Activity currentActivity = instance.getProcessDefinition().getActivity(current.getTrcTag());
        if (!(currentActivity instanceof HumanActivity)) {
            return TaskReturnAvailability.disabled("Human task only.");
        }

        HumanActivity humanActivity = (HumanActivity) currentActivity;
        if (!instance.isRunning(humanActivity.getTracingTag()) && !humanActivity.isNotificationWorkitem()) {
            return TaskReturnAvailability.disabled("Already closed or illegal status.");
        }

        Long rootInstId = current.getRootInstId() == null ? current.getInstId() : current.getRootInstId().longValue();

        // 실행 직전(previous) HumanActivity를 후보로 사용
        // - 병렬 Join에서는 구조적 previous 가 split 지점으로 튀는 경우가 있어,
        //   "정의 그래프(incoming sequence flow)" 기준으로 역방향 탐색하여 join 직전 HumanTask를 찾는다.
        List<Activity> previousHumanActivities = collectPreviousHumanActivities(currentActivity, current.getTrcTag());
        if (previousHumanActivities == null || previousHumanActivities.isEmpty()) {
            return TaskReturnAvailability.disabled("No previous human tasks.");
        }

        // UI 표시를 위해, 이전 activity(tracingTag)에 대응되는 최근 종료(Completed/Skipped) workitem 정보를 보강
        List<WorklistEntity> history = processInstanceRepository.findAllWorklistsByRootInstId(rootInstId);

        List<TaskReturnCandidate> candidates = new ArrayList<>();
        for (Activity prev : previousHumanActivities) {
            if (prev == null) continue;

            // 기본: 자기 자신은 후보에서 제외
            if (prev.getTracingTag() != null && prev.getTracingTag().equals(current.getTrcTag())) {
                continue;
            }

            WorklistEntity best = null;
            if (history != null) {
                // 1차: execScope가 있으면 동일 execScope만 우선 매칭
                boolean hasExecScope = current.getExecScope() != null && current.getExecScope().trim().length() > 0;
                for (WorklistEntity wl : history) {
                    if (wl == null) continue;
                    if (wl.getTrcTag() == null) continue;
                    if (!wl.getTrcTag().equals(prev.getTracingTag())) continue;
                    if (wl.getStatus() == null) continue;
                    if (!(wl.getStatus().equalsIgnoreCase("COMPLETED") || wl.getStatus().equalsIgnoreCase("SKIPPED"))) {
                        continue;
                    }

                    if (hasExecScope) {
                        if (wl.getExecScope() == null) continue;
                        if (!current.getExecScope().equals(wl.getExecScope())) continue;
                    }

                    if (best == null) {
                        best = wl;
                        continue;
                    }

                    Date bestEnd = best.getEndDate();
                    Date wlEnd = wl.getEndDate();
                    if (bestEnd == null && wlEnd != null) {
                        best = wl;
                    } else if (bestEnd != null && wlEnd != null && wlEnd.after(bestEnd)) {
                        best = wl;
                    } else if (bestEnd == null && wlEnd == null && wl.getTaskId() != null && best.getTaskId() != null
                            && wl.getTaskId() > best.getTaskId()) {
                        best = wl;
                    }
                }

                // 2차: execScope 매칭이 실패했으면 scope 무시하고 매칭
                if (best == null && hasExecScope) {
                    for (WorklistEntity wl : history) {
                        if (wl == null) continue;
                        if (wl.getTrcTag() == null) continue;
                        if (!wl.getTrcTag().equals(prev.getTracingTag())) continue;
                        if (wl.getStatus() == null) continue;
                        if (!(wl.getStatus().equalsIgnoreCase("COMPLETED") || wl.getStatus().equalsIgnoreCase("SKIPPED"))) {
                            continue;
                        }

                        if (best == null) {
                            best = wl;
                            continue;
                        }

                        Date bestEnd = best.getEndDate();
                        Date wlEnd = wl.getEndDate();
                        if (bestEnd == null && wlEnd != null) {
                            best = wl;
                        } else if (bestEnd != null && wlEnd != null && wlEnd.after(bestEnd)) {
                            best = wl;
                        } else if (bestEnd == null && wlEnd == null && wl.getTaskId() != null && best.getTaskId() != null
                                && wl.getTaskId() > best.getTaskId()) {
                            best = wl;
                        }
                    }
                }
            }

            TaskReturnCandidate c = new TaskReturnCandidate();
            c.setTracingTag(prev.getTracingTag());
            c.setActivityName(prev.getName());

            // 실제 수행(종료) 이력이 있는 태스크만 후보로 노출
            // - 모델 상으로는 이전이지만 이번 인스턴스에서 실행되지 않은 분기/태스크는 제외
            if (best == null) {
                continue;
            }

            c.setTaskId(best.getTaskId());
            c.setEndpoint(best.getEndpoint());
            c.setCompletedAt(best.getEndDate());
            c.setExecScope(best.getExecScope());

            candidates.add(c);
        }

        if (candidates.isEmpty()) {
            return TaskReturnAvailability.disabled("No previous human tasks.");
        }

        return TaskReturnAvailability.enabled(candidates);
    }

    /**
     * 현재 액티비티의 "직전" HumanActivity 후보를 수집합니다.
     * - previous 가 Gateway/Flow 노드로만 떨어지는 경우를 위해, HumanActivity가 나올 때까지 역방향으로 탐색합니다.
     * - HumanActivity를 발견하면 해당 노드는 후보로 추가하고 더 이상 그 뒤로는 확장하지 않습니다(직전 후보만).
     */
    private List<Activity> collectPreviousHumanActivities(Activity currentActivity, String excludeTracingTag) {
        if (currentActivity == null) return java.util.Collections.emptyList();

        // tracingTag 기준으로 방문 체크(동일 노드 중복/루프 방지)
        java.util.Set<String> visited = new java.util.HashSet<>();
        java.util.Deque<Activity> queue = new java.util.ArrayDeque<>();
        java.util.List<Activity> result = new java.util.ArrayList<>();
        java.util.Set<String> addedTracingTags = new java.util.LinkedHashSet<>();

        // 1) 우선: 그래프 기반 incoming sequence flow 사용 (BPMN join의 "직전"을 가장 정확히 표현)
        java.util.List<org.uengine.kernel.bpmn.SequenceFlow> incomings = currentActivity.getIncomingSequenceFlows();
        if (incomings != null && !incomings.isEmpty()) {
            for (org.uengine.kernel.bpmn.SequenceFlow f : incomings) {
                if (f == null) continue;
                Activity src = f.getSourceActivity();
                if (src != null) {
                    queue.add(src);
                }
            }
        } else {
            // 2) fallback: 기존 previousActivities (그래프가 구성되지 않은 모델 대비)
            Vector prevs = currentActivity.getPreviousActivities();
            if (prevs != null) {
                for (Object obj : prevs) {
                    if (obj instanceof Activity) {
                        queue.add((Activity) obj);
                    }
                }
            }
        }

        int guard = 0;
        while (!queue.isEmpty() && guard++ < 5000) {
            Activity a = queue.poll();
            if (a == null) continue;

            String tag = a.getTracingTag();
            if (tag != null) {
                if (!visited.add(tag)) continue;
            }
            if (tag != null && excludeTracingTag != null && tag.equals(excludeTracingTag)) {
                continue;
            }

            if (a instanceof HumanActivity) {
                // tracingTag 가 null 인 경우도 방어적으로 허용하되, 중복 방지를 위해 tag 기준 우선 사용
                if (tag == null || addedTracingTags.add(tag)) {
                    result.add(a);
                }
                continue;
            }

            // non-human 노드는 계속 upstream 탐색
            java.util.List<org.uengine.kernel.bpmn.SequenceFlow> moreIncomings = a.getIncomingSequenceFlows();
            if (moreIncomings != null && !moreIncomings.isEmpty()) {
                for (org.uengine.kernel.bpmn.SequenceFlow f : moreIncomings) {
                    if (f == null) continue;
                    Activity src = f.getSourceActivity();
                    if (src != null) queue.add(src);
                }
            } else {
                Vector more = a.getPreviousActivities();
                if (more != null) {
                    for (Object obj : more) {
                        if (obj instanceof Activity) {
                            queue.add((Activity) obj);
                        }
                    }
                }
            }
        }

        return result;
    }


    @RequestMapping(value = "/work-item/{taskId}/return", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    @ProcessTransactional
    @Transactional(rollbackFor = { Exception.class })
    public TaskReturnResult returnWorkItem(
            @PathVariable("taskId") String taskId,
            @RequestBody TaskReturnCommand command) throws Exception {

        if (taskId == null || taskId.equals("null")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "taskId is required");
        }
        if (command == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "command is required");
        }

        // 요청 사용자(현재 로그인 사용자) 컨텍스트
        String requestUserId = UserContext.getThreadLocalInstance().getUserId();
        if (requestUserId == null || requestUserId.trim().isEmpty()) {
            requestUserId = SecurityAwareServletFilter.getUserId();
        }
        if (requestUserId != null && requestUserId.trim().length() > 0) {
            GlobalContext.setUserId(requestUserId);
        }

        WorklistEntity current = worklistRepository.findById(new Long(taskId)).orElse(null);
        if (current == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No such work item where taskId = " + taskId);
        }

        String instanceId = String.valueOf(current.getInstId());
        ProcessInstance instance = getProcessInstanceLocal(instanceId);
        if (instance == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Instance not found for taskId=" + taskId);
        }

        Activity currentActivity = instance.getProcessDefinition().getActivity(current.getTrcTag());
        if (!(currentActivity instanceof HumanActivity)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Human task only.");
        }
        HumanActivity humanActivity = (HumanActivity) currentActivity;

        // 권한 체크: 현재 담당자(Worklist 우선, 없으면 ActualMapping fallback)
        String currentOwner = current.getEndpoint();
        try {
            if ((currentOwner == null || currentOwner.trim().isEmpty()) && humanActivity != null) {
                RoleMapping actual = humanActivity.getActualMapping(instance);
                if (actual != null) {
                    currentOwner = actual.getEndpoint();
                }
            }
        } catch (Exception ignore) {
        }
        if (requestUserId != null && currentOwner != null && !requestUserId.equals(currentOwner)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "No permission to return this task. currentOwner=" + currentOwner + ", userId=" + requestUserId);
        }

        // 실행 중이 아니면 반송 불가(알림 workitem은 정책에 따라 허용)
        if (!instance.isRunning(humanActivity.getTracingTag()) && !humanActivity.isNotificationWorkitem()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Illegal return for workitem [" + humanActivity + ":" + humanActivity.getStatus(instance)
                            + "]: Already closed or illegal status.");
        }

        // 후보/가능여부 재검증(TOCTOU 방지)
        TaskReturnAvailability availability = getTaskReturnAvailability(taskId);
        if (!availability.isEnabled() || availability.getCandidates() == null || availability.getCandidates().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    availability.getReason() != null ? availability.getReason() : "Not allowed.");
        }

        String targetTracingTag = null;
        String targetExecScope = null;

        TaskReturnCandidate hit = null;
        if (command.getTaskId() != null) {
            Long targetTaskId = command.getTaskId();
            for (TaskReturnCandidate c : availability.getCandidates()) {
                if (c != null && c.getTaskId() != null && c.getTaskId().equals(targetTaskId)) {
                    hit = c;
                    break;
                }
            }
            if (hit == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "targetTaskId is not a valid candidate.");
            }
            targetTracingTag = hit.getTracingTag();
            targetExecScope = hit.getExecScope();
        } else if (command.getTracingTag() != null && command.getTracingTag().trim().length() > 0) {
            String requested = command.getTracingTag().trim();
            for (TaskReturnCandidate c : availability.getCandidates()) {
                if (c != null && requested.equals(c.getTracingTag())) {
                    hit = c;
                    break;
                }
            }
            if (hit == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "tracingTag is not a valid candidate.");
            }
            targetTracingTag = hit.getTracingTag();
            targetExecScope = (command.getExecScope() != null && command.getExecScope().trim().length() > 0)
                    ? command.getExecScope().trim()
                    : hit.getExecScope();
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "targetTaskId or tracingTag is required.");
        }

        // 엔진 재실행(backToHere) 시 workitem이 "이전 담당자(B)"에게 생성되도록 컨텍스트 전환
        // - HumanActivity.getActualMapping()의 일부 권한/initiator 체크는 UserContext 기반으로 수행됨
        // - 따라서 backToHere 수행 동안만 UserContext/GlobalContext를 후보 endpoint로 잠깐 바꾼 뒤 원복한다.
        String engineUserId = null;
        if (hit != null && hit.getEndpoint() != null && hit.getEndpoint().trim().length() > 0) {
            engineUserId = hit.getEndpoint().trim();
        }
        if (engineUserId == null) {
            engineUserId = requestUserId;
        }
        String originalUserId = UserContext.getThreadLocalInstance().getUserId();
        String originalGlobalUserId = GlobalContext.getUserId();

        // 반송은 "삭제"가 아닌 "상태 업데이트 + 새 workitem 생성"이어야 함
        // - decision/reason은 "이전 담당자(B)에게 생성되는 새 태스크"에만 기록한다.
        current.setStatus(DefaultWorkList.WORKITEM_STATUS_SUSPENDED);
        if (command.getReason() != null && command.getReason().trim().length() > 0) {
            String existing = current.getDescription();
            String reason = command.getReason().trim();
            String msg = "[RETURN] to=" + targetTracingTag + " reason=" + reason;
            current.setDescription(existing == null || existing.trim().isEmpty() ? msg : (existing + "\n" + msg));
        }
        worklistRepository.save(current);

        // 목표 taskId(이전 담당자의 태스크 레코드)가 존재하면, 그 레코드에만 decision/reason을 기록한다.
        // - 과거 반송 기록을 덮어쓰지 않도록, decision이 이미 존재하면 수정하지 않는다.
        if (hit != null && hit.getTaskId() != null) {
            try {
                WorklistEntity targetWorkItem = worklistRepository.findById(hit.getTaskId()).orElse(null);
                if (targetWorkItem != null) {
                    if (targetWorkItem.getDecision() == null || targetWorkItem.getDecision().trim().isEmpty()) {
                        targetWorkItem.setDecision("RETURN");
                    }
                    if ((targetWorkItem.getReason() == null || targetWorkItem.getReason().trim().isEmpty())
                            && command.getReason() != null && !command.getReason().trim().isEmpty()) {
                        targetWorkItem.setReason(command.getReason()); // 그대로 저장
                    }
                    worklistRepository.save(targetWorkItem);
                }
            } catch (Exception ignore) {
            }
        }

        // execScope(있으면) 반영 후 backToHere 실행 → 엔진이 상태(Activity.STATUS_*)를 갱신하고 새 workitem을 추가로 생성
        if (targetExecScope != null && targetExecScope.trim().length() > 0) {
            instance.setExecutionScope(targetExecScope.trim());
        }

        Activity targetActivity = instance.getProcessDefinition().getActivity(targetTracingTag);
        if (targetActivity == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Target activity not found: " + targetTracingTag);
        }
        try {
            if (engineUserId != null && engineUserId.trim().length() > 0) {
                UserContext.getThreadLocalInstance().setUserId(engineUserId);
                GlobalContext.setUserId(engineUserId);
            }
            targetActivity.backToHere(instance);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Return failed: " + e.getMessage(), e);
        } finally {
            // 요청자 컨텍스트 원복
            UserContext.getThreadLocalInstance().setUserId(originalUserId);
            if (originalGlobalUserId != null) {
                GlobalContext.setUserId(originalGlobalUserId);
            }
        }

        Long rootInstId = current.getRootInstId() == null ? current.getInstId() : current.getRootInstId().longValue();
        List<WorklistEntity> currents = worklistRepository.findCurrentWorkItemByInstId(rootInstId);
        List<Long> currentTaskIds = new ArrayList<>();
        if (currents != null) {
            for (WorklistEntity wl : currents) {
                if (wl != null && wl.getTaskId() != null) {
                    currentTaskIds.add(wl.getTaskId());
                }
            }
        }

        TaskReturnResult result = new TaskReturnResult();
        result.setInstanceId(instanceId);
        result.setRootInstId(rootInstId);
        result.setTargetTracingTag(targetTracingTag);
        result.setCurrentTaskIds(currentTaskIds);
        return result;
    }

    /**
     * 태스크 SKIP(건너뛰기) 가능 여부 조회
     *
     * 기본 정책(보수적):
     * - HumanActivity + 실행 중이어야 함
     * - BoundaryEvent(attachedToRef)가 붙어있으면 SKIP 불가(부작용/미처리 이벤트 방지)
     * - 현재 execScope 기준으로 다음으로 진행 가능한 액티비티가 1개 이상 있어야 함
     */
    @RequestMapping(value = "/work-item/{taskId}/skip/availability", method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
    @ProcessTransactional(readOnly = true)
    public TaskSkipAvailability getTaskSkipAvailability(@PathVariable("taskId") String taskId) throws Exception {
        if (taskId == null || taskId.equals("null")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "taskId is required");
        }

        WorklistEntity current = worklistRepository.findById(new Long(taskId)).orElse(null);
        if (current == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No such work item where taskId = " + taskId);
        }

        // 컨텍스트 설정(조건 평가 등에 필요할 수 있음)
        String userId = SecurityAwareServletFilter.getUserId();
        if (userId != null) {
            GlobalContext.setUserId(userId);
        }

        String instanceId = String.valueOf(current.getInstId());
        ProcessInstance instance = getProcessInstanceLocal(instanceId);
        if (instance == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Instance not found for taskId=" + taskId);
        }

        Activity currentActivity = instance.getProcessDefinition().getActivity(current.getTrcTag());
        if (!(currentActivity instanceof HumanActivity)) {
            return TaskSkipAvailability.disabled("Human task only.");
        }

        HumanActivity humanActivity = (HumanActivity) currentActivity;

        // 일반적인 SKIP 가능 조건(Activity.isSkippable + 실행중)
        String status = humanActivity.getStatus(instance);
        if (!Activity.isSkippable(status) || (!instance.isRunning(humanActivity.getTracingTag()) && !humanActivity.isNotificationWorkitem())) {
            return TaskSkipAvailability.disabled("Already closed or illegal status.");
        }

        // Notification workitem은 기본적으로 SKIP 대상에서 제외(정책)
        if (humanActivity.isNotificationWorkitem()) {
            return TaskSkipAvailability.disabled("Notification workitem is not skippable.");
        }

        // BoundaryEvent(attachedToRef)가 붙어있으면 SKIP 불가(후속 이벤트/보상 처리 누락 방지)
        try {
            List<Activity> all = instance.getProcessDefinition().getChildActivities();
            if (all != null) {
                for (Activity a : all) {
                    if (a instanceof org.uengine.kernel.bpmn.Event) {
                        org.uengine.kernel.bpmn.Event ev = (org.uengine.kernel.bpmn.Event) a;
                        if (ev.getAttachedToRef() != null && ev.getAttachedToRef().equals(humanActivity.getTracingTag())) {
                            return TaskSkipAvailability.disabled("Boundary event attached: " + ev.getTracingTag());
                        }
                    }
                }
            }
        } catch (Exception ignore) {
        }

        // 정의(xml) + 현재 인스턴스 상태 기준으로 다음 진행 가능 여부를 확인(조건 분기 포함)
        try {
            String scope = current.getExecScope();
            List<Activity> nexts = humanActivity.getPossibleNextActivities(instance, scope);
            if (nexts == null || nexts.isEmpty()) {
                return TaskSkipAvailability.disabled("No possible next activity from current state.");
            }
        } catch (Exception e) {
            return TaskSkipAvailability.disabled("Cannot evaluate next activities: " + e.getMessage());
        }

        // ---------------- 변수 매핑 기반 SKIP 가능 여부(정적 분석, 보수적) ----------------
        try {
            TaskSkipAnalyzer.SkipVarReference hit = TaskSkipAnalyzer.findFirstBlockingReference(
                    instance,
                    humanActivity,
                    new TaskSkipAnalyzer.DefinitionResolver() {
                        @Override
                        public ProcessDefinition resolve(String definitionId, String version) throws Exception {
                            Object defObj = definitionService.getDefinition(definitionId, version);
                            if (defObj instanceof ProcessDefinition) {
                                return (ProcessDefinition) defObj;
                            }
                            return null;
                        }
                    });
            if (hit != null) {
                return TaskSkipAvailability.disabled("Uses mapped variable later: " + hit.varName
                        + " at " + hit.whereTracingTag + (hit.whereType != null ? (" (" + hit.whereType + ")") : ""));
            }
        } catch (Exception e) {
            // 분석 실패 시 보수적으로 불가 처리(원치 않으면 enabled로 바꿀 수 있음)
            return TaskSkipAvailability.disabled("Cannot analyze variable mapping usage: " + e.getMessage());
        }

        return TaskSkipAvailability.enabled();
    }

    /**
     * 태스크 SKIP 실행
     * - worklist 상태는 SKIPPED로 기록
     * - 엔진(Activity.STATUS_SKIPPED)로 상태 변경 후 다음 태스크로 진행
     */
    @RequestMapping(value = "/work-item/{taskId}/skip", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    @ProcessTransactional
    @Transactional(rollbackFor = { Exception.class })
    public TaskSkipResult skipWorkItem(
            @PathVariable("taskId") String taskId,
            @RequestBody(required = false) TaskSkipCommand command) throws Exception {

        if (taskId == null || taskId.equals("null")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "taskId is required");
        }

        // 요청 사용자 컨텍스트
        String requestUserId = UserContext.getThreadLocalInstance().getUserId();
        if (requestUserId == null || requestUserId.trim().isEmpty()) {
            requestUserId = SecurityAwareServletFilter.getUserId();
        }
        if (requestUserId != null && requestUserId.trim().length() > 0) {
            GlobalContext.setUserId(requestUserId);
        }

        WorklistEntity current = worklistRepository.findById(new Long(taskId)).orElse(null);
        if (current == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No such work item where taskId = " + taskId);
        }

        String instanceId = String.valueOf(current.getInstId());
        ProcessInstance instance = getProcessInstanceLocal(instanceId);
        if (instance == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Instance not found for taskId=" + taskId);
        }

        Activity currentActivity = instance.getProcessDefinition().getActivity(current.getTrcTag());
        if (!(currentActivity instanceof HumanActivity)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Human task only.");
        }
        HumanActivity humanActivity = (HumanActivity) currentActivity;

        // 권한 체크: 현재 담당자(Worklist 우선, 없으면 ActualMapping fallback)
        String currentOwner = current.getEndpoint();
        try {
            if ((currentOwner == null || currentOwner.trim().isEmpty()) && humanActivity != null) {
                RoleMapping actual = humanActivity.getActualMapping(instance);
                if (actual != null) {
                    currentOwner = actual.getEndpoint();
                }
            }
        } catch (Exception ignore) {
        }
        if (requestUserId != null && currentOwner != null && !requestUserId.equals(currentOwner)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "No permission to skip this task. currentOwner=" + currentOwner + ", userId=" + requestUserId);
        }

        // 가능여부 재검증(TOCTOU 방지)
        TaskSkipAvailability availability = getTaskSkipAvailability(taskId);
        if (!availability.isEnabled()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    availability.getReason() != null ? availability.getReason() : "Not allowed.");
        }

        // execScope 반영 후 SKIP
        if (current.getExecScope() != null && current.getExecScope().trim().length() > 0) {
            instance.setExecutionScope(current.getExecScope().trim());
        }

        // 엔진 상태 변경 + 다음 액티비티 진행
        instance.getProcessDefinition().flowControl("skip", instance, humanActivity.getTracingTag());

        // worklist 레코드 보강(결정/사유)
        try {
            WorklistEntity after = worklistRepository.findById(new Long(taskId)).orElse(null);
            if (after != null) {
                // SKIP 수행자 정보 보강 (endpoint/resName이 비어있으면 요청 사용자로 기록)
                try {
                    if (requestUserId != null && requestUserId.trim().length() > 0) {
                        applyActorToWorklistIfEmpty(after, requestUserId);
                    }
                } catch (Exception ignore) {
                }

                // after.setDecision("SKIP");
                if (command != null && command.getReason() != null && command.getReason().trim().length() > 0) {
                    if (after.getReason() == null || after.getReason().trim().isEmpty()) {
                        after.setReason(command.getReason()); // 그대로 저장
                    }
                    String existing = after.getDescription();
                    String msg = "[SKIP] reason=" + command.getReason().trim();
                    after.setDescription(existing == null || existing.trim().isEmpty() ? msg : (existing + "\n" + msg));
                }
                // 엔진(JPAWorkList.cancelWorkItem)에서 status를 SKIPPED로 세팅하지만, 혹시 모르니 한 번 더 보장
                after.setStatus("SKIPPED");
                after.setEndDate(new Date());
                worklistRepository.save(after);
            }
        } catch (Exception ignore) {
        }

        Long rootInstId = current.getRootInstId() == null ? current.getInstId() : current.getRootInstId().longValue();
        List<WorklistEntity> currents = worklistRepository.findCurrentWorkItemByInstId(rootInstId);
        List<Long> currentTaskIds = new ArrayList<>();
        if (currents != null) {
            for (WorklistEntity wl : currents) {
                if (wl != null && wl.getTaskId() != null) {
                    currentTaskIds.add(wl.getTaskId());
                }
            }
        }

        TaskSkipResult result = new TaskSkipResult();
        result.setInstanceId(instanceId);
        result.setRootInstId(rootInstId);
        result.setSkippedTracingTag(humanActivity.getTracingTag());
        result.setCurrentTaskIds(currentTaskIds);
        return result;
    // ----------------- scenario API (delegates to ScenarioController) -------------------- //

    @GetMapping(value = "/scenarios/{processDefinitionId}", produces = "application/json;charset=UTF-8")
    public List<Scenario> getScenarios(@PathVariable("processDefinitionId") String processDefinitionId) {
        try {
            return scenarioController.getScenarios(processDefinitionId);
        } catch (org.springframework.web.server.ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
        }
    }

    @RequestMapping(value = "/scenarios/{processDefinitionId}", method = RequestMethod.PUT, consumes = "application/json;charset=UTF-8", produces = "application/json;charset=UTF-8")
    public List<Scenario> putScenarios(@PathVariable("processDefinitionId") String processDefinitionId,
            @RequestBody List<Scenario> scenarios) {
        try {
            return scenarioController.putScenarios(processDefinitionId, scenarios);
        } catch (org.springframework.web.server.ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
        }
    }

    @PostMapping(value = "/scenarios/{processDefinitionId}/generate", produces = "application/json;charset=UTF-8")
    public List<Scenario> generateScenarios(
            @PathVariable("processDefinitionId") String processDefinitionId,
            @RequestParam(value = "merge", defaultValue = "false") boolean merge,
            @RequestParam(value = "includeGatewayBranches", defaultValue = "false") boolean includeGatewayBranches,
            @RequestParam(value = "includeDmnBranches", defaultValue = "false") boolean includeDmnBranches) {
        try {
            return scenarioController.generateScenarios(processDefinitionId, merge, includeGatewayBranches, includeDmnBranches);
        } catch (org.springframework.web.server.ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
        }
    }

    @PostMapping(value = "/scenarios/{processDefinitionId}/generate-branches", produces = "application/json;charset=UTF-8")
    public List<Scenario> generateBranches(@PathVariable("processDefinitionId") String processDefinitionId) {
        try {
            return scenarioController.generateBranches(processDefinitionId);
        } catch (org.springframework.web.server.ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
        }
    }

    @PostMapping(value = "/scenarios/{processDefinitionId}/run", consumes = "application/json;charset=UTF-8", produces = "application/json;charset=UTF-8")
    public RunResult runScenario(@PathVariable("processDefinitionId") String processDefinitionId,
            @RequestBody Map<String, Object> body) {
        try {
            return scenarioController.runScenario(processDefinitionId, body);
        } catch (org.springframework.web.server.ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
        }
    }

    /**
     * Execute business rule by ruleId.
     *
     * @param ruleId  Business rule ID
     * @param request Request body containing inputs map
     * @return Execution result with outcome, note, matchedRuleIndex, and
     *         executionTime
     */
    @PostMapping(value = "/business-rules/{ruleId}/execute", produces = "application/json;charset=UTF-8")
    @ProcessTransactional(readOnly = true)
    public Map<String, Object> executeBusinessRule(@PathVariable("ruleId") String ruleId,
            @RequestBody Map<String, Object> request) throws Exception {

        // 입력값 검증
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Request body is required");
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> inputs = (Map<String, Object>) request.get("inputs");
        if (inputs == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "inputs field is required");
        }

        // 실행 시간 측정 시작
        long startTime = System.currentTimeMillis();

        try {
            // 룰 조회
            BusinessRuleStore.BusinessRuleFile ruleFile = businessRuleStore.loadOrThrow(ruleId);
            JsonNode ruleJson = ruleFile.getRuleJson();

            // dmnXml 필드 확인
            JsonNode dmnXmlNode = ruleJson.get("dmnXml");
            if (dmnXmlNode == null || !dmnXmlNode.isTextual() || dmnXmlNode.asText().isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "DMN XML is not defined in the rule");
            }

            // DMN 실행
            Map<String, Object> evaluationResult = businessRuleEvaluator.evaluate(ruleJson, inputs);

            // 실행 시간 측정 종료
            long executionTime = System.currentTimeMillis() - startTime;

            // 결과 포맷팅
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("outcome", evaluationResult.get("outcome"));
            response.put("note", evaluationResult.get("note"));
            response.put("matchedRuleIndex", evaluationResult.get("matchedRuleIndex"));
            response.put("executionTime", executionTime);

            return response;

        } catch (ResponseStatusException e) {
            throw e;
        } catch (IllegalArgumentException e) {
            // DMN 파싱 실패 등
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "DMN execution error: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error executing business rule: " + e.getMessage(), e);
        }
    }

}
