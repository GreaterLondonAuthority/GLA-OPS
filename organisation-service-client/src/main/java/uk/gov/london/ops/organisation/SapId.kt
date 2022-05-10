/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.organisation

import java.time.OffsetDateTime

data class SapId(var sapId: String? = null,
                 var organisationId: Int? = null,
                 var description: String? = null,
                 var createdOn: OffsetDateTime? = null,
                 var isDefaultSapId: Boolean = false
)

