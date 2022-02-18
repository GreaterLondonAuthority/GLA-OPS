/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.programme.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import uk.gov.london.ops.framework.jpa.NonJoin;
import uk.gov.london.ops.organisation.model.OrganisationEntity;
import uk.gov.london.ops.service.ManagedEntityInterface;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity(name = "ProgrammePublicProfileSummary")
@Table(name = "programme")
@NonJoin("Programme public profile summary entity, does not provide join information")
public class ProgrammePublicProfileSummary implements ManagedEntityInterface {

    @Id
    private Integer id;

    @Column(name = "name")
    private String name;

    @Column(name = "restricted")
    private boolean restricted = false;

    @Column(name = "enabled")
    private boolean enabled = false;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private Programme.Status status = Programme.Status.Active;

    @Column(name = "created_on")
    private OffsetDateTime createdOn;

    @Column(name = "modified_on")
    private OffsetDateTime modifiedOn;

    @JsonIgnore
    @ManyToOne(cascade = {})
    @JoinColumn(name = "managing_organisation_id")
    private OrganisationEntity managingOrganisation;

    @Column(name = "description")
    private String description;

    @Column(name = "total_funding")
    private BigDecimal totalFunding;

    @Column(name = "website_link")
    private String websiteLink;

    public ProgrammePublicProfileSummary() {
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

    public boolean isRestricted() {
        return restricted;
    }

    public void setRestricted(boolean restricted) {
        this.restricted = restricted;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Programme.Status getStatus() {
        return status;
    }

    public void setStatus(Programme.Status status) {
        this.status = status;
    }

    public OffsetDateTime getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(OffsetDateTime createdOn) {
        this.createdOn = createdOn;
    }

    public OffsetDateTime getModifiedOn() {
        return modifiedOn;
    }

    public void setModifiedOn(OffsetDateTime modifiedOn) {
        this.modifiedOn = modifiedOn;
    }

    @Override
    public OrganisationEntity getManagingOrganisation() {
        return managingOrganisation;
    }

    public void setManagingOrganisation(OrganisationEntity managingOrganisation) {
        this.managingOrganisation = managingOrganisation;
    }

    public OffsetDateTime getLastPublicEdit() {
        return modifiedOn == null ? createdOn : modifiedOn;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getTotalFunding() {
        return totalFunding;
    }

    public void setTotalFunding(BigDecimal totalFunding) {
        this.totalFunding = totalFunding;
    }

    public String getWebsiteLink() {
        return websiteLink;
    }

    public void setWebsiteLink(String websiteLink) {
        this.websiteLink = websiteLink;
    }
}
