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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AssessmentOutcomeSummary {
    private List<AssessmentSectionSummary> sections = new ArrayList<>();
    private List<Assessment> outcomeAssessments = new ArrayList<>();

    public AssessmentOutcomeSummary() {
    }

    public AssessmentOutcomeSummary(AssessmentTemplate template, List<Assessment> outcomeAssessments) {
        List<AssessmentTemplateSection> sectionsWithScores = template.getSections().stream()
                .filter(s -> s.getCriteriaList().stream().anyMatch(c->CriteriaAnswerType.Score == c.getAnswerType()))
                .collect(Collectors.toList());

        for (AssessmentTemplateSection section : sectionsWithScores) {
            sections.add(new AssessmentSectionSummary(template, section, outcomeAssessments));
        }
        this.outcomeAssessments = outcomeAssessments;
    }


    public List<AssessmentSectionSummary> getSections() {
        return sections;
    }

    public void setSections(List<AssessmentSectionSummary> sections) {
        this.sections = sections;
    }

    public List<Assessment> getOutcomeAssessments() {
        return outcomeAssessments;
    }

    public void setOutcomeAssessments(List<Assessment> outcomeAssessments) {
        this.outcomeAssessments = outcomeAssessments;
    }

    public Integer getTotalSectionsWeight() {
        return sections.stream().map(AssessmentSectionSummary::getWeight).reduce(0, GlaUtils::nullSafeAdd);
    }

    public BigDecimal getTotalSectionsMaxScore() {
        return sections.stream().map(AssessmentSectionSummary::getMaxScore).reduce(BigDecimal.ZERO, GlaUtils::addBigDecimals);
    }

    public BigDecimal getTotalSectionsAverageScore() {
        return sections.stream().map(AssessmentSectionSummary::getAverageScore).reduce(BigDecimal.ZERO, GlaUtils::addBigDecimals);
    }

    public BigDecimal getTotalSectionsWeightedScore() {
        return sections.stream().map(AssessmentSectionSummary::getWeightedScore).reduce(BigDecimal.ZERO, GlaUtils::addBigDecimals);
    }

    public Map<Integer, BigDecimal> getTotalOutcomeAssessmentsScore() {
        Map<Integer, BigDecimal> totals = new HashMap<>();
        for (Assessment oa : outcomeAssessments) {
            totals.put(oa.getId(), oa.getSections().stream()
                    .flatMap(s -> s.getCriteriaList().stream())
                    .map(c -> c.getScore())
                    .reduce(BigDecimal.ZERO, GlaUtils::addBigDecimals));
        }
        return totals;
    }
}
