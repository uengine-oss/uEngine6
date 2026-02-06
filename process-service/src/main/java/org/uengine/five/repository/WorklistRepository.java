package org.uengine.five.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    /**
     * ToDo 정의
     * - 기본: endpoint 가 나(principal.userId) 이거나, endpoint 가 내 scope(roles) 중 하나인 workitem
     * - 추가: dispatchOption = 1(경합/RACING) 인 경우 roleName 이 내 scope(roles)와 일치해도 노출
     * - 상태: COMPLETED / CANCELLED 는 제외
     */
//     @Query("select wl from WorklistEntity wl where (wl.endpoint = ?#{principal.userId} or wl.endpoint in ?#{principal.scopes}) and (wl.status != 'COMPLETED') ")
    @Query("select wl from WorklistEntity wl " +
            "where (" +
            "   (wl.endpoint = ?#{principal.userId} or wl.endpoint in ?#{principal.scopes})" +
            "   or (wl.dispatchOption = 1 and wl.endpoint is null and wl.scope in ?#{principal.groups})" +
            "   or (wl.dispatchOption = 1 and wl.endpoint is null and wl.scope in ?#{principal.scopes})" +
            ") and (wl.status != 'COMPLETED') ")
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

    /**
     * 경합(DispatchOption=1) 등으로 endpoint가 비어있는 workitem들을,
     * 특정 사용자가 claim 했을 때 동일 role/scope/assignType 그룹으로 함께 소유자 세팅하기 위해 사용.
     *
     * rootInstId는 일부 레코드에서 null일 수 있어 instId로 fallback 합니다.
     */
    @Query("select wl from WorklistEntity wl " +
            "where ( (wl.rootInstId = :rootInstId) or (wl.rootInstId is null and wl.instId = :rootInstId) ) " +
            "  and wl.roleName = :roleName " +
            "  and wl.scope = :scope " +
            "  and wl.assignType = :assignType " +
            "  and (wl.status = 'NEW' or wl.status = 'RUNNING') " +
            "  and ( (:endpoint is null and wl.endpoint is null) or (:endpoint is not null and wl.endpoint = :endpoint) ) ")
    public List<WorklistEntity> findSiblingsForClaimState(@Param("rootInstId") Long rootInstId,
            @Param("roleName") String roleName,
            @Param("scope") String scope,
            @Param("assignType") Integer assignType,
            @Param("endpoint") String endpoint);
    

    // // // TEST
    // @Query("select wl from WorklistEntity wl")
    // public List<WorklistEntity> findAll();

}
