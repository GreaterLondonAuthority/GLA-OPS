/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.organisation.model

import java.time.OffsetDateTime
import javax.persistence.*

@Entity
@Table(name = "sap_id")
class SapIdEntity(

        @Id
        @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sap_id_seq_gen")
        @SequenceGenerator(name = "sap_id_seq_gen", sequenceName = "sap_id_seq", initialValue = 10000, allocationSize = 1)
        var id: Int? = null,

        @Column(name = "sap_id")
        var sapId: String? = null,

        @Column(name = "organisation_id")
        var organisationId: Int? = null,

        @Column(name = "description")
        var description: String? = null,

        @Column(name = "created_on")
        var createdOn: OffsetDateTime? = OffsetDateTime.now(),

        @Column(name = "is_default_sap_id")
        var defaultSapId: Boolean =false,

        @Transient
        var usedInProject: Boolean =false

) {
        constructor(sapId: String?, organisationId: Int?, description: String?, createdOn: OffsetDateTime?,
                    isDefaultSapId : Boolean) :
                this(null, sapId, organisationId, description, createdOn, isDefaultSapId)

        override fun equals(other: Any?): Boolean {
                if (this === other) return true
                if (javaClass != other?.javaClass) return false

                other as SapIdEntity

                if (sapId != other.sapId) return false
                if (description != other.description) return false

                return true
        }

        override fun hashCode(): Int {
                var result = sapId.hashCode()
                result = 31 * result + (description?.hashCode() ?: 0)
                return result
        }
}
