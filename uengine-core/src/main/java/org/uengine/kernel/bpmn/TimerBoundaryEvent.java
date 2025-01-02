package org.uengine.kernel.bpmn;

import java.util.Map;

import org.uengine.kernel.ProcessInstance;
import org.uengine.kernel.ValidationContext;

public class TimerBoundaryEvent extends TimerEvent {

    @Override
    public boolean onMessage(ProcessInstance instance, Object payload) throws Exception {
        unschedule(instance);
        return super.onMessage(instance, payload);
    }

    @Override
    public ValidationContext validate(Map options) {
        ValidationContext vc = new ValidationContext();

        if (getOutgoingSequenceFlows().size() < 1) {
            vc.add("해당 이벤트에서 나가는 시퀀스 플로우가 존재하지 않습니다.");
        }
        return vc;
    }
}
