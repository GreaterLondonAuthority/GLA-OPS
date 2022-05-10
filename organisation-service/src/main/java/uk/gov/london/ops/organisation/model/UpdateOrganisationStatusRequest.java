/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.organisation.model;

import uk.gov.london.ops.organisation.OrganisationChangeStatusReason;
import uk.gov.london.ops.organisation.OrganisationStatus;

public class UpdateOrganisationStatusRequest {

    private OrganisationStatus status;
    private OrganisationChangeStatusReason reason;
    private String details;
    private Integer duplicateOrgId;

    public OrganisationStatus getStatus() {
        return status;
    }

    public void setStatus(OrganisationStatus status) {
        this.status = status;
    }

    public OrganisationChangeStatusReason getReason() {
        return reason;
    }

    public void setReason(OrganisationChangeStatusReason reason) {
        this.reason = reason;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public Integer getDuplicateOrgId() {
        return duplicateOrgId;
    }

    public void setDuplicateOrgId(Integer duplicateOrgId) {
        this.duplicateOrgId = duplicateOrgId;
    }
}
