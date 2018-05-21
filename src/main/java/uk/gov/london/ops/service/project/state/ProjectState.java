/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.service.project.state;

import org.apache.commons.lang3.StringUtils;
import uk.gov.london.ops.domain.project.Project;

public class ProjectState {

    private Project.Status status;
    private Project.SubStatus subStatus;
    private boolean commentsRequired;

    public ProjectState() {}

    public ProjectState(Project.Status status) {
        this.status = status;
    }

    public ProjectState(Project.Status status, Project.SubStatus subStatus) {
        this.status = status;
        this.subStatus = subStatus;
    }

    public static ProjectState parse(String state) {
        if (state.contains("(")) {
            String status = state.split(" ")[0];
            String subStatus = StringUtils.substringBetween(state.split(" ")[1], "(", ")");
            return new ProjectState(Project.Status.valueOf(status), Project.SubStatus.valueOf(subStatus));
        }
        else {
            return new ProjectState(Project.Status.valueOf(state));
        }
    }

    public static ProjectState parse(String statusString, String commentsRequired) {
        ProjectState projectState = parse(statusString);
        projectState.setCommentsRequired(Boolean.parseBoolean(commentsRequired));
        return projectState;
    }

    public Project.Status getStatus() {
        return status;
    }

    public void setStatus(Project.Status status) {
        this.status = status;
    }

    public Project.SubStatus getSubStatus() {
        return subStatus;
    }

    public void setSubStatus(Project.SubStatus subStatus) {
        this.subStatus = subStatus;
    }

    public boolean isCommentsRequired() {
        return commentsRequired;
    }

    public void setCommentsRequired(boolean commentsRequired) {
        this.commentsRequired = commentsRequired;
    }

    public boolean equals(Project.Status status) {
        return this.equals(new ProjectState(status));
    }

    public boolean equals(Project.Status status, Project.SubStatus subStatus) {
        return this.equals(new ProjectState(status, subStatus));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ProjectState that = (ProjectState) o;

        if (status != that.status) return false;
        return subStatus == that.subStatus;
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
