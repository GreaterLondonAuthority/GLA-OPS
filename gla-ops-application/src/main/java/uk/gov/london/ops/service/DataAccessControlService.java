/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.london.ops.audit.AuditService;
import uk.gov.london.ops.framework.EntityType;
import uk.gov.london.ops.framework.exception.ForbiddenAccessException;
import uk.gov.london.ops.organisation.Organisation;
import uk.gov.london.ops.organisation.OrganisationServiceImpl;
import uk.gov.london.ops.organisation.model.OrganisationContract;
import uk.gov.london.ops.organisation.model.OrganisationEntity;
import uk.gov.london.ops.permission.implementation.DefaultAccessControlRepository;
import uk.gov.london.ops.permission.implementation.DefaultAccessControlSummaryRepository;
import uk.gov.london.ops.project.Project;
import uk.gov.london.ops.project.ProjectService;
import uk.gov.london.ops.project.accesscontrol.*;
import uk.gov.london.ops.user.UserServiceImpl;
import uk.gov.london.ops.user.domain.UserEntity;

import java.util.List;
import java.util.Set;

import static uk.gov.london.common.user.BaseRole.*;

/**
 * Provides data access control functionality for GLA OPS.
 *
 * @author Steve Leach
 */
@Service
public class DataAccessControlService {

    @Autowired
    UserServiceImpl userService;

    @Autowired
    AuditService auditService;

    @Autowired
    ProjectService projectService;

    @Autowired
    OrganisationServiceImpl organisationService;

    @Autowired
    DefaultAccessControlRepository defaultAccessControlRepository;

    @Autowired
    DefaultAccessControlSummaryRepository defaultAccessControlSummaryRepository;

    /**
     * Returns true if the specified user has access to the specified project.
     */
    public boolean hasAccess(UserEntity user, Project project) {
        for (ProjectAccessControl pac: project.getAccessControlList()) {
            if (hasAccess(user, pac.getOrganisation())) {
                return true;
            }
        }
        return hasAccess(user, project.getOrganisation());
    }

    /**
     * Returns true if the specified user has access to the specified organisation.
     */
    public boolean hasAccess(UserEntity user, OrganisationEntity organisation) {
        if (user.isOpsAdmin()) {
            return true;
        }

        if (organisation == null) {
            return false;
        }

        return user.isOrgAdmin(organisation)
                || user.inOrganisation(organisation)
                || (organisation.getManagingOrganisation() != null && user.inOrganisation(organisation.getManagingOrganisation()));
    }

    /**
     * Returns true if the specified user has access to the specified organisation contract.
     */
    public boolean hasAccess(UserEntity user, OrganisationContract contract) {
        if (user.isOpsAdmin()) {
            return true;
        }

        if (contract == null) {
            return false;
        }

        return user.hasRole(GLA_ORG_ADMIN) || user.hasRole(GLA_PM) || user.hasRole(GLA_SPM)
                || user.hasRole(PROJECT_READER) || user.hasRole(PROJECT_EDITOR) || user.hasRole(ORG_ADMIN);
    }

    /**
     * Returns true if the target user belongs to an org associated with MOs the current user has GLA_ORG_ADMIN roles in
     * or if the current user is an OPS_ADMIN
     * @param targetUser user whose password is intended for reset
     * @param currentUser current user
     * @return true if current user can reset target user's password
     */
    public boolean hasPasswordResetAccess(UserEntity targetUser, UserEntity currentUser) {
        if (currentUser.isOpsAdmin()) {
            return true;
        }

        Set<OrganisationEntity> targetOrganisations = targetUser.getOrganisations();
        for (Organisation org : targetOrganisations) {
            if ((org.getManagingOrganisation() != null && currentUser.hasRoleInOrganisation(GLA_ORG_ADMIN, org.getManagingOrganisation().getId())) || currentUser.hasRoleInOrganisation(GLA_ORG_ADMIN, org.getId())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns true if the current user has access to the specified organisation.
     */
    public boolean currentUserHasAccess(OrganisationEntity organisation) {
        return userService.currentUser() != null && hasAccess(userService.currentUser(), organisation);
    }

    /**
     * Returns true if the specified user has access to the specified organisation userse.
     */
    public boolean hasAccessToOrganisationUsers(UserEntity user, OrganisationEntity organisation) {
        if (user.isOpsAdmin() || (user.isGla() && organisation != null && organisation.isInternalOrganisation())) {
            return true;
        }
        return this.hasAccess(user, organisation);
    }

    /**
     * Checks that the specified user has access to the specified organisation.
     *
     * @throws ForbiddenAccessException
     *      if the user does not have access to the organisation
     */
    public void checkAccess(UserEntity user, OrganisationEntity organisation) {
        if (! hasAccess(user, organisation)) {
            auditService.auditActivityForUser(user.getUsername(),
                    "User blocked from access to organisation " + organisation.getId());

            throw new ForbiddenAccessException("User " + user.getUsername() + " attempted to access a restricted organisation");
        }
    }

    /**
     * Checks that the specified user has access to the specified organisation contract.
     *
     * @throws ForbiddenAccessException
     *      if the user does not have access to the contract
     */
    public void checkAccess(UserEntity user, OrganisationContract contract) {
        if (! hasAccess(user, contract)) {
            auditService.auditActivityForUser(user.getUsername(),
                "User blocked from access to organisation contract" + contract.getId());

            throw new ForbiddenAccessException("User " + user.getUsername() + " attempted to " +
                "access a restricted organisation contract");
        }
    }

    /**
     * Checks that the current user has access to the specified organisation.
     *
     * @throws ForbiddenAccessException
     *      if the user does not have access to the organisation
     */
    public void checkAccess(OrganisationEntity organisation) {
        checkAccess(userService.loadCurrentUser(), organisation);
    }

    /**
     * Checks that the specified user has access to the specified project.
     *
     * @throws ForbiddenAccessException
     *      if the user does not have access to the project
     */
    public void checkAccess(UserEntity user, Project project) {
        if (! hasAccess(user, project)) {
            auditService.auditActivityForUser(user.getUsername(), "User blocked from access to project " + project.getId());

            throw new ForbiddenAccessException("User " + user.getUsername() + " attempted to access a restricted project");
        }
    }

    /**
     * Checks that the current user has access to the specified project.
     *
     * @throws ForbiddenAccessException
     *      if the user does not have access to the project
     */
    public void checkAccess(Project project) {
        checkAccess(userService.loadCurrentUser(), project);
    }

    /**
     * Checks that the current user has access to the specified project.
     *
     * @throws ForbiddenAccessException
     *      if the user does not have access to the project
     */
    public void checkAccess(EntityType entityType, String entityId) {
        if (EntityType.project.equals(entityType)) {
            checkProjectAccess(Integer.parseInt(entityId));
        } else if (EntityType.organisation.equals(entityType)) {
            checkAccess(organisationService.findOne(Integer.parseInt(entityId)));
        }
    }

    /**
     * Checks that the current user has access to the specified project.
     *
     * @throws ForbiddenAccessException
     *      if the user does not have access to the project
     */
    public void checkProjectAccess(Integer projectId) {
        String username = userService.currentUsername();
        if (!projectService.checkAccessForProject(username, projectId)) {
            auditService.auditActivityForUser(username, "User blocked from access to project " + projectId);
            throw new ForbiddenAccessException("User " + username + " attempted to access a restricted project");
        }
    }

    public List<DefaultAccessControlSummary> getDefaultProjectAccess(Integer programmeId, Integer templateId) {
        return defaultAccessControlSummaryRepository.findAllByProgrammeIdAndTemplateId(programmeId, templateId);
    }

    public void insertDefaultAccessControl(Integer programmeId, Integer templateId, Integer organisationId,
            AccessControlRelationshipType type) {
        DefaultAccessControl accessControl = new DefaultAccessControl(programmeId, templateId, organisationId, type);
        defaultAccessControlRepository.saveAndFlush(accessControl);
    }

    public void deleteDefaultAccessControl(Integer programmeId, Integer templateId, Integer organisationId) {
        defaultAccessControlRepository.deleteById(new DefaultAccessControlId(programmeId, templateId, organisationId));
    }

    public void deleteAllDefaultAccessControl(Integer programmeId, Integer templateId) {
        defaultAccessControlRepository.deleteByProgrammeIdAndTemplateId(programmeId, templateId);
    }

    public List<DefaultAccessControlSummary> getDefaultAccessForOrgs(List<Integer> orgIds) {
        return defaultAccessControlSummaryRepository.findAllByOrganisationIds(orgIds);
    }
}
