package org.uengine.kernel.bpmn;

import org.uengine.kernel.Activity;
import org.uengine.kernel.ProcessInstance;

/**
 * Created by uengine on 2018. 3. 1..
 */
public class CompensateIntermediateThrowEvent extends CompensateEvent {

    @Override
    protected void executeActivity(final ProcessInstance instance) throws Exception {
        if (getCompensateTask() != null) {
            Activity activity = instance.getProcessDefinition()
                    .getActivity(getCompensateTask().getTracingTag());
            if (activity != null) {
                activity.backToHere(instance);
            } else {
                throw new Exception(
                        "Compensate task activity not found: " + getCompensateTask().getTracingTag());
            }
            fireComplete(instance);
        } else {
            throw new Exception("Compensate task is not set.");
        }
    }
}
