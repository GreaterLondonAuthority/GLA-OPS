/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.organisation

import java.time.OffsetDateTime

data class Team(var id: Int? = null,
                val name: String? = null,
                val organisationId: Int? = null,
                val organisationName: String? = null,
                val status: OrganisationStatus? = null,
                val registrationAllowed: Boolean? = null,
                val skillsGatewayAccessAllowed: Boolean? = false,
                val members: Int? = 0,
                val createdBy: String? = null,
                val createdOn: OffsetDateTime? = null,
                val modifiedBy: String? = null,
                val modifiedOn: OffsetDateTime? = null
)
