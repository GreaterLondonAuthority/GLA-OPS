/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.organisation;

public enum OrganisationChangeStatusReason {

    Registered(OrganisationStatus.Rejected, "Organisation is already registered"),
    Ineligible(OrganisationStatus.Rejected, "Organisation type is not eligible for funding under this department"),
    UnableToVery(OrganisationStatus.Rejected, "GLA were unable to verify organisation details"),

    Duplicate(OrganisationStatus.Inactive, null),
    ApprovedInError(OrganisationStatus.Inactive, "Approved in error"),

    Other;

    private OrganisationStatus status;
    private String description;

    OrganisationChangeStatusReason() {}

    OrganisationChangeStatusReason(OrganisationStatus status, String description) {
        this.status = status;
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public OrganisationStatus getStatus() {
        return status;
    }
}
