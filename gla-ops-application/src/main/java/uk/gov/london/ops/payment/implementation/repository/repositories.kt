package uk.gov.london.ops.payment.implementation.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.london.ops.payment.*
import uk.gov.london.ops.project.ProjectModel

internal interface SapDataRepository : JpaRepository<SapData?, Int?> {
    fun findAllByProcessed(processed: Boolean): List<SapData?>?
    fun countByFileName(fileName: String?): Long?
}

internal interface PaymentGroupRepository : JpaRepository<PaymentGroupEntity?, Int?> {
    @Query(value = "select distinct(pg.id), pg.decline_comments, pg.interest_assessed, pg.decline_reason, pg.comments, pg.payments_only_approval, pg.approval_requested_by from payment_group pg inner join payment_group_payment"
            + " pgp on pg.id = pgp.group_id inner join PROJECT_LEDGER_ENTRY ple on pgp.payment_id = ple.id and ple.ledger_status in (?1)", nativeQuery = true)
    fun findAllByStatusIn(vararg status: String?): List<PaymentGroupEntity?>?

    @Query("select pg from uk.gov.london.ops.payment.PaymentGroupEntity pg join pg.payments p where p.id = ?1")
    fun findPaymentGroupByPaymentId(id: Int?): PaymentGroupEntity?

    @Query("select pg from uk.gov.london.ops.payment.PaymentGroupEntity pg join pg.ledgerEntries ple where ple.blockId = ?1")
    fun findAllByBlockId(blockId: Int?): Set<PaymentGroupEntity?>?

    @Query(nativeQuery=true)
    fun findAllWithPayments(username: String, isOpsAdmin: Boolean, organisationIds: List<Int>, statuses: List<String>): List<PaymentGroupPayment>
}
