/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.web.api;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;
import uk.gov.london.ops.FeatureStatus;
import uk.gov.london.ops.domain.project.Project;
import uk.gov.london.ops.domain.report.BoroughReportItem;
import uk.gov.london.ops.domain.report.Report;
import uk.gov.london.ops.domain.user.Role;
import uk.gov.london.ops.exception.ForbiddenAccessException;
import uk.gov.london.ops.mapper.AffordableHousingReportMapper;
import uk.gov.london.ops.report.BoroughReportProfile;
import uk.gov.london.ops.report.MilestoneReportProfile;
import uk.gov.london.ops.service.ReportService;
import uk.gov.london.ops.util.CSVFile;
import uk.gov.london.ops.util.ExporterUtils;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * API to manage reports.
 *
 * @see BoroughReportItem
 * @author  Antonio Perez Dieppa
 */

@RestController
@RequestMapping("/api/v1")
@Api(description = "API to retrieve borough reports")
class ReportAPI {

    private static final DateFormat titleDf = new SimpleDateFormat("ddMMyyyyHHmmss");
    private static final int MILESTONE_EXT_ID = 3003;

    @Autowired
    JdbcTemplate jdbc;

    @Autowired
    BoroughReportProfile boroughReportProfile;

    @Autowired
    MilestoneReportProfile milestoneReportProfile;

    @Autowired
    AffordableHousingReportMapper affordableHousingReportMapper;

    private final FeatureStatus featureStatus;

    private final ReportService service;

    ReportAPI(final ReportService reportService,
              final FeatureStatus featureStatus) {
        this.service = reportService;
        this.featureStatus = featureStatus;
    }


    /**
     * API resource to download the borough report in CSV format.
     * @param programmeId Mandatory parameter to retrieve the borough report
     * @param projectType Optional project type to filter the borough report
     * @param status Optional project status to filter the borough report
     * @param borough Optional borough to filter the borough report
     * @throws IOException
     * @throws IllegalAccessException
     */
    @Secured({Role.OPS_ADMIN, Role.GLA_ORG_ADMIN, Role.GLA_SPM, Role.GLA_PM, Role.GLA_FINANCE, Role.GLA_READ_ONLY})
    @GetMapping("/report/borough/csv")
    @ApiOperation(value="Retrieves data for borough report",
            notes="Endpoint to download data for borough report")
    void  exportBoroughDataCsv(
            final HttpServletResponse response ,
            final @RequestParam(value = "programme") int programmeId,
            final @RequestParam(value = "projectType", required = false)
                    Integer projectType,
            final @RequestParam(value = "status", required = false)
                    String status,
            final @RequestParam(value = "borough", required = false)
                    String borough)
            throws IOException, IllegalAccessException, InterruptedException {
        if (featureStatus.isEnabled(FeatureStatus.Feature.BoroughReport)) {

            final String fileName = reportFileName("Borough");

            ExporterUtils.csvResponse(response, fileName);
            final List<BoroughReportItem> data = service.getBoroughReportItems(
                    programmeId,
                    projectType,
                    status,
                    borough);
            try (OutputStreamWriter out =
                         new OutputStreamWriter(response.getOutputStream())) {
                new CSVFile(boroughReportProfile.getHeaders(),
                        boroughReportProfile.convertBoroughReportItemsToMap(data), out);
            } finally {
                response.flushBuffer();
            }
        } else {
            throw new ForbiddenAccessException(
                    "This feature is currently disabled.");
        }
    }

     /**
     * API resource to download the milestone report in CSV format.
     * @param programmeId Mandatory parameter to retrieve the milestone report
     * @param projectType Optional project type to filter the milestone report
     * @param status Optional project status to filter the milestone report
     * @param borough Optional borough to filter the milestone report
     * @param externalId Optional milestone extId to filter based on milestone
     * @throws IOException
     * @throws IllegalAccessException
     */
    @Secured({Role.OPS_ADMIN, Role.GLA_ORG_ADMIN, Role.GLA_SPM, Role.GLA_PM, Role.GLA_FINANCE, Role.GLA_READ_ONLY})
    @GetMapping("/report/milestone/csv")
    @ApiOperation(value="Retrieves data for milestone  report",
            notes="Endpoint to download data for milestone report")
    void  exportMilestoneCsv(
            final HttpServletResponse response ,
            final @RequestParam(value = "programme") int programmeId,
            final @RequestParam(value = "projectType", required = false)
                    Integer projectType,
            final @RequestParam(value = "status", required = false)
                    String status,
            final @RequestParam(value = "borough", required = false)
                    String borough,
            final @RequestParam(value = "milestone", required = false)
                    String externalId)
            throws IOException, IllegalAccessException, InterruptedException {

        if (featureStatus.isEnabled(FeatureStatus.Feature.MilestoneReport)) {
            final String fileName = reportFileName("Milestone");
            int milestoneExternalId = externalId == null ? MILESTONE_EXT_ID : Integer.valueOf(externalId);
            ExporterUtils.csvResponse(response, fileName);
            final List<Project> data = service.getProjects(
                    programmeId,
                    projectType,
                    status,
                    borough);
            try (OutputStreamWriter out =
                         new OutputStreamWriter(response.getOutputStream())) {
                new CSVFile(milestoneReportProfile.getHeaders(),
                        milestoneReportProfile.convertProjectsToMap(data, milestoneExternalId), out);
            } finally {
                response.flushBuffer();
            }
        } else {
            throw new ForbiddenAccessException(
                    "This feature is currently disabled.");
        }
    }

    private String reportFileName(String prefix) {
        return String.format("%s%s.csv", prefix, titleDf.format(new Date()));
    }




    @Secured(Role.OPS_ADMIN)
    @RequestMapping(value="/report/explain", method = RequestMethod.POST)
    @ApiOperation(value = "Explain plan for a query", hidden = true)
    public String explain(@RequestBody String query) {
        return service.explainQuery(query);
    }

    @Secured({Role.OPS_ADMIN, Role.GLA_ORG_ADMIN, Role.GLA_SPM, Role.GLA_PM, Role.GLA_FINANCE, Role.GLA_READ_ONLY})
    @ApiOperation(value = "Run a CSV report", hidden = true)
    @RequestMapping(value="/report/csv/{reportName}", method = RequestMethod.GET, produces = "application/csv")
    public void csvReport(final HttpServletResponse response,
                          @PathVariable String reportName,
                          @RequestParam(value = "programme", required = false) String programme,
                          @RequestParam(value = "projectType", required = false) String template,
                          @RequestParam(value = "status", required = false) String status,
                          @RequestParam(value = "borough", required = false) String borough) throws IOException {
        service.sqlForReport(reportName);   // Will throw NotFoundException if reportName not known

        String fileName = reportFileName(reportName);
        ExporterUtils.csvResponse(response, fileName);
        try (OutputStreamWriter out = new OutputStreamWriter(response.getOutputStream())) {
            service.csvReport(reportName, out, programme, template, status, borough);
        } finally {
            response.flushBuffer();
        }
    }

    @Secured({Role.OPS_ADMIN, Role.GLA_ORG_ADMIN, Role.GLA_SPM, Role.GLA_PM, Role.GLA_FINANCE, Role.GLA_READ_ONLY})
    @GetMapping("/report/affordableHousing/csv")
    public void generateAffordableHousingReport(HttpServletResponse response,
                                                @RequestParam(value = "milestoneId", required = false, defaultValue = "-1") Integer milestoneId,
                                                @RequestParam(value = "programme") Integer programmeId,
                                                @RequestParam(value = "projectType", required = false) Integer projectType,
                                                @RequestParam(value = "status", required = false) String status,
                                                @RequestParam(value = "borough", required = false) String borough) throws IOException {
        String fileName = reportFileName("AffordableHousing"+(milestoneId != null && milestoneId != -1 ? ("_"+milestoneId+"_") : ""));
        ExporterUtils.csvResponse(response, fileName);
        List<Project> projects = service.getProjects(programmeId, projectType, status, borough);
        try (OutputStreamWriter out = new OutputStreamWriter(response.getOutputStream())) {
            new CSVFile(AffordableHousingReportMapper.getHeaders(milestoneId), affordableHousingReportMapper.convertProjectsToAffordableHousingReportEntries(projects, milestoneId), out);
        }
        finally {
            response.flushBuffer();
        }
    }

    @Secured({Role.OPS_ADMIN, Role.GLA_ORG_ADMIN, Role.GLA_SPM, Role.GLA_PM, Role.GLA_FINANCE, Role.GLA_READ_ONLY})
    @RequestMapping(value="/reports", method = RequestMethod.GET)
    @ApiOperation(value = "Returns the list of available reports")
    public List<Report> getAll() {
        return service.getAll();
    }

    @Secured(Role.OPS_ADMIN)
    @RequestMapping(value="/reports", method = RequestMethod.POST)
    @ApiOperation(value = "Creates a report")
    public Report create(@Valid @RequestBody Report report) {
        return service.save(report);
    }

    @Secured(Role.OPS_ADMIN)
    @RequestMapping(value="/reports/{reportId}/sqlQuery", method = RequestMethod.PUT)
    @ApiOperation(value = "Updates a report query")
    public void updateQuery(@PathVariable Integer reportId, @RequestBody String sqlQuery) {
        service.updateSqlQuery(reportId, sqlQuery);
    }

}
