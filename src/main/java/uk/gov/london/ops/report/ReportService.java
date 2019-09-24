/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.report;

import com.google.common.net.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.london.common.CSVFile;
import uk.gov.london.ops.Environment;
import uk.gov.london.ops.audit.AuditService;
import uk.gov.london.ops.domain.PreSetLabel;
import uk.gov.london.ops.domain.organisation.Organisation;
import uk.gov.london.ops.domain.organisation.Team;
import uk.gov.london.ops.domain.project.Project;
import uk.gov.london.ops.domain.project.ProjectBlockType;
import uk.gov.london.ops.domain.project.ProjectDetailsBlock;
import uk.gov.london.ops.domain.template.Programme;
import uk.gov.london.ops.domain.template.Template;
import uk.gov.london.ops.domain.user.User;
import uk.gov.london.ops.file.AttachmentFile;
import uk.gov.london.ops.file.FileService;
import uk.gov.london.ops.framework.exception.ForbiddenAccessException;
import uk.gov.london.ops.framework.exception.NotFoundException;
import uk.gov.london.ops.framework.exception.ValidationException;
import uk.gov.london.ops.refdata.Borough;
import uk.gov.london.ops.refdata.RefDataService;
import uk.gov.london.ops.report.implementation.BoroughReportRepository;
import uk.gov.london.ops.report.implementation.ReportRepository;
import uk.gov.london.ops.report.implementation.UserReportRepository;
import uk.gov.london.ops.repository.ProjectRepository;
import uk.gov.london.ops.service.OrganisationService;
import uk.gov.london.ops.service.PreSetLabelService;
import uk.gov.london.ops.service.ProgrammeService;
import uk.gov.london.ops.service.UserService;
import uk.gov.london.ops.service.project.ProjectService;
import uk.gov.london.ops.service.project.ProjectStateService;

import java.io.*;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.apache.commons.lang3.StringUtils.containsIgnoreCase;

/**
 * Service to manage Reports.
 *
 * @author Antonio Perez Dieppa
 */

@Service("report_service")
@Transactional
public class ReportService {

    private static final String TOTAL_COST = "Total Cost:";
    private final Logger log = LoggerFactory.getLogger(getClass());

    private static final Set<String> numeric_columns = Stream.of(
            "org_id",
            "organisation_id",
            "managing_organisation_id"
    ).collect(Collectors.toSet());

    @Autowired
    private AuditService auditService;

    @Autowired
    private RefDataService refDataService;

    @Autowired
    private UserService userService;

    @Autowired
    ProjectService projectService;

    @Autowired
    ProgrammeService programmeService;

    @Autowired
    private BoroughReportRepository boroughReportRepository;

    @Autowired
    private FileService fileService;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private UserReportRepository userReportRepository;

    @Autowired
    private UserReportService userReportService;

    @Autowired
    private OrganisationService organisationService;

    @Autowired
    private PreSetLabelService preSetLabelService;

    @Autowired
    private NamedParameterJdbcTemplate jdbc;

    @Autowired
    private Environment environment;

    @Value("#{new Double('${max.report.cost}')}")
    Double maxReportCost;

    @Value("${enforce.report.cost}")
    Boolean enforceReportCost = false;


    DateTimeFormatter formatter =  DateTimeFormatter.ofPattern("dd/MM/yyyy");


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

            if (projectStatus != null && !projectStatus.equals(bi.getStatusName())) {
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

    public String explainQuery(String query, List<Integer> programmeIds,  boolean analyse) {
        String prefix = analyse ? "EXPLAIN (ANALYZE TRUE, " : "EXPLAIN (";

        String fullQuery =  prefix + "COSTS TRUE, FORMAT YAML) " + query;
        Map<String, List> paramMap = new HashMap<>();
        if (programmeIds != null) {
            paramMap = Collections.singletonMap("programme_ids", programmeIds);

        }

        SqlRowSet rows = jdbc.queryForRowSet(fullQuery, paramMap);
        StringBuilder sb = new StringBuilder();
        while (rows.next()) {
            sb.append(rows.getString(1));
            sb.append("\n");
        }
        return sb.toString();
    }

    public Double getQueryCost(String query, Map paramMap) {
        String fullQuery = "EXPLAIN (COSTS TRUE, FORMAT YAML) " + query;
        SqlRowSet rows = jdbc.queryForRowSet(fullQuery, paramMap);
        Double cost = null;
        while (rows.next()) {
            String prop = rows.getString(1);
            if (prop.contains(TOTAL_COST)) {
                prop = prop.substring(prop.indexOf(TOTAL_COST) + TOTAL_COST.length()).trim();
                prop = prop.substring(0, prop.indexOf('\n'));
                cost = Double.parseDouble(prop);

            }
        }

        if (cost == null) {
            throw new ValidationException("Unable to calculate cost for specified query");
        }

        return cost;
    }

    public Map<String, List> getSqlParametersMap(List<ReportFilter> filters){
        Map<String, List> paramMap = new HashMap<>();
        for (ReportFilter filter : filters) {
            if (filter.getFilter().isSqlFilter()) {
                //TODO make generic to be able to construct sql query parameters map of other types than integers
                List<Integer> ids = filter.getParameters().stream().map(Integer::valueOf).collect(Collectors.toList());

                if (ids == null || ids.isEmpty()) {
                    throw new ValidationException(String.format("Unable to query if %s filter is not specified.", filter.getFilter().getName()));
                }

                paramMap.put(filter.getFilter().getColumnName(), ids);
            }
        }
        return paramMap;
    }



    /**
     * Runs a CSV report and writes the results to the specified writer.
     */
    public void csvReport(String reportName, Writer out, List<Integer> programmeIds, String template, String status, String borough) throws IOException {

        String sql = sqlForReport(reportName);
        Map<String, List> paramMap = Collections.singletonMap("programme_ids", programmeIds);

        SqlRowSet rowSet = jdbc.queryForRowSet(sql, paramMap);

        Set<String> headers = rowSetHeaders(rowSet);

        CSVFile csvFile = new CSVFile(headers, out);

        List<Integer> userOrgIds = userService.currentUser().getOrganisationIds();

        while (rowSet.next()) {
            Map<String, Object> row = rowSetCsvValues(rowSet, headers);
            if (matches(row, userOrgIds, template, status, borough)) {
                csvFile.writeValues(row);
            }
        }
    }

    public void validateFilters(String reportName, List<ReportFilter> filters) throws ValidationException {
        Report report = reportRepository.findOneByName(reportName);
        List<Report.Filter> reportFilters = report.getReportFiltersList();
        for(ReportFilter reportFilter : filters) {
            if(!reportFilters.contains(reportFilter.getFilter())) {
                throw new ValidationException("Invalid filter.");
            }
        }
    }

    public double validateQueryAndCost(String reportName, List<ReportFilter> filters) {

        String sql = sqlForReport(userService.currentUser(), reportName);

        boolean fakeCostExceeded = sql.equals("cost_exceeded");

        Map sqlParametersMap = getSqlParametersMap(filters);

        Double queryCost = 0d;
        if (fakeCostExceeded) {
            queryCost = maxReportCost + 100d;
        } else {
            if (enforceReportCost) {
                queryCost = getQueryCost(sql, sqlParametersMap);
            }
        }

        if (enforceReportCost || fakeCostExceeded) {
            if (queryCost > maxReportCost) {
                auditService.auditActivityForUserNewTransaction(userService.currentUser().getUsername(),
                        String.format("Report %s was too expensive to run: Query: %.2f Limit: %.2f Filters Selected: %s", reportName, queryCost, maxReportCost, getSqlFiltersAsText(filters)));
                throw new ValidationException("The dataset you have chosen is too large. There is too much information in your report. Try changing the filters to reduce the information - for example you may have selected too many programmes.");
            }
        }
        return queryCost;
    }

    /**
     * Gets comma delimited string of sql filters for auditing: Programmes: 1,2,3; Labels: 4,5,6
     */
    public String getSqlFiltersAsText(List<ReportFilter> filters){
        List<ReportFilter> sqlFilters = filters.stream()
                .filter(f -> f.getFilter().isSqlFilter()).collect(Collectors.toList());

        List<String> filtersApplied = sqlFilters.stream()
                .map(f -> f.getFilter().getName() + ":" + f.getParameters())
                .collect(Collectors.toList());

        return String.join(", ", filtersApplied);
    }

    /**
     * Runs a CSV report and writes the results to the specified writer.
     */
    @Async
    @javax.transaction.Transactional(javax.transaction.Transactional.TxType.REQUIRES_NEW)
    public void generateCsvReport(User user, UserReport userReport, String reportName, List<ReportFilter> filters, double queryCost) throws IOException {

        String sql = sqlForReport(user, reportName);

        Map<String, List> paramMap= getSqlParametersMap(filters);

        SqlRowSet rowSet = null;

        try {
            Date start = new Date();
            rowSet = jdbc.queryForRowSet(sql, paramMap);
            long timeTaken = new Date().getTime() - start.getTime();
            auditService.auditActivityForUser(user.getUsername(),
                    String.format("Report %s was successful: Query: %.2f Limit: %.2f Actual Time: %d ms Filters Selected: %s",
                            reportName, queryCost, maxReportCost, timeTaken, getSqlFiltersAsText(filters)));
        } catch (DataAccessException e) {
            userReport.setStatus(UserReport.Status.Failed);
            userReportService.saveUserReport(userReport);
            log.error("Unable to execute SQL: ", e);
            return;
        }


        Set<String> headers = rowSetHeaders(rowSet);

        ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
        BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(byteOutputStream));

        CSVFile csvFile = new CSVFile(headers, bufferedWriter);
        List<Integer> userOrgIds = user.getOrganisationIds();

        boolean rowWritten = false;
        while (rowSet.next()) {
            Map<String, Object> row = rowSetCsvValues(rowSet, headers);
            if (matches(row, userOrgIds, filters)) {
                csvFile.writeValues(row);
                rowWritten = true;
            }
        }
        csvFile.flush();

        OffsetDateTime now = environment.now();
        userReport.setEndTime(now);
        if (rowWritten) {
            AttachmentFile file = new AttachmentFile();
            file.setFileName(userReport.getName() + ".csv");
            file.setCreator(user);
            file.setFileContent(byteOutputStream.toByteArray());
            file.setCreatedOn(now);
            file.setFileSize((long) file.getFileContent().length);
            file.setContentType(MediaType.CSV_UTF_8.toString());
            fileService.save(file);
            userReport.setAttachmentFile(file);
            userReport.setStatus(UserReport.Status.Complete);
        } else {
            userReport.setStatus(UserReport.Status.noResults);
        }
        userReportService.saveUserReport(userReport);

    }


    boolean matches(Map<String, Object> row, List<Integer> userOrgIds, List<ReportFilter> filters) {
        for(ReportFilter filter : filters) {
            //Sql filters are already applied so no need to filter in java. Leaving Programme filter for now because its not clear what would be the implication if we removed it
            //More comments in ReportService.getColumn() method
            if(!filter.getFilter().isSqlFilter() || filter.getFilter() == Report.Filter.Programme) {
                Report.Filter reportFilter = filter.getFilter();
                int match = 0;
                for (String parameters : filter.getParameters()) {
                    if (matchColumn(reportFilter, parameters, row)) {
                        match += 1;
                    }
                }
                if (match == 0) {
                    return false;
                }
            }
        }
        return (userOrgIds.contains(row.get("org_id")) || userOrgIds.contains(row.get("organisation_id")) || userOrgIds.contains(row.get("managing_organisation_id")));
    }

    private boolean matchColumn(Report.Filter reportFilter, String name, Map<String, Object> row) {
        if(row != null) {
            if(name != null) {
                return name.equals(row.get(getColumn(reportFilter))) || name.equals(row.get(getColumn(reportFilter).toLowerCase()));
            }
        }
        return false;
    }

    //TODO should rethink how this should work in combination with sql filtering. We pass programme_ids to sql but
    // on top of it we filter in java by column name PROGRAMME_ID. Also if sql is not defined to use :programme_ids
    // the result without java filtering by PROGRAMME_ID will be different. Not sure if it is an issue with production reports
    private String getColumn(Report.Filter filter) {
        switch (filter) {
            case Programme:
                return "PROGRAMME_ID";
            case Borough:
                return "BOROUGH";
            case ProjectStatus:
                return "STATUS";
            case ProjectType:
                return "TEMPLATE_ID";
            case Label:
                return "LABEL_ID";
            case Team:
                return "TEAM_ID";
            default:
                return null;
        }
    }


        boolean matches(Map<String, Object> row, List<Integer> userOrgIds, String template, String status, String borough) {
        return (userOrgIds.contains(row.get("org_id")) || userOrgIds.contains(row.get("organisation_id")) || userOrgIds.contains(row.get("managing_organisation_id")))
                && (template == null || template.equals(row.get("template_id")) || template.equals(row.get("TEMPLATE_ID")))
                && (status == null || status.equals(row.get("status")) || status.equals(row.get("STATUS")))
                && (borough == null || borough.equals(row.get("borough")) || borough.equals(row.get("BOROUGH")));
    }

    /**
     * Returns the SQL for the specified report.
     */
    public String sqlForReport(String reportName) {
        return this.sqlForReport(userService.currentUser(), reportName);
    }
    public String sqlForReport(User user, String reportName) {
        if (reportName.equalsIgnoreCase("summaries")) {
            return "SELECT * FROM  v_project_summaries";
        }

        Report report = getReportByName(user, reportName);
        if (report != null) {
            return report.getSqlQuery();
        }
        throw new NotFoundException("Report not found: " + reportName);

    }

    public Report getReportByName(User user, String reportName) {
        Report report = reportRepository.findOneByName(reportName);
        if (report != null) {
            if (!report.isExternal() && !user.isGla()) {
                throw new ForbiddenAccessException();
            }
        }
        return report;
    }

    /**
     * Returns the column names from a SqlRowSet as an ordered LinkedHashSet.
     */
    Set<String> rowSetHeaders(SqlRowSet rowSet) {
        Set<String> headers = new LinkedHashSet<>();
        for (String column: rowSet.getMetaData().getColumnNames()) {
            headers.add(column.toLowerCase());
        }
        return headers;
    }

    /**
     * Returns all the data from a SqlRowSet row, as a map of column name to column value.
     */
    private Map<String, Object> rowSetCsvValues(SqlRowSet rowSet, Set<String> headers) {
        Map<String,Object> csvRow = new HashMap<>();
        for (String header: headers) {
            if (numeric_columns.contains(header.toLowerCase())) {
                csvRow.put(header.toLowerCase(), rowSet.getInt(header));
            }
            else {
                csvRow.put(header.toLowerCase(), rowSet.getString(header));
            }
        }
        return csvRow;
    }

    public List<Report> getAll() {
        if (userService.currentUser().isGla()) {
            return reportRepository.findAll();
        }
        else {
            return reportRepository.findAllByExternal(true);
        }
    }

    public Report get(Integer reportId) {
        Report report = reportRepository.findById(reportId).orElse(null);
        if (report == null) {
            throw new NotFoundException("Report with ID "+reportId+" not found!");
        }
        return report;
    }

    public Report save(Report report) {
        if (report.getId() != null) {
            Report existing = reportRepository.getOne(report.getId());
            report.setSqlQuery(existing.getSqlQuery());
        }
        else {
            validateSqlQuery(report.getSqlQuery());
        }
        return reportRepository.save(report);
    }

    public void updateSqlQuery(Integer reportId, String sqlQuery) {
        validateSqlQuery(sqlQuery);

        Report report = get(reportId);

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

    public void delete(Integer reportId) {
        Report report = get(reportId);
        reportRepository.delete(report);
        auditService.auditCurrentUserActivity(String.format("Deleted report ID: %d / name: %s", report.getId(), report.getName()));
    }

    public Map<Report.Filter, Map<String, String>> getFilterDropDowns(String reportName, Integer... programmeIds) {
        Map<Report.Filter, Map<String, String>> filterDropDowns = new HashMap<>();
        User user = userService.currentUser();
        Report report = getReportByName(user, reportName);
        List<Report.Filter> reportFilters = report.getReportFiltersList();

        for(Report.Filter reportFilter : reportFilters) {
            if(reportFilter.equals(Report.Filter.Borough)) {

                Map<String, String> boroughs = refDataService.getBoroughs().stream()
                        .sorted(Comparator.comparingInt(Borough::getDisplayOrder))
                        .collect(Collectors.toMap(
                                Borough::getBoroughName,
                                Borough::getBoroughName,
                                (a, b) -> {
                                    throw new ValidationException("Values Should be unique");
                                },
                                TreeMap::new));


                filterDropDowns.put(reportFilter, boroughs);
            }
            else if(reportFilter.equals(Report.Filter.ProjectStatus)) {

                Map<String, String> statusMap = new TreeMap<>();
                Set<String> statuses = ProjectStateService.getAvailableStatuses();
                statuses.forEach(status -> statusMap.put(status, status));

                filterDropDowns.put(reportFilter, statusMap);
            }
            else if(reportFilter.equals(Report.Filter.ProjectType)) {
                Set<Template> templates = new HashSet<>();

                for (Integer programmeId : programmeIds) {
                    Programme p = programmeService.find(programmeId);
                    templates.addAll(p.getTemplates());
                }

                TreeMap<String, String> templateMap = templates.stream().sorted(Comparator.comparing(Template::getName))
                        .collect(Collectors.toMap(
                                t -> String.valueOf(t.getId()),
                                Template::getName,
                                (a, b) -> {
                                    throw new ValidationException("Values Should be unique");
                                },
                                TreeMap::new));
                filterDropDowns.put(reportFilter, templateMap);
            }
            else if(reportFilter.equals(Report.Filter.Team)) {
                Map<String, String> dropDown = new HashMap<>();
                if (user.isGla()) {
                    for (Organisation organisation : user.getOrganisations()) {
                        if (organisation.isManagingOrganisation()) {
                            Set<Team> teams = organisationService.getTeams(organisation.getId());
                            for (Team team : teams.stream().sorted(Comparator.comparing(Team::getName)).collect(Collectors.toList())) {
                                dropDown.put(String.valueOf(team.getId()), organisation.getName() + " - " + team.getName());
                            }
                        }
                    }
                }
                filterDropDowns.put(reportFilter, dropDown);
            }
            else if(reportFilter.equals(Report.Filter.Label)) {
                Map<String, String> labelsDropdown = new HashMap<>();
                List<PreSetLabel> labels = preSetLabelService.getCorporateLabels().stream()
                        .filter(l -> l.getStatus().equals(PreSetLabel.Status.Active)).collect(Collectors.toList());
                for(PreSetLabel label : labels){
                    labelsDropdown.put(label.getId().toString(), label.getLabelName());
                }
                filterDropDowns.put(reportFilter, labelsDropdown);
            }
        }
        return filterDropDowns;
    }

    public void deleteAllTestData() {
        if (environment.isTestEnvironment()) {
            try {
                userReportRepository.deleteAll();
                reportRepository.deleteAll();
            } catch (Exception e) {
                log.error("Error deleting test reports data ",e );
            }
        }
        else {
            log.error("attempting to delete test data in a non test environment!");
        }
    }

}
