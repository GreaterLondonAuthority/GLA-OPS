/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.payment.implementation.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import uk.gov.london.ops.payment.LedgerStatus;
import uk.gov.london.ops.payment.PaymentSource;
import uk.gov.london.ops.payment.PaymentSummary;
import uk.gov.london.ops.payment.QPaymentSummary;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;

public interface PaymentSummaryRepository extends JpaRepository<PaymentSummary, Integer>, QuerydslPredicateExecutor<PaymentSummary> {

    List<PaymentSummary> findAllByProjectIdAndLedgerStatusIn(Integer projectId, Set<LedgerStatus> statuses);

    default Page<PaymentSummary> findAll(String projectIdOrName,
                                             String organisationName,
                                             String programmeName,
                                             List<PaymentSource> paymentSources,
                                             List<LedgerStatus> relevantStatuses,
                                             List<String> categories,
                                             List<String> relevantProgrammes,
                                             OffsetDateTime fromDate,
                                             OffsetDateTime toDate,
                                             List<Integer> orgIds,
                                             List<String> paymentDirection,
                                             Pageable pageable) {
        QPaymentSummary query = new QPaymentSummary();
//        if (!currentUser.isGla()) {
//            query.withOrganisations(currentUser.getOrganisationIds());
//        }
        query.andSearch(projectIdOrName, organisationName, programmeName);
        query.andStatuses(relevantStatuses);
        query.andPaymentSources(paymentSources);
        query.andCategories(categories);
        query.andProgrammes(relevantProgrammes);
        query.andAuthorisedDates(fromDate, toDate);
        query.andFilterReclaims(paymentDirection);
        query.andOrganisations(orgIds);

        return findAll(query.getPredicate(), pageable);
    }

}
