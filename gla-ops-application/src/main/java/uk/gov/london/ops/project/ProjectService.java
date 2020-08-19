/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.project;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import uk.gov.london.common.skills.SkillsGrantType;
import uk.gov.london.ops.EventType;
import uk.gov.london.ops.audit.ActivityType;
import uk.gov.london.ops.domain.PreSetLabel;
import uk.gov.london.ops.domain.Requirement;
import uk.gov.london.ops.framework.EntityType;
import uk.gov.london.ops.framework.exception.ForbiddenAccessException;
import uk.gov.london.ops.framework.exception.NotFoundException;
import uk.gov.london.ops.framework.exception.ValidationException;
import uk.gov.london.ops.framework.feature.Feature;
import uk.gov.london.ops.framework.feature.FeatureStatus;
import uk.gov.london.ops.notification.EntitySubscription;
import uk.gov.london.ops.notification.NotificationType;
import uk.gov.london.ops.organisation.OrganisationGroupService;
import uk.gov.london.ops.organisation.OrganisationService;
import uk.gov.london.ops.organisation.model.Organisation;
import uk.gov.london.ops.organisation.model.OrganisationGroup;
import uk.gov.london.ops.payment.*;
import uk.gov.london.ops.permission.implementation.DefaultAccessControlSummaryRepository;
import uk.gov.london.ops.permission.implementation.ProjectAccessControlRepository;
import uk.gov.london.ops.programme.ProgrammeService;
import uk.gov.london.ops.programme.domain.Programme;
import uk.gov.london.ops.programme.domain.ProgrammeTemplate;
import uk.gov.london.ops.project.accesscontrol.AccessControlRelationshipType;
import uk.gov.london.ops.project.accesscontrol.DefaultAccessControlSummary;
import uk.gov.london.ops.project.accesscontrol.GrantAccessTrigger;
import uk.gov.london.ops.project.block.*;
import uk.gov.london.ops.project.budget.AnnualSpendSummary;
import uk.gov.london.ops.project.budget.ProjectBudgetsBlock;
import uk.gov.london.ops.project.claim.Claim;
import uk.gov.london.ops.project.claim.ClaimStatus;
import uk.gov.london.ops.project.funding.FundingBlock;
import uk.gov.london.ops.project.grant.GrantSourceBlock;
import uk.gov.london.ops.project.implementation.repository.*;
import uk.gov.london.ops.project.internalblock.InternalProjectBlock;
import uk.gov.london.ops.project.label.Label;
import uk.gov.london.ops.project.label.LabelService;
import uk.gov.london.ops.project.label.LabelType;
import uk.gov.london.ops.project.label.PreSetLabelService;
import uk.gov.london.ops.project.milestone.Milestone;
import uk.gov.london.ops.project.milestone.ProjectMilestonesBlock;
import uk.gov.london.ops.project.question.Answer;
import uk.gov.london.ops.project.question.ProjectQuestion;
import uk.gov.london.ops.project.question.ProjectQuestionsBlock;
import uk.gov.london.ops.project.risk.*;
import uk.gov.london.ops.project.skills.*;
import uk.gov.london.ops.project.state.*;
import uk.gov.london.ops.project.template.TemplateService;
import uk.gov.london.ops.project.template.domain.*;
import uk.gov.london.ops.refdata.Borough;
import uk.gov.london.ops.refdata.CategoryValue;
import uk.gov.london.ops.refdata.RefDataService;
import uk.gov.london.ops.refdata.Ward;
import uk.gov.london.ops.user.UserFinanceThresholdService;
import uk.gov.london.ops.user.domain.User;
import uk.gov.london.ops.user.domain.UserOrgFinanceThreshold;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static uk.gov.london.common.GlaUtils.parseInt;
import static uk.gov.london.common.user.BaseRole.OPS_ADMIN;
import static uk.gov.london.ops.notification.NotificationType.ProjectTransfer;
import static uk.gov.london.ops.payment.implementation.ProjectLedgerEntryMapper.BESPOKE_PREFIX;
import static uk.gov.london.ops.payment.implementation.ProjectLedgerEntryMapper.RECLAIMED_PREFIX;
import static uk.gov.london.ops.permission.PermissionType.PROJ_READ;
import static uk.gov.london.ops.permission.PermissionType.PROJ_VIEW_RECOMMENDATION;
import static uk.gov.london.ops.project.ProjectHistory.HistoryEventType.Label;
import static uk.gov.london.ops.project.ProjectHistory.HistoryEventType.Transfer;
import static uk.gov.london.ops.project.ProjectHistory.Transition.Created;
import static uk.gov.london.ops.project.block.NamedProjectBlock.Action.DELETE;
import static uk.gov.london.ops.project.block.NamedProjectBlock.BlockStatus.LAST_APPROVED;
import static uk.gov.london.ops.project.block.NamedProjectBlock.BlockStatus.UNAPPROVED;
import static uk.gov.london.ops.project.skills.LearningGrantEntryType.SUPPORT;
import static uk.gov.london.ops.project.state.ProjectStatus.*;
import static uk.gov.london.ops.project.state.ProjectSubStatus.*;
import static uk.gov.london.ops.project.state.StateTransitionResult.Status.ILLEGAL_TRANSITION;
import static uk.gov.london.ops.project.state.StateTransitionResult.Status.INVALID;

/**
 * Service interface for managing projects.
 *
 * @author Steve Leach
 */
@Service
public class ProjectService extends BaseProjectService {

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
    LabelService labelService;

    @Autowired
    LockDetailsRepository lockDetailsRepository;

    @Autowired
    ProjectHistoryRepository projectHistoryRepository;

    @Autowired
    ProjectSummaryRepository projectSummaryRepository;

    @Autowired
    RiskLevelLookupRepository riskLevelLookupRepository;

    @Autowired
    PreSetLabelService preSetLabelService;

    @Autowired
    PreSetLabelRepository preSetLabelRepository;

    @Autowired
    private SkillsService skillsService;

    @Autowired
    private ProjectBlockRepository projectBlockRepository;

    @Autowired
    DefaultAccessControlSummaryRepository defaultAccessControlSummaryRepository;

    @Autowired
    ProjectAccessControlRepository projectAccessControlRepository;

    @Autowired
    UserFinanceThresholdService userFinanceThresholdService;

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

        if (!currentUser.isApproved() || (organisationId != null
                && !permissionService.currentUserHasPermissionForOrganisation(PROJ_READ, organisationId))) {
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

    public List<Project> findAllByProgramme(Programme programme) {
        return projectRepository.findAllByProgramme(programme);
    }

    public int countAllByProgramme(Programme programme) {
        return projectRepository.countAllByProgramme(programme);
    }

    public List<Project> findAllByProgrammeAndTemplate(Programme programme, Template template) {
        return projectRepository.findAllByProgrammeAndTemplate(programme, template);
    }

    public List<Project> findAllByProgrammeAndTemplateAndOrganisation(Programme programme, Template template,
            Organisation organisation) {
        return projectRepository.findAllByProgrammeAndTemplateAndOrganisation(programme, template, organisation);
    }

    public List<Project> findAllByOrganisationAndStatusName(Organisation organisation, String statusName) {
        return projectRepository.findAllByOrganisationAndStatusName(organisation, statusName);
    }

    public List<Project> findAllByGroupAndOrganisation(Integer organisationGroupId, Integer organisationId) {
        return projectRepository.findAllByGroupAndOrganisation(organisationGroupId, organisationId);
    }

    public List<Project> findAllByTitle(String title) {
        return projectRepository.findAllByTitle(title);
    }

    public Set<Project> findAllByPaymentsWBSCode(String blockType, String wbsCode) {
        return projectRepository.findAllByPaymentsWBSCode(blockType, wbsCode);
    }

    public Project findFirstByLegacyProjectCode(Integer legacyProjectCode) {
        return projectRepository.findFirstByLegacyProjectCode(legacyProjectCode);
    }

    public Integer countByProgrammeAndStatusName(Programme programme, String statusName) {
        return projectRepository.countByProgrammeAndStatusName(programme, statusName);
    }

    public Integer countAssociatedProjects(Integer programmeId) {
        return projectRepository.countAssociatedProjects(programmeId);
    }

    public Boolean checkAccessForProject(String username, Integer projectId) {
        return projectRepository.checkAccessForProject(username, projectId);
    }

    public Project save(Project project) {
        return projectRepository.save(project);
    }

    public void saveAll(Collection<Project> projects) {
        projectRepository.saveAll(projects);
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
            project.setOrganisationGroup(organisationGroupService.findByGroupId(project.getOrganisationGroupId()));
        });
    }

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public NamedProjectBlock updateNamedProjectBlockNewTx(NamedProjectBlock block, Integer projectId) {
        return updateProjectBlock(block, projectId);
    }

    public NamedProjectBlock updateProjectBlock(NamedProjectBlock block, Integer projectId) {
        dataAccessControlService.checkProjectAccess(projectId);

        projectRepository.updateLastModifiedForProject(environment.now(), projectId);

        block.setBlockMarkedComplete(block.isComplete());
        return projectBlockRepository.save(block);
    }

    public <T extends NamedProjectBlock> T updateProjectBlock(Integer projectId, Integer blockId, T updatedBlock,
            boolean releaseLock) {
        Project project = get(projectId);
        T existingBlock = (T) project.getSingleLatestBlockOfType(updatedBlock.getBlockType());
        checkForLock(existingBlock);
        existingBlock.merge(updatedBlock);
        releaseOrRefreshLock(existingBlock, releaseLock);
        updateProject(project);

        return existingBlock;
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
            boolean resetDPF = ObjectUtils.compare(block.getDisposalProceedsFundValue(),
                    newBlock.getDisposalProceedsFundValue()) != 0;
            boolean resetRCGF = ObjectUtils.compare(block.getRecycledCapitalGrantFundValue(),
                    newBlock.getRecycledCapitalGrantFundValue()) != 0;
            resetReclaimMilestoneAmounts(milestonesBlock, resetGrant, resetDPF, resetRCGF);

        }
    }

    private void resetReclaimMilestoneAmounts(ProjectMilestonesBlock milestonesBlock, boolean resetGrant, boolean resetDPF,
            boolean resetRCGF) {
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
        ProjectDetailsBlock existingDetailsBlock = (ProjectDetailsBlock) project
                .getSingleLatestBlockOfType(ProjectBlockType.Details);
        checkForLock(existingDetailsBlock);

        handleRestrictedAddressChange(block, existingDetailsBlock);

        if (block.getBorough() != null) {
            validateBorough(block.getBorough(),block);
        }

        existingDetailsBlock.merge(block);

        deleteLock(existingDetailsBlock);
        this.updateProject(project);
        return existingDetailsBlock;
    }

    protected void validateBorough(String boroughs, ProjectDetailsBlock block) {
        List < String > boroughNames = Arrays.asList(boroughs.split(block.getBoroughDelimiter()));

        for (String boroughName:boroughNames) {
            Borough borough = refDataService.findBoroughByName(boroughName);
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
                    throw new ValidationException(String.format("Ward with id %d is not a member of borough %s",
                            block.getWardId(), block.getBorough()));
                }
            }
        }
    }

    private void handleRestrictedAddressChange(ProjectDetailsBlock block, ProjectDetailsBlock existingDetailsBlock) {
        if (!existingDetailsBlock.isAddressRestricted() && block.isAddressRestricted()) {
            block.setAddress("Restricted");
            block.setPostcode(null);
            block.setCoordX(null);
            block.setCoordY(null);
            block.setWardId(null);
            block.setPlanningPermissionReference(null);
        }
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

        // Check if any default access control exists and add to the project access
        List<DefaultAccessControlSummary> defaultProjectAccess = dataAccessControlService
                .getDefaultProjectAccess(project.getProgrammeId(), project.getTemplateId());

        if (defaultProjectAccess != null && !defaultProjectAccess.isEmpty()) {
            for (DefaultAccessControlSummary defaultAccess : defaultProjectAccess) {
                Organisation organisation = organisationService.findOne(defaultAccess.getOrganisationId());
                project.addToAccessControlList(organisation, defaultAccess.getRelationshipType(), GrantAccessTrigger.TEMPLATE);
            }
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
            Integer countProjectsWithTemplate = projectRepository
                    .countByTemplateAndOrganisationAndStatusNameIsNot(template, organisation, "Closed");
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
            throw new ValidationException(
                    String.format("Programme %s does not contain template with ID : %d", programme.getName(),
                            project.getTemplateId()));
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
        Organisation persistedOrg;

        if (project.getOrganisation() != null) {
            Integer id = project.getOrganisation().getId();
            persistedOrg = organisationService.findOne(id);
            if (persistedOrg == null) {
                throw new ValidationException("Organisation specified is not recognised.");
            } else if (persistedOrg.isTechSupportOrganisation()) {
                throw new ValidationException("Organisation cannot create project.");
            }
        } else {
            persistedOrg = organisationService.findOne(userService.currentUser().getOrganisations().iterator().next().getId());
        }

        project.setOrganisation(persistedOrg);

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

    public void deleteProject(Integer projectId) {
        if (environment.initTestData() && (projectId == -123)) {
            // If we are in an environment that supports artificial test data
            // and we ask to delete the "magic" project ID
            // then we delete all test projects.
            deleteTestProjects();
        } else {
            financeService.deleteAllTestDataByProjectId(projectId);
            projectRepository.deleteById(projectId);
        }
    }

    private void deleteTestProjects() {
        for (Integer projectId : projectRepository.findAllProjectIdsByTitleLike("IT_PROJECT_")) {
            projectRepository.deleteById(projectId);
        }
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
            return financeService.getAnnualSpendForSpecificYear(projectBudgets.getId(), year);
        }
        return null;
    }

    public AnnualSpendSummary updateAnnualSpendAndBudgetLedgerEntries(Project project, Integer year, BigDecimal revenue,
            BigDecimal capital, boolean autosave) {
        ProjectBudgetsBlock projectBudgets = (ProjectBudgetsBlock) project
                .getSingleLatestBlockOfType(ProjectBlockType.ProjectBudgets);
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
        if (!canProjectBeAssignedToTemplate(template.getId(), project.getOrganisation().getId())) {
            String projectString = template.getNumberOfProjectAllowedPerOrg() > 1 ? "projects" : "project";
            String isAreString = template.getNumberOfProjectAllowedPerOrg() > 1 ? "are" : "is";
            throw new ValidationException(
                    String.format("Only %d %s %s allowed for this project type.", template.getNumberOfProjectAllowedPerOrg(),
                            projectString, isAreString));
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

        ProjectStateMachine stateMachine = stateMachineForProject(project.getStateModel());

        ProjectState currentState = project.currentState();

        if (project.anyBlocksLocked()) {
            return new StateTransitionResult(INVALID, "Unable to proceed as at least one block is being edited.");
        }

        Set<String> userRoles = getUserRolesForProject(project);

        StateTransition stateTransition = stateMachine.getTransition(
                project.getProjectState(),
                targetState,
                userRoles,
                project.getProgramme().isEnabled(),
                project.getProgramme().isInAssessment(),
                project.isPreviouslySubmitted(),
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

        projectRepository.save(project);

        return new StateTransitionResult(StateTransitionResult.Status.SUCCESS);
    }

    /**
     * This method does not actually make the state transition but is used by the API to verify the transition can be made.
     */
    public void validateTransitionProjectToStatus(Project project, ProjectState targetState) {
        preStateTransitionActions(project, targetState, null);
    }

    private boolean autoApprovalProjectAndFeatureDisabled(Project project) {
        return StateModel.AutoApproval.equals(project.getStateModel()) && (!featureStatus
                .isEnabled(Feature.SubmitAutoApprovalProject));
    }

    void preStateTransitionActions(Project project, ProjectState targetState, StateTransitionType transitionType) {
        if (Returned.equals(targetState.getStatusType())) {
            project.setRecommendation(null);
        }

        if (StateTransitionType.APPROVAL.equals(transitionType)) {
            approveAllProjectBlocks(project);
            initialiseWithSkillsData(project);
            project.setApprovalWillGenerateReclaimPersisted(false);
            project.setApprovalWillGeneratePaymentPersisted(false);
        }

        if (targetState.equals(Active, PaymentAuthorisationPending)) {
            validateProjectForRequestingPaymentAuthorisation(project);

            if (project.getTemplate().getMilestoneType().equals(Template.MilestoneType.MonetarySplit)
                    && project.getGrantSourceAdjustmentAmount().signum() < 0) {
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

            ProjectMilestonesBlock milestones = (ProjectMilestonesBlock) project
                    .getSingleLatestBlockOfType(ProjectBlockType.Milestones);
            if (project.getTemplate().isBlockPresent(ProjectBlockType.Milestones)) {
                for (Milestone milestone : milestones.getApplicableMilestones()) {
                    if (Requirement.mandatory.equals(milestone.getRequirement())) {
                        if (project.getStateModel().isAllowClosureWithUnapprovedMandatoryMilestones() && project.getTemplate()
                                .getAllowMonetaryMilestones() && !milestone.isManuallyCreated() && !milestone.isClaimed()) {
                            throw new ValidationException("All mandatory milestones that apply to this project must be claimed "
                                    + "before the project can be closed as complete");
                        } else if (!project.getStateModel().isAllowClosureWithUnapprovedMandatoryMilestones() && !milestone
                                .isManuallyCreated() && !milestone.isApproved()) {
                            throw new ValidationException("All mandatory milestones must be claimed and approved "
                                    + "before the project can be closed as complete");
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
        LearningGrantBlock learningGrantBlock = (LearningGrantBlock) project
                .getSingleLatestBlockOfType(ProjectBlockType.LearningGrant);
        if (learningGrantBlock != null) {
            Map<Integer, SkillsPaymentProfile> profiles = skillsService.getSkillsPaymentProfiles(
                    learningGrantBlock.getGrantType(), learningGrantBlock.getStartYear()).stream()
                    .collect(Collectors.toMap(SkillsPaymentProfile::getPeriod, Function.identity()));

            Map<Integer, SkillsPaymentProfile> supportProfiles = skillsService.getSkillsPaymentProfiles(
                    SkillsGrantType.AEB_LEARNER_SUPPORT, learningGrantBlock.getStartYear()).stream()
                    .collect(Collectors.toMap(SkillsPaymentProfile::getPeriod, Function.identity()));

            List<LearningGrantEntry> sorted = learningGrantBlock.getLearningGrantEntries().stream()
                    .sorted(Comparator.comparingInt(LearningGrantEntry::getPeriod)).collect(Collectors.toList());
            BigDecimal totalSoFar = BigDecimal.ZERO;
            LearningGrantAllocation startYearAllocation = learningGrantBlock.getAllocation(learningGrantBlock.getStartYear());
            BigDecimal allocation = startYearAllocation == null || startYearAllocation.getAllocation() == null ? BigDecimal.ZERO
                    : startYearAllocation.getAllocation();
            for (LearningGrantEntry learningGrantEntry : sorted) {
                SkillsPaymentProfile profile =
                        learningGrantEntry.getType() == SUPPORT ? supportProfiles.get(learningGrantEntry.getPeriod())
                                : profiles.get(learningGrantEntry.getPeriod());
                if (profile != null) {
                    learningGrantEntry.setPercentage(profile.getPercentage());
                    learningGrantEntry.setPaymentDate(profile.getPaymentDate());
                    if (learningGrantEntry.getPeriod() != 12) {
                        if (profile.getPercentage() != null && allocation != null) {
                            BigDecimal lgeAllocation = profile.getPercentage().divide(new BigDecimal(100)).multiply(allocation)
                                    .setScale(2, BigDecimal.ROUND_HALF_UP);
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
        if (getApprovalWillCreatePendingGrantPayment(project)) {
            //If the project will create pending  and hasn't got SAP vendor id
            if (StringUtils.isEmpty(project.getSapVendorId())) {
                throw new ValidationException("SAP vendor ID has not been provided. "
                        + "The SAP vendor ID must be added to the organisation details by a OPS Admin.");
            }

            validateWBSCodesForRequestingPaymentAuthorisation(project);

            String ceCodeForTemplate = project.getProgramme().getCeCodeForTemplate(project.getTemplateId());
            if (StringUtils.isEmpty(ceCodeForTemplate)) {
                throw new ValidationException("A cost element code must be added to the project template "
                        + "associated with this programme by an OPS admin.");
            }

            String companyName = project.getProgramme().getCompanyName();
            if (StringUtils.isEmpty(companyName)) {
                throw new ValidationException(
                        "A company name must be entered against the programme by an OPS admin, before any payment can be made.");
            }
        }

        if (project.isPendingContractSignature()) {
            throw new ValidationException("Pending payments cannot be submitted for authorisation as the contract "
                    + "for this project type has not been signed.");
        }
    }

    void validateWBSCodesForRequestingPaymentAuthorisation(Project project) {
        Set<SpendType> spendTypes = getSpendTypesForProject(project);
        if (spendTypes.isEmpty()) {
            boolean defaultWbsCodeSetForTemplate = project.getProgramme().defaultWbsCodeSetForTemplate(project.getTemplateId());
            if (!defaultWbsCodeSetForTemplate) {
                throw new ValidationException("Default WBS code has not been specified. A default WBS code must be specified on "
                        + "the project template associated with this programme by an OPS admin.");
            }

            String wbsCodeForTemplate = project.getProgramme().getWbsCodeForTemplate(project.getTemplateId());
            if (StringUtils.isEmpty(wbsCodeForTemplate)) {
                throw new ValidationException("WBS code has not been provided. A WBS code must be added to the project "
                        + "template associated with this programme by an OPS admin.");
            }
        } else {
            for (SpendType spendType : spendTypes) {
                String wbsCode = project.getProgramme().getWbsCodeForTemplate(project.getTemplateId(), spendType);
                if (StringUtils.isEmpty(wbsCode)) {
                    throw new ValidationException(spendType + " WBS code has not been provided. A " + spendType
                            + " WBS code must be added to the project template associated with this programme by an OPS admin.");
                }
            }
        }
    }

    Set<SpendType> getSpendTypesForProject(Project project) {
        Set<SpendType> spendTypes = new HashSet<>();
        FundingBlock fundingBlock = (FundingBlock) project.getSingleLatestBlockOfType(ProjectBlockType.Funding);
        if (fundingBlock != null) {
            Set<Claim> claims = fundingBlock.getClaims().stream().filter(c -> ClaimStatus.Claimed.equals(c.getClaimStatus()))
                    .collect(Collectors.toSet());

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
        ProjectStateMachine stateMachine = stateMachineForProject(project.getStateModel());

        ProjectState currentState = stateTransition.getFrom();
        ProjectState targetState = stateTransition.getTo();

        ProjectHistory.Transition historyTransition =
                stateTransition.getProjectHistoryTransition() != null ? stateTransition.getProjectHistoryTransition()
                        : stateMachine.getProjectHistoryTransition(currentState, targetState);
        if (historyTransition != null) {
            if (targetState.getStatusType().equals(Submitted)) {
                project.getHistory().removeIf(e -> ProjectHistory.Transition.Unconfirmed.equals(e.getTransition()));
            }
            String historyDescription = StringUtils.isNotEmpty(stateTransition.getProjectHistoryDescription()) ? stateTransition
                    .getProjectHistoryDescription() : stateMachine.getProjectHistoryDescription(project, historyTransition);
            createProjectHistoryEntry(project, historyTransition, historyDescription, comments);
        }

        if (Assess.equals(targetState.getStatusType())) {
            auditService.auditCurrentUserActivity(
                    String.format("Project with ID %d was moved to status of assessed.", project.getId()));
        }

        PaymentGroup paymentGroup = null; // for now we can only generate 1 payment group per state transition
        if (targetState.equals(Active, PaymentAuthorisationPending)) {
            String approvalRequestedBy = this.getFullNameOfLastUserToRequestApproval(project);
            for (ProjectPaymentGenerator projectPaymentGenerator : projectPaymentGenerators) {
                paymentGroup = projectPaymentGenerator.generatePaymentsForProject(project, approvalRequestedBy);
            }
        }

        if (StringUtils.isNotEmpty(stateTransition.getNotifcationKey())) {
            createStateTransitionNotification(NotificationType.valueOf(stateTransition.getNotifcationKey()), project,
                    currentState, targetState, paymentGroup);
        }

        if (targetState.equals(Closed, Rejected)) {
            auditService.auditCurrentUserActivity(
                    String.format("Project with ID %d was moved to status of %s", project.getId(), targetState.toString()));
        }

        for (NamedProjectBlock block : project.getProjectBlocks()) {
            block.handleStateTransition(stateTransition);
        }
    }

    void createStateTransitionNotification(NotificationType notificationType, Project project, ProjectState currentState,
            ProjectState targetState, PaymentGroup paymentGroup) {
        Map<String, Object> model = new HashMap<String, Object>() {{
            put("projectId", project.getId());
            put("organisation", project.getOrganisation());
            put("fromStatus", currentState.getStatus());
            put("toStatus", targetState.getStatus());
        }};

        if (paymentGroup != null) {
            notificationService.createNotification(notificationType, paymentGroup, model);
        } else {
            notificationService.createNotification(notificationType, project, model);
        }
    }

    void approveAllProjectBlocks(Project project) {
        String username = userService.currentUser().getUsername();
        OffsetDateTime now = environment.now();

        if (project.getStatusType().equals(ProjectStatus.Assess) || (project.getStatusType().equals(ProjectStatus.Draft)
                && !project.getStateModel().isApprovalRequired())) {
            project.setFirstApproved(now);
        }

        //approve milestone block first due to dependancy on Grant Source
        ProjectMilestonesBlock milestonesBlock = project.getMilestonesBlock();
        if (milestonesBlock != null) {
            if (milestonesBlock.isApproved()) {
                updateApprovedMilestonesBlockClaimedAmount(project, milestonesBlock);
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

    private void updateApprovedMilestonesBlockClaimedAmount(Project project, ProjectMilestonesBlock milestonesBlock) {
        milestonesBlock.updateClaimAmounts();
        List<ProjectLedgerEntry> payments = financeService.findAllByProjectIdAndLedgerType(project.getId(), LedgerType.PAYMENT);
        boolean monetaryValue = project.getTemplate().getMilestoneType().equals(Template.MilestoneType.MonetaryValue);

        if (payments.size() > 0) {

            for (Milestone milestone : milestonesBlock.getMilestones()) {
                if (milestone.hasMonetaryValue()) {
                    milestone.setClaimedGrant(0L);
                }
            }
        }

        for (ProjectLedgerEntry payment : payments) {
            if (LedgerStatus.getApprovedPaymentStatuses().contains(payment.getLedgerStatus())) {
                Milestone milestoneBySummary;
                String subCategory = monetaryValue
                        ? payment.getSubCategory().substring(BESPOKE_PREFIX.length()) : payment.getSubCategory();

                if (payment.isReclaim()) {
                    milestoneBySummary = milestonesBlock.getMilestoneBySummary(subCategory.substring(RECLAIMED_PREFIX.length()));
                } else {
                    milestoneBySummary = milestonesBlock.getMilestoneBySummary(subCategory);
                }
                if (milestoneBySummary != null && milestoneBySummary.hasMonetaryValue()) {
                    if (payment.isReclaim()) {
                        milestoneBySummary.setReclaimed(true);
                    }
                    Long claimedGrant = milestoneBySummary.getClaimedGrant();
                    milestoneBySummary.setClaimedGrant(claimedGrant + payment.getValue().negate().longValue());
                }
            }
        }
    }

    private void approveIndividualBlock(Project project, String username, OffsetDateTime now, NamedProjectBlock block) {
        if (block.isApproved() || block.isHidden()) {
            return;
        }

        if (!block.isNew() && !block.isComplete()) {
            // can't approve blocks if a non-new block is incomplete
            throw new ValidationException(
                    String.format("Unable to approve block '%s' as it is incomplete", block.getBlockDisplayName()));
        }

        if (block.isComplete()) {
            checkBlockMonetaryValueChanges(project, block, username);
            project.approveBlock(block, username, now);
        }

        //Hack to force the project to be Active(no subStatus) after approving
        if (block.isNew() && !block.isComplete()) {
            block.setLastModified(null);
        }
    }

    void checkBlockMonetaryValueChanges(Project project, NamedProjectBlock block, String username) {
        NamedProjectBlock latestApprovedBlock = project.getLatestApprovedBlock(block.getBlockType());
        if (block.hasMonetaryValueChanged(latestApprovedBlock)) {
            validateSpendThreshold(project, block, username);
            if (block.shouldRecordLastMonetaryApprover()) {
                recordLastMonetaryApproval(project, block, username);
            }
        }
    }

    void recordLastMonetaryApproval(Project project, NamedProjectBlock block, String username) {
        block.setLastMonetaryApprovalTime(environment.now());
        block.setLastMonetaryApprovalUser(username);
        this.updateProjectBlock(block, project.getId());
    }

    void validateSpendThreshold(Project project, NamedProjectBlock block, String username) {
        Long valueToBeCheckedAgainstFinanceThreshold = block.getValueToBeCheckedAgainstFinanceThresholdOnApproval();
        UserOrgFinanceThreshold financeThreshold = userFinanceThresholdService
                .getFinanceThreshold(username, project.getManagingOrganisationId());
        if (financeThreshold == null
                || financeThreshold.getApprovedThreshold() == null
                || valueToBeCheckedAgainstFinanceThreshold > financeThreshold.getApprovedThreshold()) {
            if (!Active.equals(project.getStatusType())) { // first approval
                throw new ValidationException(
                        "You cannot approve this project as the value of payment(s) are higher than your spend threshold");
            } else if (project.getGrantSourceAdjustmentAmount().intValue() != 0) {
                // subsequent approvals when there has been adjustments as we dont want to do this check for irrelevant changes
                throw new ValidationException(
                        "You cannot approve this project change as the value of payment(s) are higher than your spend threshold");
            }
        }
    }

    public NamedProjectBlock getBlockAndLock(Project project, NamedProjectBlock block) {
        return getBlockAndLock(project, block, true);
    }

    public NamedProjectBlock getBlockAndLock(Project project, NamedProjectBlock block, boolean lock) {
        StateTransition editStateTransition = stateMachineForProject(project.getStateModel())
                .getTransition(project.getProjectState(), StateTransitionType.EDIT);
        if (editStateTransition != null) {
            transitionProjectToStatus(project, editStateTransition.getTo(), null);
        }

        NamedProjectBlock blockToReturn = block;
        if (block.editRequiresCloning(environment.now())) {
            String userName =
                    userService.currentUser() == null ? userService.getSystemUserName() : userService.currentUser().getUsername();

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
            tryLock(blockToReturn);
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
            cloneListener.handleProjectClone(sourceBlock.getProject(), sourceBlock.getId(), clonedBlock.getProject(),
                    clonedBlock.getId());
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
    public LockRequestStatus tryLock(NamedProjectBlock block) {
        if (block.getLockDetails() != null) {
            return new LockRequestStatus(false, block.getLockDetails());
        } else {
            LockDetails lockDetails = lockProjectBlock(block);
            return new LockRequestStatus(true, lockDetails);
        }
    }

    LockDetails lockProjectBlock(NamedProjectBlock block) {
        User user = userService.currentUser();
        LockDetails ld = new LockDetails(user, lockTimeoutInMinutes);
        ld.setBlock(block);
        lockDetailsRepository.save(ld);

        block.setLockDetails(ld);
        projectBlockRepository.saveAndFlush(block);
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
        List<ProjectBlockHistoryItem> blockHistory = projectRepository
                .getProjectHistoryForProjectAndDisplayOrder(projectId, versionNumber);
        for (ProjectBlockHistoryItem projectBlockHistoryItem : blockHistory) {
            Set<uk.gov.london.ops.project.label.Label> labelsForBlock = labelService
                    .getLabelsForBlock(projectBlockHistoryItem.getBlockId());
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
            throw new ValidationException(String.format("This block is being edited by %s and cannot be deleted",
                    block.getLockDetails().getUsername()));
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

        List<NamedProjectBlock> allBlocksOfType = project
                .getBlocksByTypeAndDisplayOrder(block.getBlockType(), block.getDisplayOrder());

        // find latest block and set latest version flag.
        allBlocksOfType.stream().filter(b -> LAST_APPROVED.equals(b.getBlockStatus()))
                .forEach(b -> {
                    b.setLatestVersion(true);
                    project.getLatestProjectBlocks().add(b);
                });

        if (!project.hasUnapprovedBlocks()) {
            StateTransition revertStateTransition = stateMachineForProject(project.getStateModel())
                    .getTransition(project.getProjectState(), StateTransitionType.REVERT);
            if (revertStateTransition != null) {
                transitionProjectToStatus(project, revertStateTransition.getTo(), null);
            }
        }

        auditService.auditCurrentUserActivity(
                String.format("%s block unapproved version deleted from project %d", block.getBlockDisplayName(),
                        project.getId()));
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
        if (!featureStatus.isEnabled(Feature.AllowProjectCloning)) {
            throw new ForbiddenAccessException("This feature is currently disabled.");
        }

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

        List<EntitySubscription> subscriptions = notificationService
                .findAllByEntityTypeAndEntityId(EntityType.project, sourceProject.getId());
        for (EntitySubscription subscription : subscriptions) {
            notificationService.subscribe(subscription.getUsername(), EntityType.project, cloned.getId());
        }

        // TODO : this slows down the API and the integration tests, seems like it would be better done in a JUnit test
        // this will force loading of sorted blocks etc.
        //        Project enrichedCloned = getEnrichedProject(cloned.getId());
        //
        //        Set<ProjectBlockType> enriched = enrichedCloned.getLatestProjectBlocks().stream().map(NamedProjectBlock::getBlockType).collect(Collectors.toSet());
        //        Set<ProjectBlockType> original = sourceProject.getLatestProjectBlocks().stream().map(NamedProjectBlock::getBlockType).collect(Collectors.toSet());
        //        boolean matching = original.containsAll(enriched);
        //        log.warn("CLONED: " + sourceProject.getTitle() + " to " + enrichedCloned.getId());
        //        for (ProjectBlockType projectBlockType : enriched) {
        //            log.warn("CLONED Block: " + enrichedCloned.getId() + " " + projectBlockType);
        //
        //        }
        //        if (enrichedCloned.getLatestProjectBlocks().size() != sourceProject.getLatestProjectBlocks().size() || !matching) {
        //
        //            throw new ValidationException("Clone operation unsuccessful");
        //        }
        //        return enrichedCloned;

        cloned.setProjectBlocksSorted(filterSortedProjectBlocks(cloned, UNAPPROVED));
        this.insertProjectAccessControl(cloned.getProgrammeId(), cloned.getTemplateId(), cloned.getManagingOrganisationId(), AccessControlRelationshipType.MANAGING);


        return cloned;
    }

    public Project cloneProject(String existingProjectTitle, String clonedProjectTitle) {
        List<Project> projects = findAllByTitle(existingProjectTitle);
        if (projects == null || projects.size() != 1) {
            throw new ValidationException(String.format("0 or more than 1 project found with title %s", existingProjectTitle));
        }
        return cloneProject(projects.get(0).getId(), clonedProjectTitle);
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
        toProject.setApprovalWillGenerateReclaimPersisted(fromProject.getApprovalWillCreatePendingReclaim());
        toProject.setApprovalWillGeneratePaymentPersisted(fromProject.getApprovalWillCreatePendingPayment());

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
        namedProjectBlock.setHasUpdatesPersisted(block.getHasUpdatesPersisted());
        namedProjectBlock.setBlockMarkedComplete(block.getBlockMarkedComplete());
        namedProjectBlock.setNew(block.isNew());

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
            throw new NotFoundException(
                    String.format("Template with id %d is not present on programme %s.", templateId, newProgramme.getName()));
        }

        if (!orginalTemplate.getId().equals(newTemplate.getCloneOfTemplateId())) {
            throw new ValidationException(
                    String.format("Template with id %d is not a clone of the project's existing template: %d",
                            newTemplate.getId(), orginalTemplate.getId()));
        }

        if (newTemplate.isCloneModified()) {
            throw new ValidationException(
                    "The template being moved to has been modified post cloning, it's not possible to move to this template.");

        }

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

        List<NamedProjectBlock> blocksByType = project.getBlocksByType(ProjectBlockType.Milestones);
        for (NamedProjectBlock namedProjectBlock : blocksByType) {
            ProjectMilestonesBlock pmb = (ProjectMilestonesBlock) namedProjectBlock;
            if (pmb.getProcessingRouteId() != null) {
                Integer oldExternalId = oldMapIdToExternalId.get(pmb.getProcessingRouteId());
                Integer newProcessingRouteId = newMapExternalIdToId.get(oldExternalId);
                if (newProcessingRouteId == null) {
                    throw new ValidationException(
                            "Unable to move project as the processing routes for the milestones are not compatible ");
                }
                pmb.setProcessingRouteId(newProcessingRouteId);
            }
        }

        project.setProgramme(newProgramme);
        project.setTemplate(newTemplate);

        createProjectHistoryEntry(project, null,
                String.format("Transferred to %s from %s", newProgramme.getName(), orginalProgramme.getName()), "");

        return projectRepository.save(project);

    }

    public void updateProjectBlockLastModified(Integer projectId, Integer blockId, OffsetDateTime lastModified) {
        Project project = get(projectId);
        NamedProjectBlock block = project.getProjectBlockById(blockId);
        block.setLastModified(lastModified);
        projectRepository.save(project);
    }


    public ProjectRisksBlock createProjectRisk(Integer projectId, Integer blockId, ProjectRiskAndIssue risk,
            boolean releaseLock) {
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
                RiskLevelLookup one = riskLevelLookupRepository
                        .findById(new RiskLevelID(risk.getInitialImpactRating(), risk.getInitialProbabilityRating()))
                        .orElse(null);
                if (one == null) {
                    throw new ValidationException("Unable to find relevant initial Risk Level");
                }
                risk.setInitialRiskLevel(one);
            } else {
                throw new ValidationException("Initial Risk Ratings are mandatory");
            }
            if (risk.getResidualImpactRating() != null && risk.getResidualProbabilityRating() != null) {
                RiskLevelLookup one = riskLevelLookupRepository
                        .findById(new RiskLevelID(risk.getResidualImpactRating(), risk.getResidualProbabilityRating()))
                        .orElse(null);
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

    public ProjectRisksBlock addActionToRisk(Integer projectId, Integer blockId, Integer riskId, ProjectAction action,
            boolean releaseLock) {

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

    public void updateProjectRisk(Integer projectId, Integer blockId, Integer riskId, ProjectRiskAndIssue risk,
            boolean releaseLock) {
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

        auditService.auditCurrentUserActivity(
                String.format("deleted %s with ID %d and title %s", risk.getType(), risk.getId(), risk.getTitle()));

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
                auditService.auditCurrentUserActivity(
                        String.format("deleted action with ID %d and text %s", next.getId(), next.getAction()));
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
        ProjectBudgetsBlock projectBudgetsBlock = (ProjectBudgetsBlock) get(projectId)
                .getSingleLatestBlockOfType(ProjectBlockType.ProjectBudgets);
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

            if (!canProjectBeAssignedToTemplate(project.getTemplateId(), organisationId)) {
                String projectString = project.getTemplate().getNumberOfProjectAllowedPerOrg() > 1 ? "projects" : "project";
                String isAreString = project.getTemplate().getNumberOfProjectAllowedPerOrg() > 1 ? "are" : "is";
                throw new ValidationException(String.format("Only %d %s %s allowed for this project type.",
                        project.getTemplate().getNumberOfProjectAllowedPerOrg(), projectString, isAreString));
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

    private void transfer(Project project, Organisation fromOrganisation, Organisation toOrganisation) {
        String historyDescription = String
                .format("Transferred from %s to %s", fromOrganisation.getName(), toOrganisation.getName());
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

            transfer(project, fromOrganisation, toOrganisation);
            nbTransferred++;
        }

        return new ProjectsTransferResult(nbTransferred, nbErrors);
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
                    NamedProjectBlock newBlock = createBlockFromTemplate(project, project.getTemplate()
                            .getSingleBlockByTypeAndDisplayOrder(block.getBlockType(), block.getDisplayOrder()));
                    newBlock.setNew(block.isNew());
                    project.addBlockToProject(newBlock);
                    projectRepository.save(project);

                    return project
                            .getBlockByTypeDisplayOrderAndLatestVersion(newBlock.getBlockType(), newBlock.getDisplayOrder());
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
        return projectRepository.findAllProjectsWithScheduledPaymentDue(ProjectBlockType.LearningGrant.name(), asOfDate);
    }

    public void setRestricted(Integer projectId, Boolean restricted) {
        if (restricted) {
            projectBlockRepository.deleteRestrictedData(projectId);
            auditService.auditCurrentUserActivity(String.format("Restricted data for project %d was deleted", projectId));
        } else {
            projectBlockRepository.resetRestrictedFlag(projectId);
            auditService.auditCurrentUserActivity(String.format("Restricted flag for project %d was reset", projectId));
        }
    }

    public void shareProject(Integer projectId, Integer orgId, GrantAccessTrigger trigger) {
        Project project = get(projectId);
        Organisation organisation = organisationService.findOne(orgId);
        // TODO : might be useful to check if the org is already OWNER or MANAGING
        project.addToAccessControlList(organisation, AccessControlRelationshipType.ASSOCIATED, trigger);
        projectRepository.save(project);
    }

    public void unshareProject(Integer projectId, Integer orgId, GrantAccessTrigger trigger) {
        Project project = get(projectId);

        project.removeFromAccessControlList(orgId, trigger);
        projectRepository.save(project);
    }

    public void updateProjectTitleHistory(List<Integer> projectIds) {
        for (Integer projectId : projectIds) {
            updateProjectTitleHistory(projectId);
        }
    }

    public void updateProjectTitleHistory(Integer projectId) {
        Project project = get(projectId);
        ProjectDetailsBlock latestDetailsBlock = (ProjectDetailsBlock) project
                .getSingleLatestBlockOfType(ProjectBlockType.Details);
        List<NamedProjectBlock> projectDetailsBlocks = project.getBlocksByType(ProjectBlockType.Details);
        boolean hasChanges = false;
        for (NamedProjectBlock block : projectDetailsBlocks) {
            ProjectDetailsBlock detailsBlock = (ProjectDetailsBlock) block;
            if (!latestDetailsBlock.getTitle().equals(detailsBlock.getTitle())) {
                detailsBlock.setTitle(latestDetailsBlock.getTitle());
                hasChanges = true;
            }
        }

        if (hasChanges) {
            auditService.auditCurrentUserActivity(
                    String.format("Project P%d title history was updated to %s", projectId, latestDetailsBlock.getTitle()));
        }
    }

    public Integer[] getProjectIdsByProgrammeIdAndTemplateId(Integer programmeId, Integer templateId) {
        return projectRepository.findAllIdByProgrammeIdAndTemplateId(programmeId, templateId);
    }

    public void insertProjectAccessControl(Integer programmeId, Integer templateId, Integer organisationId, AccessControlRelationshipType type) {
        Integer[] projectIds = getProjectIdsByProgrammeIdAndTemplateId(programmeId, templateId);
        if (projectIds.length != 0) {
            GrantAccessTrigger trigger =  GrantAccessTrigger.TEMPLATE;
            projectAccessControlRepository
                    .insertProjectAccessControl(projectIds, organisationId, type.name(), trigger.name());
        }
    }

    public void deleteProjectAccessControl(Integer programmeId, Integer templateId, Integer organisationId) {
        Integer[] projectIds = getProjectIdsByProgrammeIdAndTemplateId(programmeId, templateId);
        if (projectIds.length != 0) {
            GrantAccessTrigger trigger = GrantAccessTrigger.TEMPLATE;
            projectAccessControlRepository
                        .deleteProjectAccessControl(projectIds, organisationId, trigger.name());

        }
    }

    public List<DefaultAccessControlSummary> getDefaultAccessByProgrammeId(Integer programmeId) {
        return defaultAccessControlSummaryRepository.findAllByProgrammeId(programmeId);
    }

    public void deleteAllMOProjectLevelAccessControl() {
        projectAccessControlRepository.deleteAllMOProjectLevelAccessControl();
    }
}
