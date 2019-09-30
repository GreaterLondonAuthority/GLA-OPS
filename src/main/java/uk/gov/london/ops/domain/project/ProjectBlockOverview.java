/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.domain.project;

import com.fasterxml.jackson.annotation.JsonIgnore;
import uk.gov.london.ops.domain.organisation.Organisation;
import uk.gov.london.ops.domain.project.state.ProjectStatus;
import uk.gov.london.ops.framework.jpa.NonJoin;

import javax.persistence.*;

/**
 * Project and block data overview, lightweight version
 */
@Entity(name = "v_project_overview")
@NonJoin("Overview entity, does not provide join information")
public class ProjectBlockOverview  {

    @Id
    @Column(name = "block_id")
    Integer projectBlockId;

    @Column(name = "project_id")
    Integer projectId;

    @Column(name="title")
    String title;

    @Column(name = "block_display_name")
    protected String blockDisplayName;

    @Column(name = "project_block_type")
    @Enumerated(EnumType.STRING)
    protected ProjectBlockType blockType;

    @Column(name = "block_status")
    @Enumerated(EnumType.STRING)
    protected NamedProjectBlock.BlockStatus blockStatus = NamedProjectBlock.BlockStatus.UNAPPROVED;

    @Column(name = "display_order")
    protected Integer displayOrder;

    @Column(name = "version_number")
    protected Integer versionNumber = 1;

    @Column(name = "block_appears_on_status")
    private String blockAppearsOnStatus;

    @Column(name = "recommendation")
    @Enumerated(EnumType.STRING)
    protected Project.Recommendation recommendation;

    @Column(name = "locked_by")
    private String lockedBy;

    @Column(name = "hidden")
    private boolean hidden;

    @Column(name = "is_new")
    private boolean isNew;

    @Column(name = "block_marked_complete")
    private Boolean blockMarkedComplete;

    @ManyToOne(cascade = {})
    @JoinColumn(name = "org_id", nullable = false)
    private Organisation organisation;

    @JsonIgnore
    @ManyToOne(cascade = {})
    @JoinColumn(name = "managing_organisation_id")
    private Organisation managingOrganisation;

    @Column(name = "status")
    protected String statusName = ProjectStatus.Draft.name();

    @Column(name = "substatus")
    protected String subStatusName;

    @Column(name = "marked_for_corporate")
    private boolean markedForCorporate;

    @Column(name = "info_message")
    private String infoMessage;

    @Column(name = "template_id")
    private Integer templateId;

    @Column(name = "programme_id")
    private Integer programmeId;

    public Integer getProjectBlockId() {
        return projectBlockId;
    }

    public void setProjectBlockId(Integer projectBlockId) {
        this.projectBlockId = projectBlockId;
    }

    public Integer getProjectId() {
        return projectId;
    }

    public void setProjectId(Integer projectId) {
        this.projectId = projectId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
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

    public NamedProjectBlock.BlockStatus getBlockStatus() {
        return blockStatus;
    }

    public void setBlockStatus(NamedProjectBlock.BlockStatus blockStatus) {
        this.blockStatus = blockStatus;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }

    public Integer getVersionNumber() {
        return versionNumber;
    }

    public void setVersionNumber(Integer versionNumber) {
        this.versionNumber = versionNumber;
    }

    public String getBlockAppearsOnStatus() {
        return blockAppearsOnStatus;
    }

    public void setBlockAppearsOnStatus(String blockAppearsOnStatus) {
        this.blockAppearsOnStatus = blockAppearsOnStatus;
    }

    public String getLockedBy() {
        return lockedBy;
    }

    public void setLockedBy(String lockedBy) {
        this.lockedBy = lockedBy;
    }

    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    public boolean isNew() {
        return isNew;
    }

    public void setNew(boolean aNew) {
        isNew = aNew;
    }

    public Boolean getBlockMarkedComplete() {
        return blockMarkedComplete;
    }

    public void setBlockMarkedComplete(Boolean blockMarkedComplete) {
        this.blockMarkedComplete = blockMarkedComplete;
    }

    public Organisation getOrganisation() {
        return organisation;
    }

    public void setOrganisation(Organisation organisation) {
        this.organisation = organisation;
    }

    public Organisation getManagingOrganisation() {
        return managingOrganisation;
    }

    public void setManagingOrganisation(Organisation managingOrganisation) {
        this.managingOrganisation = managingOrganisation;
    }

    public String getStatusName() {
        return statusName;
    }

    public void setStatusName(String statusName) {
        this.statusName = statusName;
    }

    public String getSubStatusName() {
        return subStatusName;
    }

    public void setSubStatusName(String subStatusName) {
        this.subStatusName = subStatusName;
    }

    public Project.Recommendation getRecommendation() {
        return recommendation;
    }

    public void setRecommendation(Project.Recommendation recommendation) {
        this.recommendation = recommendation;
    }

    public boolean isMarkedForCorporate() {
        return markedForCorporate;
    }

    public void setMarkedForCorporate(boolean markedForCorporate) {
        this.markedForCorporate = markedForCorporate;
    }


    public String getInfoMessage() {
        return infoMessage;
    }

    public void setInfoMessage(String infoMessage) {
        this.infoMessage = infoMessage;
    }

    public Integer getTemplateId() {
        return templateId;
    }

    public void setTemplateId(Integer templateId) {
        this.templateId = templateId;
    }

    public Integer getProgrammeId() {
        return programmeId;
    }

    public void setProgrammeId(Integer programmeId) {
        this.programmeId = programmeId;
    }
}
