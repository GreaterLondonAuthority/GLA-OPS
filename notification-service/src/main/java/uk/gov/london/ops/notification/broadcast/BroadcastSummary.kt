package uk.gov.london.ops.notification.broadcast

import java.time.OffsetDateTime

class BroadcastSummary(
        var id: Int? = null,
        val managingOrganisationName: String? = null,
        val createdByName: String? = null,
        val modifiedOn: OffsetDateTime? = null,
        val status: String? = null,
        val subject: String? = null
)
