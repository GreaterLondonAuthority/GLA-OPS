package uk.gov.london.ops.project.grant

import java.math.BigDecimal
import javax.persistence.*

enum class EntryType { Cost, Contribution }

@Entity(name = "costs_and_contributions")
class AffordableHomesCostsAndContributions(

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "costs_and_contributions_seq_gen")
    @SequenceGenerator(
        name = "costs_and_contributions_seq_gen",
        sequenceName = "costs_and_contributions_seq",
        initialValue = 10000,
        allocationSize = 1
    )
    var id: Int? = null,

    @Column(name = "description")
    val description: String? = null,

    @Column(name = "display_order")
    val displayOrder: Double? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "entry_type")
    val entryType: EntryType,

    @Column(name = "value")
    var value: BigDecimal? = null
) {
    constructor(entryType: EntryType, description: String?,displayOrder: Double?) :
            this(null, description, displayOrder, entryType, null)

    fun copy(): AffordableHomesCostsAndContributions {
        return AffordableHomesCostsAndContributions(null, description, displayOrder, entryType, value)
    }

}



