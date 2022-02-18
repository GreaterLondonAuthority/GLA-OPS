/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.project;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.london.common.error.ApiErrorItem;
import uk.gov.london.ops.framework.annotations.PermissionRequired;
import uk.gov.london.ops.framework.jpa.Join;
import uk.gov.london.ops.framework.jpa.JoinData;
import uk.gov.london.ops.organisation.model.OrganisationEntity;
import uk.gov.london.ops.programme.domain.Programme;
import uk.gov.london.ops.programme.domain.ProgrammeSummary;
import uk.gov.london.ops.project.block.NamedProjectBlock;
import uk.gov.london.ops.project.block.ProjectBlockStatus;
import uk.gov.london.ops.project.internalblock.InternalProjectBlock;
import uk.gov.london.ops.project.label.Label;
import uk.gov.london.ops.project.state.*;

import javax.persistence.*;
import java.util.*;

import static javax.persistence.CascadeType.ALL;
import static uk.gov.london.ops.permission.PermissionType.PROJ_VIEW_INTERNAL_BLOCKS;
import static uk.gov.london.ops.permission.PermissionType.PROJ_VIEW_RECOMMENDATION;

@MappedSuperclass
public class BaseProject {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "project_seq_gen")
    @SequenceGenerator(name = "project_seq_gen", sequenceName = "project_seq", initialValue = 10000, allocationSize = 1)
    protected Integer id;

    @Transient
    protected String title;

    @Column(name = "status")
    protected String statusName = ProjectStatus.Draft.name();

    @Column(name = "substatus")
    protected String subStatusName;

    @Column(name = "recommendation")
    @Enumerated(EnumType.STRING)
    protected Project.Recommendation recommendation;

    @Transient
    private List<NamedProjectBlock> projectBlocksSorted = new ArrayList<>();

    @ManyToOne(cascade = {})
    @JoinColumn(name = "org_id", nullable = false)
    protected OrganisationEntity organisation;

    @JsonIgnore
    @ManyToOne(cascade = {})
    @JoinColumn(name = "managing_organisation_id")
    protected OrganisationEntity managingOrganisation;

    @Column(name = "marked_for_corporate")
    private boolean markedForCorporate;

    @Column(name = "info_message")
    private String infoMessage;

    @JsonIgnore
    @PermissionRequired(PROJ_VIEW_INTERNAL_BLOCKS)
    @JoinData(joinType = Join.JoinType.OneToMany, sourceTable = "project", sourceColumn = "id",
        targetColumn = "project_id", targetTable = "internal_project_block", comment = "")
    @OneToMany(cascade = {CascadeType.ALL}, mappedBy = "project", orphanRemoval = true, targetEntity = InternalProjectBlock.class)
    private Set<InternalProjectBlock> internalBlocks = new HashSet<>();

    @Column(name = "approval_will_generate_reclaim")
    private Boolean approvalWillGenerateReclaimPersisted;

    @Column(name = "approval_will_generate_payment")
    private Boolean approvalWillGeneratePaymentPersisted;

    @Column(name = "suspend_payments")
    private boolean suspendPayments = false;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @ManyToOne(cascade = {})
    @JoinColumn(name = "programme_id", nullable = false)
    private Programme programme;

    @Column(name = "state_model")
    @Enumerated(EnumType.STRING)
    private StateModel stateModel;

    @Transient
    private List<InternalProjectBlock> internalBlocksSorted = new ArrayList<>();

    @Transient
    private Set<ProjectState> allowedTransitions;

    @Transient
    private Integer templateId;

    @Transient
    private boolean ableToCalculateTransitions;

    @Transient
    private boolean isPreviouslySubmitted;

    @Transient
    private boolean isPreviouslyReturned;

    @Transient
    private boolean isFinanceEmailMissing;

    @Transient
    private List<ApiErrorItem> messages = new ArrayList<>();

    /** Flag showing if the current user is watching this project. */
    @Transient
    private boolean currentUserWatching;

    /** Number of users watching the project. */
    @Transient
    private Integer nbWatchers;

    @Transient
    private boolean enriched = false;

    @Transient
    private boolean pendingPayments;

    @Transient
    private boolean reclaimedPayments;

    @Transient
    private List<Project.Action> allowedActions = new ArrayList<>();

    @JoinData(joinType = Join.JoinType.OneToMany, sourceColumn = "id",
        targetColumn = "project_id", targetTable = "label",  comment = "")
    @OneToMany(fetch = FetchType.LAZY,  cascade = ALL, orphanRemoval = true, mappedBy = "projectId", targetEntity = Label.class)
    private Set<Label> labels = new HashSet<>();

    public Integer getId() {
        return id;
    }


    public void setId(Integer id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<NamedProjectBlock> getProjectBlocksSorted() {
        return projectBlocksSorted;
    }

    public void setProjectBlocksSorted(List<NamedProjectBlock> projectBlocksSorted) {
        this.projectBlocksSorted = projectBlocksSorted;
    }

    public String getTitle() {
        return title;
    }

    public OrganisationEntity getOrganisation() {
        return organisation;
    }

    public void setOrganisation(OrganisationEntity organisation) {
        this.organisation = organisation;
    }

    public Integer getManagingOrganisationId() {
        return managingOrganisation.getId();
    }

    public OrganisationEntity getManagingOrganisation() {
        return managingOrganisation;
    }

    public void setManagingOrganisation(OrganisationEntity managingOrganisation) {
        this.managingOrganisation = managingOrganisation;
    }


    public String getSubStatusName() {
        return subStatusName;
    }

    public void setSubStatusName(String subStatusName) {
        this.subStatusName = subStatusName;
    }

    public void setSubStatus(ProjectSubStatus subStatus) {
        if (subStatus != null) {
            setSubStatusName(subStatus.name());
        }
    }

    @JsonProperty(value = "statusType", access = JsonProperty.Access.READ_ONLY)
    public ProjectStatus getStatusType() {
        return ProjectStateEntity.getStatusType(statusName, subStatusName);
    }

    @JsonProperty(value = "subStatusType", access = JsonProperty.Access.READ_ONLY)
    public ProjectSubStatus getSubStatusType() {
        return ProjectStateEntity.getSubStatusType(statusName, subStatusName);
    }

    public void setStatusName(String statusName) {
        this.statusName = statusName;
        this.subStatusName = null;
    }

    public void setStatus(ProjectStatus status) {
        if (status != null) {
            setStatusName(status.name());
        }
    }

    public String getStatusName() {
        return statusName;
    }

    @PermissionRequired({PROJ_VIEW_RECOMMENDATION})
    public Project.Recommendation getRecommendation() {
        return recommendation;
    }

    public void setRecommendation(Project.Recommendation recommendation) {
        this.recommendation = recommendation;
        if (recommendation != null) {
            this.setSubStatus(ProjectSubStatus.Recommended);
        }
    }

    protected boolean isBlockApproved(NamedProjectBlock block) {
        return ProjectBlockStatus.LAST_APPROVED.equals(block.getBlockStatus())
                || ProjectBlockStatus.APPROVED.equals(block.getBlockStatus());
    }

    public boolean isComplete() {
        final List<NamedProjectBlock> blocks = getProjectBlocksSorted();
        return calculateIsComplete(blocks);
    }

    protected boolean calculateIsComplete(Collection<NamedProjectBlock> blocks) {
        boolean allNormalBlocksAreApproved = true;
        for (NamedProjectBlock block : blocks) {
            if (!block.isNew() && !block.isHidden()) {
                allNormalBlocksAreApproved = allNormalBlocksAreApproved && isBlockApproved(block);
                if (!block.isComplete()) {
                    return false;
                }
            }
        }

        //If it gets at this point, all normal blocks are completed

        boolean atLeastOneNewBlockAndAllUncompleted = false;
        if (allNormalBlocksAreApproved) {
            for (NamedProjectBlock block : this.getProjectBlocksSorted()) {
                if (block.isNew()) {
                    atLeastOneNewBlockAndAllUncompleted = true;
                    if (block.isComplete()) {
                        return true; //At least one new block is complete
                    }
                }
            }
        }
        return !atLeastOneNewBlockAndAllUncompleted;
    }

    public boolean isMarkedForCorporate() {
        return markedForCorporate;
    }

    public void setMarkedForCorporate(boolean markedForCorporate) {
        this.markedForCorporate = markedForCorporate;
    }

    public Integer getTemplateId() {
        return templateId;
    }

    public void setTemplateId(Integer templateId) {
        this.templateId = templateId;
    }

    public String getInfoMessage() {
        return infoMessage;
    }

    public void setInfoMessage(String infoMessage) {
        this.infoMessage = infoMessage;
    }

    public boolean isEnriched() {
        return enriched;
    }

    public void setEnriched(boolean enriched) {
        this.enriched = enriched;
    }

    public Set<InternalProjectBlock> getInternalBlocks() {
        return internalBlocks;
    }

    public void setInternalBlocks(Set<InternalProjectBlock> internalBlocks) {
        this.internalBlocks = internalBlocks;
    }

    public List<InternalProjectBlock> getInternalBlocksSorted() {
        return internalBlocksSorted;
    }

    public void setInternalBlocksSorted(List<InternalProjectBlock> internalBlocksSorted) {
        this.internalBlocksSorted = internalBlocksSorted;
    }

    public Boolean getApprovalWillGenerateReclaimPersisted() {
        return approvalWillGenerateReclaimPersisted;
    }

    public void setApprovalWillGenerateReclaimPersisted(Boolean approvalWillGenerateReclaimPersisted) {
        this.approvalWillGenerateReclaimPersisted = approvalWillGenerateReclaimPersisted;
    }

    public Boolean getApprovalWillGeneratePaymentPersisted() {
        return approvalWillGeneratePaymentPersisted;
    }

    public void setApprovalWillGeneratePaymentPersisted(Boolean approvalWillGeneratePaymentPersisted) {
        this.approvalWillGeneratePaymentPersisted = approvalWillGeneratePaymentPersisted;
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


    // method for JSON only
    public Integer getProgrammeId() {
        if (programme != null) {
            return programme.getId();
        }
        return null;
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

    public boolean isPreviouslySubmitted() {
        return isPreviouslySubmitted;
    }

    public void setPreviouslySubmitted(boolean previouslySubmitted) {
        isPreviouslySubmitted = previouslySubmitted;
    }

    public boolean isPreviouslyReturned() {
        return isPreviouslyReturned;
    }

    public void setPreviouslyReturned(boolean previouslyReturned) {
        isPreviouslyReturned = previouslyReturned;
    }

    public boolean isAbleToCalculateTransitions() {
        return ableToCalculateTransitions;
    }

    public void setAbleToCalculateTransitions(boolean ableToCalculateTransitions) {
        this.ableToCalculateTransitions = ableToCalculateTransitions;
    }

    @JsonIgnore
    public boolean anyBlocksLocked() {
        return projectBlocksSorted.stream().anyMatch(b -> b.getLockDetails() != null);
    }

    public List<Project.Action> getAllowedActions() {
        return allowedActions;
    }

    public void setAllowedActions(List<Project.Action> allowedActions) {
        this.allowedActions = allowedActions;
    }

    public boolean isPendingPayments() {
        return pendingPayments;
    }

    public void setPendingPayments(boolean pendingPayments) {
        this.pendingPayments = pendingPayments;
    }

    public boolean isSuspendPayments() {
        return suspendPayments;
    }

    public void setSuspendPayments(boolean suspendProjectPayments) {
        this.suspendPayments = suspendProjectPayments;
    }

    @JsonProperty("hasReclaimedPayments")
    public boolean hasReclaimedPayments() {
        return reclaimedPayments;
    }

    public void setReclaimedPayments(boolean reclaimedPayments) {
        this.reclaimedPayments = reclaimedPayments;
    }

    public Boolean getApprovalWillCreatePendingPayment() {
        return this.approvalWillGeneratePaymentPersisted;
    }

    public Boolean getApprovalWillCreatePendingReclaim() {
        return this.approvalWillGenerateReclaimPersisted;
    }

    public Set<Label> getLabels() {
        return labels;
    }

    public void setLabels(Set<Label> labels) {
        this.labels = labels;
    }

    public boolean isFinanceEmailMissing() {
        return (this.getOrganisation() != null
                && (this.getOrganisation().getFinanceContactEmail() == null || this.getOrganisation().getFinanceContactEmail().isEmpty()));
    }

}
