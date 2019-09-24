/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.web.model;

import uk.gov.london.common.organisation.OrganisationType;
import uk.gov.london.ops.domain.organisation.Address;
import uk.gov.london.ops.domain.organisation.Team;

public class OrganisationUserDTO extends OrganisationModel {

    private Integer entityType = OrganisationType.OTHER.id();

    private String sapVendorId;

    private Integer ukprn;

    private String website;

    private String contactNumber;

    private Address address;

    private Boolean regulated = false;

    private String contactEmail;

    private UserRegistration userRegistration;

    private String email;

    private String ceoTitle;

    private String ceoName;

    private Team team;

    public Integer getEntityType() {
        return entityType;
    }

    public void setEntityType(Integer entityType) {
        this.entityType = entityType;
    }

    public String getSapVendorId() {
        return sapVendorId;
    }

    public void setSapVendorId(String sapVendorId) {
        this.sapVendorId = sapVendorId;
    }

    public Integer getUkprn() {
        return ukprn;
    }

    public void setUkprn(Integer ukprn) {
        this.ukprn = ukprn;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public Boolean getRegulated() {
        return regulated;
    }

    public void setRegulated(Boolean regulated) {
        this.regulated = regulated;
    }

    public UserRegistration getUserRegistration() {
        return userRegistration;
    }

    public void setUserRegistration(UserRegistration userRegistration) {
        this.userRegistration = userRegistration;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    public String getCeoTitle() {
        return ceoTitle;
    }

    public void setCeoTitle(String ceoTitle) {
        this.ceoTitle = ceoTitle;
    }

    public String getCeoName() {
        return ceoName;
    }

    public void setCeoName(String ceoName) {
        this.ceoName = ceoName;
    }

    public Team getTeam() {
        return team;
    }

    public void setTeam(Team team) {
        this.team = team;
    }

    @Override
    public String toString() {
        return "OrganisationUserDTO{" +
                "entityType=" + entityType +
                ", sapVendorId='" + sapVendorId + '\'' +
                ", ukprn=" + ukprn +
                ", website='" + website + '\'' +
                ", contactNumber='" + contactNumber + '\'' +
                ", address=" + address +
                ", regulated=" + regulated +
                ", contactEmail='" + contactEmail + '\'' +
                ", userRegistration=" + userRegistration +
                ", email='" + email + '\'' +
                ", ceoTitle='" + ceoTitle + '\'' +
                ", ceoName='" + ceoName + '\'' +
                ", team=" + team +
                '}';
    }
}
