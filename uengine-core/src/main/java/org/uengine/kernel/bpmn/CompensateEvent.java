package org.uengine.kernel.bpmn;

import org.uengine.kernel.Activity;
import org.uengine.kernel.ProcessInstance;

public class CompensateEvent extends Event {

    public CompensateEvent() {
        setName("Compensate");
    }

    @Override
    protected void executeActivity(final ProcessInstance instance) throws Exception {
        if(getAttachedToRef() != null){
            System.out.println("PREPIX_MESSAGE");
        }
    }

    @Override
    public boolean onMessage(ProcessInstance instance, Object payload) throws Exception {
        fireComplete(instance); // run the connected activity

        // let error is not fired.
        // instance.getProcessTransactionContext().setSharedContext("faultTolerant", new Boolean(true));

        return true;
    }
}
