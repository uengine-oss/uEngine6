package org.uengine.five.entity;

import javax.persistence.*;

import org.uengine.five.entity.converter.OracleBooleanConverter;

@Entity
@Table(name = "TB_BPM_EVTMAP")
public class EventMappingEntity {

    @PrePersist
    public void ensureRequiredColumns() {
        if (isStartEvent == null) {
            isStartEvent = Boolean.FALSE;
        }
    }

    @Id
    @Column(name = "event_type")
    private String eventType;

    @Column(name = "definition_id")
    private String definitionId;

    @Column(name = "correlation_key")
    private String correlationKey;

    @Column(name = "tracing_tag")
    private String tracingTag;
    
    @Convert(converter = OracleBooleanConverter.class)
    @Column(name = "is_start_event", nullable = false)
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