/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.payment.implementation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import uk.gov.london.ops.payment.PaymentGroup;

import java.util.List;
import java.util.Set;

public interface PaymentGroupRepository extends JpaRepository<PaymentGroup, Integer> {

    @Query(value = "select distinct(pg.id), pg.decline_comments, pg.interest_assessed, pg.decline_reason , pg.approval_requested_by from payment_group pg inner join payment_group_payment pgp on pg.id = pgp.group_id inner join PROJECT_LEDGER_ENTRY ple on pgp.payment_id = ple.id and ple.ledger_status in (?1)", nativeQuery = true)
    List<PaymentGroup> findAllByStatusIn(String... status);

    @Query("select pg from uk.gov.london.ops.payment.PaymentGroup pg join pg.payments p where p.id = ?1")
    PaymentGroup findPaymentGroupByPaymentId(Integer id);

    @Query("select pg from uk.gov.london.ops.payment.PaymentGroup pg join pg.ledgerEntries ple where ple.blockId = ?1")
    Set<PaymentGroup> findAllByBlockId(Integer blockId);

}
