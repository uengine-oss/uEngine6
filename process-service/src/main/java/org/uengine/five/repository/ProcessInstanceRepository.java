package org.uengine.five.repository;

//import org.metaworks.multitenancy.persistence.MultitenantRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.format.annotation.DateTimeFormat;
import java.util.Date;
import org.uengine.five.entity.ProcessInstanceEntity;
import org.uengine.five.framework.ProcessTransactional;

import java.util.List;

/**
 * Created by uengine on 2017. 6. 19..
 */
@ProcessTransactional
@RepositoryRestResource(collectionResourceRel = "instances", path = "instances")
public interface ProcessInstanceRepository extends JpaRepository<ProcessInstanceEntity, Long> {

        @Query("select pi from ProcessInstanceEntity pi where exists (select 1 from WorklistEntity wl where wl.endpoint = ?#{loggedUserId})")
        List<ProcessInstanceEntity> findAllICanSee();

        @Query("select pi from ProcessInstanceEntity pi where pi.mainInstId is null")
        List<ProcessInstanceEntity> findMainInstICanSee();

        @Query("select pi from ProcessInstanceEntity pi " +
                        "where 1=1 " +
                        "and (:instId is null or pi.instId = :instId )" +
                        "and (:defId is null or pi.defId like CONCAT('%',:defId,'%')) " +
                        "and (:status is null or pi.status = :status )" +
                        "and (:eventHandler is null or pi.eventHandler = :eventHandler )" +
                        "and (:name is null or pi.name like CONCAT('%',:name,'%') )" +
                        "and (:startedDate is null or pi.startedDate >= :startedDate)" +
                        "and (:finishedDate is null or :finishedDate >= pi.finishedDate )" +
                        "and (:subProcess is null or :subProcess = pi.subProcess )" +
                        "and (:initEp is null or pi.initEp like CONCAT('%',:initEp,'%'))" +
                        "and (:currEp is null or pi.currEp like CONCAT('%',:currEp,'%'))" +
                        "and (:prevCurrEp is null or pi.prevCurrEp like CONCAT('%',:prevCurrEp,'%'))" +
                        "and ((:rolePattern is null or (regexp_like(pi.currEp, :rolePattern) = true))" +
                        "or (:namePattern is null or (regexp_like(pi.currEp, :namePattern) = true)))" +
                        "group by pi.instId, pi.startedDate " + // Added GROUP BY clause
                        "order by pi.startedDate desc")
        Page<ProcessInstanceEntity> findFilterICanSee(
                        @Param("defId") String defId,
                        @Param("instId") Long instId,
                        @Param("status") String status,
                        @Param("eventHandler") String eventHandler,
                        @Param("name") String name,
                        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @Param("startedDate") Date startedDate,
                        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @Param("finishedDate") Date finishedDate,
                        @Param("initEp") String initEp,
                        @Param("prevCurrEp") String prevCurrEp,
                        @Param("currEp") String currEp,
                        @Param("subProcess") Boolean subProcess,
                        @Param("rolePattern") String rolePattern,
                        @Param("namePattern") String namePattern,
                        Pageable pageable
        // Temporal 이 먹지않거나 시작일 검색이 제대로 기능하지않는다면 (String to Date Type error)시 아래 주석해제후
        // 이내용으로 하시면됩니다.
        // @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)@Param("startedDate") Date
        // startedDate,
        // @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)@Param("finishedDate") Date
        // finishedDate
        );

        @Query("select pi from ProcessInstanceEntity pi where pi.rootInstId = :instId")
        List<ProcessInstanceEntity> findChild(@Param("instId") Long instId);

        @Query("select pi from ProcessInstanceEntity pi where (pi.corrKey = :corrKey and pi.status = :status)")
        List<ProcessInstanceEntity> findByCorrKeyAndStatus(@Param("corrKey") String corrKey,
                        @Param("status") String status);

        @Query("select pi from ProcessInstanceEntity pi where pi.status = :status")
        List<ProcessInstanceEntity> findByStatus(@Param("status") String status);

        @Query("select pi from ProcessInstanceEntity pi order by pi.startedDate desc")
        Page<ProcessInstanceEntity> findAll(Pageable pageable);

        @Query("SELECT pi FROM ProcessInstanceEntity pi " +
                        "WHERE regexp_like(pi.groups, :pattern) = true " +
                        "AND (:status is null or pi.status = :status) " +
                        "ORDER BY pi.startedDate DESC")
        Page<ProcessInstanceEntity> findAllByGroupsRegex(@Param("pattern") String pattern,
                        @Param("status") String status, Pageable pageable);

        @Query("select pi from ProcessInstanceEntity pi where (:name is null or pi.name like CONCAT('%',:name,'%')) and (:status is null or pi.status = :status) and (:startedDate is null or pi.startedDate >= :startedDate) and (:finishedDate is null or :finishedDate >= pi.finishedDate ) and (:subProcess is null or :subProcess = pi.subProcess ) order by pi.startedDate desc")
        Page<ProcessInstanceEntity> findByName(@Param("name") String name, @Param("status") String status,
                        @Param("startedDate") String startedDate, @Param("finishedDate") String finishedDate,
                        @Param("subProcess") String subProcess, Pageable pageable);
}