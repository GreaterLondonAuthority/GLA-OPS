/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.project.template.domain;

import uk.gov.london.ops.framework.enums.Requirement;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.io.Serializable;

@Embeddable
public class DetailsTemplate implements Serializable {

    @Column(name = "address_requirement")
    @Enumerated(EnumType.STRING)
    private Requirement addressRequirement;

    @Column(name = "borough_requirement")
    @Enumerated(EnumType.STRING)
    private Requirement boroughRequirement;

    @Column(name = "ward_requirement")
    @Enumerated(EnumType.STRING)
    private Requirement wardIdRequirement;

    @Column(name = "postcode_requirement")
    @Enumerated(EnumType.STRING)
    private Requirement postcodeRequirement;

    @Column(name = "coords_requirement")
    @Enumerated(EnumType.STRING)
    private Requirement coordsRequirement;

    @Column(name = "maincontact_requirement")
    @Enumerated(EnumType.STRING)
    private Requirement maincontactRequirement;

    @Column(name = "maincontactemail_requirement")
    @Enumerated(EnumType.STRING)
    private Requirement maincontactemailRequirement;

    @Column(name = "secondary_contact_requirement")
    @Enumerated(EnumType.STRING)
    private Requirement secondaryContactRequirement = Requirement.hidden;

    @Column(name = "secondary_contact_email_requirement")
    @Enumerated(EnumType.STRING)
    private Requirement secondaryContactEmailRequirement = Requirement.hidden;

    @Column(name = "site_owner_requirement")
    @Enumerated(EnumType.STRING)
    private Requirement siteOwnerRequirement;

    @Column(name = "interest_requirement")
    @Enumerated(EnumType.STRING)
    private Requirement interestRequirement;

    @Column(name = "project_manager_requirement")
    @Enumerated(EnumType.STRING)
    private Requirement projectManagerRequirement;

    @Column(name = "site_status_requirement")
    @Enumerated(EnumType.STRING)
    private Requirement siteStatusRequirement;

    @Column(name = "legacy_project_code_requirement")
    @Enumerated(EnumType.STRING)
    private Requirement legacyProjectCodeRequirement;

    @Column(name = "description_requirement")
    @Enumerated(EnumType.STRING)
    private Requirement descriptionRequirement = Requirement.optional;

    @Column(name = "planning_permission_reference_requirement")
    @Enumerated(EnumType.STRING)
    private Requirement planningPermissionReferenceRequirement = Requirement.optional;

    @Column(name = "dev_liability_org_requirement")
    @Enumerated(EnumType.STRING)
    private Requirement developmentLiabilityOrganisationRequirement = Requirement.optional;

    @Column(name = "postcomp_liability_org_requirement")
    @Enumerated(EnumType.STRING)
    private Requirement postCompletionLiabilityOrganisationRequirement = Requirement.optional;

    public Requirement getWardIdRequirement() {
        return wardIdRequirement;
    }

    public void setWardIdRequirement(Requirement wardIdRequirement) {
        this.wardIdRequirement = wardIdRequirement;
    }

    public Requirement getAddressRequirement() {
        return addressRequirement;
    }

    public void setAddressRequirement(Requirement addressRequirement) {
        this.addressRequirement = addressRequirement;
    }

    public Requirement getBoroughRequirement() {
        return boroughRequirement;
    }

    public void setBoroughRequirement(Requirement boroughRequirement) {
        this.boroughRequirement = boroughRequirement;
    }

    public Requirement getPostcodeRequirement() {
        return postcodeRequirement;
    }

    public void setPostcodeRequirement(Requirement postcodeRequirement) {
        this.postcodeRequirement = postcodeRequirement;
    }

    public Requirement getCoordsRequirement() {
        return coordsRequirement;
    }

    public void setCoordsRequirement(Requirement coordsRequirement) {
        this.coordsRequirement = coordsRequirement;
    }

    public Requirement getMaincontactRequirement() {
        return maincontactRequirement;
    }

    public void setMaincontactRequirement(Requirement maincontactRequirement) {
        this.maincontactRequirement = maincontactRequirement;
    }

    public Requirement getMaincontactemailRequirement() {
        return maincontactemailRequirement;
    }

    public void setMaincontactemailRequirement(Requirement maincontactemailRequirement) {
        this.maincontactemailRequirement = maincontactemailRequirement;
    }

    public Requirement getSecondaryContactRequirement() {
        return secondaryContactRequirement;
    }

    public void setSecondaryContactRequirement(Requirement secondaryContactRequirement) {
        this.secondaryContactRequirement = secondaryContactRequirement;
    }

    public Requirement getSecondaryContactEmailRequirement() {
        return secondaryContactEmailRequirement;
    }

    public void setSecondaryContactEmailRequirement(Requirement secondaryContactEmailRequirement) {
        this.secondaryContactEmailRequirement = secondaryContactEmailRequirement;
    }

    public Requirement getSiteOwnerRequirement() {
        return siteOwnerRequirement;
    }

    public void setSiteOwnerRequirement(Requirement siteOwnerRequirement) {
        this.siteOwnerRequirement = siteOwnerRequirement;
    }

    public Requirement getInterestRequirement() {
        return interestRequirement;
    }

    public void setInterestRequirement(Requirement interestRequirement) {
        this.interestRequirement = interestRequirement;
    }

    public Requirement getProjectManagerRequirement() {
        return projectManagerRequirement;
    }

    public void setProjectManagerRequirement(Requirement projectManagerRequirement) {
        this.projectManagerRequirement = projectManagerRequirement;
    }

    public Requirement getSiteStatusRequirement() {
        return siteStatusRequirement;
    }

    public void setSiteStatusRequirement(Requirement siteStatusRequirement) {
        this.siteStatusRequirement = siteStatusRequirement;
    }

    public Requirement getLegacyProjectCodeRequirement() {
        return legacyProjectCodeRequirement;
    }

    public void setLegacyProjectCodeRequirement(Requirement legacyProjectCodeRequirement) {
        this.legacyProjectCodeRequirement = legacyProjectCodeRequirement;
    }

    public Requirement getDescriptionRequirement() {
        return descriptionRequirement;
    }

    public void setDescriptionRequirement(Requirement descriptionRequirement) {
        this.descriptionRequirement = descriptionRequirement;
    }

    public Requirement getPlanningPermissionReferenceRequirement() {
        return planningPermissionReferenceRequirement;
    }

    public void setPlanningPermissionReferenceRequirement(Requirement planningPermissionReferenceRequirement) {
        this.planningPermissionReferenceRequirement = planningPermissionReferenceRequirement;
    }

    public Requirement getDevelopmentLiabilityOrganisationRequirement() {
        return developmentLiabilityOrganisationRequirement;
    }

    public void setDevelopmentLiabilityOrganisationRequirement(
            Requirement developmentLiabilityOrganisationRequirement) {
        this.developmentLiabilityOrganisationRequirement = developmentLiabilityOrganisationRequirement;
    }

    public Requirement getPostCompletionLiabilityOrganisationRequirement() {
        return postCompletionLiabilityOrganisationRequirement;
    }

    public void setPostCompletionLiabilityOrganisationRequirement(
            Requirement postCompletionLiabilityOrganisationRequirement) {
        this.postCompletionLiabilityOrganisationRequirement = postCompletionLiabilityOrganisationRequirement;
    }
}
