package org.uengine.five.repository;

import java.util.Date;

import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.uengine.five.entity.ProcessInstanceEntity;
import org.uengine.five.framework.ProcessTransactional;

/**
 * Oracle용 ProcessInstance Repository.
 * findFilterICanSee는 JPQL + UDF REGEXP_LIKE_YN 사용. Pageable Sort가 엔티티 필드(startedDate)로
 * 매핑되어 ORA-00904(STARTEDDATE) 방지.
 */
@Profile("oracle")
@ProcessTransactional
@RepositoryRestResource(collectionResourceRel = "instances", path = "instances")
public interface ProcessInstanceRepositoryOracle extends ProcessInstanceRepository {

        @Override
        @Query(value = "SELECT DISTINCT pi FROM ProcessInstanceEntity pi " +
                        "WHERE 1=1 " +
                        "  AND (:instId IS NULL OR pi.instId = :instId) " +
                        "  AND (:defId IS NULL OR pi.defId LIKE CONCAT(CONCAT('%', :defId), '%')) " +
                        "  AND (:status IS NULL OR pi.status = :status) " +
                        "  AND (:eventHandler IS NULL OR pi.eventHandler = :eventHandler) " +
                        "  AND (:name IS NULL OR pi.name LIKE CONCAT(CONCAT('%', :name), '%')) " +
                        "  AND (:startedDate IS NULL OR pi.startedDate >= :startedDate) " +
                        "  AND (:finishedDate IS NULL OR pi.finishedDate <= :finishedDate) " +
                        "  AND (:subProcess IS NULL OR pi.subProcess = :subProcess) " +
                        "  AND (:initEp IS NULL OR pi.initEp LIKE CONCAT(CONCAT('%', :initEp), '%')) " +
                        "  AND (:currEp IS NULL OR pi.currEp LIKE CONCAT(CONCAT('%', :currEp), '%')) " +
                        "  AND (:prevCurrEp IS NULL OR pi.prevCurrEp LIKE CONCAT(CONCAT('%', :prevCurrEp), '%')) " +
                        "  AND ( " +
                        "        (:rolePattern IS NULL AND :namePattern IS NULL) " +
                        "        OR (:rolePattern IS NOT NULL AND function('REGEXP_LIKE_YN', pi.currEp, :rolePattern) = 1) " +
                        "        OR (:namePattern IS NOT NULL AND function('REGEXP_LIKE_YN', pi.currEp, :namePattern) = 1) " +
                        "      ) " +
                        "ORDER BY pi.startedDate DESC",
                        countQuery = "SELECT COUNT(DISTINCT pi) FROM ProcessInstanceEntity pi " +
                        "WHERE 1=1 " +
                        "  AND (:instId IS NULL OR pi.instId = :instId) " +
                        "  AND (:defId IS NULL OR pi.defId LIKE CONCAT(CONCAT('%', :defId), '%')) " +
                        "  AND (:status IS NULL OR pi.status = :status) " +
                        "  AND (:eventHandler IS NULL OR pi.eventHandler = :eventHandler) " +
                        "  AND (:name IS NULL OR pi.name LIKE CONCAT(CONCAT('%', :name), '%')) " +
                        "  AND (:startedDate IS NULL OR pi.startedDate >= :startedDate) " +
                        "  AND (:finishedDate IS NULL OR pi.finishedDate <= :finishedDate) " +
                        "  AND (:subProcess IS NULL OR pi.subProcess = :subProcess) " +
                        "  AND (:initEp IS NULL OR pi.initEp LIKE CONCAT(CONCAT('%', :initEp), '%')) " +
                        "  AND (:currEp IS NULL OR pi.currEp LIKE CONCAT(CONCAT('%', :currEp), '%')) " +
                        "  AND (:prevCurrEp IS NULL OR pi.prevCurrEp LIKE CONCAT(CONCAT('%', :prevCurrEp), '%')) " +
                        "  AND ( " +
                        "        (:rolePattern IS NULL AND :namePattern IS NULL) " +
                        "        OR (:rolePattern IS NOT NULL AND function('REGEXP_LIKE_YN', pi.currEp, :rolePattern) = 1) " +
                        "        OR (:namePattern IS NOT NULL AND function('REGEXP_LIKE_YN', pi.currEp, :namePattern) = 1) " +
                        "      )")
        Page<ProcessInstanceEntity> findFilterICanSee(
                        @Param("defId") String defId,
                        @Param("instId") Long instId,
                        @Param("status") String status,
                        @Param("eventHandler") String eventHandler,
                        @Param("name") String name,
                        @Param("startedDate") Date startedDate,
                        @Param("finishedDate") Date finishedDate,
                        @Param("initEp") String initEp,
                        @Param("prevCurrEp") String prevCurrEp,
                        @Param("currEp") String currEp,
                        @Param("subProcess") Boolean subProcess,
                        @Param("rolePattern") String rolePattern,
                        @Param("namePattern") String namePattern,
                        Pageable pageable);

        @Override
        @Query(value = "SELECT pi FROM ProcessInstanceEntity pi " +
                        "WHERE function('REGEXP_LIKE_YN', pi.groups, :pattern) = 1 " +
                        "AND (:status IS NULL OR pi.status = :status) " +
                        "ORDER BY pi.startedDate DESC",
                        countQuery = "SELECT COUNT(pi) FROM ProcessInstanceEntity pi " +
                                        "WHERE function('REGEXP_LIKE_YN', pi.groups, :pattern) = 1 " +
                                        "AND (:status IS NULL OR pi.status = :status)")
        Page<ProcessInstanceEntity> findAllByGroupsRegex(@Param("pattern") String pattern,
                        @Param("status") String status, Pageable pageable);

        @Override
        @Query("select pi from ProcessInstanceEntity pi where (:name is null or pi.name like CONCAT(CONCAT('%',:name),'%')) and (:status is null or pi.status = :status) and (:startedDate is null or pi.startedDate >= :startedDate) and (:finishedDate is null or :finishedDate >= pi.finishedDate ) and (:subProcess is null or :subProcess = pi.subProcess ) order by pi.startedDate desc")
        Page<ProcessInstanceEntity> findByName(@Param("name") String name, @Param("status") String status,
                        @Param("startedDate") String startedDate, @Param("finishedDate") String finishedDate,
                        @Param("subProcess") String subProcess, Pageable pageable);
}
