package uk.gov.london.ops.project.grant

class AffordableHomesTotals(
        var totalsByYear: MutableMap<Int, Int> = mutableMapOf(),
        var totalsByOfWhichCategory: MutableMap<String, Int> = mutableMapOf(),
        var totalsByTenure: MutableMap<Int, Int> = mutableMapOf(),
        var overallTotal: Int = 0
) {

    fun processEntry(entry: AffordableHomesEntry) {
        val units: Int = if (entry.units != null) entry.units!! else 0
        if (entry.ofWhichCategory == null) {
            addUnitsToTotalsMap(units, entry.year, totalsByYear)
            addUnitsToTotalsMap(units, entry.tenureTypeId, totalsByTenure)
            overallTotal += units
        } else {
            addUnitsToTotalsMap(units, entry.ofWhichCategory!!.name, totalsByOfWhichCategory)
        }
    }

    private fun <T> addUnitsToTotalsMap(units: Int, key: T?, map: MutableMap<T, Int>) {
        if (key != null) {
            if (map[key] == null) {
                map[key] = 0
            }
            map[key] = map[key]!! + units
        }
    }

}
