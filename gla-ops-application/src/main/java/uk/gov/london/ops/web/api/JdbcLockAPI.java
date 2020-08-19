/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

package uk.gov.london.ops.web.api;

import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.jdbc.lock.JdbcLockRegistry;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.london.ops.framework.exception.ValidationException;

import java.util.concurrent.locks.Lock;

import static uk.gov.london.common.user.BaseRole.OPS_ADMIN;

@RestController
@RequestMapping("/api/v1")
public class JdbcLockAPI {

    @Autowired
    JdbcLockRegistry jdbcLockRegistry;

    @Secured(OPS_ADMIN)
    @RequestMapping(value = "/jdbc/lock/{lockKey}", method = RequestMethod.POST, produces = "application/json")
    @ApiOperation(value = "Create a lock with given lockKey, returns true if created successfully")
    public boolean createLock(@PathVariable String lockKey) {
        Lock lock = jdbcLockRegistry.obtain(lockKey);
        return lock.tryLock();
    }

    @Secured(OPS_ADMIN)
    @RequestMapping(value = "/jdbc/unlock/{lockKey}", method = RequestMethod.POST)
    @ApiOperation(value = "Release an existing lock with the given lockKey")
    public void releaseLock(@PathVariable String lockKey) {
        Lock lock = jdbcLockRegistry.obtain(lockKey);
        if (lock != null) {
            lock.unlock();
        } else {
            throw new ValidationException("Could not get lock for key:", lockKey);
        }
    }
}
