/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.project.accesscontrol

/**
 * Determines the relationship type between the organisation and the project.
 */
enum class AccessControlRelationshipType {

    /**
     * Assigned to the project's managing organisation.
     */
    MANAGING,

    /**
     * Assigned to the organisation that created the project.
     */
    OWNER,

    /**
     * Assigned to the organisations the project was shared with.
     */
    ASSOCIATED

}
