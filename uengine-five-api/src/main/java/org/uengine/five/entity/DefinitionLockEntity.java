package org.uengine.five.entity;

import javax.persistence.*;
import java.util.Date;

import org.uengine.five.entity.converter.OracleDateTimeStringConverter;

/**
 * 동시 수정 방지용 Lock. 리소스 id별로 한 사용자만 수정 모드 보유.
 * 테이블: definition_lock (DB 예약어 lock 회피)
 */
@Entity
@Table(name = "TB_BPM_DEF_LOCK")
public class DefinitionLockEntity {

    @Id
    @Column(name = "resource_id", length = 512)
    private String id;

    @Column(name = "user_id", nullable = false, length = 255)
    private String userId;

    @Convert(converter = OracleDateTimeStringConverter.class)
    @Column(name = "updated_at", nullable = false)
    private Date updatedAt;

    @PrePersist
    public void prePersist() {
        if (updatedAt == null) {
            updatedAt = new Date();
        }
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = new Date();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }
}
