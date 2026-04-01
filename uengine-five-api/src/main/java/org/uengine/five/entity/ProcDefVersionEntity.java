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

import org.uengine.five.entity.converter.OracleDateTimeStringConverter;

/**
 * 정의 버전 아카이브 저장본.
 * ProcessGPT의 proc_def_version 개념을 Oracle 저장용으로 단순화했다.
 */
@Entity
@Table(name = "TB_BPM_PDEF_VER")
public class ProcDefVersionEntity {

    @Id
    @Column(name = "arcv_id", length = 768, nullable = false)
    private String arcvId;

    @Column(name = "proc_def_id", length = 512, nullable = false)
    private String procDefId;

    @Column(name = "version", length = 64, nullable = false)
    private String version;

    @Column(name = "version_tag", length = 64)
    private String versionTag;

    @Convert(converter = OracleDateTimeStringConverter.class)
    @Column(name = "time_stamp", nullable = false)
    private Date timeStamp;

    @Lob
    @Column(name = "snapshot")
    private String snapshot;

    @Lob
    @Column(name = "definition_json")
    private String definitionJson;

    @Lob
    @Column(name = "diff_text")
    private String diff;

    @Lob
    @Column(name = "message_text")
    private String message;

    @Column(name = "created_by_name", length = 255)
    private String createdByName;

    @Column(name = "updated_by_name", length = 255)
    private String updatedByName;

    @PrePersist
    public void prePersist() {
        if (timeStamp == null) {
            timeStamp = new Date();
        }
    }

    @PreUpdate
    public void preUpdate() {
        timeStamp = new Date();
    }

    public String getArcvId() {
        return arcvId;
    }

    public void setArcvId(String arcvId) {
        this.arcvId = arcvId;
    }

    public String getProcDefId() {
        return procDefId;
    }

    public void setProcDefId(String procDefId) {
        this.procDefId = procDefId;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getVersionTag() {
        return versionTag;
    }

    public void setVersionTag(String versionTag) {
        this.versionTag = versionTag;
    }

    public Date getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Date timeStamp) {
        this.timeStamp = timeStamp;
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

    public String getDiff() {
        return diff;
    }

    public void setDiff(String diff) {
        this.diff = diff;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
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
