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
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;
import uk.gov.london.ops.framework.exception.ForbiddenAccessException;
import uk.gov.london.ops.framework.exception.ValidationException;
import uk.gov.london.ops.framework.feature.Feature;
import uk.gov.london.ops.framework.feature.FeatureStatus;

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
import static uk.gov.london.ops.user.UserUtils.currentUser;

/**
 * API to manage reports.
 *
 * @author Antonio Perez Dieppa
 */

@RestController
@RequestMapping("/api/v1")
@Api
public class ReportAPI {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final FeatureStatus featureStatus;
    private final ReportService reportService;
    private final SupportedReportsVerifier supportedReportsVerifier;
    private final UserReportService userReportService;

    ReportAPI(FeatureStatus featureStatus, ReportService reportService, SupportedReportsVerifier supportedReportsVerifier,
              UserReportService userReportService) {
        this.featureStatus = featureStatus;
        this.reportService = reportService;
        this.supportedReportsVerifier = supportedReportsVerifier;
        this.userReportService = userReportService;
    }

    private String reportFileName(String prefix) {
        return String.format("%s%s.csv", prefix, new SimpleDateFormat("ddMMyyyyHHmmss").format(new Date()));
    }

    @Secured({OPS_ADMIN, TECH_ADMIN})
    @RequestMapping(value = "/report/explain", method = RequestMethod.POST)
    @ApiOperation(value = "Explain plan for a query", hidden = true)
    public String explain(@RequestBody String query,
            @RequestParam(name = "analyse", defaultValue = "true", required = false) boolean analyse) {
        return reportService.explainQuery(query, null, analyse);
    }

    @Secured({OPS_ADMIN, TECH_ADMIN})
    @RequestMapping(value = "/report/explain/programmes/{programmeIds}", method = RequestMethod.POST)
    @ApiOperation(value = "Explain plan for a query", hidden = true)
    public String explain(@RequestBody String query, @PathVariable Integer[] programmeIds,
            @RequestParam(name = "analyse", defaultValue = "true", required = false) boolean analyse) {
        return reportService.explainQuery(query, Arrays.asList(programmeIds), analyse);
    }

    @Secured({OPS_ADMIN, GLA_ORG_ADMIN, GLA_SPM, GLA_PM, GLA_FINANCE, GLA_READ_ONLY, PROJECT_READER, ORG_ADMIN, PROJECT_EDITOR,
            TECH_ADMIN})
    @ApiOperation(value = "Generate a CSV report with multi params", hidden = true)
    @RequestMapping(value = "/generate/csv/{reportName}", method = RequestMethod.POST)
    public UserReport generateCsvReportMultiParam(@PathVariable String reportName, @RequestBody List<ReportFilter> filters)
            throws IOException {
        Report reportByName = reportService.getReportByName(currentUser(), reportName);

        if (userReportService.checkIfTooManyExistingDownloads()) {
            throw new ValidationException("Too many reports are currently downloading.");
        }

        double cost = reportService.validateQueryAndCost(reportName, filters);

        UserReport userReportFrom = userReportService.createUserReportFrom(reportByName, reportName);

        //async call
        reportService.generateCsvReport(currentUser(), userReportFrom, reportName, filters, cost);
        return userReportFrom;
    }

    @Secured({OPS_ADMIN, TECH_ADMIN})
    @RequestMapping(value = "/report/adhoc", method = RequestMethod.POST)
    public void generateAdHocReport(HttpServletResponse response,
            @RequestParam(required = false) String fileName,
            @RequestBody String sql) throws IOException {

        if (StringUtils.isEmpty(fileName)) {
            fileName = reportFileName("Adhoc");
        } else if (!fileName.endsWith(".csv")) {
            fileName = fileName + ".csv";
        }

        OutputStreamWriter out = new OutputStreamWriter(response.getOutputStream(), UTF_8);
        try {
            response.addHeader("Content-disposition", "attachment;filename=" + fileName);
            response.setContentType("text/csv");
            reportService.generateAdHocReport(sql, out);
        } catch (Exception e) {
            log.error("error executing SQL: " + sql, e);
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            out.write(e.getMessage());
        } finally {
            out.flush();
            response.flushBuffer();
        }
    }

    @Secured({OPS_ADMIN, GLA_ORG_ADMIN, GLA_SPM, GLA_PM, GLA_FINANCE, GLA_READ_ONLY, PROJECT_READER, ORG_ADMIN, PROJECT_EDITOR,
            TECH_ADMIN})
    @RequestMapping(value = "/reports", method = RequestMethod.GET)
    @ApiOperation(value = "Returns the list of available reports")
    public List<Report> getAllReports() throws ForbiddenAccessException {
        if (!featureStatus.isEnabled(Feature.AllowNonGLAReportingAccess) && !currentUser().isGla()) {
            throw new ForbiddenAccessException("Non GLA report access feature is disabled.");
        }
        return reportService.getAll();
    }

    @Secured({OPS_ADMIN, GLA_ORG_ADMIN, GLA_SPM, GLA_PM, GLA_FINANCE, GLA_READ_ONLY, PROJECT_READER, ORG_ADMIN, PROJECT_EDITOR,
            TECH_ADMIN})
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

    @Secured({OPS_ADMIN, GLA_ORG_ADMIN, GLA_SPM, GLA_PM, GLA_FINANCE, GLA_READ_ONLY, PROJECT_READER, ORG_ADMIN, PROJECT_EDITOR,
            TECH_ADMIN})
    @RequestMapping(value = "/reports/{reportName}/filters", method = RequestMethod.GET)
    @ApiOperation(value = "Returns lists for filter dropdowns")
    public Map<ReportFilterType, Map<String, String>> getFilterDropDowns(@PathVariable String reportName,
                                                                         @RequestParam Integer[] programmes)
            throws ForbiddenAccessException {
        return reportService.getFilterDropDowns(reportName, programmes);
    }

    @Secured(OPS_ADMIN)
    @RequestMapping(value = "/verifySupportedReports", method = RequestMethod.GET)
    @ApiOperation(value = "Verifies supported reports run correctly on PostGres")
    public @ResponseBody
    int verifySupportedReports() throws IOException {
        supportedReportsVerifier.verifySupportedReports();
        return supportedReportsVerifier.errorCount;
    }

    @Secured({OPS_ADMIN, GLA_ORG_ADMIN, GLA_SPM, GLA_PM, GLA_FINANCE, GLA_READ_ONLY, PROJECT_READER, ORG_ADMIN, PROJECT_EDITOR,
            TECH_ADMIN})
    @RequestMapping(value = "/userReports/", method = RequestMethod.GET)
    @ApiOperation(value = "Returns the list of available user reports")
    public List<UserReport> getAllUserReports() {
        return userReportService.getAll();
    }

    @Secured({OPS_ADMIN, GLA_ORG_ADMIN, GLA_SPM, GLA_PM, GLA_FINANCE, GLA_READ_ONLY, PROJECT_READER, ORG_ADMIN, PROJECT_EDITOR,
            TECH_ADMIN})
    @RequestMapping(value = "/userReport/{reportId}", method = RequestMethod.DELETE)
    @ApiOperation(value = "Delete an user report")
    public void deleteUserReport(@PathVariable Integer reportId) {
        userReportService.delete(reportId);
    }
}
