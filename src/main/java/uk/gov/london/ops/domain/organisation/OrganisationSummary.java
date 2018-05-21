/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.domain.organisation;

import uk.gov.london.ops.util.jpajoins.NonJoin;

import javax.persistence.*;
import java.io.Serializable;

@Entity(name = "v_organisation_summaries")
@NonJoin("Summary view, ignore join information")
public class OrganisationSummary implements Serializable {

    @Id
    private Integer id;

    @Column(name="name")
    private String name;

    @Column(name="entity_type")
    private Integer entityType;

    @Column(name = "managing_organisation_id")
    private Integer managingOrganisationId;

    @Column(name="managing_organisation_name")
    private String managingOrganisationName;

    @Column(name="email")
    private String email;

    @Column(name="sap_vendor_id")
    private String sapVendorId;

    @Column(name="user_reg_status")
    @Enumerated(EnumType.STRING)
    private RegistrationStatus userRegStatus;

    @Column(name="status")
    @Enumerated(EnumType.STRING)
    private OrganisationStatus status;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getEntityType() {
        return entityType;
    }

    public void setEntityType(Integer entityType) {
        this.entityType = entityType;
    }

    public Integer getManagingOrganisationId() {
        return managingOrganisationId;
    }

    public void setManagingOrganisationId(Integer managingOrganisationId) {
        this.managingOrganisationId = managingOrganisationId;
    }

    public String getManagingOrganisationName() {
        return managingOrganisationName;
    }

    public void setManagingOrganisationName(String managingOrganisationName) {
        this.managingOrganisationName = managingOrganisationName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSapVendorId() {
        return sapVendorId;
    }

    public void setSapVendorId(String sapVendorId) {
        this.sapVendorId = sapVendorId;
    }

    public RegistrationStatus getUserRegStatus() {
        return userRegStatus;
    }

    public void setUserRegStatus(RegistrationStatus userRegStatus) {
        this.userRegStatus = userRegStatus;
    }

    public OrganisationStatus getStatus() { return status; }

    public void setStatus(OrganisationStatus status) { this.status = status; }
}
