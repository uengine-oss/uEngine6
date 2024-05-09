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
}