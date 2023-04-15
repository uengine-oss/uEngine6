package org.uengine.processmanager;

import org.uengine.kernel.ActivityInstanceContext;
import org.uengine.kernel.DefaultProcessInstance;
import org.uengine.kernel.ProcessDefinition;
import org.uengine.kernel.ProcessInstance;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.sql.Connection;
import java.util.List;
import java.util.Map;

/**
 * Created by uengine on 2018. 11. 16..
 */
public interface ProcessTransactionContext extends TransactionContext {
    void addDebugInfo(Object message);

    StringBuilder getDebugInfo();

    // ServletRequest getServletRequest();

    // ServletResponse getServletResponse();

    boolean isManagedTransaction();

    ProcessDefinition getProcessDefinition(String pdvid) throws Exception;

    List<ActivityInstanceContext> getExecutedActivityInstanceContextsInTransaction();

    void addExecutedActivityInstanceContext(ActivityInstanceContext activityInstanceContext);

    Map<String, ProcessInstance> getProcessInstancesInTransaction();

    void registerProcessInstance(DefaultProcessInstance defaultProcessInstance);

    ProcessInstance getProcessInstanceInTransaction(String instanceId);
}
