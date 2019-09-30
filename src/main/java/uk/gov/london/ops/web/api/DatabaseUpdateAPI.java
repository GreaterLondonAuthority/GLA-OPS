/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.web.api;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;
import uk.gov.london.ops.domain.DatabaseUpdate;
import uk.gov.london.ops.service.DatabaseUpdateService;
import uk.gov.london.ops.framework.exception.ForbiddenAccessException;
import uk.gov.london.ops.framework.exception.ValidationException;
import uk.gov.london.ops.web.IpWhitelist;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

import static uk.gov.london.common.user.BaseRole.OPS_ADMIN;
import static uk.gov.london.common.user.BaseRole.TECH_ADMIN;

/**
 * REST API for tech support to update DB.
 *
 * @author Rob Bettison
 */
@RestController
@RequestMapping("/api/v1")
@Api(description = "provides ability to tech support update/query DB")
public class DatabaseUpdateAPI {

    Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    DatabaseUpdateService databaseUpdateService;

    @Autowired
    IpWhitelist ipWhitelist;

    @Secured({TECH_ADMIN, OPS_ADMIN})
    @RequestMapping(value = "support/sql/{id}", method = RequestMethod.GET)
    @ApiOperation(value = "Returns details for one update")
    public @ResponseBody DatabaseUpdate getUpdatesSummary(@PathVariable Integer id) throws ForbiddenAccessException {
        return databaseUpdateService.getDatabaseUpdate(id);
    }

    @Secured({TECH_ADMIN, OPS_ADMIN})
    @RequestMapping(value = "support/sql", method = RequestMethod.POST)
    @ApiOperation(value = "creates new DatabaseUpdate")
    public @ResponseBody DatabaseUpdate storeUpdateSql(@RequestBody DatabaseUpdate update) throws ForbiddenAccessException {
        return databaseUpdateService.createDatabaseUpdate(update);
    }

    @Secured({TECH_ADMIN, OPS_ADMIN})
    @RequestMapping(value = "support/sql/{id}", method = RequestMethod.PUT)
    @ApiOperation(value = "approves stored SQL update string")
    public @ResponseBody DatabaseUpdate approveSql(@RequestBody DatabaseUpdate update, @PathVariable Integer id,
                                            HttpServletRequest request) throws ForbiddenAccessException, ValidationException {

        ipWhitelist.checkClientIsWhitelisted(request);
        return databaseUpdateService.saveDatabaseUpdate(update, id);
    }

    @Secured({TECH_ADMIN, OPS_ADMIN})
    @RequestMapping(value = "support/sql", method = RequestMethod.GET, produces = "application/json")
    @ApiOperation(value = "returns all DatabaseUpdates")
    public List<DatabaseUpdate> listSql() throws ForbiddenAccessException {
        return databaseUpdateService.findAll();
    }
}
