package org.uengine.five.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.uengine.five.entity.DefinitionLockEntity;

public interface DefinitionLockRepository extends JpaRepository<DefinitionLockEntity, String> {
}
