package org.uengine.five.entity;

import javax.persistence.*;
import java.util.Date;

/**
 * Created by uengine on 2017. 12. 21..
 */

// JPA DDL Generation
// CREATE TABLE bpm_audit (
//     auditId BIGINT NOT NULL,
//     fullTracingTag VARCHAR(255),
//     startedDate TIMESTAMP,
//     finishedDate TIMESTAMP,
//     activityName VARCHAR(255),
//     tracingTag VARCHAR(255),
//     instId BIGINT,
//     rootInstId BIGINT,
//     PRIMARY KEY (auditId)
// );
// ALTER TABLE bpm_audit 
// ADD CONSTRAINT FK_rootProcessInstance 
// FOREIGN KEY (rootInstId) 
// REFERENCES BPM_PROCINST (instId);
// ALTER TABLE bpm_audit 
// ADD CONSTRAINT FK_processInstance 
// FOREIGN KEY (instId) 
// REFERENCES BPM_PROCINST (instId);

 
@Entity
@Table(name="bpm_audit")
public class AuditEntity {

    @ManyToOne
    @JoinColumn(name="rootInstId")
    ProcessInstanceEntity rootProcessInstance;

    @Id
    Long auditId;

    String fullTracingTag;

    @Temporal(TemporalType.TIMESTAMP)
    Date startedDate;

    @Temporal(TemporalType.TIMESTAMP)
    Date finishedDate;

    String activityName;
    private String tracingTag;


    @ManyToOne
    @JoinColumn(name="instId")
    private ProcessInstanceEntity processInstance;


    public ProcessInstanceEntity getRootProcessInstance() {
        return rootProcessInstance;
    }

    public void setRootProcessInstance(ProcessInstanceEntity rootProcessInstance) {
        this.rootProcessInstance = rootProcessInstance;
    }

    public String getFullTracingTag() {
        return fullTracingTag;
    }

    public void setFullTracingTag(String fullTracingTag) {
        this.fullTracingTag = fullTracingTag;
    }

    public Date getStartedDate() {
        return startedDate;
    }

    public void setStartedDate(Date startedDate) {
        this.startedDate = startedDate;
    }

    public Date getFinishedDate() {
        return finishedDate;
    }

    public void setFinishedDate(Date finishedDate) {
        this.finishedDate = finishedDate;
    }

    public String getActivityName() {
        return activityName;
    }

    public void setActivityName(String activityName) {
        this.activityName = activityName;
    }

    public Long getAuditId() {
        return auditId;
    }

    public void setAuditId(Long auditId) {
        this.auditId = auditId;
    }


    public void setTracingTag(String tracingTag) {
        this.tracingTag = tracingTag;
    }

    public String getTracingTag() {
        return tracingTag;
    }

    public void setProcessInstance(ProcessInstanceEntity processInstance) {
        this.processInstance = processInstance;
    }

    public ProcessInstanceEntity getProcessInstance() {
        return processInstance;
    }
}
