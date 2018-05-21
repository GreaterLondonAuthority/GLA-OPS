/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.web.model.project;

import uk.gov.london.ops.domain.project.Project;

public class UpdateStatusRequest {

    /**
     * Mandatory
     */
    private Project.Status status;

    /**
     * Optional
     */
    private Project.SubStatus subStatus;

    /**
     * Optional
     */
    private String comments;

    public UpdateStatusRequest() {}

    public UpdateStatusRequest(Project.Status status, String comments) {
        this.status = status;
        this.comments = comments;
    }

    public UpdateStatusRequest(final Project.Status status,
                               final Project.SubStatus subStatus,
                               final String comments) {
        this(status, comments);
        this.subStatus = subStatus;
    }

    public Project.SubStatus getSubStatus() {
        return subStatus;
    }

    public Project.Status getStatus() {
        return status;
    }

    public void setStatus(Project.Status status) {
        this.status = status;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

}
