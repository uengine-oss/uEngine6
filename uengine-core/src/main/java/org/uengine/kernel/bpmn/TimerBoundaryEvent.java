package org.uengine.kernel.bpmn;

import org.uengine.kernel.ProcessInstance;

public class TimerBoundaryEvent extends TimerEvent implements BoundaryEvent {

    @Override
    public boolean onMessage(ProcessInstance instance, Object payload) throws Exception {
        unschedule(instance);
        return super.onMessage(instance, payload);
    }
}
