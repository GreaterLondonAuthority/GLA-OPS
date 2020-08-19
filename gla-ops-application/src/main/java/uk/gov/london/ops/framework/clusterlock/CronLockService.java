/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

package uk.gov.london.ops.framework.clusterlock;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CronLockService implements InfoContributor {

    @Autowired
    CronLockRepository cronLockRepository;

    @Autowired
    ClusterLockService clusterLockService;

    public List<CronLock> findAll() {
        return cronLockRepository.findAllByOrderByCreatedDateAsc();
    }

    @Override
    public void contribute(Info.Builder builder) {
        Map<String, Object> data = new HashMap<>();
        Map<String, Object> clusterLocksData = new HashMap<>();
        List<CronLock> cronLocks = findAll();

        for (CronLock lock : cronLocks) {
            data.put(lock.getLockKey(),lock);
        }

        List<ClusterLock> clusterLocks = clusterLockService.findAll();

        for (ClusterLock clusterLock : clusterLocks) {
            if (clusterLock.getServerId() != null) {
                clusterLocksData.put(clusterLock.getLockType().toString(), clusterLock);
            }
        }

        if (!data.isEmpty()) {
            builder.withDetail("cronLocks", data);
        }
        if (!clusterLocksData.isEmpty()) {
            builder.withDetail("clusterLocks", clusterLocksData);
        }
    }
}
