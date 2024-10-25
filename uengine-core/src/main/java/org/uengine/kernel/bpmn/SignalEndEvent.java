package org.uengine.kernel.bpmn;

import org.uengine.kernel.Activity;
import org.uengine.kernel.FaultContext;
import org.uengine.kernel.ProcessInstance;
import org.uengine.kernel.UEngineException;
import org.uengine.kernel.ValidationContext;

import java.util.Hashtable;
import java.util.Map;

/**
 * @author Jinyoung Jang
 */

public class SignalEndEvent extends EndEvent {

	public SignalEndEvent() {
		setName("ErrorEnd");
	}

	public void executeActivity(ProcessInstance instance) throws Exception {

	}

}