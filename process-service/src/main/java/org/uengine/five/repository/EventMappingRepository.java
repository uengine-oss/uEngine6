package org.uengine.five.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.uengine.five.entity.EventMappingEntity;

@RepositoryRestResource(collectionResourceRel = "event-mappings", path = "event-mappings")
public interface EventMappingRepository extends JpaRepository<EventMappingEntity, String> {

    // @Query("SELECT e.definitionId FROM EventMappingEntity e WHERE e.eventType =
    // :eventType")
    // public String findDefinitionIdByEventType(String eventType);

    @Query("SELECT e FROM EventMappingEntity e WHERE e.eventType = :eventType")
    public EventMappingEntity findEventMappingByEventType(String eventType);
}
