package org.uengine.five.overriding;

import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.util.MimeTypeUtils;
import org.uengine.five.events.ActivityInfo;
import org.uengine.five.events.ActivityQueued;
import org.uengine.five.StandaloneApplication;
import org.uengine.five.Streams;
import org.uengine.kernel.IActivityEventQueue;

/**
 * Created by uengine on 2018. 11. 16..
 */
public class ActivityQueue implements IActivityEventQueue {


    @Override
    public void queue(String instanceId, String tracingTag, int retryingCount, String[] additionalParameters) {
        Streams streams = StandaloneApplication.getApplicationContext().getBean(Streams.class);

        ActivityQueued activityQueued = new ActivityQueued();
        activityQueued.setActivityInfo(new ActivityInfo());
        activityQueued.getActivityInfo().setInstanceId(instanceId);
        activityQueued.getActivityInfo().setTracingTag(tracingTag);


        MessageChannel messageChannel = streams.outboundChannel();
        messageChannel.send(MessageBuilder
                .withPayload(activityQueued)
                .setHeader(MessageHeaders.CONTENT_TYPE, MimeTypeUtils.APPLICATION_JSON)
                .build());

    }
}
