package org.uengine.five.events;

/**
 * Created by uengine on 2018. 11. 16..
 */
public class ActivityQueued extends BusinessEvent{

    public ActivityInfo getActivityInfo() {
        return activityInfo;
    }

    public void setActivityInfo(ActivityInfo activityInfo) {
        this.activityInfo = activityInfo;
    }

    ActivityInfo activityInfo;
}
