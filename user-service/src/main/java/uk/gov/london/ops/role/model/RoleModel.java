/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.role.model;

import uk.gov.london.ops.organisation.OrganisationStatus;

import java.util.Date;

public class RoleModel {

    private String name;
    private String description;
    private Integer organisationId;
    private boolean approved;
    private String approvedBy;
    private Date approvedOn;
    private Integer managingOrganisationId;
    private OrganisationStatus orgStatus;
    private Boolean authorisedSignatory;

    public RoleModel() {}

    public RoleModel(String name, Integer organisationId) {
        this.name = name;
        this.organisationId = organisationId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getOrganisationId() {
        return organisationId;
    }

    public void setOrganisationId(Integer organisationId) {
        this.organisationId = organisationId;
    }

    public boolean isApproved() {
        return approved;
    }

    public void setApproved(boolean approved) {
        this.approved = approved;
    }

    public String getApprovedBy() {
        return approvedBy;
    }

    public void setApprovedBy(String approvedBy) {
        this.approvedBy = approvedBy;
    }

    public Date getApprovedOn() {
        return approvedOn;
    }

    public void setApprovedOn(Date approvedOn) {
        this.approvedOn = approvedOn;
    }

    public Integer getManagingOrganisationId() {
        return managingOrganisationId;
    }

    public void setManagingOrganisationId(Integer managingOrganisationId) {
        this.managingOrganisationId = managingOrganisationId;
    }

    public Boolean getAuthorisedSignatory() {
        return authorisedSignatory;
    }

    public void setAuthorisedSignatory(Boolean authorisedSignatory) {
        this.authorisedSignatory = authorisedSignatory;
    }

    public OrganisationStatus getOrgStatus() {
        return orgStatus;
    }

    public void setOrgStatus(OrganisationStatus orgStatus) {
        this.orgStatus = orgStatus;
    }

}
