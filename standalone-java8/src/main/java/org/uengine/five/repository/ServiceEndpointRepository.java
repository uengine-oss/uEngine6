package org.uengine.five.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
//import org.metaworks.multitenancy.persistence.MultitenantRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.transaction.annotation.Transactional;
import org.uengine.five.entity.ServiceEndpointEntity;

/**
 * Created by uengine on 2017. 12. 21..
 */

@RepositoryRestResource(collectionResourceRel = "service-definitions", path = "service-definitions")
public interface ServiceEndpointRepository extends JpaRepository<ServiceEndpointEntity, String> {

    // @Modifying
    // @Transactional
    // @Query("DELETE FROM CatchEvent e WHERE e.defId = :defId")
    // void deleteEventsByDefId(@Param("defId") String defId);

    @Modifying
    @Transactional
    @Query("DELETE FROM ServiceEndpointEntity s WHERE s.path IN (SELECT s2.path FROM ServiceEndpointEntity s2 JOIN s2.events e WHERE e.defId = :defId)")
    void deleteByDefId(@Param("defId") String defId);

}
