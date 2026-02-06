package org.uengine.five.overriding;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.uengine.five.entity.ProcessInstanceEntity;
import org.uengine.five.entity.WorklistEntity;
import org.uengine.five.framework.ProcessTransactionContext;
import org.uengine.five.repository.ProcessInstanceRepository;
import org.uengine.five.repository.WorklistRepository;
import org.uengine.five.service.DefinitionServiceUtil;
import org.uengine.five.service.InstanceServiceImpl;
import org.uengine.kernel.AbstractProcessInstance;
import org.uengine.kernel.Activity;
import org.uengine.kernel.DefaultProcessInstance;
import org.uengine.kernel.ExecutionScopeContext;
import org.uengine.kernel.ProcessDefinition;
import org.uengine.kernel.ProcessInstance;
import org.uengine.kernel.RoleMapping;
import org.uengine.kernel.TransactionListener;
import org.uengine.kernel.UEngineException;
import org.uengine.kernel.bpmn.SubProcess;
import org.uengine.modeling.resource.DefaultResource;
import org.uengine.modeling.resource.IResource;
import org.uengine.modeling.resource.ResourceManager;
import org.uengine.modeling.resource.Serializer;
import org.uengine.processmanager.TransactionContext;
import org.uengine.webservices.worklist.WorkList;

/**
 * Created by uengine on 2017. 8. 9..
 */
// @Component
@Transactional
// @Scope("prototype")
public class JPAProcessInstance extends DefaultProcessInstance implements TransactionListener {

    @Autowired
    DefinitionServiceUtil definitionService;

    @Autowired
    JPAWorkList jpaWorkList;

    @Autowired
    ResourceManager resourceManager;

    @Autowired
    InstanceServiceImpl instanceService;

    @Autowired
    ProcessInstanceRepository processInstanceRepository;

    @Autowired
    WorklistRepository worklistRepository;

    ProcessInstanceEntity processInstanceEntity;

    public ProcessInstanceEntity getProcessInstanceEntity() {
        return processInstanceEntity;
    }

    public void setProcessInstanceEntity(ProcessInstanceEntity processInstanceEntity) {
        this.processInstanceEntity = processInstanceEntity;
    }

    boolean newInstance;

    public boolean isNewInstance() {
        return newInstance;
    }

    public void setNewInstance(boolean newInstance) {
        this.newInstance = newInstance;
    }

    boolean prototype;

    public boolean isPrototype() {
        return prototype;
    }

    public void setPrototype(boolean prototype) {
        this.prototype = prototype;
    }

    public JPAProcessInstance(ProcessDefinition procDefinition, String instanceId, Map options) throws Exception {

        super(procDefinition, instanceId, options);

        if (instanceId == null && procDefinition == null) {
            setPrototype(true);
            return;
        }

        if (instanceId == null) {

            setNewInstance(true);
            setProcessInstanceEntity(new ProcessInstanceEntity());

            if (options != null) {
                if (options.containsKey(AbstractProcessInstance.INIT_OPTION_INSTANCE_NAME)) {
                    getProcessInstanceEntity()
                            .setName((String) options.get(AbstractProcessInstance.INIT_OPTION_INSTANCE_NAME));

                }
            }

            getProcessInstanceEntity().setName(getProcessDefinition().getName() + instanceId);
            getProcessInstanceEntity().setDefId(procDefinition.getId());
            getProcessInstanceEntity().setCorrKey(UUID.randomUUID().toString());
            getProcessInstanceEntity().setStatus(Activity.STATUS_READY);
            getProcessInstanceEntity().setDefName(procDefinition.getName());

            // if(procDef.getModifiedDate()!=null)
            // processInstanceDAO.setDefModDate(procDef.getModifiedDate().getTime());

            //
            // processInstanceDAO.setName(name);
            // setName(name);

            getProcessInstanceEntity().setStartedDate(new Date());

            boolean isSubProcess = (options != null && options.containsKey("isSubProcess")
                    && options.get("isSubProcess").equals("yes"));

            if (isSubProcess) {
                getProcessInstanceEntity().setSubProcess(true);
                getProcessInstanceEntity()
                        .setMainInstId(new Long((String) options.get(DefaultProcessInstance.RETURNING_PROCESS)));
                getProcessInstanceEntity()
                        .setMainActTrcTag((String) options.get(DefaultProcessInstance.RETURNING_TRACINGTAG));
                getProcessInstanceEntity()
                        .setMainExecScope((String) options.get(DefaultProcessInstance.RETURNING_EXECSCOPE));
                getProcessInstanceEntity()
                        .setDontReturn(((Boolean) options.get(DefaultProcessInstance.DONT_RETURN)).booleanValue());
                getProcessInstanceEntity().setEventHandler(options.containsKey("isEventHandler"));

                // TODO: need main process definition object instance from argument not the link
                // (id) or the cache will provide the cached one
            } else {
                mainProcessInstance = this;
                rootProcessInstance = this;
            }

            if (options != null) {
                if (options.containsKey(DefaultProcessInstance.ROOT_PROCESS)) {
                    getProcessInstanceEntity()
                            .setRootInstId(new Long((String) options.get(DefaultProcessInstance.ROOT_PROCESS)));
                }

                if (options.containsKey(DefaultProcessInstance.SIMULATIONPROCESS)) {
                    setSimulation(true);
                }
            }

        }

        if (procDefinition != null && procDefinition.isVolatile())
            setPrototype(true);
    }

    // @Autowired
    // ApplicationEventPublisher applicationEventPublisher; //TODO see the
    // DefinitionService.beforeProcessInstanceCommit() and move to here someday
    @PostConstruct
    public void init() throws Exception {

        if (isPrototype())
            return;

        if (isNewInstance()) { // if new instance, create one
            processInstanceRepository.save(getProcessInstanceEntity());
            if (getProcessInstanceEntity().getRootInstId() == null) {
                getProcessInstanceEntity().setRootInstId(getProcessInstanceEntity().getInstId());
            }
        } else { // else, load the instance
            processInstanceRepository.findById(Long.valueOf(getInstanceId())).ifPresent(entity -> {
                setProcessInstanceEntity(entity);

            });

            if (getProcessInstanceEntity() == null)
                throw new UEngineException("No such process instance where id = " + getInstanceId());

            Map variables = loadVariables();
            setVariables(variables);

        }

        setInstanceId(String.valueOf(getProcessInstanceEntity().getInstId()));

        // Add this instance as transaction listener and register this so that it can be
        // cached.
        ProcessTransactionContext.getThreadLocalInstance().addTransactionListener(this);
        ProcessTransactionContext.getThreadLocalInstance().registerProcessInstance(this);

        this.ptc = ProcessTransactionContext.getThreadLocalInstance();

        // applicationEventPublisher.publishEvent(new ProcessInstanceChangeEvent(this));
    }



    @Override
    public ProcessDefinition getProcessDefinition() throws Exception {
        ProcessDefinition definition = super.getProcessDefinition();
        if (definition == null) {
            setProcessDefinition((ProcessDefinition) definitionService.getDefinition(processInstanceEntity.getDefId(),processInstanceEntity.getDefVerId()));
        }   
        return super.getProcessDefinition();
    }

    @Override
    public WorkList getWorkList() {
        return jpaWorkList;
    }

    @Override
    public ProcessInstance getInstance(String instanceId, Map options) throws Exception {

        ProcessTransactionContext ptc = ProcessTransactionContext.getThreadLocalInstance();
        ProcessInstance instance = ptc.getProcessInstanceInTransaction(instanceId);

        String executionScope = null;
        if (instanceId.indexOf("@") > 0) {
            String[] instanceIdAndExecutionScope = instanceId.split("@");
            instanceId = instanceIdAndExecutionScope[0];

            executionScope = instanceIdAndExecutionScope[1];
        }

        if (executionScope != null) {
            instance = ptc.getProcessInstanceInTransaction(instanceId);
            instance.setExecutionScope(executionScope);
        }

        return instanceService.getProcessInstanceLocal(instanceId);
    }

    public String getCorrKey() {
        return String.valueOf(getProcessInstanceEntity().getCorrKey());
    }

    @Override
    public String getRootProcessInstanceId() {
        return String.valueOf(getProcessInstanceEntity().getRootInstId());
    }

    @Override
    public String getMainProcessInstanceId() {
        return String.valueOf(getProcessInstanceEntity().getMainInstId());
    }

    @Override
    public String getMainActivityTracingTag() {
        return String.valueOf(getProcessInstanceEntity().getMainActTrcTag());
    }

    @Override
    public RoleMapping getRoleMapping(String roleName) throws Exception {
        return super.getRoleMapping(roleName);
    }

    @Override
    public void putRoleMapping(RoleMapping roleMap) throws Exception {
        super.putRoleMapping(roleMap);
    }

    ProcessInstance mainProcessInstance;

    @Override
    public ProcessInstance getMainProcessInstance() throws Exception {
        if (mainProcessInstance != null)
            return mainProcessInstance;

        if (getMainProcessInstanceId() == null)
            return null;

        mainProcessInstance = instanceService.getProcessInstanceLocal(getMainProcessInstanceId());

        return mainProcessInstance;
    }

    ProcessInstance rootProcessInstance;

    @Override
    public ProcessInstance getRootProcessInstance() throws Exception {
        if (rootProcessInstance != null)
            return rootProcessInstance;

        if (getRootProcessInstanceId() == null)
            return null;

        rootProcessInstance = instanceService.getProcessInstanceLocal(getRootProcessInstanceId());

        return rootProcessInstance;
    }

    @Override
    public boolean isSubProcess() throws Exception {
        return getProcessInstanceEntity().isSubProcess();
    }

    @Override
    public void beforeCommit(TransactionContext tx) throws Exception {
        // 인스턴스 파일과 DB 상태 동기화: 인스턴스 파일의 상태를 DB에 반영
        // 인스턴스 파일의 상태가 더 정확한 경우가 있으므로, 인스턴스 파일의 상태를 우선시
        try {
            String instanceFileStatus = getStatus("");
            if (instanceFileStatus != null && !instanceFileStatus.equals(getProcessInstanceEntity().getStatus())) {
                getProcessInstanceEntity().setStatus(instanceFileStatus);
            }
        } catch (Exception e) {
            // 상태 동기화 실패 시 무시하고 계속 진행
        }
        
        processInstanceRepository.save(getProcessInstanceEntity());
        saveVariables();
    }

    @Override
    public void beforeRollback(TransactionContext tx) throws Exception {

    }

    @Override
    public void afterCommit(TransactionContext tx) throws Exception {

    }

    @Override
    public void afterRollback(TransactionContext tx) throws Exception {

    }

    protected Map loadVariables() throws Exception {
        Date date = getProcessInstanceEntity().getStartedDate();
       
        String currentYear = String.valueOf(date.getYear() + 1900);
        String currentMonth = String.format("%02d", date.getMonth() + 1);
        IResource resource = new DefaultResource("instances/" + currentYear + "/" + currentMonth + "/" + getInstanceId());
        return (Map) resourceManager.getObject(resource);
    }

    protected void saveVariables() throws Exception {
        // DB 상태를 인스턴스 파일에 동기화: 저장 전에 DB 상태를 반영
        ProcessInstanceEntity entity = getProcessInstanceEntity();
        if (entity != null && getVariables() != null) {
            // 1. 인스턴스 전체 상태 동기화
            if (entity.getStatus() != null) {
                String dbStatus = entity.getStatus();
                getVariables().put(":_status:prop", dbStatus);
            }
            
            // 2. 각 Activity/태스크 상태 동기화 (WorklistEntity 기반)
            try {
                Long rootInstId = entity.getRootInstId() != null ? entity.getRootInstId() : entity.getInstId();
                if (rootInstId != null) {
                    // rootInstId로 모든 WorklistEntity 조회
                    List<WorklistEntity> worklists = worklistRepository.findWorkListByInstId(rootInstId);
                    if (worklists != null) {
                        for (WorklistEntity wl : worklists) {
                            if (wl != null && wl.getTrcTag() != null && wl.getStatus() != null) {
                                String trcTag = wl.getTrcTag();
                                String worklistStatus = wl.getStatus();
                                
                                // WorklistEntity 상태를 Activity 상태로 변환
                                String activityStatus = convertWorklistStatusToActivityStatus(worklistStatus);
                                if (activityStatus != null) {
                                    // 인스턴스 파일의 Activity 상태 업데이트
                                    String statusKey = trcTag + ":_status:prop";
                                    getVariables().put(statusKey, activityStatus);
                                }
                            }
                        }
                    }
                    
                    // 현재 실행 중인 태스크도 포함
                    List<WorklistEntity> currentWorklists = worklistRepository.findCurrentWorkItemByInstId(rootInstId);
                    if (currentWorklists != null) {
                        for (WorklistEntity wl : currentWorklists) {
                            if (wl != null && wl.getTrcTag() != null && wl.getStatus() != null) {
                                String trcTag = wl.getTrcTag();
                                String worklistStatus = wl.getStatus();
                                
                                // WorklistEntity 상태를 Activity 상태로 변환
                                String activityStatus = convertWorklistStatusToActivityStatus(worklistStatus);
                                if (activityStatus != null) {
                                    // 인스턴스 파일의 Activity 상태 업데이트
                                    String statusKey = trcTag + ":_status:prop";
                                    getVariables().put(statusKey, activityStatus);
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                // Worklist 동기화 실패 시 무시하고 계속 진행
                e.printStackTrace();
            }
        }
        
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        String currentYear = String.valueOf(calendar.get(java.util.Calendar.YEAR));
        String currentMonth = String.format("%02d", calendar.get(java.util.Calendar.MONTH) + 1);
        IResource resource = new DefaultResource("instances/" + currentYear + "/" + currentMonth + "/" + getInstanceId());
        resourceManager.save(resource, getVariables());
    }
    
    /**
     * WorklistEntity의 상태를 Activity 상태로 변환
     * WorklistEntity.status → Activity.status 매핑
     */
    private String convertWorklistStatusToActivityStatus(String worklistStatus) {
        if (worklistStatus == null) {
            return null;
        }
        
        // WorklistEntity 상태 → Activity 상태 매핑
        switch (worklistStatus.toUpperCase()) {
            case "COMPLETED":
                return Activity.STATUS_COMPLETED; // "Completed"
            case "SKIPPED":
                return Activity.STATUS_SKIPPED; // "Skipped"
            case "RUNNING":
                return Activity.STATUS_RUNNING; // "Running"
            case "NEW":
                return Activity.STATUS_RUNNING; // "Running" (NEW는 실행 중으로 간주)
            case "SUSPENDED":
                return Activity.STATUS_SUSPENDED; // "Suspended"
            case "CANCELLED":
                return Activity.STATUS_CANCELLED; // "Cancelled"
            default:
                // 알 수 없는 상태는 그대로 반환 (또는 null)
                return null;
        }
    }

    public void setStatus(String scope, String status) throws Exception {
        super.setStatus(scope, status);

        ProcessInstanceEntity pi = null;
        // forward status of pi to processinstance
        if (scope.equals("")) {
            // remove if this instance doesn't need to be archived
            if (status.equals(Activity.STATUS_COMPLETED) && !getProcessDefinition().isArchive()) {
                remove();
            } else {
                pi = getProcessInstanceEntity();
                pi.setStatus(status);
            }

            // when the instance is completed or stopped.
            if (status.equals(Activity.STATUS_COMPLETED) || status.equals(Activity.STATUS_STOPPED)) {
                if (pi == null) {
                    pi = getProcessInstanceEntity();
                }
                pi.setFinishedDate(new Date());
                // beforeCommit에서 JPAProcessInstance 리스너가 FlowActivity 리스너보다 먼저 호출되므로
                // 루트 완료/중지 시 여기서 즉시 변수 저장하여 variablesPath 파일의 ":_status:prop"가 일치하도록 함
                if (pi != null) {
                    saveVariables();
                }
            }
        }

    }

    public void stop(String status) throws Exception {

        if (isSimulation()) {
            try {
                super.stop(status);
            } catch (Exception e) {

            }
        } else {
            super.stop(status);
        }
        getProcessInstanceEntity().setStatus(status);
    }

    @Override
    public String getName() {
        if (getProcessInstanceEntity() != null)
            return getProcessInstanceEntity().getName();

        else
            return null;
    }

    @Override
    public void setName(String value) {
        if (getProcessInstanceEntity() != null)
            getProcessInstanceEntity().setName(value);
    }

    @Override
    public String getInfo() {
        if (getProcessInstanceEntity() != null)
            return getProcessInstanceEntity().getInfo();

        else
            return null;
    }

    @Override
    public void setInfo(String value) {
        if (getProcessInstanceEntity() != null)
            getProcessInstanceEntity().setInfo(value);
    }

    public String getExt1() {
        if (getProcessInstanceEntity() != null)
            return getProcessInstanceEntity().getExt1();
        else
            return null;
    }

    public void setExt1(String value) {
        if (getProcessInstanceEntity() != null)
            getProcessInstanceEntity().setExt1(value);
    }

    public String getExt2() {
        if (getProcessInstanceEntity() != null)
            return getProcessInstanceEntity().getExt2();
        else
            return null;
    }

    public void setExt2(String value) {
        if (getProcessInstanceEntity() != null)
            getProcessInstanceEntity().setExt2(value);
    }

    public String getExt3() {
        if (getProcessInstanceEntity() != null)
            return getProcessInstanceEntity().getExt3();
        else
            return null;
    }

    public void setExt3(String value) {
        if (getProcessInstanceEntity() != null)
            getProcessInstanceEntity().setExt3(value);
    }

    public String getExt4() {
        if (getProcessInstanceEntity() != null)
            return getProcessInstanceEntity().getExt4();
        else
            return null;
    }

    public void setExt4(String value) {
        if (getProcessInstanceEntity() != null)
            getProcessInstanceEntity().setExt4(value);
    }

    public String getExt5() {
        if (getProcessInstanceEntity() != null)
            return getProcessInstanceEntity().getExt5();
        else
            return null;
    }

    public void setExt5(String value) {
        if (getProcessInstanceEntity() != null)
            getProcessInstanceEntity().setExt5(value);
    }

    public Date getDueDate2() {
        if (getProcessInstanceEntity() != null)
            return getProcessInstanceEntity().getDueDate();
        else
            return null;
    }

    public void setDueDate(String value) {
        if (getProcessInstanceEntity() != null) {
            try {
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
                Date dueDate;
                dueDate = formatter.parse(value);
                getProcessInstanceEntity().setDueDate(dueDate);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    public void execute(String tracingTag, boolean forceToQueue) throws Exception {
        final Activity activity = getProcessDefinition().getActivity(tracingTag);

        if (activity.getParentActivity() != null) {
            if (!(activity.getParentActivity() instanceof SubProcess)) {
                setExecutionScopeContext(null);
            } else {
                if (getExecutionScopeContextTree() != null && getExecutionScopeContextTree().getChilds() != null) {
                    ExecutionScopeContext parentExecutionScopeContext = getParentExecutionScopeContext(
                            getExecutionScopeContextTree(), activity.getParentActivity());
                    if (parentExecutionScopeContext != null) {
                        setExecutionScopeContext(parentExecutionScopeContext);
                    }
                }
            }
        }
        super.execute(activity, tracingTag, forceToQueue);
    }

    ExecutionScopeContext getParentExecutionScopeContext(ExecutionScopeContext executionScopeContext,
            Activity activity) {
        ExecutionScopeContext result = null;
        for (ExecutionScopeContext child : executionScopeContext.getChilds()) {
            if (child.getRootActivityTracingTag().equals(activity.getTracingTag())) {
                if (getExecutionScopeContext().getExecutionScope().equals(child.getExecutionScope())) {
                    return child;
                }
            } else {
                if (child.getChilds() != null) {
                    return getParentExecutionScopeContext(child, activity);
                }
            }
        }
        return result;
    }
}
