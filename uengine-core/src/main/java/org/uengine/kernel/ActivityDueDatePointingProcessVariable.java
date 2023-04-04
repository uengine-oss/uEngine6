package org.uengine.kernel;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;

public class ActivityDueDatePointingProcessVariable extends ProcessVariable{
	private static final long serialVersionUID = org.uengine.kernel.GlobalContext.SERIALIZATION_UID;
	
	public ActivityDueDatePointingProcessVariable(final HumanActivity finalActivity){

		setActivity(finalActivity);
		setTracingTag(finalActivity.getTracingTag());
	}
	
	HumanActivity activity;
		public HumanActivity getActivity() {
			return activity;
		}
		public void setActivity(HumanActivity activity) {
			this.activity = activity;
		}
		
	String tracingTag;
		public String getTracingTag() {
			return tracingTag;
		}
		public void setTracingTag(String tracingTag) {
			this.tracingTag = tracingTag;
		}
		
	public String getName(){
		return "[" + getActivity().getName() + ".DueDate]";
	}

	public Class getType(){
		return (Date.class);
	}
	
}
