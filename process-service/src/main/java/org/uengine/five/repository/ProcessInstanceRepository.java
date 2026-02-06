package org.uengine.five.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;
import java.util.List;

import org.uengine.five.entity.ProcessInstanceEntity;

/**
 * 프로세스 인스턴스 Repository 공통 정의.
 * 구현체는 프로필별로 ProcessInstanceRepositoryH2 / ProcessInstanceRepositoryOracle 사용.
 */
@org.springframework.data.repository.NoRepositoryBean
public interface ProcessInstanceRepository extends JpaRepository<ProcessInstanceEntity, Long> {

    @Query("select pi from ProcessInstanceEntity pi where exists (select 1 from WorklistEntity wl where wl.endpoint = ?#{loggedUserId})")
    List<ProcessInstanceEntity> findAllICanSee();

    @Query("select pi from ProcessInstanceEntity pi where pi.mainInstId is null")
    List<ProcessInstanceEntity> findMainInstICanSee();

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

    @Query("select pi from ProcessInstanceEntity pi where pi.rootInstId = :instId")
    List<ProcessInstanceEntity> findChild(@Param("instId") Long instId);

    @Query("select pi from ProcessInstanceEntity pi where (pi.corrKey = :corrKey and pi.status = :status)")
    List<ProcessInstanceEntity> findByCorrKeyAndStatus(@Param("corrKey") String corrKey,
            @Param("status") String status);

    @Query("select pi from ProcessInstanceEntity pi where pi.status = :status")
    List<ProcessInstanceEntity> findByStatus(@Param("status") String status);

    @Query("select pi from ProcessInstanceEntity pi order by pi.startedDate desc")
    Page<ProcessInstanceEntity> findAll(Pageable pageable);

    Page<ProcessInstanceEntity> findAllByGroupsRegex(@Param("pattern") String pattern,
            @Param("status") String status, Pageable pageable);

    Page<ProcessInstanceEntity> findByName(@Param("name") String name, @Param("status") String status,
            @Param("startedDate") String startedDate, @Param("finishedDate") String finishedDate,
            @Param("subProcess") String subProcess, Pageable pageable);
}
