/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

package uk.gov.london.ops.framework.clusterlock;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
public class ClusterLockService {

    @Value("${lock.clusterlock.minutes.until.expire:60}")
    private int lockExpiryTimeMins;

    @Autowired
    ClusterLockRepository clusterLockRepository;

    /**
     * Used by data initialiser to add all locks
     */
    void save(ClusterLock clusterLock) {
        clusterLockRepository.save(clusterLock);
    }

    /**
     * Attempt to take lock by type
     *
     * @return if lock was acquired
     */
    public boolean takeLock(String serverId, ClusterLock.Type lockType) {
        ClusterLock clusterLock = clusterLockRepository.getOne(lockType);

        if (clusterLock.getExpiryTime() == null
                || Objects.equals(clusterLock.getServerId(), serverId)
                || LocalDateTime.now().isAfter(clusterLock.getExpiryTime())) {
            LocalDateTime expiryTime = LocalDateTime.now().plusMinutes(lockExpiryTimeMins);
            clusterLock.updateLock(serverId, expiryTime);
            clusterLockRepository.saveAndFlush(clusterLock);
        }
        return currentServerHasLock(serverId, lockType);
    }

    /**
     * Release lock by type
     *
     * @param lockType ClusterLock type
     */
    public void releaseLock(ClusterLock.Type lockType) {
        ClusterLock clusterLock = clusterLockRepository.getOne(lockType);
        clusterLock.updateLock(null, null);

        clusterLockRepository.save(clusterLock);
    }

    /**
     * Confirm lock is held by param server id
     */
    private boolean currentServerHasLock(String serverId, ClusterLock.Type lockType) {
        ClusterLock clusterLock = clusterLockRepository.getOne(lockType);
        return Objects.equals(serverId, clusterLock.getServerId());
    }

    void deleteAll() {
        clusterLockRepository.deleteAll();
    }

    public List<ClusterLock> findAll() {
        return clusterLockRepository.findAll();
    }
}
