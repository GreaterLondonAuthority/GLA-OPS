/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.project.migration

class MigrationResponse {

    val migrationFailures: MutableSet<ProjectMigrationFailure> = mutableSetOf()
    val migratedProjects: MutableSet<ProjectMigrationSuccess> = mutableSetOf()

}

class ProjectMigrationFailure (
    var projectId: Int,
    var failureReason: String
)

class ProjectMigrationSuccess (
    var projectId: Int)
{
    var updatedEntities: MutableList<Map<String, Any>> = mutableListOf()
    var updatedBlock: Map<String, Any> = mapOf()
}
