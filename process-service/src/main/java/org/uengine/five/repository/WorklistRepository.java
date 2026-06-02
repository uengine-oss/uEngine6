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
     * ToDo ?뺤쓽
     * - 湲곕낯: endpoint 媛 ??principal.userId) ?닿굅?? endpoint 媛 ??scope(roles) 以??섎굹??workitem
     * - 異붽?: dispatchOption = 1(寃쏀빀/RACING) ??寃쎌슦 roleName ????scope(roles)? ?쇱튂?대룄 ?몄텧
     * - ?곹깭: COMPLETED / CANCELLED ???쒖쇅
     */
//     @Query("select wl from WorklistEntity wl where (wl.endpoint = ?#{principal.userId} or wl.endpoint in ?#{principal.scopes}) and (wl.status != 'COMPLETED') ")
    @Query("select wl from WorklistEntity wl " +
            "where (" +
            "   (wl.endpoint = ?#{principal.userId} or wl.endpoint in ?#{principal.scopes})" +
            "   or (wl.endpoint is null and wl.scope in ?#{principal.groups})" +
            "   or (wl.endpoint is null and wl.scope in ?#{principal.scopes})" +
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
     * 寃쏀빀(DispatchOption=1) ?깆쑝濡?endpoint媛 鍮꾩뼱?덈뒗 workitem?ㅼ쓣,
     * ?뱀젙 ?ъ슜?먭? claim ?덉쓣 ???숈씪 role/scope/assignType 洹몃９?쇰줈 ?④퍡 ?뚯쑀???명똿?섍린 ?꾪빐 ?ъ슜.
     *
     * rootInstId???쇰? ?덉퐫?쒖뿉??null?????덉뼱 instId濡?fallback ?⑸땲??
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

