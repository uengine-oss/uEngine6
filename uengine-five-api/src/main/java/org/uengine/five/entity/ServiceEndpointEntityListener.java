package org.uengine.five.entity;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PreRemove;
import javax.transaction.Transactional;

public class ServiceEndpointEntityListener {

    @PersistenceContext
    private EntityManager entityManager;

    @PreRemove
    @Transactional
    public void preRemove(ServiceEndpointEntity serviceEndpointEntity) {
        // CatchEvent 엔티티를 삭제합니다.
        serviceEndpointEntity.getEvents().clear();
        entityManager.flush();
    }
}