package org.uengine.five;

import org.springframework.cloud.stream.annotation.Input;
import org.springframework.cloud.stream.annotation.Output;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.SubscribableChannel;

public interface Streams {
    String INPUT = "bpm-in";
    String OUTPUT = "bpm-out";
    String OUTPUT_BRODCAST = "bpm-brodcast";

    @Input(INPUT)
    SubscribableChannel inboundGreetings();

    @Output(OUTPUT)
    MessageChannel outboundChannel();

    @Output(OUTPUT_BRODCAST)
    MessageChannel outboundBrodcastChannel();

}
