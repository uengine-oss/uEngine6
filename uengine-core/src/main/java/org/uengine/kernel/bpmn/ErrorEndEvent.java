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

public class ErrorEndEvent extends StartEvent {
	private static final long serialVersionUID = org.uengine.kernel.GlobalContext.SERIALIZATION_UID;

	boolean stopAllTokens;

	public boolean isStopAllTokens() {
		return stopAllTokens;
	}

	public void setStopAllTokens(boolean stopAllTokens) {
		this.stopAllTokens = stopAllTokens;
	}

	public ErrorEndEvent() {
		// TODO: 확인해야함
		// super("terminate");
		setName("ErrorEnd");
	}

	public void executeActivity(ProcessInstance instance) throws Exception {

		// TODO Auto-generated method stub
		if (instance != null && instance.isSubProcess()) {
			instance.getProcessDefinition().returnToMainProcess(instance);
		}

		instance.setInfo(getName());
        UEngineException exception = new UEngineException();
        fireFault(instance, exception);
	}

	@Override
	public ValidationContext validate(Map options) {
		ValidationContext vc = new ValidationContext();
		if (getIncomingSequenceFlows().size() < 1) {
			vc.add("해당 이벤트에 들어오는 시퀀스 플로우가 존재하지 않습니다.");
		}

		return vc;
	}

}