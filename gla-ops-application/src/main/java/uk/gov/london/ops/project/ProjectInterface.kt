/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.project

import uk.gov.london.ops.organisation.model.OrganisationEntity
import uk.gov.london.ops.project.state.StateModel

/**
 * TODO : rename this to "Project" and rename "Project" to "ProjectEntity"
 */
interface ProjectInterface {

    var id: Int?

    var organisation: OrganisationEntity

    var managingOrganisation: OrganisationEntity

    var stateModel: StateModel

    var statusName: String

    var subStatusName: String?

    var totalGrantEligibility: Long?

    var isMarkedForCorporate: Boolean

}
