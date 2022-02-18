/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.assessment;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.StringUtils;
import uk.gov.london.ops.framework.OpsEntity;
import uk.gov.london.ops.framework.enums.Requirement;
import uk.gov.london.ops.framework.jpa.Join;
import uk.gov.london.ops.framework.jpa.JoinData;

import javax.persistence.*;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity(name = "assessment")
public class Assessment implements OpsEntity<Integer> {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "assessment_seq_gen")
    @SequenceGenerator(name = "assessment_seq_gen", sequenceName = "assessment_seq", initialValue = 100, allocationSize = 1)
    private Integer id;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private AssessmentStatus status = AssessmentStatus.InProgress;

    @Column(name = "block_id")
    private Integer blockId;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @ManyToOne
    @JoinColumn(name = "assessment_template_id")
    private AssessmentTemplate assessmentTemplate;

    @Column(name = "project_status")
    private String projectStatus;

    @Column(name = "project_substatus")
    private String projectSubStatus;

    @Column(name = "comments")
    private String comments;

    @Column(name = "created_by")
    private String createdBy;

    @Transient
    private String creatorName;

    @Column(name = "created_on", updatable = false)
    private OffsetDateTime createdOn;

    @Column(name = "modified_by")
    private String modifiedBy;

    @Transient
    private String modifierName;

    @Column(name = "modified_on")
    private OffsetDateTime modifiedOn;

    @Column(name = "completed_by")
    private String completedBy;

    @Column(name = "completed_on")
    private OffsetDateTime completedOn;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, targetEntity = AssessmentSection.class)
    @JoinColumn(name = "assessment_id")
    private List<AssessmentSection> sections = new ArrayList<>();

    @Column(name = "users_primary_organisation_id")
    private Integer usersPrimaryOrganisationId;

    @ManyToOne(cascade = {})
    @JoinColumn(name = "outcome_id")
    private AssessmentTemplateOutcome outcome;

    @Column(name = "project_id")
    @JoinData(targetTable = "project", targetColumn = "id", joinType = Join.JoinType.OneToOne,
            comment = "The project this assessment is linked to")
    private Integer projectId;

    @Column(name = "managing_org_id")
    @JoinData(targetTable = "organisation", targetColumn = "id", joinType = Join.JoinType.OneToOne,
            comment = "The managing org this assessment is linked to")
    private Integer managingOrgId;

    @Transient
    private AssessmentOutcomeSummary outcomeSummary;

    @Transient
    private String usersPrimaryOrganisationName;

    @Transient
    private Integer programmeId;

    @Transient
    private String projectTitle;

    @Transient
    private Integer templateId;

    public Assessment() {
    }

    public Assessment(AssessmentTemplate assessmentTemplate) {
        initFrom(assessmentTemplate);
    }

    public Assessment(AssessmentTemplate assessmentTemplate, AssessmentStatus status, OffsetDateTime completedOn) {
        this(assessmentTemplate);
        this.status = status;
        this.completedOn = completedOn;
    }

    @Override
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

    public Integer getBlockId() {
        return blockId;
    }

    public void setBlockId(Integer blockId) {
        this.blockId = blockId;
    }

    public AssessmentTemplate getAssessmentTemplate() {
        return assessmentTemplate;
    }

    public void setAssessmentTemplate(AssessmentTemplate assessmentTemplate) {
        this.assessmentTemplate = assessmentTemplate;
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

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    @Override
    public String getCreatedBy() {
        return createdBy;
    }

    @Override
    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public String getCreatorName() {
        return creatorName;
    }

    public void setCreatorName(String creatorName) {
        this.creatorName = creatorName;
    }

    @Override
    public OffsetDateTime getCreatedOn() {
        return createdOn;
    }

    @Override
    public void setCreatedOn(OffsetDateTime createdOn) {
        this.createdOn = createdOn;
    }

    @Override
    public String getModifiedBy() {
        return modifiedBy;
    }

    @Override
    public void setModifiedBy(String modifiedBy) {
        this.modifiedBy = modifiedBy;
    }

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public String getModifierName() {
        return modifierName;
    }

    public void setModifierName(String modifierName) {
        this.modifierName = modifierName;
    }

    @Override
    public OffsetDateTime getModifiedOn() {
        return modifiedOn;
    }

    @Override
    public void setModifiedOn(OffsetDateTime modifiedOn) {
        this.modifiedOn = modifiedOn;
    }

    public String getCompletedBy() {
        return completedBy;
    }

    public void setCompletedBy(String completedBy) {
        this.completedBy = completedBy;
    }

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public Integer getProjectId() {
        return projectId;
    }

    public void setProjectId(Integer projectId) {
        this.projectId = projectId;
    }

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public Integer getManagingOrgId() {
        return managingOrgId;
    }

    public void setManagingOrgId(Integer managingOrgId) {
        this.managingOrgId = managingOrgId;
    }

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public boolean isComplete() {
        Requirement commentRequirement = assessmentTemplate.getCommentsRequirement();
        if (commentRequirement != null && commentRequirement.equals(Requirement.mandatory)) {
            if (StringUtils.isEmpty(getComments())) {
                return false;
            }
        }

        if (assessmentTemplate.getOutcomes() != null && !assessmentTemplate.getOutcomes().isEmpty() && this.outcome == null) {
            return false;
        }

        for (AssessmentSection assessmentSection : sections) {
            for (AssessmentCriteria criteria : assessmentSection.getCriteriaList()) {
                if (criteria.getAnswerType() == CriteriaAnswerType.Score && criteria.getScore() == null) {
                    return false;
                }  else if (criteria.getAnswerType() == CriteriaAnswerType.PassFail && criteria.getFailed() == null) {
                    return false;
                } else if (criteria.getCommentsRequirement() == Requirement.mandatory
                        && StringUtils.isEmpty(criteria.getComments())) {
                    return false;
                }
            }
            if (assessmentSection.getCommentsRequirement() == Requirement.mandatory
                    && StringUtils.isEmpty(assessmentSection.getComments())) {
                return false;
            }
        }
        return true;
    }

    public OffsetDateTime getCompletedOn() {
        return completedOn;
    }

    public void setCompletedOn(OffsetDateTime completedOn) {
        this.completedOn = completedOn;
    }

    public List<AssessmentSection> getSections() {
        return sections;
    }

    public void setSections(List<AssessmentSection> sections) {
        this.sections = sections;
    }

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public Integer getAssessmentTemplateId() {
        return assessmentTemplate != null ? assessmentTemplate.getId() : null;
    }

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public String getAssessmentTemplateName() {
        return assessmentTemplate != null ? assessmentTemplate.getName() : null;
    }

    public void initFrom(AssessmentTemplate assessmentTemplate) {
        this.setAssessmentTemplate(assessmentTemplate);
        for (AssessmentTemplateSection templateSection : assessmentTemplate.getSections()) {
            AssessmentSection section = new AssessmentSection(templateSection);
            this.sections.add(section);
            for (AssessmentTemplateCriteria templateCriteria : templateSection.getCriteriaList()) {
                section.getCriteriaList().add(new AssessmentCriteria(templateCriteria));
            }
        }
    }

    public void merge(Assessment updated) {
        this.setStatus(updated.getStatus());
        this.setComments(updated.getComments());
        this.setCompletedOn(updated.getCompletedOn());
        this.setCompletedBy(updated.getCompletedBy());
        this.setOutcome(updated.getOutcome());
        for (AssessmentSection section : updated.getSections()) {
            this.getSection(section.getId()).merge(section);
        }
    }

    private AssessmentSection getSection(Integer id) {
        return sections.stream().filter(s -> s.getId().equals(id)).findFirst().orElse(null);
    }

    public Integer getUsersPrimaryOrganisationId() {
        return usersPrimaryOrganisationId;
    }

    public void setUsersPrimaryOrganisationId(Integer usersPrimaryOrganisationId) {
        this.usersPrimaryOrganisationId = usersPrimaryOrganisationId;
    }

    public AssessmentTemplateOutcome getOutcome() {
        return outcome;
    }

    public void setOutcome(AssessmentTemplateOutcome outcome) {
        this.outcome = outcome;
    }

    public Integer getProgrammeId() {
        return programmeId;
    }

    public void setProgrammeId(Integer programmeId) {
        this.programmeId = programmeId;
    }

    public String getProjectTitle() {
        return projectTitle;
    }

    public void setProjectTitle(String projectTitle) {
        this.projectTitle = projectTitle;
    }

    public Integer getTemplateId() {
        return templateId;
    }

    public void setTemplateId(Integer templateId) {
        this.templateId = templateId;
    }

    public AssessmentOutcomeSummary getOutcomeSummary() {
        return outcomeSummary;
    }

    public void setOutcomeSummary(AssessmentOutcomeSummary outcomeSummary) {
        this.outcomeSummary = outcomeSummary;
    }

    public String getUsersPrimaryOrganisationName() {
        return usersPrimaryOrganisationName;
    }

    public void setUsersPrimaryOrganisationName(String usersPrimaryOrganisationName) {
        this.usersPrimaryOrganisationName = usersPrimaryOrganisationName;
    }

}
