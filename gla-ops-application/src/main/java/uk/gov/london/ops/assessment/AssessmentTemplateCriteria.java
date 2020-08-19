/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.assessment;

import uk.gov.london.ops.domain.Requirement;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity(name = "assessment_template_criteria")
public class AssessmentTemplateCriteria {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "assessment_template_criteria_seq_gen")
    @SequenceGenerator(name = "assessment_template_criteria_seq_gen", sequenceName = "assessment_template_criteria_seq", initialValue = 100, allocationSize = 1)
    private Integer id;

    @Column(name = "title")
    private String title;

    @Column(name = "weight")
    private Integer weight;

    @Column(name = "comments_requirement")
    @Enumerated(EnumType.STRING)
    private Requirement commentsRequirement;

    @Column(name = "answer_type")
    @Enumerated(EnumType.STRING)
    private CriteriaAnswerType answerType;

    @OneToMany(cascade = CascadeType.ALL, targetEntity = CriteriaAnswerOption.class)
    @JoinColumn(name = "assessment_template_criteria_id")
    private List<CriteriaAnswerOption> criteriaAnswerOptions = new ArrayList<>();

    @Column(name = "display_order")
    private Double displayOrder;

    public AssessmentTemplateCriteria() {}

    public AssessmentTemplateCriteria(String title, Integer weight, Requirement commentsRequirement, CriteriaAnswerType answerType, Double displayOrder) {
        this.title = title;
        this.weight = weight;
        this.commentsRequirement = commentsRequirement;
        this.answerType = answerType;
        this.displayOrder = displayOrder;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Integer getWeight() {
        return weight;
    }

    public void setWeight(Integer weight) {
        this.weight = weight;
    }

    public Requirement getCommentsRequirement() {
        return commentsRequirement;
    }

    public void setCommentsRequirement(Requirement commentsRequirement) {
        this.commentsRequirement = commentsRequirement;
    }

    public CriteriaAnswerType getAnswerType() {
        return answerType;
    }

    public void setAnswerType(CriteriaAnswerType answerType) {
        this.answerType = answerType;
    }

    public List<CriteriaAnswerOption> getCriteriaAnswerOptions() {
        return criteriaAnswerOptions;
    }

    public void setCriteriaAnswerOptions(List<CriteriaAnswerOption> criteriaAnswerOptions) {
        this.criteriaAnswerOptions = criteriaAnswerOptions;
    }

    public Double getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(Double displayOrder) {
        if (displayOrder == null) {
            this.displayOrder = new Double(id);
        } else {
            this.displayOrder = displayOrder;
        }
    }
}
