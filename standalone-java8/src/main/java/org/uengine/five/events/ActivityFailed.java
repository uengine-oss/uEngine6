package org.uengine.five.events;

import org.uengine.kernel.ActivityInstanceContext;

/**
 * Created by uengine on 2018. 11. 16..
 */
public class ActivityFailed extends BusinessEvent{

    public ActivityInfo getActivityInfo() {
        return activityInfo;
    }

    public void setActivityInfo(ActivityInfo activityInfo) {
        this.activityInfo = activityInfo;
    }

    ActivityInfo activityInfo;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    String message;
}
