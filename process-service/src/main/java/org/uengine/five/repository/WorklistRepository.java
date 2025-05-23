package org.uengine.five.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
//import org.metaworks.multitenancy.persistence.MultitenantRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.uengine.five.entity.ProcessInstanceEntity;
import org.uengine.five.entity.WorklistEntity;

/**
 * Created by uengine on 2017. 6. 19..
 */
@RepositoryRestResource(collectionResourceRel = "worklist", path = "worklist")
public interface WorklistRepository extends JpaRepository<WorklistEntity, Long> {

    // @Query("select wl from WorklistEntity wl where (wl.endpoint =
    // ?#{loggedUserId} or wl.endpoint in ?#{loggedUserScopes}) and (wl.status =
    // 'NEW' or wl.status = 'DRAFT')")

    @Query("select wl from WorklistEntity wl where (wl.endpoint = ?#{principal.userId} or wl.endpoint in ?#{principal.scopes}) and (wl.status != 'COMPLETED') ")
    public List<WorklistEntity> findToDo();

    // @Query("select wl from WorklistEntity wl where (wl.endpoint =
    // ?#{principal.userId} or wl.endpoint in ?#{principal.scopes}) and (wl.status =
    // 'IN_PROGRESS')")
    // public List<WorklistEntity> findInProgress();

    // @Query("select wl from WorklistEntity wl where (wl.endpoint =
    // ?#{principal.userId} or wl.endpoint in ?#{principal.scopes}) and (wl.status =
    // 'PENDING')")
    // public List<WorklistEntity> findPending();

    // @Query("select wl from WorklistEntity wl where (wl.endpoint = ?#{principal.userId} or wl.endpoint in ?#{principal.scopes}) and (wl.status = 'COMPLETED')")
    // public List<WorklistEntity> findCompleted(Pageable pageable);

    @Query("select pi from WorklistEntity pi " +
            "where (pi.endpoint = ?#{principal.userId} or pi.endpoint in ?#{principal.scopes}) " +
            "and (pi.status = 'COMPLETED')" )
    Page<WorklistEntity> findCompleted(Pageable pageable);

    @Query("select wl from WorklistEntity wl where (wl.rootInstId = :rootInstId and wl.status = 'COMPLETED') order by wl.endDate ")
    public List<WorklistEntity> findWorkListByInstId(@Param(value = "rootInstId") Number rootInstId);

    @Query("select wl from WorklistEntity wl where (wl.rootInstId = :rootInstId and (wl.status = 'NEW' or wl.status = 'RUNNING')) order by wl.endDate ")
    public List<WorklistEntity> findCurrentWorkItemByInstId(@Param(value = "rootInstId") Number rootInstId);

    @Query("select wl from WorklistEntity wl where (wl.rootInstId = :rootInstId and wl.status = :status)")
    public List<WorklistEntity> findWorkListByInstIdAndStatus(@Param(value = "rootInstId") long rootInstId, @Param(value = "status") String status);
    

    // // // TEST
    // @Query("select wl from WorklistEntity wl")
    // public List<WorklistEntity> findAll();

}
