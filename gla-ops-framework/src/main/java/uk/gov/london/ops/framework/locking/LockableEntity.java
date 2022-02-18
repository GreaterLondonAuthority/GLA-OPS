/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.framework.locking;

import uk.gov.london.ops.framework.EntityType;

import java.time.OffsetDateTime;

public interface LockableEntity {

    Integer getId();

    EntityType getEntityType();

    String getLockedBy();

    void setLockedBy(String lockedBy);

    OffsetDateTime getLockTimeoutTime();

    void setLockTimeoutTime(OffsetDateTime lockTimeoutTime);

    void setLockedByFirstName(String firstName);

    void setLockedByLastName(String lastName);

    String getLockedByFirstName();

    String getLockedByLastName();

    default void lock(String username, OffsetDateTime time) {
        setLockedBy(username);
        setLockTimeoutTime(time);
    }

    default void unlock() {
        setLockedBy(null);
        setLockTimeoutTime(null);
        setLockedByFirstName(null);
        setLockedByLastName(null);
    }

    default boolean isLocked() {
        return getLockedBy() != null;
    }

    default boolean isLockedBy(String username) {
        return isLocked() && getLockedBy().equals(username);
    }

}
