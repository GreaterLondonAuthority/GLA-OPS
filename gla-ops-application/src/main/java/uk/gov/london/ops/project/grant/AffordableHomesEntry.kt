package uk.gov.london.ops.project.grant

import javax.persistence.*

enum class AffordableHomesType {
    StartOnSite,
    Completion
}

@Entity(name = "affordable_homes_entry")
class AffordableHomesEntry(

        @Id
        @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "affordable_homes_entry_seq_gen")
        @SequenceGenerator(name = "affordable_homes_entry_seq_gen", sequenceName = "affordable_homes_entry_seq", initialValue = 10000, allocationSize = 1)
        var id: Int? = null,

        @Column(name = "year")
        val year: Int? = null,

        @Column(name = "tenure_type_id")
        var tenureTypeId: Int,

        @Enumerated(EnumType.STRING)
        @Column(name = "type")
        val type: AffordableHomesType,

        @Enumerated(EnumType.STRING)
        @Column(name = "of_which_category")
        val ofWhichCategory: AffordableHomesOfWhichCategory? = null,

        @Column(name = "units")
        var units: Int? = null

) {
    constructor(year: Int, tenureTypeId: Int, type: AffordableHomesType) : this(null, year, tenureTypeId, type, null, null)

    constructor(year: Int, tenureTypeId: Int, type: AffordableHomesType, units: Int) : this(null, year, tenureTypeId, type, null, units)

    constructor(id: Int, year: Int, tenureTypeId: Int, type: AffordableHomesType, units: Int) : this(id, year, tenureTypeId, type, null, units)

    constructor(tenureTypeId: Int, type: AffordableHomesType, ofWhichCategory: AffordableHomesOfWhichCategory)
            : this(null, null, tenureTypeId, type, ofWhichCategory, null)

    fun copy(): AffordableHomesEntry {
        return AffordableHomesEntry(null, year, tenureTypeId, type, ofWhichCategory, units)
    }

}
