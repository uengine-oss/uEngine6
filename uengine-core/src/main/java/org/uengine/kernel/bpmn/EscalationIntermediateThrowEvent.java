package org.uengine.kernel.bpmn;

import org.uengine.kernel.Activity;
import org.uengine.kernel.ProcessInstance;
import org.uengine.kernel.ScopeActivity;

/**
 * Created by uengine on 2018. 3. 18..
 */
public class EscalationIntermediateThrowEvent extends EscalationEvent {

    @Override
    public void executeActivity(ProcessInstance instance) throws Exception {

        for (Activity childActivity : getProcessDefinition().getChildActivities()) {
            onMessageBoundaryEvents(childActivity, instance);
        }

        super.executeActivity(instance);
    }

    private void onMessageBoundaryEvents(Activity activity, ProcessInstance instance) throws Exception {
        if (activity instanceof EscalationBoundaryEvent) {
            EscalationBoundaryEvent event = (EscalationBoundaryEvent) activity;
            if (this.getParentActivity().getTracingTag().equals(event.getAttachedToRef())) {
                if (getExpression().equals(event.getExpression())) {
                    event.onMessage(instance, event.getTracingTag());
                }

            }
        } else if (activity instanceof ScopeActivity) {
            ScopeActivity scopeActivity = (ScopeActivity) activity;
            for (Activity childActivity : scopeActivity.getChildActivities()) {
                onMessageBoundaryEvents(childActivity, instance);
            }
        }
    }

}
