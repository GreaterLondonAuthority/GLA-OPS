/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.report;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.london.ops.audit.AuditService;
import uk.gov.london.ops.framework.environment.Environment;
import uk.gov.london.ops.framework.exception.NotFoundException;
import uk.gov.london.ops.report.implementation.UserReportRepository;

import javax.transaction.Transactional;
import java.time.OffsetDateTime;
import java.util.List;

import static uk.gov.london.ops.framework.OPSUtils.currentUsername;

@Transactional
@Service
public class UserReportService {

    private final AuditService auditService;
    private final Environment environment;
    private final UserReportRepository userReportRepository;

    @Value("${max-concurrent-report-downloads:4}")
    int maxConcurrentDownloads;

    public UserReportService(AuditService auditService, Environment environment, UserReportRepository userReportRepository) {
        this.auditService = auditService;
        this.environment = environment;
        this.userReportRepository = userReportRepository;
    }

    public List<UserReport> getAll() {
        return userReportRepository.findAllByUsername(currentUsername());
    }

    public UserReport createUserReportFrom(Report report, String reportName) {
        UserReport userReport = new UserReport();
        OffsetDateTime now = environment.now();
        userReport.setName(reportName);
        userReport.setStartTime(now);
        userReport.setStatus(UserReport.Status.inProgress);
        userReport.setUserName(currentUsername());
        if (report != null) {
            userReport.setReportId(report.getId());
        }
        return userReportRepository.save(userReport);
    }

    public UserReport saveUserReport(UserReport report) {
        return userReportRepository.save(report);
    }

    public boolean checkIfTooManyExistingDownloads() {
        return userReportRepository.countAllByUsernameAndStatus(currentUsername(), UserReport.Status.inProgress)
                >= maxConcurrentDownloads;
    }

    public void delete(Integer reportId) {
        UserReport report = get(reportId);
        userReportRepository.delete(report);
        auditService.auditCurrentUserActivity(
                String.format("Deleted user report ID: %d / name: %s", report.getId(), report.getName()));
    }

    public UserReport get(Integer reportId) {
        UserReport report = userReportRepository.findById(reportId).orElse(null);
        if (report == null) {
            throw new NotFoundException("User Report with ID " + reportId + " not found!");
        }
        return report;
    }

}
