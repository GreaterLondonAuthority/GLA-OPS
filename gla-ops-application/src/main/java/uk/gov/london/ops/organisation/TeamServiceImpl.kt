/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.organisation

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import uk.gov.london.common.user.BaseRole.GLA_ORG_ADMIN
import uk.gov.london.ops.framework.exception.ValidationException
import uk.gov.london.ops.organisation.OrganisationStatus.Approved
import uk.gov.london.ops.organisation.implementation.repository.TeamRepository
import uk.gov.london.ops.organisation.model.OrganisationEntity
import uk.gov.london.ops.organisation.model.TeamEntity
import uk.gov.london.ops.permission.PermissionService
import uk.gov.london.ops.permission.PermissionType
import uk.gov.london.ops.user.UserService
import uk.gov.london.ops.user.UserUtils.currentUser
import javax.transaction.Transactional

@Transactional
@Service
class TeamServiceImpl @Autowired constructor(val organisationService: OrganisationServiceImpl,
                                             val permissionService: PermissionService,
                                             val userService: UserService,
                                             val teamRepository: TeamRepository) : TeamService {

    override fun getOrganisationTeams(organisationId: Int): Set<Team> {
        return teamRepository.findByOrganisationId(organisationId).map { toModel(it) }.toSet()
    }

    fun toModel(entity: TeamEntity): Team {
        return Team(
                entity.id,
                entity.name,
                entity.organisationId,
                entity.organisationName,
                entity.status,
                entity.registrationAllowed,
                entity.isSkillsGatewayAccessAllowed,
                entity.members,
                entity.createdBy,
                entity.createdOn,
                entity.modifiedBy,
                entity.modifiedOn)
    }

    fun getTeams(searchText: String?, managingOrgIds: List<Int>?, orgStatuses: List<OrganisationStatus>?,
                 pageable: Pageable): Page<TeamEntity> {
        val user = currentUser()
        val currentUserOrgIds = user.organisationIds
        return teamRepository.findAll(currentUserOrgIds, searchText, managingOrgIds, orgStatuses, pageable)
    }

    fun createTeam(team: TeamEntity): OrganisationEntity {
        validateCurrentUserPermission(PermissionType.TEAM_ADD, team.organisationId)
        val createdOrgTeam = createOrganisationFromTeam(team)
        makeCurrentUserTeamOrgAdmin(createdOrgTeam)
        return createdOrgTeam
    }

    private fun createOrganisationFromTeam(team: TeamEntity): OrganisationEntity {
        val organisation = OrganisationEntity(OrganisationType.TEAM, Approved)
        updateOrganisationDetailsFromTeam(team, organisation)
        return organisationService.create(organisation)
    }

    private fun updateOrganisationDetailsFromTeam(team: TeamEntity, organisation: OrganisationEntity) {
        val managingOrganisation = organisationService.findOne(team.organisationId)
        organisation.name = team.name
        organisation.managingOrganisation = managingOrganisation
        organisation.registrationAllowed = team.registrationAllowed
        organisation.isSkillsGatewayAccessAllowed = team.isSkillsGatewayAccessAllowed
    }

    fun makeCurrentUserTeamOrgAdmin(team: OrganisationEntity) {
        val currentUser = currentUser()
        userService.assignRole(currentUser.username, GLA_ORG_ADMIN, team.id, false)
    }

    fun updateTeam(teamId: Int, team: TeamEntity) {
        validateCurrentUserPermission(PermissionType.TEAM_EDIT, team.organisationId)
        val organisation = organisationService.findOne(teamId)
        updateOrganisationDetailsFromTeam(team, organisation)
        organisationService.save(organisation)
    }

    fun deleteTeam(teamId: Int) {
        val existingTeam = organisationService.findOne(teamId)
        validateCurrentUserPermission(PermissionType.TEAM_ADD, existingTeam.managingOrganisationId)
        organisationService.deleteOrganisation(teamId)
    }

    fun validateCurrentUserPermission(permission: PermissionType, organisationId: Int?) {
        if (!permissionService.currentUserHasPermissionForOrganisation(permission, organisationId)) {
            throw ValidationException("You have no permission to perform this operation")
        }
    }

}
