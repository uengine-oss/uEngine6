package org.uengine.five.repository;

import org.springframework.data.jpa.repository.JpaRepository;
//import org.metaworks.multitenancy.persistence.MultitenantRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.uengine.five.entity.ServiceEndpointEntity;

/**
 * Created by uengine on 2017. 12. 21..
 */

@RepositoryRestResource(collectionResourceRel = "service-definitions", path = "service-definitions")
public interface ServiceEndpointRepository extends JpaRepository<ServiceEndpointEntity, String> {
}
