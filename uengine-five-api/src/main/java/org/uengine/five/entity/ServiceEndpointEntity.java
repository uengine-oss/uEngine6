package org.uengine.five.entity;

import java.util.List;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.EntityManager;
import javax.persistence.Id;
import javax.persistence.PersistenceContext;
import javax.persistence.PrePersist;
import javax.persistence.PreRemove;
import javax.persistence.Table;

import org.springframework.transaction.annotation.Transactional;

/**
 * Created by uengine on 2018. 1. 5..
 */
@Entity
@Table(name = "BPM_SERVICE")
@EntityListeners(ServiceEndpointEntityListener.class)
public class ServiceEndpointEntity {

    @Id
    String path;

    @ElementCollection
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
