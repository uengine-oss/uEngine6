package org.uengine.five.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
//import org.metaworks.multitenancy.persistence.MultitenantRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.uengine.five.entity.WorklistEntity;

/**
 * Created by uengine on 2017. 6. 19..
 */
@RepositoryRestResource(collectionResourceRel = "worklist", path = "worklist")
public interface WorklistRepository extends JpaRepository<WorklistEntity, Long> {

    // @Query("select wl from WorklistEntity wl where (wl.endpoint =
    // ?#{loggedUserId} or wl.endpoint in ?#{loggedUserScopes}) and (wl.status =
    // 'NEW' or wl.status = 'DRAFT')")

    @Query("select wl from WorklistEntity wl where (wl.endpoint = ?#{principal.userId} or wl.endpoint in ?#{principal.scopes})")
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
    // public List<WorklistEntity> findCompleted();

    @Query("select wl from WorklistEntity wl where (wl.instId = :instId and wl.status = 'COMPLETED') order by wl.endDate ")
    public List<WorklistEntity> findWorkListByInstId(@Param(value = "instId") Long instId);
}
