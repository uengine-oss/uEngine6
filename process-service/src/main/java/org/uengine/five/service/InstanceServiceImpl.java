package org.uengine.five.service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
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
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.MimeTypeUtils;
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
import org.uengine.contexts.HtmlFormContext;
import org.uengine.five.ProcessServiceApplication;
import org.uengine.five.Streams;
import org.uengine.five.dto.InstanceResource;
import org.uengine.five.dto.Message;
import org.uengine.five.dto.ProcessExecutionCommand;
import org.uengine.five.dto.StartAndCompleteCommand;
import org.uengine.five.dto.WorkItemResource;
import org.uengine.five.entity.ProcessInstanceEntity;
import org.uengine.five.entity.ServiceEndpointEntity;
import org.uengine.five.entity.WorklistEntity;
import org.uengine.five.framework.ProcessTransactionContext;
import org.uengine.five.framework.ProcessTransactional;
import org.uengine.five.overriding.JPAProcessInstance;
import org.uengine.five.repository.ProcessInstanceRepository;
import org.uengine.five.repository.ServiceEndpointRepository;
import org.uengine.five.repository.WorklistRepository;
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
import org.uengine.kernel.ReceiveActivity;
import org.uengine.kernel.RoleMapping;
import org.uengine.kernel.UEngineException;
import org.uengine.kernel.ValidationContext;
import org.uengine.kernel.bpmn.CatchingRestMessageEvent;
import org.uengine.kernel.bpmn.Event;
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

    static ObjectMapper objectMapper = BpmnXMLParser.createTypedJsonObjectMapper();
    static ObjectMapper arrayObjectMapper = BpmnXMLParser.createTypedJsonArrayObjectMapper();

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

                ((JPAProcessInstance) instance).getProcessInstanceEntity().setDefVerId(processDefinition.getVersion());
                // instance.setDefinitionVersionId(processDefinition.getVersion());
                instance.execute();
                return new InstanceResource(instance); // TODO: returns HATEOAS _self link instead.
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
            String name1 = f1.getName().replace(".bpmn", "");
            String name2 = f2.getName().replace(".bpmn", "");

            boolean isName1Parsable = isParsableAsFloat(name1);
            boolean isName2Parsable = isParsableAsFloat(name2);

            if (isName1Parsable && isName2Parsable) {
                return Float.compare(Float.parseFloat(name2), Float.parseFloat(name1));
            } else if (isName1Parsable) {
                return -1;
            } else if (isName2Parsable) {
                return 1;
            } else {
                return name1.compareTo(name2);
            }
        });

        return files[0].getName().replace(".bpmn", "");
    }

    private boolean isParsableAsFloat(String str) {
        try {
            Float.parseFloat(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
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

        // original code
        // if (workItem.getWorklist().getExecScope() != null) {
        //     if (instance.getExecutionScopeContext() == null) {
        //         ExecutionScopeContext executionScopeContext = new ExecutionScopeContext();
        //         executionScopeContext.setExecutionScope(workItem.getWorklist().getExecScope());
        //         instance.setExecutionScopeContext(executionScopeContext);
        //     }
        // }
        // result = instance.get("", varName);
        // instance.setExecutionScopeContext(oldExecutionScopeContext);

        // 서브 프로세스 관련.
        if (workItem.getWorklist().getExecScope() == null) {
            if(instance.getExecutionScopeContextTree() != null && instance.getExecutionScopeContextTree().getChilds() != null) {
                ArrayList<Serializable> resultList = new ArrayList<>();
                instance.getExecutionScopeContextTree().getChilds().forEach(child -> {
                    ExecutionScopeContext executionScopeContext = new ExecutionScopeContext();
                    executionScopeContext.setExecutionScope(child.getExecutionScope());
                    instance.setExecutionScopeContext(executionScopeContext);
                    try {
                        // if(child.getRootActivityInTheScope().getStatus(instance) == 'Completed'){}
                        HtmlFormContext htmlFormContext = (HtmlFormContext) instance.get("", varName);
                        htmlFormContext.setSubProcessLabel(((SubProcess)child.getRootActivityInTheScope()).getSubProcessLabel(instance));
                        resultList.add(htmlFormContext);
                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                });  
                result = resultList;
            } else {
                result = instance.get("", varName);
            }
        } else {
            if (instance.getExecutionScopeContext() == null) {
                ExecutionScopeContext executionScopeContext = new ExecutionScopeContext();
                executionScopeContext.setExecutionScope(workItem.getWorklist().getExecScope());
                instance.setExecutionScopeContext(executionScopeContext);
            }
            result = instance.get("", varName);
        }
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
            @PathVariable("roleName") String roleName, @RequestBody RoleMapping roleMapping) throws Exception {

        ProcessInstance instance = applicationContext.getBean(
                ProcessInstance.class,
                new Object[] {
                        null,
                        instanceId,
                        null
                });
        // 예상에는, 롤매핑도 인스턴스처럼 DB 에 넣고, 튀어나오는 아이디를 roleMapping 객체에 넣은다음
        // instance.putRoleMapping 을 해야할듯.?
        instance.putRoleMapping(roleName, roleMapping);

        roleMapping.setName(roleName);

        return roleMapping;
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
        IResource resource = new DefaultResource(
                "payloads/" + currentYear + "/" + currentMonth + "/" + instance.getInstanceId());

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

    @RequestMapping(value = "/work-item/{taskId}/complete", method = RequestMethod.POST)
    @ProcessTransactional // important!
    @Transactional(rollbackFor = { Exception.class })
    public void putWorkItemComplete(@PathVariable("taskId") String taskId, @RequestBody WorkItemResource workItem,
            @RequestHeader("isSimulate") String isSimulate)

            throws Exception {

        // instance.setExecutionScope(esc.getExecutionScope());
        WorklistEntity worklistEntity = worklistRepository.findById(new Long(taskId)).get();

        String instanceId = worklistEntity.getInstId().toString();
        ProcessInstance instance = getProcessInstanceLocal(instanceId);

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

        // if (isSimulate.equals("record")) {

        // } else {
        // boolean simulate = Boolean.parseBoolean(isSimulate);
        // if (simulate) {
        // Map<String, Object> readValues = readFromFile(
        // instance.getProcessDefinition().getId() + "/" + humanActivity.getTracingTag()
        // + ".json");
        // workItem.setParameterValues(readValues);
        // }
        // }

        // map the argument list to variables change list
        Map<String, Object> parameterValues = workItem.getParameterValues();

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String payload = objectMapper.writeValueAsString(parameterValues);
            ProcessTransactionContext tc = ProcessTransactionContext.getThreadLocalInstance();
            tc.setSharedContext(humanActivity.getTracingTag() + "_payload@" + instance.getInstanceId() + ":", payload);

            humanActivity.fireReceived(instance, parameterValues);
            // writeToFile(instance.getProcessDefinition().getId()+"/"+humanActivity.getTracingTag()+".json",
            // "result: " + instance.getAll().toString() + "}");
            //
        } catch (Exception e) {
            humanActivity.fireFault(instance, e);

            throw new UEngineException(e.getMessage(), null, new UEngineException(e.getMessage(), e), instance,
                    humanActivity);
        }
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

            if (defPath.endsWith("form"))
                return;

            ProcessDefinition definition = (ProcessDefinition) definitionService.getDefinition(defPath);
            definition.setId(defPath);

            if (definition != null && definition instanceof ProcessDefinition) {
                invokeDeployFilters(definition, defPath);
            }
        } catch (Exception e) {
            throw new ResourceNotFoundException("Post CreatedRawDefinition : " + e.getMessage(), e);
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

}
