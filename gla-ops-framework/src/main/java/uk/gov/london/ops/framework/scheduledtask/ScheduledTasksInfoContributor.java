/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

package uk.gov.london.ops.framework.scheduledtask;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import uk.gov.london.ops.framework.scheduledtask.ScheduledTask;
import uk.gov.london.ops.framework.scheduledtask.ScheduledTaskRepository;

/**
 * Provides information on OPS scheduled tasks
 *
 * Created by rbettison on 13/03/2018.
 */
@Configuration
@Component
public class ScheduledTasksInfoContributor implements InfoContributor {

    Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    ScheduledTaskRepository scheduledTaskRepository;

    public void contribute(Info.Builder builder) {
        List<ScheduledTask> tasks = getScheduledTasks();
        builder.withDetail("scheduledTasks", tasks);
    }

    public List<ScheduledTask> getScheduledTasks() {
        List<ScheduledTask> scheduledTasks = scheduledTaskRepository.findAll();
        log.debug("Scheduled tasks loaded from repository");
        return scheduledTasks;
    }

}
