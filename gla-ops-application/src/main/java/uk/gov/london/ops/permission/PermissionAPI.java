/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.permission;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.london.ops.framework.annotations.PermissionRequired;
import uk.gov.london.ops.permission.implementation.PermissionDetails;

import java.util.List;
import java.util.Map;

import static uk.gov.london.ops.permission.PermissionType.PERMISSION_LIST_VIEW;

/**
 * Spring MVC controller for the Permissions REST endpoint.
 *
 * Created by cmatias on 30/11/2018.
 */
@RestController
@RequestMapping("/api/v1")
@Api
public class PermissionAPI {

    @Autowired
    private PermissionServiceImpl permissionService;

    @PermissionRequired(PERMISSION_LIST_VIEW)
    @RequestMapping(value = "/permissions", method = RequestMethod.GET)
    @ApiOperation(value = "get all permission data", notes = "retrieves a map of role and all associated permissions")
    public Map<String, List<PermissionDetails>> getAllPermissions() {
        return permissionService.getPermissions();
    }

}
