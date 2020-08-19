/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.project.migration

import uk.gov.london.ops.project.block.ProjectBlockType

class MigrationRequest (

    var templateId: Int,
    var sourceBlockId: Int,
    var destinationBlockId: Int,
    var projectBlockType: ProjectBlockType
) {
    val mappedFields: MutableList<Map<String, Any>> = mutableListOf()
    val mappedBlockFields: MutableMap<String, Any> = mutableMapOf()

    var dryRun: Boolean = true

}
