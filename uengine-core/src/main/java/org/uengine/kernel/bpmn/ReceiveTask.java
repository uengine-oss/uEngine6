package org.uengine.kernel.bpmn;

import org.uengine.kernel.ReceiveActivity;

public class ReceiveTask extends ReceiveActivity {

    public ReceiveTask() {
        super();
        setMessage(getName());
    }

}
