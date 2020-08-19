/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.user.domain

import uk.gov.london.ops.user.OrganisationDetailsDTO
import java.util.*

class UserProfile {

    var username: String? = null

    var userId: Int? = null

    var firstName: String? = null

    var lastName: String? = null

    var enabled: Boolean = false

    val organisations: List<UserProfileOrgDetails> = ArrayList()

    val assignableOrganisations: List<OrganisationDetailsDTO> = ArrayList()

}
