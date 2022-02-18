package uk.gov.london.ops.payment.implementation.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import uk.gov.london.ops.payment.PaymentAuditItem

@Repository
interface PaymentAuditItemRepository : JpaRepository<PaymentAuditItem, Int> {

    fun findAllByPaymentIdOrderByActivityTimeDesc(id: Int): List<PaymentAuditItem>

    @Query(value = "delete from payment_audit_item", nativeQuery = true)
    @Modifying
    override fun deleteAll()

}
