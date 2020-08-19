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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.london.ops.framework.EntityType;
import uk.gov.london.ops.domain.LockableEntity;
import uk.gov.london.ops.service.LockService;

import static uk.gov.london.common.user.BaseRole.*;

@RestController
@RequestMapping("/api/v1")
@Api(description="lock api")
public class LockAPI {

    @Autowired
    private LockService lockService;

    @Secured({OPS_ADMIN, ORG_ADMIN, PROJECT_EDITOR})
    @RequestMapping(value = "/locks", method = RequestMethod.POST)
    @ApiOperation(value = "locks an entity", notes = "locks an entity")
    public LockableEntity lock(@RequestParam String entityType, @RequestParam Integer entityId) {
        return lockService.lock(EntityType.valueOf(entityType), entityId);
    }

    @Secured({OPS_ADMIN, ORG_ADMIN, PROJECT_EDITOR})
    @RequestMapping(value = "/locks", method = RequestMethod.DELETE)
    @ApiOperation(value = "locks an entity", notes = "locks an entity")
    public void unlock(@RequestParam String entityType, @RequestParam Integer entityId) {
        lockService.unlock(EntityType.valueOf(entityType), entityId);
    }

}
