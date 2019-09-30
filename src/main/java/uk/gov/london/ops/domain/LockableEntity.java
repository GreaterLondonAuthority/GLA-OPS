/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.domain;

import uk.gov.london.ops.domain.user.User;

import java.time.OffsetDateTime;

public interface LockableEntity {

    EntityType getEntityType();

    User getLockedBy();

    void setLockedBy(User lockedBy);

    OffsetDateTime getLockTimeoutTime();

    void setLockTimeoutTime(OffsetDateTime lockTimeoutTime);

    String getLockedByUsername();

    String getLockedByFirstName();

    String getLockedByLastName();

    default void lock(User user, OffsetDateTime time) {
        setLockedBy(user);
        setLockTimeoutTime(time);
    }

    default void unlock() {
        setLockedBy(null);
        setLockTimeoutTime(null);
    }

    default boolean isLocked() {
        return getLockedBy() != null;
    }

    default boolean isLockedBy(User currentUser) {
        return isLocked() && getLockedBy().equals(currentUser);
    }

}
