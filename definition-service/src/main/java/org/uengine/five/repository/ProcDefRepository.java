package org.uengine.five.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.uengine.five.entity.ProcDefEntity;

import java.util.List;

public interface ProcDefRepository extends JpaRepository<ProcDefEntity, String> {

    List<ProcDefEntity> findByPathStartingWith(String prefix);
}
