package org.uengine.five.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.uengine.five.entity.ProcDefCommentEntity;

import java.util.List;
import java.util.Optional;

public interface ProcDefCommentRepository extends JpaRepository<ProcDefCommentEntity, String> {

    List<ProcDefCommentEntity> findByProcDefIdOrderByCreatedAtAsc(String procDefId);

    List<ProcDefCommentEntity> findByProcDefIdAndElementIdOrderByCreatedAtAsc(String procDefId, String elementId);

    Optional<ProcDefCommentEntity> findByIdAndProcDefId(String id, String procDefId);

    /** element_id 별 total 개수 (답글 포함) */
    @Query("SELECT c.elementId, COUNT(c) FROM ProcDefCommentEntity c WHERE c.procDefId = :procDefId GROUP BY c.elementId")
    List<Object[]> countByProcDefIdGroupByElementId(@Param("procDefId") String procDefId);

    /** element_id 별 is_resolved = false 개수 */
    @Query("SELECT c.elementId, COUNT(c) FROM ProcDefCommentEntity c WHERE c.procDefId = :procDefId AND c.isResolved = false GROUP BY c.elementId")
    List<Object[]> countUnresolvedByProcDefIdGroupByElementId(@Param("procDefId") String procDefId);

    void deleteByParentCommentId(String parentCommentId);
}
