/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.notification

import uk.gov.london.ops.framework.EntityType

class EntitySubscription(val username: String? = null,
                         val entityType: EntityType? = null,
                         val entityId: Int? = null)
