/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.project.funding

import com.fasterxml.jackson.annotation.JsonIgnore
import uk.gov.london.common.GlaUtils.addBigDecimals
import uk.gov.london.ops.project.StandardAttachment
import uk.gov.london.ops.project.claim.Claim
import java.math.BigDecimal
import java.util.*

enum class FundingClaimStatus {
    Paid, Processing, Claimable, Claimed, NotClaimable
}

class FundingSectionClaimsSummary(
        val nbActivitiesClaimed: Int,
        val totalCapitalClaimed: BigDecimal,
        val totalRevenueClaimed: BigDecimal
)

interface ClaimableFundingEntity {

    var claim: Claim?

    var status: FundingClaimStatus?

    var notClaimableReason: String?

    fun getSectionNumber(): Int?

    fun isMonetaryClaimRequired(): Boolean

    fun isEvidenceAttached(): Boolean

}

class FundingActivityLineItem : ClaimableFundingEntity {

    override var claim: Claim? = null

    override var status: FundingClaimStatus? = null

    override var notClaimableReason: String? = null

    var id: Int? = null

    var originalId: Int? = null
        get() {
            if (field == null) {
                return id
            } else {
                return field
            }
        }

    var year: Int? = null

    var quarter: Int? = null

    var externalId: Int? = null

    var categoryDescription: String? = null

    var capitalValue: BigDecimal? = null

    var capitalMatchFundValue: BigDecimal? = null

    var revenueValue: BigDecimal? = null

    var revenueMatchFundValue: BigDecimal? = null

    var name: String? = null

    var attachments: List<StandardAttachment> = ArrayList()

    val total: BigDecimal
        get() = addBigDecimals(capitalValue, capitalMatchFundValue, revenueValue, revenueMatchFundValue)

    val isClaimed: Boolean
        get() = claim != null

    override fun getSectionNumber(): Int? = this.quarter

    @JsonIgnore
    override fun isMonetaryClaimRequired(): Boolean {
        return this.capitalValue != null && BigDecimal.ZERO.compareTo(this.capitalValue) != 0 || this.revenueValue != null && BigDecimal.ZERO.compareTo(this.revenueValue) != 0
    }

    override fun isEvidenceAttached(): Boolean = this.attachments.isNotEmpty()

}

fun createFundingActivityLineItemFrom(fundingActivity: FundingActivity): FundingActivityLineItem {
    val lineItem = FundingActivityLineItem()
    lineItem.id = fundingActivity.id
    lineItem.originalId = fundingActivity.originalId
    lineItem.year = fundingActivity.year
    lineItem.quarter = fundingActivity.quarter
    lineItem.externalId = fundingActivity.externalId
    lineItem.categoryDescription = fundingActivity.categoryDescription
    lineItem.capitalValue = fundingActivity.capitalMainValue
    lineItem.capitalMatchFundValue = fundingActivity.capitalMatchFundValue
    lineItem.revenueValue = fundingActivity.revenueMainValue
    lineItem.revenueMatchFundValue = fundingActivity.revenueMatchFundValue
    lineItem.name = fundingActivity.name
    lineItem.attachments = fundingActivity.attachments
    return lineItem
}
