package org.uengine.kernel.bpmn;

/**
 * Created by Ryuha on 2015-06-11.
 */
public class EscalationEvent extends Event {

    String expression = "";

    public void setExpression(String expression) {
        this.expression = expression;
    }

    public String getExpression() {
        return expression;
    }

    public EscalationEvent() {
        setName("Escalation");
    }

}
