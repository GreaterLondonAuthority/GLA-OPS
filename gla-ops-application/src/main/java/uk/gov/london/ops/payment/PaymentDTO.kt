package uk.gov.london.ops.payment

import uk.gov.london.ops.organisation.Organisation
import uk.gov.london.ops.organisation.model.OrganisationEntity
import uk.gov.london.ops.refdata.CategoryValue
import java.math.BigDecimal
import java.time.OffsetDateTime
import java.util.*

data class PaymentGroupPayment (val organisationId: Int, val managingOrganisationId: Int, val ledgerStatus: String, val ledgerType: String, val paymentGroupId: Int, val declineComments: String?,
                                val declineReason: String?, val approvalRequestedBy: String?, val interestAssessed:  Boolean?,
                                val comments: String?, val paymentsOnlyApproval: Boolean?,  val paymentId: Int,
                                val projectId: Int, val projectName: String, val companyName: String,
                                val programmeName: String, val vendorName: String, val category: String,
                                val subcategory: String, val orgSapId: String?, val sapVendorId: String?, val ledgerSource: String?,
                                val interestPayment: Boolean, val interest: BigDecimal?,
                                val amount: BigDecimal, val reclaimOfPaymentId: Int?,  val paymentSource: String?,
                                val approvedThreshold: Long?, val modifiedBy: String?, val createdBy: String?
                                ) {

    fun createPaymentGroup(): PaymentGroupEntity {
        val pg = PaymentGroupEntity()
        pg.id = paymentGroupId
        pg.comments = comments
        pg.declineComments = declineComments
        pg.interestAssessed = interestAssessed
        pg.approvalRequestedBy = approvalRequestedBy
        pg.paymentsOnlyApproval = paymentsOnlyApproval
        return pg;
    }

    fun createProjectLedgerEntry(): ProjectLedgerEntry {
        val ple = ProjectLedgerEntry()
        ple.id = paymentId
        ple.organisationId = organisationId
        ple.managingOrganisation = OrganisationEntity(managingOrganisationId, "");
        ple.projectId = projectId
        ple.category = category
        ple.subCategory = subcategory
        ple.interest = interest
        ple.updateValue(amount);
        ple.ledgerSource = ledgerSource?.let { LedgerSource.valueOf(it) }
        ple.projectName = projectName
        ple.programmeName = programmeName
        ple.vendorName = vendorName
        ple.thresholdValue = approvedThreshold
        ple.ledgerStatus = LedgerStatus.valueOf(ledgerStatus)
        ple.ledgerType = LedgerType.valueOf(ledgerType)
        ple.paymentSource = paymentSource
        ple.reclaimOfPaymentId = reclaimOfPaymentId
        ple.sapVendorId = sapVendorId
        ple.isInterestPayment = interestPayment
        ple.modifiedBy = modifiedBy
        ple.createdBy = createdBy
        return ple
    }

}
