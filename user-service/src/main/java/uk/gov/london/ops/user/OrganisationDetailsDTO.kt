/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.user

import uk.gov.london.ops.role.model.RoleNameAndDescription

class OrganisationDetailsDTO (
        var id: Int,
        var name: String,
        var availableRoles: MutableList<RoleNameAndDescription>
)