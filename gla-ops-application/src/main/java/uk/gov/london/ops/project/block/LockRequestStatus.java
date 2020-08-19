/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.project.block;

/**
 * Indicates if the lock was successfully obtained or not and the new(or old) lock details
 *
 * Created by chris on 06/01/2017.
 */
public class LockRequestStatus {

    private boolean lockRequestSuccessful;

    private LockDetails lockDetails;

    public LockRequestStatus() {
    }

    public LockRequestStatus(boolean lockRequestSuccessful, LockDetails lockDetails) {
        this.lockRequestSuccessful = lockRequestSuccessful;
        this.lockDetails = lockDetails;
    }

    public boolean isLockRequestSuccessful() {
        return lockRequestSuccessful;
    }

    public void setLockRequestSuccessful(boolean lockRequestSuccessful) {
        this.lockRequestSuccessful = lockRequestSuccessful;
    }

    public LockDetails getLockDetails() {
        return lockDetails;
    }

    public void setLockDetails(LockDetails lockDetails) {
        this.lockDetails = lockDetails;
    }
}
