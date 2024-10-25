package org.uengine.kernel.bpmn;


import java.util.Map;

import org.uengine.kernel.MessageListener;
import org.uengine.kernel.ProcessInstance;
import org.uengine.kernel.ValidationContext;

public class SignalEvent extends Event implements MessageListener {

	@Override
	protected void executeActivity(ProcessInstance instance) throws Exception {
		//start listens...
        if(getAttachedToRef() != null) {

        }
	}

	public boolean onMessage(ProcessInstance instance, Object payload)	throws Exception {
//		Vector activityInstances =  instance.getCurrentRunningActivities();
//		for(int i=0; i<activityInstances.size(); i++){
//			Activity nextAct = (Activity) activityInstances.get(i);
//			nextAct.stop(instance);
//		}
		
		fireComplete(instance);
		
		return true;
	}
	
	public SignalEvent(){
		super();
		if( this.getName() == null ){
			setName(this.getClass().getSimpleName());
		}
//		setActivityStop(PASS_ACTIVITY);
	}

	public String getMessage() {
        // Todo: Process Designer에서 설정되어야함.
        // 이름이 없는 경우.
		return "event" + getTracingTag();//getClass().getName();  //just simply return the event name as it's classname.
	}

    // @Override
    // public ValidationContext validate(Map options) {
    //     // TODO Auto-generated method stub
    //     ValidationContext vc = super.validate(options);
        
    //     return vc;
    // }

}
