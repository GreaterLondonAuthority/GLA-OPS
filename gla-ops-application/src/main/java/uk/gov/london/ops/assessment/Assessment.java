/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.assessment;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.StringUtils;
import uk.gov.london.ops.domain.OpsEntity;
import uk.gov.london.ops.domain.Requirement;
import uk.gov.london.ops.framework.jpa.Join;
import uk.gov.london.ops.framework.jpa.JoinData;
import uk.gov.london.ops.organisation.model.Organisation;
import uk.gov.london.ops.project.internalblock.InternalAssessmentBlock;
import uk.gov.london.ops.user.domain.User;

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

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "block_id")
    private InternalAssessmentBlock block;

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

    @JsonIgnore
    @ManyToOne(cascade = {})
    @JoinColumn(name = "created_by")
    private User creator;

    @Column(name = "created_on", updatable = false)
    private OffsetDateTime createdOn;

    @JsonIgnore
    @ManyToOne(cascade = {})
    @JoinColumn(name = "modified_by")
    private User modifier;

    @Column(name = "modified_on")
    private OffsetDateTime modifiedOn;

    @JsonIgnore
    @ManyToOne(cascade = {})
    @JoinColumn(name = "completed_by")
    private User completedBy;

    @Column(name = "completed_on")
    private OffsetDateTime completedOn;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, targetEntity = AssessmentSection.class)
    @JoinColumn(name = "assessment_id")
    private List<AssessmentSection> sections = new ArrayList<>();

    @JsonIgnore
    @ManyToOne(cascade = {})
    @JoinColumn(name = "users_primary_organisation_id")
    private Organisation usersPrimaryOrganisation;

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

    public InternalAssessmentBlock getBlock() {
        return block;
    }

    public void setBlock(InternalAssessmentBlock block) {
        this.block = block;
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

    public User getCreator() {
        return creator;
    }

    public void setCreator(User creator) {
        this.creator = creator;
    }

    @Override
    public String getCreatedBy() {
        return creator != null ? creator.getUsername() : null;
    }

    @Override
    public void setCreatedBy(String createdBy) {
        this.creator = new User(createdBy);
    }

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public String getCreatorName() {
        return creator != null ? creator.getFullName() : null;
    }

    @Override
    public OffsetDateTime getCreatedOn() {
        return createdOn;
    }

    @Override
    public void setCreatedOn(OffsetDateTime createdOn) {
        this.createdOn = createdOn;
    }

    public User getModifier() {
        return modifier;
    }

    public void setModifier(User modifier) {
        this.modifier = modifier;
    }

    @Override
    public String getModifiedBy() {
        return modifier != null ? modifier.getUsername() : null;
    }

    @Override
    public void setModifiedBy(String modifiedBy) {
        this.modifier = new User(modifiedBy);
    }

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public String getModifierName() {
        return modifier != null ? modifier.getFullName() : null;
    }

    @Override
    public OffsetDateTime getModifiedOn() {
        return modifiedOn;
    }

    @Override
    public void setModifiedOn(OffsetDateTime modifiedOn) {
        this.modifiedOn = modifiedOn;
    }

    public User getCompletedBy() {
        return completedBy;
    }

    public void setCompletedBy(User completedBy) {
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
            if (StringUtils.isEmpty(getComments())) return false;
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
                } else if (criteria.getCommentsRequirement() == Requirement.mandatory && StringUtils.isEmpty(criteria.getComments())) return false;
            }

            if (assessmentSection.getCommentsRequirement() == Requirement.mandatory && StringUtils.isEmpty(assessmentSection.getComments())) return false;
        }

        return true;
    }


    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public String getCompletedByName() {
        return completedBy != null ? completedBy.getFullName() : null;
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
    public Integer getBlockId() {
        return block != null ? block.getId() : null;
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

    public Organisation getUsersPrimaryOrganisation() {
        return usersPrimaryOrganisation;
    }

    public void setUsersPrimaryOrganisation(Organisation usersPrimaryOrganisation) {
        this.usersPrimaryOrganisation = usersPrimaryOrganisation;
    }

    public String getUsersPrimaryOrganisationName() {
        return usersPrimaryOrganisation == null ? "" : usersPrimaryOrganisation.getName();
    }

    public AssessmentTemplateOutcome getOutcome() {
        return outcome;
    }

    public void setOutcome(AssessmentTemplateOutcome outcome) {
        this.outcome = outcome;
    }

    public Integer getProgrammeId() {
        return this.getBlock().getProject().getProgrammeId();
    }

    public String getProjectTitle() {
        return this.getBlock().getProject().getTitle();
    }

    public Integer getTemplateId() {
        return this.getBlock().getProject().getTemplateId();
    }

    public AssessmentOutcomeSummary getOutcomeSummary() {
        return outcomeSummary;
    }

    public void setOutcomeSummary(AssessmentOutcomeSummary outcomeSummary) {
        this.outcomeSummary = outcomeSummary;
    }
}
