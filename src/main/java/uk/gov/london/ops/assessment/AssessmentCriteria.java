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
import uk.gov.london.ops.domain.Requirement;

import javax.persistence.*;
import java.math.BigDecimal;

@Entity(name="assessment_criteria")
public class AssessmentCriteria {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "assessment_criteria_seq_gen")
    @SequenceGenerator(name = "assessment_criteria_seq_gen", sequenceName = "assessment_criteria_seq", initialValue = 100, allocationSize = 1)
    private Integer id;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "assessment_template_criteria_id")
    private AssessmentTemplateCriteria criteriaTemplate;

    @Column(name = "comments")
    private String comments;

    @Column(name = "score")
    private BigDecimal score;

    @Column(name = "failed")
    private Boolean failed;

    public AssessmentCriteria() {}

    public AssessmentCriteria(AssessmentTemplateCriteria criteriaTemplate) {
        this.criteriaTemplate = criteriaTemplate;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public AssessmentTemplateCriteria getCriteriaTemplate() {
        return criteriaTemplate;
    }

    public void setCriteriaTemplate(AssessmentTemplateCriteria criteriaTemplate) {
        this.criteriaTemplate = criteriaTemplate;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public BigDecimal getScore() {
        return score;
    }

    public void setScore(BigDecimal score) {
        this.score = score;
    }

    public Boolean getFailed() {
        return failed;
    }

    public void setFailed(Boolean failed) {
        this.failed = failed;
    }

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public String getTitle() {
        return criteriaTemplate != null ? criteriaTemplate.getTitle() : null;
    }

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public Integer getWeight() {
        return criteriaTemplate != null ? criteriaTemplate.getWeight() : null;
    }

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public Requirement getCommentsRequirement() {
        return criteriaTemplate != null ? criteriaTemplate.getCommentsRequirement() : null;
    }

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public CriteriaAnswerType getAnswerType() {
        return criteriaTemplate != null ? criteriaTemplate.getAnswerType() : null;
    }

    public void merge(AssessmentCriteria updated) {
        this.setComments(updated.getComments());
        this.setScore(updated.getScore());
        this.setFailed(updated.getFailed());
    }

}
