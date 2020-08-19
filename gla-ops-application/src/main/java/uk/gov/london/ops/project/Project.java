/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.project;

import static uk.gov.london.common.GlaUtils.nullSafeAdd;
import static uk.gov.london.ops.project.block.NamedProjectBlock.BlockStatus.APPROVED;
import static uk.gov.london.ops.project.block.NamedProjectBlock.BlockStatus.LAST_APPROVED;
import static uk.gov.london.ops.project.implementation.spe.SimpleProjectExportConstants.FieldNames.org_id;
import static uk.gov.london.ops.project.implementation.spe.SimpleProjectExportConstants.FieldNames.org_name;
import static uk.gov.london.ops.project.implementation.spe.SimpleProjectExportConstants.FieldNames.programme_id;
import static uk.gov.london.ops.project.implementation.spe.SimpleProjectExportConstants.FieldNames.programme_name;
import static uk.gov.london.ops.project.implementation.spe.SimpleProjectExportConstants.FieldNames.project_id;
import static uk.gov.london.ops.project.implementation.spe.SimpleProjectExportConstants.FieldNames.template_id;
import static uk.gov.london.ops.project.implementation.spe.SimpleProjectExportConstants.FieldNames.template_name;
import static uk.gov.london.ops.project.template.domain.Template.MilestoneType.MonetaryValue;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Transient;
import uk.gov.london.common.error.ApiErrorItem;
import uk.gov.london.ops.EventType;
import uk.gov.london.ops.OpsEvent;
import uk.gov.london.ops.framework.exception.ValidationException;
import uk.gov.london.ops.framework.jpa.Join;
import uk.gov.london.ops.framework.jpa.JoinData;
import uk.gov.london.ops.notification.NotificationTargetEntity;
import uk.gov.london.ops.organisation.model.Organisation;
import uk.gov.london.ops.organisation.model.OrganisationGroup;
import uk.gov.london.ops.programme.domain.ProgrammeTemplate;
import uk.gov.london.ops.project.ProjectHistory.Transition;
import uk.gov.london.ops.project.accesscontrol.AccessControlRelationshipType;
import uk.gov.london.ops.project.accesscontrol.GrantAccessTrigger;
import uk.gov.london.ops.project.accesscontrol.ProjectAccessControl;
import uk.gov.london.ops.project.block.DesignStandardsBlock;
import uk.gov.london.ops.project.block.FundingSourceProvider;
import uk.gov.london.ops.project.block.NamedProjectBlock;
import uk.gov.london.ops.project.block.NamedProjectBlock.BlockStatus;
import uk.gov.london.ops.project.block.ProjectBlockType;
import uk.gov.london.ops.project.block.ProjectDetailsBlock;
import uk.gov.london.ops.project.budget.ProjectBudgetsBlock;
import uk.gov.london.ops.project.funding.FundingBlock;
import uk.gov.london.ops.project.grant.BaseGrantBlock;
import uk.gov.london.ops.project.grant.CalculateGrantBlock;
import uk.gov.london.ops.project.grant.DeveloperLedGrantBlock;
import uk.gov.london.ops.project.grant.GrantSourceBlock;
import uk.gov.london.ops.project.grant.GrantType;
import uk.gov.london.ops.project.grant.IndicativeGrantBlock;
import uk.gov.london.ops.project.grant.NegotiatedGrantBlock;
import uk.gov.london.ops.project.grant.ProjectTenureDetails;
import uk.gov.london.ops.project.implementation.spe.SimpleProjectExportConfig;
import uk.gov.london.ops.project.internalblock.InternalAssessmentBlock;
import uk.gov.london.ops.project.internalblock.InternalBlockType;
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
import uk.gov.london.ops.project.state.ProjectStatus;
import uk.gov.london.ops.project.state.StateModel;
import uk.gov.london.ops.project.template.domain.Template;
import uk.gov.london.ops.project.unit.UnitDetailsBlock;
import uk.gov.london.ops.service.ManagedEntityInterface;

@Entity
@JsonFilter("roleBasedFilter")
public class Project extends BaseProject implements ProjectInterface, Serializable, ManagedEntityInterface,
    NotificationTargetEntity {

    public enum Recommendation {
        RecommendApproval, RecommendRejection
    }

    public enum Action {
        ViewChangeReport, Transfer, ViewSummaryReport, Reinstate, Share
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
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, targetEntity = ProjectHistory.class)
    @JoinColumn(name = "project_id")
    private List<ProjectHistory> history = new ArrayList<>();

    @JsonIgnore
    @JoinData(joinType = Join.JoinType.OneToMany, sourceColumn = "id", targetColumn = "project_id",
        targetTable = "project_access_control", comment = "")
    @OneToMany(mappedBy = "project", fetch = FetchType.LAZY, cascade = CascadeType.ALL,
        targetEntity = ProjectAccessControl.class, orphanRemoval = true)
    private Set<ProjectAccessControl> accessControlList = new HashSet<>();

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

    public Project() {
    }

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

    public void setOrganisation(Organisation organisation) {
        if (this.organisation != null) {
            removeFromAccessControlList(this.organisation, GrantAccessTrigger.PROJECT);
        }
        super.setOrganisation(organisation);
        addToAccessControlList(organisation, AccessControlRelationshipType.OWNER, GrantAccessTrigger.PROJECT);
    }

    public void setManagingOrganisation(Organisation managingOrganisation) {
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
    public NamedProjectBlock getBlockByTypeDisplayOrderAndStatus(ProjectBlockType blockType, Integer order, BlockStatus status) {
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

    public List<ProjectHistory> getHistory() {
        return history;
    }

    public void setHistory(List<ProjectHistory> history) {
        this.history = history;
    }

    public ProjectHistory getLastHistoryEntry() {
        return history.stream().max(Comparator.comparing(ProjectHistory::getCreatedOn)).orElse(null);
    }

    public Set<ProjectAccessControl> getAccessControlList() {
        return accessControlList;
    }

    public void setAccessControlList(Set<ProjectAccessControl> accessControlList) {
        this.accessControlList = accessControlList;
    }

    public void addToAccessControlList(Organisation organisation, AccessControlRelationshipType type, GrantAccessTrigger trigger) {
        this.getAccessControlList().add(new ProjectAccessControl(this, organisation, type, trigger));
    }

    public void removeFromAccessControlList(Organisation organisation, GrantAccessTrigger trigger) {
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
            if (!projectBlock.isNew() && NamedProjectBlock.BlockStatus.UNAPPROVED.equals(projectBlock.getBlockStatus())) {
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
            .filter(b -> NamedProjectBlock.BlockStatus.LAST_APPROVED.equals(b.getBlockStatus())).findFirst().orElse(null);
    }

    @JsonIgnore
    public List<NamedProjectBlock> getLatestApprovedBlocks() {
        return projectBlocks.stream().filter(b -> NamedProjectBlock.BlockStatus.LAST_APPROVED.equals(b.getBlockStatus()))
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

    public Map<String, Object> simpleDataExtract(SimpleProjectExportConfig config) {
        Map<String, Object> projectValues = new HashMap<>();
        projectValues.put(project_id.name(), this.getId());

        if (this.getProgramme() != null) {
            projectValues.put(programme_id.name(), this.getProgrammeId());
            projectValues.put(programme_name.name(), this.getProgramme().getName());
        }

        if (this.getTemplate() != null) {
            projectValues.put(template_id.name(), this.getTemplateId());
            projectValues.put(template_name.name(), this.getTemplate().getName());
        }

        if (this.getOrganisation() != null) {
            projectValues.put(org_id.name(), this.getOrganisation().getId());
            projectValues.put(org_name.name(), this.getOrganisation().getName());
        }

        for (NamedProjectBlock namedProjectBlock : this.getReportingVersionBlocks()) {
            projectValues.putAll(namedProjectBlock.simpleDataExtract(config));
        }

        return projectValues;

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

    public void approveBlock(final NamedProjectBlock block,
        final String username,
        final OffsetDateTime approvalTime) {
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
            ProjectHistory history = new ProjectHistory(
                ProjectHistory.HistoryEventType.MilestoneClaimApproved, opsEvent.getMessage());
            history.setExternalId(opsEvent.getExternalId());
            for (NamedProjectBlock namedProjectBlock : this.getLatestProjectBlocks()) {
                namedProjectBlock.handleEvent(opsEvent);
            }
            this.getHistory().add(history);
        } else if (EventType.QuarterApproval.equals(opsEvent.getEventType())) {
            ProjectHistory history = new ProjectHistory(
                ProjectHistory.HistoryEventType.QuarterlyClaimApproved, opsEvent.getMessage());
            this.getHistory().add(history);
        }
    }

    @JsonIgnore
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

    @JsonIgnore
    /**
     * Returns a map of tenure type ext iD to total number of units for each tenure type
     */
    public Map<Integer, Integer> getTotalUnitsByExternalId() {
        Map<Integer, Integer> response = new HashMap<>();
        for (NamedProjectBlock namedProjectBlock : this.getProjectBlocks()) {
            if (namedProjectBlock instanceof BaseGrantBlock && namedProjectBlock.isLatestVersion()) {
                BaseGrantBlock block = (BaseGrantBlock) namedProjectBlock;
                Set<ProjectTenureDetails> projectTenureDetailsEntries = block.getTenureTypeAndUnitsEntries();
                for (ProjectTenureDetails projectTenureDetailsEntry : projectTenureDetailsEntries) {
                    Integer calculatedTotalUnits = block.calculateTotalUnits(
                        projectTenureDetailsEntry);
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
        }
        return response;
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

    @Transient
    public Boolean getApprovalWillCreatePendingReclaim() {
        GrantSourceBlock grantSourceBlock = getGrantSourceBlock();
        // pending reclaims can only happen on unapproved grant source
        if (grantSourceBlock != null && grantSourceBlock.isApproved()) {
            return false;
        }

        ProjectMilestonesBlock milestonesBlock = getMilestonesBlock();
        if (milestonesBlock != null) {
            return milestonesBlock.getApprovalWillCreatePendingReclaim();
        }

        LearningGrantBlock learningGrantBlock = getLearningGrantBlock();
        if (learningGrantBlock != null) {
            return learningGrantBlock.getApprovalWillCreatePendingReclaim();
        }

        return false;
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
        ProjectMilestonesBlock milestonesBlock = getMilestonesBlock();
        if (milestonesBlock != null) {
            GrantSourceBlock grantSourceBlock = getGrantSourceBlock();
            if (grantSourceBlock != null && NamedProjectBlock.BlockStatus.UNAPPROVED.equals(grantSourceBlock.getBlockStatus())) {
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
            && NamedProjectBlock.BlockStatus.UNAPPROVED.equals(newLearningGrantBlock.getBlockStatus())) {
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
            .anyMatch(history -> Objects.equals(history.getProjectState().getStatusType(), ProjectStatus.Submitted));
    }

    public boolean isPreviouslyReturned() {
        return this.getHistory().stream()
            .anyMatch(history -> Transition.Returned.equals(this.getLastHistoryEntry().getTransition()));
    }

}
