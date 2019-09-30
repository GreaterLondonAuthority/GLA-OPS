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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.london.common.CSVFile;
import uk.gov.london.ops.framework.feature.Feature;
import uk.gov.london.ops.framework.feature.FeatureStatus;
import uk.gov.london.ops.domain.project.Project;
import uk.gov.london.ops.report.implementation.AffordableHousingReportMapper;
import uk.gov.london.ops.report.implementation.BoroughReportProfile;
import uk.gov.london.ops.report.implementation.MilestoneReportProfile;
import uk.gov.london.ops.service.UserService;
import uk.gov.london.ops.framework.ExporterUtils;
import uk.gov.london.ops.framework.exception.ForbiddenAccessException;
import uk.gov.london.ops.framework.exception.ValidationException;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.nio.charset.StandardCharsets.UTF_8;
import static uk.gov.london.common.user.BaseRole.*;

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
    LegacyIMSDataImporter legacyIMSDataImporter;

    @Autowired
    UserReportService userReportService;

    @Autowired
    UserService userService;

    @Autowired
    SupportedReportsVerifier supportedReportsVerifier;

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
    @Secured({OPS_ADMIN, GLA_ORG_ADMIN, GLA_SPM, GLA_PM, GLA_FINANCE, GLA_READ_ONLY})
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
        if (featureStatus.isEnabled(Feature.BoroughReport)) {

            final String fileName = reportFileName("Borough");

            ExporterUtils.csvResponse(response, fileName);
            final List<BoroughReportItem> data = service.getBoroughReportItems(
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

        if (featureStatus.isEnabled(Feature.MilestoneReport)) {
            final String fileName = reportFileName("Milestone");
            int milestoneExternalId = externalId == null ? MILESTONE_EXT_ID : Integer.parseInt(externalId);
            ExporterUtils.csvResponse(response, fileName);
            final List<Project> data = service.getProjects(
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
    @RequestMapping(value="/report/explain", method = RequestMethod.POST)
    @ApiOperation(value = "Explain plan for a query", hidden = true)
    public String explain(@RequestBody String query,  @RequestParam(name = "analyse",  defaultValue = "true", required = false) boolean analyse) {
        return service.explainQuery(query ,null, analyse);
    }

    @Secured({OPS_ADMIN, TECH_ADMIN})
    @RequestMapping(value="/report/explain/programmes/{programmeIds}", method = RequestMethod.POST)
    @ApiOperation(value = "Explain plan for a query", hidden = true)
    public String explain(@RequestBody String query, @PathVariable Integer[] programmeIds,  @RequestParam(name = "analyse",  defaultValue = "true", required = false) boolean analyse) {
        return service.explainQuery(query , Arrays.asList(programmeIds), analyse);
    }

    @Secured({OPS_ADMIN})
    @ApiOperation(value = "Run a CSV report", hidden = true)
    @RequestMapping(value="/report/csv/{reportName}", method = RequestMethod.GET, produces = "application/csv")
    // method left for legacy testing
    public void csvReport(final HttpServletResponse response,
                          @PathVariable String reportName,
                          @RequestParam(value = "programme", required = false) String programmeString,
                          @RequestParam(value = "projectType", required = false) String template,
                          @RequestParam(value = "status", required = false) String status,
                          @RequestParam(value = "borough", required = false) String borough) throws IOException {
        service.sqlForReport(reportName);   // Will throw NotFoundException if reportName not known

        if (programmeString == null || programmeString.length() == 0) {
            throw new ValidationException("No programme specified");
        }

        List<Integer> programmes = Arrays.stream(programmeString.split(",")).map(Integer::parseInt).collect(Collectors.toList());


        String fileName = reportFileName(reportName);
        ExporterUtils.csvResponse(response, fileName);
        try (OutputStreamWriter out = new OutputStreamWriter(response.getOutputStream(), UTF_8)) {
            service.csvReport(reportName, out, programmes, template, status, borough);
        } finally {
            response.flushBuffer();
        }
    }

    @Secured({OPS_ADMIN, GLA_ORG_ADMIN, GLA_SPM, GLA_PM, GLA_FINANCE, GLA_READ_ONLY, PROJECT_READER, ORG_ADMIN, PROJECT_EDITOR, TECH_ADMIN})
    @ApiOperation(value = "Generate a CSV report with multi params", hidden = true)
    @RequestMapping(value="/generate/csv/{reportName}", method = RequestMethod.POST)
    public UserReport generateCsvReportMultiParam(@PathVariable String reportName, @RequestBody List<ReportFilter> filters) throws IOException {
        Report reportByName = service.getReportByName(userService.currentUser(), reportName);

        if(userReportService.checkIfTooManyExistingDownloads()) {
            throw new ValidationException("Too many reports are currently downloading.");
        }


        double cost = service.validateQueryAndCost(reportName,filters);

        UserReport userReportFrom = userReportService.createUserReportFrom(reportByName, reportName);

        //async call
        service.generateCsvReport(userService.currentUser(), userReportFrom, reportName, filters, cost);
        return userReportFrom;
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
        List<Project> projects = service.getProjects(programmeId, projectType, status, borough);
        try (OutputStreamWriter out = new OutputStreamWriter(response.getOutputStream(), UTF_8)) {
            new CSVFile(AffordableHousingReportMapper.getHeaders(milestoneId), affordableHousingReportMapper.convertProjectsToAffordableHousingReportEntries(projects, milestoneId), out);
        }
        finally {
            response.flushBuffer();
        }
    }

    @Secured({OPS_ADMIN, GLA_ORG_ADMIN, GLA_SPM, GLA_PM, GLA_FINANCE, GLA_READ_ONLY, PROJECT_READER, ORG_ADMIN, PROJECT_EDITOR, TECH_ADMIN})
    @RequestMapping(value="/reports", method = RequestMethod.GET)
    @ApiOperation(value = "Returns the list of available reports")
    public List<Report> getAll() throws ForbiddenAccessException {
        if(!featureStatus.isEnabled(Feature.AllowNonGLAReportingAccess) && !userService.currentUser().isGla()) {
            throw new ForbiddenAccessException("Non GLA report access feature is disabled.");
        }
        return service.getAll();
    }

    @Secured({OPS_ADMIN, GLA_ORG_ADMIN, GLA_SPM, GLA_PM, GLA_FINANCE, GLA_READ_ONLY, PROJECT_READER, ORG_ADMIN, PROJECT_EDITOR, TECH_ADMIN})
    @RequestMapping(value="/reports/{id}", method = RequestMethod.GET)
    @ApiOperation(value = "Returns a report")
    public Report get(@PathVariable Integer id) {
        return service.get(id);
    }

    @Secured(OPS_ADMIN)
    @RequestMapping(value="/reports/{reportId}/sqlQuery", method = RequestMethod.GET)
    @ApiOperation(value = "Gets a report query")
    public String getSqlQuery(@PathVariable Integer reportId) {
        return get(reportId).getSqlQuery();
    }

    @Secured(OPS_ADMIN)
    @RequestMapping(value="/reports", method = RequestMethod.POST)
    @ApiOperation(value = "Creates a report")
    public Report create(@Valid @RequestBody Report report) {
        return service.save(report);
    }

    @Secured(OPS_ADMIN)
    @RequestMapping(value="/reports/{reportId}", method = RequestMethod.PUT)
    @ApiOperation(value = "Updates a report query")
    public void update(@PathVariable Integer reportId, @Valid @RequestBody Report report) {
        service.save(report);
    }

    @Secured(OPS_ADMIN)
    @RequestMapping(value="/reports/{reportId}/sqlQuery", method = RequestMethod.PUT)
    @ApiOperation(value = "Updates a report query")
    public void updateQuery(@PathVariable Integer reportId, @RequestBody String sqlQuery) {
        service.updateSqlQuery(reportId, sqlQuery);
    }

    @Secured(OPS_ADMIN)
    @RequestMapping(value="/reports/{reportId}", method = RequestMethod.DELETE)
    @ApiOperation(value = "Delete a report")
    public void delete(@PathVariable Integer reportId) {
        service.delete(reportId);
    }

    @Secured(OPS_ADMIN)
    @RequestMapping(value="/reports/legacyImsProject", method = RequestMethod.POST)
    public void importLegacyImsProject(MultipartFile file) throws IOException {
        legacyIMSDataImporter.importLegacyImsProject(file.getInputStream());
    }

    @Secured(OPS_ADMIN)
    @RequestMapping(value="/reports/legacyImsReportedFigures", method = RequestMethod.POST)
    public void importLegacyImsReportedFigures(MultipartFile file) throws IOException {
        legacyIMSDataImporter.importLegacyImsReportedFigures(file.getInputStream());
    }


    @Secured({OPS_ADMIN, GLA_ORG_ADMIN, GLA_SPM, GLA_PM, GLA_FINANCE, GLA_READ_ONLY, PROJECT_READER, ORG_ADMIN, PROJECT_EDITOR, TECH_ADMIN})
    @RequestMapping(value = "/reports/{reportName}/filters", method = RequestMethod.GET)
    @ApiOperation(value = "Returns lists for filter dropdowns")
    public Map<Report.Filter, Map<String, String>> getFilterDropDowns(@PathVariable String reportName, @RequestParam Integer[] programmes) throws ForbiddenAccessException {
        return service.getFilterDropDowns(reportName, programmes);
    }

    @Secured(OPS_ADMIN)
    @RequestMapping(value = "/verifySupportedReports", method = RequestMethod.GET)
    @ApiOperation(value = "Verifies supported reports run correctly on PostGres")
    public @ResponseBody int verifySupportedReports() throws IOException {
        supportedReportsVerifier.verifySupportedReports();
        return supportedReportsVerifier.errorCount;
    }

}
