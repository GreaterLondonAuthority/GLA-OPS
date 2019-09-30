/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.service.project;

import static uk.gov.london.ops.di.SystemUserDataInitialiser.SYSTEM_SCHEDULER_USER;
import static uk.gov.london.ops.framework.OPSUtils.withLoggedInUser;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import javax.transaction.Transactional;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.jdbc.lock.JdbcLockRegistry;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import uk.gov.london.ops.Environment;
import uk.gov.london.ops.domain.ScheduledTask;
import uk.gov.london.ops.domain.project.Project;
import uk.gov.london.ops.domain.project.ProjectBlockType;
import uk.gov.london.ops.domain.project.SpendType;
import uk.gov.london.ops.domain.project.skills.LearningGrantBlock;
import uk.gov.london.ops.domain.project.skills.LearningGrantEntry;
import uk.gov.london.ops.domain.template.ProgrammeTemplate;
import uk.gov.london.ops.domain.user.User;
import uk.gov.london.ops.framework.feature.Feature;
import uk.gov.london.ops.framework.feature.FeatureStatus;
import uk.gov.london.ops.payment.LedgerSource;
import uk.gov.london.ops.payment.LedgerStatus;
import uk.gov.london.ops.payment.LedgerType;
import uk.gov.london.ops.payment.PaymentGroup;
import uk.gov.london.ops.payment.PaymentService;
import uk.gov.london.ops.payment.PaymentSource;
import uk.gov.london.ops.payment.ProjectLedgerEntry;
import uk.gov.london.ops.repository.ProjectRepository;
import uk.gov.london.ops.service.ScheduledTaskService;
import uk.gov.london.ops.service.UserService;

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
    UserService userService;

    @Autowired
    JdbcLockRegistry lockRegistry;

    @Autowired
    ScheduledTaskService scheduledTaskService;

    @Autowired
    PaymentService paymentService;

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

        if (!featureStatus.isEnabled(Feature.SkillsPaymentsScheduler)) {
            scheduledTaskService.update(TASK_KEY, ScheduledTask.SKIPPED, "Task toggle is off");
            return;
        }
        if (lock != null && lock.tryLock()) {
            try {
                SchedulePaymentsForProjectsResult result = schedulePaymentsForProjects(asOfDate);

                logMessage = String.format("%d Active Learning Grant projects found, %d failed to missing WBS code, %d failed due to duplication",
                        result.getNbProjects(), result.getFailedDueToMissingWbsCode(), result.getFailedDueToDuplication());
                taskDesc = ScheduledTask.SUCCESS;
            } finally {
                if (lock != null) lock.unlock();
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
        User user = userService.get(SYSTEM_SCHEDULER_USER);
        withLoggedInUser(user);

        SchedulePaymentsForProjectsResult result = new SchedulePaymentsForProjectsResult(projects.size());

        for (Project project : projects) {
            schedulePaymentsForProject(project, time, result);
        }

        return result;
    }

    private void schedulePaymentsForProject(Project project, OffsetDateTime time, SchedulePaymentsForProjectsResult result) {
        if (!project.getGrantSourceAdjustmentAmount().equals(BigDecimal.ZERO)) {
            log.info(String.format("skipping learning grant project P%d %s as allocation amount changed", project.getId(), project.getTitle()));
            return;
        }

        LearningGrantBlock learningGrantBlock = (LearningGrantBlock) project.getLatestApprovedBlock(ProjectBlockType.LearningGrant);
        Set<LearningGrantEntry> paymentsDueForMonth = learningGrantBlock.getPaymentsDueForDate(time);

        log.info(String.format("processing Learning Grant project P%d %s", project.getId(), project.getTitle()));
        PaymentGroup paymentGroup = new PaymentGroup();
        for (LearningGrantEntry learningGrantEntry : paymentsDueForMonth) {
            if (StringUtils.isEmpty(project.getProgrammeTemplate().getDefaultWbsCode())) {
                result.incrementFailedDueToMissingWbsCode();
                log.error(String.format("Attempted to make a scheduled payment on %d for month %d year %d without WBS code.", project.getId(), learningGrantEntry.getActualMonth(), learningGrantEntry.getAcademicYear()));
            }
            else if (paymentService.anyExistingScheduledPayments(project.getId(), learningGrantEntry.getActualYear(), learningGrantEntry.getActualMonth())) {
                result.incrementFailedDueToDuplication();
                log.error(String.format("Attempted to make a duplicate scheduled payment on %d for month %d year %d.", project.getId(), learningGrantEntry.getActualMonth(), learningGrantEntry.getAcademicYear()));
            }
            else if (learningGrantEntry.getAllocation() != null) {
                ProgrammeTemplate.WbsCodeType defaultWbsCodeType = project.getProgrammeTemplate().getDefaultWbsCodeType();
                SpendType spendType = ProgrammeTemplate.WbsCodeType.Capital.equals(defaultWbsCodeType) ? SpendType.CAPITAL : SpendType.REVENUE;
                User requester = userService.get(learningGrantBlock.getApproverUsername());
                ProjectLedgerEntry payment = generatePayment(project, learningGrantBlock, learningGrantEntry, spendType, requester);
                paymentGroup.setApprovalRequestedBy(requester.getFullName());
                paymentGroup.getLedgerEntries().add(payment);
            }
        }

        if (!paymentGroup.getLedgerEntries().isEmpty()) {
            paymentService.createAndAuthorisePaymentGroup(paymentGroup);
        }
    }

    private ProjectLedgerEntry generatePayment(Project project, LearningGrantBlock learningGrantBlock, LearningGrantEntry learningGrantEntry, SpendType spendType, User requester) {
        String subCategory = learningGrantEntry.buildPaymentSubCategory();
        ProjectLedgerEntry payment = paymentService.createPayment(project,
                learningGrantBlock.getId(),
                LedgerType.PAYMENT,
                PaymentSource.Grant,
                LedgerStatus.Authorised,
                spendType,
                SCHEDULED_PAYMENT_CATEGORY,
                subCategory,
                learningGrantEntry.getAllocation().negate(),
                learningGrantEntry.getActualYear(),
                learningGrantEntry.getActualMonth(),
                learningGrantEntry.getOriginalId(),
                LedgerSource.WebUI
        );

        payment.setManagingOrganisation(project.getManagingOrganisation());
        payment.setOrganisationId(project.getOrganisation().getId());
        payment.setVendorName(project.getOrganisation().getName());
        payment.setAuthorisedOn(environment.now());
        payment.setAuthorisedBy(requester.getUsername());
        payment.setCreatedOn(environment.now());
        payment.setCreatedBy(SYSTEM_SCHEDULER_USER);
        return payment;
    }

    private class SchedulePaymentsForProjectsResult {
        final int nbProjects;
        int failedDueToMissingWbsCode = 0;
        int failedDueToDuplication = 0;

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
    }

}
