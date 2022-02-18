/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.project;

import uk.gov.london.ops.framework.OpsEntity;
import uk.gov.london.ops.framework.jpa.Join;
import uk.gov.london.ops.framework.jpa.JoinData;
import uk.gov.london.ops.framework.jpa.NonJoin;

import javax.persistence.*;
import java.io.Serializable;
import java.time.OffsetDateTime;

@Entity(name = "project_history")
public class ProjectHistoryEntity implements OpsEntity<Integer>, Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "project_history_seq_gen")
    @SequenceGenerator(name = "project_history_seq_gen", sequenceName = "project_history_seq",
        initialValue = 10000, allocationSize = 1)
    private Integer id;

    @Column(name = "project_id")
    @JoinData(targetTable = "project", targetColumn = "id", joinType = Join.JoinType.OneToOne,
            comment = "The project this history item is related to")
    private Integer projectId;

    @Column(name = "transition")
    @Enumerated(EnumType.STRING)
    private ProjectTransition transition;

    @Column(name = "event_type")
    @Enumerated(EnumType.STRING)
    private ProjectHistoryEventType historyEventType = ProjectHistoryEventType.StateTransition;

    @Column(name = "description")
    private String description;

    @Column(name = "comments")
    private String comments;

    @Column(name = "created_on")
    private OffsetDateTime createdOn;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "external_id")
    @NonJoin("This would usually identify a milestone for this history item, but is not joinable in itself")
    private Integer externalId;

    @Transient
    private String creatorName;

    @Column(name = "status")
    private String statusName;

    @Column(name = "substatus")
    private String subStatusName;

    public ProjectHistoryEntity() {}

    public ProjectHistoryEntity(ProjectTransition transition) {
        this.transition = transition;
    }

    public ProjectHistoryEntity(ProjectTransition transition, OffsetDateTime createdOn) {
        this(transition);
        this.createdOn = createdOn;
    }

    public ProjectHistoryEntity(ProjectHistoryEventType historyEventType, String description) {
        this.historyEventType = historyEventType;
        this.description = description;
    }

    public ProjectHistoryEntity(ProjectHistoryEventType historyEventType, String description, String comments) {
        this(historyEventType, description);
        this.comments = comments;
    }

    public Integer getId() {
        return id;
    }

    public Integer getProjectId() {
        return projectId;
    }

    public void setProjectId(Integer projectId) {
        this.projectId = projectId;
    }

    public ProjectTransition getTransition() {
        return transition;
    }

    public void setTransition(ProjectTransition transition) {
        this.transition = transition;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public OffsetDateTime getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(OffsetDateTime createdOn) {
        this.createdOn = createdOn;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    @Override
    public String getCreatorName() {
        return creatorName;
    }

    @Override
    public void setCreatorName(String creatorName) {
        this.creatorName = creatorName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ProjectHistoryEventType getHistoryEventType() {
        return historyEventType;
    }

    public void setHistoryEventType(ProjectHistoryEventType historyEventType) {
        this.historyEventType = historyEventType;
    }

    public Integer getExternalId() {
        return externalId;
    }

    public void setExternalId(Integer externalId) {
        this.externalId = externalId;
    }

    @Override
    public OffsetDateTime getModifiedOn() {
        return null;
    }

    @Override
    public void setModifiedOn(OffsetDateTime modifiedOn) {

    }

    @Override
    public String getModifiedBy() {
        return null;
    }

    @Override
    public void setModifiedBy(String modifiedBy) {

    }

    public String getStatusName() {
        return statusName;
    }

    public void setStatusName(String statusName) {
        this.statusName = statusName;
    }

    public String getSubStatusName() {
        return subStatusName;
    }

    public void setSubStatusName(String subStatusName) {
        this.subStatusName = subStatusName;
    }

}
