package org.uengine.five.framework;

import org.uengine.kernel.*;
import org.uengine.processmanager.TransactionContext;
import org.uengine.util.ForLoop;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

/**
 * Created by uengine on 2017. 11. 10..
 */
public class ProcessTransactionContext implements org.uengine.processmanager.ProcessTransactionContext {

    static ThreadLocal<org.uengine.five.framework.ProcessTransactionContext> local = new ThreadLocal<org.uengine.five.framework.ProcessTransactionContext>();
    private boolean commitable;

    protected ProcessTransactionContext() {
        if (local.get() != null && local.get().isCommitable()) {
            throw new RuntimeException("There's uncommitted transactionContext remains.");
        }
        setCommitable(true);
        local.set(this);
    }

    public static org.uengine.five.framework.ProcessTransactionContext getThreadLocalInstance() {
        org.uengine.five.framework.ProcessTransactionContext tc = local.get();
        if (tc == null) {
            // if the local instance is obtained without @ProcessTransactional demarcation
            tc = new org.uengine.five.framework.ProcessTransactionContext();
            tc.setCommitable(false);
        }
        return tc;
    }

    List<TransactionListener> transactionListeners = new ArrayList<TransactionListener>();
    public void addTransactionListener(TransactionListener tl) {
        transactionListeners.add(tl);
    }
    public List<TransactionListener> getTransactionListeners() {
        return transactionListeners;
    }

    transient List<ActivityInstanceContext> executedActivities = new ArrayList<ActivityInstanceContext>();
    public void addExecutedActivityInstanceContext(ActivityInstanceContext aic) {
        if (!executedActivities.contains(aic)) {
            executedActivities.add(aic);
        }
    }
    public List<ActivityInstanceContext> getExecutedActivityInstanceContextsInTransaction() {
        return executedActivities;
    }

    transient Map<String, ProcessInstance> processInstances = new Hashtable<String, ProcessInstance>();
    public void registerProcessInstance(ProcessInstance pi) {
        processInstances.put(pi.getInstanceId(), pi);
    }
    public ProcessInstance getProcessInstanceInTransaction(String instanceId) {
        return processInstances.get(instanceId);
    }

    public Map<String, ProcessInstance> getProcessInstancesInTransaction() {
        return processInstances;
    }

   
    @Override
    public void registerProcessInstance(DefaultProcessInstance defaultProcessInstance) {

    }

    transient Map<String, Object> sharedContexts = new Hashtable<String, Object>();
    public void setSharedContext(String contextKey, Object value) {
        if (value == null) {
            sharedContexts.remove(contextKey);
        } else {
            sharedContexts.put(contextKey, value);
        }
    }
    public Object getSharedContext(String contextKey) {
        if (!sharedContexts.containsKey(contextKey)) {
            return null;
        }
        return sharedContexts.get(contextKey);
    }


    protected void beforeCommit() throws Exception {
        ForLoop beforeCommitLoop = new ForLoop() {
            public void logic(Object target) {
                TransactionListener tl = (TransactionListener) target;
                try {
                    tl.beforeCommit(org.uengine.five.framework.ProcessTransactionContext.this);
                } catch (Exception e) {
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }
            }
        };

        beforeCommitLoop.run((ArrayList)transactionListeners);
    }

    public void commit() throws Exception {
        if (!isCommitable()) {
            throw new Exception("This transaction is uncommitable. If you want to commit this, mark with @ProcessTransactional at the beginning of the service method.");
        }
        beforeCommit();
        remove();
        afterCommit();
    }

    private void afterCommit() throws Exception {

        for(TransactionListener tl : getTransactionListeners()){
            tl.afterCommit(this);
        }

    }

    public void rollback() throws Exception {
        remove();
    }

    @Override
    public void releaseResources() throws Exception {

    }

    @Override
    public void addDebugInfo(String s) {

    }

    public void remove() throws Exception {
        local.set(null);
    }

    public void setCommitable(boolean commitable) {
        this.commitable = commitable;
    }

    public boolean isCommitable() {
        return commitable;
    }


    @Override
    public void addDebugInfo(Object message) {

    }

    @Override
    public StringBuilder getDebugInfo() {
        return null;
    }

    // @Override
    // public ServletRequest getServletRequest() {
    //     return null;
    // }

    // @Override
    // public ServletResponse getServletResponse() {
    //     return null;
    // }

    // @Override
    // public ProcessManagerBean getProcessManager() {
    //     return null;
    // }

    @Override
    public boolean isManagedTransaction() {
        return false;
    }

    @Override
    public ProcessDefinition getProcessDefinition(String pdvid) throws Exception {
        return null;
    }
}