/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import uk.gov.london.ops.domain.finance.LedgerStatus;
import uk.gov.london.ops.domain.finance.LedgerType;
import uk.gov.london.ops.domain.finance.PaymentSummary;
import uk.gov.london.ops.domain.finance.QPaymentSummary;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.OffsetDateTime;
import java.util.List;

public interface PaymentSummaryRepository extends JpaRepository<PaymentSummary, Integer>, QueryDslPredicateExecutor<PaymentSummary> {

    default Page<PaymentSummary> findAll(String projectIdOrName,
                                             String organisationName,
                                             List<LedgerType> relevantSources,
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
        query.andSearch(projectIdOrName, organisationName);
        query.andStatuses(relevantStatuses);
        query.andSources(relevantSources);
        query.andCategories(categories);
        query.andProgrammes(relevantProgrammes);
        query.andAuthorisedDates(fromDate, toDate);
        query.andFilterReclaims(paymentDirection);
        query.andOrganisations(orgIds);

        return findAll(query.getPredicate(), pageable);
    }

}
