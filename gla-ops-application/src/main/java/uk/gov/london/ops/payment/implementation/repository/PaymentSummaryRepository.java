/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.payment.implementation.repository;

import com.querydsl.core.types.Predicate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import uk.gov.london.ops.payment.LedgerStatus;
import uk.gov.london.ops.payment.PaymentSummary;
import uk.gov.london.ops.project.accesscontrol.DefaultAccessControlSummary;

public interface PaymentSummaryRepository extends JpaRepository<PaymentSummary, Integer>, QuerydslPredicateExecutor<PaymentSummary> {

    List<PaymentSummary> findAllByProjectIdAndLedgerStatusIn(Integer projectId, Set<LedgerStatus> statuses);

    default Page<PaymentSummary> findAll(String projectIdOrName,
                                             String organisationName,
                                             String programmeName,
                                             List<String> paymentSources,
                                             List<LedgerStatus> relevantStatuses,
                                             List<String> categories,
                                             List<String> relevantProgrammes,
                                             OffsetDateTime fromDate,
                                             OffsetDateTime toDate,
                                             List<Integer> organisations,
                                             List<DefaultAccessControlSummary> defaultAccess,
                                             List<String> paymentDirection,
                                             Pageable pageable) {
        PaymentSummaryPredicateBuilder query = new PaymentSummaryPredicateBuilder();
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
        query.andOrganisations(organisations, defaultAccess);

        Predicate predicate = query.getPredicate();
        if (predicate != null) {
            return findAll(predicate, pageable);
        }
        else {
            return findAll(pageable);
        }
    }

}
