package org.uengine.five.entity;

import javax.persistence.*;

@Entity
@Table(name = "BPM_EVENT_MAPPING")
public class EventMappingEntity {
    
    @Id
    @Column(name = "event_type")
    private String eventType;

    @Column(name = "definition_id")
    private String definitionId;

    @Column(name = "correlation_key")
    private String correlationKey;

    @Column(name = "tracing_tag")
    private String tracingTag;
    
    @Column(name = "is_start_event")
    private Boolean isStartEvent;

   
    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getDefinitionId() {
        return definitionId;
    }

    public void setDefinitionId(String definitionId) {
        this.definitionId = definitionId;
    }

    public String getTracingTag() {
        return tracingTag;
    }

    public void setTracingTag(String tracingTag) {
        this.tracingTag = tracingTag;
    }

    public String getCorrelationKey() {
        return correlationKey;
    }

    public void setCorrelationKey(String correlationKey) {
        this.correlationKey = correlationKey;
    }

    public Boolean isStartEvent() {
        return isStartEvent;
    }

    public void setIsStartEvent(Boolean isStartEvent) {
        this.isStartEvent = isStartEvent;
    }

}