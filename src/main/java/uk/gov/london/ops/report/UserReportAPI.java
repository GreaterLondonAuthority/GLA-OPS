/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.report;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.london.ops.service.UserService;

import java.util.List;

import static uk.gov.london.common.user.BaseRole.*;

/**
 * API to manage user reports.
 *
 */
@RestController
@RequestMapping("/api/v1")
@Api(description = "API to retrieve borough reports")
class UserReportAPI {

    @Autowired
    UserService userService;

    @Autowired
    UserReportService service;

    @Secured({OPS_ADMIN, GLA_ORG_ADMIN, GLA_SPM, GLA_PM, GLA_FINANCE, GLA_READ_ONLY, PROJECT_READER, ORG_ADMIN, PROJECT_EDITOR, TECH_ADMIN})
    @RequestMapping(value="/userReports/", method = RequestMethod.GET)
    @ApiOperation(value = "Returns the list of available user reports")
    public List<UserReport> getAll() {
        return service.getAll();
    }


}
