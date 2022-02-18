package uk.gov.london.ops.notification.broadcast

import java.time.OffsetDateTime

class Broadcast(
        val id: Int? = null,
        val managingOrganisationId: Int? = null,
        val managingOrganisationName: String? = null,
        val createdByName: String? = null,
        val createdOn: OffsetDateTime? = null,
        val modifiedByName: String? = null,
        val modifiedOn: OffsetDateTime? = null,
        val status: String? = null,
        val mainProjectContacts: Boolean? = null,
        val secondaryProjectContacts: Boolean? = null,
        val organisationAdmins: Boolean? = null,
        val programmeId: Int? = null,
        val templateIds: List<Int>? = null,
        val projectStatus: String? = null,
        val subject: String? = null,
        val body: String? = null,
        val signOff: String? = null,
        val approverPrimaryOrg: String? = null,
        val canDelete: Boolean = false,
        val canApprove: Boolean = false
)
