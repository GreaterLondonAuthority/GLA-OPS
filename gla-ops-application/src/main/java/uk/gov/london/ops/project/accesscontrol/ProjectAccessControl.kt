/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.project.accesscontrol

import javax.persistence.*

interface ProjectAccessControlInterface {

    fun getOrganisationId() : Int?

    fun getRelationshipType() : AccessControlRelationshipType?

}


@Entity
@Table(name = "project_access_control")
class ProjectAccessControlSummary(

        @EmbeddedId
        val id: ProjectAccessControlId

) : ProjectAccessControlInterface {

    override fun getOrganisationId(): Int? {
        return id.organisationId
    }

    override fun getRelationshipType(): AccessControlRelationshipType? {
        return id.relationshipType
    }

}
