package org.uengine.five.entity;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import java.util.Date;

import org.uengine.five.entity.converter.OracleBooleanConverter;
import org.uengine.five.entity.converter.OracleDateTimeStringConverter;

/**
 * 현재 정의 저장본.
 * BPMN/XML/JSON/raw 파일과 디렉터리 메타데이터를 함께 저장한다.
 */
@Entity
@Table(name = "TB_BPM_PROCDEF")
public class ProcDefEntity {

    @Id
    @Column(name = "id", length = 512, nullable = false)
    private String id;

    @Column(name = "name", length = 255)
    private String name;

    @Column(name = "path", length = 512, nullable = false)
    private String path;

    @Convert(converter = OracleBooleanConverter.class)
    @Column(name = "is_directory", nullable = false)
    private Boolean directory = Boolean.FALSE;

    @Column(name = "resource_type", length = 64)
    private String resourceType;

    @Lob
    @Column(name = "snapshot")
    private String snapshot;

    @Lob
    @Column(name = "definition_json")
    private String definitionJson;

    @Convert(converter = OracleDateTimeStringConverter.class)
    @Column(name = "created_at", nullable = false)
    private Date createdAt;

    @Convert(converter = OracleDateTimeStringConverter.class)
    @Column(name = "updated_at", nullable = false)
    private Date updatedAt;

    @Column(name = "created_by_name", length = 255)
    private String createdByName;

    @Column(name = "updated_by_name", length = 255)
    private String updatedByName;

    @PrePersist
    public void prePersist() {
        Date now = new Date();
        if (createdAt == null) {
            createdAt = now;
        }
        if (updatedAt == null) {
            updatedAt = now;
        }
        if (directory == null) {
            directory = Boolean.FALSE;
        }
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = new Date();
        if (directory == null) {
            directory = Boolean.FALSE;
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Boolean getDirectory() {
        return directory;
    }

    public void setDirectory(Boolean directory) {
        this.directory = directory;
    }

    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    public String getSnapshot() {
        return snapshot;
    }

    public void setSnapshot(String snapshot) {
        this.snapshot = snapshot;
    }

    public String getDefinitionJson() {
        return definitionJson;
    }

    public void setDefinitionJson(String definitionJson) {
        this.definitionJson = definitionJson;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getCreatedByName() {
        return createdByName;
    }

    public void setCreatedByName(String createdByName) {
        this.createdByName = createdByName;
    }

    public String getUpdatedByName() {
        return updatedByName;
    }

    public void setUpdatedByName(String updatedByName) {
        this.updatedByName = updatedByName;
    }
}
