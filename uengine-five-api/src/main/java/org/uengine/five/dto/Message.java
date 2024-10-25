package org.uengine.five.dto;

import java.io.Serializable;

public class Message implements Serializable {
    private String event;
    private Serializable payload;
    private String tracingTag;

    public Message() {
    }

    public Message(String event, Serializable payload) {
        this.event = event;
        this.payload = payload;
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public Serializable getPayload() {
        return payload;
    }

    public void setPayload(Serializable payload) {
        this.payload = payload;
    }

    public String getTracingTag() {
        return tracingTag;
    }

    public void setTracingTag(String tracingTag) {
        this.tracingTag = tracingTag;
    }

    
}
