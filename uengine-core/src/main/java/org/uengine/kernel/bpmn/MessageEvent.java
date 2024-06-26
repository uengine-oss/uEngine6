package org.uengine.kernel.bpmn;

import org.uengine.kernel.GlobalContext;
import org.uengine.kernel.ProcessInstance;
import org.uengine.kernel.Role;

/**
 * Created by Ryuha on 2015-06-11.
 */
public class MessageEvent extends Event {

    public MessageEvent(){
        setName("Send Message");
    }

    public String msg;

    public String getMsg() {
        return msg;
    }
    public void setMsg(String msg) {
        this.msg = msg;
    }


    public Role toRole;
    //@Face(displayName="수신자", faceClassName="org.uengine.kernel.face.RoleSelectorFace")
        public Role getToRole() {
            return toRole;
        }

        public void setToRole(Role toRole) {
            this.toRole = toRole;
        }


    @Override
    protected void executeActivity(ProcessInstance instance) throws Exception {

        NotificationService notificationService = GlobalContext.getComponent(NotificationService.class);

        notificationService.notificate(getToRole().getMapping(instance).getEndpoint(), evaluateContent(instance, getMsg()).toString(), instance.getInstanceId());

        super.executeActivity(instance);
    }
}

