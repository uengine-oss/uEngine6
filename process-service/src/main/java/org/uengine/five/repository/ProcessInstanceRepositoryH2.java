package org.uengine.five.repository;

import java.util.Date;

import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.uengine.five.entity.ProcessInstanceEntity;
import org.uengine.five.framework.ProcessTransactional;

/**
 * H2용 ProcessInstance Repository.
 * CONCAT 3인자, regexp_like 사용 (H2 dialect 지원).
 */
@Profile("!oracle")
@ProcessTransactional
@RepositoryRestResource(collectionResourceRel = "instances", path = "instances")
public interface ProcessInstanceRepositoryH2 extends ProcessInstanceRepository {

    @Override
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
            "group by pi.instId, pi.startedDate " +
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
            Pageable pageable);

    @Override
    @Query("SELECT pi FROM ProcessInstanceEntity pi " +
            "WHERE regexp_like(pi.groups, :pattern) = true " +
            "AND (:status is null or pi.status = :status) " +
            "ORDER BY pi.startedDate DESC")
    Page<ProcessInstanceEntity> findAllByGroupsRegex(@Param("pattern") String pattern,
            @Param("status") String status, Pageable pageable);

    @Override
    @Query("select pi from ProcessInstanceEntity pi where (:name is null or pi.name like CONCAT('%',:name,'%')) and (:status is null or pi.status = :status) and (:startedDate is null or pi.startedDate >= :startedDate) and (:finishedDate is null or :finishedDate >= pi.finishedDate ) and (:subProcess is null or :subProcess = pi.subProcess ) order by pi.startedDate desc")
    Page<ProcessInstanceEntity> findByName(@Param("name") String name, @Param("status") String status,
            @Param("startedDate") String startedDate, @Param("finishedDate") String finishedDate,
            @Param("subProcess") String subProcess, Pageable pageable);
}
