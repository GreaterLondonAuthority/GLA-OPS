package uk.gov.london.ops.project.grant

import uk.gov.london.ops.framework.ComparableItem
import java.math.BigDecimal
import javax.persistence.*

const val TOTAL_SCHEME_COST = "TOTAL_SCHEME_COST"

@Entity(name = "indicative_grant_request_entry")
class IndicativeGrantRequestedEntry(

        @Id
        @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "indicative_grant_request_entry_seq_gen")
        @SequenceGenerator(name = "indicative_grant_request_entry_seq_gen", sequenceName = "indicative_grant_request_entry_seq", initialValue = 10000, allocationSize = 1)
        var id: Int? = null,

        @Column(name = "tenure_type_id")
        var tenureTypeId: Int,

        @Column(name = "type")
        var type: String,

        @Column(name = "value")
        var value: BigDecimal? = null

): ComparableItem {

    constructor(tenureTypeId: Int, type: String) : this(null, tenureTypeId, type, null)

    fun copy(): IndicativeGrantRequestedEntry {
        return IndicativeGrantRequestedEntry(null, tenureTypeId, type, value)
    }

    override fun getComparisonId(): String {
        return "$tenureTypeId:$type"
    }
}
