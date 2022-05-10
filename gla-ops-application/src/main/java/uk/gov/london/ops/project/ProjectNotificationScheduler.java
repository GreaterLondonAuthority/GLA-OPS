/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.project;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.integration.jdbc.lock.JdbcLockRegistry;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import uk.gov.london.ops.framework.environment.Environment;
import uk.gov.london.ops.framework.feature.Feature;
import uk.gov.london.ops.framework.feature.FeatureStatus;
import uk.gov.london.ops.framework.scheduledtask.ScheduledTask;
import uk.gov.london.ops.framework.scheduledtask.ScheduledTaskService;
import uk.gov.london.ops.notification.EmailService;
import uk.gov.london.ops.notification.NotificationType;
import uk.gov.london.ops.project.block.NamedProjectBlock;
import uk.gov.london.ops.project.template.TemplateServiceImpl;
import uk.gov.london.ops.user.UserService;

import javax.transaction.Transactional;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.concurrent.locks.Lock;

import static uk.gov.london.ops.project.block.ProjectBlockStatus.UNAPPROVED;

@Transactional
@Component
public class ProjectNotificationScheduler {
    Logger log = LoggerFactory.getLogger(getClass());
    private static final String TASK_KEY = "SUBMISSION_ALERT";
    private static final String PROJECT_SUBMIT_LOCK = "PROJECT_REMINDER_LOCK";
    private Environment environment;
    private FeatureStatus featureStatus;
    private TemplateServiceImpl templateService;
    private ProjectService projectService;
    private UserService userService;
    private EmailService emailService;
    private ScheduledTaskService scheduledTaskService;
    private JdbcLockRegistry lockRegistry;

    @Value("${project.submission.reminder.hours}")
    Integer projectSubmissionReminderHours;
    @Value("${project.submission.reminder.frequency}")
    Integer projectSubmissionReminderFrequency;

    ProjectNotificationScheduler(Environment environment, FeatureStatus featureStatus, TemplateServiceImpl templateService,
                                 ProjectService projectService, UserService userService, EmailService emailService,
                                 ScheduledTaskService scheduledTaskService, JdbcLockRegistry lockRegistry) {
        this.environment = environment;
        this.featureStatus = featureStatus;
        this.templateService = templateService;
        this.projectService = projectService;
        this.userService = userService;
        this.emailService = emailService;
        this.scheduledTaskService = scheduledTaskService;
        this.lockRegistry = lockRegistry;
    }

    @Scheduled(cron = "${project.submission.reminder.cron.expression}")
    public void runProjectSubmissionReminder() {
        log.info("Started - runProjectSubmissionReminder");
        if (!featureStatus.isEnabled(Feature.ProjectSubmissionReminder)) {
            scheduledTaskService.update(TASK_KEY, ScheduledTask.SKIPPED, "Task toggle is off");
            return;
        }
        final Lock lock = lockRegistry.obtain(PROJECT_SUBMIT_LOCK);
        if (lock != null && lock.tryLock()) {
            try {
                Set<Integer> templateIds = templateService.getSubmissionReminderTemplates();
                templateIds.forEach(t -> createProjectSubmissionReminder(t));
            } finally {
                lock.unlock();
            }
        } else {
            scheduledTaskService.update(TASK_KEY, ScheduledTask.SKIPPED, "Could not obtain lock: " + TASK_KEY);
        }
        log.info("Completed - runProjectSubmissionReminder");
    }

    void createProjectSubmissionReminder(Integer templateId) {
        int emailCount =0;
        OffsetDateTime startDateTime = environment.now().minusHours(projectSubmissionReminderHours);
        List<Project> projects = projectService.getProjectsNotSubmitted(templateId, startDateTime,
                startDateTime.minusHours(projectSubmissionReminderFrequency));

        for (Project project: projects) {
            String blockModifiedBy = getUserUnapprovedBlock(project);
            if (blockModifiedBy != null) {
                Map<String, Object> model = new HashMap<String, Object>() {{
                    put("projectId", project.getId());
                    put("projectName", project.getTitle());
                    put("organisationId", project.getOrganisation().getId());
                    put("organisationName", project.getOrganisation().getName());
                    put("programmeId", project.getProgramme().getId());
                    put("programmeName", project.getProgramme().getName());
                    put("userFullName", userService.get(blockModifiedBy).getFullName());
                    put("recipient", blockModifiedBy);
                }};
                emailCount++;
                emailService.sendEmailNotification(NotificationType.ProjectSubmissionReminder, model);
            }
        }
        scheduledTaskService.update(TASK_KEY, ScheduledTask.SUCCESS, "Submit project alerts sent: " + emailCount);
    }

    String getUserUnapprovedBlock(Project project) {
        return project.getProjectBlocks().stream()
                .filter(p -> UNAPPROVED.equals(p.getBlockStatus()) && p.getLastModified() != null)
                .max(Comparator.comparing(NamedProjectBlock::getLastModified))
                .map(NamedProjectBlock::getModifiedBy)
                .orElse(null);
    }
}
