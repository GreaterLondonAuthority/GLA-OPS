/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.web.model.project;

import uk.gov.london.ops.domain.project.NamedProjectBlock;

import java.time.OffsetDateTime;

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

    public ProjectBlockHistoryItem() {
    }

    public ProjectBlockHistoryItem(Integer projectId, Integer blockId, NamedProjectBlock.BlockStatus status,  Integer blockVersion, OffsetDateTime versionUpdated, String actionedBy) {
        this.projectId = projectId;
        this.blockId = blockId;
        this.status = status;
        this.blockVersion = blockVersion;
        this.versionUpdated = versionUpdated;
        this.actionedBy = actionedBy;
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
}
