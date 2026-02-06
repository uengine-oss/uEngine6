package org.uengine.five.overriding;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.util.MimeTypeUtils;
import org.uengine.five.Streams;
import org.uengine.five.entity.ProcessInstanceEntity;
import org.uengine.five.framework.ProcessTransactionContext;
import org.uengine.five.repository.ProcessInstanceRepository;
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
import org.uengine.processmanager.EMailServiceLocal;
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
    ApplicationContext applicationContext;

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
    EMailServiceLocal emailService;

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
                            .setName((String) options.get(AbstractProcessInstance.INIT_OPTION_INSTANCE_NAME)
                                    + instanceId);

                }
            }

            getProcessInstanceEntity().setName(getProcessDefinition().getName());
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
            String instanceIdStr = getInstanceId();
            
            // instanceId가 null, "undefined", 또는 빈 문자열인 경우 처리
            if (instanceIdStr == null || instanceIdStr.trim().isEmpty() || "undefined".equals(instanceIdStr)) {
                throw new UEngineException("Invalid instance ID: " + instanceIdStr + ". Cannot load process instance.");
            }
            
            try {
                Long instanceId = Long.valueOf(instanceIdStr);
                processInstanceRepository.findById(instanceId).ifPresent(entity -> {
                    setProcessInstanceEntity(entity);
                });
            } catch (NumberFormatException e) {
                throw new UEngineException("Invalid instance ID format: " + instanceIdStr + ". Expected a numeric value.", e);
            }

            if (getProcessInstanceEntity() == null)
                throw new UEngineException("No such process instance where id = " + instanceIdStr);

            Map variables = loadVariables();
            setVariables(variables);

        }

        setInstanceId(String.valueOf(getProcessInstanceEntity().getInstId()));

        ProcessTransactionContext.getThreadLocalInstance().addTransactionListener(this);
        ProcessTransactionContext.getThreadLocalInstance().registerProcessInstance(this);

        this.ptc = ProcessTransactionContext.getThreadLocalInstance();

        // applicationEventPublisher.publishEvent(new ProcessInstanceChangeEvent(this));
    }

    @Override
    public ProcessDefinition getProcessDefinition() throws Exception {
        ProcessDefinition definition = super.getProcessDefinition();
        if (definition == null) {
            setProcessDefinition((ProcessDefinition) definitionService.getDefinition(processInstanceEntity.getDefId(),
                    processInstanceEntity.getDefVerId()));
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
        processInstanceRepository.save(getProcessInstanceEntity());
        saveVariables();
    }

    @Override
    public void beforeRollback(TransactionContext tx) throws Exception {

    }

    @Override
    public void afterCommit(TransactionContext tx) throws Exception {
        // variablesPath에 설정된 경로가 아닌 이전 파일 삭제 (동일 인스턴스의 고아 파일 정리)
        if (variablesPathPreviousBeforeTx != null && !variablesPathPreviousBeforeTx.equals(variablesPathCreatedThisTx)) {
            try {
                IResource prevResource = new DefaultResource(variablesPathPreviousBeforeTx);
                if (resourceManager.exists(prevResource)) {
                    resourceManager.delete(prevResource);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        variablesPathCreatedThisTx = null;
        variablesPathPreviousBeforeTx = null;
    }

    /** 롤백 시 이번 트랜잭션에서 생성한 새 파일만 삭제 → DB와 파일 일치. */
    @Override
    public void afterRollback(TransactionContext tx) throws Exception {
        if (variablesPathCreatedThisTx != null) {
            try {
                IResource createdResource = new DefaultResource(variablesPathCreatedThisTx);
                if (resourceManager.exists(createdResource)) {
                    resourceManager.delete(createdResource);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            variablesPathCreatedThisTx = null;
        }
        variablesPathPreviousBeforeTx = null;
    }

    /** 이번 트랜잭션에서 생성한 변수 파일 경로. 롤백 시 삭제용. */
    private transient String variablesPathCreatedThisTx;
    /** 커밋 전 variablesPath. afterCommit에서 이전 파일 삭제용. */
    private transient String variablesPathPreviousBeforeTx;

    /** DB에 저장된 경로가 있으면 사용, 없으면 startedDate 기준 계산 경로(하위 호환). */
    protected String getInstanceVariablesPath() {
        String path = getProcessInstanceEntity().getVariablesPath();
        if (path != null && !path.isEmpty()) {
            return path;
        }
        return computeDefaultVariablesPath();
    }

    /** startedDate 기준 인스턴스 디렉터리 경로 (instances/YYYY/MM/DD/instanceId). */
    protected String computeDefaultVariablesPath() {
        Date date = getProcessInstanceEntity().getStartedDate();
        if (date == null) {
            java.util.Calendar calendar = java.util.Calendar.getInstance();
            String currentYear = String.valueOf(calendar.get(java.util.Calendar.YEAR));
            String currentMonth = String.format("%02d", calendar.get(java.util.Calendar.MONTH) + 1);
            String currentDay = String.format("%02d", calendar.get(java.util.Calendar.DAY_OF_MONTH));
            return "instances/" + currentYear + "/" + currentMonth + "/" + currentDay + "/" + getInstanceId();
        }
        String year = String.valueOf(date.getYear() + 1900);
        String month = String.format("%02d", date.getMonth() + 1);
        String day = String.format("%02d", date.getDate());
        return "instances/" + year + "/" + month + "/" + day + "/" + getInstanceId();
    }

    /** 트랜잭션마다 새 파일 경로 생성 (시간 순서). DB+파일 트랜잭션 일치를 위해 beforeCommit에서 사용. */
    protected String generateNewVariablesPath() {
        return computeDefaultVariablesPath() + "_" + System.currentTimeMillis();
    }

    /** variablesPath(또는 기본 경로)에서 변수 로드. */
    protected Map loadVariables() throws Exception {
        String path = getInstanceVariablesPath();
        IResource resource = new DefaultResource(path);
        return (Map) resourceManager.getObject(resource);
    }

    /**
     * 변수 저장. JPAProcessInstance는 새 파일 경로에 저장·promote 후 variablesPath 갱신.
     * CLOBProcessInstance는 varLob에만 저장(오버라이드).
     */
    protected void saveVariables() throws Exception {
        // 이전 경로 기억 (afterCommit에서 이전 파일 삭제용)
        variablesPathPreviousBeforeTx = getProcessInstanceEntity().getVariablesPath();
        if (variablesPathPreviousBeforeTx != null) {
            variablesPathPreviousBeforeTx = variablesPathPreviousBeforeTx.trim();
            if (variablesPathPreviousBeforeTx.isEmpty()) {
                variablesPathPreviousBeforeTx = null;
            }
        }
        String newPath = generateNewVariablesPath();
        resourceManager.save(new DefaultResource(newPath), getVariables());
        getProcessInstanceEntity().setVariablesPath(newPath);
        variablesPathCreatedThisTx = newPath;
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
                saveVariables();
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

    public Date getStartedDate() {
        if (getProcessInstanceEntity() != null)
            return getProcessInstanceEntity().getStartedDate();
        else
            return null;
    }

    public void setStartedDate(Date value) {
        if (getProcessInstanceEntity() != null)
            getProcessInstanceEntity().setStartedDate(value);
    }

    public Date getFinishedDate() {
        if (getProcessInstanceEntity() != null)
            return getProcessInstanceEntity().getFinishedDate();
        else
            return null;
    }

    public void setFinishedDate(Date value) {
        if (getProcessInstanceEntity() != null)
            getProcessInstanceEntity().setFinishedDate(value);
    }

    public String getInitEp() {
        if (getProcessInstanceEntity() != null)
            return getProcessInstanceEntity().getInitEp();
        else
            return null;
    }

    public void setInitEp(String value) {
        if (getProcessInstanceEntity() != null)
            getProcessInstanceEntity().setInitEp(value);
    }

    @Override
    public String getGroups() {
        if (getProcessInstanceEntity() != null)
            return getProcessInstanceEntity().getGroups();
        else
            return null;
    }

    @Override
    public void setGroups(String value) {
        if (getProcessInstanceEntity() != null)
            getProcessInstanceEntity().setGroups(value);
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

    public boolean sendBroadcast(String eventType, Object payload) throws Exception {
        Streams processor = applicationContext.getBean(Streams.class);
        MessageChannel outputChannel = processor.outboundChannel();

        boolean sent = outputChannel.send(
                MessageBuilder
                        .withPayload(payload)
                        .setHeader(
                                MessageHeaders.CONTENT_TYPE,
                                MimeTypeUtils.APPLICATION_JSON)
                        .setHeader("type", eventType)
                        .build());
        return sent;
    }

    @Override
    public EMailServiceLocal getEmailService() {
        return emailService;
    }
}
