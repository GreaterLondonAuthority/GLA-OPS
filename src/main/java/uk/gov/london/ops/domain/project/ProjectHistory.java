/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.domain.project;

import uk.gov.london.ops.domain.OpsEntity;
import uk.gov.london.ops.service.project.state.ProjectState;
import uk.gov.london.ops.framework.jpa.Join;
import uk.gov.london.ops.framework.jpa.JoinData;
import uk.gov.london.ops.framework.jpa.NonJoin;

import javax.persistence.*;
import java.io.Serializable;
import java.time.OffsetDateTime;

@Entity
public class ProjectHistory implements OpsEntity<Integer>, Serializable {

    public enum HistoryEventType {
        StateTransition,
        MilestoneClaimApproved,
        QuarterlyClaimApproved,
        Transfer,
        Label
    }

    public enum Transition {
        Unconfirmed,
        Created,
        Submitted,
        Withdrawn,
        Returned,
        Assess,
        Initial_Assessment,
        Resubmitted,
        Approved,
        Closed,
        Completed,
        Abandoned,
        Amend,
        ApprovalRequested,
        PaymentAuthorisationRequested,
        DeletedUnapprovedChanges,
        AbandonRequested,
        AbandonRejected,
        Reinstated
    }

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "project_history_seq_gen")
    @SequenceGenerator(name = "project_history_seq_gen", sequenceName = "project_history_seq", initialValue = 10000, allocationSize = 1)
    private Integer id;

    @Column(name="project_id")
    @JoinData(targetTable = "project", targetColumn = "id", joinType = Join.JoinType.OneToOne,
            comment = "The project this history item is related to")
    private Integer projectId;

    @Column(name="transition")
    @Enumerated(EnumType.STRING)
    private Transition transition;

    @Column(name="event_type")
    @Enumerated(EnumType.STRING)
    private HistoryEventType historyEventType = HistoryEventType.StateTransition;

    @Column(name="description")
    private String description;

    @Column(name="comments")
    private String comments;

    @Column(name="created_on")
    private OffsetDateTime createdOn;

    @Column(name="created_by")
    private String createdBy;

    @Column(name="external_id")
    @NonJoin("This would usually identify a milestone for this history item, but is not joinable in itself")
    private Integer externalId;

    @Transient
    private String createdByFirstName;

    @Transient
    private String createdByLastName;

    @Column(name = "status")
    private String statusName;

    @Column(name = "substatus")
    private String subStatusName;

    public ProjectHistory() {}

    public ProjectHistory(Transition transition) {
        this.transition = transition;
    }

    public ProjectHistory(Transition transition, OffsetDateTime createdOn) {
        this(transition);
        this.createdOn = createdOn;
    }

    public ProjectHistory(HistoryEventType historyEventType, String description) {
        this.historyEventType = historyEventType;
        this.description = description;
    }

    public ProjectHistory(HistoryEventType historyEventType, String description, String comments) {
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

    public Transition getTransition() {
        return transition;
    }

    public void setTransition(Transition transition) {
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

    public String getCreatedByFirstName() {
        return createdByFirstName;
    }

    public void setCreatedByFirstName(String createdByFirstName) {
        this.createdByFirstName = createdByFirstName;
    }

    public String getCreatedByLastName() {
        return createdByLastName;
    }

    public void setCreatedByLastName(String createdByLastName) {
        this.createdByLastName = createdByLastName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public HistoryEventType getHistoryEventType() {
        return historyEventType;
    }

    public void setHistoryEventType(HistoryEventType historyEventType) {
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

    public ProjectState getProjectState() {
        return new ProjectState(statusName, subStatusName);
    }
}
