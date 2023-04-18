package org.uengine.five.rpa;

import org.uengine.kernel.ProcessInstance;
import org.uengine.kernel.ProcessVariable;
import org.uengine.kernel.ReceiveActivity;


public class RPAActivity extends ReceiveActivity{

    @Override
    protected void executeActivity(ProcessInstance instance) throws Exception {
        // TODO Auto-generated method stub
        super.executeActivity(instance);

        //TODO: python 명령을 보내는 부분 -> kafka event publish 

        Object argValue = getArgument().get(instance, "");

        getArgument().set(instance, "", argValue + "_");
    }

    

    
    @Override
    protected void onReceive(ProcessInstance instance, Object payload) throws Exception {
        // TODO Auto-generated method stub
        super.onReceive(instance, payload);

        //....
    }




    ProcessVariable argument;
        public ProcessVariable getArgument() {
            return argument;
        }
        public void setArgument(ProcessVariable argument) {
            this.argument = argument;
        }
        

    
    
}
