package org.uengine.five.overriding;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.uengine.five.entity.ProcessInstanceEntity;
import org.uengine.five.framework.ProcessTransactionContext;
import org.uengine.five.repository.ProcessInstanceRepository;
import org.uengine.five.service.DefinitionServiceUtil;
import org.uengine.five.service.InstanceServiceImpl;
import org.uengine.kernel.AbstractProcessInstance;
import org.uengine.kernel.Activity;
import org.uengine.kernel.DefaultProcessInstance;
import org.uengine.kernel.ProcessDefinition;
import org.uengine.kernel.ProcessInstance;
import org.uengine.kernel.RoleMapping;
import org.uengine.kernel.TransactionListener;
import org.uengine.kernel.UEngineException;
import org.uengine.modeling.resource.DefaultResource;
import org.uengine.modeling.resource.IResource;
import org.uengine.modeling.resource.ResourceManager;
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

            if (options.containsKey(AbstractProcessInstance.INIT_OPTION_INSTANCE_NAME)) {
                getProcessInstanceEntity()
                        .setName((String) options.get(AbstractProcessInstance.INIT_OPTION_INSTANCE_NAME));

            }

            getProcessInstanceEntity().setName(getProcessDefinition().getName() + instanceId);
            getProcessInstanceEntity().setDefId(procDefinition.getId());

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
            setProcessDefinition((ProcessDefinition) definitionService.getDefinition(processInstanceEntity.getDefId()));
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

    }

    @Override
    public void afterRollback(TransactionContext tx) throws Exception {

    }

    protected Map loadVariables() throws Exception {
        IResource resource = new DefaultResource("instances/" + getInstanceId());
        return (Map) resourceManager.getObject(resource);
    }

    protected void saveVariables() throws Exception {
        IResource resource = new DefaultResource("instances/" + getInstanceId());
        resourceManager.save(resource, getVariables());
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
}
