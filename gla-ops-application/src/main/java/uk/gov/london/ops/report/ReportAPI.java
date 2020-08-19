/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.report;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;
import uk.gov.london.common.CSVFile;
import uk.gov.london.ops.framework.ExporterUtils;
import uk.gov.london.ops.framework.annotations.PermissionRequired;
import uk.gov.london.ops.framework.exception.ForbiddenAccessException;
import uk.gov.london.ops.framework.exception.ValidationException;
import uk.gov.london.ops.framework.feature.Feature;
import uk.gov.london.ops.framework.feature.FeatureStatus;
import uk.gov.london.ops.project.Project;
import uk.gov.london.ops.report.implementation.AffordableHousingReportMapper;
import uk.gov.london.ops.report.implementation.BoroughReportProfile;
import uk.gov.london.ops.report.implementation.MilestoneReportProfile;
import uk.gov.london.ops.user.UserService;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;
import static uk.gov.london.common.user.BaseRole.*;
import static uk.gov.london.ops.permission.PermissionType.REPORTS_ADHOC;

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

    private final Logger log = LoggerFactory.getLogger(getClass());

    private static final int MILESTONE_EXT_ID = 3003;

    @Autowired
    JdbcTemplate jdbc;

    @Autowired
    BoroughReportProfile boroughReportProfile;

    @Autowired
    MilestoneReportProfile milestoneReportProfile;

    @Autowired
    AffordableHousingReportMapper affordableHousingReportMapper;

    @Autowired
    UserReportService userReportService;

    @Autowired
    UserService userService;

    @Autowired
    SupportedReportsVerifier supportedReportsVerifier;

    private final FeatureStatus featureStatus;

    private final ReportService reportService;

    ReportAPI(final ReportService reportService,
              final FeatureStatus featureStatus) {
        this.reportService = reportService;
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
    @Secured({OPS_ADMIN, GLA_ORG_ADMIN, GLA_SPM, GLA_PM, GLA_FINANCE, GLA_READ_ONLY})
    @GetMapping("/report/borough/csv")
    @ApiOperation(value = "Retrieves data for borough report",
            notes = "Endpoint to download data for borough report")
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
        if (featureStatus.isEnabled(Feature.BoroughReport)) {

            final String fileName = reportFileName("Borough");

            ExporterUtils.csvResponse(response, fileName);
            final List<BoroughReportItem> data = reportService.getBoroughReportItems(
                    programmeId,
                    projectType,
                    status,
                    borough);
            try (OutputStreamWriter out =
                         new OutputStreamWriter(response.getOutputStream(), UTF_8)) {
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
    @Secured({OPS_ADMIN, GLA_ORG_ADMIN, GLA_SPM, GLA_PM, GLA_FINANCE, GLA_READ_ONLY})
    @GetMapping("/report/milestone/csv")
    @ApiOperation(value = "Retrieves data for milestone  report",
            notes = "Endpoint to download data for milestone report")
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

        if (featureStatus.isEnabled(Feature.MilestoneReport)) {
            final String fileName = reportFileName("Milestone");
            int milestoneExternalId = externalId == null ? MILESTONE_EXT_ID : Integer.parseInt(externalId);
            ExporterUtils.csvResponse(response, fileName);
            final List<Project> data = reportService.getProjects(
                    programmeId,
                    projectType,
                    status,
                    borough);
            try (OutputStreamWriter out =
                         new OutputStreamWriter(response.getOutputStream(), UTF_8)) {
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
        return String.format("%s%s.csv", prefix, new SimpleDateFormat("ddMMyyyyHHmmss").format(new Date()));
    }




    @Secured({OPS_ADMIN, TECH_ADMIN})
    @RequestMapping(value = "/report/explain", method = RequestMethod.POST)
    @ApiOperation(value = "Explain plan for a query", hidden = true)
    public String explain(@RequestBody String query,  @RequestParam(name = "analyse",  defaultValue = "true", required = false) boolean analyse) {
        return reportService.explainQuery(query ,null, analyse);
    }

    @Secured({OPS_ADMIN, TECH_ADMIN})
    @RequestMapping(value = "/report/explain/programmes/{programmeIds}", method = RequestMethod.POST)
    @ApiOperation(value = "Explain plan for a query", hidden = true)
    public String explain(@RequestBody String query, @PathVariable Integer[] programmeIds,  @RequestParam(name = "analyse",  defaultValue = "true", required = false) boolean analyse) {
        return reportService.explainQuery(query , Arrays.asList(programmeIds), analyse);
    }

    @Secured({OPS_ADMIN, GLA_ORG_ADMIN, GLA_SPM, GLA_PM, GLA_FINANCE, GLA_READ_ONLY, PROJECT_READER, ORG_ADMIN, PROJECT_EDITOR, TECH_ADMIN})
    @ApiOperation(value = "Generate a CSV report with multi params", hidden = true)
    @RequestMapping(value = "/generate/csv/{reportName}", method = RequestMethod.POST)
    public UserReport generateCsvReportMultiParam(@PathVariable String reportName, @RequestBody List<ReportFilter> filters) throws IOException {
        Report reportByName = reportService.getReportByName(userService.currentUser(), reportName);

        if (userReportService.checkIfTooManyExistingDownloads()) {
            throw new ValidationException("Too many reports are currently downloading.");
        }


        double cost = reportService.validateQueryAndCost(reportName,filters);

        UserReport userReportFrom = userReportService.createUserReportFrom(reportByName, reportName);

        //async call
        reportService.generateCsvReport(userService.currentUser(), userReportFrom, reportName, filters, cost);
        return userReportFrom;
    }

    @PermissionRequired(REPORTS_ADHOC)
    @RequestMapping(value = "/report/adhoc", method = RequestMethod.POST)
    public void generateAdHocReport(HttpServletResponse response,
                                    @RequestParam(required = false) String fileName,
                                    @RequestBody String sql) throws IOException {

        if (StringUtils.isEmpty(fileName)) {
            fileName = reportFileName("Adhoc");
        }
        else if (!fileName.endsWith(".csv")) {
            fileName = fileName + ".csv";
        }

        ExporterUtils.csvResponse(response, fileName);
        OutputStreamWriter out = new OutputStreamWriter(response.getOutputStream(), UTF_8);
        try {
            response.setContentType("text/csv");
            reportService.generateAdHocReport(sql, out);
        }
        catch (Exception e) {
            log.error("error executing SQL: "+sql, e);
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            out.write(e.getMessage());
            out.flush();
        }
        finally {
            response.flushBuffer();
        }
    }

    @Secured({OPS_ADMIN, GLA_ORG_ADMIN, GLA_SPM, GLA_PM, GLA_FINANCE, GLA_READ_ONLY})
    @GetMapping("/report/affordableHousing/csv")
    public void generateAffordableHousingReport(HttpServletResponse response,
                                                @RequestParam(value = "milestoneId", required = false, defaultValue = "-1") Integer milestoneId,
                                                @RequestParam(value = "programme") Integer programmeId,
                                                @RequestParam(value = "projectType", required = false) Integer projectType,
                                                @RequestParam(value = "status", required = false) String status,
                                                @RequestParam(value = "borough", required = false) String borough) throws IOException {
        String fileName = reportFileName("AffordableHousing"+(milestoneId != null && milestoneId != -1 ? ("_"+milestoneId+"_") : ""));
        ExporterUtils.csvResponse(response, fileName);
        List<Project> projects = reportService.getProjects(programmeId, projectType, status, borough);
        try (OutputStreamWriter out = new OutputStreamWriter(response.getOutputStream(), UTF_8)) {
            new CSVFile(AffordableHousingReportMapper.getHeaders(milestoneId), affordableHousingReportMapper.convertProjectsToAffordableHousingReportEntries(projects, milestoneId), out);
        }
        finally {
            response.flushBuffer();
        }
    }

    @Secured({OPS_ADMIN, GLA_ORG_ADMIN, GLA_SPM, GLA_PM, GLA_FINANCE, GLA_READ_ONLY, PROJECT_READER, ORG_ADMIN, PROJECT_EDITOR, TECH_ADMIN})
    @RequestMapping(value = "/reports", method = RequestMethod.GET)
    @ApiOperation(value = "Returns the list of available reports")
    public List<Report> getAllReports() throws ForbiddenAccessException {
        if (!featureStatus.isEnabled(Feature.AllowNonGLAReportingAccess) && !userService.currentUser().isGla()) {
            throw new ForbiddenAccessException("Non GLA report access feature is disabled.");
        }
        return reportService.getAll();
    }

    @Secured({OPS_ADMIN, GLA_ORG_ADMIN, GLA_SPM, GLA_PM, GLA_FINANCE, GLA_READ_ONLY, PROJECT_READER, ORG_ADMIN, PROJECT_EDITOR, TECH_ADMIN})
    @RequestMapping(value = "/reports/{id}", method = RequestMethod.GET)
    @ApiOperation(value = "Returns a report")
    public Report get(@PathVariable Integer id) {
        return reportService.get(id);
    }

    @Secured(OPS_ADMIN)
    @RequestMapping(value = "/reports/{reportId}/sqlQuery", method = RequestMethod.GET)
    @ApiOperation(value = "Gets a report query")
    public String getSqlQuery(@PathVariable Integer reportId) {
        return get(reportId).getSqlQuery();
    }

    @Secured(OPS_ADMIN)
    @RequestMapping(value = "/reports", method = RequestMethod.POST)
    @ApiOperation(value = "Creates a report")
    public Report create(@Valid @RequestBody Report report) {
        return reportService.save(report);
    }

    @Secured(OPS_ADMIN)
    @RequestMapping(value = "/reports/{reportId}", method = RequestMethod.PUT)
    @ApiOperation(value = "Updates a report query")
    public void update(@PathVariable Integer reportId, @Valid @RequestBody Report report) {
        reportService.save(report);
    }

    @Secured(OPS_ADMIN)
    @RequestMapping(value = "/reports/{reportId}/sqlQuery", method = RequestMethod.PUT)
    @ApiOperation(value = "Updates a report query")
    public void updateQuery(@PathVariable Integer reportId, @RequestBody String sqlQuery) {
        reportService.updateSqlQuery(reportId, sqlQuery);
    }

    @Secured(OPS_ADMIN)
    @RequestMapping(value = "/reports/{reportId}", method = RequestMethod.DELETE)
    @ApiOperation(value = "Delete a report")
    public void delete(@PathVariable Integer reportId) {
        reportService.delete(reportId);
    }

    @Secured({OPS_ADMIN, GLA_ORG_ADMIN, GLA_SPM, GLA_PM, GLA_FINANCE, GLA_READ_ONLY, PROJECT_READER, ORG_ADMIN, PROJECT_EDITOR, TECH_ADMIN})
    @RequestMapping(value = "/reports/{reportName}/filters", method = RequestMethod.GET)
    @ApiOperation(value = "Returns lists for filter dropdowns")
    public Map<Report.Filter, Map<String, String>> getFilterDropDowns(@PathVariable String reportName, @RequestParam Integer[] programmes) throws ForbiddenAccessException {
        return reportService.getFilterDropDowns(reportName, programmes);
    }

    @Secured(OPS_ADMIN)
    @RequestMapping(value = "/verifySupportedReports", method = RequestMethod.GET)
    @ApiOperation(value = "Verifies supported reports run correctly on PostGres")
    public @ResponseBody int verifySupportedReports() throws IOException {
        supportedReportsVerifier.verifySupportedReports();
        return supportedReportsVerifier.errorCount;
    }

    @Secured({OPS_ADMIN, GLA_ORG_ADMIN, GLA_SPM, GLA_PM, GLA_FINANCE, GLA_READ_ONLY, PROJECT_READER, ORG_ADMIN, PROJECT_EDITOR, TECH_ADMIN})
    @RequestMapping(value = "/userReports/", method = RequestMethod.GET)
    @ApiOperation(value = "Returns the list of available user reports")
    public List<UserReport> getAllUserReports() {
        return userReportService.getAll();
    }

    @Secured({OPS_ADMIN, GLA_ORG_ADMIN, GLA_SPM, GLA_PM, GLA_FINANCE, GLA_READ_ONLY, PROJECT_READER, ORG_ADMIN, PROJECT_EDITOR, TECH_ADMIN})
    @RequestMapping(value = "/userReport/{reportId}", method = RequestMethod.DELETE)
    @ApiOperation(value = "Delete an user report")
    public void deleteUserReport(@PathVariable Integer reportId) {
        userReportService.delete(reportId);
    }
}
