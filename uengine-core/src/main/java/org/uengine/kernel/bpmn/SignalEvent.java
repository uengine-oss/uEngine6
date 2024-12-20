package org.uengine.kernel.bpmn;

import java.util.Map;

import org.uengine.kernel.MessageListener;
import org.uengine.kernel.ProcessInstance;
import org.uengine.kernel.ValidationContext;

public class SignalEvent extends Event implements MessageListener {

	public SignalEvent() {
		super();
		if (this.getName() == null) {
			setName(this.getClass().getSimpleName());
		}
		// setActivityStop(PASS_ACTIVITY);
	}

	public String getMessage() {
		// Todo: Process Designer에서 설정되어야함.
		// 이름이 없는 경우.
		return "event" + getTracingTag();// getClass().getName(); //just simply return the event name as it's classname.
	}

	// @Override
	// public ValidationContext validate(Map options) {
	// // TODO Auto-generated method stub
	// ValidationContext vc = super.validate(options);

	// return vc;
	// }

}
