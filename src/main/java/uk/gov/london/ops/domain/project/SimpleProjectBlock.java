/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.domain.project;

import uk.gov.london.ops.domain.user.User;

import javax.persistence.Embeddable;

/**
 * Simple base project block class
 */
@Embeddable
public class SimpleProjectBlock extends  NamedProjectBlock{


    public boolean isComplete() {
        return this.getBlockMarkedComplete() == null ? false : this.getBlockMarkedComplete();
    }

    @Override
    protected void generateValidationFailures() {

    }

    public SimpleProjectBlock() {

    }

    public SimpleProjectBlock(Integer blockId, Integer versionNumber, Boolean markedComplete, String displayName, Integer displayOrder, BlockStatus blockStatus, ProjectBlockType blockType, String lockedBy) {
        super();
        this.id = blockId;
        this.setVersionNumber(versionNumber);
        this.setBlockMarkedComplete(markedComplete);
        this.setBlockDisplayName(displayName);
        this.setDisplayOrder(displayOrder);
        this.setBlockStatus(blockStatus);
        this.setBlockType(blockType);
        if (lockedBy != null) {
            this.setLockDetails(new LockDetails(new User(lockedBy), 0));
        }
    }
}
