/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.payment

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.london.ops.framework.Environment
import uk.gov.london.ops.payment.implementation.repository.PaymentAuditItemRepository
import uk.gov.london.ops.user.UserService

@Service
class PaymentAuditService @Autowired constructor (
        private var paymentAuditItemRepository: PaymentAuditItemRepository,
        private var userService: UserService,
        private var environment: Environment)  {


    fun getAllAuditItems(paymentId: Int): List<PaymentAuditItem> {
        val items = paymentAuditItemRepository.findAllByPaymentIdOrderByActivityTimeDesc(paymentId)
        for (item in items) {
            if (item.username != null) {
                item.userFullName = userService.getUserFullName(item.username)
            }
        }
        return items
    }

    fun recordPaymentAuditItem(payment: ProjectLedgerEntry, type: PaymentAuditItemType) {
        this.recordPaymentAuditItem(payment, type, null)
    }

    fun recordPaymentAuditItem(payment: ProjectLedgerEntry, type: PaymentAuditItemType, xml:String?) {
        val auditItem = PaymentAuditItem(payment.id, environment.now(), type)
        auditItem.username = userService.currentUsername()
        auditItem.xmlPayload = xml
        savePaymentAuditItem(auditItem)
    }

    fun savePaymentAuditItem(item: PaymentAuditItem) {
        paymentAuditItemRepository.save(item)
    }

    fun deleteAll() {
        if (environment.isTestEnvironment) {
            paymentAuditItemRepository.deleteAll()
        }
    }

}