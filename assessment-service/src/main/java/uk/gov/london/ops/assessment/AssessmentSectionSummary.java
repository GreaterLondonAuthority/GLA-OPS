/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.assessment;

import uk.gov.london.common.GlaUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class AssessmentSectionSummary {

    private Integer id;
    private String title;
    private Integer weight;
    private AssessmentTemplate assessmentTemplate;
    private List<AssessmentCriteriaSummary> criteriaList = new ArrayList<>();
    private Double displayOrder;

    public AssessmentSectionSummary() {
    }

    public AssessmentSectionSummary(AssessmentTemplate assessmentTemplate, AssessmentTemplateSection sectionTemplate,
                                    List<Assessment> outcomeAssessments) {
        this.id = sectionTemplate.getId();
        this.title = sectionTemplate.getTitle();
        this.weight = sectionTemplate.getWeight();
        this.assessmentTemplate = assessmentTemplate;
        List<AssessmentTemplateCriteria> scoreCriteriaList = sectionTemplate.getCriteriaList().stream()
                .filter(c -> CriteriaAnswerType.Score == c.getAnswerType())
                .collect(Collectors.toList());
        for (AssessmentTemplateCriteria criteriaTemplate : scoreCriteriaList) {
            criteriaList.add(new AssessmentCriteriaSummary(getTemplateMaxScore(), sectionTemplate, criteriaTemplate,
                    outcomeAssessments));
        }
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

    public List<AssessmentCriteriaSummary> getCriteriaList() {
        return criteriaList;
    }

    public void setCriteriaList(List<AssessmentCriteriaSummary> criteriaList) {
        this.criteriaList = criteriaList;
    }

    public Double getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(Double displayOrder) {
        this.displayOrder = displayOrder;
    }

    private BigDecimal getTemplateMaxScore() {
        if (assessmentTemplate.getScores() == null) {
            return null;
        }
        return assessmentTemplate.getScores().stream()
                .map(s -> s.getScore())
                .max(Comparator.naturalOrder())
                .orElseGet(null);
    }

    public BigDecimal getMaxScore() {
        BigDecimal maxTemplateScore = getTemplateMaxScore();

        if (maxTemplateScore != null) {
            return new BigDecimal(criteriaList.size()).multiply(maxTemplateScore);
        }

        return null;
    }

    public BigDecimal getAverageScore() {
        return criteriaList.stream()
                .map(AssessmentCriteriaSummary::getAverageScore)
                .reduce(BigDecimal.ZERO, GlaUtils::addBigDecimals);
    }

    public BigDecimal getWeightedScore() {
        return criteriaList.stream()
                .map(AssessmentCriteriaSummary::getWeightedScore)
                .reduce(BigDecimal.ZERO, GlaUtils::addBigDecimals);
    }
}
