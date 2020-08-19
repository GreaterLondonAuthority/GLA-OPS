/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.payment

import java.time.OffsetDateTime
import javax.persistence.*

enum class PaymentAuditItemType {
        Created, Authorised, Declined, Sent, Modified, Resent, Acknowledged, UnderReview, Cleared, SupplierError
}

/**
 * Record of a payment activity as it gets approves processes through sap statuses etc
 *
 * @author Chris Melville
 */
@Entity(name = "payment_audit_item")
class PaymentAuditItem (

    var paymentId: Int,

    var activityTime: OffsetDateTime,

    @Enumerated(EnumType.STRING)
    var type: PaymentAuditItemType

) {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "payment_audit_seq_gen")
    @SequenceGenerator(name = "payment_audit_seq_gen", sequenceName = "payment_audit_seq", initialValue = 100001, allocationSize = 1)
    var id: Int? = null

    var xmlPayload: String? = null


    var username: String? = null

    @Transient
    var userFullName: String? = null
}