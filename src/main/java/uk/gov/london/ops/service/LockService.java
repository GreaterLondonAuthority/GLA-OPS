/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import uk.gov.london.ops.annualsubmission.AnnualSubmissionService;
import uk.gov.london.ops.domain.EntityType;
import uk.gov.london.ops.domain.LockableEntity;
import uk.gov.london.ops.domain.user.User;
import uk.gov.london.ops.framework.exception.ForbiddenAccessException;
import uk.gov.london.ops.framework.exception.ValidationException;

import javax.annotation.PostConstruct;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Service
public class LockService {

    Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    private UserService userService;

    @Autowired
    private AnnualSubmissionService annualSubmissionService;

    @Value("${default.lock.timeout.minutes}")
    private Integer lockTimeoutInMinutes = 60;

    private Map<EntityType, LockableEntityService> lockableEntityServices = new HashMap<>();

    @PostConstruct
    public void init() {
        lockableEntityServices.put(EntityType.annualSubmissionBlock, annualSubmissionService);
    }

    public LockableEntity lock(EntityType entityType, Integer entityId) {
        LockableEntity lockableEntity = getLockableEntity(entityType, entityId);

        validateLockable(lockableEntity);

        User currentUser = userService.currentUser();
        OffsetDateTime time = OffsetDateTime.now().plusMinutes(lockTimeoutInMinutes);
        lockableEntity.lock(currentUser, time);
        saveLockableEntity(lockableEntity);

        return lockableEntity;
    }

    public void unlock(EntityType entityType, Integer entityId) {
        LockableEntity lockableEntity = getLockableEntity(entityType, entityId);
        validateLockedByCurrentUser(lockableEntity);
        lockableEntity.unlock();
        saveLockableEntity(lockableEntity);
    }

    public void validateLockedByCurrentUser(LockableEntity lockableEntity) {
        User currentUser = userService.currentUser();
        if (!lockableEntity.isLockedBy(currentUser)) {
            throw new ForbiddenAccessException();
        }
    }

    private void validateLockable(LockableEntity lockableEntity) {
        if (lockableEntity.isLocked()) {
            throw new ValidationException("entity already locked!");
        }
        getLES(EntityType.annualSubmissionBlock).validateLockable(lockableEntity);
    }

    private LockableEntity getLockableEntity(EntityType entityType, Integer entityId) {
        return getLES(entityType).getLockable(entityId);
    }

    private void saveLockableEntity(LockableEntity lockableEntity) {
        getLES(lockableEntity.getEntityType()).saveLockable(lockableEntity);
    }

    private LockableEntityService getLES(EntityType entityType) {
        return lockableEntityServices.get(entityType);
    }

    @Scheduled(fixedDelayString = "${lock.time.checker.run.interval.milliseconds}")
    public void deleteExpiredLocks() {
        for (EntityType entityType: lockableEntityServices.keySet()) {
            LockableEntityService service = lockableEntityServices.get(entityType);
            Collection<LockableEntity> lockableEntities = service.findAllByLockTimeoutTimeBefore(OffsetDateTime.now());
            for (LockableEntity lockableEntitiy: lockableEntities) {
                lockableEntitiy.unlock();
            }
            service.saveLockables(lockableEntities);
            log.debug(String.format("Deleted %d timedout %s locks.", lockableEntities.size(), entityType.name()));
        }
    }

}
