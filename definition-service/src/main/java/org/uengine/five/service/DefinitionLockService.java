package org.uengine.five.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.uengine.five.entity.DefinitionLockEntity;
import org.uengine.five.repository.DefinitionLockRepository;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.Optional;

/**
 * 동시 수정 방지용 Lock 서비스.
 * 리소스 id + user_id만 관리(테넌트 구분 없음).
 */
@Service
public class DefinitionLockService {

    @Autowired
    private DefinitionLockRepository repository;

    /**
     * JWT 또는 X-User-Id에서 현재 사용자 식별. ProcDefDataService와 동일 패턴.
     */
    public static String getCurrentUserId(HttpServletRequest request) {
        return ProcDefDataService.getCurrentUserId(request);
    }

    @Transactional(readOnly = true)
    public Optional<DefinitionLockDto> getLock(String resourceId) {
        return repository.findById(resourceId).map(this::toDto);
    }

    /**
     * Lock 생성 또는 본인 lock 갱신(연장). 다른 사용자가 잡고 있으면 409.
     */
    @Transactional
    public DefinitionLockDto putLock(String resourceId, String userId) {
        Optional<DefinitionLockEntity> existing = repository.findById(resourceId);
        if (existing.isEmpty()) {
            DefinitionLockEntity e = new DefinitionLockEntity();
            e.setId(resourceId);
            e.setUserId(userId);
            e.setUpdatedAt(new Date());
            e = repository.save(e);
            return toDto(e);
        }
        DefinitionLockEntity e = existing.get();
        if (!e.getUserId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Resource is locked by another user: " + e.getUserId());
        }
        e.setUpdatedAt(new Date());
        e = repository.save(e);
        return toDto(e);
    }

    /**
     * Lock 해제(체크인). 해당 id만 맞으면 삭제.
     */
    @Transactional
    public void deleteLock(String resourceId) {
        repository.deleteById(resourceId);
    }

    private DefinitionLockDto toDto(DefinitionLockEntity e) {
        DefinitionLockDto dto = new DefinitionLockDto();
        dto.setId(e.getId());
        dto.setUserId(e.getUserId());
        return dto;
    }
}
