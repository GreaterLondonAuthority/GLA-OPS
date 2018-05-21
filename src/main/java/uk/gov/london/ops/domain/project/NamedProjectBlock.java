/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.domain.project;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import uk.gov.london.ops.OpsEvent;
import uk.gov.london.ops.aop.LogMetrics;
import uk.gov.london.ops.domain.template.TemplateBlock;
import uk.gov.london.ops.exception.ApiErrorItem;
import uk.gov.london.ops.spe.SimpleProjectExportConfig;
import uk.gov.london.ops.util.jpajoins.Join;
import uk.gov.london.ops.util.jpajoins.JoinData;

import javax.persistence.*;
import java.time.OffsetDateTime;
import java.util.*;

import static uk.gov.london.ops.domain.project.NamedProjectBlock.BlockStatus.LAST_APPROVED;
import static uk.gov.london.ops.domain.project.NamedProjectBlock.BlockStatus.UNAPPROVED;

/**
 * Abstract base class for different project block types.
 */
@Entity(name = "project_block")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name="block_type")
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = BaseGrantBlock.class),
        @JsonSubTypes.Type(value = GrantSourceBlock.class),
        @JsonSubTypes.Type(value = ProjectMilestonesBlock.class),
        @JsonSubTypes.Type(value = ProjectQuestionsBlock.class),
        @JsonSubTypes.Type(value = OutputsBlock.class),
        @JsonSubTypes.Type(value = ReceiptsBlock.class),
        @JsonSubTypes.Type(value = ProjectDetailsBlock.class),
        @JsonSubTypes.Type(value = DesignStandardsBlock.class),
        @JsonSubTypes.Type(value = ProjectBudgetsBlock.class),
        @JsonSubTypes.Type(value = ProjectRisksBlock.class)
})
@DiscriminatorValue("BASE")
public abstract class NamedProjectBlock implements Comparable, ComparableItem {

    public enum BlockStatus {UNAPPROVED, APPROVED, LAST_APPROVED}

    public enum Action {APPROVE, DELETE, EDIT}

    @Id
    @JoinData(joinType = Join.JoinType.Complex, sourceTable = "project_block",
            comment = "This id is shared amongst all child blocks, for example tenure_block, design_standards, outputs,project_details_block etc")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "project_block_seq_gen")
    @SequenceGenerator(name = "project_block_seq_gen", sequenceName = "project_block_seq", initialValue = 10000, allocationSize = 1)
    private Integer id;

    @Column(name = "last_modified")
    protected OffsetDateTime lastModified;

    @Column(name = "modified_by")
    private String modifiedBy;

    @Column(name = "block_display_name")
    protected String blockDisplayName;

    @Column(name = "project_block_type")
    @Enumerated(EnumType.STRING)
    protected ProjectBlockType blockType;

    @Column(name = "block_status")
    @Enumerated(EnumType.STRING)
    protected BlockStatus blockStatus = BlockStatus.UNAPPROVED;

    @Column(name = "approver_name")
    protected String approverUsername;

    @Column(name = "approval_timestamp")
    protected OffsetDateTime approvalTime;

    @Column(name = "display_order")
    protected Integer displayOrder;

    @Column(name = "version_number")
    protected Integer versionNumber = 1;

    @Column(name = "reporting_version")
    protected Boolean reportingVersion = true;

    @Column(name = "latest_version")
    protected Boolean latestVersion = true;

    @ManyToOne
    @JoinColumn(name = "project_id")
    protected Project project;

    @OneToOne(cascade = CascadeType.ALL, mappedBy = "block", orphanRemoval = true)
    private LockDetails lockDetails;

    @Column(name = "block_appears_on_status")
    private Project.Status blockAppearsOnStatus;

    @Column(name = "hidden")
    private boolean hidden;

    @Column(name = "info_message")
    private String infoMessage;

    @Transient
    protected Map<String, List<ApiErrorItem>> errors;

    @Transient
    protected Collection<Action> allowedActions;

    @Transient
    private boolean newBlock= false;

    @Transient
    private String modifiedByName;

    @Transient
    private String approvedByName;

    @Transient
    private ProjectDifferences differences;

    public NamedProjectBlock() {
    }

    public NamedProjectBlock(Project project) {
        setProject(project);
    }

    @JsonIgnore
    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public boolean isValid() {
        return getValidationFailures().size() == 0;
    }

    public void updateNewBlock() {
        this.setNewBlock(
                this.getBlockAppearsOnStatus() != null
                        && project.getStatus().equals(this.getBlockAppearsOnStatus())
                        && UNAPPROVED.equals(this.blockStatus)
                        && this.versionNumber == 1);
    }

    public abstract boolean isComplete();

    /**
     * Merges values from another project structure into the block.
     * <p>
     * Any values not in the block should be ignored.
     */
    public void merge(NamedProjectBlock block) {
    }


    /**
     * Retrieves a list of validation failures specifically for the UI, the key
     * is meaningful to the UI, so could be the block etc of the page that the
     * error refers to, then for each key a list of appropriate errors
     *
     * @return Map of keys to errors
     */
    public Map<String, List<ApiErrorItem>> getValidationFailures() {
        if (errors == null) {
            errors = new HashMap<>();
            generateValidationFailures();
        }
        return errors;
    }

    // Internal method for each subclass to generate the list of validation errors for current object state.
    protected abstract void generateValidationFailures();

    protected void addErrorMessage(String key, String field, String description) {
        if (errors == null) {
            errors = new HashMap<>();
        }
        ApiErrorItem item = new ApiErrorItem(field, description);
        errors.putIfAbsent(key, new ArrayList<>());
        errors.get(key).add(item);
    }


    public OffsetDateTime getLastModified() {
        return lastModified;
    }

    public void setLastModified(OffsetDateTime lastModified) {
        this.lastModified = lastModified;
    }

    public String getModifiedBy() {
        return modifiedBy;
    }

    public void setModifiedBy(String modifiedBy) {
        this.modifiedBy = modifiedBy;
    }

    @JsonIgnore
    public boolean isVisited() {
        return lastModified != null;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getBlockDisplayName() {
        return blockDisplayName;
    }

    public void setBlockDisplayName(String blockDisplayName) {
        this.blockDisplayName = blockDisplayName;
    }

    public ProjectBlockType getBlockType() {
        return blockType;
    }

    public void setBlockType(ProjectBlockType blockType) {
        this.blockType = blockType;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }

    public Boolean isReportingVersion() {
        return reportingVersion;
    }

    public void setReportingVersion(Boolean reportingVersion) {
        this.reportingVersion = reportingVersion;
    }

    public Boolean isLatestVersion() {
        return latestVersion;
    }

    public void setLatestVersion(Boolean latestVersion) {
        this.latestVersion = latestVersion;
    }

    @Override
    public int compareTo(Object o) {
        if (this == o) {
            return 0;
        }
        if (o instanceof NamedProjectBlock) {
            NamedProjectBlock other = (NamedProjectBlock) o;
            if (this.getDisplayOrder() != null) {
                return this.getDisplayOrder().compareTo(other.getDisplayOrder());
            }
        }
        return 0;
    }

    public BlockStatus getBlockStatus() {
        return blockStatus;
    }

    public void setBlockStatus(BlockStatus blockStatus) {
        this.blockStatus = blockStatus;
    }

    public String getApproverUsername() {
        return approverUsername;
    }

    public void setApproverUsername(String approverUsername) {
        this.approverUsername = approverUsername;
    }

    public OffsetDateTime getApprovalTime() {
        return approvalTime;
    }

    public void setApprovalTime(OffsetDateTime approvalTime) {
        this.approvalTime = approvalTime;
    }

    public Integer getVersionNumber() {
        return versionNumber;
    }

    public void setVersionNumber(Integer versionNumber) {
        this.versionNumber = versionNumber;
    }

    @JsonIgnore
    public boolean isEditable() {
        return allowedActions != null && allowedActions.contains(Action.EDIT);
    }

    public Collection<Action> getAllowedActions() {
        return allowedActions;
    }

    public void setAllowedActions(Collection<Action> allowedActions) {
        this.allowedActions = allowedActions;
    }

    public Integer getProjectId() {

        return project == null ? null : project.getId();
    }

    public LockDetails getLockDetails() {
        return lockDetails;
    }

    public void setLockDetails(LockDetails lockDetails) {
        this.lockDetails = lockDetails;
        if (lockDetails != null) {
            lockDetails.setBlock(this);
        }
    }

    public boolean isNewBlock() {
        return newBlock;
    }

    public void setNewBlock(boolean newBlock) {
        this.newBlock = newBlock;
    }

    public Project.Status getBlockAppearsOnStatus() {
        return blockAppearsOnStatus;
    }

    public void setBlockAppearsOnStatus(Project.Status blockAppearsOnStatus) {
        this.blockAppearsOnStatus = blockAppearsOnStatus;
    }

    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    public String getInfoMessage() {
        return infoMessage;
    }

    public void setInfoMessage(String infoMessage) {
        this.infoMessage = infoMessage;
    }

    public String getModifiedByName() {
        return modifiedByName;
    }

    public void setModifiedByName(String modifiedByName) {
        this.modifiedByName = modifiedByName;
    }

    public String getApprovedByName() {
        return approvedByName;
    }

    public void setApprovedByName(String approvedByName) {
        this.approvedByName = approvedByName;
    }

    public ProjectDifferences getDifferences() {
        return differences;
    }

    public void setDifferences(ProjectDifferences differences) {
        this.differences = differences;
    }

    @JsonIgnore
    public Map<String, Object> simpleDataExtract(SimpleProjectExportConfig simpleProjectExportConfig) {
        return new HashMap<>();
    }

    /**
     * Makes a copy of the block.
     *
     * The block ID is not copied, so the clone will not have a block ID.
     * The cloned block status will be set to UNAPPROVED. The version number will not be cloned.
     *
     * Subclasses cannot override this as it is final. They should override createCloneInstance()
     * and copyBlockContentInto() instead.
     */
    public final NamedProjectBlock cloneBlock(String modifiedBy, OffsetDateTime modifiedOn)  {
        NamedProjectBlock clone = createCloneInstance();
        populateCommonBlockData(clone, modifiedBy, modifiedOn);
        copyBlockContentInto(clone);
        return clone;
    }

    /**
     * Subclasses can override this method if they need explicit control over creating a clone instance.
     */
    protected NamedProjectBlock createCloneInstance() {
        return getBlockType().newProjectBlockInstance();
    }

    protected final void populateCommonBlockData(NamedProjectBlock target, String modifiedBy, OffsetDateTime modifiedOn) {
        // Project is mutable, so clone will share the reference. It is safe as it is the parent of the block.
        target.setProject(getProject());

        target.setBlockType(getBlockType());
        target.setDisplayOrder(getDisplayOrder());
        target.setBlockDisplayName(getBlockDisplayName());
        target.setBlockStatus(NamedProjectBlock.BlockStatus.UNAPPROVED);
        target.setBlockAppearsOnStatus(getBlockAppearsOnStatus());
        target.setVersionNumber(getVersionNumber() + 1);
        target.setApprovalTime(null);
        target.setApproverUsername(null);
        target.setLatestVersion(true);
        target.setModifiedBy(modifiedBy);
        target.setLastModified(modifiedOn);
    }

    /**
     * Subclasses should override this method to ensure all block content is copied during cloning.
     */
    protected void copyBlockContentInto(NamedProjectBlock target) {
        // Subclasses can override as necessary
    }

    /**
     * Finds the differences between two NamedProjectBlock instances.
     *
     * Most effective when comparing different versions of the same block.
     *
     * NamedProjectBlock subclasses should override compareBlockSpecificContent() as necessary.
     */
    @LogMetrics
    public ProjectDifferences compareContent(NamedProjectBlock other) {
        ProjectDifferences result = new ProjectDifferences();

        if (other != null) {
            result.setDifferentBlockTypes( ! this.getBlockType().equals(other.getBlockType()) );
            result.setDifferentVersions( ! this.getVersionNumber().equals( other.getVersionNumber()));
            result.setDifferentProjects( ! this.getProjectId().equals( other.getProjectId()));

            if (result.shouldCompareProperties()) {
                compareBlockSpecificContent(other, result);
            }
        }

        return result;
    }

    protected void compareBlockSpecificContent(NamedProjectBlock other, ProjectDifferences differences) {

    }

    /**
     * Initialise the new project block using the template block configuration.
     */
    public final void initFromTemplate(TemplateBlock templateBlock) {
        this.setBlockType(templateBlock.getBlock());
        this.setDisplayOrder(templateBlock.getDisplayOrder());
        this.setBlockDisplayName(templateBlock.displayName());
        this.setBlockAppearsOnStatus(templateBlock.getBlockAppearsOnStatus());
        this.setInfoMessage(templateBlock.getInfoMessage());
        this.initFromTemplateSpecific(templateBlock);
    }

    protected void initFromTemplateSpecific(TemplateBlock templateBlock) {
        // subclasses should override as necessary
    }

    /**
     * Returns a string representing the block type and display order.
     *
     * Two versions of the same block should have the same key.
     */
    @JsonIgnore
    public String versionAgnosticKey() {
        if (displayOrder == null) {
            return getBlockType().name();
        } else {
            return getBlockType().name() + ":" + displayOrder;
        }
    }

    public boolean allowMultipleVersions() {
        return false;
    }

    public void approve(String username, OffsetDateTime approvalTime) {
        this.setBlockStatus(LAST_APPROVED);
        this.setLastModified(approvalTime);
        this.setApprovalTime(approvalTime);
        this.setApproverUsername(username);
    }

    public boolean isApproved() {
        return !UNAPPROVED.equals(blockStatus);
    }

    public boolean editRequiresCloning(OffsetDateTime now) {
        if (!Project.Status.Active.equals(project.getStatus())) {
            return false;
        }

        if (blockStatus.equals(NamedProjectBlock.BlockStatus.UNAPPROVED)) {
            return project.isAutoApproval() && (lastModified != null && (!now.getMonth().equals(lastModified.getMonth()) || now.getYear() != lastModified.getYear()));
        }

        return this.equals(project.getLatestBlockOfType(blockType, displayOrder));
    }

    @Override
    public String getComparisonId() {
        return this.getBlockType() + ":" + this.getDisplayOrder();
    }

    public void handleEvent(OpsEvent opsEvent) {

    }
}