package org.uengine.five.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
//import org.metaworks.multitenancy.persistence.MultitenantRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.uengine.five.entity.ProcessDefinitionEntity;

/**
 * Created by uengine on 2017. 6. 19..
 */
@RepositoryRestResource(collectionResourceRel = "definitions", path = "definitions")
public interface DefinitionEntityRepository extends JpaRepository<ProcessDefinitionEntity, Long> {
    @Query("select pd from ProcessDefinitionEntity pd " +
            "where (:name is null or pd.name like CONCAT('%', :name, '%')) " +
            "and (:path is null or pd.path like CONCAT('%', :path, '%'))")
    List<ProcessDefinitionEntity> findByNameOrPath(
            @Param("name") String name,
            @Param("path") String path);
}
