package org.uengine.five.overriding;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.uengine.five.framework.ProcessTransactionContext;
import org.uengine.kernel.Activity;
import org.uengine.kernel.ActivityFilter;
import org.uengine.kernel.DefaultProcessInstance;
import org.uengine.kernel.ProcessDefinition;
import org.uengine.kernel.ProcessInstance;
import org.uengine.kernel.ReceiveActivity;
import org.uengine.modeling.resource.DefaultResource;
import org.uengine.modeling.resource.IResource;
import org.uengine.modeling.resource.ResourceManager;

public class PayloadFilter implements ActivityFilter, Serializable {
    private static final long serialVersionUID = org.uengine.kernel.GlobalContext.SERIALIZATION_UID;

    @Autowired
    ResourceManager resourceManager;

    @Override
    public void beforeExecute(Activity activity, ProcessInstance instance) throws Exception {

    }

    @Override
    public void afterExecute(Activity activity, ProcessInstance instance) throws Exception {

    }

    @Override
    public void afterComplete(Activity activity, ProcessInstance instance) throws Exception {
        if (activity instanceof ReceiveActivity) {

            ProcessTransactionContext tc = ProcessTransactionContext.getThreadLocalInstance();
            String payloadKey = activity.getTracingTag() + "_payload@" + instance.getInstanceId() + ":";
            String payload = (String) tc
                    .getSharedContext(payloadKey);
            if (tc != null && payload != null) {
                System.out.println("Activity" + activity.getTracingTag() + "_payload: " + payload);

                if (instance instanceof JPAProcessInstance) {
                    Date date = ((JPAProcessInstance) instance).getProcessInstanceEntity().getStartedDate();
                    String currentYear = String.valueOf(date.getYear() + 1900);
                    String currentMonth = String.format("%02d", date.getMonth() + 1);
                    IResource resource = new DefaultResource(
                            "payloads/" + currentYear + "/" + currentMonth + "/" + instance.getInstanceId());

                    boolean resourceExists = resourceManager.exists(resource);
                    Map<String, String> payloadMap = null;
                    if (resourceExists) {
                        payloadMap = (Map) resourceManager.getObject(resource);
                    } else {
                        payloadMap = new HashMap<String, String>();
                    }

                    payloadMap.put(payloadKey, payload);
                    resourceManager.save(resource, payloadMap);
                }
            }
        }
    }

    @Override
    public void onPropertyChange(Activity activity, ProcessInstance instance, String propertyName, Object changedValue)
            throws Exception {
    }

    @Override
    public void onDeploy(ProcessDefinition definition) throws Exception {
    }
}
