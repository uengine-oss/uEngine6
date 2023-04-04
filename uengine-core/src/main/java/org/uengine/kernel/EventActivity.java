package org.uengine.kernel;

/**
 * Created by soo on 2015. 7. 8..
 */
public class EventActivity extends Activity{

    public static final String STOP_ACTIVITY = "STOP_ACTIVITY";
    public static final String PASS_ACTIVITY = "PASS_ACTIVITY";

    String activityStop;
    public String getActivityStop() {
        return activityStop;
    }
    public void setActivityStop(String activityStop) {
        this.activityStop = activityStop;
    }

    @Override
    protected void executeActivity(ProcessInstance instance) throws Exception {
        // TODO Auto-generated method stub
    }

}

