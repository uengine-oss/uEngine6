package org.uengine.five.overriding;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import org.springframework.util.MimeTypeUtils;
import org.uengine.five.Streams;
import org.uengine.kernel.Activity;
import org.uengine.kernel.HumanActivity;
import org.uengine.kernel.IActivityCompletionListener;
import org.uengine.kernel.ProcessInstance;

/**
 * Publishes task progress events to bpm-brodcast after commit, whenever a
 * HumanActivity completes.
 */
@Component
public class ActivityCompletionBrodcastListener implements IActivityCompletionListener {

    @Autowired
    Streams streams;

    @Override
    public void onActivityCompleted(ProcessInstance instance, Activity activity) throws Exception {
        if (!(activity instanceof HumanActivity))
            return;

        HumanActivity humanActivity = (HumanActivity) activity;

        Map<String, Object> taskEvent = new HashMap<>();
        taskEvent.put("eventType", "TASK_COMPLETED");
        taskEvent.put("instanceId", instance != null ? instance.getInstanceId() : null);
        taskEvent.put("tracingTag", activity.getTracingTag());
        taskEvent.put("activityName", activity.getName());
        try {
            taskEvent.put("taskIds", humanActivity.getTaskIds(instance));
        } catch (Exception ignored) {
            // optional
        }

        MessageChannel messageChannel = streams.outboundBrodcastChannel();
        messageChannel.send(MessageBuilder
                .withPayload(taskEvent)
                .setHeader(MessageHeaders.CONTENT_TYPE, MimeTypeUtils.APPLICATION_JSON)
                .setHeader("type", "TASK_COMPLETED")
                .build());
    }
}
