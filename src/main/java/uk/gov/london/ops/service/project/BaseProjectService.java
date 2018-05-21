/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.service.project;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import uk.gov.london.ops.Environment;
import uk.gov.london.ops.FeatureStatus;
import uk.gov.london.ops.domain.EntityType;
import uk.gov.london.ops.domain.organisation.OrganisationGroup;
import uk.gov.london.ops.domain.project.NamedProjectBlock;
import uk.gov.london.ops.domain.project.Project;
import uk.gov.london.ops.domain.template.Contract;
import uk.gov.london.ops.domain.template.TemplateBlock;
import uk.gov.london.ops.domain.user.User;
import uk.gov.london.ops.exception.ApiErrorItem;
import uk.gov.london.ops.exception.ForbiddenAccessException;
import uk.gov.london.ops.exception.NotFoundException;
import uk.gov.london.ops.exception.ValidationException;
import uk.gov.london.ops.repository.EntitySubscriptionRepository;
import uk.gov.london.ops.repository.LockDetailsRepository;
import uk.gov.london.ops.repository.ProjectLedgerRepository;
import uk.gov.london.ops.repository.ProjectRepository;
import uk.gov.london.ops.service.*;
import uk.gov.london.ops.service.finance.PaymentService;
import uk.gov.london.ops.service.project.block.ProjectBlockActivityMap;
import uk.gov.london.ops.service.project.state.*;

import javax.transaction.Transactional;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static uk.gov.london.ops.domain.project.NamedProjectBlock.BlockStatus.LAST_APPROVED;
import static uk.gov.london.ops.domain.project.NamedProjectBlock.BlockStatus.UNAPPROVED;
import static uk.gov.london.ops.domain.project.Project.Status.Active;
import static uk.gov.london.ops.domain.project.Project.Status.Closed;
import static uk.gov.london.ops.domain.project.Project.SubStatus.*;
import static uk.gov.london.ops.service.PermissionService.*;

@Transactional
public class BaseProjectService {

    Logger log = LoggerFactory.getLogger(getClass());


    @Autowired
    AuditService auditService;

    @Autowired
    DataAccessControlService dataAccessControlService;

    @Autowired
    PaymentService paymentService;

    @Autowired
    PermissionService permissionService;

    @Autowired
    UserService userService;

    @Autowired
    ManualApprovalProjectStateMachine manualApprovalProjectStateMachine;

    @Autowired
    AutoApprovalProjectStateMachine autoApprovalProjectStateMachine;

    @Autowired
    ProjectBlockActivityMap projectBlockActivityMap;

    @Autowired
    EntitySubscriptionRepository entitySubscriptionRepository;

    @Autowired
    LockDetailsRepository lockDetailsRepository;

    @Autowired
    ProjectRepository projectRepository;

    @Autowired
    ProjectLedgerRepository projectLedgerRepository;

    @Autowired
    Environment environment;

    @Value("${default.lock.timeout.minutes}")
    Integer lockTimeoutInMinutes = 60;

    @Autowired
    Set<EnrichmentRequiredListener> enrichmentListeners;

    @Autowired
    OrganisationGroupService organisationGroupService;

    @Autowired
    FeatureStatus featureStatus;

    /**
     * Loads the project and populates all the transient data associated with it: permissions, messages, etc.
     * @id
     * @return the project corresponding to the given id.
     * @throws NotFoundException if no project found with the given id.
     */
    public Project getEnrichedProject(Integer id) {
        return getEnrichedProject(id, true, false, null, null, false);
    }

    /**
     * Loads the project and populates all the transient data associated with it: permissions, messages, etc.
     * @param loadUsersFullNames if true it will set the approved by and modified by users full names.
     * @return the project corresponding to the given id.
     */
    public Project getEnrichedProject(Integer id, boolean unapprovedChanges, boolean loadUsersFullNames, NamedProjectBlock.BlockStatus compareToStatus, String compareToDate, boolean forComparison) {
        Project project = get(id);
        if (project == null) {
            throw new NotFoundException();
        }

        project.setProjectBlocksSorted(filterSortedProjectBlocks(project, unapprovedChanges ? UNAPPROVED : LAST_APPROVED));
        if (loadUsersFullNames) {
            setUsersFullNames(project.getProjectBlocksSorted());
        }

        project.setPendingPayments(paymentService.hasPendingPayments(id));
        project.setReclaimedPayments(paymentService.hasReclaims(id));

        setPendingContractSignatureFlag(project);

        setProjectMessages(project);

        calculateProjectPermissions(project, userService.currentUser());

        project.setCurrentUserWatching(entitySubscriptionRepository.findFirstByUsernameAndEntityTypeAndEntityId(
                userService.currentUser().getUsername(), EntityType.project, project.getId()) != null);
        project.setNbWatchers(entitySubscriptionRepository.countByEntityTypeAndEntityId(EntityType.project, project.getId()));

        for (EnrichmentRequiredListener enrichmentListener : enrichmentListeners) {
            enrichmentListener.enrichProject(project, forComparison);
        }

        if (compareToStatus != null || compareToDate !=null) {
            if (unapprovedChanges) { // we are source being compared with previous version
                Project projectForComparison = getProjectForComparison(project, compareToStatus, compareToDate);
                compare(project, projectForComparison);
            } else { // we are a previous version needing enrichment
                updateProjectForSpecificState(project, compareToStatus, compareToDate);
            }

        }

        return project;
    }

    void setPendingContractSignatureFlag(Project project) {
        Contract contract = project.getTemplate().getContract();
        OrganisationGroup.Type orgGroupType = null;
        if (project.getOrganisationGroupId() != null) {
            OrganisationGroup organisationGroup = organisationGroupService.find(project.getOrganisationGroupId());
            orgGroupType = organisationGroup.getType();
        }
        project.setPendingContractSignature(contract != null && project.getOrganisation().isPendingContractSignature(contract, orgGroupType));
    }

    private Project getProjectForComparison(Project project, NamedProjectBlock.BlockStatus compareToStatus, String date) {
        Project projectToCompareTo = new Project(project.getId(),"");
        projectToCompareTo.setTemplate(project.getTemplate());
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

    private Project updateProjectForSpecificState(Project project, NamedProjectBlock.BlockStatus compareToStatus, String date) {
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
        return project;
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
                sortedProjectBlocks.add(project.getBlockByTypeDisplayOrderAndLatestVersion(templateBlock.getBlock(), templateBlock.getDisplayOrder()));
            }
        }
        return sortedProjectBlocks.stream().sorted(Comparator.comparingInt(NamedProjectBlock::getDisplayOrder)).collect(Collectors.toList());
    }

    List<NamedProjectBlock> filterSortedProjectBlocks(Project project, NamedProjectBlock.BlockStatus blockStatus) {
        if (project.getProjectBlocks() == null) {
            return Collections.emptyList();
        }

        Collection<NamedProjectBlock> applicableBlocks = UNAPPROVED.equals(blockStatus) ? project.getLatestProjectBlocks() : project.getLatestApprovedBlocks();
        return applicableBlocks.stream().sorted().collect(Collectors.toList());
    }

    void setUsersFullNames(List<NamedProjectBlock> blocks) {
        for (NamedProjectBlock block: blocks) {
            block.setModifiedByName(userService.getUserFullName(block.getModifiedBy()));
            block.setApprovedByName(userService.getUserFullName(block.getApproverUsername()));
        }
    }

    void compare(Project project, Project projectToCompareTo) {
        for (NamedProjectBlock block: project.getProjectBlocksSorted()) {
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
     * @param id
     * @return the project corresponding to the given id or null if not found.
     */
    public Project get(Integer id) {
        Project project = projectRepository.findOne(id);
        if (project != null && featureStatus != null) {
            project.setReclaimEnabled(featureStatus.isEnabled(FeatureStatus.Feature.Reclaims));
        }
        return project;
    }


    public List<Project> get(List<Integer> idList) {
        return projectRepository.findAll(idList);
    }


    public void checkForLock(NamedProjectBlock block) {
        User currentUser = userService.currentUser();

        if (!UNAPPROVED.equals(block.getBlockStatus())) {
            log.info("Attempt made to modify a historic block");
            throw new ForbiddenAccessException("Unable to edit historic blocks.");
        }
        else if (block.getLockDetails() == null || !currentUser.getUsername().equals(block.getLockDetails().getUsername())) {
            log.info(String.format("Attempt made to modify an block without owning the lock, user %s lock owner %s ",
                    currentUser.getUsername(), block.getLockDetails() == null ? "no lock" : block.getLockDetails().getUsername()));
            throw new ForbiddenAccessException("User is not the owner of the lock, unable to allow edit.");
        }
        else if (block.getLockDetails().getLockTimeoutTime().isBefore(OffsetDateTime.now())) {
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
            lockDetailsRepository.delete(blockToCheck.getId());
            blockToCheck.setLockDetails(null);

        } else {
            // refresh lock timeout
            blockToCheck.getLockDetails().setLockTimeoutTime(OffsetDateTime.now().plusMinutes(lockTimeoutInMinutes));
        }
    }

    protected void ensureRPCanModifyProject(Project project) {
        if (!(Project.Status.Draft.equals(project.getStatus()) || Project.Status.Returned.equals(project.getStatus()) || Active.equals(project.getStatus()))) {
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

        if (!isProjectEditable(project)) {
            throw new ValidationException("project not editable!");
        }

        dataAccessControlService.checkAccess(project);

        project.setLastModified(environment.now());

        return projectRepository.save(project);
    }

    void setProjectMessages(Project project) {
        if (ApprovalRequested.equals(project.getSubStatus()) || PaymentAuthorisationPending.equals(project.getSubStatus())) {
            project.getMessages().add(new ApiErrorItem("", "The project is awaiting approval and cannot be edited at this stage"));
        }

        if (AbandonPending.equals(project.getSubStatus())) {
            project.getMessages().add(new ApiErrorItem("", "This project has a pending abandon request and cannot be updated"));
        }

        if (Closed.equals(project.getStatus()) && project.hasReclaimedPayments()) {
            project.getMessages().add(new ApiErrorItem("", "This project contains authorised or pending reclaim(s)"));
        }
    }

    private void calculateProjectPermissions(Project project, User user) {
        dataAccessControlService.checkAccess(user, project);

        for (NamedProjectBlock block : project.getProjectBlocks()) {
            block.setAllowedActions(projectBlockActivityMap.getAllowedActionsFor(project, block, user));
        }

        calculateAllowedStateTransitions(project, user);
        calculateAllowedActions(project);
    }

    private void calculateAllowedStateTransitions(Project project, User user) {

        Set<String> roles = getUserRolesForProject(user, project);
        Set<ProjectState> states = stateMachineForProject(project).getAllowedTransitionsFor(
                project.currentState(),
                roles,
                project.getProgramme().isEnabled(),
                project.isComplete(),
                project.getApprovalWillCreatePendingPayment() || project.getApprovalWillCreatePendingReclaim());
        project.setAllowedTransitions(states);
    }

    void calculateAllowedActions(Project project) {
        List<Project.Action> allowedActions = new ArrayList<>();

        if (permissionService.currentUserHasPermissionForOrganisation(PROJ_CHANGE_REPORT, project.getOrganisation().getId())) {
            allowedActions.add(Project.Action.ViewChangeReport);
        }

        if (permissionService.currentUserHasPermissionForOrganisation(PROJ_TRANSFER, project.getOrganisation().getId())
                && !Project.SubStatus.UnapprovedChanges.equals(project.getSubStatus())
                && !Project.SubStatus.ApprovalRequested.equals(project.getSubStatus())
                && !Project.SubStatus.PaymentAuthorisationPending.equals(project.getSubStatus())
                && !project.anyBlocksLocked()) {
            allowedActions.add(Project.Action.Transfer);
        }
        if (permissionService.currentUserHasPermissionForOrganisation(PROJ_REINSTATE, project.getOrganisation().getId())) {
            allowedActions.add(Project.Action.Reinstate);
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
    Set<String> getUserRolesForProject(User user, Project project) {
        return user.getApprovedRolesForOrgs(project.getOrganisation(), project.getManagingOrganisation());
    }

    private boolean isProjectEditable(Project project) {
        return !(Project.Status.Draft.equals(project.getStatus()) && !project.getProgramme().isEnabled());
    }

    ProjectStateMachine stateMachineForProject(Project project) {
        if (StateMachine.AUTO_APPROVAL.equals(project.getStateMachine())) {
            return autoApprovalProjectStateMachine;
        } else {
            return manualApprovalProjectStateMachine;
        }
    }


    public Project getByLegacyProjectCode(Integer legacyCode) {
        return projectRepository.findFirstByLegacyProjectCode(legacyCode);
    }
}
