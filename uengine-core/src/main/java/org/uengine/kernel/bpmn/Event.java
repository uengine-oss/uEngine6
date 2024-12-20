package org.uengine.kernel.bpmn;

import java.util.Map;

import org.uengine.kernel.Activity;
import org.uengine.kernel.ComplexActivity;
import org.uengine.kernel.DefaultActivity;
import org.uengine.kernel.MessageListener;
import org.uengine.kernel.ProcessInstance;
import org.uengine.kernel.ReceiveActivity;
import org.uengine.kernel.Validatable;
import org.uengine.kernel.ValidationContext;
import org.uengine.modeling.ElementView;

// TODO: 매핑 관련 개선
// extends DefaultActivity => extends ReceiveActivity
public class Event extends ReceiveActivity implements MessageListener, Validatable {

    public static final String STOP_ACTIVITY = "STOP_ACTIVITY";
    public static final String PASS_ACTIVITY = "PASS_ACTIVITY";

    public static final String START_EVENT = "StartEvent";
    public static final String END_EVENT = "EndEvent";
    public static final String CATCH_EVENT = "CatchEvent";
    public static final String THROW_EVENT = "ThrowEvent";
    public static final String BOUNDARY_EVENT = "BoundaryEvent";

    // String activityStop;
    // @Face(displayName="호출한 상위 엑티비티 종료",
    // ejsPath="dwr/metaworks/genericfaces/RadioButton.ejs",
    // options={"stop Activity where called this event", "no"},
    // values={STOP_ACTIVITY, PASS_ACTIVITY})
    // public String getActivityStop() {
    // return activityStop;
    // }
    // public void setActivityStop(String activityStop) {
    // this.activityStop = activityStop;
    // }
    //

    String attachedToRef;

    public String getAttachedToRef() {
        return attachedToRef;
    }

    public void setAttachedToRef(String attachedToRef) {
        if (attachedToRef != null) {
            setEventType("Catching");
        }

        this.attachedToRef = attachedToRef;
    }

    boolean cancelActivity;

    public boolean isCancelActivity() {
        return cancelActivity;
    }

    public void setCancelActivity(boolean cancelActivity) {
        this.cancelActivity = cancelActivity;
    }

    @Override
    protected void executeActivity(ProcessInstance instance) throws Exception {
        // TODO Auto-generated method stub

        // System.out.print("event started");
    }

    ///// slightly different from the OMG's BPMN 2.0 xml schema model. but more
    ///// simple.

    String eventType;

    // @Range(options={"Catching","Throwing", "Non-Interrupting"},
    // values={"Catching", "Throwing", "Non-Interrupting"})
    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    String eventKey;

    public String getEventKey() {
        return eventKey;
    }

    public void setEventKey(String eventKey) {
        this.eventKey = eventKey;
    }

    boolean intermediate;

    // @Hidden
    public boolean isIntermediate() {
        return intermediate;
    }

    public void setIntermediate(boolean intermediate) {
        this.intermediate = intermediate;
    }

    @Override
    public boolean onMessage(ProcessInstance instance, Object payload) throws Exception {

        // Boundary Event 전용 -> 각 Boundary Event 별로 넣는 성질의 코드가 아니기 때문에 Event 클래스에 넣음
        if (getAttachedToRef() != null) {// 중단 비중단 처리
            if (isCancelActivity()) {
                Activity activity = getProcessDefinition().getActivity(getAttachedToRef());
                activity.stop(instance);
                if (getParentActivity() instanceof ComplexActivity) {
                    ComplexActivity complexActivity = (ComplexActivity) getParentActivity();
                    for (Activity child : complexActivity.getChildActivities()) {
                        if (child instanceof Event && ((Event) child).getAttachedToRef() != null) {
                            if (!child.getTracingTag().equals(activity.getTracingTag())
                                    && ((Event) child).getAttachedToRef().contains(activity.getTracingTag())) {
                                child.stop(instance);
                            }
                        }
                    }
                }
            }
        }

        if (payload instanceof String) {
            String message = (String) payload;

            if (getTracingTag().equals(message)) {

                fireComplete(instance); // let event starts

                return true;

            }
        }

        return false;
    }

    @Override
    public String getMessage() {
        return "event";
    }

    @Override
    public void beforeRegistered(ProcessInstance instance) throws Exception {
        System.out.println("beforeRegistered");
    }

    @Override
    public void afterRegistered(ProcessInstance instance) throws Exception {
        System.out.println("afterRegistered");
    }

    @Override
    public void afterUnregistered(ProcessInstance instance) throws Exception {
    }

    @Override
    public ValidationContext validate(Map options) {
        ValidationContext vc = new ValidationContext();

        if (getAttachedToRef() != null) {
            if (getOutgoingSequenceFlows().size() < 1) {
                vc.add("해당 이벤트에서 호출하는 태스크가 존재하지 않습니다.");
            }
        } else {
            if (getOutgoingSequenceFlows().size() < 1) {
                vc.add("해당 이벤트에서 나가는 시퀀스 플로우가 존재하지 않습니다.");
            }
            if (getIncomingSequenceFlows().size() < 1) {
                vc.add("해당 이벤트에 들어오는 시퀀스 플로우가 존재하지 않습니다.");
            }
        }

        return vc;
    }

}
