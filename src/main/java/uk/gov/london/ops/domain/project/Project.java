/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.domain.project;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.london.ops.EventType;
import uk.gov.london.ops.OpsEvent;
import uk.gov.london.ops.domain.organisation.OrganisationGroup;
import uk.gov.london.ops.domain.project.funding.FundingBlock;
import uk.gov.london.ops.domain.project.outputs.OutputsCostsBlock;
import uk.gov.london.ops.domain.project.question.ProjectQuestionsBlock;
import uk.gov.london.ops.domain.project.skills.FundingClaimsBlock;
import uk.gov.london.ops.domain.project.skills.LearningGrantBlock;
import uk.gov.london.ops.domain.project.state.ProjectStatus;
import uk.gov.london.ops.domain.project.state.ProjectSubStatus;
import uk.gov.london.ops.domain.template.Programme;
import uk.gov.london.ops.domain.template.ProgrammeSummary;
import uk.gov.london.ops.domain.template.ProgrammeTemplate;
import uk.gov.london.ops.domain.template.Template;
import uk.gov.london.ops.notification.NotificationTargetEntity;
import uk.gov.london.ops.project.implementation.spe.SimpleProjectExportConfig;
import uk.gov.london.ops.service.ManagedEntityInterface;
import uk.gov.london.ops.service.project.state.ProjectState;
import uk.gov.london.ops.service.project.state.StateModel;
import uk.gov.london.ops.framework.annotations.PermissionRequired;
import uk.gov.london.common.error.ApiErrorItem;
import uk.gov.london.ops.framework.exception.ValidationException;
import uk.gov.london.ops.framework.jpa.Join;
import uk.gov.london.ops.framework.jpa.JoinData;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import static javax.persistence.CascadeType.ALL;
import static uk.gov.london.common.GlaUtils.nullSafeAdd;
import static uk.gov.london.ops.domain.project.NamedProjectBlock.BlockStatus.APPROVED;
import static uk.gov.london.ops.domain.project.NamedProjectBlock.BlockStatus.LAST_APPROVED;
import static uk.gov.london.ops.domain.template.Template.MilestoneType.MonetaryValue;
import static uk.gov.london.ops.project.implementation.spe.SimpleProjectExportConstants.FieldNames.*;
import static uk.gov.london.ops.service.PermissionType.PROJ_VIEW_INTERNAL_BLOCKS;


@Entity
@JsonFilter("roleBasedFilter")
public class Project extends BaseProject implements Serializable, ManagedEntityInterface, NotificationTargetEntity {

    @Transient
    private final Logger log = LoggerFactory.getLogger(getClass());


    public enum Recommendation {
        RecommendApproval, RecommendRejection
    }

    public enum Action {
        ViewChangeReport, Transfer, ViewSummaryReport, Reinstate
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

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @ManyToOne(cascade = {})
    @JoinColumn(name = "programme_id", nullable = false)
    private Programme programme;

    @Column(name = "first_approved")
    private OffsetDateTime firstApproved;

    @Column(name = "created_on")
    private OffsetDateTime createdOn;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "last_modified")
    private OffsetDateTime lastModified;

    @Column(name = "state_model")
    @Enumerated(EnumType.STRING)
    private StateModel stateModel;

    @JoinData(joinType = Join.JoinType.OneToMany, sourceTable = "project", sourceColumn = "id", targetColumn = "project_id", targetTable = "project_block",
            comment = "")
    @OneToMany(cascade = {CascadeType.ALL}, mappedBy = "project", orphanRemoval = true, targetEntity = NamedProjectBlock.class)
    private Set<NamedProjectBlock> projectBlocks = new HashSet<>();

    @OneToMany(fetch = FetchType.EAGER, targetEntity = NamedProjectBlock.class)
    @JoinColumn(name = "latest_for_project")
    private Set<NamedProjectBlock> latestProjectBlocks = new HashSet<>();

    @JsonIgnore
    @PermissionRequired(PROJ_VIEW_INTERNAL_BLOCKS)
    @JoinData(joinType = Join.JoinType.OneToMany, sourceTable = "project", sourceColumn = "id", targetColumn = "project_id", targetTable = "internal_project_block",
            comment = "")
    @OneToMany(cascade = {CascadeType.ALL}, mappedBy = "project", orphanRemoval = true, targetEntity = InternalProjectBlock.class)
    private Set<InternalProjectBlock> internalBlocks = new HashSet<>();

    @Transient
    private List<InternalProjectBlock> internalBlocksSorted = new ArrayList<>();

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

    @Transient
    private Set<ProjectState> allowedTransitions;

    @Transient
    private boolean pendingPayments;

    @Transient
    private boolean reclaimedPayments;

    @Transient
    private List<ApiErrorItem> messages = new ArrayList<>();

    @Transient
    private List<Action> allowedActions = new ArrayList<>();

    /** Flag showing if the current user is watching this project. */
    @Transient
    private boolean currentUserWatching;

    /** Number of users watching the project. */
    @Transient
    private Integer nbWatchers;

    @Transient
    private boolean pendingContractSignature;

    @Transient
    private boolean isReclaimEnabled;


    @JoinData(joinType = Join.JoinType.OneToMany, sourceColumn = "id", targetColumn = "project_id", targetTable = "label",  comment = "")
    @OneToMany(fetch = FetchType.LAZY,  cascade = ALL, orphanRemoval = true, mappedBy = "projectId", targetEntity = Label.class)
    private Set<Label> labels = new HashSet<>();

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
        if ( newDetailsBlock != null && newDetailsBlock.getTitle()!=null) {
            titleToUse = newDetailsBlock.getTitle() ;
        }
        return titleToUse;
    }


    public void setTitle(String title) {
        super.setTitle(title);
        ProjectDetailsBlock newDetailsBlock = this.getDetailsBlock();
        if ( newDetailsBlock != null) {
            newDetailsBlock.setTitle(title);
        }
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

    @JsonIgnore
    public Programme getProgramme() {
        return programme;
    }

    @JsonProperty(value = "programme", access = JsonProperty.Access.READ_ONLY)
    public ProgrammeSummary getProgrammeSummary() {
        return programme != null ? ProgrammeSummary.createFrom(programme) : null;
    }

    public void setProgramme(Programme programme) {
        this.programme = programme;
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

    public void setFirstApproved(OffsetDateTime firstApproved) { this.firstApproved = firstApproved; }

    public OffsetDateTime getFirstApproved() { return firstApproved; }

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

    // method for JSON only
    public Integer getProgrammeId() {
        if (programme != null) {
            return programme.getId();
        }
        return null;
    }


    public boolean isComplete() {
        final Set<NamedProjectBlock> blocks = getLatestProjectBlocks();
        boolean allNormalBlocksAreApproved = true;
        for (NamedProjectBlock block : blocks) {
            if(!block.isNew() && !block.isHidden()) {
                allNormalBlocksAreApproved = allNormalBlocksAreApproved && isBlockApproved(block);
                if (!block.isComplete()) {
                    return false;
                }
            }
        }

        //If it gets at this point, all normal blocks are completed
        boolean atLeastOneNewBlockAndAllUncompleted = false;
        if(allNormalBlocksAreApproved) {
            for (NamedProjectBlock block : blocks) {
                if(block.isNew()) {
                    atLeastOneNewBlockAndAllUncompleted = true;
                    if (block.isComplete()) {
                        return true;//At least one new block is complete
                    }
                }
            }
        }
        //if it gets at this point, all blocks are completed and either there no new block or
        //all new blocks are uncompleted
        return !atLeastOneNewBlockAndAllUncompleted;
    }

    private boolean isBlockApproved(NamedProjectBlock block) {
        return NamedProjectBlock.BlockStatus.LAST_APPROVED.equals(block.getBlockStatus())
                || NamedProjectBlock.BlockStatus.APPROVED.equals(block.getBlockStatus());
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
        if(blocks != null) {
            return blocks.stream()
                    .filter(b-> b.getBlockType().equals(blockType))
                    .filter(b-> displayOrder == null
                            || b.getDisplayOrder().equals(displayOrder))
                    .collect(Collectors.toList());
        } else {
            return Collections.emptyList();
        }
    }

    /**
     * Finds a block with the given type, display order and status. Returns null if not found.
     */
    public NamedProjectBlock getBlockByTypeDisplayOrderAndStatus(ProjectBlockType blockType, Integer displayOrder, NamedProjectBlock.BlockStatus status) {
        return getBlocksByTypeAndDisplayOrder(blockType, displayOrder).stream()
                .filter(b -> b.getBlockStatus().equals(status))
                .findFirst()
                .orElse(null);
    }

    public NamedProjectBlock getSingleBlockByType(ProjectBlockType block) {
        List<NamedProjectBlock> blocksByType = getBlocksByType(block);
        if (blocksByType.size() != 1) {
            throw new ValidationException(String.format("Unable to retrieve single block of type: %s for project: %s found %d blocks", block.name(), this.getTitle(),blocksByType.size()));
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
                .filter(b -> b.latestVersion.equals(true))
                .findFirst()
                .orElse(null);
    }


    public Set<ProjectState> getAllowedTransitions() {
        return allowedTransitions;
    }

    public void setAllowedTransitions(Set<ProjectState> allowedTransitions) {
        this.allowedTransitions = allowedTransitions;
    }

    public StateModel getStateModel() {
        return stateModel;
    }

    public void setStateModel(StateModel stateModel) {
        this.stateModel = stateModel;
    }

    public boolean isPendingPayments() {
        return pendingPayments;
    }

    public void setPendingPayments(boolean pendingPayments) {
        this.pendingPayments = pendingPayments;
    }

    @JsonProperty("hasReclaimedPayments")
    public boolean hasReclaimedPayments() {
        return reclaimedPayments;
    }

    public void setReclaimedPayments(boolean reclaimedPayments) {
        this.reclaimedPayments = reclaimedPayments;
    }

    public List<ApiErrorItem> getMessages() {
        return messages;
    }

    public void setMessages(List<ApiErrorItem> messages) {
        this.messages = messages;
    }

    public List<Action> getAllowedActions() {
        return allowedActions;
    }

    public void setAllowedActions(List<Action> allowedActions) {
        this.allowedActions = allowedActions;
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
        List<ProjectQuestionsBlock> list =
                getProjectBlocks().stream().
                        filter(namedProjectBlock -> namedProjectBlock.getBlockType().equals(ProjectBlockType.Questions)).
                        map(namedProjectBlock -> (ProjectQuestionsBlock) namedProjectBlock).collect(Collectors.toList());
        return list;
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
        }
        else if (getDeveloperLedGrantBlock() != null) {
            return getDeveloperLedGrantBlock();
        }
        else if (getIndicativeGrantBlock() != null) {
            return getIndicativeGrantBlock();
        }
        else if (getNegotiatedGrantBlock() != null) {
            return getNegotiatedGrantBlock();
        }
        else {
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
    public ProgressUpdateBlock getProgressUpdatesBlock() {
        return (ProgressUpdateBlock) getSingleLatestBlockOfType(ProjectBlockType.ProgressUpdates);
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

    public Set<Label> getLabels() {
        return labels;
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

    public void recalculateProjectGrantEligibility() {
        this.totalGrantEligibility = null;

        for (NamedProjectBlock block : getLatestProjectBlocks()) {
            if (block instanceof BaseGrantBlock) {
                BaseGrantBlock grantBlock = (BaseGrantBlock)block;
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
        return blocks.stream().filter(b -> NamedProjectBlock.BlockStatus.LAST_APPROVED.equals(b.getBlockStatus())).findFirst().orElse(null);
    }

    @JsonIgnore
    public List<NamedProjectBlock> getLatestApprovedBlocks() {
        return projectBlocks.stream().filter(b -> NamedProjectBlock.BlockStatus.LAST_APPROVED.equals(b.getBlockStatus())).sorted().collect(Collectors.toList());
    }

    private Set<NamedProjectBlock> getBlocks(final BiFunction<
            NamedProjectBlock, NamedProjectBlock, Boolean>conditionToAddBlock) {
        Map<String, NamedProjectBlock> latestBlocks = new HashMap<>();

        for (NamedProjectBlock block : getProjectBlocks()) {
            String key = block.versionAgnosticKey();
            if (latestBlocks.containsKey(key)) {
                if (conditionToAddBlock.apply(block,latestBlocks.get(key))) {
                    latestBlocks.put(key,block);
                }
            }
            else if (!block.isHidden()) {
                latestBlocks.put(key, block);
            }
        }

        return new HashSet<>(latestBlocks.values());
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Project project = (Project) o;

        return !(id != null ? !id.equals(project.id) : project.id != null);

    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "Project{" +
                "id=" + id +
                ", title='" + title + '\'' +
                '}';
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
        return this.getBlocksByTypeAndDisplayOrder(type, displayOrder).stream().filter(b -> !b.getBlockStatus().equals(APPROVED)).collect(Collectors.toList());

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

    /**
     * Returns the ProjectState for the project.
     */
    public ProjectState currentState() {
        ProjectState state = new ProjectState(
                getStatusName(),
                getSubStatusName());
        if (ProjectStatus.Assess.name().equals(getStatusName())) {
            if (getSubStatusName() != null) {
                state.setSubStatus(ProjectSubStatus.Recommended);
            }
        }
        return state;

    }

    public Integer getAdvancePaymentAmount() {
        OutputsCostsBlock costs = (OutputsCostsBlock) this.getSingleLatestBlockOfType(ProjectBlockType.OutputsCosts);
        return costs == null ? null : costs.getAdvancePayment();
    }



    public void approveBlock(final NamedProjectBlock block,
                             final String username,
                             final OffsetDateTime approvalTime) {
        final List<NamedProjectBlock> previousApprovedBlocks = getBlocksByTypeAndDisplayOrder(
                block.getBlockType(), block.getDisplayOrder());

        //Mark the previous LAST_APPROVED as APPROVED
        if(previousApprovedBlocks != null) {
            previousApprovedBlocks.stream()
                        .filter(b-> !b.getId().equals(block.getId()))
                    .filter(b-> LAST_APPROVED.equals(b.getBlockStatus()))
                    .forEach(b-> {
                        b.setBlockStatus(APPROVED);
                        b.setReportingVersion(false);
                    });
        }
        if (block.getProject().getStateModel().isReportOnLastApproved()) {
            block.setReportingVersion(true);
        }
        if(block.isApproved()) {
            block.approve(block.getApproverUsername(), block.getApprovalTime());
        }
        else {
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
    public Map<GrantType, Long> getGrantsRequested() {

        Map<GrantType, Long> existingRequests = new HashMap<>();
        existingRequests.put(GrantType.Grant, 0L);
        existingRequests.put(GrantType.RCGF, 0L);
        existingRequests.put(GrantType.DPF, 0L);

        for (NamedProjectBlock latestProjectBlock : this.getLatestProjectBlocks()) {
            if (latestProjectBlock instanceof FundingSourceProvider) {
                Map<GrantType, Long> fundingRequested = ((FundingSourceProvider) latestProjectBlock).getFundingRequested();
                fundingRequested.forEach((key, value) -> existingRequests.merge(key, value, (v1, v2) -> v1 + v2));

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
    public boolean getApprovalWillCreatePendingPayment() {
        if (ProjectStatus.Active.equals(this.getStatusType())) {
            for (NamedProjectBlock block: getLatestProjectBlocks()) {
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
        if (ProjectStatus.Active.equals(this.getStatusType())) {
            for (NamedProjectBlock block: getLatestProjectBlocks()) {
                if (block.getApprovalWillCreatePendingGrantPayment()) {
                    return true;
                }
            }
        }

        return false;
    }

    @Transient
    public boolean getMonetaryValueReclaimRequired() {
        return template.getMilestoneType().equals(MonetaryValue) && getGrantSourceAdjustmentAmount().signum() < 0;
    }

    @Transient
    public boolean getApprovalWillCreatePendingReclaim() {
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
        if (oldLearningGrantBlock != null && NamedProjectBlock.BlockStatus.UNAPPROVED.equals(newLearningGrantBlock.getBlockStatus())) {
            BigDecimal oldAllocation = Optional.ofNullable(oldLearningGrantBlock.getTotalYearlyAllocation()).orElse(BigDecimal.ZERO);
            BigDecimal newAllocation = Optional.ofNullable(newLearningGrantBlock.getTotalYearlyAllocation()).orElse(BigDecimal.ZERO);
            return newAllocation.subtract(oldAllocation);
        }

        return BigDecimal.ZERO;
    }

    @Deprecated
    public boolean isAutoApproval() {
        return this.getStateModel().equals(StateModel.AutoApproval);
    }

    @JsonIgnore
    public OrganisationGroup.Type getOrganisationGroupType() {
        return organisationGroup != null ? organisationGroup.getType() : null;
    }

    public void setPendingContractSignature(boolean pendingContractSignature) {
        this.pendingContractSignature = pendingContractSignature;
    }

    public boolean isPendingContractSignature() {
        return this.pendingContractSignature;
    }
    

    @JsonIgnore
    public Set<NamedProjectBlock> getLatestProjectBlocks() {
        return latestProjectBlocks;
    }

    public Set<InternalProjectBlock> getInternalBlocks() {
        return internalBlocks;
    }

    public void setInternalBlocks(Set<InternalProjectBlock> internalBlocks) {
        this.internalBlocks = internalBlocks;
    }

    public void addBlockToProject(NamedProjectBlock block) {
        if (!block.isHidden()) {
            this.getLatestProjectBlocks().add(block);
        }
        this.getProjectBlocks().add(block);

    }

    public void addLabel(Label label){
        labels.add(label);
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
        for (NamedProjectBlock projectBlock : projectBlocks) {
            if (projectBlock.getLockDetails() != null) {
                return true;
            }
        }
        return false;
    }

    public boolean isReclaimEnabled() {
        return isReclaimEnabled;
    }

    public void setReclaimEnabled(boolean reclaimEnabled) {
        isReclaimEnabled = reclaimEnabled;
    }

    public InternalProjectBlock getInternalBlockById(Integer blockId) {
        return internalBlocks.stream().filter(b -> b.getId().equals(blockId)).findFirst().orElse(null);
    }

    public InternalProjectBlock getInternalBlockByType(InternalBlockType type) {
        return internalBlocks.stream().filter(b -> b.getType().equals(type)).findFirst().orElse(null);
    }

    public List<InternalProjectBlock> getInternalBlocksSorted() {
        return internalBlocksSorted;
    }

    public void setInternalBlocksSorted(List<InternalProjectBlock> internalBlocksSorted) {
        this.internalBlocksSorted = internalBlocksSorted;
    }

    @JsonIgnore
    public InternalAssessmentBlock getInternalAssessmentBlock() {
        return (InternalAssessmentBlock) getInternalBlockByType(InternalBlockType.Assessment);
    }

    @JsonIgnore
    public ProgrammeTemplate getProgrammeTemplate() {
        return this.programme.getProgrammeTemplateByTemplateID(this.template.getId());
    }

    public NamedProjectBlock getSingleBlockByDisplayOrder(Integer displayOrder) {
        return projectBlocks.stream().filter(b -> displayOrder.equals(b.getDisplayOrder()))
            .findFirst().orElse(null);
    }

    public List<String> getLabelNamesByType(String type){
        List<String> labelsName = new ArrayList<>();

        if(type.equalsIgnoreCase(LabelType.Custom.name())) {
            labelsName = labels.stream()
                  .filter(l -> l.getType().name().equalsIgnoreCase(type))
                  .map(label -> label.getText())
                  .collect(Collectors.toList());
        }

        if(type.equalsIgnoreCase(LabelType.Predefined.name())) {
            labelsName = labels.stream()
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

}
