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
}
