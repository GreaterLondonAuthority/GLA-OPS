/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.assessment.implementation;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import uk.gov.london.ops.assessment.AssessmentSummary;
import uk.gov.london.ops.framework.jpa.ReadOnlyRepository;
import uk.gov.london.ops.user.domain.User;

import java.util.List;

public interface AssessmentSummaryRepository extends ReadOnlyRepository<AssessmentSummary, Integer>, QuerydslPredicateExecutor<AssessmentSummary> {

    default Page<AssessmentSummary> findAll(User currentUser,
        String createdByName,
        String projectNameOrID,
        List<String> assessmentTemplates,
        List<String> assessmentStatuses,
        List<Integer> programmes,
        List<String> projectStatuses,
        Pageable pageable) {
        AssessmentSummaryPredicateBuilder query = new AssessmentSummaryPredicateBuilder();

        query.build(currentUser.getManagingOrganisationsIds());
        query.andCreatedBy(createdByName);
        query.andProjectNameOrID(projectNameOrID);
        query.andStatuses(assessmentStatuses);
        query.andAssessmentTemplates(assessmentTemplates);
        query.andProgrammes(programmes);
        query.andProjectStatuses(projectStatuses);
        return findAll(query.getPredicateBuilder(), pageable);
    }

}
