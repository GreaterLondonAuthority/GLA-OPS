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
import uk.gov.london.ops.framework.EntityType;
import uk.gov.london.ops.framework.exception.ForbiddenAccessException;
import uk.gov.london.ops.framework.exception.ValidationException;
import uk.gov.london.ops.framework.locking.LockService;
import uk.gov.london.ops.framework.locking.LockableEntity;
import uk.gov.london.ops.framework.locking.LockableEntityService;
import uk.gov.london.ops.user.User;

import javax.annotation.PostConstruct;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static uk.gov.london.ops.user.UserUtils.currentUser;

@Service
public class LockServiceImpl implements LockService {

    Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    public LockableEntityService[] lockableEntityServices;

    @Value("${default.lock.timeout.minutes}")
    private final Integer lockTimeoutInMinutes = 60;

    private final Map<EntityType, LockableEntityService> lockableEntityServicesMap = new HashMap<>();

    @PostConstruct
    public void init() {
        for (LockableEntityService service : lockableEntityServices) {
            lockableEntityServicesMap.put(service.getEntityType(), service);
        }
    }

    public LockableEntity lock(EntityType entityType, Integer entityId) {
        LockableEntity lockableEntity = getLockableEntity(entityType, entityId);

        validateLockable(lockableEntity);

        User currentUser = currentUser();
        OffsetDateTime time = OffsetDateTime.now().plusMinutes(lockTimeoutInMinutes);
        lockableEntity.lock(currentUser.getUsername(), time);
        lockableEntity.setLockedByFirstName(currentUser.getFirstName());
        lockableEntity.setLockedByLastName(currentUser.getLastName());
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
        User currentUser = currentUser();
        if (!lockableEntity.isLockedBy(currentUser.getUsername())) {
            throw new ForbiddenAccessException();
        }
    }

    private void validateLockable(LockableEntity lockableEntity) {
        if (lockableEntity.isLocked()) {
            throw new ValidationException("entity already locked!");
        }
        getLockableEntityService(EntityType.annualSubmissionBlock).validateLockable(lockableEntity);
    }

    private LockableEntity getLockableEntity(EntityType entityType, Integer entityId) {
        return getLockableEntityService(entityType).getLockable(entityId);
    }

    private void saveLockableEntity(LockableEntity lockableEntity) {
        getLockableEntityService(lockableEntity.getEntityType()).saveLockable(lockableEntity);
    }

    private LockableEntityService getLockableEntityService(EntityType entityType) {
        return lockableEntityServicesMap.get(entityType);
    }

    @Scheduled(fixedDelayString = "${lock.time.checker.run.interval.milliseconds}")
    public void deleteExpiredLocks() {
        for (EntityType entityType : lockableEntityServicesMap.keySet()) {
            LockableEntityService service = lockableEntityServicesMap.get(entityType);
            Collection<LockableEntity> lockableEntities = service.findAllByLockTimeoutTimeBefore(OffsetDateTime.now());
            for (LockableEntity lockableEntity : lockableEntities) {
                lockableEntity.unlock();
            }
            service.saveLockables(lockableEntities);
            log.debug(String.format("Deleted %d timedout %s locks.", lockableEntities.size(), entityType.name()));
        }
    }

}
