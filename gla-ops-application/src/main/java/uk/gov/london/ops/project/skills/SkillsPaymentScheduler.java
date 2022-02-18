/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.project.skills;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.jdbc.lock.JdbcLockRegistry;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import uk.gov.london.ops.framework.environment.Environment;
import uk.gov.london.ops.framework.feature.Feature;
import uk.gov.london.ops.framework.feature.FeatureStatus;
import uk.gov.london.ops.framework.scheduledtask.ScheduledTask;
import uk.gov.london.ops.framework.scheduledtask.ScheduledTaskService;
import uk.gov.london.ops.notification.NotificationService;
import uk.gov.london.ops.payment.*;
import uk.gov.london.ops.programme.domain.ProgrammeTemplate;
import uk.gov.london.ops.project.Project;
import uk.gov.london.ops.project.ProjectService;
import uk.gov.london.ops.project.block.ProjectBlockType;
import uk.gov.london.ops.project.implementation.repository.ProjectRepository;
import uk.gov.london.ops.user.UserServiceImpl;
import uk.gov.london.ops.user.domain.UserEntity;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;

import static uk.gov.london.ops.notification.NotificationType.PaymentSchedulerSummary;
import static uk.gov.london.ops.refdata.PaymentSourceKt.GRANT;
import static uk.gov.london.ops.user.UserBuilder.SYSTEM_SCHEDULER_USER;
import static uk.gov.london.ops.user.UserServiceImpl.withLoggedInUser;

@Component
@Transactional
public class SkillsPaymentScheduler {

    public static final String SCHEDULED_PAYMENT_CATEGORY = "Scheduled";

    Logger log = LoggerFactory.getLogger(getClass());

    private static final String TASK_KEY = "SKILLS_PAYMENT";
    private static final String SKILLS_PAYMENT_LOCK = "SKILLS_PAYMENT_LOCK";

    @Autowired
    ProjectService projectService;

    @Autowired
    ProjectRepository projectRepository;

    @Autowired
    UserServiceImpl userService;

    @Autowired
    JdbcLockRegistry lockRegistry;

    @Autowired
    ScheduledTaskService scheduledTaskService;

    @Autowired
    PaymentService paymentService;

    @Autowired
    NotificationService notificationService;

    @Autowired
    FeatureStatus featureStatus;

    @Autowired
    Environment environment;

    @Scheduled(cron = "${skills.payment.scheduler.cron.expression}")
    public void schedulePayments() {
        this.schedulePayments(environment.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
    }

    public void schedulePayments(String asOfDate) {
        final Lock lock = lockRegistry.obtain(SKILLS_PAYMENT_LOCK);
        String taskDesc;
        String logMessage;
        SchedulePaymentsForProjectsResult result;
        Map<String, Object> model = new HashMap<String, Object>() {{
            put("scheduledDate", asOfDate);
        }};

        if (!featureStatus.isEnabled(Feature.SkillsPaymentsScheduler)) {
            scheduledTaskService.update(TASK_KEY, ScheduledTask.SKIPPED, "Task toggle is off");
            return;
        }
        if (lock != null && lock.tryLock()) {
            try {
                result = schedulePaymentsForProjects(asOfDate);
                logMessage = String.format("%d Active Learning Grant projects found, %d failed to missing WBS code, "
                                + "%d failed due to duplication, " + "%d failed due to other reason",
                        result.getNbProjects(), result.getFailedDueToMissingWbsCode(),
                        result.getFailedDueToDuplication(), result.getFailedDueToOtherReason());

                // Populate model for notification and email
                model.put("nbProjects", result.getNbProjects());
                model.put("failedDueToMissingWbsCode", result.getFailedDueToMissingWbsCode());
                model.put("failedDueToDuplication", result.getFailedDueToDuplication());
                model.put("failedDueToOtherReason", result.getFailedDueToOtherReason());
                taskDesc = ScheduledTask.SUCCESS;
            } finally {
                if (lock != null) {
                    lock.unlock();
                }
                UserEntity opsAdmin = environment.isTestEnvironment()
                        ? userService.get("test.admin@gla.com") : userService.get("ops@london.gov.uk");
                notificationService.createNotificationForUser(PaymentSchedulerSummary, opsAdmin, model, opsAdmin.getUsername());
            }
        } else {
            taskDesc = ScheduledTask.SKIPPED;
            logMessage = "Could not obtain lock: " + TASK_KEY;
        }
        scheduledTaskService.update(TASK_KEY, taskDesc, logMessage);
    }

    private SchedulePaymentsForProjectsResult schedulePaymentsForProjects(String asOfDate) {
        LocalDate date = LocalDate.parse(asOfDate, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        OffsetDateTime time = OffsetDateTime.of(date, LocalTime.MIDNIGHT, ZoneOffset.UTC);

        List<Project> projects = projectService.findAllProjectsWithScheduledPaymentDue(asOfDate);
        UserEntity user = userService.get(SYSTEM_SCHEDULER_USER);
        withLoggedInUser(user);

        SchedulePaymentsForProjectsResult result = new SchedulePaymentsForProjectsResult(projects.size());

        for (Project project : projects) {
            try {
                if (!shouldSkipPaymentForProject(project)) {
                    schedulePaymentsForProject(project, time, result);
                }
            } catch (Exception e) {
                result.incrementFailedDueToOtherReason();
                log.error(String.format("Attempted to make a scheduled payment on project %d but failed due to %s.",
                        project.getId(), e.toString()));
            }
        }

        return result;
    }

    private boolean shouldSkipPaymentForProject(Project project) {
        boolean skipPayments = false;
        if (!project.getGrantSourceAdjustmentAmount().equals(BigDecimal.ZERO)) {
            log.info(String.format("skipping learning grant project P%d %s as allocation amount changed", project.getId(),
                    project.getTitle()));
            skipPayments = true;
        }
        if (project.isSuspendPayments()) {
            log.info(String.format("Payment scheduler: skipping learning grant project P%d %s as project payments are suspended",
                    project.getId(), project.getTitle()));
            skipPayments = true;;
        }
        return skipPayments;
    }

    private void schedulePaymentsForProject(Project project, OffsetDateTime time, SchedulePaymentsForProjectsResult result) {
        LearningGrantBlock learningGrantBlock = (LearningGrantBlock) project
                .getLatestApprovedBlock(ProjectBlockType.LearningGrant);
        Set<LearningGrantEntry> paymentsDueForMonth = learningGrantBlock.getPaymentsDueForDate(time);

        log.info(String.format("processing Learning Grant project P%d %s", project.getId(), project.getTitle()));
        PaymentGroupEntity paymentGroup = new PaymentGroupEntity();
        for (LearningGrantEntry learningGrantEntry : paymentsDueForMonth) {
            if (StringUtils.isEmpty(project.getProgrammeTemplate().getDefaultWbsCode())) {
                result.incrementFailedDueToMissingWbsCode();
                log.error(String.format("Attempted to make a scheduled payment on %d for month %d year %d without WBS code.",
                        project.getId(), learningGrantEntry.getActualMonth(), learningGrantEntry.getAcademicYear()));
            } else if (paymentService.anyExistingScheduledPayments(project.getId(), learningGrantEntry.getActualYear(),
                    learningGrantEntry.getActualMonth())) {
                result.incrementFailedDueToDuplication();
                log.error(String.format("Attempted to make a duplicate scheduled payment on %d for month %d year %d.",
                        project.getId(), learningGrantEntry.getActualMonth(), learningGrantEntry.getAcademicYear()));
            } else if (learningGrantEntry.getAllocation() != null) {
                ProgrammeTemplate.WbsCodeType defaultWbsCodeType = project.getProgrammeTemplate().getDefaultWbsCodeType();
                SpendType spendType =
                        ProgrammeTemplate.WbsCodeType.Capital.equals(defaultWbsCodeType) ? SpendType.CAPITAL : SpendType.REVENUE;
                UserEntity requester = null;
                OffsetDateTime lastMonetaryApprovalTime = null;
                if (learningGrantBlock.getLastMonetaryApprovalUser() == null) {
                    log.warn(String.format("Unable to use requester to approve project %d using original approver. ",
                            project.getId()));
                    requester = userService.get(learningGrantBlock.getApproverUsername());
                    lastMonetaryApprovalTime = learningGrantBlock.getApprovalTime();
                } else {
                    requester = userService.get(learningGrantBlock.getLastMonetaryApprovalUser());
                    lastMonetaryApprovalTime = learningGrantBlock.getLastMonetaryApprovalTime();
                }
                ProjectLedgerEntry payment = generatePayment(project, learningGrantBlock, learningGrantEntry, spendType,
                        requester, lastMonetaryApprovalTime);
                paymentGroup.setApprovalRequestedBy(requester.getFullName());
                paymentGroup.getLedgerEntries().add(payment);
            }
        }

        if (!paymentGroup.getLedgerEntries().isEmpty()) {
            paymentService.createAndAuthorisePaymentGroup(paymentGroup);
        }
    }

    private ProjectLedgerEntry generatePayment(Project project, LearningGrantBlock learningGrantBlock,
                                               LearningGrantEntry learningGrantEntry, SpendType spendType,
                                               UserEntity requester, OffsetDateTime time) {
        String subCategory = learningGrantEntry.buildPaymentSubCategory();
        ProjectLedgerEntry payment = paymentService.createPayment(project,
                learningGrantBlock.getId(),
                LedgerType.PAYMENT,
                GRANT,
                LedgerStatus.Authorised,
                spendType,
                SCHEDULED_PAYMENT_CATEGORY,
                subCategory,
                learningGrantEntry.getPaymentDue().negate(),
                learningGrantEntry.getActualYear(),
                learningGrantEntry.getActualMonth(),
                null,
                learningGrantEntry.getOriginalId(),
                LedgerSource.WebUI
        );

        payment.setCompanyName(project.getProgramme().getCompanyName());
        payment.setManagingOrganisation(project.getManagingOrganisation());
        payment.setOrganisationId(project.getOrganisation().getId());
        payment.setVendorName(project.getOrganisation().getName());
        payment.setAuthorisedOn(time);
        payment.setAuthorisedBy(requester.getUsername());
        payment.setCreatedOn(environment.now());
        payment.setCreatedBy(SYSTEM_SCHEDULER_USER);
        return payment;
    }

    private class SchedulePaymentsForProjectsResult {

        final int nbProjects;
        int failedDueToMissingWbsCode = 0;
        int failedDueToDuplication = 0;
        int failedDueToOtherReason = 0;

        SchedulePaymentsForProjectsResult(int nbProjects) {
            this.nbProjects = nbProjects;
        }

        int getNbProjects() {
            return nbProjects;
        }

        int getFailedDueToMissingWbsCode() {
            return failedDueToMissingWbsCode;
        }

        void incrementFailedDueToMissingWbsCode() {
            failedDueToMissingWbsCode++;
        }

        int getFailedDueToDuplication() {
            return failedDueToDuplication;
        }

        void incrementFailedDueToDuplication() {
            failedDueToDuplication++;
        }

        int getFailedDueToOtherReason() {
            return failedDueToOtherReason;
        }

        void incrementFailedDueToOtherReason() {
            failedDueToOtherReason++;
        }
    }

}
