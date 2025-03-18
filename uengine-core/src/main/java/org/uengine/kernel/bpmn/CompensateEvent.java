package org.uengine.kernel.bpmn;

import org.uengine.kernel.ProcessInstance;

public class CompensateEvent extends Event {

    public CompensateTask compensateTask;

    public CompensateTask getCompensateTask() {
        return compensateTask;
    }

    public void setCompensateTask(CompensateTask compensateTask) {
        this.compensateTask = compensateTask;
    }

    public CompensateEvent() {
        setName("Compensate");
    }

    @Override
    protected void executeActivity(final ProcessInstance instance) throws Exception {
        if (getAttachedToRef() != null) {
            // System.out.println("PREPIX_MESSAGE");
        }
    }

    @Override
    public boolean onMessage(ProcessInstance instance, Object payload) throws Exception {
        fireComplete(instance); // run the connected activity

        // let error is not fired.
        // instance.getProcessTransactionContext().setSharedContext("faultTolerant", new
        // Boolean(true));

        return true;
    }

    public static class CompensateTask {
        String name;
        String tracingTag;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getTracingTag() {
            return tracingTag;
        }

        public void setTracingTag(String tracingTag) {
            this.tracingTag = tracingTag;
        }
    }
}
