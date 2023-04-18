package org.uengine.five.rpa;

import org.uengine.kernel.ProcessInstance;
import org.uengine.kernel.ProcessVariable;
import org.uengine.kernel.ReceiveActivity;


public class RPAActivity extends ReceiveActivity{

    @Override
    protected void executeActivity(ProcessInstance instance) throws Exception {
        // TODO Auto-generated method stub
        super.executeActivity(instance);

        Object argValue = getArgument().get(instance, null);

        getArgument().set(instance, null, argValue + "_");
    }

    
    ProcessVariable argument;
        public ProcessVariable getArgument() {
            return argument;
        }
        public void setArgument(ProcessVariable argument) {
            this.argument = argument;
        }
        

    
    
}
