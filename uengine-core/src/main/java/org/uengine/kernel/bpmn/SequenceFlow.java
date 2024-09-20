package org.uengine.kernel.bpmn;

import org.uengine.kernel.*;
import org.uengine.modeling.RelationView;
import org.uengine.util.UEngineUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class SequenceFlow extends AbstractFlow
		implements Validatable, java.io.Serializable, NeedArrangementToSerialize {
	private static final long serialVersionUID = org.uengine.kernel.GlobalContext.SERIALIZATION_UID;

	private boolean feedback;

	public void setFeedback(boolean feedback) {
		this.feedback = feedback;
	}

	public boolean isFeedback() {
        // Serialization
        // return feedback;
        if(getSourceActivity() == null) {
            return false;
        }
        int sourceDepth = getSourceActivity().getProcessDefinition()
                .getDepthFromStartEvent(getSourceActivity());
        int targetDepth = getSourceActivity().getProcessDefinition()
                .getDepthFromStartEvent(getTargetActivity());

        return sourceDepth > targetDepth;
	}

	int priority;

	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}

	Condition condition;

	public Condition getCondition() {
		return condition;
	}

	public void setCondition(Condition condition) {
		this.condition = condition;
	}

	boolean otherwise;

	public boolean isOtherwise() {
		return otherwise;
	}

	public void setOtherwise(boolean otherwise) {
		this.otherwise = otherwise;
	}

	public Activity getSourceActivity() {
		return (Activity) this.getSourceElement();
	}

	public void setSourceActivity(Activity activity) {
		this.setSourceElement(activity);
	}

	public Activity getTargetActivity() {
		return (Activity) this.getTargetElement();
	}

	public void setTargetActivity(Activity activity) {
		this.setTargetElement(activity);
	}

	public SequenceFlow() {
	}

	public SequenceFlow(String source, String target) {
		setSourceRef(source);
		setTargetRef(target);
	}

	@Override
	public RelationView createView() {

		RelationView relationView = (RelationView) UEngineUtil.getComponentByEscalation(getClass(), "view");

		if (relationView == null)
			throw new RuntimeException("can't find View component for " + getClass());

		relationView.setRelation(this);
		relationView.setLabel(getName());

		// relationView.setLabel(get);

		return relationView;
	}

	public boolean isMet(ProcessInstance instance, String scope) throws Exception {
		if (condition == null) {
			return true;
		}
		return condition.isMet(instance, scope);
	}

	// public boolean isMatch() throws Exception {
	//
	// if (getSourceElement() instanceof ExclusiveGateway) {
	// // condition needed
	// } else if (getSourceElement() instanceof InclusiveGateway) {
	// // condition needed
	// } else if (getSourceElement() instanceof LoopGateway) {
	// // assume to get the outgoing transition from sourceActivity
	// if (((LoopGateway) getSourceElement()).getMap().get("outgoing").equals(this))
	// {
	// return true;
	// }
	// } else {
	// return true;
	// }
	//
	// return false;
	// }

	public List<Activity> fillActivityToTransition(List<Activity> activityList) {
		for (Activity activity : activityList) {
			activity.getOutgoingSequenceFlows().clear();
			activity.getIncomingSequenceFlows().clear();
			if (activity.getTracingTag().equals(this.getSourceRef())) {
				List<SequenceFlow> list = activity.getOutgoingSequenceFlows();
				boolean addFalg = true;
				for (SequenceFlow ts : list) {
					if (ts.getSourceRef().equals(this.getSourceRef())
							&& ts.getTargetRef().equals(this.getTargetRef())) {
						addFalg = false;
					}
				}
				if (addFalg)
					list.add(this);
			} else if (activity.getTracingTag().equals(this.getTargetRef())) {
				List<SequenceFlow> list = activity.getIncomingSequenceFlows();
				boolean addFalg = true;
				for (SequenceFlow ts : list) {
					if (ts.getSourceRef().equals(this.getSourceRef())
							&& ts.getTargetRef().equals(this.getTargetRef())) {
						addFalg = false;
					}
				}
				if (addFalg)
					list.add(this);
			}

			if (activity instanceof ScopeActivity) {
				ArrayList<SequenceFlow> tsList = ((ScopeActivity) activity).getSequenceFlows();
				if (tsList != null && ((ScopeActivity) activity).getChildActivities() != null) {
					for (SequenceFlow ts : tsList) {
						ts.fillActivityToTransition(((ScopeActivity) activity).getChildActivities());
					}
				}
			}
		}
		return activityList;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof SequenceFlow) {
			SequenceFlow comparee = (SequenceFlow) obj;

			try {
				if (comparee.getTargetRef().equals(getTargetRef()) && comparee.getSourceRef().equals(getSourceRef()))
					return true;
			} catch (Exception e) {
				return false;
			}
		}

		return false;
	}

	@Override
	public void beforeSerialization() {
		if (isOtherwise()) {
			setCondition(new Otherwise());
		}
	}

	@Override
	public void afterDeserialization() {

	}

	public ValidationContext validate(Map options) {
		ValidationContext vc = new ValidationContext();

		Condition condition = getCondition();
		if (condition != null) {
			vc = condition.validate(new HashMap<>());
		}

		return vc;
	}

}
