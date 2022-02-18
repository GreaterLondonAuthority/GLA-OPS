/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.payment.implementation.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import uk.gov.london.ops.payment.LedgerStatus;
import uk.gov.london.ops.payment.QProjectLedgerEntry;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import static uk.gov.london.common.GlaUtils.parseInt;

class ProjectLedgerEntryPredicateBuilder {

    private final BooleanBuilder predicateBuilder = new BooleanBuilder();

    void andSearch(String projectIdOrName, String organisationName) {
        List<Predicate> predicates = new ArrayList<>();

        if (projectIdOrName != null) {
            predicates.add(QProjectLedgerEntry.projectLedgerEntry.projectName.containsIgnoreCase(projectIdOrName));
            Integer projectId = getProjectId(projectIdOrName);
            if (projectId != null) {
                predicates.add(QProjectLedgerEntry.projectLedgerEntry.projectId.eq(projectId));
            }
        } else if (organisationName != null) {
            predicates.add(QProjectLedgerEntry.projectLedgerEntry.vendorName.containsIgnoreCase(organisationName));
        }
        predicateBuilder.andAnyOf(predicates.toArray(new Predicate[predicates.size()]));
    }

    private Integer getProjectId(String project) {
        if (project.startsWith("P") || project.startsWith("p")) {
            project = project.substring(1);
        }
        return parseInt(project);
    }

    void andStatuses(List<LedgerStatus> statuses) {
        List<Predicate> predicates = new ArrayList<>();

        if (statuses != null) {
            predicates.add(QProjectLedgerEntry.projectLedgerEntry.ledgerStatus.in(statuses));
        }

        predicateBuilder.andAnyOf(predicates.toArray(new Predicate[predicates.size()]));
    }

    Predicate getPredicate() {
        return predicateBuilder.getValue();
    }

    void andPaymentSources(List<String> paymentSources) {
        List<Predicate> predicates = new ArrayList<>();

        if (paymentSources != null) {
            predicates.add(QProjectLedgerEntry.projectLedgerEntry.paymentSource.in(paymentSources));
        }

        predicateBuilder.andAnyOf(predicates.toArray(new Predicate[predicates.size()]));
    }

    void andCategories(List<String> categories) {
        if (categories != null) {
            predicateBuilder.and(QProjectLedgerEntry.projectLedgerEntry.category.in(categories));
        }
    }

    void andProgrammes(List<String> relevantProgrammes) {
        List<Predicate> predicates = new ArrayList<>();

        if (relevantProgrammes != null) {
            predicates.add(QProjectLedgerEntry.projectLedgerEntry.programmeName.in(relevantProgrammes));
        }

        predicateBuilder.andAnyOf(predicates.toArray(new Predicate[predicates.size()]));
    }

    void andAuthorisedDates(OffsetDateTime fromDate, OffsetDateTime toDate) {

        if (fromDate != null) {
            predicateBuilder.and(QProjectLedgerEntry.projectLedgerEntry.authorisedOn.goe(fromDate));
        }
        if (toDate != null) {
            predicateBuilder.and(QProjectLedgerEntry.projectLedgerEntry.authorisedOn.loe(toDate));
        }
    }
}
