package org.uengine.kernel.bpmn;

import java.util.HashMap;

import org.uengine.kernel.*;

public class SignalIntermediateThrowEvent extends SignalEvent implements IntermediateEvent {

	public static final String SIGNAL_EVENTS = "signalEvents";

	public SignalIntermediateThrowEvent() {
		super();
		setEventType(Event.THROW_EVENT);
	}

	String signalType;

	public String getSignalType() {
		return signalType;
	}

	public void setSignalType(String signalType) {
		this.signalType = signalType;
	}

	@Override
	protected void executeActivity(ProcessInstance instance) throws Exception {
		instance.sendBroadcast(getEventType(), new HashMap<>());
		super.executeActivity(instance);
		fireComplete(instance);
	}
}
