package org.uengine.five.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.uengine.five.entity.ProcDefVersionEntity;

import java.util.List;
import java.util.Optional;

public interface ProcDefVersionRepository extends JpaRepository<ProcDefVersionEntity, String> {

    List<ProcDefVersionEntity> findByProcDefId(String procDefId);

    Optional<ProcDefVersionEntity> findByProcDefIdAndVersion(String procDefId, String version);
}
