/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

package uk.gov.london.ops.framework.clusterlock;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import static uk.gov.london.common.user.BaseRole.OPS_ADMIN;
import static uk.gov.london.common.user.BaseRole.TECH_ADMIN;

@RestController
@RequestMapping("/api/v1")
public class ClusterLockAPI {

    @Autowired
    ClusterLockService clusterLockService;

    @Secured({OPS_ADMIN, TECH_ADMIN})
    @RequestMapping(value = "/clusterlock/type/{lockType}/serverid/{serverId}/take", method = RequestMethod.PUT)
    public boolean takeLock(@PathVariable ClusterLock.Type lockType, @PathVariable String serverId) {
        return clusterLockService.takeLock(serverId, lockType);
    }

    @Secured({OPS_ADMIN, TECH_ADMIN})
    @RequestMapping(value = "/clusterlock/type/{lockType}/release", method = RequestMethod.PUT)
    public void releaseLock(@PathVariable ClusterLock.Type lockType) {
        clusterLockService.releaseLock(lockType);
    }
}
