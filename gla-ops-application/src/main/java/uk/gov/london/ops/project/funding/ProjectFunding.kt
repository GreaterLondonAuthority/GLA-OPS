package uk.gov.london.ops.project.funding

import uk.gov.london.common.GlaUtils
import java.math.BigDecimal
import java.util.*

class ProjectFunding  {

    var previousFundingTotals: FundingTotals?  = null;

    var fundingByYear: Map<Integer, FundingYearBreakdown> = mutableMapOf()

    fun getTotalProjectFunding() : FundingTotals {
        var totalCapitalValue = BigDecimal.ZERO
        var totalCapitalMatchFund = BigDecimal.ZERO
        var totalRevenueValue = BigDecimal.ZERO
        var totalRevenueMatchFund = BigDecimal.ZERO

        for (fundingYearBreakdown in fundingByYear.values) {
            totalCapitalValue = totalCapitalValue.add(fundingYearBreakdown.getTotalCapitalValue())
            totalCapitalMatchFund = totalCapitalMatchFund.add(fundingYearBreakdown.getTotalCapitalMatchFund())
            totalRevenueValue = totalRevenueValue.add(fundingYearBreakdown.getTotalRevenueValue())
            totalRevenueMatchFund = totalRevenueMatchFund.add(fundingYearBreakdown.getTotalRevenueMatchFund())
        }

        return FundingTotals(totalCapitalValue, totalCapitalMatchFund, totalRevenueValue, totalRevenueMatchFund)
    }




}

class FundingTotals internal constructor(val totalCapitalValue: BigDecimal?,
                                         val totalCapitalMatchFund: BigDecimal?,
                                         val totalRevenueValue: BigDecimal?,
                                         val totalRevenueMatchFund: BigDecimal?) {
    val totalProjectBudget: BigDecimal
        get() = GlaUtils.addBigDecimals(totalCapitalValue, totalCapitalMatchFund, totalRevenueValue, totalRevenueMatchFund)

}

class FundingYearBreakdown(val year: Int, activities: List<FundingActivity>) : FundingTotalsWrapper(), Comparable<FundingActivity> {

    var previousYearlyTotal: FundingTotalsWrapper? = null;

    private val sections: MutableMap<Int, FundingSection> = HashMap()

    fun getSections(): List<FundingSection> {
        return sections.values.sortedBy { it.getSectionNumber() };
    }

    fun getSection(sectionNumber: Int?): FundingSection? {
        return sections[sectionNumber]
    }

    public override fun addActivity(activity: FundingActivity) {
        super.addActivity(activity)
        if (!sections.containsKey(activity.quarter)) {
            sections[activity.quarter] = FundingSection(activity.quarter)
        }
        sections[activity.quarter]!!.addActivity(activity)
    }

    init {
        for (activity in activities) {
            addActivity(activity)
        }
    }

    override fun compareTo(other: FundingActivity): Int {
        return other.year.compareTo(this.year)
    }

}
