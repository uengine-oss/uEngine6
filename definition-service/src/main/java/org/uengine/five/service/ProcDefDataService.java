package org.uengine.five.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.uengine.contexts.UserContext;
import org.uengine.five.entity.ProcDefCommentEntity;
import org.uengine.five.repository.ProcDefCommentRepository;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 프로세스 정의 관련 데이터 서비스.
 * 댓글, 승인 상태, 버전 등 정의에 붙는 저장 데이터를 다룬다.
 */
@Service
public class ProcDefDataService {

    @Autowired
    private ProcDefCommentRepository repository;

    public static String getCurrentUserId(HttpServletRequest request) {
        String userId = null;
        try {
            userId = UserContext.getThreadLocalInstance().getUserId();
        } catch (Exception ignored) {
        }
        if (userId == null && request != null) {
            String auth = request.getHeader("Authorization");
            if (auth != null && auth.startsWith("Bearer ")) {
                try {
                    String token = auth.substring(7);
                    userId = com.auth0.jwt.JWT.decode(token).getClaim("email").asString();
                } catch (Exception ignored) {
                }
            }
        }
        if (userId == null) {
            userId = request != null ? request.getHeader("X-User-Id") : null;
        }
        return userId != null ? userId : "anonymous";
    }

    @Transactional(readOnly = true)
    public List<ElementCommentDto> listComments(String procDefId, String elementId) {
        List<ProcDefCommentEntity> list = elementId != null && !elementId.isEmpty()
                ? repository.findByProcDefIdAndElementIdOrderByCreatedAtAsc(procDefId, elementId)
                : repository.findByProcDefIdOrderByCreatedAtAsc(procDefId);
        return list.stream().map(ProcDefDataService::commentToDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Map<String, Map<String, Integer>> getElementCommentCounts(String procDefId) {
        List<Object[]> totals = repository.countByProcDefIdGroupByElementId(procDefId);
        List<Object[]> unresolved = repository.countUnresolvedByProcDefIdGroupByElementId(procDefId);
        Map<String, Integer> totalMap = totals.stream()
                .collect(Collectors.toMap(row -> (String) row[0], row -> ((Number) row[1]).intValue()));
        Map<String, Integer> unresolvedMap = unresolved.stream()
                .collect(Collectors.toMap(row -> (String) row[0], row -> ((Number) row[1]).intValue()));
        Set<String> elementIds = new HashSet<>(totalMap.keySet());
        elementIds.addAll(unresolvedMap.keySet());
        Map<String, Map<String, Integer>> result = new LinkedHashMap<>();
        for (String eid : elementIds) {
            Map<String, Integer> counts = new LinkedHashMap<>();
            counts.put("total", totalMap.getOrDefault(eid, 0));
            counts.put("unresolved", unresolvedMap.getOrDefault(eid, 0));
            result.put(eid, counts);
        }
        return result;
    }

    @Transactional
    public ElementCommentDto createComment(ElementCommentCreateRequest request, HttpServletRequest httpRequest) {
        ProcDefCommentEntity e = new ProcDefCommentEntity();
        e.setProcDefId(request.getProcDefId());
        e.setElementId(request.getElementId());
        e.setElementType(request.getElementType());
        e.setElementName(request.getElementName());
        e.setContent(request.getContent());
        e.setParentCommentId(request.getParentCommentId());
        String authorId = getCurrentUserId(httpRequest);
        e.setAuthorId(authorId);
        e.setAuthorName(authorId);
        e.setIsResolved(false);
        e.setTenantId(null);
        e.prePersist();
        repository.save(e);
        return commentToDto(e);
    }

    @Transactional
    public Optional<ElementCommentDto> updateCommentContent(String commentId, ElementCommentPatchRequest request) {
        return repository.findById(commentId)
                .map(entity -> {
                    entity.setContent(request.getContent());
                    entity.preUpdate();
                    repository.save(entity);
                    return commentToDto(entity);
                });
    }

    @Transactional
    public boolean deleteComment(String commentId) {
        Optional<ProcDefCommentEntity> opt = repository.findById(commentId);
        if (!opt.isPresent()) {
            return false;
        }
        repository.deleteByParentCommentId(commentId);
        repository.delete(opt.get());
        return true;
    }

    @Transactional
    public Optional<ElementCommentDto> resolveComment(String commentId, ElementCommentResolveRequest request, HttpServletRequest httpRequest) {
        return repository.findById(commentId)
                .map(entity -> {
                    Boolean resolved = request.getResolved();
                    entity.setIsResolved(Boolean.TRUE.equals(resolved));
                    if (Boolean.TRUE.equals(resolved)) {
                        entity.setResolvedBy(getCurrentUserId(httpRequest));
                        entity.setResolvedAt(new Date());
                        entity.setResolveActionText(request.getResolveActionText());
                    } else {
                        entity.setResolvedBy(null);
                        entity.setResolvedAt(null);
                        entity.setResolveActionText(null);
                    }
                    entity.preUpdate();
                    return repository.save(entity);
                })
                .map(ProcDefDataService::commentToDto);
    }

    public Optional<ElementCommentDto> getComment(String commentId) {
        return repository.findById(commentId).map(ProcDefDataService::commentToDto);
    }

    private static ElementCommentDto commentToDto(ProcDefCommentEntity e) {
        ElementCommentDto dto = new ElementCommentDto();
        dto.setId(e.getId());
        dto.setProcDefId(e.getProcDefId());
        dto.setElementId(e.getElementId());
        dto.setElementType(e.getElementType());
        dto.setElementName(e.getElementName());
        dto.setAuthorId(e.getAuthorId());
        dto.setAuthorName(e.getAuthorName());
        dto.setContent(e.getContent());
        dto.setParentCommentId(e.getParentCommentId());
        dto.setIsResolved(e.getIsResolved());
        dto.setResolvedBy(e.getResolvedBy());
        dto.setResolvedAt(e.getResolvedAt());
        dto.setResolveActionText(e.getResolveActionText());
        dto.setTenantId(e.getTenantId());
        dto.setCreatedAt(e.getCreatedAt());
        dto.setUpdatedAt(e.getUpdatedAt());
        return dto;
    }
}
