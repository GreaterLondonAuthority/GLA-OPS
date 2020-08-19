/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.permission.implementation

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import uk.gov.london.ops.project.accesscontrol.*

interface ProjectAccessControlRepository : JpaRepository<ProjectAccessControl, ProjectAccessControlId> {

    @Modifying
    @Query(value = "insert into project_access_control (project_id, organisation_id, relationship_type, grant_access_trigger) select id, ?2, ?3,?4 from project where id in ?1", nativeQuery = true)
    fun insertProjectAccessControl(projectIds: Array<Int>, organisationId: Int, relationshipType: String, grantAccessTrigger: String): Unit

    @Modifying
    @Query(value = "delete from project_access_control where project_id in ?1 and organisation_id = ?2 and grant_access_trigger = ?3", nativeQuery = true)
    fun deleteProjectAccessControl(projectIds: Array<Int>, organisationId: Int, grantAccessTrigger: String): Unit

    @Modifying
    @Query(value = "delete from project_access_control where relationship_type='MANAGING' and grant_access_trigger = 'PROJECT'", nativeQuery = true)
    fun deleteAllMOProjectLevelAccessControl(): Unit


}

interface ProjectAccessControlSummaryRepository : JpaRepository<ProjectAccessControlSummary, ProjectAccessControlId> {

    fun findAllByIdProjectId(projectId: Int) : List<ProjectAccessControlSummary>

}

interface DefaultAccessControlRepository : JpaRepository<DefaultAccessControl, DefaultAccessControlId> {
    fun deleteByProgrammeIdAndTemplateId(programmeId: Int, templateId: Int)

}

interface DefaultAccessControlSummaryRepository : JpaRepository<DefaultAccessControlSummary, DefaultAccessControlId> {

    @Query(value = "SELECT dac.programme_id, dac.template_id, dac.organisation_id, dac.relationship_type,  org.name organisation_name, morg.id managing_organisation_id, morg.name managing_organisation_name FROM DEFAULT_ACCESS_CONTROL dac " +
            "INNER JOIN organisation org ON dac.organisation_id = org.id INNER JOIN organisation morg ON org.managing_organisation_id= morg.id  WHERE dac.programme_id = ?1", nativeQuery = true)
    fun findAllByProgrammeId(programmeId: Int) : List<DefaultAccessControlSummary>

    @Query(value = "SELECT dac.organisation_id, dac.programme_id, dac.template_id, dac.relationship_type,  org.name organisation_name, morg.id managing_organisation_id, morg.name managing_organisation_name FROM DEFAULT_ACCESS_CONTROL dac " +
            "INNER JOIN organisation org ON dac.organisation_id = org.id INNER JOIN organisation morg ON org.managing_organisation_id= morg.id WHERE dac.programme_id = ?1 AND dac.template_id = ?2", nativeQuery = true)
    fun findAllByProgrammeIdAndTemplateId(programmeId: Int, templateId: Int) : List<DefaultAccessControlSummary>

    @Query(value = "SELECT dac.organisation_id, dac.programme_id, dac.template_id, dac.relationship_type,  org.name organisation_name, morg.id managing_organisation_id, morg.name managing_organisation_name FROM DEFAULT_ACCESS_CONTROL dac " +
            "INNER JOIN organisation org ON dac.organisation_id = org.id INNER JOIN organisation morg ON org.managing_organisation_id= morg.id WHERE dac.organisation_id in ?1", nativeQuery = true)
    fun findAllByOrganisationIds(organisationIds: List<Int>) : List<DefaultAccessControlSummary>

}
