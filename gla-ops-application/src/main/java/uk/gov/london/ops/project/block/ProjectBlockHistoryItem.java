/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.project.block;

import java.time.OffsetDateTime;
import java.util.Set;
import uk.gov.london.ops.project.label.Label;

/**
 * Created by chris on 05/04/2017.
 */
public class ProjectBlockHistoryItem {

    private Integer projectId;

    private Integer blockId;

    private NamedProjectBlock.BlockStatus status;

    private Integer blockVersion;

    private OffsetDateTime versionUpdated;

    private String actionedBy;

    private String approvedOnStatus;

    private Set<Label> labels;

    public ProjectBlockHistoryItem(Integer projectId, Integer blockId, NamedProjectBlock.BlockStatus status, Integer blockVersion,
            OffsetDateTime versionUpdated, String modifiedBy, String actionedBy, String approvedOnStatus) {
        this(projectId, blockId, status, blockVersion, versionUpdated, null, approvedOnStatus, (Set) null);
        if (status.equals(NamedProjectBlock.BlockStatus.UNAPPROVED)) {
            this.actionedBy = modifiedBy;
        } else {
            this.actionedBy = actionedBy;
        }
    }

    public ProjectBlockHistoryItem(Integer projectId, Integer blockId, NamedProjectBlock.BlockStatus status, Integer blockVersion,
            OffsetDateTime versionUpdated, String actionedBy, String approvedOnStatus, Set<Label> labels) {
        this.projectId = projectId;
        this.blockId = blockId;
        this.status = status;
        this.blockVersion = blockVersion;
        this.versionUpdated = versionUpdated;
        this.actionedBy = actionedBy;
        this.approvedOnStatus = approvedOnStatus;
        this.labels = labels;
    }

    public Integer getProjectId() {
        return projectId;
    }

    public void setProjectId(Integer projectId) {
        this.projectId = projectId;
    }

    public Integer getBlockId() {
        return blockId;
    }

    public void setBlockId(Integer blockId) {
        this.blockId = blockId;
    }

    public Integer getBlockVersion() {
        return blockVersion;
    }

    public void setBlockVersion(Integer blockVersion) {
        this.blockVersion = blockVersion;
    }

    public OffsetDateTime getVersionUpdated() {
        return versionUpdated;
    }

    public void setVersionUpdated(OffsetDateTime versionUpdated) {
        this.versionUpdated = versionUpdated;
    }

    public String getActionedBy() {
        return actionedBy;
    }

    public void setActionedBy(String actionedBy) {
        this.actionedBy = actionedBy;
    }

    public NamedProjectBlock.BlockStatus getStatus() {
        return status;
    }

    public void setStatus(NamedProjectBlock.BlockStatus status) {
        this.status = status;
    }

    public String getApprovedOnStatus() {
        return approvedOnStatus;
    }

    public void setApprovedOnStatus(String approvedOnStatus) {
        this.approvedOnStatus = approvedOnStatus;
    }

    public Set<Label> getLabels() {
        return labels;
    }

    public void setLabels(Set<Label> labels) {
        this.labels = labels;
    }
}
