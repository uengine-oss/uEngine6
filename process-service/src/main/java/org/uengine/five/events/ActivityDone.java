package org.uengine.five.events;

import java.io.Serializable;

/**
 * Created by uengine on 2018. 11. 16..
 */
public class ActivityDone extends BusinessEvent {

    public ActivityInfo getActivityInfo() {
        return activityInfo;
    }

    public void setActivityInfo(ActivityInfo activityInfo) {
        this.activityInfo = activityInfo;
    }

    ActivityInfo activityInfo;

    Serializable result;

    public Serializable getResult() {
        return result;
    }

    public void setResult(Serializable result) {
        this.result = result;
    }
}
