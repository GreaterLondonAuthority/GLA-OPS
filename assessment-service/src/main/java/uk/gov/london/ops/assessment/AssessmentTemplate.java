/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.assessment;

import uk.gov.london.common.GlaUtils;
import uk.gov.london.common.error.ApiErrorItem;
import uk.gov.london.ops.framework.OpsEntity;
import uk.gov.london.ops.framework.enums.Requirement;

import javax.persistence.*;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Entity(name = "assessment_template")
public class AssessmentTemplate implements OpsEntity<Integer> {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "assessment_template_seq_gen")
    @SequenceGenerator(name = "assessment_template_seq_gen", sequenceName = "assessment_template_seq",
            initialValue = 100, allocationSize = 1)
    private Integer id;

    @Column
    private String name;

    @Column(name = "managing_organisation_id")
    private Integer managingOrganisationId;

    @Transient
    private String managingOrganisationName;

    @Column
    private Boolean summary = false;

    @Column(name = "outcome_of_assessment_template_id")
    private Integer outcomeOfAssessmentTemplateId;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "created_on", updatable = false)
    private OffsetDateTime createdOn;

    @Column(name = "modified_by")
    private String modifiedBy;

    @Column(name = "modified_on")
    private OffsetDateTime modifiedOn;

    @Transient
    private String creatorName;

    @Transient
    private String modifierName;

    @Transient
    private boolean used;

    @Column(name = "comments_requirement")
    @Enumerated(EnumType.STRING)
    private Requirement commentsRequirement;

    @Column(name = "status")
    private AssessmentTemplateStatus status = AssessmentTemplateStatus.Draft;

    @Column(name = "include_weight")
    private Boolean includeWeight = true;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, targetEntity = AssessmentTemplateScore.class)
    @JoinColumn(name = "assessment_template_id")
    private List<AssessmentTemplateScore> scores = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, targetEntity = AssessmentTemplateSection.class)
    @JoinColumn(name = "assessment_template_id")
    private List<AssessmentTemplateSection> sections = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, targetEntity = AssessmentTemplateOutcome.class)
    @JoinColumn(name = "assessment_template_id")
    private List<AssessmentTemplateOutcome> outcomes = new ArrayList<>();

    public AssessmentTemplate() {
    }

    public AssessmentTemplate(Integer id, String name, AssessmentTemplateStatus status, Integer organisationId) {
        this(name, organisationId);
        this.id = id;
        this.status = status;
    }

    public AssessmentTemplate(String name, Integer managingOrganisationId) {
        this.name = name;
        this.managingOrganisationId = managingOrganisationId;
    }

    @Override
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getManagingOrganisationId() {
        return managingOrganisationId;
    }

    public void setManagingOrganisationId(Integer managingOrganisationId) {
        this.managingOrganisationId = managingOrganisationId;
    }

    public String getManagingOrganisationName() {
        return managingOrganisationName;
    }

    public void setManagingOrganisationName(String managingOrganisationName) {
        this.managingOrganisationName = managingOrganisationName;
    }

    public Boolean getSummary() {
        return summary;
    }

    public void setSummary(Boolean summary) {
        this.summary = summary;
    }

    public Integer getOutcomeOfAssessmentTemplateId() {
        return outcomeOfAssessmentTemplateId;
    }

    public void setOutcomeOfAssessmentTemplateId(Integer outcomeOfAssessmentTemplateId) {
        this.outcomeOfAssessmentTemplateId = outcomeOfAssessmentTemplateId;
    }

    @Override
    public String getCreatedBy() {
        return createdBy;
    }

    @Override
    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
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

    @Override
    public OffsetDateTime getModifiedOn() {
        return modifiedOn;
    }

    @Override
    public void setModifiedOn(OffsetDateTime modifiedOn) {
        this.modifiedOn = modifiedOn;
    }

    public boolean isUsed() {
        return used;
    }

    public void setUsed(boolean used) {
        this.used = used;
    }

    public Requirement getCommentsRequirement() {
        return commentsRequirement;
    }

    public void setCommentsRequirement(Requirement commentsRequirement) {
        this.commentsRequirement = commentsRequirement;
    }

    public List<AssessmentTemplateScore> getScores() {
        return scores;
    }

    public Boolean isIncludeWeight() {
        return includeWeight;
    }

    public void setIncludeWeight(Boolean includeWeight) {
        this.includeWeight = includeWeight;
    }

    public void setScores(List<AssessmentTemplateScore> scores) {
        this.scores = scores;
    }

    public List<AssessmentTemplateSection> getSections() {
        return sections;
    }

    public void setSections(List<AssessmentTemplateSection> sections) {
        this.sections = sections;
    }

    public int getTotalSectionsWeight() {
        if (sections != null && !sections.isEmpty()) {
            return sections.stream().map(s -> s.getWeight()).reduce(0, GlaUtils::nullSafeAdd);
        }
        return 0;
    }

    public boolean sectionWeightValid() {
        return sections.stream()
                .filter(section -> section.getWeight() != null)
                .mapToInt(AssessmentTemplateSection::getWeight)
                .sum() == 100;
    }

    public boolean sectionEmpty() {
        List<AssessmentTemplateSection> emptySections = sections.stream()
                .filter(s -> s.getCriteriaList().isEmpty())
                .collect(Collectors.toList());

        return sections.size() > 0 && emptySections.size() > 0;
    }

    public List<AssessmentTemplateOutcome> getOutcomes() {
        return outcomes;
    }

    public void setOutcomes(List<AssessmentTemplateOutcome> outcomes) {
        this.outcomes = outcomes;
    }

    public Map<String, List<ApiErrorItem>> getValidationFailures() {
        boolean hasSubErrors = false;
        Map<String, List<ApiErrorItem>> errors = new HashMap<>();

        if (scores != null && scores.size() == 1) {
            errors.put("scores", new ArrayList<>());
            errors.get("scores").add(new ApiErrorItem("At least 2 scores must be entered"));
            hasSubErrors = true;
        }

        if (getTotalSectionsWeight() > 0 && !includeWeight) {
            errors.put("sections", new ArrayList<>());
            errors.get("sections").add(new ApiErrorItem("Weight will have no effect, as Include weight % is unchecked"));
        } else if (!sections.isEmpty() && includeWeight && !sectionWeightValid()) {
            errors.put("sections", new ArrayList<>());
            errors.get("sections").add(new ApiErrorItem("Total weight percentage for sections in the assessment must total 100%"));
            hasSubErrors = true;
        }

        if (summary && outcomeOfAssessmentTemplateId == null) {
            errors.put("summary", new ArrayList<>());
            errors.get("summary").add(new ApiErrorItem("Assessments with summary must have individual template selected"));
            hasSubErrors = true;
        }

        if (hasSubErrors) {
            errors.put("readyForUse", new ArrayList<>());
            errors.get("readyForUse").add(new ApiErrorItem("Please resolve all errors before making it ready for use"));
        }

        return errors;
    }

    public AssessmentTemplateStatus getStatus() {
        return status;
    }

    public void setStatus(AssessmentTemplateStatus status) {
        this.status = status;
    }

    public String getCreatorName() {
        return creatorName;
    }

    public void setCreatorName(String creatorName) {
        this.creatorName = creatorName;
    }

    public String getModifierName() {
        return modifierName;
    }

    public void setModifierName(String modifierName) {
        this.modifierName = modifierName;
    }

}
