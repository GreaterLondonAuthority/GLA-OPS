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
import uk.gov.london.ops.payment.QPaymentSummary;
import uk.gov.london.ops.project.accesscontrol.DefaultAccessControlSummary;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import static uk.gov.london.common.GlaUtils.parseInt;

class PaymentSummaryPredicateBuilder {

    private final BooleanBuilder predicateBuilder = new BooleanBuilder();

    void andSearch(String projectIdOrName, String organisationIdOrName, String programmeName,
                   String sapVendorId) {
        List<Predicate> predicates = new ArrayList<>();

        if (projectIdOrName != null) {
            predicates.add(QPaymentSummary.paymentSummary.projectName.containsIgnoreCase(projectIdOrName));
            Integer projectId = getProjectId(projectIdOrName);
            if (projectId != null) {
                predicates.add(QPaymentSummary.paymentSummary.projectId.eq(projectId));
            }
        } else if (organisationIdOrName != null) {
            predicates.add(QPaymentSummary.paymentSummary.vendorName.containsIgnoreCase(organisationIdOrName));
            Integer organisationId = parseInt(organisationIdOrName);
            if (organisationId != null) {
                predicates.add(QPaymentSummary.paymentSummary.organisationId.eq(organisationId));
            }
        } else if (programmeName != null) {
            predicates.add(QPaymentSummary.paymentSummary.programmeName.containsIgnoreCase(programmeName));
        } else if (sapVendorId != null) {
            predicates.add(QPaymentSummary.paymentSummary.sapVendorId.equalsIgnoreCase(sapVendorId));
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
            predicates.add(QPaymentSummary.paymentSummary.ledgerStatus.in(statuses));
        }

        predicateBuilder.andAnyOf(predicates.toArray(new Predicate[predicates.size()]));
    }

    Predicate getPredicate() {
        return predicateBuilder.getValue();
    }

    void andPaymentSources(List<String> paymentSources) {
        List<Predicate> predicates = new ArrayList<>();

        if (paymentSources != null) {
            predicates.add(QPaymentSummary.paymentSummary.paymentSource.in(paymentSources));
        }

        predicateBuilder.andAnyOf(predicates.toArray(new Predicate[predicates.size()]));
    }

    void andCategories(List<String> categories) {
        if (categories != null) {
            predicateBuilder.and(QPaymentSummary.paymentSummary.category.in(categories));
        }
    }

    void andProgrammes(List<String> relevantProgrammes) {
        List<Predicate> predicates = new ArrayList<>();

        if (relevantProgrammes != null) {
            predicates.add(QPaymentSummary.paymentSummary.programmeName.in(relevantProgrammes));
        }

        predicateBuilder.andAnyOf(predicates.toArray(new Predicate[predicates.size()]));
    }

    void andManagingOrganisations(List<Integer> managingOrganisations) {
        List<Predicate> predicates = new ArrayList<>();

        if (managingOrganisations != null) {
            predicates.add(QPaymentSummary.paymentSummary.managingOrganisation.id.in(managingOrganisations));
        }

        predicateBuilder.andAnyOf(predicates.toArray(new Predicate[predicates.size()]));
    }

    void andAuthorisedDates(OffsetDateTime fromDate, OffsetDateTime toDate) {

        if (fromDate != null) {
            predicateBuilder.and(QPaymentSummary.paymentSummary.authorisedOn.goe(fromDate));
        }
        if (toDate != null) {
            predicateBuilder.and(QPaymentSummary.paymentSummary.authorisedOn.loe(toDate));
        }
    }

    void andOrganisations(List<Integer> organisations, List<DefaultAccessControlSummary> dac) {
        BooleanBuilder outer = new BooleanBuilder();
        for (DefaultAccessControlSummary defaultAccess : dac) {
            BooleanBuilder defaultAccessBuilder = new BooleanBuilder();
            defaultAccessBuilder.and(QPaymentSummary.paymentSummary.managingOrganisation.id
                    .eq(defaultAccess.getManagingOrganisationId()));
            defaultAccessBuilder.and(QPaymentSummary.paymentSummary.programmeId.eq(defaultAccess.getProgrammeId()));
            defaultAccessBuilder.and(QPaymentSummary.paymentSummary.templateId.eq(defaultAccess.getTemplateId()));
            outer.or(defaultAccessBuilder);
        }

        outer.or(QPaymentSummary.paymentSummary.organisationId.in(organisations));
        outer.or(QPaymentSummary.paymentSummary.managingOrganisation.id.in(organisations));
        predicateBuilder.andAnyOf(outer);
    }


    void andFilterReclaims(List<String> paymentDirection) {
if (paymentDirection != null && paymentDirection.size() == 1) {
            if (paymentDirection.get(0).equalsIgnoreCase("OUT")) {
                predicateBuilder.and(QPaymentSummary.paymentSummary.reclaimOfPaymentId.isNull());
            } else if (paymentDirection.get(0).equalsIgnoreCase("IN")) {
                predicateBuilder.and(QPaymentSummary.paymentSummary.reclaimOfPaymentId.isNotNull());
            }
        }
    }
}
