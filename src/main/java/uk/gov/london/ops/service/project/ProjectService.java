/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.service.project;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import uk.gov.london.ops.EventType;
import uk.gov.london.ops.FeatureStatus;
import uk.gov.london.ops.domain.EntityType;
import uk.gov.london.ops.domain.finance.LedgerStatus;
import uk.gov.london.ops.domain.finance.LedgerType;
import uk.gov.london.ops.domain.finance.PaymentGroup;
import uk.gov.london.ops.domain.importdata.ImportErrorLog;
import uk.gov.london.ops.domain.importdata.ImportJobType;
import uk.gov.london.ops.domain.notification.NotificationType;
import uk.gov.london.ops.domain.organisation.Organisation;
import uk.gov.london.ops.domain.organisation.OrganisationGroup;
import uk.gov.london.ops.domain.project.*;
import uk.gov.london.ops.domain.refdata.Borough;
import uk.gov.london.ops.domain.refdata.CategoryValue;
import uk.gov.london.ops.domain.refdata.Ward;
import uk.gov.london.ops.domain.template.*;
import uk.gov.london.ops.domain.user.EntitySubscription;
import uk.gov.london.ops.domain.user.Role;
import uk.gov.london.ops.domain.user.User;
import uk.gov.london.ops.exception.ForbiddenAccessException;
import uk.gov.london.ops.exception.NotFoundException;
import uk.gov.london.ops.exception.ValidationException;
import uk.gov.london.ops.mapper.AnnualSpendSummaryMapper;
import uk.gov.london.ops.mapper.IMSProjectImportMapper;
import uk.gov.london.ops.repository.*;
import uk.gov.london.ops.service.*;
import uk.gov.london.ops.service.finance.FinanceService;
import uk.gov.london.ops.service.project.state.ProjectState;
import uk.gov.london.ops.service.project.state.ProjectStateMachine;
import uk.gov.london.ops.service.project.state.StateMachine;
import uk.gov.london.ops.service.project.state.StateTransitionResult;
import uk.gov.london.ops.util.CSVFile;
import uk.gov.london.ops.web.model.AnnualSpendSummary;
import uk.gov.london.ops.web.model.LockRequestStatus;
import uk.gov.london.ops.web.model.ProjectLedgerItemRequest;
import uk.gov.london.ops.web.model.project.BulkProjectUpdateOperation;
import uk.gov.london.ops.web.model.project.BulkUpdateResult;
import uk.gov.london.ops.web.model.project.FileImportResult;
import uk.gov.london.ops.web.model.project.ProjectBlockHistoryItem;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static uk.gov.london.ops.domain.organisation.Organisation.GLA_HNL_ID;
import static uk.gov.london.ops.domain.project.NamedProjectBlock.Action.DELETE;
import static uk.gov.london.ops.domain.project.NamedProjectBlock.BlockStatus.LAST_APPROVED;
import static uk.gov.london.ops.domain.project.NamedProjectBlock.BlockStatus.UNAPPROVED;
import static uk.gov.london.ops.domain.project.Project.Status.*;
import static uk.gov.london.ops.domain.project.Project.SubStatus.*;
import static uk.gov.london.ops.domain.project.ProjectHistory.HistoryEventType.Transfer;
import static uk.gov.london.ops.domain.project.ProjectHistory.Transition.Created;
import static uk.gov.london.ops.domain.project.ProjectHistory.Transition.DeletedUnapprovedChanges;
import static uk.gov.london.ops.service.PermissionService.PROJ_VIEW_RECOMMENDATION;
import static uk.gov.london.ops.service.project.state.StateTransitionResult.Status.ILLEGAL_TRANSITION;
import static uk.gov.london.ops.service.project.state.StateTransitionResult.Status.INVALID;

/**
 * Service interface for managing projects.
 *
 * @author Steve Leach
 */
@Service
@Transactional
public class ProjectService extends BaseProjectService {

    public static final String QUESTION_ID = "532";
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
    public static final String IMS_ORGANISATION_NAME = "Organisation name";
    public static final String IMS_ORGANISATION_CODE = "Lead Org. Code";
    public static final String IMS_PROGRAMME_NAME = "Programme Selected";

    private static final DateTimeFormatter IMPORT_DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final String ORGANISATION_GROUP_TYPE = "Consortium/Partnership";
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
    BoroughRepository boroughRepository;

    @Autowired
    RiskLevelLookupRepository riskLevelLookupRepository;

    @Autowired
    CategoryValueRepository categoryValueRepository;

    @Autowired
    EntitySubscriptionRepository entitySubscriptionRepository;

    @Autowired
    IMSProjectImportMapper imsProjectImportMapper;

    public Page<ProjectSummary> findAll(String project, Integer organisationId, Integer programmeId, String programmeName,
                                        List<Project.Status> statuses, List<Project.SubStatus> subStatuses, Pageable pageable) {
        User currentUser = userService.loadCurrentUser();

        if (!currentUser.isApproved() ||
                (organisationId != null && !permissionService.currentUserHasPermissionForOrganisation(PermissionService.PROJ_READ, organisationId))) {
            throw new ForbiddenAccessException();
        }

        Page<ProjectSummary> result = projectSummaryRepository.findAll(currentUser, project, organisationId, programmeId, programmeName, statuses, subStatuses, pageable);
        cleanProjectSummaries(result.getContent());
        return result;
    }

    public List<ProjectSummary> getProjectSummaries(String title, Integer projectId, Integer orgId, Integer programmeId, String programmeFilter) {
        User currentUser = userService.loadCurrentUser();

        if (!currentUser.isApproved()) {
            throw new ForbiddenAccessException();
        }
        List<Integer> userOrgs = currentUser.getOrganisationIds();
        List<ProjectSummary> summaries = new ArrayList<>();

        if (projectId != null) {
            List<Integer> projectIDs = null;
            projectIDs = Arrays.asList(projectId);
            List<ProjectSummary> projects = projectSummaryRepository.findProjectsByOrgAndId(userOrgs, projectIDs);
            //Search by id or title. If can't find by id return by title
            if (projects.size() == 0 && title != null) {
                projects = projectSummaryRepository.findProjectsByOrgAndTitle(userOrgs, title.toLowerCase().trim());
            }
            summaries = projects;
        } else if (orgId != null) {
            List<Integer> orgIDs = null;
            if (!permissionService.currentUserHasPermissionForOrganisation(PermissionService.PROJ_READ, orgId)) {
                throw new ForbiddenAccessException();
            }
            orgIDs = Arrays.asList(orgId);
            summaries = projectSummaryRepository.findProjectsByOrgID(userOrgs, orgIDs);
        } else if (programmeId != null || programmeFilter != null) {
            List<Integer> programmeIDs = null;
            if (programmeId != null) {

                programmeIDs = new LinkedList<>();
                programmeIDs.add(programmeId);
                summaries = projectSummaryRepository.findProjectsByOrgAndProgramme(userOrgs, programmeIDs);
            } else if (programmeFilter != null) {
                List<Programme> programmes = programmeService.findAllByNameContaining(programmeFilter);
                programmeIDs = new LinkedList<>();
                for (Programme programme : programmes) {
                    programmeIDs.add(programme.getId());
                }
                if (programmeIDs.size() == 0) {
                    return new LinkedList<>();
                }
                summaries = projectSummaryRepository.findProjectsByOrgAndProgramme(userOrgs, programmeIDs);
            }
        } else if (title != null) {
            summaries = projectSummaryRepository.findProjectsByOrgAndTitle(userOrgs, title.toLowerCase().trim());
        } else {
            summaries = projectSummaryRepository.findAll(userOrgs);
        }
        return cleanProjectSummaries(summaries);
    }

    // removes recommendation for roels without permission to view it, annotation approach is too slow for large data volumes
    private List<ProjectSummary> cleanProjectSummaries(List<ProjectSummary> summaries) {
        User user = userService.currentUser();
        Set<String> permissionsForUser = permissionService.getPermissionsForUser(user);
        boolean showProperty = false;
        for (String userPermission : permissionsForUser) {
            if (PROJ_VIEW_RECOMMENDATION.equals(userPermission)) {
                showProperty = true;
            }
        }

        if (!showProperty) {
            for (ProjectSummary summary : summaries) {
                summary.setRecommendation(null);
            }
        }
        return summaries;
    }

    private void populateOrganisationGroups(Collection<Project> projects) {
        projects.stream().filter(project -> project.getOrganisationGroupId() != null).forEach(project -> {
            project.setOrganisationGroup(organisationGroupRepository.findOne(project.getOrganisationGroupId()));
        });
    }

    public Project updateProjectAnswers(Project project, Integer blockId, ProjectQuestionsBlock answers, boolean autosave) {

        NamedProjectBlock block = project.getProjectBlockById(blockId);
        if (block == null) {
            throw new ValidationException(String.format("Attempted to update block with id: %d, but none was found on project with id: %d", blockId, project.getId()));
        }

        checkForLock(block);
        block.merge(answers);
        releaseOrRefreshLock(block, !autosave);
        return this.updateProject(project);
    }

    public DesignStandardsBlock updateProjectDesignStandards(Integer projectId, DesignStandardsBlock designStandardsBlock) {
        Project project = get(projectId);

        checkForLock(project.getDesignStandardsBlock());

        project.getDesignStandardsBlock().merge(designStandardsBlock);

        deleteLock(project.getDesignStandardsBlock());

        updateProject(project);

        return project.getDesignStandardsBlock();
    }

    public ProjectRisksBlock updateProjectRisks(Integer projectId, Integer blockId, ProjectRisksBlock updatedBlock, boolean releaseLock) {
        Project project = get(projectId);
        ProjectRisksBlock existingBlock = project.getRisksBlock();

        checkForLock(existingBlock);

        existingBlock.merge(updatedBlock);

        releaseOrRefreshLock(existingBlock, releaseLock);

        updateProject(project);
        return existingBlock;
    }

    public GrantSourceBlock getProjectGrantSource(Integer projectId) {
        Project project = get(projectId);
        GrantSourceBlock grantSourceBlock = project.getGrantSourceBlock();

        grantSourceBlock.setAssociatedProjectFlagUpdatable(
                project.isAssociatedProjectsEnabled()
                        && !project.getMilestonesBlock().hasClaimedMilestones()
                        && organisationService.isStrategic(project.getOrganisation().getId(), project.getProgrammeId())
                        && !paymentService.hasPayments(projectId)
        );

        return grantSourceBlock;
    }

    public GrantSourceBlock updateProjectGrantSource(Integer projectId, GrantSourceBlock grantSourceBlock) {
        Project project = get(projectId);

        checkForLock(project.getGrantSourceBlock());
        resetReclaimMilestoneAmountsOnGrantSourceChange(project.getGrantSourceBlock(), grantSourceBlock);
        project.getGrantSourceBlock().merge(grantSourceBlock);
        deleteLock(project.getGrantSourceBlock());

        this.updateProject(project);

        return project.getGrantSourceBlock();
    }

    // If adjusting the grant source then reset the reclaims of any existing unapproved milestones of the same grant source type
    private void resetReclaimMilestoneAmountsOnGrantSourceChange(GrantSourceBlock block, GrantSourceBlock newBlock) {

        Project project = block.getProject();
        ProjectMilestonesBlock milestonesBlock = project.getMilestonesBlock();

        if (milestonesBlock != null && !milestonesBlock.isApproved()) {
            boolean resetGrant  = ObjectUtils.compare(block.getGrantValue() , newBlock.getGrantValue()) != 0;
            boolean resetDPF    = ObjectUtils.compare(block.getDisposalProceedsFundValue() , newBlock.getDisposalProceedsFundValue()) != 0;
            boolean resetRCGF   = ObjectUtils.compare(block.getRecycledCapitalGrantFundValue() , newBlock.getRecycledCapitalGrantFundValue()) != 0;
            resetReclaimMilestoneAmounts(milestonesBlock, resetGrant, resetDPF, resetRCGF);

        }
    }

    private void resetReclaimMilestoneAmounts(ProjectMilestonesBlock milestonesBlock, boolean resetGrant, boolean resetDPF, boolean resetRCGF) {
        for (Milestone milestone : milestonesBlock.getMilestones()) {
            if (resetGrant) {
                milestone.setReclaimedGrant(null);
            }
            if (resetDPF) {
                milestone.setReclaimedDpf(null);
            }
            if (resetRCGF) {
                milestone.setReclaimedRcgf(null);
            }
        }
    }

    public ProjectDetailsBlock updateProjectDetails(Integer projectId, ProjectDetailsBlock block) {
        Project project = get(projectId);
        NamedProjectBlock existingDetailsBlock = project.getSingleLatestBlockOfType(ProjectBlockType.Details);
        checkForLock(existingDetailsBlock);

        if (block.getBorough() != null) {
            Borough borough = boroughRepository.findByBoroughName(block.getBorough());
            if (borough == null) {
                throw new ValidationException(String.format("Borough with name %s is not recognised", block.getBorough()));
            }

            if (block.getWardId() != null) {
                boolean found = false;
                for (Ward ward : borough.getWards()) {
                    if (ward.getId().equals(block.getWardId())) {
                        found = true;
                    }
                }
                if (!found) {
                    throw new ValidationException(String.format("Ward with id %d is not a member of borough %s", block.getWardId(), block.getBorough()));
                }
            }
        }

        existingDetailsBlock.merge(block);

        deleteLock(existingDetailsBlock);
        this.updateProject(project);
        return (ProjectDetailsBlock) existingDetailsBlock;
    }

    public Project createProject(Project project) {
        setOrganisationOnProjectCreation(project);

        validateProjectForCreation(project);

        dataAccessControlService.checkAccess(project);

        if (project.getProgramme() != null) {
            // get real programme as Programme from project may be skeleton from UI
            Programme programme = programmeService.find(project.getProgrammeId());
            project.setManagingOrganisation(programme.getManagingOrganisation());
        }

        if (project.getTemplate() != null) {

            // need to re-inflate the template
            Template template = templateService.find(project.getTemplateId());
            updateProjectFromTemplate(project, template);
            List<TemplateBlock> blocksEnabled = template.getBlocksEnabled();
            for (TemplateBlock templateBlock : blocksEnabled) {
                NamedProjectBlock namedProjectBlock = templateBlock.getBlock().newProjectBlockInstance();

                namedProjectBlock.setProject(project);


                namedProjectBlock.initFromTemplate(templateBlock);
                namedProjectBlock.setHidden(templateBlock.getBlockAppearsOnStatus() != null && !Project.Status.Draft.equals(templateBlock.getBlockAppearsOnStatus()));
                project.addBlockToProject(namedProjectBlock);

            }

            if (template.isBlockPresent(ProjectBlockType.Details)) {
                ((ProjectDetailsBlock) project.getSingleBlockByType(ProjectBlockType.Details)).setTitle(project.getTitle());
            }

        }
        prepopulateProjectData(project);

        projectRepository.save(project);

        createProjectHistoryEntry(project, Created, null, null);

        return project;
    }

    private void updateProjectFromTemplate(Project project, Template template) {
        project.setTemplate(template);
        project.setStrategicProject(template.isStrategicTemplate());
        project.setAssociatedProjectsEnabled(template.isAssociatedProjectsEnabled());
        project.setInfoMessage(template.getInfoMessage());
    }

    private void prepopulateProjectData(Project project) {
        if (project.getCreatedOn() == null) {
            project.setCreatedOn(environment.now());
        }

        if (project.getLastModified() == null) {
            project.setLastModified(environment.now());
        }

        if (project.getCreatedBy() == null) {
            project.setCreatedBy(userService.currentUser().getUsername());
        }

        User currentUser = userService.currentUser();
        project.getDetailsBlock().setMainContact(currentUser.getFullName());
        project.getDetailsBlock().setMainContactEmail(currentUser.getUsername());
    }

    private void validateProjectForCreation(Project project) {
        if (project.getId() != null) {
            throw new ValidationException("New projects must not have an ID");
        }

        Programme programme = programmeService.find(project.getProgramme().getId());

        if (!userService.currentUser().isGla() && programme.isRestricted()) {
            throw new ValidationException("non-GLA user cannot use restricted programmes");
        }

        if (!programme.isEnabled()) {
            throw new ValidationException("cannot create project with disabled programme");
        }

        if (!userService.currentUser().isOpsAdmin()
                && !Objects.equals(programme.getManagingOrganisationId(), project.getOrganisation().getManagingOrganisationId())
                && !Objects.equals(programme.getManagingOrganisationId(), project.getOrganisation().getId())) {
            throw new ValidationException("cannot create a project in a programme you do not have access to");
        }
    }

    private void setOrganisationOnProjectCreation(Project project) {
        if (project.getOrganisation() != null) {
            Integer id = project.getOrganisation().getId();
            Organisation org = organisationRepository.findOne(id);
            if (org == null) {
                throw new ValidationException("Organisation specified is not recognised.");
            }
            else if(org.isTechSupportOrganisation()) {
                throw new ValidationException("Organisation cannot create project.");
            }
            project.setOrganisation(org);
        } else {
            project.setOrganisation(userService.currentUser().getOrganisations().iterator().next());
        }

        // check if the org is in a consortium, if not the user does not have to select an org or consortium
        Set<OrganisationGroup> orgGroups = organisationGroupService.getOrganisationGroupsByProgrammeAndOrganisation(
                project.getProgrammeId(), project.getOrganisation().getId());
        if (CollectionUtils.isEmpty(orgGroups)) {
            project.setOrgSelected(true);
        }
    }

    public void deleteProject(Project project) {
        projectRepository.delete(project);
    }

    public void deleteProject(Integer id) {
        if (environment.initTestData() && (id == -123)) {
            // If we are in an environment that supports artificial test data
            // and we ask to delete the "magic" project ID
            // then we delete all test projects.
            deleteTestProjects();
        } else {
            deleteAllProjectData(id);
        }
    }

    private void deleteAllProjectData(Integer projectId) {
        lockDetailsRepository.deleteAllByProjectId(projectId);
        projectHistoryRepository.deleteByProjectId(projectId);
        projectRepository.delete(projectId);
    }

    private void deleteTestProjects() {
        for (Project project : projectRepository.findAll()) {
            if (isTestProject(project)) {
                deleteAllProjectData(project.getId());
            }
        }
    }

    private boolean isTestProject(Project project) {
        return project.getTitle().startsWith("IT_PROJECT_")
                || project.getTitle().equals("IT Test Project");
    }

    void createProjectHistoryEntry(Project project, ProjectHistory.Transition transition, String description, String comments) {
        ProjectHistory projectHistory = new ProjectHistory();
        projectHistory.setProjectId(project.getId());
        projectHistory.setTransition(transition);
        projectHistory.setComments(comments);
        projectHistory.setCreatedOn(environment.now());
        projectHistory.setCreatedBy(userService.currentUser().getUsername());
        projectHistory.setDescription(description);
        project.getHistory().add(projectHistory);
    }

    public void saveDraftComments(Integer id, String comments) {
        Project project = get(id);

        List<ProjectHistory> projectHistory = getProjectHistory(id);
        ProjectHistory statusToUse = null;
        if (projectHistory != null && !projectHistory.isEmpty()) {
            for (ProjectHistory status : projectHistory) {
                if (ProjectHistory.Transition.Unconfirmed.equals(status.getTransition())) {
                    statusToUse = status;
                }
            }
        }

        if (statusToUse == null) {
            statusToUse = new ProjectHistory();
            statusToUse.setProjectId(project.getId());
            statusToUse.setTransition(ProjectHistory.Transition.Unconfirmed);
        }
        statusToUse.setComments(comments);
        statusToUse.setCreatedOn(environment.now());
        statusToUse.setCreatedBy(userService.currentUser().getUsername());

        projectHistoryRepository.save(statusToUse);
    }

    public List<ProjectHistory> getProjectHistory(Integer projectId) {
        dataAccessControlService.checkAccess(projectRepository.findOne(projectId));

        List<ProjectHistory> histories = projectHistoryRepository.findAllByProjectIdOrderByCreatedOnDesc(projectId);
        for (ProjectHistory history : histories) {
            User user = userService.find(history.getCreatedBy());
            history.setCreatedByFirstName(user.getFirstName());
            history.setCreatedByLastName(user.getLastName());
        }
        return histories;
    }

    public String getFullNameOfLastUserToRequestApproval(Project project) {
        dataAccessControlService.checkAccess(project);

        List<ProjectHistory> histories = projectHistoryRepository.findAllByProjectIdOrderByCreatedOnDesc(project.getId());

        for (ProjectHistory history : histories) {
            if (ProjectHistory.Transition.ApprovalRequested.equals(history.getTransition())) {
                User user = userService.find(history.getCreatedBy());
                if (user != null) {
                    return user.getFullName();
                } else { // unlikely we wont by able find user so soon after they auth'd but just in case
                    return history.getCreatedBy();
                }
            }
        }
        // should not be possible for this error message to occur.
        throw new ValidationException("Unable to identify last approval user");
    }


    public AnnualSpendSummary getAnnualSpendSummaryForSpecificYear(Project project, Integer year) {


        NamedProjectBlock singleLatestBlockOfType = project.getSingleLatestBlockOfType(ProjectBlockType.ProjectBudgets);

        if (singleLatestBlockOfType != null) {
            ProjectBudgetsBlock projectBudgets = (ProjectBudgetsBlock) singleLatestBlockOfType;
            AnnualSpendSummary yearSummary = financeService.getAnnualSpendForSpecificYear(projectBudgets.getId(), year);
            return yearSummary;
        }
        return null;
    }

    public AnnualSpendSummary updateAnnualSpendAndBudgetLedgerEntries(Project project, Integer year, BigDecimal revenue, BigDecimal capital, boolean autosave) {
        ProjectBudgetsBlock projectBudgets = (ProjectBudgetsBlock) project.getSingleLatestBlockOfType(ProjectBlockType.ProjectBudgets);
        checkForLock(projectBudgets);
        List<ProjectLedgerEntry> allByBlockIdAndFinancialYear = financeService.updateAnnualSpendAndBudgetLedgerEntries(project, year, revenue, capital);
        updateProject(project);
        AnnualSpendSummary annualSpendSummary = financeService.getAnnualSpendForSpecificYear(projectBudgets.getId(), year);
        releaseOrRefreshLock(projectBudgets, !autosave);
        return annualSpendSummary;
    }

    /**
     * handles bulk operation requests
     */
    public BulkUpdateResult handleBulkOperation(BulkProjectUpdateOperation projects) {
        BulkUpdateResult result = new BulkUpdateResult();
        for (Integer projectId : projects.getProjects()) {
            Project project = getEnrichedProject(projectId);
            StateTransitionResult stateTransitionResult = null;

            if (BulkProjectUpdateOperation.Operation.ASSESS.equals(projects.getOperation())) {
                stateTransitionResult = transitionProjectToStatus(project, new ProjectState(Assess, null), null);
            } else if (BulkProjectUpdateOperation.Operation.REVERT.equals(projects.getOperation())) {
                // temp do this here, might change when we do revert properly
                if (!Project.Status.Assess.equals(project.getStatus())) {
                    stateTransitionResult = new StateTransitionResult(ILLEGAL_TRANSITION,
                            "Project must be in assess status to be reverted");
                } else {
                    stateTransitionResult = transitionProjectToStatus(project, new ProjectState(Submitted, null), null);
                }
            } else {
                throw new ValidationException("Unrecognised bulk operation");
            }
            if (stateTransitionResult.wasSuccessful()) {
                result.recordResult(projectId, BulkUpdateResult.Result.SUCCESS);
            } else {
                result.recordResult(projectId, BulkUpdateResult.Result.FAILURE);
            }

        }
        return result;
    }

    public void testOnlyMoveProjectToStatus(Project project, Project.Status newStatus) {
        if (featureStatus.isEnabled(FeatureStatus.Feature.TestOnlyStatusTransitions)) {
            switch (newStatus) {
                case Assess:
                    project.setStatus(Assess);
                    projectRepository.save(project);
                    break;
            }
        } else {
            throw new ValidationException("Unable to make transition, this is a test api only.");
        }

    }

    public StateTransitionResult reinstateProject(Project project, String comments) {

        ProjectDetailsBlock detailsBlock = project.getDetailsBlock();
        OffsetDateTime approvalTime = detailsBlock.getApprovalTime();
        ProjectState state = new ProjectState(Project.Status.Active);
        if (approvalTime == null && detailsBlock.getVersionNumber() == 1) {
            state = new ProjectState(Project.Status.Draft);
        } else {
            for (NamedProjectBlock block : project.getProjectBlocks()) {
                if (UNAPPROVED.equals(block.getBlockStatus())) {
                    state.setSubStatus(UnapprovedChanges);
                    break;
                }
            }
        }


        createProjectHistoryEntry(project,  ProjectHistory.Transition.Reinstated, "Project Reinstated", comments);

        return transitionProjectToStatus(project, state, comments);
    }

    public StateTransitionResult transitionProjectToStatus(Project project, ProjectState targetState, String comments) {
        if (autoApprovalProjectAndFeatureDisabled(project)) {
            return new StateTransitionResult(INVALID, "Submitting of auto-approval projects is currently not enabled.");
        }

        ProjectStateMachine stateMachine = stateMachineForProject(project);

        ProjectState currentState = project.currentState();

        if (project.anyBlocksLocked()) {
            return new StateTransitionResult(INVALID, "Unable to submit project as at least 1 block is currently being edited.");
        }

        Set<ProjectState> allowedTransitions = stateMachine.getAllowedTransitionsFor(
                new ProjectState(project.getStatus(), project.getSubStatus()),
                getUserRolesForProject(project),
                project.getProgramme().isEnabled(),
                !StringUtils.isEmpty(comments),
                project.isComplete(),
                project.getApprovalWillCreatePendingPayment() || project.getApprovalWillCreatePendingReclaim());

        if (!allowedTransitions.contains(targetState)) {
            return new StateTransitionResult(INVALID, currentState, targetState, project.getId());
        }

        preStateTransitionActions(project, targetState);

        project.setState(targetState);
        performProjectUpdate(project);

        ProjectHistory.Transition transition = stateMachine.transitionFor(currentState, targetState);
        postStateTransitionActions(project, transition, currentState, targetState, comments);

        return new StateTransitionResult(StateTransitionResult.Status.SUCCESS);
    }

    /**
     * This method does not actually make the state transition but is used by the API to verify the transition can be made.
     */
    public void validateTransitionProjectToStatus(Project project, ProjectState projectState) {
        preStateTransitionActions(project, projectState);
    }

    private boolean autoApprovalProjectAndFeatureDisabled(Project project) {
        return StateMachine.AUTO_APPROVAL.equals(project.getStateMachine()) && (!featureStatus.isEnabled(FeatureStatus.Feature.SubmitAutoApprovalProject));
    }

    void preStateTransitionActions(Project project, ProjectState targetState) {
        if (targetState.getStatus().equals(Returned)) {
            project.setRecommendation(null);
        }

        if (targetState.equals(Active, null) && !Closed.equals(project.getStatus())) {
            approveAllProjectBlocks(project);
        }

        if (targetState.equals(Active, PaymentAuthorisationPending)) {
            //If the project will create pending  and hasn't got SAP vendor id
            if (project.getApprovalWillCreatePendingGrantPayment() && StringUtils.isEmpty(project.getOrganisation().getsapVendorId())) {
                throw new ValidationException("SAP vendor ID has not been provided. The SAP vendor ID must be added to the organisation details by a OPS Admin.");
            }

            if (project.isPendingContractSignature()) {
                throw new ValidationException("Pending payments cannot be submitted for authorisation as the contract for this project type has not been signed.");
            }

            if (project.getGrantSourceAdjustmentAmount() < 0) {
                // grant reclaim needed
                for (Milestone milestone : project.getMilestonesBlock().getMilestones()) {
                    Long newGrantClaimed = project.getMilestonesBlock().getMilestoneGrantClaimed(milestone.getId());
                    if (newGrantClaimed != null) {
                        Long original = milestone.getClaimedGrant();
                        milestone.setReclaimedGrant(original - newGrantClaimed);
                    }

                }
            }
        }

        if (targetState.equals(Closed, Completed)) {
            if (!(project.getStatus().equals(Active) && project.getSubStatus() == null)) {
                throw new ValidationException("Project cannot be complete at this stage");
            }
            boolean incomplete = false;
            boolean unapproved = false;
            for (NamedProjectBlock block : project.getLatestProjectBlocks()) {
                if (!block.isApproved()) {
                    unapproved = true;
                }
                if (!block.isComplete()) {
                    incomplete = true;
                }
            }
            if (project.isAutoApproval() && incomplete) {
                throw new ValidationException("Cannot complete a project with incomplete blocks");

            }
            if (!project.isAutoApproval() && (incomplete || unapproved)) {
                throw new ValidationException("Cannot complete a project with unapproved or incomplete blocks");
            }

            ProjectMilestonesBlock milestones = (ProjectMilestonesBlock) project.getSingleLatestBlockOfType(ProjectBlockType.Milestones);
            if (project.getTemplate().isBlockPresent(ProjectBlockType.Milestones)) {
                for (Milestone milestone : milestones.getApplicableMilestones()) {
                    if (project.isAutoApproval() && project.getTemplate().getAllowMonetaryMilestones() && !milestone.isManuallyCreated() && !milestone.isClaimed()) {
                        throw new ValidationException("All mandatory milestones that apply to this project must be claimed before the project can be closed as complete");
                    } else if (!project.isAutoApproval() && !milestone.isManuallyCreated() && !milestone.isApproved()) {
                        throw new ValidationException("All mandatory milestones must be claimed and approved before the project can be closed as complete");
                    }
                }
            }

        }

        if (targetState.equals(Closed, Abandoned) || targetState.equals(Active, AbandonPending)) {
            if (project.isStrategicProject() && projectRepository.countAssociatedProjects(project.getProgrammeId()) > 0) {
                throw new ValidationException("This project has associated projects and cannot be abandoned.");
            }

            if (project.isPendingPayments() || !userService.currentUser().isOpsAdmin() && paymentService.hasPayments(project.getId())) {
                throw new ValidationException("Project cannot be abandoned at this stage.");
            }
        }
    }

    void postStateTransitionActions(Project project, ProjectHistory.Transition transition, ProjectState currentState, ProjectState targetState, String comments) {
        String historyDescription = projectHistoryDescription(transition, project);

        if (transition != null) {
            if (targetState.getStatus().equals(Submitted)) {
                project.getHistory().removeIf(e -> ProjectHistory.Transition.Unconfirmed.equals(e.getTransition()));
            }
            createProjectHistoryEntry(project, transition, historyDescription, comments);
        }

        if (targetState.getStatus().equals(Assess)) {
            auditService.auditCurrentUserActivity(String.format("Project with ID %d was moved to status of assessed.", project.getId()));
        }

        if (targetState.equals(Returned) || (currentState.equals(Active, ApprovalRequested) && targetState.equals(Active, UnapprovedChanges))) {
            String message = String.format("Project P%d for %s has been returned and may require updates", project.getId(), project.getOrganisation().getName());
            notificationService.createNotification(NotificationType.Info, message, project);
        }

        if (targetState.equals(Active, ApprovalRequested)) {
            String message = String.format("Project P%d for %s has updates requiring approval", project.getId(), project.getOrganisation().getName());
            notificationService.createNotification(NotificationType.Action, message, project);
        }

        if (targetState.equals(Active, PaymentAuthorisationPending)) {
            String approvalRequestedBy = this.getFullNameOfLastUserToRequestApproval(project);
            PaymentGroup paymentGroup = paymentService.generatePaymentsForClaimedMilestones(project, approvalRequestedBy);

            String message = String.format("A payment for project P%d is awaiting authorisation", project.getId());
            notificationService.createNotification(NotificationType.Action, message, paymentGroup, Role.GLA_SPM);
        }

        updateBlocksVisibility(project);
    }

    private String projectHistoryDescription(ProjectHistory.Transition transition, Project project) {
        String historyDescription = null;
        if (ProjectHistory.Transition.ApprovalRequested.equals(transition)) {
            historyDescription = "Approval requested for unapproved blocks " + unapprovedBlockDisplayNames(project);
        }
        if (ProjectHistory.Transition.Returned.equals(transition) && project.getStatus().equals(Active)) {
            historyDescription = "Returned to organisation";
        }
        if (ProjectHistory.Transition.Approved.equals(transition) && project.isAutoApproval()) {
            historyDescription = "Project saved to active";
        }
        return historyDescription;
    }

    private String unapprovedBlockDisplayNames(Project project) {
        return project.getLatestProjectBlocks()
                .stream()
                .filter(b -> UNAPPROVED.equals(b.getBlockStatus()))
                .map(NamedProjectBlock::getBlockDisplayName)
                .collect(Collectors.joining(","));
    }

    void approveAllProjectBlocks(Project project) {
        String username = userService.currentUser().getUsername();
        OffsetDateTime now = environment.now();

        if(project.getStatus().equals(Project.Status.Assess) || (project.getStatus().equals(Project.Status.Draft) && project.isAutoApproval())) {
            project.setFirstApproved(now);
        }

        //approve milestone block first due to dependancy on Grant Source
        ProjectMilestonesBlock milestonesBlock = project.getMilestonesBlock();
        if (milestonesBlock != null) {
            if (milestonesBlock.isApproved()) {
                milestonesBlock.updateClaimAmounts();
            } else {
                approveIndividualBlock(project, username, now, milestonesBlock);
            }
        }
        for (NamedProjectBlock block : project.getLatestProjectBlocks()) {

            if (block != milestonesBlock) {
                approveIndividualBlock(project, username, now, block);
            }
        }
    }

    private void approveIndividualBlock(Project project, String username, OffsetDateTime now, NamedProjectBlock block) {
        if (block.isApproved()) {
            return;
        }

        if (!block.isNewBlock() && !block.isComplete()) {
            // can't approve blocks if a non-new block is incomplete
            throw new ValidationException(String.format("Unable to approve block '%s' as it is incomplete", block.getBlockDisplayName()));
        }

        if (block.isComplete()) {
            project.approveBlock(block, username, now);
        }

        //Hack to force the project to be Active(no subStatus) after approving
        if (block.isNewBlock() && !block.isComplete()) {
            block.setLastModified(null);
        }
    }

    /**
     * On state transition, this method updates the project blocks "hidden" field if applicable.
     */
    private void updateBlocksVisibility(Project project) {
        project.getProjectBlocks().stream()
                .filter(block -> project.getStatus().equals(block.getBlockAppearsOnStatus()) && block.isHidden())
                .forEach(block -> {
                    block.setHidden(false);
                    project.getLatestProjectBlocks().add(block);
                });

        projectRepository.save(project);
    }

    public NamedProjectBlock getBlockAndLock(Project project, NamedProjectBlock block) {
        return getBlockAndLock(project, block, true);
    }

    public NamedProjectBlock getBlockAndLock(Project project, NamedProjectBlock block, boolean lock) {
        NamedProjectBlock blockToReturn = block;
        if (block.editRequiresCloning(environment.now())) {
            String userName = userService.currentUser() == null ? userService.getSystemUserName() : userService.currentUser().getUsername();

            if (project.isAutoApproval()) {
                project.approveBlock(block, userName, environment.now());
            } else if (project.getSubStatus() == null) {
                // the unapproved flag is set pre-saving, and we want to add an unapproved history entry only once until project is approved
                createProjectHistoryEntry(project, ProjectHistory.Transition.Amend, null, "Active: Unapproved Changes");
            }

            NamedProjectBlock clone = block.cloneBlock(userName, environment.now());
            block.setLatestVersion(false);

            if (block.getProject().isAutoApproval()) {
                clone.setReportingVersion(true);
                block.setReportingVersion(false);
            } else {
                clone.setReportingVersion(false);
            }

            project.getLatestProjectBlocks().remove(block);
            project.getLatestProjectBlocks().add(clone);

            project.addBlockToProject(clone);
            project = projectRepository.save(project);

            blockToReturn = project.getLatestBlockOfType(clone.getBlockType(), clone.getDisplayOrder());
            performPostCloneActions(block, blockToReturn);
        }

        if (lock) {
            tryLock(project, blockToReturn);
        }
        return blockToReturn;
    }

    private void performPostCloneActions(NamedProjectBlock sourceBlock, NamedProjectBlock clonedBlock) {
        for (PostCloneNotificationListener cloneListener : cloneListeners) {
            cloneListener.handleBlockClone(sourceBlock.getProject(), sourceBlock.getId(), clonedBlock.getId());
        }
    }

    private void performPostProjectCloneActions(NamedProjectBlock sourceBlock, NamedProjectBlock clonedBlock) {
        for (PostCloneNotificationListener cloneListener : cloneListeners) {
            cloneListener.handleProjectClone(sourceBlock.getProject(), sourceBlock.getId(), clonedBlock.getProject(), clonedBlock.getId());
        }
    }


    /**
     * Filters a collection of projects, returning only those that the current user has access to.
     */
    public List<Project> filterByUserAccess(Collection<Project> projects) {
        List<Project> results = new LinkedList<>();

        User user = userService.currentUser();

        for (Project project : projects) {
            if (dataAccessControlService.hasAccess(user, project)) {
                results.add(project);
            }
        }

        return results;
    }

    public List<Project> findAllForQuestionId(int questionId) {
        return projectRepository.findAllForQuestion(questionId);
    }

    public Collection<Project> getProjectbyProgrammeId(int id) {
        return projectRepository.findAllByProgramme(new Programme(id, ""));
    }

    public List<Project> getProjectsForProgramme(Integer id, String name) {
        List<Project> projects = new ArrayList<>();
        List<Programme> programmes = new ArrayList<>();

        if (id != null) {
            programmes.add(programmeService.getById(id));
        } else {
            programmes = programmeService.findAllByNameContaining(name);
        }

        for (Programme programme : programmes) {
            projects.addAll(projectRepository.findAllByProgramme(programme));
        }

        populateOrganisationGroups(projects);

        projects = filterByUserAccess(projects);

        return projects;
    }

    @Scheduled(fixedDelayString = "${lock.time.checker.run.interval.milliseconds}")
    public void deleteExpiredLocks() {
        int count = lockDetailsRepository.deleteAllByLockTimeoutTimeBefore(OffsetDateTime.now());
        log.debug(String.format("Deleted %d timedout project locks.", count));
    }

    // tries to lock given block
    public LockRequestStatus tryLock(Project fromDB, NamedProjectBlock block) {
        if (block.getLockDetails() != null) {
            return new LockRequestStatus(false, block.getLockDetails());
        } else {
            LockDetails lockDetails = lockProjectBlock(fromDB, block);
            return new LockRequestStatus(true, lockDetails);
        }
    }

    public LockDetails lockProjectBlock(Project project, NamedProjectBlock block) {
        User user = userService.currentUser();
        LockDetails ld = new LockDetails(user, lockTimeoutInMinutes);
        block.setLockDetails(ld);
        projectRepository.save(project);
        return ld;
    }

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

                        project = createProject(project);

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

                if (!(Project.Status.Active.name().equals(opsProjectStatus) || Project.Status.Closed.name().equals(opsProjectStatus))) {
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

                    project = this.createProject(project);

                    ProjectDetailsBlock detailsBlock = project.getDetailsBlock();

                    detailsBlock.setLegacyProjectCode(legacyProjectCode);
                    detailsBlock.setDescription("Imported from IMS");
                    if (project.getOrganisationGroupId() != null) {
                        handleOrgGroupSpecificFields(csvFile, detailsBlock);
                    }
                    project.setOrgSelected(true);
                    imsProjectImportMapper.mapIMSRecordToProject(project, csvFile);


                    if (Project.Status.Active.name().equals(opsProjectStatus)) {
                        moveImportedProjectToStatus(project, new ProjectState(Project.Status.Active), "Migrated as part of IMS migration.");
                    } else if (Project.Status.Closed.name().equals(opsProjectStatus)) {
                        moveImportedProjectToStatus(project, new ProjectState(Project.Status.Closed, Completed), "Migrated as part of IMS migration.");
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
            namedProjectBlock.approve(user.getUsername(), now);
        }
        if (Project.Status.Active.equals(state.getStatus())) {
            createProjectHistoryEntry(project, ProjectHistory.Transition.Approved, "Approved by Migration", comments);
            project.setFirstApproved(now);
        } else {
            createProjectHistoryEntry(project, ProjectHistory.Transition.Closed, "Closed by Migration", comments);
        }
        project.setStatus(state.getStatus());
        project.setSubStatus(state.getSubStatus());

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

    private void handleProjectBudgetsUpdate(CSVFile csvFile, Project project) {
        ProjectBudgetsBlock projectBudgets = (ProjectBudgetsBlock) project.getSingleLatestBlockOfType(ProjectBlockType.ProjectBudgets);
        if (projectBudgets != null) {

            String projectStart = getFinancialYearFromString(csvFile.getString(PROJECT_START_DATE));
            String projectEnd = getFinancialYearFromString(csvFile.getString(PROJECT_END_DATE));

            projectBudgets.setFromDate(projectStart);
            projectBudgets.setToDate(projectEnd);
        }
    }


    public Project recordRecommendation(Integer id, Project.Recommendation recommendation, String comments) {
        Project project = this.get(id);
        if (Project.Status.Assess.equals(project.getStatus())) {
            if (Project.Recommendation.RecommendRejection.equals(recommendation)) {
                if (StringUtils.isEmpty(comments)) {
                    throw new ValidationException("Comments are mandatory if recommending for rejection.");
                }
            }
            project.setRecommendation(recommendation);
            performProjectUpdate(project);
        } else {
            // TODO : remove this once recording recommendation is part of the generic state transition code
            throw new ValidationException("Unable to record recommendation for project not in Assess status.");
        }
        createProjectHistoryEntry(project, ProjectHistory.Transition.Initial_Assessment, null, comments);
        return project;
    }

    public List<ProjectBlockHistoryItem> getHistoryForBlock(Project fromDB, Integer blockId) {
        NamedProjectBlock block = fromDB.getProjectBlockById(blockId);
        List<NamedProjectBlock> blocksByType = fromDB.getBlocksByType(block.getBlockType());
        List<ProjectBlockHistoryItem> blocksToUse = new ArrayList<>();

        for (NamedProjectBlock namedProjectBlock : blocksByType) {
            if (namedProjectBlock.getDisplayOrder().equals(block.getDisplayOrder())) {
                if (NamedProjectBlock.BlockStatus.UNAPPROVED.equals(namedProjectBlock.getBlockStatus())) {

                    blocksToUse.add(new ProjectBlockHistoryItem(
                            fromDB.getId(), namedProjectBlock.getId(), namedProjectBlock.getBlockStatus(), namedProjectBlock.getVersionNumber(),
                            namedProjectBlock.getLastModified(), userService.getUserFullName(namedProjectBlock.getModifiedBy())));
                } else {
                    blocksToUse.add(new ProjectBlockHistoryItem(
                            fromDB.getId(), namedProjectBlock.getId(), namedProjectBlock.getBlockStatus(), namedProjectBlock.getVersionNumber(),
                            namedProjectBlock.getApprovalTime(), userService.getUserFullName(namedProjectBlock.getApproverUsername())));
                }
            }
        }
        Collections.sort(blocksToUse, (a, b) ->
                a.getBlockVersion().compareTo(b.getBlockVersion()));

        return blocksToUse;
    }

    public void deleteUnapprovedBlock(Integer projectId, Integer blockId) {
        Project project = get(projectId);
        NamedProjectBlock block = project.getProjectBlockById(blockId);
        User currentUser = userService.currentUser();

        if (!projectBlockActivityMap.isActionAllowed(project, block, currentUser, DELETE)) {
            throw new ValidationException("DELETE action not allowed!");
        }

        if ((block.getLockDetails() != null) && !block.getLockDetails().getUsername().equals(currentUser.getUsername())) {
            throw new ValidationException(String.format("This block is being edited by %s and cannot be deleted", block.getLockDetails().getUsername()));
        }

        // clear any reclaimed milestones from grant source block
        if (block instanceof GrantSourceBlock) {
            if (!project.getMilestonesBlock().isApproved()) {
                resetReclaimMilestoneAmounts(project.getMilestonesBlock(), true, true, true);
            }
        }

        project.getProjectBlocks().removeIf(b -> b.getId().equals(block.getId()));
        updateProject(project);

        List<NamedProjectBlock> allBlocksOfType = project.getBlocksByTypeAndDisplayOrder(block.getBlockType(), block.getDisplayOrder());

        // find latest block and set latest version flag.
        allBlocksOfType.stream().filter(b -> LAST_APPROVED.equals(b.getBlockStatus()))
                .forEach(b -> {
                    b.setLatestVersion(true);
                    project.getLatestProjectBlocks().add(b);
                });

        if (!project.hasUnapprovedBlocks()) {
            createProjectHistoryEntry(project, DeletedUnapprovedChanges, "", "");
        }

        auditService.auditCurrentUserActivity(String.format("%s block unapproved version deleted from project %d", block.getBlockDisplayName(), project.getId()));
    }


    public Project cloneProject(Integer id, String newTitle) {
        Project sourceProject = get(id);
        Project clonedProject = this.copyProjectProperties(sourceProject, new Project());

        if (newTitle != null) {
            clonedProject.setTitle(newTitle);
        }


        sourceProject.getProjectBlocks().forEach(block -> {
            NamedProjectBlock clonedBlock = this.cloneBlock(block);
            clonedBlock.setProject(clonedProject);
            clonedProject.getProjectBlocks().add(clonedBlock);

            if (sourceProject.getLatestProjectBlocks().contains(block)) {
                clonedProject.getLatestProjectBlocks().add(clonedBlock);
            }
            projectRepository.save(clonedProject);

        });

        final Project finalProject = projectRepository.save(clonedProject);

        sourceProject.getProjectBlocks().forEach(block -> {
            List<NamedProjectBlock> blocksByType = finalProject.getBlocksByType(block.getBlockType());

            for (NamedProjectBlock namedProjectBlock : blocksByType) {
                if (namedProjectBlock.getVersionNumber().equals(block.getVersionNumber())) {
                    performPostProjectCloneActions(block, namedProjectBlock);
                }
            }
        });

        Project cloned = projectRepository.save(finalProject);

        List<ProjectHistory> history = projectHistoryRepository.findAllByProjectIdOrderByCreatedOnDesc(sourceProject.getId());
        for (ProjectHistory projectHistory : history) {
            ProjectHistory clone = new ProjectHistory();
            clone.setProjectId(cloned.getId());
            clone.setCreatedBy(projectHistory.getCreatedBy());
            clone.setTransition(projectHistory.getTransition());
            clone.setCreatedOn(projectHistory.getCreatedOn());
            clone.setComments(projectHistory.getComments());
            clone.setDescription(projectHistory.getDescription());
            projectHistoryRepository.save(clone);
        }

        List<EntitySubscription> subscriptions = entitySubscriptionRepository.findAllByEntityTypeAndEntityId(EntityType.project, sourceProject.getId());
        for (EntitySubscription subscription : subscriptions) {
            entitySubscriptionRepository.save(new EntitySubscription(subscription.getUsername(), EntityType.project, cloned.getId()));
        }

        // this will force loading of sorted blocks etc.
        return getEnrichedProject(cloned.getId());
    }

    private Project copyProjectProperties(Project fromProject, Project toProject) {
        toProject.setTitle(fromProject.getTitle());
        toProject.setStatus(fromProject.getStatus());
        toProject.setSubStatus(fromProject.getSubStatus());
        toProject.setRecommendation(fromProject.getRecommendation());
        toProject.setTemplate(fromProject.getTemplate());
        toProject.setProgramme(fromProject.getProgramme());

        toProject.setOrganisation(fromProject.getOrganisation());
        toProject.setOrganisationGroup(fromProject.getOrganisationGroup());
        toProject.setOrgSelected(fromProject.isOrgSelected());
        toProject.setOrganisationGroupId(fromProject.getOrganisationGroupId());
        toProject.setManagingOrganisation(fromProject.getManagingOrganisation());

        toProject.setTotalGrantEligibility(fromProject.getTotalGrantEligibility());
        toProject.setPendingPayments(fromProject.isPendingPayments());
        toProject.setAllowedTransitions(fromProject.getAllowedTransitions());
        toProject.setMessages(fromProject.getMessages());
        toProject.setStrategicProject(fromProject.isStrategicProject());
        toProject.setAssociatedProjectsEnabled(fromProject.isAssociatedProjectsEnabled());

        toProject.setCreatedBy(fromProject.getCreatedBy());
        toProject.setCreatedOn(fromProject.getCreatedOn());
        toProject.setFirstApproved(fromProject.getFirstApproved());
        toProject.setLastModified(fromProject.getLastModified());
        return toProject;
    }

    private NamedProjectBlock cloneBlock(NamedProjectBlock block) {
        NamedProjectBlock namedProjectBlock = block.cloneBlock(block.getModifiedBy(), block.getLastModified());
        namedProjectBlock.setBlockStatus(block.getBlockStatus());
        namedProjectBlock.setVersionNumber(block.getVersionNumber());
        namedProjectBlock.setLatestVersion(block.isLatestVersion());
        namedProjectBlock.setApprovalTime(block.getApprovalTime());
        namedProjectBlock.setApproverUsername(block.getApproverUsername());
        namedProjectBlock.setReportingVersion(block.isReportingVersion());
        namedProjectBlock.setHidden(block.isHidden());
        namedProjectBlock.setAllowedActions(block.getAllowedActions());
        return namedProjectBlock;
    }

    public void refreshProjectStatus(final Set<Integer> projectIds, EventType eventType) {
        projectRepository.findAll(projectIds).forEach(p -> refreshProjectStatus(p, eventType));
    }

    private void refreshProjectStatus(final Project project, final EventType eventType) {

        if (Active.equals(project.getStatus()) && PaymentAuthorisationPending.equals(project.getSubStatus())) {
            if (EventType.PaymentAuthorised.equals(eventType)) {
                transitionProjectToStatus(project, new ProjectState(Active, null), null);
            } else if (EventType.PaymentDeclined.equals(eventType)) {
                project.setSubStatus(Project.SubStatus.ApprovalRequested);
                projectRepository.save(project);
            }
        }
    }

    /**
     * * Add a block(based on a template) to a list of projects by template
     * <p>
     * Notes:
     * - Assumes  projectBlocks in all projects are not null
     * - There is not synchronization, so in case of two simultaneous requests,
     * some projects can experiment display order duplications
     *
     * @param template
     * @param templateBlock
     * @return
     */
    public List<Project> addBlockToProjectsByTemplate(final Template template,
                                                      final TemplateBlock templateBlock) {

        final List<Project> projects = projectRepository.findAllByTemplate(template);
        for (final Project project : projects) {
            project.addBlockToProject(createBlockFromTemplate(project, templateBlock));
            project.setLastModified(environment.now());
        }
        return projectRepository.save(projects);
    }


    private NamedProjectBlock createBlockFromTemplate(final Project project, final TemplateBlock templateBlock) {
        NamedProjectBlock namedProjectBlock = templateBlock.getBlock().newProjectBlockInstance();
        namedProjectBlock.setProject(project);
        namedProjectBlock.initFromTemplate(templateBlock);
        namedProjectBlock.setHidden(Active.equals(templateBlock.getBlockAppearsOnStatus()) && !project.getStatus().equals(templateBlock.getBlockAppearsOnStatus()));

        return namedProjectBlock;
    }

    public Project moveProjectToProgrammeAndTemplate(Integer id, Integer progId, Integer templateId) {

        Project project = get(id);
        Programme newProgramme = programmeService.getById(progId);
        Template orginalTemplate = project.getTemplate();
        Programme orginalProgramme = project.getProgramme();

        // Check the template requested in in the programme
        Template newTemplate = null;
        for (Template template : newProgramme.getTemplates()) {
            if (template.getId().equals(templateId)) {
                newTemplate = template;
            }
        }

        if (newTemplate == null) {
            throw new NotFoundException(String.format("Template with id %d is not present on programme %s.", templateId, newProgramme.getName()));
        }

        if (!orginalTemplate.getId().equals(newTemplate.getCloneOfTemplateId())) {
            throw new ValidationException(String.format("Template with id %d is not a clone of the project's existing template: %d", newTemplate.getId(), orginalTemplate.getId()));
        }

        if (newTemplate.isCloneModified()) {
            throw new ValidationException("The template being moved to has been modified post cloning, it's not possible to move to this template.");

        }

        List<NamedProjectBlock> blocksByType = project.getBlocksByType(ProjectBlockType.Milestones);

        Set<TemplateBlock> milestoneBlocks = orginalTemplate.getBlocksByType(ProjectBlockType.Milestones);
        Map<Integer, Integer> oldMapIdToExternalId = new HashMap<>();
        for (TemplateBlock milestoneBlock : milestoneBlocks) {
            for (ProcessingRoute route : ((MilestonesTemplateBlock) milestoneBlock).getProcessingRoutes()) {
                oldMapIdToExternalId.put(route.getId(), route.getExternalId());
            }
        }

        milestoneBlocks = newTemplate.getBlocksByType(ProjectBlockType.Milestones);
        Map<Integer, Integer> newMapExternalIdToId = new HashMap<>();
        for (TemplateBlock milestoneBlock : milestoneBlocks) {
            for (ProcessingRoute route : ((MilestonesTemplateBlock) milestoneBlock).getProcessingRoutes()) {
                newMapExternalIdToId.put(route.getExternalId(), route.getId());
            }
        }

        for (NamedProjectBlock namedProjectBlock : blocksByType) {
            ProjectMilestonesBlock pmb = (ProjectMilestonesBlock) namedProjectBlock;
            if (pmb.getProcessingRouteId() != null) {
                Integer oldExternalId = oldMapIdToExternalId.get(pmb.getProcessingRouteId());
                Integer newProcessingRouteId = newMapExternalIdToId.get(oldExternalId);
                if (newProcessingRouteId == null) {
                    throw new ValidationException("Unable to move project as the processing routes for the milestones are not compatible ");
                }
                pmb.setProcessingRouteId(newProcessingRouteId);
            }
        }

        project.setProgramme(newProgramme);
        project.setTemplate(newTemplate);

        createProjectHistoryEntry(project, null, String.format("Transferred to %s from %s", newProgramme.getName(), orginalProgramme.getName()), "");

        return projectRepository.save(project);

    }

    public void updateProjectBlockLastModified(Integer projectId, Integer blockId, OffsetDateTime lastModified) {
        Project project = get(projectId);
        NamedProjectBlock block = project.getProjectBlockById(blockId);
        block.setLastModified(lastModified);
        projectRepository.save(project);
    }


    public ProjectRisksBlock createProjectRisk(Integer projectId, Integer blockId, ProjectRiskAndIssue risk, boolean releaseLock) {
        Project project = get(projectId);
        ProjectRisksBlock block = (ProjectRisksBlock) project.getSingleLatestBlockOfType(ProjectBlockType.Risks);
        if (!block.getId().equals(blockId)) {
            throw new ValidationException("Attempt to create risk on invalid block");
        }

        checkForLock(block);

        if (StringUtils.isEmpty(risk.getTitle())) {
            throw new ValidationException("title", "Title is mandatory for project risk");
        }
        if (StringUtils.isEmpty(risk.getDescription())) {
            throw new ValidationException("description", "Description is mandatory for project risk");
        }


        if (ProjectRiskAndIssue.Type.Risk.equals(risk.getType())) {
            if (risk.getRiskCategory() != null) {
                CategoryValue cat = categoryValueRepository.findOne(risk.getRiskCategory().getId());
                if (cat == null || !CategoryValue.Category.RiskCategory.equals(cat.getCategory())) {
                    throw new ValidationException("riskCategory", "Invalid Risk Category");
                }
            } else {
                throw new ValidationException("riskCategory", "Risk Category is mandatory for project risk");

            }

            if (risk.getInitialImpactRating() != null && risk.getInitialProbabilityRating() != null) {
                RiskLevelLookup one = riskLevelLookupRepository.findOne(new RiskLevelID(risk.getInitialImpactRating(), risk.getInitialProbabilityRating()));
                if (one == null) {
                    throw new ValidationException("Unable to find relevant initial Risk Level");
                }
                risk.setInitialRiskLevel(one);
            } else {
                throw new ValidationException("Initial Risk Ratings are mandatory");
            }
            if (risk.getResidualImpactRating() != null && risk.getResidualProbabilityRating() != null) {
                RiskLevelLookup one = riskLevelLookupRepository.findOne(new RiskLevelID(risk.getResidualImpactRating(), risk.getResidualProbabilityRating()));
                if (one == null) {
                    throw new ValidationException("Unable to find relevant residual Risk Level");
                }
                risk.setResidualRiskLevel(one);
            }
        } else {
            if (risk.getInitialImpactRating() == null) {
                throw new ValidationException("InitialImpactRating", "Unable to find relevant initial Impact Level");
            }
        }

        block.getProjectRiskAndIssues().add(risk);
        releaseOrRefreshLock(block, releaseLock);
        project = this.updateProject(project);
        return (ProjectRisksBlock) project.getSingleLatestBlockOfType(ProjectBlockType.Risks);

    }

    public ProjectRisksBlock addActionToRisk(Integer projectId, Integer blockId, Integer riskId, ProjectAction action, boolean releaseLock) {

        Project project = get(projectId);
        ProjectRisksBlock block = (ProjectRisksBlock) project.getSingleLatestBlockOfType(ProjectBlockType.Risks);
        if (!block.getId().equals(blockId)) {
            throw new ValidationException("Attempt to create risk on invalid block");
        }

        checkForLock(block);

        boolean found = false;
        for (ProjectRiskAndIssue riskAndIssue : block.getProjectRiskAndIssues()) {
            if (riskAndIssue.getId().equals(riskId)) {
                found = true;
                if (StringUtils.isEmpty(action.getAction())) {
                    throw new ValidationException("action", "Action is mandatory");
                }
                if (StringUtils.isEmpty(action.getOwner())) {
                    throw new ValidationException("owner", "Owner is mandatory");
                }
                action.setLastModified(environment.now());
                riskAndIssue.getActions().add(action);
            }
        }

        if (!found) {
            throw new ValidationException("Unable to find relevant risk to add action to.");
        }

        releaseOrRefreshLock(block, releaseLock);
        project = this.updateProject(project);
        return (ProjectRisksBlock) project.getSingleLatestBlockOfType(ProjectBlockType.Risks);
    }

    public void updateProjectRisk(Integer projectId, Integer blockId, Integer riskId, ProjectRiskAndIssue risk, boolean releaseLock) {
        Project project = get(projectId);
        ProjectRisksBlock block = project.getRisksBlock();

        checkForLock(block);

        ProjectRiskAndIssue existing = block.getRisk(riskId);
        existing.merge(risk);

        updateProject(project);

        releaseOrRefreshLock(block, releaseLock);
    }

    public void closeProjectRisk(Integer projectId, Integer blockId, Integer riskId, boolean releaseLock) {
        Project project = get(projectId);
        ProjectRisksBlock block = project.getRisksBlock();
        if (!block.getId().equals(blockId)) {
            throw new ValidationException("Invalid block id");
        }

        checkForLock(block);

        ProjectRiskAndIssue existing = block.getRisk(riskId);
        existing.setStatus(ProjectRiskAndIssue.Status.Closed);
        updateProject(project);

        releaseOrRefreshLock(block, releaseLock);
    }

    public void deleteProjectRisk(Integer projectId, Integer blockId, Integer riskId, boolean releaseLock) {
        Project project = get(projectId);
        ProjectRisksBlock block = project.getRisksBlock();

        checkForLock(block);

        ProjectRiskAndIssue risk = block.getRisk(riskId);
        block.getProjectRiskAndIssues().remove(risk);

        auditService.auditCurrentUserActivity(String.format("deleted %s with ID %d and title %s", risk.getType(), risk.getId(), risk.getTitle()));

        updateProject(project);

        releaseOrRefreshLock(block, releaseLock);
    }

    public void deleteActionFromRisk(Integer projectId, Integer blockId, Integer riskId, Integer actionId, boolean releaseLock) {
        Project project = get(projectId);
        ProjectRisksBlock block = project.getRisksBlock();

        checkForLock(block);

        ProjectRiskAndIssue risk = block.getRisk(riskId);

        boolean removed = false;
        for (Iterator<ProjectAction> iterator = risk.getActions().iterator(); iterator.hasNext(); ) {
            ProjectAction next = iterator.next();
            if (next.getId().equals(actionId)) {
                iterator.remove();
                auditService.auditCurrentUserActivity(String.format("deleted action with ID %d and text %s", next.getId(), next.getAction()));
                removed = true;
            }
        }

        if (!removed) {
            throw new ValidationException("Unable to find action to remove");
        }

        updateProject(project);

        releaseOrRefreshLock(block, releaseLock);
    }

    public AnnualSpendSummary createLedgerEntry(Integer projectId, ProjectLedgerItemRequest lineItem, Integer year) {
        Project project = projectRepository.findOne(projectId);
        ProjectBudgetsBlock projectBudgetsBlock = (ProjectBudgetsBlock) get(projectId).getSingleLatestBlockOfType(ProjectBlockType.ProjectBudgets);
        checkForLock(projectBudgetsBlock);

        lineItem.setBlockId(projectBudgetsBlock.getId());
        lineItem.setProjectId(projectId);

        financeService.addProjectLedgerEntry(lineItem);

        return getAnnualSpendSummaryForSpecificYear(project, year);
    }

    public void addQuestion(Template template, int blockDisplayOrder, TemplateQuestion templateQuestion) {
        List<Project> projects = projectRepository.findAllByTemplate(template);
        for (Project project : projects) {
            ProjectQuestionsBlock questionsBlock = (ProjectQuestionsBlock) project.getLatestBlockOfType(ProjectBlockType.Questions, blockDisplayOrder);
            questionsBlock.getAnswers().add(new Answer(templateQuestion.getQuestion()));
            questionsBlock.getQuestionEntities().add(templateQuestion);
        }
        projectRepository.save(projects);
    }

    public void updateAssociatedProjectsEnabled(Template template, Boolean enabled) {
        for (Project project : projectRepository.findAllByTemplate(template)) {
            project.setAssociatedProjectsEnabled(enabled);
            projectRepository.save(project);
        }
    }


    public void updateManagingOrgForProjectsOnProgramme(Programme programme, Organisation managingOrganisation) {

        if (!Objects.equals(programme.getManagingOrganisationId(), managingOrganisation.getId())) {
            throw new ValidationException("Managing Organisation does not match programme");
        }
        if (!managingOrganisation.isManagingOrganisation()) {
            throw new ValidationException("Organisation is not a managing organisation");
        }
        projectRepository.updateProjectManagingOrgByProgramme(managingOrganisation.getId(), programme.getId());
        projectLedgerRepository.updatePaymentEntriesManagingOrgByProgramme(managingOrganisation.getId(), programme.getId());
    }


    public void transfer(Integer projectId, Integer organisationId) {
        Project project = getEnrichedProject(projectId);
        if (!project.getAllowedActions().contains(Project.Action.Transfer)) {
            throw new ValidationException("cannot transfer this project!");
        }

        Organisation fromOrganisation = project.getOrganisation();

        Organisation toOrganisation = organisationService.findOne(organisationId);
        if (toOrganisation == null) {
            throw new NotFoundException();
        }

        String historyDescription = String.format("Transferred from %s to %s", fromOrganisation.getName(), toOrganisation.getName());
        ProjectHistory historyEntry = new ProjectHistory(Transfer, historyDescription, "Project transferred");

        project.setOrganisation(toOrganisation);
        project.setOrganisationGroupId(null); // for consortium we are for now changing the bidding arrangement to individual
        project.getHistory().add(historyEntry);
        updateProject(project);

        String notificationText = String.format("Project %d has been transferred from %s to %s. %s no longer has access to this project. If you have any issues, email the OPS team at ops@london.gov.uk.",
                projectId, fromOrganisation.getName(), toOrganisation.getName(), fromOrganisation.getName());

        List<String> fromOrgUsersToBeNotified = new ArrayList<>();
        fromOrgUsersToBeNotified.addAll(fromOrganisation.getUsernames(Role.GLA_ORG_ADMIN, Role.ORG_ADMIN));
        fromOrgUsersToBeNotified.addAll(notificationService.getSubscribers(EntityType.project, projectId));
        notificationService.createNotification(NotificationType.Info, notificationText, fromOrgUsersToBeNotified);

        List<String> toOrgAdmins = toOrganisation.getUsernames(Role.GLA_ORG_ADMIN, Role.ORG_ADMIN);
        notificationService.createNotification(NotificationType.Info, notificationText, project, toOrgAdmins);
    }

    public List<SAPMetaData> getPaymentMetaData(Integer projectId,Integer blockId, Integer categoryId, Integer yearMonth) {
        return projectLedgerRepository.getSapMetaData(projectId, blockId, yearMonth, LedgerType.PAYMENT, LedgerStatus.ACTUAL, categoryId);
    }

    public void updateMilestoneEvidentialStatus(Template template, Integer blockDisplayOrder, Integer newMaximum, MilestonesTemplateBlock.EvidenceApplicability evidenceApplicability) {
        List<Project> allByTemplate = projectRepository.findAllByTemplate(template);
        for (Project project : allByTemplate) {
            List<NamedProjectBlock> blocksByTypeAndDisplayOrder = project.getBlocksByTypeAndDisplayOrder(ProjectBlockType.Milestones, blockDisplayOrder);
            for (NamedProjectBlock namedProjectBlock : blocksByTypeAndDisplayOrder) {
                ProjectMilestonesBlock milestonesBlock = (ProjectMilestonesBlock) namedProjectBlock;
                milestonesBlock.setMaxEvidenceAttachments(newMaximum);
                milestonesBlock.setEvidenceApplicability(evidenceApplicability);
            }
        }
    }

    public void moveAnswerValueToGrantSource(Integer questionId) {
        List<Project> projects = projectRepository.findAllForQuestion(questionId);

        for (Project project: projects) {
            Answer answer = null;

            for (ProjectQuestionsBlock questionsBlock: project.getQuestionsBlocks()) {
                for (Answer a: questionsBlock.getAnswers()) {
                    if (a.getQuestionId().equals(questionId)) {
                        answer = a;
                    }
                }
            }

            if (answer == null) {
                log.error("could not find answer for question {} in project P{}", questionId, project.getId());
            }
            else if (answer.getNumericAnswer() != null) {
                project.getGrantSourceBlock().setZeroGrantRequested(false);
                project.getGrantSourceBlock().setGrantValue(answer.getNumericAnswer().longValue());
                project.getGrantSourceBlock().setLastModified(environment.now());
            }
        }

        projectRepository.save(projects);
    }

    public void updateMilestoneDescriptionEnabled(Template template, Integer blockDisplayOrder, boolean enabled) {
        List<Project> projects = projectRepository.findAllByTemplate(template);
        for (Project project: projects) {
            for (NamedProjectBlock block: project.getBlocksByTypeAndDisplayOrder(ProjectBlockType.Milestones, blockDisplayOrder)) {
                ((ProjectMilestonesBlock) block).setDescriptionEnabled(enabled);
            }
        }
        projectRepository.save(projects);
    }

    public void removeQuestion(Template template, Integer blockDisplayOrder, Integer questionId) {
        List<Project> projects = projectRepository.findAllByTemplate(template);
        for (Project project: projects) {
            ProjectQuestionsBlock questionsBlock = (ProjectQuestionsBlock) project.getLatestBlockOfType(ProjectBlockType.Questions, blockDisplayOrder);
            questionsBlock.getAnswers().removeIf(a -> a.getQuestion().getId().equals(questionId));
            questionsBlock.getQuestionEntities().removeIf(q -> q.getQuestion().getId().equals(questionId));
        }
        projectRepository.save(projects);
    }

    public void updateMilestoneNaSelectable(Template template, Integer blockDisplayOrder, Integer processingRouteId, Integer milestoneId, Boolean naSelectable) {
        List<Project> projects = projectRepository.findAllByTemplate(template);
        for (Project project: projects) {
            for (NamedProjectBlock projectBlock: project.getBlocksByTypeAndDisplayOrder(ProjectBlockType.Milestones, blockDisplayOrder)) {
                ProjectMilestonesBlock milestonesBlock = (ProjectMilestonesBlock) projectBlock;
                if (milestonesBlock.getProcessingRouteId() == null // projects with default processing route wont store its ID
                        || milestonesBlock.getProcessingRouteId().equals(processingRouteId)) {
                    milestonesBlock.getMilestoneByExternalId(milestoneId).setNaSelectable(naSelectable);
                }
            }
        }
        projectRepository.save(projects);
    }

}
