/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.role;

import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;
import uk.gov.london.ops.framework.annotations.PermissionRequired;
import uk.gov.london.ops.role.model.RoleNameAndDescription;

import java.util.ArrayList;
import java.util.List;

import static uk.gov.london.common.user.BaseRole.*;
import static uk.gov.london.ops.permission.PermissionType.TEAM_ADD;

@RestController
@RequestMapping("/api/v1")
public class RoleAPI {

    final RoleService roleService;

    public RoleAPI(RoleService roleService) {
        this.roleService = roleService;
    }

    @Secured(OPS_ADMIN)
    @RequestMapping(value = "/availableRoles", method = RequestMethod.GET)
    public @ResponseBody List<RoleNameAndDescription> getAvailableRoles() {
        List<RoleNameAndDescription> allRolesNameAndDescription = new ArrayList<>();
        for (String role: ROLES_AND_DESCRIPTIONS.keySet()) {
            allRolesNameAndDescription.add(new RoleNameAndDescription(role, ROLES_AND_DESCRIPTIONS.get(role)));
        }
        return allRolesNameAndDescription;
    }

    @Secured({OPS_ADMIN, GLA_ORG_ADMIN, GLA_SPM, GLA_PM, GLA_REGISTRATION_APPROVER, GLA_PROGRAMME_ADMIN, GLA_FINANCE, GLA_READ_ONLY,
            TECH_ADMIN, ORG_ADMIN})
    @RequestMapping(value = "/assignableRoles", method = RequestMethod.GET)
    public @ResponseBody List<RoleNameAndDescription> assignableRoles(@RequestParam Integer orgId) {
        return roleService.getAssignableRoles(orgId);
    }

    @PermissionRequired(TEAM_ADD)
    @RequestMapping(value = "/teamRoles", method = RequestMethod.GET)
    public @ResponseBody List<RoleNameAndDescription> teamAvailableRoles() {
        return roleService.getTeamRoles();
    }

}
