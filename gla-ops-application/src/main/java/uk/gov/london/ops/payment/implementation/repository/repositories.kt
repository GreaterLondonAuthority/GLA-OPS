package uk.gov.london.ops.payment.implementation.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import uk.gov.london.ops.payment.PaymentAuditItem
import uk.gov.london.ops.payment.PaymentGroup
import uk.gov.london.ops.payment.PaymentSource
import uk.gov.london.ops.payment.SapData

interface PaymentAuditItemRepository : JpaRepository<PaymentAuditItem, Int> {
    fun findAllByPaymentIdOrderByActivityTimeDesc(id: Int): List<PaymentAuditItem>

    @Query(value = "delete from payment_audit_item", nativeQuery = true)
    @Modifying
    override fun deleteAll()

}

interface PaymentSourceRepository : JpaRepository<PaymentSource, String>

internal interface SapDataRepository : JpaRepository<SapData?, Int?> {
    fun findAllByProcessed(processed: Boolean): List<SapData?>?
    fun countByFileName(fileName: String?): Long?
}

internal interface PaymentGroupRepository : JpaRepository<PaymentGroup?, Int?> {
    @Query(value = "select distinct(pg.id), pg.decline_comments, pg.interest_assessed, pg.decline_reason , pg.approval_requested_by from payment_group pg inner join payment_group_payment pgp on pg.id = pgp.group_id inner join PROJECT_LEDGER_ENTRY ple on pgp.payment_id = ple.id and ple.ledger_status in (?1)", nativeQuery = true)
    fun findAllByStatusIn(vararg status: String?): List<PaymentGroup?>?

    @Query("select pg from uk.gov.london.ops.payment.PaymentGroup pg join pg.payments p where p.id = ?1")
    fun findPaymentGroupByPaymentId(id: Int?): PaymentGroup?

    @Query("select pg from uk.gov.london.ops.payment.PaymentGroup pg join pg.ledgerEntries ple where ple.blockId = ?1")
    fun findAllByBlockId(blockId: Int?): Set<PaymentGroup?>?
}