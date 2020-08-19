/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.project.implementation.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import uk.gov.london.ops.project.QProjectSummary;

import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.collections.CollectionUtils.isNotEmpty;

class ProjectSummaryQueryBuilder {

    private final BooleanBuilder predicateBuilder = new BooleanBuilder();

    Predicate getPredicate() {
        return predicateBuilder.getValue();
    }

    ProjectSummaryQueryBuilder withAclUser(String username) {
        predicateBuilder.and(QProjectSummary.projectSummary.aclUser.eq(username));
        return this;
    }

    ProjectSummaryQueryBuilder withProjectIdOrName(Integer projectId, String projectName) {
        if (projectId != null || projectName != null) {
            List<Predicate> projectPredicates = new ArrayList<>();

            if (projectId != null) {
                projectPredicates.add(QProjectSummary.projectSummary.id.eq(projectId));
            }

            if (projectName != null) {
                projectPredicates.add(QProjectSummary.projectSummary.title.containsIgnoreCase(projectName));
            }

            predicateBuilder.andAnyOf(projectPredicates.toArray(new Predicate[projectPredicates.size()]));
        }
        return this;
    }

    ProjectSummaryQueryBuilder withOrganisationIdOrName(Integer organisationId, String organisationName) {
        if (organisationId != null || organisationName != null) {
            List<Predicate> organisationPredicates = new ArrayList<>();

            if (organisationId != null) {
                organisationPredicates.add(QProjectSummary.projectSummary.orgId.eq(organisationId));
            }

            if (organisationName != null) {
                organisationPredicates.add(QProjectSummary.projectSummary.orgName.containsIgnoreCase(organisationName));
            }

            predicateBuilder.andAnyOf(organisationPredicates.toArray(new Predicate[organisationPredicates.size()]));
        }
        return this;
    }

    ProjectSummaryQueryBuilder withProgrammeIdOrName(Integer programmeId, String programmeName) {
        if (programmeId != null || programmeName != null) {
            List<Predicate> programmePredicate = new ArrayList<>();

            if (programmeId != null) {
                programmePredicate.add(QProjectSummary.projectSummary.programmeId.eq(programmeId));
            }

            if (programmeName != null) {
                programmePredicate.add(QProjectSummary.projectSummary.programmeName.containsIgnoreCase(programmeName));
            }

            predicateBuilder.andAnyOf(programmePredicate.toArray(new Predicate[programmePredicate.size()]));
        }
        return this;
    }

    ProjectSummaryQueryBuilder withProgrammes(List<Integer> programmes) {
        if (isNotEmpty(programmes)) {
            predicateBuilder.and(QProjectSummary.projectSummary.programmeId.in(programmes));
        }
        return this;
    }

    ProjectSummaryQueryBuilder withTemplates(List<Integer> templates) {
        if (isNotEmpty(templates)) {
            predicateBuilder.and(QProjectSummary.projectSummary.templateId.in(templates));
        }
        return this;
    }

    ProjectSummaryQueryBuilder withStates(List<String> states) {
        if (isNotEmpty(states)) {
            predicateBuilder.and(QProjectSummary.projectSummary.state.in(states));
        }
        return this;
    }

    ProjectSummaryQueryBuilder withUserWatchingProject(String watchingProjectUsername) {
        if (watchingProjectUsername != null) {
            List<Predicate> watchingPredicates = new ArrayList<>();
            watchingPredicates.add(QProjectSummary.projectSummary.subscriptions.containsIgnoreCase("|" + watchingProjectUsername));
            predicateBuilder.andAnyOf(watchingPredicates.toArray(new Predicate[watchingPredicates.size()]));
        }
        return this;
    }

}
