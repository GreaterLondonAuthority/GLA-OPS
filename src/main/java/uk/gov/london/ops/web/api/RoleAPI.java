/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.web.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;
import uk.gov.london.ops.domain.user.Role;
import uk.gov.london.ops.service.OrganisationService;
import uk.gov.london.ops.web.model.AssignableRole;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class RoleAPI {

    @Autowired
    OrganisationService organisationService;

    @Secured({Role.OPS_ADMIN, Role.GLA_ORG_ADMIN, Role.ORG_ADMIN})
    @RequestMapping(value = "/roles", method = RequestMethod.GET)
    public @ResponseBody List<AssignableRole> assignableRoles(@RequestParam Integer orgId) {
        return organisationService.getAssignableRoles(orgId);
    }

}
