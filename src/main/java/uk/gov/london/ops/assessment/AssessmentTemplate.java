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
import org.apache.commons.collections.CollectionUtils;
import uk.gov.london.common.GlaUtils;
import uk.gov.london.ops.domain.OpsEntity;
import uk.gov.london.ops.domain.Requirement;
import uk.gov.london.ops.domain.organisation.Organisation;
import uk.gov.london.ops.domain.user.User;
import uk.gov.london.ops.service.ManagedEntityInterface;
import uk.gov.london.common.error.ApiErrorItem;

import javax.persistence.*;
import javax.transaction.Transactional;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Entity(name="assessment_template")
public class AssessmentTemplate implements OpsEntity<Integer>, ManagedEntityInterface {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "assessment_template_seq_gen")
    @SequenceGenerator(name = "assessment_template_seq_gen", sequenceName = "assessment_template_seq", initialValue = 100, allocationSize = 1)
    private Integer id;

    @Column
    private String name;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @ManyToOne(cascade = {})
    @JoinColumn(name = "managing_organisation_id")
    private Organisation managingOrganisation;

    @JsonIgnore
    @ManyToOne(cascade = {})
    @JoinColumn(name = "created_by")
    private User creator;

    @Column(name="created_on", updatable = false)
    private OffsetDateTime createdOn;

    @JsonIgnore
    @ManyToOne(cascade = {})
    @JoinColumn(name = "modified_by")
    private User modifier;

    @Column(name="modified_on")
    private OffsetDateTime modifiedOn;

    @Transient
    private boolean used;

    @Transient
    private Integer temporaryManagingOrgId;

    @Column(name = "comments_requirement")
    @Enumerated(EnumType.STRING)
    private Requirement commentsRequirement;

    @Column(name="status")
    private AssessmentTemplateStatus status = AssessmentTemplateStatus.Draft;

    @Column(name="include_weight")
    private Boolean includeWeight = true;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, targetEntity = AssessmentTemplateScore.class)
    @JoinColumn(name = "assessment_template_id")
    private List<AssessmentTemplateScore> scores = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, targetEntity = AssessmentTemplateSection.class)
    @JoinColumn(name = "assessment_template_id")
    private List<AssessmentTemplateSection> sections = new ArrayList<>();

    public AssessmentTemplate() {}

    public AssessmentTemplate(Integer id, String name, AssessmentTemplateStatus status, Integer organisationId, String organisationName) {
        this(name, new Organisation(organisationId, organisationName));
        this.id = id;
        this.status = status;
    }

    public AssessmentTemplate(String name, Organisation managingOrganisation) {
        this.name = name;
        this.managingOrganisation = managingOrganisation;
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

    @Override
    public Organisation getManagingOrganisation() {
        return managingOrganisation;
    }

    public void setManagingOrganisation(Organisation managingOrganisation) {
        this.managingOrganisation = managingOrganisation;
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
        if (CollectionUtils.isNotEmpty(sections)) {
            return sections.stream().map(s -> s.getWeight()).reduce(0, GlaUtils::nullSafeAdd);
        }
        return 0;
    }

    public Integer getTemporaryManagingOrganisationId(){
        return this.temporaryManagingOrgId;
    }

    public void setManagingOrganisationId(Integer id){
        this.temporaryManagingOrgId = id;
    }

    public Map<String, List<ApiErrorItem>> getValidationFailures() {
        boolean hasSubErrors = false;
        Map<String, List<ApiErrorItem>> errors = new HashMap<>();

        if (CollectionUtils.isNotEmpty(scores) && scores.size() < 2) {
            errors.put("scores", new ArrayList<>());
            errors.get("scores").add(new ApiErrorItem("At least 2 scores must be entered"));
            hasSubErrors = true;
        }

        if (getTotalSectionsWeight() > 0 && !includeWeight) {
            errors.put("sections", new ArrayList<>());
            errors.get("sections").add(new ApiErrorItem("Weight will have no effect, as Include weight % is unchecked"));
        } else if(getTotalSectionsWeight() > 0 && getTotalSectionsWeight() != 100){
            errors.put("sections", new ArrayList<>());
            errors.get("sections").add(new ApiErrorItem("Total weight percentage for sections in the assessment must total 100%"));
            hasSubErrors = true;
        }


        if(!hasSubErrors){
            for(AssessmentTemplateSection s : this.getSections()){
                if(!hasSubErrors && !s.getValidationFailures().isEmpty()){
                    hasSubErrors = true;
                }
            }
        }

        if(hasSubErrors){
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
}
