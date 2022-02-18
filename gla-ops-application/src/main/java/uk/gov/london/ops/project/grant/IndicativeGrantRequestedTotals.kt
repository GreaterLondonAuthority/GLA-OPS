package uk.gov.london.ops.project.grant

import uk.gov.london.common.GlaUtils
import uk.gov.london.ops.framework.OPSUtils.calculatePercentage
import java.math.BigDecimal

class IndicativeGrantRequestedTenureTotals(
        var totalUnits: Int? = null,
        var grantTotal: BigDecimal? = null,
        var totalSchemeCost: BigDecimal? = null
) {

    fun getGrantPerUnit(): Int? {
        var grantPerUnit: Int? = null
        if (grantTotal != null && totalUnits != null && totalUnits!! > 0) {
            grantPerUnit = grantTotal!!.divide(BigDecimal(totalUnits!!), 2).toInt()
        }
        return grantPerUnit
    }

    fun getTscPerUnit(): Int? {
        var tscPerUnit: Int? = null
        if (totalSchemeCost != null && totalUnits != null && totalUnits!! > 0) {
            tscPerUnit = totalSchemeCost!!.divide(BigDecimal(totalUnits!!), 0, BigDecimal.ROUND_HALF_UP).toInt()
        }
        return tscPerUnit
    }

    fun getGrantAsPercentageOfTsc(): Int? {
        var grantAsPercentageOfTsc: Int? = null
        if (grantTotal != null && totalSchemeCost != null && totalSchemeCost!!.compareTo(BigDecimal.ZERO) != 0) {
            grantAsPercentageOfTsc = calculatePercentage(grantTotal!!, totalSchemeCost, 2, BigDecimal.ROUND_HALF_UP)?.toInt()
        }
        return grantAsPercentageOfTsc
    }

}

class IndicativeCostsAndContributionsSummaryTotals{
    var totalCosts : BigDecimal

    var totalContributions : BigDecimal

    var totalEligibleGrant : BigDecimal

    constructor(costs: Set<AffordableHomesCostsAndContributions>,
                contributions: Set<AffordableHomesCostsAndContributions> ) {
        this.totalCosts = calcCosts(costs)
        this.totalContributions = calcCosts(contributions)
        this.totalEligibleGrant = totalCosts.subtract(totalContributions)

    }

    fun calcCosts(list: Set<AffordableHomesCostsAndContributions>): BigDecimal {
        return totalListEntries(list)
    }

    private fun totalListEntries(list: Set<AffordableHomesCostsAndContributions>): BigDecimal {
        return list.stream()
            .map { it.value ?: BigDecimal.ZERO }
            .reduce(
                BigDecimal.ZERO
            ) { bd1: BigDecimal, bd2: BigDecimal ->
                GlaUtils.addBigDecimals(
                    bd1,
                    bd2
                )
            }.setScale(0, BigDecimal.ROUND_HALF_UP)
    }

}


class IndicativeGrantRequestedTotals(
        var totalsByType: MutableMap<String, BigDecimal> = mutableMapOf(),
        var totalsByTenure: MutableMap<Int, IndicativeGrantRequestedTenureTotals> = mutableMapOf()
) {

    fun processEntry(entry: AffordableHomesEntry, type: AffordableHomesType) {
        if (type == entry.type && entry.units != null
                && entry.ofWhichCategory == null) {
            val units: Int = entry.units!!
            addValueToTotalsMap(BigDecimal(units), type.name, totalsByType)
            val tenureTotals = getTenureTotals(entry.tenureTypeId)
            if (tenureTotals.totalUnits == null) {
                tenureTotals.totalUnits = 0
            }
            tenureTotals.totalUnits = tenureTotals.totalUnits!! + units
        }
    }

    fun processEntry(entry: IndicativeGrantRequestedEntry) {
        addValueToTotalsMap(entry.value, entry.type, totalsByType)
        val tenureTotals = getTenureTotals(entry.tenureTypeId)
        if (entry.value != null) {
            if (entry.type == TOTAL_SCHEME_COST) {
                val existingTotalSchemeCost = if (tenureTotals.totalSchemeCost != null) tenureTotals.totalSchemeCost else BigDecimal.ZERO
                tenureTotals.totalSchemeCost = existingTotalSchemeCost!!.add(entry.value)
            }
            else {
                val existingGrantTotal = if (tenureTotals.grantTotal != null) tenureTotals.grantTotal else BigDecimal.ZERO
                tenureTotals.grantTotal = existingGrantTotal!!.add(entry.value)
            }
        }
    }

    private fun addValueToTotalsMap(value: BigDecimal?, key: String, map: MutableMap<String, BigDecimal>) {
        if (value != null) {
            map[key] = map.getOrDefault(key, BigDecimal.ZERO).add(value)
        }
    }

    private fun getTenureTotals(tenureTypeId: Int): IndicativeGrantRequestedTenureTotals {
        if (totalsByTenure[tenureTypeId] == null) {
            totalsByTenure[tenureTypeId] = IndicativeGrantRequestedTenureTotals()
        }
        return totalsByTenure[tenureTypeId]!!
    }
}
