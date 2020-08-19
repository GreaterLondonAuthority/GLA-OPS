/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.project.repeatingentity;

import java.time.OffsetDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.london.ops.project.BaseProjectService;
import uk.gov.london.ops.project.ProjectService;
import uk.gov.london.ops.project.block.ProjectBlockService;
import uk.gov.london.ops.user.UserService;

public abstract class RepeatingEntityService<T extends RepeatingEntity> extends BaseProjectService {

    @Autowired
    ProjectService projectService;

    @Autowired
    ProjectBlockService projectBlockService;

    @Autowired
    UserService userService;

    public abstract Class<T> getEntityType();

    public T addRepeatingEntity(Integer projectId, Integer blockId, T entity) {
        RepeatingEntityBlock repeatingEntityBlock = getRepeatingEntityBlockAndCheckLock(blockId);

        repeatingEntityBlock.createNewEntity(entity);
        entity.setCreatedBy(userService.currentUsername());
        entity.setCreatedOn(OffsetDateTime.now());

        repeatingEntityBlock = (RepeatingEntityBlock) projectService.updateProjectBlock(repeatingEntityBlock, projectId);

        return (T) repeatingEntityBlock.getLastEntry();
    }

    public T updateRepeatingEntity(Integer projectId, Integer blockId, T entity) {
        RepeatingEntityBlock repeatingEntityBlock = getRepeatingEntityBlockAndCheckLock(blockId);

        repeatingEntityBlock.updateExistingEntity(entity);

        T updated = (T) repeatingEntityBlock.getEntry(entity.getId());
        updated.setModifiedBy(userService.currentUsername());
        updated.setModifiedOn(OffsetDateTime.now());

        repeatingEntityBlock = (RepeatingEntityBlock) projectService.updateProjectBlock(repeatingEntityBlock, projectId);

        return (T) repeatingEntityBlock.getEntry(entity.getId());
    }

    public void deleteRepeatingEntity(Integer projectId, Integer blockId, Integer entityId) {
        RepeatingEntityBlock repeatingEntityBlock = getRepeatingEntityBlockAndCheckLock(blockId);
        repeatingEntityBlock.delete(entityId);
        projectService.updateProjectBlock(repeatingEntityBlock, projectId);
    }

    private RepeatingEntityBlock getRepeatingEntityBlockAndCheckLock(Integer blockId) {
        RepeatingEntityBlock repeatingEntityBlock = (RepeatingEntityBlock) projectBlockService.getProjectBlock(blockId);
        checkForLock(repeatingEntityBlock);
        return repeatingEntityBlock;
    }

}
