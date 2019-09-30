/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.service.project.state;

import org.apache.commons.lang3.StringUtils;
import uk.gov.london.ops.domain.project.state.ProjectStateEntity;
import uk.gov.london.ops.domain.project.state.ProjectStatus;
import uk.gov.london.ops.domain.project.state.ProjectSubStatus;

import java.io.Serializable;
import java.util.Objects;

public class ProjectState implements Serializable {

    private String status;
    private String subStatus;
    private boolean commentsRequired;
    private String actionName;

    public ProjectState() {}

    public ProjectState(String status) {
        this.status = status;
    }

    public ProjectState(String status, String subStatus) {
        this.status = status;
        this.subStatus = subStatus;
    }

    public ProjectState(ProjectStatus status) {
        this.status = status.name();
    }

    public ProjectState(ProjectStatus status, ProjectSubStatus subStatus) {
        this.status = status != null ? status.name() : null;
        this.subStatus = subStatus != null ? subStatus.name() : null;
    }

    public static ProjectState parse(String state) {
        if (state.contains("(")) {
            String status = state.substring(0, state.indexOf("(") - 1);
            String subStatus = StringUtils.substringBetween(state, "(", ")");
            return new ProjectState(status, subStatus);
        }
        else {
            return new ProjectState(state);
        }
    }

    public static ProjectState parse(String statusString, String commentsRequired, String actionName) {
        ProjectState projectState = parse(statusString);
        projectState.setCommentsRequired(Boolean.parseBoolean(commentsRequired));
        projectState.setActionName(actionName);
        return projectState;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public ProjectStatus getStatusType() {
        return ProjectStateEntity.getStatusType(status, subStatus);
    }

    public String getSubStatus() {
        return subStatus;
    }

    public void setSubStatus(String subStatus) {
        this.subStatus = subStatus;
    }

    public ProjectSubStatus getSubStatusType() {
        return ProjectStateEntity.getSubStatusType(status, subStatus);
    }

    public void setSubStatus(ProjectSubStatus subStatus) {
        if (subStatus != null) {
            setSubStatus(subStatus.name());
        }
    }

    public boolean isCommentsRequired() {
        return commentsRequired;
    }

    public void setCommentsRequired(boolean commentsRequired) {
        this.commentsRequired = commentsRequired;
    }

    public String getActionName() {
        return actionName;
    }

    public void setActionName(String actionName) {
        this.actionName = actionName;
    }

    public boolean equals(String status) {
        return this.equals(new ProjectState(status));
    }

    public boolean equals(ProjectStatus status) {
        return this.equals(new ProjectState(status));
    }

    public boolean equals(String status, String subStatus) {
        return this.equals(new ProjectState(status, subStatus));
    }

    public boolean equals(ProjectStatus status, ProjectSubStatus subStatus) {
        return this.equals(new ProjectState(status, subStatus));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ProjectState that = (ProjectState) o;

        if (!Objects.equals(status, that.status)) return false;
        return Objects.equals(subStatus, that.subStatus);
    }

    @Override
    public int hashCode() {
        int result = status != null ? status.hashCode() : 0;
        result = 31 * result + (subStatus != null ? subStatus.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return status+(subStatus != null ? ": "+subStatus : "");
    }

}
