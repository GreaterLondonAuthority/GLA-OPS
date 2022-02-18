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

    /**
     * Optional
     */
    private boolean approvePaymentsOnly = false;

    /**
     * Optional
     */
    private String reason;

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

    public UpdateStatusRequest(String status, String subStatus, String comments, boolean approvePaymentsOnly) {
        this(status, subStatus, comments);
        this.approvePaymentsOnly = approvePaymentsOnly;
    }

    public UpdateStatusRequest(String status, String subStatus, String comments, boolean approvePaymentsOnly, String reason) {
        this(status, subStatus, comments);
        this.approvePaymentsOnly = approvePaymentsOnly;
        this.reason = reason;
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

    public void setSubStatus(String subStatus) {
        this.subStatus = subStatus;
    }

    public boolean isApprovePaymentsOnly() {
        return approvePaymentsOnly;
    }

    public void setApprovePaymentsOnly(boolean approvePaymentsOnly) {
        this.approvePaymentsOnly = approvePaymentsOnly;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
