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
import uk.gov.london.ops.annotations.PermissionRequired;
import uk.gov.london.ops.domain.organisation.Organisation;
import uk.gov.london.ops.domain.organisation.OrganisationGroup;
import uk.gov.london.ops.domain.template.Programme;
import uk.gov.london.ops.domain.template.ProgrammeSummary;
import uk.gov.london.ops.domain.template.Template;
import uk.gov.london.ops.exception.ApiErrorItem;
import uk.gov.london.ops.exception.ValidationException;
import uk.gov.london.ops.service.ManagedEntityInterface;
import uk.gov.london.ops.service.project.state.ProjectState;
import uk.gov.london.ops.service.project.state.StateMachine;
import uk.gov.london.ops.spe.SimpleProjectExportConfig;
import uk.gov.london.ops.util.jpajoins.Join;
import uk.gov.london.ops.util.jpajoins.JoinData;

import javax.persistence.*;
import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import static uk.gov.london.ops.domain.project.NamedProjectBlock.BlockStatus.APPROVED;
import static uk.gov.london.ops.domain.project.NamedProjectBlock.BlockStatus.LAST_APPROVED;
import static uk.gov.london.ops.domain.template.Template.MilestoneType.MonetarySplit;
import static uk.gov.london.ops.service.PermissionService.PROJ_VIEW_RECOMMENDATION;
import static uk.gov.london.ops.spe.SimpleProjectExportConstants.FieldNames.*;
import static uk.gov.london.ops.util.GlaOpsUtils.nullSafeAdd;

@Entity
@JsonFilter("roleBasedFilter")
public class Project implements Serializable, ManagedEntityInterface {

    @Transient
    private final Logger log = LoggerFactory.getLogger(getClass());


    public enum Status {
        Draft, Submitted, Assess, Returned, Active, Closed
    }

    public enum SubStatus {
        Recommended, UnapprovedChanges, ApprovalRequested, PaymentAuthorisationPending, AbandonPending, Rejected, Abandoned, Completed
    }

    public enum Recommendation {
        RecommendApproval, RecommendRejection
    }

    public enum Action {
        ViewChangeReport, Transfer, Reinstate
    }

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "project_seq_gen")
    @SequenceGenerator(name = "project_seq_gen", sequenceName = "project_seq", initialValue = 10000, allocationSize = 1)
    private Integer id;

    @Transient
    private String title;

    @ManyToOne(cascade = {})
    @JoinColumn(name = "org_id", nullable = false)
    private Organisation organisation;

    @Column(name = "organisation_group_id")
    @JoinData(targetTable = "organisation_group", targetColumn = "id", joinType = Join.JoinType.OneToOne,
            comment = "The consortium/partnership owning this project.")
    private Integer organisationGroupId;


    @JsonIgnore
    @ManyToOne(cascade = {})
    @JoinColumn(name = "managing_organisation_id")
    private Organisation managingOrganisation;

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

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private Status status = Status.Draft;

    @Column(name = "substatus")
    @Enumerated(EnumType.STRING)
    private SubStatus subStatus;

    @Column(name = "recommendation")
    @Enumerated(EnumType.STRING)
    private Recommendation recommendation;

    @JoinData(joinType = Join.JoinType.OneToMany, sourceTable = "project", sourceColumn = "id", targetColumn = "project_id", targetTable = "project_block",
            comment = "")
    @OneToMany(cascade = {CascadeType.ALL}, mappedBy = "project", orphanRemoval = true, targetEntity = NamedProjectBlock.class)
    private Set<NamedProjectBlock> projectBlocks = new HashSet<>();

    @Transient
    private List<NamedProjectBlock> projectBlocksSorted;

    @OneToMany(fetch = FetchType.EAGER, targetEntity = NamedProjectBlock.class)
    @JoinColumn(name = "latest_for_project")
    private Set<NamedProjectBlock> latestProjectBlocks = new HashSet<>();

    @Column(name = "total_grant_eligibility")
    private Long totalGrantEligibility;

    @Column(name = "strategic_project")
    private Boolean strategicProject = false;

    @Column(name = "associated_projects_enabled")
    private boolean associatedProjectsEnabled;

    @Column(name = "info_message")
    private String infoMessage;

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



    public Project() {
    }

    public Project(Integer id, String title) {
        this.id = id;
        this.title = title;
    }

    public Integer getId() {
        return id;
    }

    public String getTitle() {
        String titleToUse = title;
        ProjectDetailsBlock newDetailsBlock = this.getDetailsBlock();
        if ( newDetailsBlock != null && newDetailsBlock.getTitle()!=null) {
            titleToUse = newDetailsBlock.getTitle() ;
        }
        return titleToUse;
    }

    public void setTitle(String title) {
        this.title = title;
        ProjectDetailsBlock newDetailsBlock = this.getDetailsBlock();
        if ( newDetailsBlock != null) {
            newDetailsBlock.setTitle(title);
        }
    }

    public Organisation getOrganisation() {
        return organisation;
    }

    public void setOrganisation(Organisation organisation) {
        this.organisation = organisation;
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

    public Status getStatus() {
        return status;
    }

    public void setState(ProjectState state) {
        if (state == null || state.getStatus() == null) {
            throw new ValidationException("Attempt to update project state with no new state.");
        }
        this.status = state.getStatus();
        this.subStatus = state.getSubStatus();
    }

    public void setStatus(Status status) {
        this.status = status;
        this.subStatus = null;
    }


    // method for JSON only
    public Integer getTemplateId() {
        return template.getId();
    }

    // method for JSON only
    public Integer getProgrammeId() {
        return programme.getId();
    }

    @PermissionRequired({PROJ_VIEW_RECOMMENDATION})
    public Recommendation getRecommendation() {
        return recommendation;
    }

    public void setRecommendation(Recommendation recommendation) {
        this.recommendation = recommendation;
        if(recommendation != null) {
            this.setSubStatus(SubStatus.Recommended);
        }
    }


    public SubStatus getSubStatus() {
        return subStatus;
    }

    public void setSubStatus(SubStatus subStatus) {
        this.subStatus = subStatus;
    }

    public boolean isComplete() {
        final Set<NamedProjectBlock> blocks = getLatestProjectBlocks();
        boolean allNormalBlocksAreApproved = true;
        for (NamedProjectBlock block : blocks) {
            if(!block.isNewBlock() && !block.isHidden()) {
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
                if(block.isNewBlock()) {
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
    public ProjectRisksBlock getRisksBlock() {
        return (ProjectRisksBlock) getSingleLatestBlockOfType(ProjectBlockType.Risks);
    }

    @JsonIgnore
    public UnitDetailsBlock getUnitDetailsBlock() {
        return (UnitDetailsBlock) getSingleLatestBlockOfType(ProjectBlockType.UnitDetails);
    }

    public Organisation getManagingOrganisation() {
        return managingOrganisation;
    }

    public void setManagingOrganisation(Organisation managingOrganisation) {
        this.managingOrganisation = managingOrganisation;
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

    public String getInfoMessage() {
        return infoMessage;
    }

    public void setInfoMessage(String infoMessage) {
        this.infoMessage = infoMessage;
    }

    public List<ProjectHistory> getHistory() {
        return history;
    }

    public void setHistory(List<ProjectHistory> history) {
        this.history = history;
    }

    @PreUpdate
    public void preSave() {
        recalculateProjectGrantEligibility();
        determineIfUnapprovedChanges();
    }

    private void recalculateProjectGrantEligibility() {
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
     * For active projects sets the subStatus, if the subStatus is other than
     * ApprovalRequested, by checking all the blocks to see if any of them
     * has un approved changes. If that is the case, the project subStatus
     * is set to UnapprovedChanges, otherwise to null.
     */
    protected void determineIfUnapprovedChanges() {
        if (Status.Active.equals(this.getStatus())
                && !SubStatus.ApprovalRequested.equals(this.getSubStatus())
                && !SubStatus.PaymentAuthorisationPending.equals(this.getSubStatus())
                && !SubStatus.AbandonPending.equals(this.getSubStatus())
                && !this.isAutoApproval()) {
            this.subStatus = hasUnapprovedBlocks() ? SubStatus.UnapprovedChanges : null;
        }
    }

    /**
     * @return true if any of the project blocks is unapproved, false otherwise.
     */
    public boolean hasUnapprovedBlocks() {
        for (NamedProjectBlock projectBlock : getProjectBlocks()) {
            if (!projectBlock.isNewBlock() && NamedProjectBlock.BlockStatus.UNAPPROVED.equals(projectBlock.getBlockStatus())) {
                return true;
            }
        }
        return hasNewEditedBlocks();
    }

    public boolean hasNewEditedBlocks() {
        for (NamedProjectBlock b : getProjectBlocks()) {
            if (b.isNewBlock() && b.getLastModified() != null) {
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
        projectBlocks.forEach(NamedProjectBlock::updateNewBlock);
        return projectBlocks;
    }

    @JsonIgnore
    @Transient
    public Set<NamedProjectBlock> getReportingVersionBlocks() {
        return projectBlocks.stream().filter(NamedProjectBlock::isReportingVersion).collect(Collectors.toSet());
    }

    public NamedProjectBlock getLatestApprovedBlock(ProjectBlockType type) {
        List<NamedProjectBlock> blocks = this.getBlocksByType(type);
        return blocks.stream().filter(b -> NamedProjectBlock.BlockStatus.LAST_APPROVED.equals(b.getBlockStatus())).findFirst().get();
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

    public List<NamedProjectBlock> getProjectBlocksSorted() {
        return projectBlocksSorted;
    }

    public void setProjectBlocksSorted(List<NamedProjectBlock> projectBlocksSorted) {
        this.projectBlocksSorted = projectBlocksSorted;
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

    public NamedProjectBlock getLatestBlockOfType(ProjectBlockType type, Integer displayOrder) {
        List<NamedProjectBlock> blocksByType = getBlocksByType(type);

        // Removed by SL.
        // If only one block, and displayOrder parameter is null, the for loop below will do just fine.
        // If displayOrder is not null and dosesn't match, then this code will erroneously return it.
        //
        //if (blocksByType.size() == 1) {
        //    return blocksByType.get(0);
        //}

        NamedProjectBlock latest = null;

        for (NamedProjectBlock next : blocksByType) {
            if(displayOrder == null || displayOrder.equals(next.getDisplayOrder())){
                if (latest == null || next.getVersionNumber() > latest.getVersionNumber()) {
                    latest = next;
                }
            }

        }

        return latest;
    }

    /**
     * Returns the ProjectState for the project.
     */
    public ProjectState currentState() {
        ProjectState state = new ProjectState(
                getStatus(),
                getSubStatus());
        if (Project.Status.Assess.equals(getStatus())) {
            if (getSubStatus() != null) {
                state.setSubStatus(SubStatus.Recommended);
            }
        }
        return state;

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
        if (!block.getProject().isAutoApproval()) {
            block.setReportingVersion(true);
        }
        block.approve(username, approvalTime);
    }

    void handleEvent(OpsEvent opsEvent) {
        if (EventType.MilestoneApproval.equals(opsEvent.getEventType())) {
            ProjectHistory history = new ProjectHistory(
                    ProjectHistory.HistoryEventType.MilestoneClaimApproved, opsEvent.getMessage());
            history.setExternalId(opsEvent.getExternalId());
            for (NamedProjectBlock namedProjectBlock : this.getLatestProjectBlocks()) {
                namedProjectBlock.handleEvent(opsEvent);
            }
            this.getHistory().add(history);
        }
    }

    @JsonIgnore
    public Map<GrantType, Long> getGrantsRequested() {
        GrantSourceBlock grantSource = getGrantSourceBlock();
        if (grantSource != null) {
            return grantSource.getGrantsRequested();
        }
        return null;
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
                Set<TenureTypeAndUnits> tenureTypeAndUnitsEntries = block.getTenureTypeAndUnitsEntries();
                for (TenureTypeAndUnits tenureTypeAndUnitsEntry : tenureTypeAndUnitsEntries) {
                    Integer calculatedTotalUnits = block.calculateTotalUnits(tenureTypeAndUnitsEntry);
                    Integer totalUnits = calculatedTotalUnits == null ? 0 : calculatedTotalUnits;
                    Integer externalId = tenureTypeAndUnitsEntry.getTenureType().getExternalId();
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


    @Transient
    public boolean getApprovalWillCreatePendingPayment() {
        ProjectMilestonesBlock milestonesBlock = getMilestonesBlock();
        if (milestonesBlock != null && Status.Active.equals(this.getStatus())) {
            return milestonesBlock.getApprovalWillCreatePendingPayment() ||
                    (MonetarySplit.equals(template.getMilestoneType()) && getGrantSourceAdjustmentAmount() > 0);
        }
        return false;
    }


    @Transient
    public boolean getApprovalWillCreatePendingReclaim() {
        ProjectMilestonesBlock milestonesBlock = getMilestonesBlock();

        GrantSourceBlock grantSourceBlock = getGrantSourceBlock();
        // pending reclaims can only happen on unapproved grant source
        if (grantSourceBlock != null && grantSourceBlock.isApproved()) {
            return false;
        }

        if (milestonesBlock != null) {
            for (Milestone milestone : milestonesBlock.getMilestones()) {
                if (milestone.getReclaimedGrant() != null && milestone.getReclaimedGrant() > 0) {
                    return true;
                }
                if (milestone.getReclaimedDpf() != null && milestone.getReclaimedDpf() > 0) {
                    return true;
                }
                if (milestone.getReclaimedRcgf() != null && milestone.getReclaimedRcgf() > 0) {
                    return true;
                }
            }
            // negative indicates new grant is less than old grant
            if (milestonesBlock.anyClaimedOrApprovedMilestones() && this.getGrantSourceAdjustmentAmount() < 0) {
                return true;
            }
        }


        return false;
    }

    @Transient
    public boolean getApprovalWillCreatePendingGrantPayment() {
        ProjectMilestonesBlock milestonesBlock = getMilestonesBlock();
        if (milestonesBlock != null && Status.Active.equals(this.getStatus())) {
            return milestonesBlock.anyClaimedOrApprovedMilestones() && ( milestonesBlock.getApprovalWillCreatePendingGrantPayment() ||
                    (MonetarySplit.equals(template.getMilestoneType()) && getGrantSourceAdjustmentAmount() != 0));
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
            // don't add grant it's unreliable until mentary value is sorted
//            map.put(GrantType.Grant, grantSourceBlock.getGrantValue());
        }
        return map;
    }

    public Long getGrantSourceAdjustmentAmount() {
        ProjectMilestonesBlock milestonesBlock = getMilestonesBlock();
        if (milestonesBlock != null && Status.Active.equals(this.getStatus())) {
            GrantSourceBlock grantSourceBlock = getGrantSourceBlock();
            if (grantSourceBlock != null && NamedProjectBlock.BlockStatus.UNAPPROVED.equals(grantSourceBlock.getBlockStatus())) {
                GrantSourceBlock approvedBlock = (GrantSourceBlock) getLatestApprovedBlock(ProjectBlockType.GrantSource);
                Long newGrant = grantSourceBlock.getGrantValue() == null ? 0 : grantSourceBlock.getGrantValue();
                Long oldGrant = approvedBlock.getGrantValue() == null ? 0 : approvedBlock.getGrantValue();
                return newGrant - oldGrant ;
            }
        }
        return 0L;
    }

    public boolean isAutoApproval() {
        return template.isAutoApproval();
    }

    @JsonIgnore
    public StateMachine getStateMachine() {
        if (this.isAutoApproval()) {
            return StateMachine.AUTO_APPROVAL;
        } else {
            return StateMachine.DEFAULT;
        }
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
        latestProjectBlocks.forEach(NamedProjectBlock::updateNewBlock);
        return latestProjectBlocks;//.stream().filter(npb -> !npb.isHidden()).collect(Collectors.toSet());
    }

    public void addBlockToProject(NamedProjectBlock block) {
        if (!block.isHidden()) {
            this.getLatestProjectBlocks().add(block);
        }
        this.getProjectBlocks().add(block);

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
}
