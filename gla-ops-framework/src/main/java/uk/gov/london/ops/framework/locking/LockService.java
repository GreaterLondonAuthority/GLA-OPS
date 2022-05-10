/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.framework.locking;

import uk.gov.london.ops.framework.EntityType;
import uk.gov.london.ops.framework.exception.ForbiddenAccessException;

public interface LockService {

    LockableEntity lock(EntityType entityType, Integer entityId);

    void unlock(EntityType entityType, Integer entityId);

    void validateLockedByCurrentUser(LockableEntity lockableEntity) throws ForbiddenAccessException;

}
