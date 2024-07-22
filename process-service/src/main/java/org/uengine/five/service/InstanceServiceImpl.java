package org.uengine.five.service;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.QueryParam;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.HandlerMapping;
import org.uengine.five.ProcessServiceApplication;
import org.uengine.five.dto.DryRunExecutionCommand;
import org.uengine.five.dto.InstanceResource;
import org.uengine.five.dto.Message;
import org.uengine.five.dto.ProcessExecutionCommand;
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
import org.uengine.kernel.RoleMapping;
import org.uengine.kernel.UEngineException;
import org.uengine.kernel.ValidationContext;
import org.uengine.kernel.bpmn.CatchingRestMessageEvent;
import org.uengine.kernel.bpmn.SendTask;
import org.uengine.kernel.bpmn.SequenceFlow;
import org.uengine.kernel.bpmn.SignalEventInstance;
import org.uengine.kernel.bpmn.SignalIntermediateCatchEvent;
import org.uengine.kernel.bpmn.SubProcess;
import org.uengine.util.UEngineUtil;

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
public class InstanceServiceImpl implements InstanceService {

    @Autowired
    DefinitionServiceUtil definitionService;

    @Autowired
    ApplicationContext applicationContext;

    @Autowired
    WorklistRepository worklistRepository;

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
        //

        boolean simulation = command.isSimulation();
        String filePath = command.getProcessDefinitionId();
        String corrKeyValue = command.getCorrelationKeyValue();

        Object definition;
        try {
            String defPath = java.net.URLDecoder.decode(filePath, "UTF-8");
            definition = definitionService.getDefinition(defPath, !simulation); // if simulation time, use the version
                                                                                // under construction
        } catch (ClassNotFoundException cnfe) {
            // ClassNotFoundException을 처리하고, 500 Internal Server Error 반환
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Class not found", cnfe);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
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

        return new InstanceResource(instance);
    }

    @RequestMapping(value = "/instance/{instanceId}/eventList")
    @ProcessTransactional(readOnly = true)
    public Vector getEventList(@PathVariable("instanceId") String instanceId) throws Exception {
        ProcessInstance instance = getProcessInstanceLocal(instanceId);
        Vector messageListener = (Vector) instance.getMessageListeners("event");

        return messageListener;
    }

    @RequestMapping(value = "/instance/{instanceId}/activity/{tracingTag}/backToHere", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    @ProcessTransactional
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
        ProcessInstance instance = getProcessInstanceLocal(instanceId);
        WorkItemResource workItem = getWorkItem(taskId);
        ExecutionScopeContext oldExecutionScopeContext = instance.getExecutionScopeContext();

        if (workItem.getWorklist().getExecScope() != null) {
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
    public ProcessInstance getProcessInstanceLocal(String instanceId) {

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
                    true);

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
        ProcessDefinition definition = (ProcessDefinition) definitionService.getDefinition(defId);
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
        // Argument Key -> Value
        //
        if (parameterValues.size() > 0) {
            workItem.setParameterValues(parameterValues);
        }

        workItem.getWorklist().setProcessInstance(null); // disconnect recursive json path

        return workItem;
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

        // if (workItem.getWorklist() != null &&
        // "SAVED".equals(workItem.getWorklist().getStatus())) {
        // humanActivity.mappingOut(instance, variableChanges);
        // } else {
        // try {
        // humanActivity.fireReceived(instance, variableChanges);
        // } catch (Exception e) {
        // humanActivity.fireFault(instance, e);

        // throw new UEngineException(e.getMessage(), null, new
        // UEngineException(e.getMessage(), e), instance,
        // humanActivity);
        // }
        // }

    }

    @RequestMapping(value = "/work-item/{taskId}/complete", method = RequestMethod.POST)
    @org.springframework.transaction.annotation.Transactional
    @ProcessTransactional // important!
    public void putWorkItemComplete(@PathVariable("taskId") String taskId, @RequestBody WorkItemResource workItem)
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

        // map the argument list to variables change list
        Map<String, Object> parameterValues = workItem.getParameterValues();
        // if (workItem.getParameterValues() != null
        //         && humanActivity.getEventSynchronization().getMappingContext().getMappingElements() != null) {
        //     for (ParameterContext parameterContext : humanActivity.getEventSynchronization().getMappingContext()
        //             .getMappingElements()) {
        //         if (parameterContext.getDirection().indexOf("out") >= 0) {
        //             Serializable data;
        //             if (parameterContext.getTransformerMapping() != null) {
        //                 for 
        //                 // data =
        //                 // parameterContext.getTransformerMapping().getTransformer().letTransform(instance,
        //                 // null, null);
        //             } else {
        //                 String varName = parameterContext.getVariable().getName();
        //                 String[] parts = varName.split("\\.");
        //                 String name = String.join(".", Arrays.copyOfRange(parts, 1, parts.length));
        //                 if (workItem.getParameterValues().containsKey(name)) {
        //                     data = (Serializable) workItem.getParameterValues()
        //                             .get(name);
        //                     variableChanges.put(parameterContext.getVariable().getName(), data);
        //                 }

        //             }
        //             // Serializable data = (Serializable) workItem.getParameterValues()
        //             // .get(parameterContext.getArgument().getText());

        //             // if (data instanceof Map && ((Map) data).containsKey("_type")) {
        //             // String typeName = null;
        //             // try {
        //             // typeName = (String) ((Map) data).get("_type");
        //             // Class classType =
        //             // Thread.currentThread().getContextClassLoader().loadClass(typeName);
        //             // data = (Serializable)
        //             // ProcessServiceApplication.objectMapper.convertValue(data, classType);
        //             // } catch (Exception e) {
        //             // throw new Exception("Error while convert map to type: " + typeName, e);
        //             // }
        //             // }

        //         }
        //     }
        // }

        try {
            humanActivity.fireReceived(instance, parameterValues);
        } catch (Exception e) {
            humanActivity.fireFault(instance, e);

            throw new UEngineException(e.getMessage(), null, new UEngineException(e.getMessage(), e), instance,
                    humanActivity);
        }
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

    @ProcessTransactional(readOnly = true)
    @RequestMapping(value = "/dry-run/**", method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
    public Object getDryRun(HttpServletRequest request) throws Exception {
        String path = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        String definitionPath = path.substring("/dry-run".length() + 1);

        return getDryRun(definitionPath);
    }

    //

    @ProcessTransactional(readOnly = true)
    @RequestMapping(value = "/dry-run/{defId:.+}", method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
    public Object getDryRun(@PathVariable("defId") String defId) throws Exception {
        ProcessExecutionCommand command = new ProcessExecutionCommand();
        command.setProcessDefinitionId(defId);

        Object definition;
        try {
            definition = definitionService.getDefinition(defId, false);
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
                        command.getInstanceName(), null);

                org.uengine.five.dto.RoleMapping[] roleMappings = command.getRoleMappings();
                if (roleMappings != null) {
                    for (org.uengine.five.dto.RoleMapping roleMapping : roleMappings) {
                        instance.putRoleMapping(roleMapping.getName(), roleMapping.toKernelRoleMapping());
                    }
                }

                if (command.getCorrelationKeyValue() != null) {
                    ((JPAProcessInstance) instance).getProcessInstanceEntity()
                            .setCorrKey(command.getCorrelationKeyValue());
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

    @RequestMapping(value = "/dry-run", consumes = "application/json;charset=UTF-8", method = { RequestMethod.POST,
            RequestMethod.PUT }, produces = "application/json;charset=UTF-8")
    @Transactional(rollbackFor = { Exception.class })
    @ProcessTransactional
    public InstanceResource dryRunInstance(@RequestBody DryRunExecutionCommand command) throws Exception {
        try {
            ProcessExecutionCommand processExecutionCommand = command.getProcessExecutionCommand();
            InstanceResource instance = start(processExecutionCommand);

            if (instance == null)
                return null;

            String instId = instance.getInstanceId();
            WorklistEntity worklistEntity = worklistRepository.findCurrentWorkItemByInstId(new Long(instId));

            if (worklistEntity == null)
                return null;
            String taskId = worklistEntity.getTaskId().toString();

            if (command.getVariables() != null) {
                for (String varName : command.getVariables().keySet()) {
                    String json = command.getVariables().get(varName);
                    setVariableWithTaskId(instId, taskId, varName, json);
                }
            }

            putWorkItemComplete(taskId, command.getWorkItem());
            return instance;
        } catch (Exception e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error executing dry-run process instance: " + e.getMessage(), e);
        }
    }

    @RequestMapping(value = "/validation", method = RequestMethod.POST, consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
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
    public WorkItemResource getCurrentWorkItemByCorrKey(@RequestParam("corrKey") String corrKey) throws Exception {
        if (corrKey == null)
            return null;

        List<ProcessInstanceEntity> processInstanceList = processInstanceRepository.findByCorrKeyAndStatus(corrKey,
                "Running");
        for (ProcessInstanceEntity processInstanceEntity : processInstanceList) {
            WorklistEntity worklistEntity = worklistRepository
                    .findCurrentWorkItemByInstId(processInstanceEntity.getInstId());

            if (worklistEntity != null) {
                ProcessDefinition definition = (ProcessDefinition) definitionService
                        .getDefinition(worklistEntity.getDefId());
                HumanActivity activity = (HumanActivity) definition.getActivity(worklistEntity.getTrcTag());

                WorkItemResource workItem = new WorkItemResource();
                workItem.setActivity(activity);
                workItem.setWorklist(worklistEntity);

                return workItem;
            }
        }
        return null;
    }

}
