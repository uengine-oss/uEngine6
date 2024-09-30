package org.uengine.five.events;

/**
 * Created by uengine on 2018. 11. 17..
 */
public class ActivityInfo extends BusinessEvent{

    private String instanceId;
    private String tracingTag;

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public void setTracingTag(String tracingTag) {
        this.tracingTag = tracingTag;
    }

    public String getTracingTag() {
        return tracingTag;
    }

}
