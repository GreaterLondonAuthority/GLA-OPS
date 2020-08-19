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
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

public class AssessmentCriteriaSummary {
    private Integer id;
    private String title;
    private Integer weight;
    private BigDecimal maxScore;
    private Double displayOrder;

    private List<Assessment> outcomeAssessments;
    private List<AssessmentCriteria> assessmentCriteriaList;


    public AssessmentCriteriaSummary() {
    }

    public AssessmentCriteriaSummary(BigDecimal templateMaxScore, AssessmentTemplateSection sectionTemplate, AssessmentTemplateCriteria criteriaTemplate, List<Assessment> outcomeAssessments) {
        this.id = criteriaTemplate.getId();
        this.title = criteriaTemplate.getTitle();
        this.weight = criteriaTemplate.getWeight();
        this.maxScore = templateMaxScore;
        this.outcomeAssessments = outcomeAssessments;
        this.assessmentCriteriaList = getAssessmentCriteriaList(sectionTemplate.getId(), criteriaTemplate.getId(), outcomeAssessments);
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

    public List<AssessmentCriteria> getAssessmentCriteriaList() {
        return assessmentCriteriaList;
    }

    public void setAssessmentCriteriaList(List<AssessmentCriteria> assessmentCriteriaList) {
        this.assessmentCriteriaList = assessmentCriteriaList;
    }

    public Double getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(Double displayOrder) {
        this.displayOrder = displayOrder;
    }

    public BigDecimal getMaxScore() {
        return maxScore;
    }

    public void setMaxScore(BigDecimal maxScore) {
        this.maxScore = maxScore;
    }

    private List<AssessmentSection> getAssessmentSections(Integer sectionTemplateId, List<Assessment> outcomeAssessments) {
        return outcomeAssessments.stream()
                .flatMap(a -> a.getSections().stream())
                .filter(s -> s.getSectionTemplate().getId().equals(sectionTemplateId))
                .collect(Collectors.toList());
    }

    private List<AssessmentCriteria> getAssessmentCriteriaList(Integer sectionTemplateId, Integer criteriaTemplateId, List<Assessment> outcomeAssessments) {
        List<AssessmentSection> assessmentSections = getAssessmentSections(sectionTemplateId, outcomeAssessments);

        return assessmentSections.stream()
                .flatMap(section -> section.getCriteriaList().stream())
                .filter(c -> c.getCriteriaTemplate().getId().equals(criteriaTemplateId))
                .collect(Collectors.toList());
    }


    public BigDecimal getAverageScore() {
        List<AssessmentCriteria> scoreCriteriaList = assessmentCriteriaList.stream()
                .filter(c -> CriteriaAnswerType.Score == c.getAnswerType() && isAssessmentComplete(c))
                .collect(Collectors.toList());

        if (scoreCriteriaList == null || scoreCriteriaList.size() == 0) {
            return null;
        }

        BigDecimal sum = scoreCriteriaList.stream().map(AssessmentCriteria::getScore).reduce(BigDecimal.ZERO, GlaUtils::addBigDecimals);
        return sum.divide(new BigDecimal(scoreCriteriaList.size()), 4, RoundingMode.HALF_UP);
    }

    private boolean isAssessmentComplete(AssessmentCriteria assessmentCriteria) {
        if (outcomeAssessments == null || outcomeAssessments.size() == 0) {
            return false;
        }

        return outcomeAssessments.stream()
                .anyMatch(oa -> AssessmentStatus.Completed == oa.getStatus() && oa.getSections().stream()
                        .flatMap(s -> s.getCriteriaList().stream()).anyMatch(ac -> ac.getId().equals(assessmentCriteria.getId())));

    }

    public BigDecimal getWeightedScore() {
        BigDecimal avgScore = getAverageScore();
        if (avgScore != null && maxScore != null && BigDecimal.ZERO.compareTo(maxScore) != 0 && weight != null) {
            return avgScore.divide(maxScore, 4, RoundingMode.HALF_UP).multiply(new BigDecimal(weight));
        }
        return null;
    }
}
