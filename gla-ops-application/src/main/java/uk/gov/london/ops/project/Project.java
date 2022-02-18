/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.project;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.StringUtils;
import uk.gov.london.common.GlaUtils;
import uk.gov.london.common.error.ApiErrorItem;
import uk.gov.london.ops.EventType;
import uk.gov.london.ops.OpsEvent;
import uk.gov.london.ops.framework.enums.GrantType;
import uk.gov.london.ops.framework.exception.ValidationException;
import uk.gov.london.ops.framework.jpa.Join;
import uk.gov.london.ops.framework.jpa.JoinData;
import uk.gov.london.ops.notification.NotificationTargetEntity;
import uk.gov.london.ops.organisation.model.OrganisationEntity;
import uk.gov.london.ops.organisation.model.OrganisationGroup;
import uk.gov.london.ops.programme.domain.ProgrammeTemplate;
import uk.gov.london.ops.project.accesscontrol.AccessControlRelationshipType;
import uk.gov.london.ops.project.accesscontrol.GrantAccessTrigger;
import uk.gov.london.ops.project.accesscontrol.ProjectAccessControl;
import uk.gov.london.ops.project.block.*;
import uk.gov.london.ops.project.budget.ProjectBudgetsBlock;
import uk.gov.london.ops.project.claim.ClaimStatus;
import uk.gov.london.ops.project.funding.FundingBlock;
import uk.gov.london.ops.project.grant.*;
import uk.gov.london.ops.project.internalblock.InternalAssessmentBlock;
import uk.gov.london.ops.project.internalblock.InternalBlockType;
import uk.gov.london.ops.project.internalblock.InternalProjectAdminBlock;
import uk.gov.london.ops.project.internalblock.InternalProjectBlock;
import uk.gov.london.ops.project.label.Label;
import uk.gov.london.ops.project.label.LabelType;
import uk.gov.london.ops.project.milestone.ProjectMilestonesBlock;
import uk.gov.london.ops.project.outputs.OutputsCostsBlock;
import uk.gov.london.ops.project.question.ProjectQuestionsBlock;
import uk.gov.london.ops.project.receipt.ReceiptsBlock;
import uk.gov.london.ops.project.repeatingentity.OtherFundingBlock;
import uk.gov.london.ops.project.risk.ProjectRisksBlock;
import uk.gov.london.ops.project.skills.FundingClaimsBlock;
import uk.gov.london.ops.project.skills.LearningGrantBlock;
import uk.gov.london.ops.project.state.ProjectState;
import uk.gov.london.ops.project.state.ProjectStateEntity;
import uk.gov.london.ops.project.state.ProjectStatus;
import uk.gov.london.ops.project.state.StateModel;
import uk.gov.london.ops.project.template.domain.LearningGrantTemplateBlock;
import uk.gov.london.ops.project.template.domain.Template;
import uk.gov.london.ops.project.unit.UnitDetailsBlock;
import uk.gov.london.ops.service.ManagedEntityInterface;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static uk.gov.london.common.GlaUtils.nullSafeAdd;
import static uk.gov.london.ops.project.block.ProjectBlockStatus.*;
import static uk.gov.london.ops.project.template.domain.Template.MilestoneType.MonetaryValue;

@Entity
@JsonFilter("roleBasedFilter")
public class Project extends BaseProject implements ProjectInterface, Serializable, ManagedEntityInterface,
    NotificationTargetEntity {


    public enum Recommendation {
        RecommendApproval, RecommendRejection
    }

    public enum Action {
        ViewChangeReport, Transfer, ViewSummaryReport, Reinstate, Share, Delete
    }

    @Column(name = "organisation_group_id")
    @JoinData(targetTable = "organisation_group", targetColumn = "id", joinType = Join.JoinType.OneToOne,
        comment = "The consortium/partnership owning this project.")
    private Integer organisationGroupId;

    @Column(name = "org_selected")
    private boolean orgSelected;

    @Transient
    private OrganisationGroup organisationGroup;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @ManyToOne(cascade = {}, fetch = FetchType.EAGER)
    @JoinColumn(name = "template_id", nullable = false)
    private Template template;

    @Column(name = "first_approved")
    private OffsetDateTime firstApproved;

    @Column(name = "created_on")
    private OffsetDateTime createdOn;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "last_modified")
    private OffsetDateTime lastModified;

    @JoinData(joinType = Join.JoinType.OneToMany, sourceTable = "project", sourceColumn = "id",
        targetColumn = "project_id", targetTable = "project_block", comment = "")
    @OneToMany(cascade = {CascadeType.ALL}, mappedBy = "project", orphanRemoval = true, targetEntity = NamedProjectBlock.class)
    private final Set<NamedProjectBlock> projectBlocks = new HashSet<>();

    @OneToMany(fetch = FetchType.EAGER, targetEntity = NamedProjectBlock.class)
    @JoinColumn(name = "latest_for_project")
    private final Set<NamedProjectBlock> latestProjectBlocks = new HashSet<>();

    @Column(name = "total_grant_eligibility")
    private Long totalGrantEligibility;

    @Column(name = "strategic_project")
    private Boolean strategicProject = false;

    @Column(name = "associated_projects_enabled")
    private boolean associatedProjectsEnabled;

    @JsonIgnore
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, targetEntity = ProjectHistoryEntity.class)
    @JoinColumn(name = "project_id")
    private List<ProjectHistoryEntity> history = new ArrayList<>();

    @JsonIgnore
    @JoinData(joinType = Join.JoinType.OneToMany, sourceColumn = "id", targetColumn = "project_id",
        targetTable = "project_access_control", comment = "")
    @OneToMany(mappedBy = "project", fetch = FetchType.LAZY, cascade = CascadeType.ALL,
        targetEntity = ProjectAccessControl.class, orphanRemoval = true)
    private Set<ProjectAccessControl> accessControlList = new HashSet<>();

    @Column(name = "payments_only")
    private boolean paymentsOnly;


    @Transient
    private List<ApiErrorItem> messages = new ArrayList<>();

    @Transient
    private boolean approvalWillCreatePendingGrantPayment = false;

    /**
     * Flag showing if the current user is watching this project.
     */
    @Transient
    private boolean currentUserWatching;

    /**
     * Number of users watching the project.
     */
    @Transient
    private Integer nbWatchers;

    @Transient
    private boolean pendingContractSignature;

    @Transient
    private String sapVendorId;

    @Transient
    private Integer leadOrganisationId;

    @Transient
    private boolean isReclaimEnabled;

    public Project() {}

    public Project(Integer id, String title) {
        this.id = id;
        this.title = title;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTitle() {
        String titleToUse = super.getTitle();
        ProjectDetailsBlock newDetailsBlock = this.getDetailsBlock();
        if (newDetailsBlock != null && newDetailsBlock.getTitle() != null) {
            titleToUse = newDetailsBlock.getTitle();
        }
        return titleToUse;
    }

    public void setTitle(String title) {
        super.setTitle(title);
        ProjectDetailsBlock newDetailsBlock = this.getDetailsBlock();
        if (newDetailsBlock != null) {
            newDetailsBlock.setTitle(title);
        }
    }

    public void setOrganisation(OrganisationEntity organisation) {
        if (this.organisation != null) {
            removeFromAccessControlList(this.organisation, GrantAccessTrigger.PROJECT);
        }
        super.setOrganisation(organisation);
        addToAccessControlList(organisation, AccessControlRelationshipType.OWNER, GrantAccessTrigger.PROJECT);
    }

    public void setManagingOrganisation(OrganisationEntity managingOrganisation) {
        if (this.managingOrganisation != null) {
            removeFromAccessControlList(this.managingOrganisation, GrantAccessTrigger.PROJECT);
        }
        super.setManagingOrganisation(managingOrganisation);
    }

    public Integer getOrganisationGroupId() {
        return organisationGroupId;
    }

    public void setOrganisationGroupId(Integer organisationGroupId) {
        this.organisationGroupId = organisationGroupId;
    }

    public boolean isOrgSelected() {
        return orgSelected;
    }

    public void setOrgSelected(boolean orgSelected) {
        this.orgSelected = orgSelected;
    }

    public OrganisationGroup getOrganisationGroup() {
        return organisationGroup;
    }

    public void setOrganisationGroup(OrganisationGroup organisationGroup) {
        this.organisationGroup = organisationGroup;
    }

    public Template getTemplate() {
        return template;
    }

    public void setTemplate(Template template) {
        this.template = template;
    }

    public Integer getLegacyProjectCode() {
        return this.getDetailsBlock().getLegacyProjectCode();
    }

    public OffsetDateTime getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(OffsetDateTime createdOn) {
        this.createdOn = createdOn;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public void setFirstApproved(OffsetDateTime firstApproved) {
        this.firstApproved = firstApproved;
    }

    public OffsetDateTime getFirstApproved() {
        return firstApproved;
    }

    public OffsetDateTime getLastModified() {
        return lastModified;
    }

    public void setLastModified(OffsetDateTime lastModified) {
        this.lastModified = lastModified;
    }

    public ProjectState getProjectState() {
        return new ProjectState(statusName, subStatusName);
    }

    public void setProjectState(ProjectState state) {
        if (state == null || state.getStatus() == null) {
            throw new ValidationException("Attempt to update project state with no new state.");
        }
        this.statusName = state.getStatus();
        this.subStatusName = state.getSubStatus();
    }

    // method for JSON only
    public Integer getTemplateId() {
        return template.getId();
    }

    public boolean isComplete() {
        final Set<NamedProjectBlock> blocks = getLatestProjectBlocks();
        return calculateIsComplete(blocks);
    }

    public List<NamedProjectBlock> getBlocksByType(ProjectBlockType block) {
        List<NamedProjectBlock> list = new ArrayList<>();
        Set<NamedProjectBlock> blocks = getProjectBlocks();
        for (NamedProjectBlock namedProjectBlock : blocks) {
            if (namedProjectBlock.getBlockType().equals(block)) {
                list.add(namedProjectBlock);
            }
        }
        return list;
    }

    public List<NamedProjectBlock> getBlocksByTypeAndDisplayOrder(
        final ProjectBlockType blockType,
        final Integer displayOrder) {

        Collection<NamedProjectBlock> blocks = getProjectBlocks();
        if (blocks != null) {
            return blocks.stream()
                .filter(b -> b.getBlockType().equals(blockType))
                .filter(b -> displayOrder == null
                    || b.getDisplayOrder().equals(displayOrder))
                .collect(Collectors.toList());
        } else {
            return Collections.emptyList();
        }
    }

    /**
     * Finds a block with the given type, display order and status. Returns null if not found.
     */
    public NamedProjectBlock getBlockByTypeDisplayOrderAndStatus(ProjectBlockType blockType, Integer order,
                                                                 ProjectBlockStatus status) {
        return getBlocksByTypeAndDisplayOrder(blockType, order).stream()
            .filter(b -> b.getBlockStatus().equals(status))
            .findFirst()
            .orElse(null);
    }

    public NamedProjectBlock getSingleBlockByType(ProjectBlockType block) {
        List<NamedProjectBlock> blocksByType = getBlocksByType(block);
        if (blocksByType.size() != 1) {
            throw new ValidationException(
                String.format("Unable to retrieve single block of type: %s for project: %s found %d blocks",
                block.name(), this.getTitle(), blocksByType.size()));
        }
        return blocksByType.get(0);
    }

    public NamedProjectBlock getSingleBlockByTypeAndId(ProjectBlockType block, Integer blockId) {
        List<NamedProjectBlock> blocksByType = getBlocksByType(block);
        for (NamedProjectBlock namedProjectBlock : blocksByType) {
            if (blockId != null && blockId.equals(namedProjectBlock.getId())) {
                return namedProjectBlock;
            }
        }
        throw new ValidationException("Unable to find block of required type, with ID: " + blockId);
    }

    public NamedProjectBlock getBlockByTypeDisplayOrderAndLatestVersion(ProjectBlockType blockType, Integer displayOrder) {
        return getBlocksByTypeAndDisplayOrder(blockType, displayOrder).stream()
            .filter(b -> b.isLatestVersion().equals(true))
            .findFirst()
            .orElse(null);
    }

    public List<ApiErrorItem> getMessages() {
        return messages;
    }

    public void setMessages(List<ApiErrorItem> messages) {
        this.messages = messages;
    }

    public boolean isCurrentUserWatching() {
        return currentUserWatching;
    }

    public void setCurrentUserWatching(boolean currentUserWatching) {
        this.currentUserWatching = currentUserWatching;
    }

    public Integer getNbWatchers() {
        return nbWatchers;
    }

    public void setNbWatchers(Integer nbWatchers) {
        this.nbWatchers = nbWatchers;
    }

    public NamedProjectBlock getProjectBlockById(Integer id) {
        for (NamedProjectBlock block : this.getProjectBlocks()) {
            if (block.getId() != null && block.getId().equals(id)) {
                return block;
            }
        }
        return null;
    }

    public boolean isClaimsEnabled() {
        return ProjectStatus.Active.equals(getStatusType());
    }

    @JsonIgnore
    public List<ProjectQuestionsBlock> getQuestionsBlocks() {
        return getProjectBlocks().stream()
            .filter(namedProjectBlock -> namedProjectBlock.getBlockType().equals(ProjectBlockType.Questions))
            .map(namedProjectBlock -> (ProjectQuestionsBlock) namedProjectBlock).collect(Collectors.toList());
    }

    @JsonIgnore
    public ProjectDetailsBlock getDetailsBlock() {
        return (ProjectDetailsBlock) this.getSingleLatestBlockOfType(ProjectBlockType.Details);
    }

    @JsonIgnore
    public ProjectMilestonesBlock getMilestonesBlock() {
        return (ProjectMilestonesBlock) getSingleLatestBlockOfType(ProjectBlockType.Milestones);
    }

    @JsonIgnore
    public ProjectBudgetsBlock getProjectBudgetsBlock() {
        return (ProjectBudgetsBlock) getSingleLatestBlockOfType(ProjectBlockType.ProjectBudgets);
    }

    @JsonIgnore
    public ReceiptsBlock getReceiptsBlock() {
        return (ReceiptsBlock) getSingleLatestBlockOfType(ProjectBlockType.Receipts);
    }

    @JsonIgnore
    public FundingBlock getFundingBlock() {
        return (FundingBlock) getSingleLatestBlockOfType(ProjectBlockType.Funding);
    }

    @JsonIgnore
    public CalculateGrantBlock getCalculateGrantBlock() {
        return (CalculateGrantBlock) getSingleLatestBlockOfType(ProjectBlockType.CalculateGrant);
    }

    @JsonIgnore
    public DeveloperLedGrantBlock getDeveloperLedGrantBlock() {
        return (DeveloperLedGrantBlock) getSingleLatestBlockOfType(ProjectBlockType.DeveloperLedGrant);
    }

    @JsonIgnore
    public IndicativeGrantBlock getIndicativeGrantBlock() {
        return (IndicativeGrantBlock) getSingleLatestBlockOfType(ProjectBlockType.IndicativeGrant);
    }

    @JsonIgnore
    public NegotiatedGrantBlock getNegotiatedGrantBlock() {
        return (NegotiatedGrantBlock) getSingleLatestBlockOfType(ProjectBlockType.NegotiatedGrant);
    }

    @JsonIgnore
    public AffordableHomesBlock getAffordableHomesBlock() {
        return (AffordableHomesBlock) getSingleLatestBlockOfType(ProjectBlockType.AffordableHomes);
    }

    @JsonIgnore
    public BaseGrantBlock getBaseGrantBlock() {
        if (getCalculateGrantBlock() != null) {
            return getCalculateGrantBlock();
        } else if (getDeveloperLedGrantBlock() != null) {
            return getDeveloperLedGrantBlock();
        } else if (getIndicativeGrantBlock() != null) {
            return getIndicativeGrantBlock();
        } else if (getNegotiatedGrantBlock() != null) {
            return getNegotiatedGrantBlock();
        } else {
            return null;
        }
    }

    @JsonIgnore
    public GrantSourceBlock getGrantSourceBlock() {
        return (GrantSourceBlock) getSingleLatestBlockOfType(ProjectBlockType.GrantSource);
    }

    @JsonIgnore
    public DesignStandardsBlock getDesignStandardsBlock() {
        return (DesignStandardsBlock) getSingleLatestBlockOfType(ProjectBlockType.DesignStandards);
    }

    @JsonIgnore
    public ProjectRisksBlock getRisksBlock() {
        return (ProjectRisksBlock) getSingleLatestBlockOfType(ProjectBlockType.Risks);
    }

    @JsonIgnore
    public UnitDetailsBlock getUnitDetailsBlock() {
        return (UnitDetailsBlock) getSingleLatestBlockOfType(ProjectBlockType.UnitDetails);
    }

    @JsonIgnore
    public LearningGrantBlock getLearningGrantBlock() {
        return (LearningGrantBlock) getSingleLatestBlockOfType(ProjectBlockType.LearningGrant);
    }

    @JsonIgnore
    public FundingClaimsBlock getFundingClaimsBlock() {
        return (FundingClaimsBlock) getSingleLatestBlockOfType(ProjectBlockType.FundingClaims);
    }

    @JsonIgnore
    public OutputsCostsBlock getOutputsCostsBlock() {
        return (OutputsCostsBlock) getSingleLatestBlockOfType(ProjectBlockType.OutputsCosts);
    }

    @JsonIgnore
    public OtherFundingBlock getOtherFundingBlock() {
        return (OtherFundingBlock) getSingleLatestBlockOfType(ProjectBlockType.OtherFunding);
    }

    public Boolean isStrategicProject() {
        return strategicProject;
    }

    public void setStrategicProject(Boolean strategicProject) {
        this.strategicProject = strategicProject;
    }

    public boolean isAssociatedProjectsEnabled() {
        return associatedProjectsEnabled;
    }

    public void setAssociatedProjectsEnabled(boolean associatedProjectsEnabled) {
        this.associatedProjectsEnabled = associatedProjectsEnabled;
    }

    public List<ProjectHistoryEntity> getHistory() {
        return history;
    }

    public void setHistory(List<ProjectHistoryEntity> history) {
        this.history = history;
    }

    public ProjectHistoryEntity getLastHistoryEntry() {
        return history.stream().max(Comparator.comparing(ProjectHistoryEntity::getCreatedOn)).orElse(null);
    }

    public Set<ProjectAccessControl> getAccessControlList() {
        return accessControlList;
    }

    public void setAccessControlList(Set<ProjectAccessControl> accessControlList) {
        this.accessControlList = accessControlList;
    }

    public void addToAccessControlList(OrganisationEntity organisation, AccessControlRelationshipType type,
                                       GrantAccessTrigger trigger) {
        this.getAccessControlList().add(new ProjectAccessControl(this, organisation, type, trigger));
    }

    public void removeFromAccessControlList(OrganisationEntity organisation, GrantAccessTrigger trigger) {
        removeFromAccessControlList(organisation.getId(), trigger);
    }

    public void removeFromAccessControlList(Integer orgId, GrantAccessTrigger trigger) {
        this.getAccessControlList()
            .removeIf(ac -> Objects.equals(orgId, ac.getId().getOrganisationId()) && ac.getGrantAccessTrigger().equals(trigger));
    }

    public void recalculateProjectGrantEligibility() {
        this.totalGrantEligibility = null;

        for (NamedProjectBlock block : getLatestProjectBlocks()) {
            if (block instanceof BaseGrantBlock) {
                BaseGrantBlock grantBlock = (BaseGrantBlock) block;
                if (grantBlock.getBlockType().equals(ProjectBlockType.IndicativeGrant)) {
                    // Indicative Grant block NOT included here (GLA-553)
                    continue;
                }
                this.totalGrantEligibility = nullSafeAdd(this.totalGrantEligibility, grantBlock.getTotalGrantEligibility());
            }
        }
    }

    /**
     * @return true if any of the project blocks is unapproved, false otherwise.
     */
    public boolean hasUnapprovedBlocks() {
        for (NamedProjectBlock projectBlock : getProjectBlocks()) {
            if (!projectBlock.isNew() && ProjectBlockStatus.UNAPPROVED.equals(projectBlock.getBlockStatus())) {
                return true;
            }
        }
        return hasNewEditedBlocks();
    }

    public boolean hasNewEditedBlocks() {
        for (NamedProjectBlock b : getProjectBlocks()) {
            if (b.isNew() && b.getLastModified() != null) {
                return true;
            }
        }
        return false;
    }

    public Long getTotalGrantEligibility() {
        return totalGrantEligibility;
    }

    public void setTotalGrantEligibility(Long totalGrantEligibility) {
        this.totalGrantEligibility = totalGrantEligibility;
    }

    @JsonIgnore
    public Set<NamedProjectBlock> getProjectBlocks() {
        return projectBlocks;
    }

    @JsonIgnore
    @Transient
    public Set<NamedProjectBlock> getReportingVersionBlocks() {
        return projectBlocks.stream().filter(NamedProjectBlock::isReportingVersion).collect(Collectors.toSet());
    }

    public NamedProjectBlock getLatestApprovedBlock(ProjectBlockType type) {
        List<NamedProjectBlock> blocks = this.getBlocksByType(type);
        return blocks.stream()
                .filter(b -> ProjectBlockStatus.LAST_APPROVED.equals(b.getBlockStatus())).findFirst().orElse(null);
    }

    @JsonIgnore
    public List<NamedProjectBlock> getLatestApprovedBlocks() {
        return projectBlocks.stream().filter(b -> ProjectBlockStatus.LAST_APPROVED.equals(b.getBlockStatus()))
            .sorted().collect(Collectors.toList());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Project project = (Project) o;

        return !(id != null ? !id.equals(project.id) : project.id != null);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public String toString() {
        return String.format("Project{id=%d, title=%s}", id, title);
    }

    // todo this will hopefully be redundant eventually
    public NamedProjectBlock getSingleLatestBlockOfType(ProjectBlockType type) {
        return getLatestBlockOfType(type, null);
    }

    public List<NamedProjectBlock> getLastApprovedAndUnapproved(ProjectBlockType type, Integer displayOrder) {
        return this.getBlocksByTypeAndDisplayOrder(type, displayOrder).stream()
            .filter(b -> !b.getBlockStatus().equals(APPROVED)).collect(Collectors.toList());
    }

    public NamedProjectBlock getLatestBlockOfType(ProjectBlockType type, Integer displayOrder) {
        Set<NamedProjectBlock> blocksByType = getLatestProjectBlocks();
        NamedProjectBlock latest = null;
        for (NamedProjectBlock next : blocksByType) {
            if (type.equals(next.getBlockType()) && (displayOrder == null || displayOrder.equals(next.getDisplayOrder()))) {
                latest = next;
            }
        }
        return latest;
    }

    public BigDecimal getAdvancePaymentAmount() {
        OutputsCostsBlock costs = (OutputsCostsBlock) this.getSingleLatestBlockOfType(ProjectBlockType.OutputsCosts);
        return costs == null ? null : costs.getAdvancePayment();
    }

    public void approveBlock(NamedProjectBlock block, String username, OffsetDateTime approvalTime) {
        final List<NamedProjectBlock> previousApprovedBlocks = getBlocksByTypeAndDisplayOrder(
            block.getBlockType(), block.getDisplayOrder());

        //Mark the previous LAST_APPROVED as APPROVED
        if (previousApprovedBlocks != null) {
            previousApprovedBlocks.stream()
                .filter(b -> !b.getId().equals(block.getId()))
                .filter(b -> LAST_APPROVED.equals(b.getBlockStatus()))
                .forEach(b -> {
                    b.setBlockStatus(APPROVED);
                    b.setReportingVersion(false);
                });
        }
        if (block.getProject().getStateModel().isReportOnLastApproved()) {
            block.setReportingVersion(true);
        }
        if (block.isApproved()) {
            block.approve(block.getApproverUsername(), block.getApprovalTime());
        } else {
            block.approve(username, approvalTime);
        }
    }

    public void handleEvent(OpsEvent opsEvent) {
        if (EventType.MilestoneApproval.equals(opsEvent.getEventType())) {
            ProjectHistoryEntity history = new ProjectHistoryEntity(
                ProjectHistoryEventType.MilestoneClaimApproved, opsEvent.getMessage());
            history.setExternalId(opsEvent.getExternalId());
            this.getHistory().add(history);
        } else if (EventType.QuarterApproval.equals(opsEvent.getEventType())) {
            ProjectHistoryEntity history = new ProjectHistoryEntity(
                    ProjectHistoryEventType.QuarterlyClaimApproved, opsEvent.getMessage());
            if (StringUtils.isNotEmpty(opsEvent.getComments())) {
                history.setComments(opsEvent.getComments());
            }
            this.getHistory().add(history);
        } else if (EventType.CancelApprovedClaim.equals(opsEvent.getEventType())) {
            ProjectHistoryEntity history = new ProjectHistoryEntity(ProjectHistoryEventType.ApprovedClaimCancelled, opsEvent.getMessage());
            if (StringUtils.isNotEmpty(opsEvent.getComments())) {
                history.setComments(opsEvent.getComments());
            }
            history.setCreatedBy(opsEvent.getUser().getFullName());
            this.getHistory().add(history);
        }
        for (NamedProjectBlock namedProjectBlock : this.getLatestProjectBlocks()) {
            namedProjectBlock.handleEvent(opsEvent);
        }

    }

    public Map<GrantType, BigDecimal> getGrantsRequested() {
        Map<GrantType, BigDecimal> existingRequests = new HashMap<>();
        existingRequests.put(GrantType.Grant, BigDecimal.ZERO);
        existingRequests.put(GrantType.RCGF, BigDecimal.ZERO);
        existingRequests.put(GrantType.DPF, BigDecimal.ZERO);

        for (NamedProjectBlock latestProjectBlock : this.getLatestProjectBlocks()) {
            if (latestProjectBlock instanceof FundingSourceProvider) {
                Map<GrantType, BigDecimal> fundingRequested = ((FundingSourceProvider) latestProjectBlock).getFundingRequested();
                fundingRequested.forEach((key, value) -> existingRequests.merge(key, value, BigDecimal::add));
            }
        }
        return existingRequests;
    }

    public BigDecimal getTotalGrantRequested() {

        return this.getLatestProjectBlocks().stream()
                .filter(pb -> pb instanceof FundingSourceProvider)
                .map(fsp -> ((FundingSourceProvider) fsp).getTotalGrantRequested() == null
                        ? BigDecimal.ZERO : ((FundingSourceProvider) fsp).getTotalGrantRequested())
                .reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
    }

    @JsonIgnore
    /**
     * Returns a map of tenure type ext iD to total number of units for each tenure type
     */
    public Map<Integer, Integer> getTotalUnitsByExternalId() {
        Map<Integer, Integer> response = new HashMap<>();
        this.getTemplate().getTenureTypes().forEach(t -> response.put(t.getExternalId(), 0));

        for (NamedProjectBlock block : this.getProjectBlocks()) {
            if (block instanceof AffordableHomesBlock && block.isLatestVersion()) {
                getDataFromGrantBlock(response, (AffordableHomesBlock) block);
            } else if (block instanceof BaseGrantBlock  && block.isLatestVersion()) {
                getDataFromGrantBlock(response, (BaseGrantBlock) block);
            }
        }
        return response;
    }

    private void getDataFromGrantBlock(Map<Integer, Integer> response, AffordableHomesBlock block) {
        block.getEntries().stream()
                .filter(e -> e.getType().equals(AffordableHomesType.Completion))
                .filter(e -> e.getOfWhichCategory() == null)
                .forEach(e -> updateMap(response, e));
    }

    private void getDataFromGrantBlock(Map<Integer, Integer> response, BaseGrantBlock namedProjectBlock) {
        BaseGrantBlock block = namedProjectBlock;
        Set<ProjectTenureDetails> projectTenureDetailsEntries = block.getTenureTypeAndUnitsEntries();
        for (ProjectTenureDetails projectTenureDetailsEntry : projectTenureDetailsEntries) {
            Integer calculatedTotalUnits = block.calculateTotalUnits(projectTenureDetailsEntry);
            Integer totalUnits = calculatedTotalUnits == null ? 0 : calculatedTotalUnits;
            Integer externalId = projectTenureDetailsEntry.getTenureType().getExternalId();
            Integer existingValue = response.get(externalId);

            if (existingValue == null) {
                response.put(externalId, totalUnits);
            } else {
                response.replace(externalId, totalUnits + existingValue);
            }
        }
    }

    private void updateMap(Map<Integer, Integer> response, AffordableHomesEntry entry) {
        if (entry.getUnits() != null) {
            Integer existingUnits = response.get(entry.getTenureTypeId());
            response.replace(entry.getTenureTypeId(), existingUnits + entry.getUnits());
        }
    }

    /**
     * @return true if approving the project will result in a pending payment being generated.
     */
    @Transient
    public Boolean getApprovalWillCreatePendingPayment() {
        if (ProjectStatus.Active.equals(this.getStatusType())) {
            for (NamedProjectBlock block : getLatestProjectBlocks()) {
                if (block.getApprovalWillCreatePendingPayment()) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * @return true if approving the project will result in a grant pending payment being generated.
     * By grant pending payment we mean a payment that will be processed to SAP.
     */
    @Transient
    public boolean getApprovalWillCreatePendingGrantPayment() {
        return approvalWillCreatePendingGrantPayment;
    }

    public void setApprovalWillCreatePendingGrantPayment(boolean approvalWillCreatePendingGrantPayment) {
        this.approvalWillCreatePendingGrantPayment = approvalWillCreatePendingGrantPayment;
    }

    @Transient
    @JsonIgnore
    public Set<String> getApprovalPaymentSources() {
        Set<String> sources = new HashSet<>();
        if (ProjectStatus.Active.equals(this.getStatusType())) {
            for (NamedProjectBlock block : getLatestProjectBlocks()) {
                sources.addAll(block.getPaymentsSourcesCreatedViaApproval());
            }
        }
        return sources;
    }

    @Transient
    public boolean getMonetaryValueReclaimRequired() {
        return template.getMilestoneType().equals(MonetaryValue) && getGrantSourceAdjustmentAmount().signum() < 0;
    }

    @JsonIgnore
    public boolean ignoreReconcilliationAmountValidation() {
        if (template.isBlockPresent(ProjectBlockType.LearningGrant)) {
            return ((LearningGrantTemplateBlock) template.getSingleBlockByType(ProjectBlockType.LearningGrant))
                    .getCanManuallyClaimP14();
        }
        return false;
    }

    @Transient
    public Boolean getApprovalWillCreatePendingReclaim() {
        //TODO create similar check for the funding block
        ProjectMilestonesBlock milestonesBlock = getMilestonesBlock();

        Set<NamedProjectBlock> blocksNeedingReclaim = this.getLatestProjectBlocks().stream()
                .filter(b -> b.getBlockStatus().equals(UNAPPROVED) && b.getApprovalWillCreatePendingReclaim())
                .collect(Collectors.toSet());

        boolean milestoneReclaim = false;
        if (milestonesBlock != null) {
            milestoneReclaim = milestonesBlock.getMilestones().stream()
                            .filter(m -> m.getClaimStatus() != null)
                            .anyMatch(m -> m.getClaimStatus().equals(ClaimStatus.Withdrawn)
                                && m.calculateTotalValueReclaimed() != 0L);

            if (milestonesBlock.getApprovalWillCreatePendingReclaim()) {
                milestoneReclaim = true;
            }
        }

        if (milestoneReclaim) {
            return true;
        }

        return !blocksNeedingReclaim.isEmpty() && blocksNeedingReclaim.stream()
                .anyMatch(NamedProjectBlock::getApprovalWillCreatePendingReclaim);
    }

    public Map<GrantType, Long> getCurrentGrantSourceValuesByType() {
        GrantSourceBlock grantSourceBlock = getGrantSourceBlock();
        Map<GrantType, Long> map = null;
        if (grantSourceBlock != null) {
            map = new HashMap<>();
            map.put(GrantType.RCGF, grantSourceBlock.getRecycledCapitalGrantFundValue());
            map.put(GrantType.DPF, grantSourceBlock.getDisposalProceedsFundValue());
            if (template.getMilestoneType().equals(MonetaryValue)) {
                map.put(GrantType.Grant, grantSourceBlock.getGrantValue());
            }
        }
        return map;
    }

    public BigDecimal getGrantSourceAdjustmentAmount() {
        if (!ProjectStatus.Active.equals(this.getStatusType())) {
            return BigDecimal.ZERO;
        }

        BigDecimal adjustmentAmount = this.getLatestProjectBlocks().stream()
                .filter(b -> b instanceof FundingSourceProvider && ProjectBlockStatus.UNAPPROVED.equals(b.getBlockStatus()))
                .map(b -> ((FundingSourceProvider) b).getGrantAdjustmentAmount((FundingSourceProvider)
                        this.getLatestApprovedBlock(b.getBlockType())))
                .reduce(BigDecimal::add).orElse(BigDecimal.ZERO);

        return adjustmentAmount;


    }

    public BigDecimal getGrantSourceAdjustmentAmountOld() {
        if (!ProjectStatus.Active.equals(this.getStatusType())) {
            return BigDecimal.ZERO;
        }
        ProjectMilestonesBlock milestonesBlock = getMilestonesBlock();
        if (milestonesBlock != null) {
            GrantSourceBlock grantSourceBlock = getGrantSourceBlock();
            if (grantSourceBlock != null && ProjectBlockStatus.UNAPPROVED.equals(grantSourceBlock.getBlockStatus())) {
                GrantSourceBlock approvedBlock = (GrantSourceBlock) getLatestApprovedBlock(ProjectBlockType.GrantSource);
                // approved block should never be null
                if (approvedBlock == null || (grantSourceBlock.isAssociatedProject() && !approvedBlock.isAssociatedProject())) {
                    return BigDecimal.ZERO;
                } else {
                    Long newGrant = grantSourceBlock.getGrantValue() == null ? 0L : grantSourceBlock.getGrantValue();
                    Long oldGrant = approvedBlock.getGrantValue() == null ? 0L : approvedBlock.getGrantValue();
                    return new BigDecimal(newGrant - oldGrant);
                }
            }
        }

        LearningGrantBlock oldLearningGrantBlock = (LearningGrantBlock) getLatestApprovedBlock(ProjectBlockType.LearningGrant);
        LearningGrantBlock newLearningGrantBlock = getLearningGrantBlock();
        if (oldLearningGrantBlock != null
            && ProjectBlockStatus.UNAPPROVED.equals(newLearningGrantBlock.getBlockStatus())) {
            BigDecimal oldAlloc = Optional.ofNullable(oldLearningGrantBlock.getTotalYearlyAllocation()).orElse(BigDecimal.ZERO);
            BigDecimal newAlloc = Optional.ofNullable(newLearningGrantBlock.getTotalYearlyAllocation()).orElse(BigDecimal.ZERO);
            return newAlloc.subtract(oldAlloc);
        }
        return BigDecimal.ZERO;
    }

    @Deprecated
    public boolean isAutoApproval() {
        return this.getStateModel().equals(StateModel.AutoApproval);
    }

    public void setPendingContractSignature(boolean pendingContractSignature) {
        this.pendingContractSignature = pendingContractSignature;
    }

    public boolean isPendingContractSignature() {
        return this.pendingContractSignature;
    }

    public String getSapVendorId() {
        return sapVendorId;
    }

    public void setSapVendorId(String sapVendorId) {
        this.sapVendorId = sapVendorId;
    }

    public Integer getLeadOrganisationId() {
        return leadOrganisationId;
    }

    public void setLeadOrganisationId(Integer leadOrganisationId) {
        this.leadOrganisationId = leadOrganisationId;
    }

    @JsonIgnore
    public Set<NamedProjectBlock> getLatestProjectBlocks() {
        return latestProjectBlocks;
    }

    public void addBlockToProject(NamedProjectBlock block) {
        if (!block.isHidden()) {
            this.getLatestProjectBlocks().add(block);
        }
        this.getProjectBlocks().add(block);
    }

    public void addLabel(Label label) {
        getLabels().add(label);
        for (NamedProjectBlock block : this.getLatestProjectBlocks()) {
            block.getLabels().add(label);
        }
    }

    public boolean isAssociatedProject() {
        GrantSourceBlock grantSourceBlock = getGrantSourceBlock();
        return grantSourceBlock != null && grantSourceBlock.isAssociatedProject();
    }

    @JsonIgnore
    public boolean anyBlocksLocked() {
        return projectBlocks.stream().anyMatch(b -> b.getLockDetails() != null);
    }

    public boolean isReclaimEnabled() {
        return isReclaimEnabled;
    }

    public void setReclaimEnabled(boolean reclaimEnabled) {
        isReclaimEnabled = reclaimEnabled;
    }

    public InternalProjectBlock getInternalBlockById(Integer blockId) {
        return getInternalBlocks().stream().filter(b -> b.getId().equals(blockId)).findFirst().orElse(null);
    }

    public InternalProjectBlock getInternalBlockByType(InternalBlockType type) {
        return getInternalBlocks().stream().filter(b -> b.getType().equals(type)).findFirst().orElse(null);
    }

    public InternalProjectBlock getInternalBlockByTypeAndDisplayOrder(InternalBlockType type, Integer displayOrder) {
        return getInternalBlocks().stream()
                .filter(b -> b.getType().equals(type) && b.getDisplayOrder().equals(displayOrder))
                .findFirst()
                .orElse(null);
    }

    @JsonIgnore
    public InternalProjectBlock getSingleInternalBlockByDisplayOrder(Integer displayOrder) {
        return getInternalBlocks().stream()
                .filter(b -> displayOrder.equals(b.getDisplayOrder()))
                .findFirst()
                .orElse(null);
    }

    @JsonIgnore
    public InternalAssessmentBlock getInternalAssessmentBlock() {
        return (InternalAssessmentBlock) getInternalBlockByType(InternalBlockType.Assessment);
    }

    @JsonIgnore
    public ProgrammeTemplate getProgrammeTemplate() {
        return this.getProgramme().getProgrammeTemplateByTemplateID(this.template.getId());
    }

    public NamedProjectBlock getSingleBlockByDisplayOrder(Integer displayOrder) {
        return projectBlocks.stream().filter(b -> displayOrder.equals(b.getDisplayOrder()))
            .findFirst().orElse(null);
    }


    public List<String> getLabelNamesByType(String type) {
        List<String> labelsName = new ArrayList<>();

        if (type.equalsIgnoreCase(LabelType.Custom.name())) {
            labelsName = getLabels().stream()
                .filter(l -> l.getType().name().equalsIgnoreCase(type))
                .map(label -> label.getText())
                .collect(Collectors.toList());
        }

        if (type.equalsIgnoreCase(LabelType.Predefined.name())) {
            labelsName = getLabels().stream()
                .filter(l -> l.getType().name().equalsIgnoreCase(type))
                .map(label -> label.getPreSetLabel().getLabelName())
                .collect(Collectors.toList());
        }

        return labelsName;
    }

    @Override
    public String getIdAsString() {
        return id != null ? id.toString() : null;
    }

    public boolean isPreviouslySubmitted() {
        return this.getHistory().stream()
            .anyMatch(history -> Objects.equals(ProjectStateEntity.getStatusType(history.getStatusName(),
                    history.getSubStatusName()), ProjectStatus.Submitted));
    }

    public boolean isPreviouslyReturned() {
        return this.getHistory().stream()
            .anyMatch(history -> ProjectTransition.Returned.equals(this.getLastHistoryEntry().getTransition()));
    }

    @JsonIgnore
    public String getSupplierProductCode() {
        InternalProjectAdminBlock internalProjectAdminBlock =
                (InternalProjectAdminBlock) getInternalBlockByType(InternalBlockType.ProjectAdmin);

        if (internalProjectAdminBlock == null) {
            return null;
        }

        if (GlaUtils.isNullOrEmpty(internalProjectAdminBlock.getOrganisationShortName())
                && GlaUtils.isNullOrEmpty(internalProjectAdminBlock.getProjectShortName())) {
            return null;
        }
        return getSupplierCodeFromAdminBlock(internalProjectAdminBlock);
    }

    private String getSupplierCodeFromAdminBlock(InternalProjectAdminBlock adminBlock) {
        String returnSupplerCode;

        if (GlaUtils.isNullOrEmpty(adminBlock.getOrganisationShortName())) {
            returnSupplerCode = adminBlock.getProjectShortName();
        } else if (GlaUtils.isNullOrEmpty(adminBlock.getProjectShortName())) {
            returnSupplerCode =  adminBlock.getOrganisationShortName();
        } else {
            returnSupplerCode = adminBlock.getOrganisationShortName() + "/" + adminBlock.getProjectShortName();
        }

        return returnSupplerCode.length() > 35 ? returnSupplerCode.substring(0, 35)
                : returnSupplerCode;
    }

    public boolean isPaymentOnlyApprovalPossible() {
        return this.getLatestProjectBlocks().stream().anyMatch(NamedProjectBlock::isPaymentsOnlyApprovalPossible);
    }

    public boolean isPaymentsOnly() {
        return paymentsOnly;
    }

    public void setPaymentsOnly(boolean paymentsOnly) {
        this.paymentsOnly = paymentsOnly;
    }

}
