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
import uk.gov.london.ops.aop.LogMetrics;
import uk.gov.london.ops.domain.EntityType;
import uk.gov.london.ops.domain.organisation.Organisation;
import uk.gov.london.ops.domain.project.Project;
import uk.gov.london.ops.domain.user.User;
import uk.gov.london.ops.exception.ForbiddenAccessException;
import uk.gov.london.ops.repository.OrganisationRepository;
import uk.gov.london.ops.repository.ProjectRepository;

/**
 * Provides data access control functionality for GLA OPS.
 *
 * @author Steve Leach
 */
@Service
public class DataAccessControlService {

    @Autowired
    UserService userService;

    @Autowired
    AuditService auditService;

    @Autowired
    ProjectRepository projectRepository;

    @Autowired
    OrganisationRepository organisationRepository;

    /**
     * Returns true if the specified user has access to the specified project.
     */
    public boolean hasAccess(User user, Project project) {
        return hasAccess(user, project.getOrganisation()) || hasAccess(user, project.getManagingOrganisation());
    }

    /**
     * Checks that the specified user has access to the specified project.
     *
     * @throws ForbiddenAccessException
     *      if the user does not have access to the project
     */
    @LogMetrics
    public void checkAccess(User user, Project project) {
        if (! hasAccess(user,project)) {
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
    public void checkAccess(EntityType entityType, Integer entityId) {
        if (EntityType.project.equals(entityType)) {
            checkAccess(projectRepository.findOne(entityId));
        }
        else if (EntityType.organisation.equals(entityType)) {
            checkAccess(organisationRepository.findOne(entityId));
        }
    }

    /**
     * Returns true if the specified user has access to the specified organisation.
     */
    public boolean hasAccess(User user, Organisation organisation) {
        if (user.isOpsAdmin()) {
            return true;
        }

        if (organisation == null) {
            return false;
        }

        return user.isOrgAdmin(organisation) || user.inOrganisation(organisation) || organisation.getManagingOrganisation() != null && user.inOrganisation(organisation.getManagingOrganisation());
    }

    /**
     * Checks that the specified user has access to the specified organisation.
     *
     * @throws ForbiddenAccessException
     *      if the user does not have access to the organisation
     */
    public void checkAccess(User user, Organisation organisation) {
        if (! hasAccess(user,organisation)) {
            auditService.auditActivityForUser(user.getUsername(), "User blocked from access to organisation " + organisation.getId());

            throw new ForbiddenAccessException("User " + user.getUsername() + " attempted to access a restricted organisation");
        }
    }

    /**
     * Checks that the current user has access to the specified organisation.
     *
     * @throws ForbiddenAccessException
     *      if the user does not have access to the organisation
     */
    public void checkAccess(Organisation organisation) {
        checkAccess(userService.loadCurrentUser(), organisation);
    }
}
