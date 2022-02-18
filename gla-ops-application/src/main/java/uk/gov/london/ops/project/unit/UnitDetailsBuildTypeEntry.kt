package uk.gov.london.ops.project.unit

import javax.persistence.*

@Entity(name = "units_details_build_type_entry")
class UnitDetailsBuildTypeEntry(

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "units_details_build_type_entry_seq_gen")
    @SequenceGenerator(name = "units_details_build_type_entry_seq_gen", sequenceName = "units_details_build_type_entry_seq", initialValue = 100, allocationSize = 1)
    var id: Int? = null,

    @Column(name = "category")
    var category: String? = null,

    @Column(name = "new_build_units")
    var newBuildUnits: Int? = null,

    @Column(name = "refurbished_units")
    var refurbishedUnits: Int? = null) {

    fun total(): Int {
        return (newBuildUnits ?: 0) + (refurbishedUnits ?: 0)
    }

    fun merge(updated: UnitDetailsBuildTypeEntry) {
        this.newBuildUnits = updated.newBuildUnits
        this.refurbishedUnits = updated.refurbishedUnits
    }

    fun copy(): UnitDetailsBuildTypeEntry {
        return UnitDetailsBuildTypeEntry(null, category, newBuildUnits, refurbishedUnits)
    }

}
