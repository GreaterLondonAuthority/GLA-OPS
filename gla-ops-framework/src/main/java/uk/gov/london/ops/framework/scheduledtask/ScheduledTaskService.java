/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.framework.scheduledtask;

import static uk.gov.london.common.GlaUtils.getStackTraceAsString;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.integration.jdbc.lock.JdbcLockRegistry;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import uk.gov.london.ops.framework.environment.Environment;

/**
 * Created by sleach on 15/02/2017.
 */
@Service
public class ScheduledTaskService implements InfoContributor {

    private static final String EXPIRED_LOCKS = "EXPIRED_LOCKS";

    Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    ScheduledTaskRepository scheduledTaskRepository;

    @Autowired
    JdbcLockRegistry lockRegistry;

    @Autowired
    Environment environment;

    public ScheduledTask findOne(String key) {
        return scheduledTaskRepository.findById(key).orElse(null);
    }

    public void update(String taskKey, String status, String result) {
        ScheduledTask task = getScheduledTask(taskKey);
        task.setLastExecuted(environment.now());
        if (ScheduledTask.SUCCESS.equals(status)) {
            task.setLastSuccess(environment.now());
        }
        task.setStatus(status);
        task.setResults(result);
        scheduledTaskRepository.save(task);
    }

    public void update(String taskKey, Throwable error) {
        update(taskKey, ScheduledTask.ERROR, getStackTraceAsString(error));
    }

    /**
     * Gets the scheduled task with the specified key, or creates a new (unsaved) one if necessary.
     */
    private ScheduledTask getScheduledTask(String taskKey) {
        ScheduledTask task = findOne(taskKey);
        if (task == null) {
            task = new ScheduledTask();
            task.setKey(taskKey);
        }
        return task;
    }

    /**
     * Remove all expired locks from lock registry. We assume the life of a lock cannot be more
     * than 1 hour = 3600000 milliseconds. Therefore, any locks not used in the last hour will be
     * considered expired locks and removed.
     */
    @Scheduled(cron = "${skills.payment.scheduler.cron.expression}")
    public void removeExpiredLocks() {
        lockRegistry.expireUnusedOlderThan(3600000);
        log.debug("Successfully removed expired locks");
        update(EXPIRED_LOCKS, ScheduledTask.SUCCESS, "Expired locks are removed");
    }

    @Override
    public void contribute(Info.Builder builder) {
        builder.withDetail("scheduledTasks", scheduledTaskRepository.findAll());
    }
}
