/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.assessment;

import com.querydsl.core.annotations.QueryEntity;

import javax.persistence.*;
import java.time.OffsetDateTime;

@Entity(name = "v_assessment_summaries")
@QueryEntity
public class AssessmentSummary {

    @Id
    private Integer id;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private AssessmentStatus status = AssessmentStatus.InProgress;

    @JoinColumn(name = "assessment_template")
    private String assessmentTemplate;

    @Column(name = "project_id")
    private Integer projectId;

    @Column(name = "project_status")
    private String projectStatus;

    @Column(name = "project_substatus")
    private String projectSubStatus;

    @Column(name = "creator")
    private String creator;

    @Column(name = "created_on", updatable = false)
    private OffsetDateTime createdOn;

    @Column(name = "modified_on")
    private OffsetDateTime modifiedOn;

    @Column(name = "users_primary_org")
    private String usersPrimaryOrganisation;

    @Column(name = "project_title")
    private String projectTitle;

    @Column(name = "programme_id")
    private Integer programmeId;

    @Column(name = "programme_name")
    private String programmeName;

    @Column(name = "managing_org_id")
    private Integer managingOrgId;

    public AssessmentSummary() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public AssessmentStatus getStatus() {
        return status;
    }

    public void setStatus(AssessmentStatus status) {
        this.status = status;
    }

    public String getAssessmentTemplate() {
        return assessmentTemplate;
    }

    public void setAssessmentTemplate(String assessmentTemplate) {
        this.assessmentTemplate = assessmentTemplate;
    }

    public Integer getProjectId() {
        return projectId;
    }

    public void setProjectId(Integer projectId) {
        this.projectId = projectId;
    }

    public String getProjectStatus() {
        return projectStatus;
    }

    public void setProjectStatus(String projectStatus) {
        this.projectStatus = projectStatus;
    }

    public String getProjectSubStatus() {
        return projectSubStatus;
    }

    public void setProjectSubStatus(String projectSubStatus) {
        this.projectSubStatus = projectSubStatus;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public OffsetDateTime getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(OffsetDateTime createdOn) {
        this.createdOn = createdOn;
    }

    public OffsetDateTime getModifiedOn() {
        return modifiedOn;
    }

    public void setModifiedOn(OffsetDateTime modifiedOn) {
        this.modifiedOn = modifiedOn;
    }

    public String getUsersPrimaryOrganisation() {
        return usersPrimaryOrganisation;
    }

    public void setUsersPrimaryOrganisation(String usersPrimaryOrganisation) {
        this.usersPrimaryOrganisation = usersPrimaryOrganisation;
    }

    public String getProjectTitle() {
        return projectTitle;
    }

    public void setProjectTitle(String projectTitle) {
        this.projectTitle = projectTitle;
    }

    public Integer getProgrammeId() {
        return programmeId;
    }

    public void setProgrammeId(Integer programmeId) {
        this.programmeId = programmeId;
    }

    public String getProgrammeName() {
        return programmeName;
    }

    public void setProgrammeName(String programmeName) {
        this.programmeName = programmeName;
    }

    public Integer getManagingOrgId() {
        return managingOrgId;
    }

    public void setManagingOrgId(Integer managingOrgId) {
        this.managingOrgId = managingOrgId;
    }
}
