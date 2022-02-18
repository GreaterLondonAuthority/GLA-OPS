/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.project.block;

import javax.persistence.Embeddable;
import uk.gov.london.ops.user.domain.UserEntity;

/**
 * Simple base project block class
 */
@Embeddable
public class SimpleProjectBlock extends NamedProjectBlock {

    public boolean isComplete() {
        return this.getBlockMarkedComplete() == null ? false : this.getBlockMarkedComplete();
    }

    @Override
    protected void generateValidationFailures() {

    }

    public SimpleProjectBlock() {

    }

    @Override
    public Boolean hasUpdates() {
        return super.getHasUpdatesPersisted();
    }

    public SimpleProjectBlock(Integer blockId, Integer versionNumber, Boolean markedComplete, String displayName,
            Integer displayOrder, ProjectBlockStatus blockStatus, ProjectBlockType blockType, String lockedBy,
            Boolean hasUpdatesPersisted) {
        super();
        this.id = blockId;
        this.setVersionNumber(versionNumber);
        this.setBlockMarkedComplete(markedComplete);
        this.setBlockDisplayName(displayName);
        this.setDisplayOrder(displayOrder);
        this.setBlockStatus(blockStatus);
        this.setBlockType(blockType);
        this.setHasUpdatesPersisted(hasUpdatesPersisted);
        if (lockedBy != null) {
            this.setLockDetails(new LockDetails(new UserEntity(lockedBy), 0));
        }
    }
}
