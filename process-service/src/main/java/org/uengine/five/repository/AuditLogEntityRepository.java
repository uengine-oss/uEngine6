package org.uengine.five.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.uengine.five.entity.AuditLogEntity;

import java.util.List;

/**
 * 감사 로그 저장소. Oracle 표준 스키마에서는 TB_BPM_AUDIT_LOG를 사용하며,
 * 애플리케이션에서는 save(삽입)와 조회 메서드만 사용하고 delete/update는 호출하지 않는다.
 */
public interface AuditLogEntityRepository extends JpaRepository<AuditLogEntity, Long> {

    List<AuditLogEntity> findByRootInstIdOrderByOccurredAtDesc(Long rootInstId, Pageable pageable);
}
