/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.assessment.implementation;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import uk.gov.london.ops.assessment.AssessmentStatus;
import uk.gov.london.ops.assessment.QAssessmentSummary;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static uk.gov.london.common.GlaUtils.parseInt;

public class AssessmentSummaryPredicateBuilder {

    private final BooleanBuilder predicateBuilder = new BooleanBuilder();

    public void build(Set<Integer> managingOrganisationsIds) {
        Predicate[] predicates = new Predicate[]{
                QAssessmentSummary.assessmentSummary.managingOrgId.in(managingOrganisationsIds),
        };
        predicateBuilder.andAnyOf(predicates);
    }

    public void andCreatedBy(String createdBy) {
        if (createdBy != null) {
            predicateBuilder.and(QAssessmentSummary.assessmentSummary.creator.containsIgnoreCase(createdBy));
        }
    }

    public void andProjectNameOrID(String projectNameOrId) {
        List<Predicate> predicates = new ArrayList<>();

        if (projectNameOrId != null) {
            predicates.add(QAssessmentSummary.assessmentSummary.projectTitle.containsIgnoreCase(projectNameOrId));
            Integer projectId = parseInt(projectNameOrId);
            if (projectId != null) {
                predicates.add(QAssessmentSummary.assessmentSummary.projectId.eq(projectId));
            }
        }

        predicateBuilder.andAnyOf(predicates.toArray(new Predicate[predicates.size()]));
    }

    public void andAssessmentTemplates(List<String> assessmentTemplates) {
        List<Predicate> predicates = new ArrayList<>();

        if (assessmentTemplates != null) {
            predicates.add(QAssessmentSummary.assessmentSummary.assessmentTemplate.in(assessmentTemplates));
        }

        predicateBuilder.andAnyOf(predicates.toArray(new Predicate[predicates.size()]));
    }

    public void andStatuses(List<String> statuses) {
        List<Predicate> predicates = new ArrayList<>();

        if (statuses != null) {
            List<AssessmentStatus> assessmentStatuses = statuses.stream().map(AssessmentStatus::valueOf).collect(Collectors.toList());
            predicates.add(QAssessmentSummary.assessmentSummary.status.in(assessmentStatuses));
        }

        predicateBuilder.andAnyOf(predicates.toArray(new Predicate[predicates.size()]));
    }

    public void andProgrammes(List<Integer> programmes) {
        List<Predicate> predicates = new ArrayList<>();

        if (programmes != null) {
            predicates.add(QAssessmentSummary.assessmentSummary.programmeId.in(programmes));
        }

        predicateBuilder.andAnyOf(predicates.toArray(new Predicate[predicates.size()]));
    }

    public void andProjectStatuses(List<String> projectStatuses) {
        List<Predicate> predicates = new ArrayList<>();

        if (projectStatuses != null) {
            predicates.add(QAssessmentSummary.assessmentSummary.projectStatus.in(projectStatuses));
        }

        predicateBuilder.andAnyOf(predicates.toArray(new Predicate[predicates.size()]));
    }

    public BooleanBuilder getPredicateBuilder() {
        return predicateBuilder;
    }

}

