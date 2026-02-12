package org.uengine.five.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.uengine.five.entity.AuditLogEntity;

import java.util.List;

/**
 * 감사 로그 저장소. bpm_audit_log는 append-only 사용을 전제로 하며,
 * 애플리케이션에서는 save(삽입)와 조회 메서드만 사용하고 delete/update는 호출하지 않는다.
 */
public interface AuditLogEntityRepository extends JpaRepository<AuditLogEntity, Long> {

    List<AuditLogEntity> findByRootInstIdOrderByOccurredAtDesc(Long rootInstId, Pageable pageable);
}
