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
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.london.ops.domain.project.Project;
import uk.gov.london.ops.domain.project.ProjectBlockType;
import uk.gov.london.ops.domain.project.ProjectDetailsBlock;
import uk.gov.london.ops.domain.report.BoroughReportItem;
import uk.gov.london.ops.domain.report.Report;
import uk.gov.london.ops.domain.template.Programme;
import uk.gov.london.ops.domain.template.Template;
import uk.gov.london.ops.exception.NotFoundException;
import uk.gov.london.ops.exception.ValidationException;
import uk.gov.london.ops.repository.BoroughReportRepository;
import uk.gov.london.ops.repository.ProjectRepository;
import uk.gov.london.ops.repository.ReportRepository;
import uk.gov.london.ops.util.CSVFile;

import java.io.IOException;
import java.io.Writer;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.containsIgnoreCase;

/**
 * Service to manage Reports.
 *
 * @author Antonio Perez Dieppa
 */

@Service("report_service")
@Transactional
public class ReportService {

    private final Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    private UserService userService;

    @Autowired
    private BoroughReportRepository boroughReportRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private JdbcTemplate jdbc;

    public List<BoroughReportItem> getBoroughReportItems(Integer programmeId, Integer projectType, String projectStatus, String borough) {
        log.debug("Retrieving Borough report by programmeId: {},projectType: {}, ProjectStatus: {}, Borough:{}", programmeId, projectType, projectStatus, borough);

        List<Integer> currentUserOrgIds = userService.currentUser().getOrganisationIds();

        return boroughReportRepository.find(programmeId, projectType, projectStatus, borough)
                .stream()
                .filter(e -> currentUserOrgIds.contains(e.getOrgId()) || currentUserOrgIds.contains(e.getManagingOrganisationId()))
                .collect(Collectors.toList());
    }

    public List<Project> getProjects(Integer programmeId, Integer projectType, String projectStatus, String borough) {
        log.debug("Retrieving Milestone report by programmeId: " +
                        "{},projectType: {}, ProjectStatus: {}, Borough:{}",
                programmeId, projectType, projectStatus, borough);

        Function<Project, Boolean> filter = bi -> {

            if (projectStatus != null && !projectStatus.equals(bi.getStatus().name())) {
                return false;
            }

            ProjectDetailsBlock projectDetails = (ProjectDetailsBlock) bi.getSingleLatestBlockOfType(ProjectBlockType.Details);
            if (borough != null && !borough.equals(projectDetails.getBorough())) {
                return false;
            }

            return bi.getSingleLatestBlockOfType(ProjectBlockType.Milestones) != null;

        };

        Collection<Project> projects;
        Programme programme = new Programme(programmeId, "");
        if (projectType != null) {
            projects = projectRepository.findAllByProgrammeAndTemplate(programme, new Template(projectType, ""));
        } else {
            projects = projectRepository.findAllByProgramme(programme);
        }
        List<Project> allProjects = projects
                .stream()
                .filter(filter::apply)
                .collect(Collectors.toList());



        return allProjects;

    }

    public String explainQuery(String query) {
        SqlRowSet rows = jdbc.queryForRowSet("EXPLAIN (ANALYZE TRUE, COSTS TRUE, FORMAT YAML) " + query);
        StringBuilder sb = new StringBuilder();
        while (rows.next()) {
            sb.append(rows.getString(1));
            sb.append("\n");
        }
        return sb.toString();
    }

    /**
     * Runs a CSV report and writes the results to the specified writer.
     */
    public void csvReport(String reportName, Writer out, String programme, String template, String status, String borough) throws IOException {

        String sql = sqlForReport(reportName);

        SqlRowSet rowSet = jdbc.queryForRowSet(sql);

        Set<String> headers = rowSetHeaders(rowSet);

        CSVFile csvFile = new CSVFile(headers, out);

        while (rowSet.next()) {
            Map<String, Object> row = rowSetCsvValues(rowSet, headers);
            if (matches(row, programme, template, status, borough)) {
                csvFile.writeValues(row);
            }
        }
    }

    private boolean matches(Map<String, Object> row, String programme, String template, String status, String borough) {
        return (programme == null || programme.equals(row.get("programme_id")) || programme.equals(row.get("PROGRAMME_ID")))
                && (template == null || template.equals(row.get("template_id")) || template.equals(row.get("TEMPLATE_ID")))
                && (status == null || status.equals(row.get("status")) || status.equals(row.get("STATUS")))
                && (borough == null || borough.equals(row.get("borough")) || borough.equals(row.get("BOROUGH")));
    }

    /**
     * Returns the SQL for the specified report.
     */
    public String sqlForReport(String reportName) {
        if (reportName.equalsIgnoreCase("summaries")) {
            return "SELECT * FROM  v_project_summaries";
        }

        Report report = reportRepository.findOneByName(reportName);
        if (report != null) {
            return report.getSqlQuery();
        }

        throw new NotFoundException("Report not found: " + reportName);
    }

    /**
     * Returns the column names from a SqlRowSet as an ordered LinkedHashSet.
     */
    Set<String> rowSetHeaders(SqlRowSet rowSet) {
        Set<String> headers = new LinkedHashSet<>();
        for (String column: rowSet.getMetaData().getColumnNames()) {
            headers.add(column);
        }
        return headers;
    }

    /**
     * Returns all the data from a SqlRowSet row, as a map of column name to column value.
     */
    private Map<String, Object> rowSetCsvValues(SqlRowSet rowSet, Set<String> headers) {
        Map<String,Object> csvRow = new HashMap<>();
        for (String header: headers) {
            csvRow.put(header, rowSet.getString(header));
        }
        return csvRow;
    }

    public List<Report> getAll() {
        return reportRepository.findAll();
    }

    public Report save(Report report) {
        validateSqlQuery(report.getSqlQuery());
        return reportRepository.save(report);
    }

    public void updateSqlQuery(Integer reportId, String sqlQuery) {
        validateSqlQuery(sqlQuery);

        Report report = reportRepository.findOne(reportId);
        if (report == null) {
            throw new NotFoundException("Report with ID "+reportId+" not found!");
        }

        report.setSqlQuery(sqlQuery);
        reportRepository.save(report);
    }

    private void validateSqlQuery(String sqlQuery) {
        if (containsIgnoreCase(sqlQuery, "insert")
                || containsIgnoreCase(sqlQuery, "update")
                || containsIgnoreCase(sqlQuery, "delete")) {
            throw new ValidationException("SQL query can only be a select!");
        }
    }

}
