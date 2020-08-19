/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.organisation.model;

import com.querydsl.core.annotations.QueryEntity;

import javax.persistence.*;
import java.time.OffsetDateTime;

@Entity
@QueryEntity
public class Team {

    @Id
    private Integer id;

    @Column(name = "name")
    private String name;

    @Column(name = "managing_organisation_id")
    private Integer organisationId;

    @Column(name = "managing_organisation_name")
    private String organisationName;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private OrganisationStatus status;

    @Column(name = "registration_allowed")
    private Boolean registrationAllowed;

    @Column(name = "skills_gateway_access_allowed")
    private boolean skillsGatewayAccessAllowed;

    @Column(name = "members")
    private int members;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "created_on")
    private OffsetDateTime createdOn;

    @Column(name = "modified_by")
    private String modifiedBy;

    @Column(name = "modified_on")
    private OffsetDateTime modifiedOn;

    public Team() {}

    public Team(String name) {
        this.name = name;
    }

    public Team(String name, Integer organisationId) {
        this.name = name;
        this.organisationId = organisationId;
    }

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

    public Integer getOrganisationId() {
        return organisationId;
    }

    public void setOrganisationId(Integer organisationId) {
        this.organisationId = organisationId;
    }

    public String getOrganisationName() {
        return organisationName;
    }

    public void setOrganisationName(String organisationName) {
        this.organisationName = organisationName;
    }

    public OrganisationStatus getStatus() {
        return status;
    }

    public void setStatus(OrganisationStatus status) {
        this.status = status;
    }

    public Boolean getRegistrationAllowed() {
        return registrationAllowed;
    }

    public void setRegistrationAllowed(Boolean registrationAllowed) {
        this.registrationAllowed = registrationAllowed;
    }

    public boolean isSkillsGatewayAccessAllowed() {
        return skillsGatewayAccessAllowed;
    }

    public void setSkillsGatewayAccessAllowed(boolean skillsGatewayAccessAllowed) {
        this.skillsGatewayAccessAllowed = skillsGatewayAccessAllowed;
    }

    public int getMembers() {
        return members;
    }

    public void setMembers(int members) {
        this.members = members;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public OffsetDateTime getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(OffsetDateTime createdOn) {
        this.createdOn = createdOn;
    }

    public String getModifiedBy() {
        return modifiedBy;
    }

    public void setModifiedBy(String modifiedBy) {
        this.modifiedBy = modifiedBy;
    }

    public OffsetDateTime getModifiedOn() {
        return modifiedOn;
    }

    public void setModifiedOn(OffsetDateTime modifiedOn) {
        this.modifiedOn = modifiedOn;
    }

}
