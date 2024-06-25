package org.uengine.kernel.bpmn;

import java.util.Map;

import org.uengine.kernel.DefaultActivity;
import org.uengine.kernel.ProcessInstance;
import org.uengine.kernel.ValidationContext;
import org.uengine.modeling.ElementView;

public class StartEvent extends Event {
	public StartEvent() {
		setName("Start");
	}

	@Override
	protected void executeActivity(ProcessInstance instance) throws Exception {
		fireComplete(instance);
	}

	String startTriggerCode;

	public String getStartTriggerCode() {
		return startTriggerCode;
	}

	public void setStartTriggerCode(String startTriggerCode) {
		this.startTriggerCode = startTriggerCode;
	}

	@Override
	public ValidationContext validate(Map options) {
		ValidationContext vc = new ValidationContext();

		if (getOutgoingSequenceFlows().size() < 1) {
			vc.getErrorMessage().add("해당 이벤트에서 나가는 시퀀스 플로우가 존재하지 않습니다.");
		}
		return vc;
	}

}
