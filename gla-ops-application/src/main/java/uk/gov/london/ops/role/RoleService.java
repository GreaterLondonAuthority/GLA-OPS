/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.role;

import org.springframework.stereotype.Service;
import uk.gov.london.ops.framework.Environment;
import uk.gov.london.ops.organisation.OrganisationService;
import uk.gov.london.ops.role.implementation.RoleRepository;
import uk.gov.london.ops.role.model.Role;
import uk.gov.london.ops.role.model.RoleNameAndDescription;
import uk.gov.london.ops.user.UserService;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static uk.gov.london.common.user.BaseRole.*;
import static uk.gov.london.common.user.BaseRole.GLA_READ_ONLY_DESC;

@Service
public class RoleService {

    private static final List<RoleNameAndDescription> ops_admin_assignable_roles = Arrays.asList(
            new RoleNameAndDescription(OPS_ADMIN, OPS_ADMIN_DESC),
            new RoleNameAndDescription(GLA_ORG_ADMIN, GLA_ORG_ADMIN_DESC),
            new RoleNameAndDescription(GLA_SPM, GLA_SPM_DESC),
            new RoleNameAndDescription(GLA_PM, GLA_PM_DESC, true),
            new RoleNameAndDescription(GLA_REGISTRATION_APPROVER, GLA_REGISTRATION_APPROVER_DESC),
            new RoleNameAndDescription(GLA_FINANCE, GLA_FINANCE_DESC),
            new RoleNameAndDescription(GLA_READ_ONLY, GLA_READ_ONLY_DESC)
    );

    private static final List<RoleNameAndDescription> team_roles = Arrays.asList(
            new RoleNameAndDescription(GLA_ORG_ADMIN, GLA_ORG_ADMIN_DESC),
            new RoleNameAndDescription(GLA_SPM, GLA_SPM_DESC),
            new RoleNameAndDescription(GLA_PM, GLA_PM_DESC),
            new RoleNameAndDescription(GLA_READ_ONLY, GLA_READ_ONLY_DESC)
    );

    private static final List<RoleNameAndDescription> managing_organisation_assignable_roles = Arrays.asList(
            new RoleNameAndDescription(GLA_ORG_ADMIN, GLA_ORG_ADMIN_DESC),
            new RoleNameAndDescription(GLA_SPM, GLA_SPM_DESC),
            new RoleNameAndDescription(GLA_PM, GLA_PM_DESC, true),
            new RoleNameAndDescription(GLA_REGISTRATION_APPROVER, GLA_REGISTRATION_APPROVER_DESC),
            new RoleNameAndDescription(GLA_FINANCE, GLA_FINANCE_DESC),
            new RoleNameAndDescription(GLA_READ_ONLY, GLA_READ_ONLY_DESC)
    );

    private static final List<RoleNameAndDescription> organisation_assignable_roles = Arrays.asList(
            new RoleNameAndDescription(ORG_ADMIN, ORG_ADMIN_DESC),
            new RoleNameAndDescription(PROJECT_EDITOR, PROJECT_EDITOR_DESC, true),
            new RoleNameAndDescription(PROJECT_READER, PROJECT_READER_DESC)
    );

    private static final List<RoleNameAndDescription> tech_organisation_assignable_roles = Arrays.asList(
            new RoleNameAndDescription(TECH_ADMIN, TECH_ADMIN_DESC),
            new RoleNameAndDescription(ORG_ADMIN, ORG_ADMIN_DESC),
            new RoleNameAndDescription(GLA_READ_ONLY, GLA_READ_ONLY_DESC)
    );

    final OrganisationService organisationService;
    final UserService userService;
    final RoleRepository roleRepository;
    final Environment environment;

    public RoleService(OrganisationService organisationService, UserService userService, RoleRepository roleRepository,
                       Environment environment) {
        this.organisationService = organisationService;
        this.userService = userService;
        this.roleRepository = roleRepository;
        this.environment = environment;
    }

    public Integer getNumberOfAdmins() {
        return  roleRepository.getNumberOfAdmins();
    }

    public List<Role> findAllByName(String roleName) {
        return roleRepository.findAllByName(roleName);
    }

    public int updateRoles(String fromRole, String toRole, int entityType) {
        return roleRepository.updateRoles(fromRole, toRole, entityType);
    }

    public List<Role> findAll() {
        if (environment.isTestEnvironment()) {
            return roleRepository.findAll();
        }
        return Collections.emptyList();
    }

    public void delete(Role role) {
        if (environment.isTestEnvironment()) {
            roleRepository.delete(role);
        }
    }

    public List<RoleNameAndDescription> getTeamRoles() {
        return team_roles;
    }

    public List<RoleNameAndDescription> getAssignableRoles(Integer orgId) {
        if (organisationService.isManagingOrganisation(orgId)) {
            if (userService.currentUser().isOpsAdmin()) {
                return ops_admin_assignable_roles;
            } else {
                return managing_organisation_assignable_roles;
            }
        } else if (organisationService.isTechSupportOrganisation(orgId)) {
            return tech_organisation_assignable_roles;
        } else {
            return organisation_assignable_roles;
        }
    }

}
