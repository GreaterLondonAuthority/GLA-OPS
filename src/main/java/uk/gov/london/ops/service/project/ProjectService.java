/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.service.project;

import static uk.gov.london.common.GlaUtils.parseInt;
import static uk.gov.london.common.user.BaseRole.OPS_ADMIN;
import static uk.gov.london.ops.domain.project.NamedProjectBlock.Action.DELETE;
import static uk.gov.london.ops.domain.project.NamedProjectBlock.BlockStatus.LAST_APPROVED;
import static uk.gov.london.ops.domain.project.NamedProjectBlock.BlockStatus.UNAPPROVED;
import static uk.gov.london.ops.domain.project.ProjectHistory.HistoryEventType.Label;
import static uk.gov.london.ops.domain.project.ProjectHistory.HistoryEventType.Transfer;
import static uk.gov.london.ops.domain.project.ProjectHistory.Transition.Created;
import static uk.gov.london.ops.domain.project.skills.LearningGrantEntryType.SUPPORT;
import static uk.gov.london.ops.domain.project.state.ProjectStatus.Active;
import static uk.gov.london.ops.domain.project.state.ProjectStatus.Assess;
import static uk.gov.london.ops.domain.project.state.ProjectStatus.Closed;
import static uk.gov.london.ops.domain.project.state.ProjectStatus.Returned;
import static uk.gov.london.ops.domain.project.state.ProjectStatus.Submitted;
import static uk.gov.london.ops.domain.project.state.ProjectSubStatus.AbandonPending;
import static uk.gov.london.ops.domain.project.state.ProjectSubStatus.Abandoned;
import static uk.gov.london.ops.domain.project.state.ProjectSubStatus.Completed;
import static uk.gov.london.ops.domain.project.state.ProjectSubStatus.PaymentAuthorisationPending;
import static uk.gov.london.ops.domain.project.state.ProjectSubStatus.Rejected;
import static uk.gov.london.ops.domain.project.state.ProjectSubStatus.UnapprovedChanges;
import static uk.gov.london.ops.notification.NotificationType.ProjectTransfer;
import static uk.gov.london.ops.payment.implementation.ProjectLedgerEntryMapper.BESPOKE_PREFIX;
import static uk.gov.london.ops.payment.implementation.ProjectLedgerEntryMapper.RECLAIMED_PREFIX;
import static uk.gov.london.ops.service.PermissionType.PROJ_VIEW_RECOMMENDATION;
import static uk.gov.london.ops.service.project.state.StateTransitionResult.Status.ILLEGAL_TRANSITION;
import static uk.gov.london.ops.service.project.state.StateTransitionResult.Status.INVALID;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
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
import uk.gov.london.common.skills.SkillsGrantType;
import uk.gov.london.ops.EventType;
import uk.gov.london.ops.framework.feature.Feature;
import uk.gov.london.ops.framework.feature.FeatureStatus;
import uk.gov.london.ops.audit.ActivityType;
import uk.gov.london.ops.domain.EntityType;
import uk.gov.london.ops.domain.PreSetLabel;
import uk.gov.london.ops.domain.Requirement;
import uk.gov.london.ops.domain.organisation.Organisation;
import uk.gov.london.ops.domain.organisation.OrganisationGroup;
import uk.gov.london.ops.domain.project.Claim;
import uk.gov.london.ops.domain.project.ClaimStatus;
import uk.gov.london.ops.domain.project.GrantSourceBlock;
import uk.gov.london.ops.domain.project.InternalProjectBlock;
import uk.gov.london.ops.domain.project.Label;
import uk.gov.london.ops.domain.project.LabelType;
import uk.gov.london.ops.domain.project.LockDetails;
import uk.gov.london.ops.domain.project.Milestone;
import uk.gov.london.ops.domain.project.NamedProjectBlock;
import uk.gov.london.ops.domain.project.Project;
import uk.gov.london.ops.domain.project.ProjectAction;
import uk.gov.london.ops.domain.project.ProjectBlockType;
import uk.gov.london.ops.domain.project.ProjectBudgetsBlock;
import uk.gov.london.ops.domain.project.ProjectDetailsBlock;
import uk.gov.london.ops.domain.project.ProjectHistory;
import uk.gov.london.ops.domain.project.ProjectMilestonesBlock;
import uk.gov.london.ops.domain.project.ProjectRiskAndIssue;
import uk.gov.london.ops.domain.project.ProjectRisksBlock;
import uk.gov.london.ops.domain.project.ProjectSummary;
import uk.gov.london.ops.domain.project.RiskLevelID;
import uk.gov.london.ops.domain.project.RiskLevelLookup;
import uk.gov.london.ops.domain.project.SAPMetaData;
import uk.gov.london.ops.domain.project.SpendType;
import uk.gov.london.ops.domain.project.funding.FundingBlock;
import uk.gov.london.ops.domain.project.question.Answer;
import uk.gov.london.ops.domain.project.question.ProjectQuestion;
import uk.gov.london.ops.domain.project.question.ProjectQuestionsBlock;
import uk.gov.london.ops.domain.project.skills.LearningGrantAllocation;
import uk.gov.london.ops.domain.project.skills.LearningGrantBlock;
import uk.gov.london.ops.domain.project.skills.LearningGrantEntry;
import uk.gov.london.ops.domain.project.state.ProjectStatus;
import uk.gov.london.ops.domain.project.state.ProjectSubStatus;
import uk.gov.london.ops.domain.skills.SkillsPaymentProfile;
import uk.gov.london.ops.domain.template.InternalTemplateBlock;
import uk.gov.london.ops.domain.template.MilestonesTemplateBlock;
import uk.gov.london.ops.domain.template.ProcessingRoute;
import uk.gov.london.ops.domain.template.Programme;
import uk.gov.london.ops.domain.template.ProgrammeTemplate;
import uk.gov.london.ops.domain.template.Template;
import uk.gov.london.ops.domain.template.TemplateBlock;
import uk.gov.london.ops.domain.user.User;
import uk.gov.london.ops.notification.EntitySubscription;
import uk.gov.london.ops.notification.NotificationType;
import uk.gov.london.ops.payment.FinanceService;
import uk.gov.london.ops.payment.LedgerStatus;
import uk.gov.london.ops.payment.LedgerType;
import uk.gov.london.ops.payment.PaymentGroup;
import uk.gov.london.ops.payment.ProjectLedgerEntry;
import uk.gov.london.ops.project.implementation.AnnualSpendSummaryMapper;
import uk.gov.london.ops.project.implementation.IMSProjectImportMapper;
import uk.gov.london.ops.refdata.Borough;
import uk.gov.london.ops.refdata.CategoryValue;
import uk.gov.london.ops.refdata.RefDataService;
import uk.gov.london.ops.refdata.Ward;
import uk.gov.london.ops.repository.LockDetailsRepository;
import uk.gov.london.ops.repository.OrganisationGroupRepository;
import uk.gov.london.ops.repository.OrganisationRepository;
import uk.gov.london.ops.repository.OutputCategoryAssumptionRepository;
import uk.gov.london.ops.repository.PreSetLabelRepository;
import uk.gov.london.ops.repository.ProjectHistoryRepository;
import uk.gov.london.ops.repository.ProjectSummaryRepository;
import uk.gov.london.ops.repository.RiskLevelLookupRepository;
import uk.gov.london.ops.service.MessageService;
import uk.gov.london.ops.service.OrganisationGroupService;
import uk.gov.london.ops.service.OrganisationService;
import uk.gov.london.ops.service.PermissionType;
import uk.gov.london.ops.service.PreSetLabelService;
import uk.gov.london.ops.service.ProgrammeService;
import uk.gov.london.ops.service.SkillsService;
import uk.gov.london.ops.service.TemplateService;
import uk.gov.london.ops.service.project.state.ProjectState;
import uk.gov.london.ops.service.project.state.ProjectStateMachine;
import uk.gov.london.ops.service.project.state.StateModel;
import uk.gov.london.ops.service.project.state.StateTransition;
import uk.gov.london.ops.service.project.state.StateTransitionResult;
import uk.gov.london.ops.service.project.state.StateTransitionType;
import uk.gov.london.ops.framework.exception.ForbiddenAccessException;
import uk.gov.london.ops.framework.exception.NotFoundException;
import uk.gov.london.ops.framework.exception.ValidationException;
import uk.gov.london.ops.web.model.AnnualSpendSummary;
import uk.gov.london.ops.web.model.LockRequestStatus;
import uk.gov.london.ops.web.model.ProjectLedgerItemRequest;
import uk.gov.london.ops.web.model.ProjectsTransferResult;
import uk.gov.london.ops.web.model.project.BulkProjectUpdateOperation;
import uk.gov.london.ops.web.model.project.BulkUpdateResult;
import uk.gov.london.ops.web.model.project.ProjectBlockHistoryItem;

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

    Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    Set<PostCloneNotificationListener> cloneListeners;

    @Autowired
    Set<ProjectPaymentGenerator> projectPaymentGenerators;

    @Autowired
    FinanceService financeService;

    @Autowired
    OrganisationService organisationService;

    @Autowired
    ProgrammeService programmeService;

    @Autowired
    RefDataService refDataService;

    @Autowired
    TemplateService templateService;

    @Autowired
    FeatureStatus featureStatus;

    @Autowired
    OrganisationGroupService organisationGroupService;

    @Autowired
    AnnualSpendSummaryMapper annualSpendSummaryMapper;

    @Autowired
    LabelService labelService;

    @Autowired
    LockDetailsRepository lockDetailsRepository;

    @Autowired
    OrganisationRepository organisationRepository;

    @Autowired
    OrganisationGroupRepository organisationGroupRepository;

    @Autowired
    ProjectHistoryRepository projectHistoryRepository;

    @Autowired
    OutputCategoryAssumptionRepository outputCategoryAssumptionRepository;

    @Autowired
    ProjectSummaryRepository projectSummaryRepository;

    @Autowired
    RiskLevelLookupRepository riskLevelLookupRepository;

    @Autowired
    IMSProjectImportMapper imsProjectImportMapper;

    @Autowired
    MessageService messageService;

    @Autowired
    PreSetLabelService preSetLabelService;

    @Autowired
    PreSetLabelRepository preSetLabelRepository;

    @Autowired
    private SkillsService skillsService;


    public Page<ProjectSummary> findAll(String project,
                                        String organisation,
                                        String programme,
                                        List<Integer> programmes,
                                        List<Integer> templates,
                                        List<String> states,
                                        boolean watchingProject,
                                        Pageable pageable) {
        User currentUser = userService.loadCurrentUser();

        Integer organisationId = parseInt(organisation);
        Integer programmeId = parseInt(programme);

        if (!currentUser.isApproved() ||
                (organisationId != null && !permissionService.currentUserHasPermissionForOrganisation(PermissionType.PROJ_READ.getPermissionKey(), organisationId))) {
            throw new ForbiddenAccessException();
        }

        Page<ProjectSummary> result = projectSummaryRepository.findAll(currentUser,
                getProjectId(project),
                project,
                organisationId,
                organisation,
                programmeId,
                programme,
                programmes,
                templates,
                states,
                watchingProject,
                pageable);

        cleanProjectSummaries(result.getContent());

        return result;
    }

    private Integer getProjectId(String project) {
        if (project != null && (project.startsWith("P") || project.startsWith("p"))) {
            project = project.substring(1);
        }
        return parseInt(project);
    }

    // removes recommendation for roles without permission to view it, annotation approach is too slow for large data volumes
    private List<ProjectSummary> cleanProjectSummaries(List<ProjectSummary> summaries) {
        User user = userService.currentUser();
        Set<String> permissionsForUser = permissionService.getPermissionsForUser(user);
        boolean showProperty = false;
        for (String userPermission : permissionsForUser) {
            if (PROJ_VIEW_RECOMMENDATION.getPermissionKey().equals(userPermission)) {
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
            project.setOrganisationGroup(organisationGroupRepository.findById(project.getOrganisationGroupId()).orElse(null));
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

    public GrantSourceBlock getProjectGrantSource(Integer projectId) {
        Project project = get(projectId);

        enrichGrantSourceBlock(project);

        return project.getGrantSourceBlock();
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
            boolean resetGrant = ObjectUtils.compare(block.getGrantValue(), newBlock.getGrantValue()) != 0;
            boolean resetDPF = ObjectUtils.compare(block.getDisposalProceedsFundValue(), newBlock.getDisposalProceedsFundValue()) != 0;
            boolean resetRCGF = ObjectUtils.compare(block.getRecycledCapitalGrantFundValue(), newBlock.getRecycledCapitalGrantFundValue()) != 0;
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
            Borough borough = refDataService.findBoroughByName(block.getBorough());
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

            initialiseProjectFromTemplate(project, template);

        }
        prepopulateProjectData(project);

        projectRepository.save(project);


        createProjectHistoryEntry(project, Created, null, null);

        notificationService.subscribe(userService.currentUsername(), EntityType.project, project.getId());
        return project;
    }

    public void initialiseProjectFromTemplate(Project project, Template template) {
        // need to re-inflate the template
        String initialStatus = template.getStateModel().getInitialStatus();
        String initialSubStatus = template.getStateModel().getInitialSubStatus();

        updateProjectFromTemplate(project, template);
        project.setStateModel(template.getStateModel());
        project.setStatusName(initialStatus);
        project.setSubStatusName(initialSubStatus);
        List<TemplateBlock> blocksEnabled = template.getBlocksEnabled();
        for (TemplateBlock templateBlock : blocksEnabled) {
            NamedProjectBlock namedProjectBlock = templateBlock.getBlock().newProjectBlockInstance();

            namedProjectBlock.setProject(project);

            namedProjectBlock.initFromTemplate(templateBlock);
            namedProjectBlock.setHidden(StringUtils.isNotEmpty(templateBlock.getBlockAppearsOnStatus())
                    && !(initialStatus.equals(templateBlock.getBlockAppearsOnStatus())));
            project.addBlockToProject(namedProjectBlock);

        }

            for (InternalTemplateBlock internalTemplateBlock : template.getInternalBlocks()) {
                addInternalBlockToProject(project, internalTemplateBlock);
            }

        if (template.isBlockPresent(ProjectBlockType.Details)) {
            ((ProjectDetailsBlock) project.getSingleBlockByType(ProjectBlockType.Details)).setTitle(project.getTitle());
        }
    }

    private void updateProjectFromTemplate(Project project, Template template) {
        project.setTemplate(template);
        project.setStrategicProject(template.isStrategicTemplate());
        project.setAssociatedProjectsEnabled(template.isAssociatedProjectsEnabled());
        project.setInfoMessage(template.getInfoMessage());
    }

    public boolean canProjectBeAssignedToTemplate(Integer templateId, Integer organisationId) {
        Template template = templateService.find(templateId);
        Organisation organisation = organisationService.findOne(organisationId);

        if (template.getNumberOfProjectAllowedPerOrg() == null) {
            return true;
        } else {
            Integer countProjectsWithTemplate = projectRepository.countByTemplateAndOrganisationAndStatusNameIsNot(template, organisation, "Closed");
            return !Objects.equals(template.getNumberOfProjectAllowedPerOrg(), countProjectsWithTemplate);
        }

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

        ProgrammeTemplate programmeTemplate = programme.getProgrammeTemplateByTemplateID(project.getTemplateId());
        if (programmeTemplate == null) {
            throw new ValidationException(String.format("Programme %s does not contain template with ID : %d", programme.getName(), project.getTemplateId()));
        }
        if (programmeTemplate.getStatus().equals(ProgrammeTemplate.Status.Inactive)) {
            throw new ValidationException("cannot create project with inactive template");
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
            Organisation org = organisationRepository.findById(id).orElse(null);
            if (org == null) {
                throw new ValidationException("Organisation specified is not recognised.");
            } else if (org.isTechSupportOrganisation()) {
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
        financeService.deleteAllTestDataByProjectId(projectId);
        projectRepository.deleteById(projectId);
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
        projectHistory.setStatusName(project.getStatusName());
        projectHistory.setSubStatusName(project.getSubStatusName());
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
        dataAccessControlService.checkProjectAccess(projectId);

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
        financeService.updateAnnualSpendAndBudgetLedgerEntries(project, year, revenue, capital);
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
                if (!ProjectStatus.Assess.equals(project.getStatusType())) {
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

    public void testOnlyMoveProjectToStatus(Project project, ProjectStatus newStatus) {
        if (featureStatus.isEnabled(Feature.TestOnlyStatusTransitions)) {
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
        Template template = project.getTemplate();
        if (!canProjectBeAssignedToTemplate(template.getId(), project.getOrganisation().getId())){
            String projectString = template.getNumberOfProjectAllowedPerOrg() > 1 ? "projects" : "project";
            String isAreString = template.getNumberOfProjectAllowedPerOrg() > 1 ? "are" : "is";
            throw new ValidationException(String.format("Only %d %s %s allowed for this project type.", template.getNumberOfProjectAllowedPerOrg(), projectString, isAreString));
        }

        ProjectDetailsBlock detailsBlock = project.getDetailsBlock();
        OffsetDateTime approvalTime = detailsBlock.getApprovalTime();
        ProjectState state = new ProjectState(Active);
        if (approvalTime == null && detailsBlock.getVersionNumber() == 1) {
            state = new ProjectState(ProjectStatus.Draft);
        } else if (project.getStateModel().isApprovalRequired()) {
            for (NamedProjectBlock block : project.getProjectBlocks()) {
                if (UNAPPROVED.equals(block.getBlockStatus())) {
                    state.setSubStatus(UnapprovedChanges);
                    break;
                }
            }
        }

        createProjectHistoryEntry(project, ProjectHistory.Transition.Reinstated, "Project Reinstated", comments);

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

        Set<String> userRoles = getUserRolesForProject(project);

        StateTransition stateTransition = stateMachine.getTransition(
                project.getProjectState(),
                targetState,
                userRoles,
                project.getProgramme().isEnabled(),
                project.getProgramme().isInAssessment(),
                project.getHistory(),
                !StringUtils.isEmpty(comments),
                project.isComplete(),
                project.getApprovalWillCreatePendingPayment()
                        || project.getApprovalWillCreatePendingReclaim());

        if (stateTransition == null) {
            return new StateTransitionResult(INVALID, currentState, targetState, project.getId());
        }

        preStateTransitionActions(project, stateTransition.getTo(), stateTransition.getTransitionType());

        project.setProjectState(targetState);
        performProjectUpdate(project);

        postStateTransitionActions(project, stateTransition, comments);

        return new StateTransitionResult(StateTransitionResult.Status.SUCCESS);
    }

    /**
     * This method does not actually make the state transition but is used by the API to verify the transition can be made.
     */
    public void validateTransitionProjectToStatus(Project project, ProjectState targetState) {
        preStateTransitionActions(project, targetState, null);
    }

    private boolean autoApprovalProjectAndFeatureDisabled(Project project) {
        return StateModel.AutoApproval.equals(project.getStateModel()) && (!featureStatus.isEnabled(Feature.SubmitAutoApprovalProject));
    }

    void preStateTransitionActions(Project project, ProjectState targetState, StateTransitionType transitionType) {
        if (Returned.equals(targetState.getStatusType())) {
            project.setRecommendation(null);
        }

        if (StateTransitionType.APPROVAL.equals(transitionType)) {
            approveAllProjectBlocks(project);
            initialiseWithSkillsData(project);
        }

        if (targetState.equals(Active, PaymentAuthorisationPending)) {
            validateProjectForRequestingPaymentAuthorisation(project);

            if (project.getTemplate().getMilestoneType().equals(Template.MilestoneType.MonetarySplit) && project.getGrantSourceAdjustmentAmount().signum() < 0) {
                // grant reclaim needed
                for (Milestone milestone : project.getMilestonesBlock().getMilestones()) {
                    Long newGrantClaimed = project.getMilestonesBlock().getMilestoneGrantClaimed(milestone.getId());
                    if (newGrantClaimed != null && milestone.getClaimedGrant() != null) {
                        Long original = milestone.getClaimedGrant();
                        milestone.setReclaimedGrant(original - newGrantClaimed);
                    }

                }
            }
        }

        if (targetState.equals(Closed, Completed)) {
            if (!(project.getStatusType().equals(Active) && project.getSubStatusType() == null)) {
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
            if (project.getStateModel().isAllowClosureWithUnapprovedBlocks() && incomplete) {
                throw new ValidationException("Cannot complete a project with incomplete blocks");

            }
            if (!project.getStateModel().isAllowClosureWithUnapprovedBlocks() && (incomplete || unapproved)) {
                throw new ValidationException("Cannot complete a project with unapproved or incomplete blocks");
            }

            ProjectMilestonesBlock milestones = (ProjectMilestonesBlock) project.getSingleLatestBlockOfType(ProjectBlockType.Milestones);
            if (project.getTemplate().isBlockPresent(ProjectBlockType.Milestones)) {
                for (Milestone milestone : milestones.getApplicableMilestones()) {
                    if (Requirement.mandatory.equals(milestone.getRequirement())) {
                        if (project.getStateModel().isAllowClosureWithUnapprovedMandatoryMilestones() && project.getTemplate().getAllowMonetaryMilestones() && !milestone.isManuallyCreated() && !milestone.isClaimed()) {
                            throw new ValidationException("All mandatory milestones that apply to this project must be claimed before the project can be closed as complete");
                        } else if (!project.getStateModel().isAllowClosureWithUnapprovedMandatoryMilestones() && !milestone.isManuallyCreated() && !milestone.isApproved()) {
                            throw new ValidationException("All mandatory milestones must be claimed and approved before the project can be closed as complete");
                        }
                    }
                }
            }

        }
        boolean isAbandoning = targetState.equals(Closed, Abandoned);
        boolean isRejecting = targetState.equals(Closed, Rejected);
        if (isAbandoning || isRejecting || targetState.equals(Active, AbandonPending)) {
            if (project.isStrategicProject() && projectRepository.countAssociatedProjects(project.getProgrammeId()) > 0) {
                if (isRejecting) {
                    throw new ValidationException("This project has associated projects and cannot be rejected.");
                } else {

                    if (!userService.currentUser().hasRoleInOrganisation(OPS_ADMIN, project.getManagingOrganisationId())) {
                        throw new ValidationException("This project has associated projects and cannot be abandoned.");
                    }
                }
            }

            // removing validation as part of GLA-25277
//            if (project.isPendingPayments() || !userService.currentUser().isOpsAdmin() && paymentService.hasPayments(project.getId())) {
            if (project.isPendingPayments()) {
                if (isRejecting) {
                    throw new ValidationException("Project cannot be rejected at this stage.");
                } else {
                    throw new ValidationException("Project cannot be abandoned at this stage.");
                }
            }
        }
    }

    public void initialiseWithSkillsData(Project project) {
        LearningGrantBlock learningGrantBlock = (LearningGrantBlock) project.getSingleLatestBlockOfType(ProjectBlockType.LearningGrant);
        if (learningGrantBlock != null) {
            Map<Integer, SkillsPaymentProfile> profiles = skillsService.getSkillsPaymentProfiles(
                    learningGrantBlock.getGrantType(), learningGrantBlock.getStartYear()).stream()
                    .collect(Collectors.toMap(SkillsPaymentProfile::getPeriod, Function.identity()));

            Map<Integer, SkillsPaymentProfile> supportProfiles = skillsService.getSkillsPaymentProfiles(
                    SkillsGrantType.AEB_LEARNER_SUPPORT, learningGrantBlock.getStartYear()).stream()
                    .collect(Collectors.toMap(SkillsPaymentProfile::getPeriod, Function.identity()));

            List<LearningGrantEntry> sorted = learningGrantBlock.getLearningGrantEntries().stream().sorted(Comparator.comparingInt(LearningGrantEntry::getPeriod)).collect(Collectors.toList());
            BigDecimal totalSoFar = BigDecimal.ZERO;
            LearningGrantAllocation startYearAllocation = learningGrantBlock.getAllocation(learningGrantBlock.getStartYear());
            BigDecimal allocation = startYearAllocation == null || startYearAllocation.getAllocation() == null ? BigDecimal.ZERO : startYearAllocation.getAllocation();
            for (LearningGrantEntry learningGrantEntry : sorted) {
                SkillsPaymentProfile profile = learningGrantEntry.getType() == SUPPORT ? supportProfiles.get(learningGrantEntry.getPeriod()) : profiles.get(learningGrantEntry.getPeriod());
                if (profile != null) {
                    learningGrantEntry.setPercentage(profile.getPercentage());
                    learningGrantEntry.setPaymentDate(profile.getPaymentDate());
                    if (learningGrantEntry.getPeriod() != 12) {
                        if (profile.getPercentage() != null && allocation != null) {
                            BigDecimal lgeAllocation = profile.getPercentage().divide(new BigDecimal(100)).multiply(allocation).setScale(2, BigDecimal.ROUND_HALF_UP);
                            learningGrantEntry.setAllocation(lgeAllocation);
                            totalSoFar = totalSoFar.add(lgeAllocation);
                        }
                    } else {
                        learningGrantEntry.setAllocation(allocation.subtract(totalSoFar));
                    }
                }
            }
        }
    }

    void validateProjectForRequestingPaymentAuthorisation(Project project) {
        if (project.getApprovalWillCreatePendingGrantPayment()) {
            //If the project will create pending  and hasn't got SAP vendor id
            if (StringUtils.isEmpty(project.getOrganisation().getsapVendorId())) {
                throw new ValidationException("SAP vendor ID has not been provided. The SAP vendor ID must be added to the organisation details by a OPS Admin.");
            }

            validateWBSCodesForRequestingPaymentAuthorisation(project);

            String ceCodeForTemplate = project.getProgramme().getCeCodeForTemplate(project.getTemplateId());
            if (StringUtils.isEmpty(ceCodeForTemplate)) {
                throw new ValidationException("A cost element code must be added to the project template associated with this programme by an OPS admin.");
            }
        }

        if (project.isPendingContractSignature()) {
            throw new ValidationException("Pending payments cannot be submitted for authorisation as the contract for this project type has not been signed.");
        }
    }

    void validateWBSCodesForRequestingPaymentAuthorisation(Project project) {
        Set<SpendType> spendTypes = getSpendTypesForProject(project);
        if (spendTypes.isEmpty()) {
            boolean defaultWbsCodeSetForTemplate = project.getProgramme().defaultWbsCodeSetForTemplate(project.getTemplateId());
            if (!defaultWbsCodeSetForTemplate) {
                throw new ValidationException("Default WBS code has not been specified. A default WBS code must be specified on the project template associated with this programme by an OPS admin.");
            }

            String wbsCodeForTemplate = project.getProgramme().getWbsCodeForTemplate(project.getTemplateId());
            if (StringUtils.isEmpty(wbsCodeForTemplate)) {
                throw new ValidationException("WBS code has not been provided. A WBS code must be added to the project template associated with this programme by an OPS admin.");
            }
        } else {
            for (SpendType spendType : spendTypes) {
                String wbsCode = project.getProgramme().getWbsCodeForTemplate(project.getTemplateId(), spendType);
                if (StringUtils.isEmpty(wbsCode)) {
                    throw new ValidationException(spendType + " WBS code has not been provided. A " + spendType + " WBS code must be added to the project template associated with this programme by an OPS admin.");
                }
            }
        }
    }

    Set<SpendType> getSpendTypesForProject(Project project) {
        Set<SpendType> spendTypes = new HashSet<>();
        FundingBlock fundingBlock = (FundingBlock) project.getSingleLatestBlockOfType(ProjectBlockType.Funding);
        if (fundingBlock != null) {
            Set<Claim> claims = fundingBlock.getClaims().stream().filter(c -> ClaimStatus.Claimed.equals(c.getClaimStatus())).collect(Collectors.toSet());

            if (!claims.isEmpty()) {
                for (Claim claim : claims) {
                    if (spendTypes.size() != 2) {
                        Set<ProjectLedgerEntry> allForClaim = paymentService.findAllForClaim(fundingBlock.getId(), claim.getId());
                        spendTypes.addAll(allForClaim.stream().map(ProjectLedgerEntry::getSpendType).collect(Collectors.toSet()));
                    }
                }
            }
        }
        return spendTypes;
    }

    void postStateTransitionActions(Project project, StateTransition stateTransition, String comments) {
        ProjectStateMachine stateMachine = stateMachineForProject(project);

        ProjectState currentState = stateTransition.getFrom();
        ProjectState targetState = stateTransition.getTo();

        ProjectHistory.Transition historyTransition = stateTransition.getProjectHistoryTransition() != null ? stateTransition.getProjectHistoryTransition() : stateMachine.getProjectHistoryTransition(currentState, targetState);
        if (historyTransition != null) {
            if (targetState.getStatusType().equals(Submitted)) {
                project.getHistory().removeIf(e -> ProjectHistory.Transition.Unconfirmed.equals(e.getTransition()));
            }
            String historyDescription = StringUtils.isNotEmpty(stateTransition.getProjectHistoryDescription()) ? stateTransition.getProjectHistoryDescription() : stateMachine.getProjectHistoryDescription(project, historyTransition);
            createProjectHistoryEntry(project, historyTransition, historyDescription, comments);
        }

        if (Assess.equals(targetState.getStatusType())) {
            auditService.auditCurrentUserActivity(String.format("Project with ID %d was moved to status of assessed.", project.getId()));
        }

        PaymentGroup paymentGroup = null; // for now we can only generate 1 payment group per state transition
        if (targetState.equals(Active, PaymentAuthorisationPending)) {
            String approvalRequestedBy = this.getFullNameOfLastUserToRequestApproval(project);
            for (ProjectPaymentGenerator projectPaymentGenerator : projectPaymentGenerators) {
                paymentGroup = projectPaymentGenerator.generatePaymentsForProject(project, approvalRequestedBy);
            }
        }

        if (StringUtils.isNotEmpty(stateTransition.getNotifcationKey())) {
            createStateTransitionNotification(NotificationType.valueOf(stateTransition.getNotifcationKey()), project, currentState, targetState, paymentGroup);
        }

        if (targetState.equals(Closed, Rejected)) {
            auditService.auditCurrentUserActivity(String.format("Project with ID %d was moved to status of %s", project.getId(), targetState.toString()));
        }

        for (NamedProjectBlock block : project.getProjectBlocks()) {
            block.handleStateTransition(stateTransition);
        }
    }

    void createStateTransitionNotification(NotificationType notificationType, Project project, ProjectState currentState, ProjectState targetState, PaymentGroup paymentGroup) {
        Map<String, Object> model = new HashMap<String, Object>() {{
            put("projectId", project.getId());
            put("organisation", project.getOrganisation());
            put("fromStatus", currentState.getStatus());
            put("toStatus", targetState.getStatus());
        }};

        if(paymentGroup != null) {
            notificationService.createNotification(notificationType, paymentGroup, model);
        } else {
            notificationService.createNotification(notificationType, project, model);
        }
    }

    void approveAllProjectBlocks(Project project) {
        String username = userService.currentUser().getUsername();
        OffsetDateTime now = environment.now();

        if (project.getStatusType().equals(ProjectStatus.Assess) || (project.getStatusType().equals(ProjectStatus.Draft) && !project.getStateModel().isApprovalRequired())) {
            project.setFirstApproved(now);
        }

        //approve milestone block first due to dependancy on Grant Source
        ProjectMilestonesBlock milestonesBlock = project.getMilestonesBlock();
        if (milestonesBlock != null) {
            if (milestonesBlock.isApproved()) {
                milestonesBlock.updateClaimAmounts();
                List<ProjectLedgerEntry> payments = financeService.findAllByBlockIdAndLedgerType(milestonesBlock.getId(), LedgerType.PAYMENT);
                boolean monetaryValue = project.getTemplate().getMilestoneType().equals(Template.MilestoneType.MonetaryValue);

                if (payments.size() > 0) {

                    for (Milestone milestone : milestonesBlock.getMilestones()) {
                        if (monetaryValue || (milestone.getMonetarySplit() != null && milestone.getMonetarySplit() > 0)) {
                            milestone.setClaimedGrant(0L);
                        }
                    }
                }

                for (ProjectLedgerEntry payment : payments) {
                    if (LedgerStatus.getApprovedPaymentStatuses().contains(payment.getLedgerStatus())) {
                        Milestone milestoneBySummary;
                        String subCategory = monetaryValue ?
                                payment.getSubCategory().substring(BESPOKE_PREFIX.length()) : payment.getSubCategory();


                        if (payment.isReclaim()) {
                            milestoneBySummary = milestonesBlock.getMilestoneBySummary(subCategory.substring(RECLAIMED_PREFIX.length()));
                        } else {
                            milestoneBySummary = milestonesBlock.getMilestoneBySummary(subCategory);
                        }
                        if (milestoneBySummary.getMonetarySplit() != null && milestoneBySummary.getMonetarySplit() > 0) {
                            if (payment.isReclaim()) {
                                milestoneBySummary.setReclaimed(true);
                            }
                            Long claimedGrant = milestoneBySummary.getClaimedGrant();
                            milestoneBySummary.setClaimedGrant(claimedGrant + payment.getValue().negate().longValue());
                        }
                    }
                }
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
        if (block.isApproved() || block.isHidden()) {
            return;
        }

        if (!block.isNew() && !block.isComplete()) {
            // can't approve blocks if a non-new block is incomplete
            throw new ValidationException(String.format("Unable to approve block '%s' as it is incomplete", block.getBlockDisplayName()));
        }

        if (block.isComplete()) {
            project.approveBlock(block, username, now);
        }

        //Hack to force the project to be Active(no subStatus) after approving
        if (block.isNew() && !block.isComplete()) {
            block.setLastModified(null);
        }
    }

    public NamedProjectBlock getBlockAndLock(Project project, NamedProjectBlock block) {
        return getBlockAndLock(project, block, true);
    }

    public NamedProjectBlock getBlockAndLock(Project project, NamedProjectBlock block, boolean lock) {
        StateTransition editStateTransition = stateMachineForProject(project).getTransition(project.getProjectState(), StateTransitionType.EDIT);
        if (editStateTransition != null) {
            transitionProjectToStatus(project, editStateTransition.getTo(), null);
        }

        NamedProjectBlock blockToReturn = block;
        if (block.editRequiresCloning(environment.now())) {
            String userName = userService.currentUser() == null ? userService.getSystemUserName() : userService.currentUser().getUsername();


            // Clear new label if project state is AutoApproval
            if (project.getStateModel().equals(StateModel.AutoApproval)) {
                block.setNew(false);
            }

            // Clear new label from Questions block
            if (block.getBlockType().equals(ProjectBlockType.Questions)) {
                ProjectQuestionsBlock pqb = (ProjectQuestionsBlock) block;

                for (ProjectQuestion question : pqb.getQuestions()) {
                    if (question.isNew()) {
                        question.setNew(false);
                    }
                }
            }


            if (!project.getStateModel().isApprovalRequired()) {
                project.approveBlock(block, userName, environment.now());
            }

            NamedProjectBlock clone = block.cloneBlock(userName, environment.now());

            if (!block.getProject().getStateModel().isReportOnLastApproved()) {
                clone.setReportingVersion(true);
                block.setReportingVersion(false);
            } else {
                clone.setReportingVersion(false);
            }

            block.setLatestVersion(false);
            project.getLatestProjectBlocks().remove(block);

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

    public Integer countByQuestion(Integer questionId) {
        return projectRepository.countByQuestion(questionId);
    }

    public Collection<Project> getProjectbyProgrammeId(int id) {
        return projectRepository.findAllByProgramme(new Programme(id, ""));
    }

    public List<Project> getProjectsForProgramme(Integer id, String name) {
        List<Project> projects = new ArrayList<>();
        List<Programme> programmes = new ArrayList<>();

        if (id != null) {
            programmes.add(programmeService.getById(id, false));
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
        ld.setBlock(block);
        lockDetailsRepository.save(ld);

        block.setLockDetails(ld);
        projectRepository.save(project);
        auditService.auditCurrentUserActivity(EntityType.projectBlock, block.getId(), ActivityType.StartEdit);
        return ld;
    }


    public Project recordRecommendation(Integer id, Project.Recommendation recommendation, String comments) {
        Project project = this.get(id);
        if (ProjectStatus.Assess.equals(project.getStatusType())) {
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

    public List<ProjectBlockHistoryItem> getHistoryForBlock(Integer projectId, Integer versionNumber) {
        List<ProjectBlockHistoryItem> blockHistory = projectRepository.getProjectHistoryForProjectAndDisplayOrder(projectId, versionNumber);
        for (ProjectBlockHistoryItem projectBlockHistoryItem : blockHistory) {
            Set<Label> labelsForBlock = labelService.getLabelsForBlock(projectBlockHistoryItem.getBlockId());
            projectBlockHistoryItem.setLabels(labelsForBlock);
            String userFullName = userService.getUserFullName(projectBlockHistoryItem.getActionedBy());
            projectBlockHistoryItem.setActionedBy(userFullName);
        }
        return blockHistory;
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
        project.getLatestProjectBlocks().removeIf(b -> b.getId().equals(block.getId()));
        updateProject(project);


        List<NamedProjectBlock> allBlocksOfType = project.getBlocksByTypeAndDisplayOrder(block.getBlockType(), block.getDisplayOrder());

        // find latest block and set latest version flag.
        allBlocksOfType.stream().filter(b -> LAST_APPROVED.equals(b.getBlockStatus()))
                .forEach(b -> {
                    b.setLatestVersion(true);
                    project.getLatestProjectBlocks().add(b);
                });

        if (!project.hasUnapprovedBlocks()) {
            StateTransition revertStateTransition = stateMachineForProject(project).getTransition(project.getProjectState(), StateTransitionType.REVERT);
            if (revertStateTransition != null) {
                transitionProjectToStatus(project, revertStateTransition.getTo(), null);
            }
        }

        auditService.auditCurrentUserActivity(String.format("%s block unapproved version deleted from project %d", block.getBlockDisplayName(), project.getId()));
    }

    public void setMarkedForCorporate(int projectId, boolean markedForCorporate) {
        Project project = get(projectId);
        project.setMarkedForCorporate(markedForCorporate);

        if (markedForCorporate) {
            auditService.auditCurrentUserActivity("Marked project with id " + projectId + " for corporate reporting");
        } else {
            auditService.auditCurrentUserActivity("Removed project with id " + projectId + " from corporate reporting");
        }
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

        sourceProject.getInternalBlocks().forEach(block -> {
            InternalProjectBlock clone = block.clone();
            clone.setProject(clonedProject);
            clonedProject.getInternalBlocks().add(clone);
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
            clone.setStatusName(projectHistory.getStatusName());
            clone.setSubStatusName(projectHistory.getSubStatusName());
            projectHistoryRepository.save(clone);
        }

        List<EntitySubscription> subscriptions = notificationService.findAllByEntityTypeAndEntityId(EntityType.project, sourceProject.getId());
        for (EntitySubscription subscription : subscriptions) {
            notificationService.subscribe(subscription.getUsername(), EntityType.project, cloned.getId());
        }

        // this will force loading of sorted blocks etc.
        return getEnrichedProject(cloned.getId());
    }

    private Project copyProjectProperties(Project fromProject, Project toProject) {
        toProject.setTitle(fromProject.getTitle());
        toProject.setStatusName(fromProject.getStatusName());
        toProject.setSubStatusName(fromProject.getSubStatusName());
        toProject.setRecommendation(fromProject.getRecommendation());
        toProject.setTemplate(fromProject.getTemplate());
        toProject.setProgramme(fromProject.getProgramme());
        toProject.setStateModel(fromProject.getStateModel());

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
        toProject.setMarkedForCorporate(fromProject.isMarkedForCorporate());
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
        namedProjectBlock.setInfoMessage(block.getInfoMessage());
        return namedProjectBlock;
    }

    public void refreshProjectStatus(final Set<Integer> projectIds, EventType eventType) {
        projectRepository.findAllById(projectIds).forEach(p -> refreshProjectStatus(p, eventType));
    }

    private void refreshProjectStatus(final Project project, final EventType eventType) {

        if (Active.equals(project.getStatusType()) && PaymentAuthorisationPending.equals(project.getSubStatusType())) {
            if (EventType.PaymentAuthorised.equals(eventType)) {
                transitionProjectToStatus(project, new ProjectState(Active, null), null);
            } else if (EventType.PaymentDeclined.equals(eventType)) {
                project.setSubStatus(ProjectSubStatus.ApprovalRequested);
                projectRepository.save(project);
            }
        }
    }

    public Project moveProjectToProgrammeAndTemplate(Integer id, Integer progId, Integer templateId) {

        Project project = get(id);
        Programme newProgramme = programmeService.getById(progId, false);
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
                CategoryValue cat = refDataService.getCategoryValue(risk.getRiskCategory().getId());
                if (cat == null || !CategoryValue.Category.RiskCategory.equals(cat.getCategory())) {
                    throw new ValidationException("riskCategory", "Invalid Risk Category");
                }
            } else {
                throw new ValidationException("riskCategory", "Risk Category is mandatory for project risk");

            }

            if (risk.getInitialImpactRating() != null && risk.getInitialProbabilityRating() != null) {
                RiskLevelLookup one = riskLevelLookupRepository.findById(new RiskLevelID(risk.getInitialImpactRating(), risk.getInitialProbabilityRating())).orElse(null);
                if (one == null) {
                    throw new ValidationException("Unable to find relevant initial Risk Level");
                }
                risk.setInitialRiskLevel(one);
            } else {
                throw new ValidationException("Initial Risk Ratings are mandatory");
            }
            if (risk.getResidualImpactRating() != null && risk.getResidualProbabilityRating() != null) {
                RiskLevelLookup one = riskLevelLookupRepository.findById(new RiskLevelID(risk.getResidualImpactRating(), risk.getResidualProbabilityRating())).orElse(null);
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

    public AnnualSpendSummary createOrUpdateSpendEntry(Integer projectId, ProjectLedgerItemRequest lineItem, Integer year) {
        Project project = projectRepository.getOne(projectId);
        ProjectBudgetsBlock projectBudgetsBlock = (ProjectBudgetsBlock) get(projectId).getSingleLatestBlockOfType(ProjectBlockType.ProjectBudgets);
        checkForLock(projectBudgetsBlock);

        lineItem.setBlockId(projectBudgetsBlock.getId());
        lineItem.setProjectId(projectId);

        financeService.createOrUpdateSpendEntry(lineItem);

        return getAnnualSpendSummaryForSpecificYear(project, year);
    }

    public void createOrUpdateProjectLedgerEntry(Integer projectId, Integer blockId, ProjectLedgerItemRequest entry) {
        Project project = projectRepository.getOne(projectId);
        NamedProjectBlock block = project.getProjectBlockById(blockId);
        checkForLock(block);

        entry.setProjectId(projectId);
        entry.setBlockId(blockId);

        financeService.createOrUpdateProjectLedgerEntry(entry);
    }

    public void updateManagingOrgForProjectsOnProgramme(Programme programme, Organisation managingOrganisation) {

        if (!Objects.equals(programme.getManagingOrganisationId(), managingOrganisation.getId())) {
            throw new ValidationException("Managing Organisation does not match programme");
        }
        if (!managingOrganisation.isManagingOrganisation()) {
            throw new ValidationException("Organisation is not a managing organisation");
        }
        projectRepository.updateProjectManagingOrgByProgramme(managingOrganisation.getId(), programme.getId());
        financeService.updatePaymentEntriesManagingOrgByProgramme(managingOrganisation.getId(), programme.getId());
    }


    public ProjectsTransferResult transfer(List<Integer> projectIds, Integer organisationId) {
        Organisation toOrganisation = organisationService.findOne(organisationId);
        if (toOrganisation == null) {
            throw new NotFoundException();
        }

        int nbTransferred = 0;
        int nbErrors = 0;
        Organisation fromOrganisation = null;
        for (Integer projectId : projectIds) {
            Project project = getEnrichedProject(projectId);

            if (!canProjectBeAssignedToTemplate(project.getTemplateId(), organisationId)){
                String projectString = project.getTemplate().getNumberOfProjectAllowedPerOrg() > 1 ? "projects" : "project";
                String isAreString = project.getTemplate().getNumberOfProjectAllowedPerOrg() > 1 ? "are" : "is";
                throw new ValidationException(String.format("Only %d %s %s allowed for this project type.", project.getTemplate().getNumberOfProjectAllowedPerOrg(), projectString, isAreString));
            }

            if (fromOrganisation == null) {
                fromOrganisation = project.getOrganisation();
            } else if (!fromOrganisation.getId().equals(project.getOrganisation().getId())) {
                throw new ValidationException("You can only bulk transfer projects from the same organisation.");
            }

            if (!project.getAllowedActions().contains(Project.Action.Transfer)) {
                nbErrors++;
            } else {
                transfer(project, fromOrganisation, toOrganisation);
                nbTransferred++;
            }
        }

        return new ProjectsTransferResult(nbTransferred, nbErrors);
    }
    // this function should only be used to transfer project for e2e test in DEV and QAS.
    public ProjectsTransferResult transferTestProject(List<Integer> projectIds, Integer organisationId) {

        if (!environment.isTestEnvironment()) {
            throw new ValidationException("You can only bulk transfer test projects in test environments.");
        }

        Organisation toOrganisation = organisationService.findOne(organisationId);
        if (toOrganisation == null) {
            throw new NotFoundException();
        }

        int nbTransferred = 0;
        int nbErrors = 0;
        Organisation fromOrganisation = null;
        for (Integer projectId : projectIds) {
            Project project = getEnrichedProject(projectId);

            if (fromOrganisation == null) {
                fromOrganisation = project.getOrganisation();
            } else if (!fromOrganisation.equals(project.getOrganisation())) {
                throw new ValidationException("You can only bulk transfer projects from the same organisation.");
            }

//            if (!project.getAllowedActions().contains(Project.Action.Transfer)) {
//                nbErrors++;
//            }
//            else {
            transfer(project, fromOrganisation, toOrganisation);
            nbTransferred++;
//            }
        }

        return new ProjectsTransferResult(nbTransferred, nbErrors);
    }

    private void transfer(Project project, Organisation fromOrganisation, Organisation toOrganisation) {
        String historyDescription = String.format("Transferred from %s to %s", fromOrganisation.getName(), toOrganisation.getName());
        ProjectHistory historyEntry = new ProjectHistory(Transfer, historyDescription, "Project transferred");

        project.setOrganisation(toOrganisation);
        project.setOrganisationGroupId(null); // for consortium we are for now changing the bidding arrangement to individual
        project.getHistory().add(historyEntry);
        updateProject(project);

        Map<String, Object> model = new HashMap<String, Object>() {{
            put("fromOrganisation", fromOrganisation);
            put("toOrganisation", toOrganisation);
        }};
        notificationService.createNotification(ProjectTransfer, project, model);
    }

    public List<SAPMetaData> getPaymentMetaData(Integer projectId, Integer blockId, Integer categoryId, Integer yearMonth) {
        return financeService.getSapMetaData(projectId, blockId, yearMonth, LedgerType.PAYMENT, LedgerStatus.ACTUAL, categoryId);
    }

    public void moveAnswerValueToGrantSource(Integer questionId) {
        List<Project> projects = projectRepository.findAllForQuestion(questionId);

        for (Project project : projects) {
            Answer answer = null;

            for (ProjectQuestionsBlock questionsBlock : project.getQuestionsBlocks()) {
                for (Answer a : questionsBlock.getAnswers()) {
                    if (a.getQuestionId().equals(questionId)) {
                        answer = a;
                    }
                }
            }

            if (answer == null) {
                log.error("could not find answer for question {} in project P{}", questionId, project.getId());
            } else if (answer.getNumericAnswer() != null) {
                project.getGrantSourceBlock().setZeroGrantRequested(false);
                project.getGrantSourceBlock().setGrantValue(answer.getNumericAnswer().longValue());
                project.getGrantSourceBlock().setLastModified(environment.now());
            }
        }

        projectRepository.saveAll(projects);
    }

    public <T extends NamedProjectBlock> T updateProjectBlock(Integer projectId, Integer blockId, T updatedBlock, boolean releaseLock) {
        Project project = get(projectId);
        T existingBlock = (T) project.getSingleLatestBlockOfType(updatedBlock.getBlockType());
        checkForLock(existingBlock);
        existingBlock.merge(updatedBlock);
        releaseOrRefreshLock(existingBlock, releaseLock);
        updateProject(project);

        return existingBlock;
    }

    public void updateInternalProjectBlock(Integer projectId, Integer blockId, InternalProjectBlock updated) {
        Project project = get(projectId);

        InternalProjectBlock existing = project.getInternalBlockById(blockId);
        String auditSummary = existing.merge(updated);

        if (auditSummary != null) {
            auditService.auditCurrentUserActivity(auditSummary);
        }

        updateProject(project);
    }

    public NamedProjectBlock revertProjectBlock(Integer id, Integer blockId) {
        Project project = this.get(id);

        NamedProjectBlock block = project.getProjectBlockById(blockId);
        if (block != null) {

            if (block.isBlockReversionAllowed()) {

                project.getProjectBlocks().remove(block);
                project.getLatestProjectBlocks().remove(block);

                try {
                    NamedProjectBlock newBlock = createBlockFromTemplate(project, project.getTemplate().getSingleBlockByTypeAndDisplayOrder(block.getBlockType(), block.getDisplayOrder()));
                    newBlock.setNew(block.isNew());
                    project.addBlockToProject(newBlock);
                    projectRepository.save(project);

                    return project.getBlockByTypeDisplayOrderAndLatestVersion(newBlock.getBlockType(), newBlock.getDisplayOrder());
                } catch (Exception e) {
                    throw new ValidationException("Unable to revert this block");
                }
            } else {
                throw new ValidationException("Unable to revert this type of block");
            }
        }
        throw new ValidationException("Unable to find the block to revert");
    }

    public Label createProjectLabel(Integer projectId, Label label) {
        Project project = this.get(projectId);

        if (project == null) {
            throw new ValidationException("Project not found: " + projectId);
        }

        if (label.getType() == null) {
            throw new ValidationException("Label type cannot be null");
        }

        label.setProjectId(projectId);
        label = labelService.createLabel(label);

        if (label.getType().name().equalsIgnoreCase(LabelType.Custom.name())) {

            // Check if the label was already applied to the project
            String labelText = label.getText();
            if (project.getLabelNamesByType(LabelType.Custom.name()).stream()
                    .anyMatch(labelName -> labelName.equalsIgnoreCase(labelText))) {
                throw new ValidationException("Label already exists");
            }

            String historyDescription = String.format("Label \"%s\" applied", label.getText());
            ProjectHistory historyEntry = new ProjectHistory(Label, historyDescription, null);
            historyEntry.setExternalId(label.getId());
            project.getHistory().add(historyEntry);
        } else {

            // Check if pre-set label is already used, if not set to true
            PreSetLabel preSetLabel = preSetLabelService.find(label.getPreSetLabel().getId());
            if (preSetLabel != null && !preSetLabel.isUsed()) {
                preSetLabel.setUsed(true);
                preSetLabelRepository.saveAndFlush(preSetLabel);
            }

            String historyDescription = String.format("Label \"%s\" applied", label.getPreSetLabel().getLabelName());
            ProjectHistory historyEntry = new ProjectHistory(Label, historyDescription, null);
            historyEntry.setExternalId(label.getId());
            project.getHistory().add(historyEntry);
        }

        project.addLabel(label);
        projectRepository.save(project);
        return label;
    }

    public List<Project> findAllProjectsWithScheduledPaymentDue(String asOfDate) {
        List<Project> allProjectsWithScheduledPaymentDue = projectRepository.findAllProjectsWithScheduledPaymentDue(ProjectBlockType.LearningGrant.name(), asOfDate);
        return allProjectsWithScheduledPaymentDue;
    }
}
