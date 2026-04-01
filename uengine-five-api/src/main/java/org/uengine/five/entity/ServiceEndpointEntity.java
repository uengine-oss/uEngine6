package org.uengine.five.entity;

import java.util.List;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.CollectionTable;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.PrePersist;
import javax.persistence.Table;

import org.springframework.transaction.annotation.Transactional;

/**
 * Created by uengine on 2018. 1. 5..
 */
@Entity
@Table(name = "TB_BPM_SERVICE")
@EntityListeners(ServiceEndpointEntityListener.class)
public class ServiceEndpointEntity {

    @Id
    String path;

    @ElementCollection
    @CollectionTable(name = "TB_BPM_SVC_EVT", joinColumns = @JoinColumn(name = "service_path"))
    @AttributeOverrides({
            @AttributeOverride(name = "messageClass", column = @javax.persistence.Column(name = "message_class")),
            @AttributeOverride(name = "correlationKey", column = @javax.persistence.Column(name = "correlation_key")),
            @AttributeOverride(name = "defId", column = @javax.persistence.Column(name = "def_id"))
    })
    private List<CatchEvent> events;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }


    @PrePersist
    public void trimPaths() {
        if (getPath().startsWith("/")) {
            setPath(getPath().substring(1));
        }
    }

    public List<CatchEvent> getEvents() {
        return events;
    }

    public void setEvents(List<CatchEvent> events) {
        this.events = events;
    }

}
