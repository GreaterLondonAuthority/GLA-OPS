/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.audit

import uk.gov.london.ops.framework.EntityType
import java.math.BigDecimal
import java.time.OffsetDateTime

class AuditableActivity(
        val id: Int?,
        val userName: String? = null,
        val affectedUserName: String? = null,
        val organisationId: Int? = null,
        val amount: BigDecimal? = null,
        val timestamp: OffsetDateTime? = null,
        val summary: String? = null,
        val entityType: EntityType? = null,
        val entityId: Int? = null,
        val type: ActivityType? = null
)
