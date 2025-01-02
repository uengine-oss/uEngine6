package org.uengine.kernel.bpmn;

import java.util.Map;

import org.uengine.kernel.ValidationContext;

/**
 * Created by uengine on 2018. 3. 1..
 */
public class ConditionalBoundaryEvent extends ConditionalEvent {

    @Override
    public ValidationContext validate(Map options) {
        ValidationContext vc = new ValidationContext();

        if (getOutgoingSequenceFlows().size() < 1) {
            vc.add("해당 이벤트에서 나가는 시퀀스 플로우가 존재하지 않습니다.");
        }
        return vc;
    }
}
