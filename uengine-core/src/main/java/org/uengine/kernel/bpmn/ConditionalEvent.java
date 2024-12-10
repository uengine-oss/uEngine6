package org.uengine.kernel.bpmn;

import org.uengine.kernel.Condition;
import org.uengine.kernel.ProcessInstance;

/**
 * Created by uengine on 2018. 3. 1..
 */
public class ConditionalEvent extends Event {

    public ConditionalEvent() {
        setName("Conditional");
    }

    Condition condition;

    public Condition getCondition() {
        return condition;
    }

    public void setCondition(Condition condition) {
        this.condition = condition;
    }

    @Override
    public boolean onMessage(ProcessInstance instance, Object payload) throws Exception {
        if (getCondition().isMet(instance, "")) {
            return super.onMessage(instance, payload);
        } else {
            return false;
        }
    }
}
