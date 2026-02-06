package org.uengine.kernel.bpmn;

import org.uengine.kernel.Activity;
import org.uengine.kernel.ActivityEventInterceptor;
import org.uengine.kernel.ProcessInstance;

/**
 * Created by uengine on 2018. 1. 15..
 */
public class CatchingErrorEvent extends Event{

    @Override
    protected void executeActivity(final ProcessInstance instance) throws Exception {
        if(getAttachedToRef()!=null){
            Activity listeningActivity = instance.getProcessDefinition().getActivity(getAttachedToRef());
            CatchingErrorEventActivityEventInterceptor catchingErrorEventActivityEventInterceptor = new CatchingErrorEventActivityEventInterceptor();
            catchingErrorEventActivityEventInterceptor.setTracingTag(getTracingTag());
            instance.addActivityEventInterceptor(catchingErrorEventActivityEventInterceptor);
        }
    }

    @Override
    public boolean onMessage(ProcessInstance instance, Object payload) throws Exception {
        if(instance.getProcessDefinition().getActivity(getTracingTag()).getStatus(instance).equals(STATUS_RUNNING)) {
            // Interrupting: 연결된 액티비티의 토큰을 소진해 getCurrentRunningActivities()에서 제외되고,
            // 게이트웨이/조인 등 후속 플로우가 정상 진행되도록 한다.
            if (getAttachedToRef() != null) {
                Activity attached = instance.getProcessDefinition().getActivity(getAttachedToRef());
                if (attached != null) {
                    attached.setTokenCount(instance, 0);
                }
            }
            fireComplete(instance); // run the connected activity
            //let error is not fired.
            instance.getProcessTransactionContext().setSharedContext("faultTolerant", new Boolean(true));
        }
        return true;
            

    }
}
