/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.project;

import uk.gov.london.ops.project.state.ProjectStatus;
import uk.gov.london.ops.project.state.ProjectSubStatus;

public class UpdateStatusRequest {

    /**
     * Mandatory
     */
    private String status;

    /**
     * Optional
     */
    private String subStatus;

    /**
     * Optional
     */
    private String comments;

    public UpdateStatusRequest() {}

    public UpdateStatusRequest(ProjectStatus status, String comments) {
        this(status.name(), comments);
    }

    public UpdateStatusRequest(String status, String comments) {
        this.status = status;
        this.comments = comments;
    }

    public UpdateStatusRequest(ProjectStatus status, ProjectSubStatus subStatus, String comments) {
        this(status.name(), subStatus.name(), comments);
    }

    public UpdateStatusRequest(String status, String subStatus, String comments) {
        this(status, comments);
        this.subStatus = subStatus;
    }

    public String getSubStatus() {
        return subStatus;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

}
