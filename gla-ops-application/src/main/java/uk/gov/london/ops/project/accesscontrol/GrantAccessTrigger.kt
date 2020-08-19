/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.project.accesscontrol

/**
 * Determines how the access was granted to the project.
 */
enum class GrantAccessTrigger {

    /**
     * When the access was granted to this project specifically (by sharing or owning).
     */
    PROJECT,

    /**
     * When the access was granted to all projects in a programme, regardless of template.
     */
    PROGRAMME,

    /**
     * When the access was granted to all projects in a programme that use a specific template.
     */
    TEMPLATE,

}
