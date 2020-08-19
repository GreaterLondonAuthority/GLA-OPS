/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.service;

import uk.gov.london.ops.domain.LockableEntity;
import uk.gov.london.ops.framework.exception.ValidationException;

import java.time.OffsetDateTime;
import java.util.Collection;

public interface LockableEntityService<T extends LockableEntity> {

    /**
     * throws a ValidationException if the given entity is lockable due to business specific logic, for example the entity
     * is in a status where it shouldn't be edited.
     */
    void validateLockable(T t) throws ValidationException;

    T getLockable(Integer entityId);

    void saveLockable(T t);

    void saveLockables(Collection<T> t);

    Collection<T> findAllByLockTimeoutTimeBefore(OffsetDateTime now);

}
