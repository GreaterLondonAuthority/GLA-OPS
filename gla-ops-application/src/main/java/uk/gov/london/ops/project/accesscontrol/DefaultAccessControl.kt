/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.project.accesscontrol

import javax.persistence.*

interface DefaultAccessControlInterface {

    val organisationId: Int

    val programmeId: Int

    val templateId: Int

    val relationshipType: AccessControlRelationshipType
}


@Entity
@Table(name = "default_access_control")
@IdClass(DefaultAccessControlId::class)
class DefaultAccessControlSummary(

        @Column(name = "programme_id")
        @Id
        override val programmeId: Int,

        @Column(name = "template_id")
        @Id
        override val templateId: Int,

        @Column(name = "organisation_id")
        @Id
        override val organisationId: Int,

        val organisationName: String,
        val managingOrganisationId: Int,
        val managingOrganisationName: String,

        @Enumerated(EnumType.STRING)
        @Column(name = "relationship_type")
        override val relationshipType: AccessControlRelationshipType

) : DefaultAccessControlInterface
