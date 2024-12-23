package org.uengine.kernel.bpmn;

import org.uengine.kernel.*;
import org.uengine.processdesigner.mapper.TransformerMapping;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SignalIntermediateCatchEvent extends SignalEvent implements IntermediateEvent {

	public static final String SIGNAL_EVENTS = "signalEvents";

	public SignalIntermediateCatchEvent() {
		super();
		setEventType(Event.CATCH_EVENT);
	}

	String signalType;

	public String getSignalType() {
		return signalType;
	}

	public void setSignalType(String signalType) {
		this.signalType = signalType;
	}

	public static Map<String, SignalEventInstance> getSignalEvents(ProcessInstance instance) throws Exception {
		Map<String, SignalEventInstance> signals = (Map<String, SignalEventInstance>) instance.getProperty("",
				SIGNAL_EVENTS);

		if (signals == null)
			signals = new HashMap<>();

		return signals;
	}

	public static void setSignalEvents(ProcessInstance instance, Map<String, SignalEventInstance> signals)
			throws Exception {
		instance/* .getRootProcessInstance() */.setProperty("", SIGNAL_EVENTS, (Serializable) signals);
	}

}
