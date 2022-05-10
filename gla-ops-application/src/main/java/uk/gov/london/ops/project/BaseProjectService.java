/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.project;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import uk.gov.london.common.error.ApiErrorItem;
import uk.gov.london.ops.audit.ActivityType;
import uk.gov.london.ops.audit.AuditService;
import uk.gov.london.ops.contracts.ContractModel;
import uk.gov.london.ops.contracts.ContractService;
import uk.gov.london.ops.framework.EntityType;
import uk.gov.london.ops.framework.environment.Environment;
import uk.gov.london.ops.framework.exception.ForbiddenAccessException;
import uk.gov.london.ops.framework.exception.NotFoundException;
import uk.gov.london.ops.framework.exception.ValidationException;
import uk.gov.london.ops.framework.feature.Feature;
import uk.gov.london.ops.framework.feature.FeatureStatus;
import uk.gov.london.ops.notification.NotificationService;
import uk.gov.london.ops.organisation.OrganisationGroupService;
import uk.gov.london.ops.organisation.OrganisationGroupType;
import uk.gov.london.ops.organisation.OrganisationProgrammeService;
import uk.gov.london.ops.organisation.model.OrganisationContract;
import uk.gov.london.ops.organisation.model.OrganisationEntity;
import uk.gov.london.ops.organisation.model.OrganisationGroup;
import uk.gov.london.ops.payment.PaymentService;
import uk.gov.london.ops.permission.PermissionServiceImpl;
import uk.gov.london.ops.programme.domain.Programme;
import uk.gov.london.ops.project.block.*;
import uk.gov.london.ops.project.grant.GrantSourceBlock;
import uk.gov.london.ops.project.implementation.repository.*;
import uk.gov.london.ops.project.internalblock.InternalBlockType;
import uk.gov.london.ops.project.internalblock.InternalProjectBlock;
import uk.gov.london.ops.project.internalblock.InternalProjectBlockOverview;
import uk.gov.london.ops.project.internalblock.InternalProjectBlockSummary;
import uk.gov.london.ops.project.label.Label;
import uk.gov.london.ops.project.label.LabelServiceImpl;
import uk.gov.london.ops.project.state.*;
import uk.gov.london.ops.project.template.domain.TemplateBlock;
import uk.gov.london.ops.refdata.RefDataService;
import uk.gov.london.ops.service.DataAccessControlService;
import uk.gov.london.ops.user.UserServiceImpl;
import uk.gov.london.ops.user.domain.UserEntity;

import javax.transaction.Transactional;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static uk.gov.london.ops.permission.PermissionType.*;
import static uk.gov.london.ops.project.block.ProjectBlockStatus.LAST_APPROVED;
import static uk.gov.london.ops.project.block.ProjectBlockStatus.UNAPPROVED;
import static uk.gov.london.ops.project.state.ProjectStatus.*;
import static uk.gov.london.ops.project.state.ProjectSubStatus.*;

@Transactional
public class BaseProjectService {

    Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    protected AuditService auditService;

    @Autowired
    DataAccessControlService dataAccessControlService;

    @Autowired
    NotificationService notificationService;

    @Autowired
    protected PaymentService paymentService;

    @Autowired
    PermissionServiceImpl permissionService;

    @Autowired
    protected UserServiceImpl userService;

    @Autowired
    ManualApprovalProjectStateMachine manualApprovalProjectStateMachine;

    @Autowired
    MultiAssessmentProjectStateMachine multiAssessmentProjectStateMachine;

    @Autowired
    AutoApprovalProjectStateMachine autoApprovalProjectStateMachine;

    @Autowired
    ProjectBlockActivityMap projectBlockActivityMap;

    @Autowired
    LockDetailsRepository lockDetailsRepository;

    @Autowired
    protected ProjectRepository projectRepository;

    @Autowired
    InternalProjectBlockSummaryRepository internalProjectBlockSummaryRepository;

    @Autowired
    ProjectBlockOverviewRepository projectBlockOverviewRepository;

    @Autowired
    ProjectOverviewRepository projectOverviewRepository;

    @Autowired
    ProjectStateRepository projectStateRepository;

    @Autowired
    protected Environment environment;

    @Value("${default.lock.timeout.minutes}")
    Integer lockTimeoutInMinutes = 60;

    @Value("${project.deletion.enabled}")
    Boolean projectDeletionEnabled = false;

    @Autowired
    Set<EnrichmentRequiredListener> enrichmentListeners;

    @Autowired
    OrganisationGroupService organisationGroupService;

    @Autowired
    OrganisationProgrammeService organisationProgrammeService;

    @Autowired
    private LabelServiceImpl labelService;

    @Autowired
    FeatureStatus featureStatus;

    @Autowired
    RefDataService refDataService;

    @Autowired
    ContractService contractService;

    public void setProjectRepository(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    public void setPaymentService(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    /**
     * Loads the project and populates all the transient data associated with it: permissions, messages, etc.
     *
     * @return the project corresponding to the given id.
     * @throws NotFoundException if no project found with the given id.
     */
    public Project getEnrichedProject(Integer id) {
        return getEnrichedProject(id, true, false, null, null, false);
    }

    /**
     * Loads the project and populates all the transient data associated with it: permissions, messages, etc.
     *
     * @param loadUsersFullNames if true it will set the approved by and modified by users full names.
     * @return the project corresponding to the given id.
     */
    public Project getEnrichedProject(Integer id, boolean unapprovedChanges, boolean loadUsersFullNames,
            ProjectBlockStatus compareToStatus, String compareToDate, boolean forComparison) {
        Project project = get(id);
        if (project == null) {
            throw new NotFoundException();
        }

        project.setEnriched(true);

        project.setProjectBlocksSorted(filterSortedProjectBlocks(project, unapprovedChanges ? UNAPPROVED : LAST_APPROVED));

        if (permissionService.currentUserHasPermission(PROJ_VIEW_INTERNAL_BLOCKS)) {
            project.setInternalBlocksSorted(filterSortedInternalBlocks(project));
        }

        if (loadUsersFullNames) {
            setUsersFullNames(project.getProjectBlocksSorted());
        }

        project.setPendingPayments(paymentService.hasPendingPayments(id));
        project.setReclaimedPayments(paymentService.hasReclaims(id));

        if (project.getGrantSourceBlock() != null) {
            enrichGrantSourceBlock(project);
        }

        for (EnrichmentRequiredListener enrichmentListener : enrichmentListeners) {
            enrichmentListener.enrichProject(project, forComparison);
        }

        populateTransientProjectPaymentProperties(project);

        project.setApprovalWillCreatePendingGrantPayment(this.getApprovalWillCreatePendingGrantPayment(project));

        setProjectMessages(project);

        calculateProjectPermissions(project, userService.currentUser());

        project.setCurrentUserWatching(notificationService.isUserSubscribed(
                userService.currentUser().getUsername(), EntityType.project, project.getId()));
        project.setNbWatchers(notificationService.countByEntityTypeAndEntityId(EntityType.project, project.getId()));

        if (compareToStatus != null || compareToDate != null) {
            if (unapprovedChanges) { // we are source being compared with previous version
                Project projectForComparison = getProjectForComparison(project, compareToStatus, compareToDate);
                compare(project, projectForComparison);
            } else { // we are a previous version needing enrichment
                updateProjectForSpecificState(project, compareToStatus, compareToDate);
            }

        }

        return project;
    }

    public BaseProject projectOverview(Integer id) {
        UserEntity user = userService.currentUser();

        ProjectOverview projectDetails = projectOverviewRepository.findByIdForUser(id, user.getUsername());
        if (projectDetails == null) {
            throw new NotFoundException("Unable to find project with id : " + id);
        }

        List<ProjectBlockOverview> allByProjectIdOrderByDisplayOrder = projectBlockOverviewRepository
                .findAllByProjectIdOrderByDisplayOrder(id);
        BaseProject project = populateProject(projectDetails);
        for (ProjectBlockOverview projectBlockOverview : allByProjectIdOrderByDisplayOrder) {
            if (!projectBlockOverview.isHidden()) {
                project.getProjectBlocksSorted().add(populateProjectBlock(projectBlockOverview));
            }
        }
        if (permissionService.currentUserHasPermission(PROJ_VIEW_INTERNAL_BLOCKS)) {
            Set<InternalProjectBlockSummary> internalBlockSummary = internalProjectBlockSummaryRepository
                    .findAllByProjectIdAndShow(project.getId(), true);
            for (InternalProjectBlockSummary projectBlockSummary : internalBlockSummary) {
                InternalProjectBlockOverview overview = new InternalProjectBlockOverview(projectBlockSummary);
                project.getInternalBlocks().add(overview);
            }
            project.setInternalBlocksSorted(project.getInternalBlocks().stream()
                    .sorted(Comparator.comparingInt(InternalProjectBlock::getDisplayOrder))
                    .collect(Collectors.toList()));
        }

        boolean projectBeenSubmittedPreviously = projectRepository.hasProjectBeenSubmittedPreviously(id);
        boolean projectBeenReturnedPreviously = projectRepository.hasProjectBeenReturnedPreviously(id);

        project.setPreviouslySubmitted(projectBeenSubmittedPreviously);
        project.setPreviouslyReturned(projectBeenReturnedPreviously);

        project.setPendingPayments(paymentService.hasPendingPayments(id));
        project.setReclaimedPayments(paymentService.hasReclaims(id));

        setProjectMessages(project);
        project.setCurrentUserWatching(notificationService.isUserSubscribed(
                userService.currentUser().getUsername(), EntityType.project, project.getId()));
        project.setNbWatchers(notificationService.countByEntityTypeAndEntityId(EntityType.project, project.getId()));

        calculateAllowedStateTransitions(project, user);
        calculateAllowedActions(project);

        Set<Label> projectLabels = labelService.getLabelsByProjectId(id);
        project.setLabels(projectLabels);

        return project;
    }

    NamedProjectBlock populateProjectBlock(ProjectBlockOverview projectBlockOverview) {
        SimpleProjectBlock spb = new SimpleProjectBlock();
        spb.setId(projectBlockOverview.getProjectBlockId());
        spb.setDisplayOrder(projectBlockOverview.getDisplayOrder());
        spb.setVersionNumber(projectBlockOverview.getVersionNumber());
        spb.setBlockAppearsOnStatus(projectBlockOverview.getBlockAppearsOnStatus());
        spb.setBlockMarkedComplete(projectBlockOverview.getBlockMarkedComplete());
        spb.setBlockDisplayName(projectBlockOverview.getBlockDisplayName());
        spb.setBlockType(projectBlockOverview.getBlockType());
        spb.setHidden(projectBlockOverview.isHidden());
        spb.setNew(projectBlockOverview.isNew());
        spb.setBlockStatus(projectBlockOverview.getBlockStatus());
        spb.setHasUpdatesPersisted(projectBlockOverview.getHasUpdatesPersisted());
        if (projectBlockOverview.getLockedBy() != null) {
            spb.setLockDetails(new LockDetails(new UserEntity(projectBlockOverview.getLockedBy()), 0));
        }
        return spb;

    }

    BaseProject populateProject(ProjectOverview projectOverview) {
        BaseProject project = new BaseProject();
        project.setOrganisation(projectOverview.getOrganisation());
        project.setManagingOrganisation(projectOverview.getManagingOrganisation());
        project.setId(projectOverview.getId());
        project.setTitle(projectOverview.getTitle());
        project.setProjectBlocksSorted(new ArrayList<>());
        project.setStatusName(projectOverview.getStatusName());
        project.setRecommendation(projectOverview.getRecommendation());
        project.setSubStatusName(projectOverview.getSubStatusName());
        project.setStateModel(projectOverview.getStateModel());
        project.setInfoMessage(projectOverview.getInfoMessage());
        project.setTemplateId(projectOverview.getTemplateId());
        project.setMarkedForCorporate(projectOverview.isMarkedForCorporate());
        project.setApprovalWillGenerateReclaimPersisted(projectOverview.getApprovalWillGenerateReclaimPersisted());
        project.setApprovalWillGeneratePaymentPersisted(projectOverview.getApprovalWillGeneratePaymentPersisted());
        project.setSuspendPayments(projectOverview.getSuspendPayments());

        Programme programme = new Programme();
        programme.setId(projectOverview.getProgrammeId());
        programme.setName(projectOverview.getProgrammeName());
        programme.setStatus(projectOverview.getProgrammeStatus());
        programme.setInAssessment(projectOverview.getInAssessment());
        programme.setEnabled(projectOverview.getEnabled());
        project.setProgramme(programme);

        return project;
    }

    void populateTransientProjectPaymentProperties(Project project) {
        OrganisationEntity organisationForPayment = project.getOrganisation();
        OrganisationGroupType orgGroupType = null;
        if (project.getOrganisationGroupId() != null) {
            OrganisationGroup organisationGroup = organisationGroupService.find(project.getOrganisationGroupId());
            organisationForPayment = organisationGroup.getLeadOrganisation();
            project.setLeadOrganisationId(organisationGroup.getLeadOrganisation().getId());
            orgGroupType = organisationGroup.getType();
        }
        //enrich organisation contracts for organisationForPayment
        List<OrganisationContract> contractEntities = organisationForPayment.getContractEntities();
        for (OrganisationContract organisationContract : contractEntities) {
            ContractModel contractModel = contractService.findById(organisationContract.getContractId());
            if (contractModel != null) {
                organisationContract.setContract(contractModel);
            }
        }

        ContractModel contractModel = null;
        if (project.getTemplate().getContractId() != null) {
            contractModel = contractService.find(project.getTemplate().getContractId());
        }
        project.setPendingContractSignature(contractModel != null
                && organisationForPayment.isPendingContractSignature(contractModel, orgGroupType));
        project.setSapVendorId(getProjectSapId(project, organisationForPayment));
    }

    public String getProjectSapId(Integer projectId, OrganisationEntity organisation) {
        return getVendorSapId(projectRepository.getSapIdFromProjectDetails(projectId),
                organisation.getDefaultSapVendorId());
    }

    private String getProjectSapId(Project project, OrganisationEntity organisation) {
        ProjectDetailsBlock detailsBlock = project.getDetailsBlock();
        return getVendorSapId(detailsBlock == null ? null : detailsBlock.getSapId(),
                organisation.getDefaultSapVendorId());
    }

    private String getVendorSapId(String projectSapVendorId, String orgSapVendorId) {
        return projectSapVendorId != null ? projectSapVendorId :  orgSapVendorId;
    }

    private Project getProjectForComparison(Project project, ProjectBlockStatus compareToStatus, String date) {
        Project projectToCompareTo = new Project(project.getId(), "");
        projectToCompareTo.setTemplate(project.getTemplate());
        projectToCompareTo.setOrganisation(project.getOrganisation());
        projectToCompareTo.getProjectBlocks().addAll(project.getProjectBlocks());
        List<NamedProjectBlock> projectBlocksSorted = new ArrayList<>();
        if (compareToStatus != null) {
            projectBlocksSorted = filterSortedProjectBlocks(projectToCompareTo, compareToStatus);
        } else if (date != null) {
            projectBlocksSorted = filterSortedProjectBlocks(projectToCompareTo, date);
        }
        projectToCompareTo.setProjectBlocksSorted(projectBlocksSorted);
        for (EnrichmentRequiredListener enrichmentListener : enrichmentListeners) {
            enrichmentListener.enrichProject(projectToCompareTo, true);
        }
        return projectToCompareTo;
    }

    private void updateProjectForSpecificState(Project project, ProjectBlockStatus compareToStatus, String date) {
        List<NamedProjectBlock> namedProjectBlocks = new ArrayList<>();
        if (compareToStatus != null) {
            namedProjectBlocks = filterSortedProjectBlocks(project, compareToStatus);
        } else if (date != null) {
            namedProjectBlocks = filterSortedProjectBlocks(project, date);
        }
        project.getProjectBlocksSorted().clear();
        project.getProjectBlocksSorted().addAll(namedProjectBlocks);
        for (EnrichmentRequiredListener enrichmentListener : enrichmentListeners) {
            enrichmentListener.enrichProject(project, true);
        }
    }

    List<NamedProjectBlock> filterSortedProjectBlocks(Project project, String priorToDateString) {
        OffsetDateTime priorToDate = OffsetDateTime.parse(priorToDateString + "T00:00:00+00:00");
        if (project.getProjectBlocks() == null) {
            return Collections.emptyList();
        }

        List<NamedProjectBlock> sortedProjectBlocks = new ArrayList<>();

        List<TemplateBlock> blocksEnabled = project.getTemplate().getBlocksEnabled();

        Set<NamedProjectBlock> projectBlocks = new HashSet<>();
        projectBlocks.addAll(project.getProjectBlocks());
        projectBlocks.removeIf(pb -> pb.getApprovalTime() != null && pb.getApprovalTime().isBefore(priorToDate));

        for (TemplateBlock templateBlock : blocksEnabled) {
            Optional<NamedProjectBlock> first = projectBlocks.stream()
                    .filter(pb -> pb.getBlockType().equals(templateBlock.getBlock()))
                    .sorted(Comparator.comparingInt(NamedProjectBlock::getVersionNumber))
                    .findFirst();
            if (first.isPresent()) {
                sortedProjectBlocks.add(first.get());
            } else {
                sortedProjectBlocks.add(
                        project.getBlockByTypeDisplayOrderAndLatestVersion(templateBlock.getBlock(),
                                templateBlock.getDisplayOrder()));
            }
        }
        return sortedProjectBlocks.stream()
                .sorted(Comparator.comparingInt(NamedProjectBlock::getDisplayOrder)).collect(Collectors.toList());
    }

    List<NamedProjectBlock> filterSortedProjectBlocks(Project project, ProjectBlockStatus blockStatus) {
        if (project.getProjectBlocks() == null) {
            return Collections.emptyList();
        }

        Collection<NamedProjectBlock> applicableBlocks = UNAPPROVED.equals(blockStatus)
                ? project.getLatestProjectBlocks() : project.getLatestApprovedBlocks();
        applicableBlocks = applicableBlocks.stream().filter(block -> !block.isHidden()).collect(Collectors.toList());

        return applicableBlocks.stream().sorted().collect(Collectors.toList());
    }

    List<InternalProjectBlock> filterSortedInternalBlocks(Project project) {
        List<InternalProjectBlock> internalProjectBlocks = new ArrayList<>();
        for (InternalProjectBlock block : project.getInternalBlocks()) {
            if (!InternalBlockType.Assessment.equals(block.getType())
                    || !project.getProgrammeTemplate().getAssessmentTemplates().isEmpty()) {
                internalProjectBlocks.add(block);
            }
        }
        return internalProjectBlocks.stream().sorted().collect(Collectors.toList());
    }

    void setUsersFullNames(List<NamedProjectBlock> blocks) {
        for (NamedProjectBlock block : blocks) {
            block.setModifiedByName(userService.getUserFullName(block.getModifiedBy()));
            block.setApprovedByName(userService.getUserFullName(block.getApproverUsername()));
        }
    }

    void compare(Project project, Project projectToCompareTo) {
        for (NamedProjectBlock block : project.getProjectBlocksSorted()) {
            NamedProjectBlock other = findEquivalentBlock(projectToCompareTo, block);
            block.setDifferences(block.compareContent(other));
        }
    }

    private NamedProjectBlock findEquivalentBlock(Project project, NamedProjectBlock block) {
        return project.getProjectBlocksSorted().stream()
                .filter(b -> b.getBlockType().equals(block.getBlockType()) && b.getDisplayOrder().equals(block.getDisplayOrder()))
                .findFirst().orElse(null);
    }

    /**
     * Interface to the project repository find project method.
     *
     * @return the project corresponding to the given id or null if not found.
     */
    public Project get(Integer id) {
        if (environment.printGetProjectStackTrace()) {
            new Exception().printStackTrace();
        }
        Project project = projectRepository.findById(id).orElse(null);
        if (project != null && featureStatus != null) {
            project.setReclaimEnabled(featureStatus.isEnabled(Feature.Reclaims));
        }
        return project;
    }

    /**
     * Finds block by id and casts to required type. If block is null or incorrect type then throws ValidationException
     */
    public <T> T getBlock(Project project, Integer blockId, Class<T> type) {
        NamedProjectBlock projectBlockById = project.getProjectBlockById(blockId);

        if (projectBlockById == null || !type.isAssignableFrom(projectBlockById.getClass())) {
            throw new ValidationException("Incorrect block specified");
        }
        return type.cast(projectBlockById);
    }

    public void checkForLock(NamedProjectBlock block) {
        UserEntity currentUser = userService.currentUser();

        if (!UNAPPROVED.equals(block.getBlockStatus())) {
            log.info("Attempt made to modify a historic block");
            throw new ForbiddenAccessException("Unable to edit historic blocks.");
        } else if (block.getLockDetails() == null || !currentUser.getUsername().equals(block.getLockDetails().getUsername())) {
            log.info(String.format("Attempt made to modify an block without owning the lock, user %s lock owner %s ",
                    currentUser.getUsername(),
                    block.getLockDetails() == null ? "no lock" : block.getLockDetails().getUsername()));
            throw new ForbiddenAccessException("User is not the owner of the lock, unable to allow edit.");
        } else if (block.getLockDetails().getLockTimeoutTime().isBefore(OffsetDateTime.now())) {
            log.info("Attempt made to modify a  block with an expired lock");
            throw new ForbiddenAccessException("Lock has timed out, please reacquire the lock.  ");
        }

        block.setLastModified(environment.now());
        block.setModifiedBy(currentUser.getUsername());
    }

    public void deleteLock(NamedProjectBlock blockToCheck) {
        this.releaseOrRefreshLock(blockToCheck, true);
    }

    protected void releaseOrRefreshLock(NamedProjectBlock blockToCheck, boolean releaseLock) {
        checkForLock(blockToCheck);
        if (releaseLock) {
            lockDetailsRepository.deleteById(blockToCheck.getId());
            blockToCheck.setLockDetails(null);
            auditService.auditCurrentUserActivity(EntityType.projectBlock, blockToCheck.getId(), ActivityType.StopEdit);

        } else {
            // refresh lock timeout
            blockToCheck.getLockDetails().setLockTimeoutTime(OffsetDateTime.now().plusMinutes(lockTimeoutInMinutes));
        }
    }

    protected void ensureRPCanModifyProject(Project project) {
        if (!(ProjectStatus.Draft.equals(project.getStatusType())
                || ProjectStatus.Returned.equals(project.getStatusType())
                || Active.equals(project.getStatusType()))) {
            throw new ValidationException("Unable to update a project that is not in Draft/Returned status.");
        }
    }

    public Project updateProject(Project project) {
        if (!userService.currentUser().isGla()) {
            ensureRPCanModifyProject(project);
        }
        return performProjectUpdate(project);
    }

    protected Project performProjectUpdate(Project project) {
        if (project.getId() == null) {
            throw new ValidationException("Projects must have an ID for update");
        }

        dataAccessControlService.checkAccess(project);

        project.setLastModified(environment.now());

        String user = userService.currentUser().getUsername();
        long time = System.nanoTime();
        Project updated = projectRepository.save(project);
        log.trace("({}ms) performProjectUpdate end: projectID: {} user: {}",
                System.nanoTime() - time, project.getIndicativeGrantBlock(), user);

        return updated;
    }

    void setProjectMessages(BaseProject project) {
        if (Draft.equals(project.getStatusType())
                && !project.isPreviouslySubmitted()
                && !project.getProgramme().isEnabled()) {
            project.getMessages().add(new ApiErrorItem("",
                    "The programme is now closed for bidding and projects cannot be submitted"));
        }

        if (Draft.equals(project.getStatusType()) && project.isPreviouslyReturned()) {
            project.getMessages().add(new ApiErrorItem("",
                    "Project has been returned for further information. Update the project and resubmit."));
        }

        if (project.getProgramme() != null
                && project.getProgramme().isInAssessment()
                && project.getStatusType().equals(Submitted)
                && Objects.equals(project.getStateModel(), StateModel.MultiAssessment)) {
            project.getMessages().add(new ApiErrorItem("", "Project is in assessment and can't be withdrawn."));
        }

        if (Submitted.equals(project.getStatusType())
                && project.getProgramme().isEnabled()
                && (!project.getProgramme().isInAssessment()
                || project.getProgramme().isInAssessment()
                && !Objects.equals(project.getStateModel(), StateModel.MultiAssessment))) {
            project.getMessages().add(new ApiErrorItem("",
                    "This project is submitted and must be withdrawn before being edited or abandoned"));
        }

        if (ApprovalRequested.equals(project.getSubStatusType()) || PaymentAuthorisationPending
                .equals(project.getSubStatusType())) {
            project.getMessages().add(new ApiErrorItem("",
                    "The project is awaiting approval and cannot be edited at this stage"));
        }

        if (AbandonPending.equals(project.getSubStatusType())) {
            project.getMessages().add(new ApiErrorItem("",
                    "This project has a pending abandon request and cannot be updated"));
        }

        if (Closed.equals(project.getStatusType()) && project.hasReclaimedPayments()) {
            project.getMessages().add(new ApiErrorItem("",
                    "This project contains authorised or pending reclaim(s)"));
        }
    }

    private void calculateProjectPermissions(Project project, UserEntity user) {
        dataAccessControlService.checkAccess(user, project);

        for (NamedProjectBlock block : project.getProjectBlocks()) {
            block.setAllowedActions(projectBlockActivityMap.getAllowedActionsFor(project, block, user));
        }

        calculateAllowedStateTransitions(project, user);
        calculateAllowedActions(project);
    }

    private void calculateAllowedStateTransitions(BaseProject project, UserEntity user) {
        Boolean willGeneratePayment = project.getApprovalWillGeneratePaymentPersisted();
        Boolean approvalWillGenerateReclaim = project.getApprovalWillGenerateReclaimPersisted();

        if (project instanceof Project) {
            if (willGeneratePayment == null) {
                willGeneratePayment = project.getApprovalWillCreatePendingPayment();
            }
            if (approvalWillGenerateReclaim == null) {
                approvalWillGenerateReclaim = project.getApprovalWillCreatePendingReclaim();
            }
        }

        if (willGeneratePayment == null || approvalWillGenerateReclaim == null) {
            project.setAbleToCalculateTransitions(false);
            return;
        }
        project.setAbleToCalculateTransitions(true);
        Set<String> roles = getUserRolesForProject(user, project);
        Set<ProjectState> states = stateMachineForProject(project.getStateModel()).getAllowedTransitionsFor(
                project.currentState(),
                roles,
                project.getProgramme().isEnabled(),
                project.getProgramme().isInAssessment(),
                project.isPreviouslySubmitted(),
                project.isComplete(),
                willGeneratePayment || approvalWillGenerateReclaim);
        project.setAllowedTransitions(states);
    }

    void calculateAllowedActions(BaseProject project) {
        List<Project.Action> allowedActions = new ArrayList<>();

        // order of summary report and change report is directly used by UI and should remain summary first then change
        if (permissionService.currentUserHasPermissionForOrganisation(PROJ_SUMMARY_REPORT, project.getOrganisation().getId())) {
            allowedActions.add(Project.Action.ViewSummaryReport);
        }

        if (permissionService.currentUserHasPermissionForOrganisation(PROJ_CHANGE_REPORT, project.getOrganisation().getId())) {
            allowedActions.add(Project.Action.ViewChangeReport);
        }

        if (permissionService.currentUserHasPermissionForOrganisation(PROJ_TRANSFER, project.getOrganisation().getId())
                && !ProjectSubStatus.UnapprovedChanges.equals(project.getSubStatusType())
                && !ApprovalRequested.equals(project.getSubStatusType())
                && !PaymentAuthorisationPending.equals(project.getSubStatusType())
                && !project.anyBlocksLocked()) {
            allowedActions.add(Project.Action.Transfer);
        }

        if (permissionService.currentUserHasPermissionForOrganisation(PROJ_REINSTATE, project.getOrganisation().getId())) {
            allowedActions.add(Project.Action.Reinstate);
        }

        if (featureStatus.isEnabled(Feature.ProjectSharing) && permissionService.currentUserHasPermission(PROJ_SHARE)) {
            allowedActions.add(Project.Action.Share);
        }

        if (projectDeletionEnabled && permissionService.currentUserHasPermissionForOrganisation(PROJ_DELETE,
                project.getOrganisation().getId())) {
            allowedActions.add(Project.Action.Delete);
        }

        project.setAllowedActions(allowedActions);
    }

    /**
     * Returns the names of the roles that the current user has with a project.
     */
    Set<String> getUserRolesForProject(Project project) {
        return getUserRolesForProject(userService.currentUser(), project);
    }

    /**
     * Returns the names of the roles that a user has with a project.
     */
    Set<String> getUserRolesForProject(UserEntity user, BaseProject project) {
        Set<String> roles = user.getApprovedRolesForOrgs(project.getOrganisation(), project.getManagingOrganisation());
        List<String> rolesForProject = projectRepository.getUserRolesForProject(user.getUsername(), project.getId());
        roles.addAll(rolesForProject);
        return roles;
    }

    ProjectStateMachine stateMachineForProject(StateModel stateModel) {
        if (StateModel.AutoApproval.equals(stateModel)) {
            return autoApprovalProjectStateMachine;
        } else if (StateModel.MultiAssessment.equals(stateModel)) {
            return multiAssessmentProjectStateMachine;
        } else {
            return manualApprovalProjectStateMachine;
        }
    }


    public Project getByLegacyProjectCode(Integer legacyCode) {
        return projectRepository.findFirstByLegacyProjectCode(legacyCode);
    }

    public Set<Integer> findAllProjectIdsByWBSCode(String wbsCode) {
        return projectRepository.findAllProjectIdsByWBSCode(wbsCode);
    }

    protected boolean isWbsCodeUsedInProjectsOtherThan(String wbsCode, Integer projectId) {
        Set<Integer> projectIds = findAllProjectIdsByWBSCode(wbsCode);
        projectIds.remove(projectId);
        return projectIds.size() > 0;
    }

    protected void enrichGrantSourceBlock(Project project) {
        GrantSourceBlock grantSourceBlock = project.getGrantSourceBlock();

        grantSourceBlock.setAssociatedProjectFlagUpdatable(
                project.isAssociatedProjectsEnabled()
                        && !project.getMilestonesBlock().hasClaimedMilestones()
                        && organisationProgrammeService.isStrategic(project.getOrganisation().getId(), project.getProgrammeId())
                        && !paymentService.hasPayments(project.getId())
        );
    }

    protected boolean getApprovalWillCreatePendingGrantPayment(Project project) {

        return project.getApprovalPaymentSources().stream()
                .anyMatch(s -> refDataService.getPaymentSourceMap().get(s).shouldPaymentSourceBeSentToSAP());
    }


}
