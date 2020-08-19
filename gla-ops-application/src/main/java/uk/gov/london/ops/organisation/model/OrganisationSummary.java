/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.organisation.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.querydsl.core.annotations.QueryEntity;
import uk.gov.london.ops.framework.jpa.NonJoin;

import javax.persistence.*;
import java.io.Serializable;

@Entity(name = "v_organisation_summaries")
@QueryEntity
@NonJoin("Summary view, ignore join information")
public class OrganisationSummary implements Serializable {

    public static final Integer GLA_HNL_ID = 10000;

    @Id
    private Integer id;

    @Column(name = "name")
    private String name;

    @Column(name = "entity_type")
    private Integer entityType;

    @Column(name = "managing_organisation_id")
    private Integer managingOrganisationId;

    @Column(name = "managing_organisation_name")
    private String managingOrganisationName;

    @Column(name = "managing_organisation_icon_attachment_id")
    private String managingOrganisationIconAttachmentId;

    @Column(name = "sap_vendor_id")
    private String sapVendorId;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private OrganisationStatus status;

    @Column(name = "team_name")
    private String teamName;

    @Column(name = "team_id")
    private Integer teamId;

    @Column(name = "registration_allowed")
    private Boolean registrationAllowed;

    @Column(name = "icon_attachment_id")
    private Integer iconAttachmentId;

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

    public String getManagingOrganisationIconAttachmentId() {
        return managingOrganisationIconAttachmentId;
    }

    public void setManagingOrganisationIconAttachmentId(String managingOrganisationIconAttachmentId) {
        this.managingOrganisationIconAttachmentId = managingOrganisationIconAttachmentId;
    }

    public String getSapVendorId() {
        return sapVendorId;
    }

    public void setSapVendorId(String sapVendorId) {
        this.sapVendorId = sapVendorId;
    }

    public OrganisationStatus getStatus() {
        return status;
    }

    public void setStatus(OrganisationStatus status) {
        this.status = status;
    }

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

    public Integer getTeamId() {
        return teamId;
    }

    public void setTeamId(Integer teamId) {
        this.teamId = teamId;
    }

    public Boolean getRegistrationAllowed() {
        return registrationAllowed;
    }

    public void setRegistrationAllowed(Boolean registrationAllowed) {
        this.registrationAllowed = registrationAllowed;
    }

    public Integer getIconAttachmentId() {
        return iconAttachmentId;
    }

    public void setIconAttachmentId(Integer iconAttachmentId) {
        this.iconAttachmentId = iconAttachmentId;
    }

    /**
     * @return true if the organisation is GLA House and Landing.
     */
    @JsonProperty(value = "isGlaHNL", access = JsonProperty.Access.READ_ONLY)
    public Boolean getIsGlaHNL() {
        return GLA_HNL_ID.equals(this.getId());
    }
}
