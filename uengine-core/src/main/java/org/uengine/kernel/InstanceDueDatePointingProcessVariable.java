package org.uengine.kernel;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import org.uengine.kernel.CommandVariableValue;

public class InstanceDueDatePointingProcessVariable extends ProcessVariable{
	private static final long serialVersionUID = org.uengine.kernel.GlobalContext.SERIALIZATION_UID;
	
	public String getName(){
		return "[Instance.DueDate]";
	}

	public Class getType(){
		return (Date.class);
	}
	
}
