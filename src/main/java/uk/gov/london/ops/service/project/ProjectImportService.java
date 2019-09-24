/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.service.project;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.london.common.CSVFile;
import uk.gov.london.ops.framework.feature.FeatureStatus;
import uk.gov.london.ops.domain.importdata.ImportErrorLog;
import uk.gov.london.ops.domain.importdata.ImportJobType;
import uk.gov.london.ops.domain.organisation.Organisation;
import uk.gov.london.ops.domain.organisation.OrganisationGroup;
import uk.gov.london.ops.domain.project.*;
import uk.gov.london.ops.domain.project.state.ProjectStatus;
import uk.gov.london.ops.domain.template.Programme;
import uk.gov.london.ops.domain.template.Template;
import uk.gov.london.ops.domain.user.User;
import uk.gov.london.ops.notification.NotificationService;
import uk.gov.london.ops.payment.FinanceService;
import uk.gov.london.ops.project.implementation.AnnualSpendSummaryMapper;
import uk.gov.london.ops.project.implementation.IMSProjectImportMapper;
import uk.gov.london.ops.repository.*;
import uk.gov.london.ops.service.*;
import uk.gov.london.ops.service.project.state.ProjectState;
import uk.gov.london.ops.framework.exception.ValidationException;
import uk.gov.london.ops.web.model.project.FileImportResult;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import static uk.gov.london.ops.domain.organisation.Organisation.GLA_HNL_ID;
import static uk.gov.london.ops.domain.project.NamedProjectBlock.BlockStatus.LAST_APPROVED;
import static uk.gov.london.ops.domain.project.state.ProjectSubStatus.Completed;

/**
 * Service interface for managing projects.
 *
 * @author Steve Leach
 */
@Service
@Transactional
public class ProjectImportService extends BaseProjectService {

    public static final String PROJECT_NAME = "Project_Name";
    public static final String PROGRAMME = "Programme";
    public static final String TEMPLATE = "Project_Type";
    public static final String PCS_NUMBER = "PCS_Number";
    public static final String PROJECT_MANAGER = "Project_Manager";
    public static final String BOROUGH = "borough";
    public static final String PROJECT_DESCRIPTION = "project_description";
    public static final String PROJECT_START_DATE = "start_date";
    public static final String PROJECT_END_DATE = "finish_date";
    public static final String SCHEME_ID = "Scheme Id";

    public static final String IMS_PROJECT_NAME = "Project name";
    public static final String IMS_ORGANISATION_CODE = "Lead Org. Code";
    public static final String IMS_PROGRAMME_NAME = "Programme Selected";

    private static final DateTimeFormatter IMPORT_DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final String ORGANISATION_GROUP_NAME = "Consortium/ Partnership name";
    private static final String DEV_ORG = "Dev Org";
    private static final String LIABILIY_DURING_DEV = "Liability during Development";
    private static final String LIABILIY_POST_COMPLETION = "Liability post Completion";
    private static final String OPS_STATUS = "OPS Status";

    Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    Set<PostCloneNotificationListener> cloneListeners;

    @Autowired
    FinanceService financeService;

    @Autowired
    ProjectService projectService;

    @Autowired
    ImportLogService importLogService;

    @Autowired
    OrganisationService organisationService;

    @Autowired
    ProgrammeService programmeService;

    @Autowired
    TemplateService templateService;

    @Autowired
    FeatureStatus featureStatus;

    @Autowired
    OrganisationGroupService organisationGroupService;

    @Autowired
    NotificationService notificationService;

    @Autowired
    AnnualSpendSummaryMapper annualSpendSummaryMapper;

    @Autowired
    LockDetailsRepository lockDetailsRepository;

    @Autowired
    OrganisationRepository organisationRepository;

    @Autowired
    OrganisationGroupRepository organisationGroupRepository;

    @Autowired
    ProjectHistoryRepository projectHistoryRepository;

    @Autowired
    ProjectSummaryRepository projectSummaryRepository;

    @Autowired
    RiskLevelLookupRepository riskLevelLookupRepository;

    @Autowired
    IMSProjectImportMapper imsProjectImportMapper;

    @Autowired
    MessageService messageService;




    public FileImportResult importImsProjectFile(InputStream fileInputStream) {
        return importImsProjectFile(fileInputStream, 9999);
    }

    public FileImportResult importImsProjectFile(InputStream fileInputStream, int maxRows) {
        log.info("Importing IMS projects");
        int imported =0;
        try {
            CSVFile csvFile = new CSVFile(fileInputStream);

            imported = this.importIMSProjects(csvFile, maxRows);
            log.info("Imported " + imported + " projects");

            importLogService.recordImport(ImportJobType.IMS_PROJECT_IMPORT);

            return new FileImportResult(imported, null);

        } catch (ValidationException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error processing file import", e);
            importLogService.recordError(ImportJobType.IMS_PROJECT_IMPORT, "Unable to read/process import file, please check format. ", 0, "");

            throw new ValidationException("Unable to import file: " + e.getMessage());
        }

    }

    public FileImportResult importImsUnitDetailsFile(InputStream fileInputStream) {
        return importImsUnitDetailsFile(fileInputStream, 9999);
    }

    public FileImportResult importImsUnitDetailsFile(InputStream fileInputStream, int maxRows) {
        log.info("Importing IMS Unit Details");
        importLogService.deleteAllErrorsByImportType(ImportJobType.IMS_UNIT_DETAILS_IMPORT);

        try {
            CSVFile csvFile = new CSVFile(fileInputStream);

            int imported = this.importIMSUnitsRowDetails(csvFile, maxRows);
            log.info("Imported " + imported + " projects");

            importLogService.recordImport(ImportJobType.IMS_UNIT_DETAILS_IMPORT);

            List<ImportErrorLog> allByImportJobType = importLogService.findAllErrorsByImportType(ImportJobType.IMS_UNIT_DETAILS_IMPORT);
            return new FileImportResult(imported, allByImportJobType);
        } catch (ValidationException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error processing file import", e);
            throw new ValidationException("Unable to import file: " + e.getMessage());
        }
    }

    public FileImportResult importImsClaimedUnitsFile(InputStream fileInputStream) {
        return importImsClaimedUnitsFile(fileInputStream, 9999);
    }

    private FileImportResult importImsClaimedUnitsFile(InputStream fileInputStream, int maxRows) {
        log.info("Importing IMS Claimed Unit Details");
        importLogService.deleteAllErrorsByImportType(ImportJobType.IMS_CLAIMED_UNITS_IMPORT);

        try {
            CSVFile csvFile = new CSVFile(fileInputStream);

            int imported = importImsClaimedUnitsFile(csvFile, maxRows);

            importLogService.recordImport(ImportJobType.IMS_CLAIMED_UNITS_IMPORT);

            List<ImportErrorLog> allByImportJobType = importLogService.findAllErrorsByImportType(ImportJobType.IMS_CLAIMED_UNITS_IMPORT);
            return new FileImportResult(imported, allByImportJobType);
        } catch (ValidationException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error processing file import", e);
            throw new ValidationException("Unable to import file: " + e.getMessage());
        }
    }

    public int importImsClaimedUnitsFile(CSVFile csvFile, int maxRows) throws IOException {
        int importCount = 0;

        while (csvFile.nextRow()) {
            if (++importCount > maxRows) {
                log.warn("Aborting import after {} rows", (importCount - 1));
                break;
            }

            int schemeID = csvFile.getInteger(SCHEME_ID);
            Project project = projectRepository.findFirstByLegacyProjectCode(schemeID);
            if (project == null) {
                importLogService.recordError(ImportJobType.IMS_CLAIMED_UNITS_IMPORT, "Unable to find project with schemeId: " + schemeID, csvFile.getRowIndex(), csvFile.getCurrentRowSource());
                continue;
            }

            try {
                imsProjectImportMapper.handleUpdatesToGrantSource(project, csvFile);
            } catch (Exception e) {
                importLogService.recordError(ImportJobType.IMS_CLAIMED_UNITS_IMPORT, e.getMessage(), csvFile.getRowIndex(), csvFile.getCurrentRowSource());
                continue;
            }

            projectRepository.save(project);
        }

        List<ImportErrorLog> allErrorsByImportType = importLogService.findAllErrorsByImportType(ImportJobType.IMS_CLAIMED_UNITS_IMPORT);
        if (allErrorsByImportType.size() > 0) {
            throw new ValidationException("Unable to import all claimed units data");
        }
        return importCount;
    }

    public FileImportResult importPcsProjectFile(InputStream fileInputStream) {
        log.info("Importing PCS projects");

        try {
            Organisation gla = organisationService.findOne(GLA_HNL_ID);

            CSVFile csvFile = new CSVFile(fileInputStream);

            int imported = this.importLegacyLandProjects(csvFile, gla);
            log.info("Imported " + imported + " projects");

            importLogService.recordImport(ImportJobType.PCS_PROJECT_IMPORT);

            List<ImportErrorLog> allByImportJobType = importLogService.findAllErrorsByImportType(ImportJobType.PCS_PROJECT_IMPORT);
            return new FileImportResult(imported, allByImportJobType);
        } catch (Exception e) {
            log.error("Error processing file import", e);
            throw new ValidationException("Unable to import file: " + e.getMessage());
        }
    }

    int importLegacyLandProjects(CSVFile csvFile, Organisation org) throws IOException {
        int importCount = 0;

        while (csvFile.nextRow()) {
            Integer pcsProjectId = csvFile.getInteger(PCS_NUMBER);
            if (getByLegacyProjectCode(pcsProjectId) != null) {
                importLogService.recordError(ImportJobType.PCS_PROJECT_IMPORT, "project with PCD ID " + pcsProjectId + " already exists!", csvFile.getRowIndex(), csvFile.getCurrentRowSource());
                log.warn("project with PCD ID {} already exists!", pcsProjectId);
                continue;
            }

            importCount++;
            try {
                Project project = new Project();

                String programmeName = csvFile.getString(PROGRAMME);
                Programme programme = programmeService.findByName(programmeName);
                if (programme == null) {
                    importLogService.recordError(ImportJobType.PCS_PROJECT_IMPORT, "Unable to find programme with name: " + programmeName, csvFile.getRowIndex(), csvFile.getCurrentRowSource());
                } else if (!programme.isEnabled()) {
                    importLogService.recordError(ImportJobType.PCS_PROJECT_IMPORT, "Programme with name: " + programmeName + " is not enabled", csvFile.getRowIndex(), csvFile.getCurrentRowSource());
                } else {
                    project.setProgramme(programme);

                    String templateName = csvFile.getString(TEMPLATE);
                    project.setTemplate(programme.getTemplate(templateName));
                    if (project.getTemplate() == null) {
                        importLogService.recordError(ImportJobType.PCS_PROJECT_IMPORT, "Template with name: " + templateName + " could not be found", csvFile.getRowIndex(), csvFile.getCurrentRowSource());
                    } else {
                        project.setOrganisation(org);
                        project.setTitle(csvFile.getString(PROJECT_NAME));

                        project = projectService.createProject(project);

                        ProjectDetailsBlock detailsBlock = project.getDetailsBlock();
                        detailsBlock.setLegacyProjectCode(pcsProjectId);
                        detailsBlock.setProjectManager(csvFile.getString(PROJECT_MANAGER));
                        detailsBlock.setDescription(csvFile.getString(PROJECT_DESCRIPTION));
                        detailsBlock.setBorough(csvFile.getString(BOROUGH));
                        detailsBlock.setMainContact("");
                        detailsBlock.setMainContactEmail("");

                        handleProjectBudgetsUpdate(csvFile, project);

                        projectRepository.save(project);
                    }
                }
            } catch (Exception e) {
                log.error("Error in import PCS Project import:  " + e.getMessage());
                try {
                    importLogService.recordError(ImportJobType.PCS_PROJECT_IMPORT, e.getMessage(), csvFile.getRowIndex(), csvFile.getCurrentRowSource());
                } catch (IOException e1) {
                    log.error("Error with writing CSV details during error logging:  " + e.getMessage());
                }
            }
        }

        return importCount;
    }

    public int importIMSUnitsRowDetails(CSVFile csvFile, int maxRows) throws IOException {
        int importCount = 0;

        while (csvFile.nextRow()) {
            if (++importCount > maxRows) {
                log.warn("Aborting import after {} rows", (importCount - 1));
                break;
            }

            try {
                int schemeID = csvFile.getInteger(SCHEME_ID);
                Project project = projectRepository.findFirstByLegacyProjectCode(schemeID);
                if (project == null) {
                    importLogService.recordError(ImportJobType.IMS_UNIT_DETAILS_IMPORT, "Unable to find project with schemeId: " + schemeID, csvFile.getRowIndex(), csvFile.getCurrentRowSource());
                    continue;
                } else if (project.getSingleLatestBlockOfType(ProjectBlockType.UnitDetails) == null) {
                    importLogService.recordError(ImportJobType.IMS_UNIT_DETAILS_IMPORT, "No units block for project with schemeId: " + schemeID, csvFile.getRowIndex(), csvFile.getCurrentRowSource());
                    continue;
                }

                UnitDetailsBlock units = (UnitDetailsBlock) project.getSingleLatestBlockOfType(ProjectBlockType.UnitDetails);

                imsProjectImportMapper.mapIMSUnitDetails(units, csvFile);

                projectRepository.save(project);
            } catch (ValidationException e) {
                importLogService.recordError(ImportJobType.IMS_UNIT_DETAILS_IMPORT, e.getMessage(), csvFile.getRowIndex(), csvFile.getCurrentRowSource());
            } catch (Exception e) {
                log.error("Error in import IMS Unit Details import:  " + e.getMessage(), e);
                try {
                    importLogService.recordError(ImportJobType.IMS_UNIT_DETAILS_IMPORT, "Error: " + e.getMessage(), csvFile.getRowIndex(), csvFile.getCurrentRowSource());
                } catch (IOException e1) {
                    log.error("Error with writing CSV details during error logging:  " + e.getMessage());
                }
            }
        }
        List<ImportErrorLog> allErrorsByImportType = importLogService.findAllErrorsByImportType(ImportJobType.IMS_UNIT_DETAILS_IMPORT);
        if (allErrorsByImportType.size() > 0) {
            throw new ValidationException("Unable to import all projects");
        }
        return importCount;
    }

    public int importIMSProjects(CSVFile csvFile, int maxRows) throws Exception  {
        int importCount = 0;
        importLogService.deleteAllErrorsByImportType(ImportJobType.IMS_PROJECT_IMPORT);
        while (csvFile.nextRow()) {
            if (++importCount > maxRows) {
                log.warn("Aborting import after {} rows", (importCount - 1));
                break;
            }

            try {
                Project project = new Project();

                String programmeName = csvFile.getString(IMS_PROGRAMME_NAME);
                String organisationCode = csvFile.getString(IMS_ORGANISATION_CODE);
                String projectName = csvFile.getString(IMS_PROJECT_NAME);
                String opsProjectStatus = csvFile.getString(OPS_STATUS);
                Organisation organisation = organisationRepository.findFirstByImsNumber(organisationCode);
                int legacyProjectCode = csvFile.getInteger(SCHEME_ID);
                Programme programme = programmeService.findByName(programmeName);

                if (!(ProjectStatus.Active.name().equals(opsProjectStatus) || ProjectStatus.Closed.name().equals(opsProjectStatus))) {
                    importLogService.recordError(ImportJobType.IMS_PROJECT_IMPORT, "Unrecognised Status: " + opsProjectStatus, csvFile.getRowIndex(), csvFile.getCurrentRowSource());
                } else if (organisation == null) {
                    importLogService.recordError(ImportJobType.IMS_PROJECT_IMPORT, "Unable to find organisation with code: " + organisationCode, csvFile.getRowIndex(), csvFile.getCurrentRowSource());
                } else if (projectRepository.findFirstByLegacyProjectCode(legacyProjectCode) != null) {
                    importLogService.recordError(ImportJobType.IMS_PROJECT_IMPORT, "Project with schemeID already imported: " + legacyProjectCode, csvFile.getRowIndex(), csvFile.getCurrentRowSource());
                } else if (programme == null) {
                    importLogService.recordError(ImportJobType.IMS_PROJECT_IMPORT, "Unable to find programme with name: " + programmeName, csvFile.getRowIndex(), csvFile.getCurrentRowSource());
                } else {
                    log.debug("Importing project into {}", programme.getName());

                    Template template = programme.getTemplates().stream().sorted(Comparator.comparing(Template::getName)).findFirst().get();

                    project.setProgramme(programme);
                    project.setOrganisation(organisation);
                    project.setTemplate(template);
                    project.setTitle(projectName);

                    if (StringUtils.isNotEmpty(csvFile.getString(ORGANISATION_GROUP_NAME))) {
                        OrganisationGroup organisationGroup = organisationGroupRepository.findFirstByName(csvFile.getString(ORGANISATION_GROUP_NAME));
                        if (organisationGroup != null) {
                            project.setOrganisationGroupId(organisationGroup.getId());
                        }
                        else {
                            throw new ValidationException("could not find org group with name "+csvFile.getString(ORGANISATION_GROUP_NAME));
                        }
                    }

                    project = projectService.createProject(project);

                    ProjectDetailsBlock detailsBlock = project.getDetailsBlock();

                    detailsBlock.setLegacyProjectCode(legacyProjectCode);
                    detailsBlock.setDescription("Imported from IMS");
                    if (project.getOrganisationGroupId() != null) {
                        handleOrgGroupSpecificFields(csvFile, detailsBlock);
                    }
                    project.setOrgSelected(true);
                    imsProjectImportMapper.mapIMSRecordToProject(project, csvFile);


                    if (ProjectStatus.Active.name().equals(opsProjectStatus)) {
                        moveImportedProjectToStatus(project, new ProjectState(ProjectStatus.Active), "Migrated as part of IMS migration.");
                    } else if (ProjectStatus.Closed.name().equals(opsProjectStatus)) {
                        moveImportedProjectToStatus(project, new ProjectState(ProjectStatus.Closed, Completed), "Migrated as part of IMS migration.");
                    }
                }
            } catch (Exception e) {
                log.error("Error in import IMS Project import:  " + e.getMessage(), e);
                try {
                    importLogService.recordError(ImportJobType.IMS_PROJECT_IMPORT, "Error: " + e.getMessage(), csvFile.getRowIndex(), csvFile.getCurrentRowSource());
                } catch (IOException e1) {
                    log.error("Error with writing CSV details during error logging:  " + e.getMessage());
                }
            }
        }
        List<ImportErrorLog> allErrorsByImportType = importLogService.findAllErrorsByImportType(ImportJobType.IMS_PROJECT_IMPORT);
        if (allErrorsByImportType.size() > 0) {
            throw new ValidationException("Unable to import all projects");
        }

        return importCount;
    }



    public FileImportResult importImsAnswerCorrections(InputStream fileInputStream) {
        CSVFile csvFile = null;
        FileImportResult result = new FileImportResult();

        try {
            csvFile = new CSVFile(fileInputStream);
            return this.importImsAnswerCorrections(csvFile);
        } catch (IOException e) {
            throw new ValidationException("Unable to process CSV File: " + e.getMessage());
        }

    }

    public FileImportResult importImsAnswerCorrections(CSVFile csvFile) {
        importLogService.deleteAllErrorsByImportType(ImportJobType.IMS_ANSWER_CORRECTIONS_IMPORT);

        int count =0;
        while (csvFile.nextRow()) {
            try {
                int legacyProjectCode = csvFile.getInteger(SCHEME_ID);

                Project project = projectRepository.findFirstByLegacyProjectCode(legacyProjectCode);

                if (project.getProjectBlocks().stream().anyMatch(p -> p.getLockDetails()!=null)) {
                    importLogService.recordError(ImportJobType.IMS_ANSWER_CORRECTIONS_IMPORT,
                            String.format("Unable to import project with Scheme_ID of %d as it contains locked blocks", legacyProjectCode),
                            csvFile.getRowIndex(), csvFile.getCurrentRowSource());
                    continue;
                }

                imsProjectImportMapper.mapProjectQuestions(project, csvFile);
                projectRepository.save(project);
                count++;

            } catch (Exception e) {
                log.error("Error in import IMS Project Answer Corrections import:  " + e.getMessage(), e);
                try {
                    importLogService.recordError(ImportJobType.IMS_PROJECT_IMPORT, "Error: " + e.getMessage(), csvFile.getRowIndex(), csvFile.getCurrentRowSource());
                } catch (IOException e1) {
                    log.error("Error with writing CSV details during error logging:  " + e.getMessage());
                }
            }
        }
        List<ImportErrorLog> allErrorsByImportType = importLogService.findAllErrorsByImportType(ImportJobType.IMS_PROJECT_IMPORT);
        return new FileImportResult(count, allErrorsByImportType);

    }

    private void handleOrgGroupSpecificFields(CSVFile csvFile, ProjectDetailsBlock detailsBlock) {
        Organisation devOrg = organisationRepository.findFirstByNameIgnoreCase(csvFile.getString(DEV_ORG));
        String devLiabilityName = csvFile.getString(LIABILIY_DURING_DEV);
        String postCompletionOrg = csvFile.getString(LIABILIY_POST_COMPLETION);
        if (devOrg != null) {
            detailsBlock.setDevelopingOrganisationId(devOrg.getId());
        } else {
            throw new ValidationException("could not find developing organisation with name "+csvFile.getString(DEV_ORG));
        }
        if (StringUtils.isNotEmpty(devLiabilityName)) {
            Organisation devLiability = organisationRepository.findFirstByNameIgnoreCase(devLiabilityName);
            if (devLiability != null) {
                detailsBlock.setDevelopmentLiabilityOrganisationId(devLiability.getId());
            } else {
                throw new ValidationException("could not find organisation with development liability with name " + devLiabilityName);
            }
        }

        if (StringUtils.isNotEmpty(postCompletionOrg)) {
            Organisation postCompletionLiability = organisationRepository.findFirstByNameIgnoreCase(postCompletionOrg);
            if (postCompletionLiability != null) {
                detailsBlock.setPostCompletionLiabilityOrganisationId(postCompletionLiability.getId());
            } else {
                throw new ValidationException("could not find organisation with post completion liability with name " + postCompletionOrg);
            }
        }
    }

    private void moveImportedProjectToStatus(Project project, ProjectState state, String comments) {
        User user = userService.currentUser();
        OffsetDateTime now = environment.now();
        for (NamedProjectBlock namedProjectBlock : project.getProjectBlocks()) {
            namedProjectBlock.setBlockStatus(LAST_APPROVED);
            namedProjectBlock.setLastModified(now);
            namedProjectBlock.setApprovalTime(now);
            namedProjectBlock.setApproverUsername(user.getUsername());
        }
        if (ProjectStatus.Active.equals(state.getStatusType())) {
            projectService.createProjectHistoryEntry(project, ProjectHistory.Transition.Approved, "Approved by Migration", comments);
            project.setFirstApproved(now);
        } else {
            projectService.createProjectHistoryEntry(project, ProjectHistory.Transition.Closed, "Closed by Migration", comments);
        }
        project.setStatusName(state.getStatus());
        project.setSubStatusName(state.getSubStatus());

    }

    private void handleProjectBudgetsUpdate(CSVFile csvFile, Project project) {
        ProjectBudgetsBlock projectBudgets = (ProjectBudgetsBlock) project.getSingleLatestBlockOfType(ProjectBlockType.ProjectBudgets);
        if (projectBudgets != null) {

            String projectStart = getFinancialYearFromString(csvFile.getString(PROJECT_START_DATE));
            String projectEnd = getFinancialYearFromString(csvFile.getString(PROJECT_END_DATE));

            projectBudgets.setFromDate(projectStart);
            projectBudgets.setToDate(projectEnd);
        }
    }

    private String getFinancialYearFromString(String date) {
        if (!StringUtils.isEmpty(date)) {
            LocalDate dateTime = LocalDate.parse(date, IMPORT_DATE_FORMATTER);
            int year = dateTime.getYear();
            if (dateTime.getMonthValue() < 4) {
                year--;
            }
            int toYear = (year % 100) + 1;
            return year + "/" + (toYear == 100 ? 00 : String.format("%02d", toYear));
        }
        return null;
    }

}
