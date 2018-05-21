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
import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.stereotype.Service;
import uk.gov.london.ops.Environment;
import uk.gov.london.ops.domain.Email;
import uk.gov.london.ops.domain.ScheduledTask;
import uk.gov.london.ops.repository.ScheduledTaskRepository;

import java.util.Map;
import java.util.TreeMap;

import static uk.gov.london.ops.util.GlaOpsUtils.getStackTraceAsString;

/**
 * Created by sleach on 15/02/2017.
 */
@Service
public class ScheduledTaskService implements InfoContributor {

    Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    ScheduledTaskRepository scheduledTaskRepository;

    @Autowired
    Environment environment;

    public ScheduledTask findOne(String key) {
        return scheduledTaskRepository.findOne(key);
    }

    public void update(String task_key, String status, String result) {
        ScheduledTask task = getScheduledTask(task_key);
        task.setLastExecuted(environment.now());
        if (ScheduledTask.SUCCESS.equals(status)) {
            task.setLastSuccess(environment.now());
        }
        task.setStatus(status);
        task.setResults(result);
        scheduledTaskRepository.save(task);
    }

    public void update(String task_key, Throwable error) {
        update(task_key, ScheduledTask.ERROR, getStackTraceAsString(error));
    }

    /**
     * Gets the scheduled task with the specified key, or creates a new (unsaved) one if necessary.
     */
    private ScheduledTask getScheduledTask(String task_key) {
        ScheduledTask task = findOne(task_key);
        if (task == null) {
            task = new ScheduledTask();
            task.setKey(task_key);
        }
        return task;
    }

    @Override
    public void contribute(Info.Builder builder) {
        builder.withDetail("scheduledTasks",scheduledTaskRepository.findAll());
    }
}
