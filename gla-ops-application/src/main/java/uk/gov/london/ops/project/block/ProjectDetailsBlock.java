/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.project.block;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import uk.gov.london.ops.framework.enums.Requirement;
import uk.gov.london.ops.framework.jpa.Join;
import uk.gov.london.ops.framework.jpa.JoinData;
import uk.gov.london.ops.framework.jpa.NonJoin;
import uk.gov.london.ops.project.Project;
import uk.gov.london.ops.project.state.ProjectStatus;
import uk.gov.london.ops.project.template.domain.DetailsTemplate;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;
import java.util.Objects;

/**
 * The Project Details block in a Project.
 *
 * @author Chris Melville
 */
@Entity(name = "project_details_block")
@DiscriminatorValue("DETAILS")
@JoinData(sourceTable = "project_details_block", sourceColumn = "id", targetTable = "project_block", targetColumn = "id",
        joinType = Join.JoinType.OneToOne,
        comment = "the project details block is a subclass of the project block and shares a common key")
public class ProjectDetailsBlock extends NamedProjectBlock {

    @Transient
    Logger log = LoggerFactory.getLogger(getClass());

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description")
    private String description;

    @Column(name = "address")
    private String address;

    @Column(name = "borough")
    private String borough;

    @Column(name = "ward_id")
    @JoinData(targetTable = "ward", targetColumn = "id", joinType = Join.JoinType.OneToOne,
            comment = "join to table containing ward details. ")
    private Integer wardId;

    @Column(name = "postcode")
    private String postcode;

    @Column(name = "coord_x")
    private String coordX;

    @Column(name = "coord_y")
    private String coordY;

    @Column(name = "main_contact_name")
    private String mainContact;

    @Column(name = "main_contact_email")
    private String mainContactEmail;

    @Column(name = "secondary_contact_name")
    private String secondaryContact;

    @Column(name = "secondary_contact_email")
    private String secondaryContactEmail;

    @Column(name = "site_owner")
    private String siteOwner;

    @Column(name = "address_restricted")
    private boolean addressRestricted;

    @Column(name = "interest")
    private String interest;

    @Column(name = "project_manager")
    private String projectManager;

    @Column(name = "site_status")
    private String siteStatus;

    @Column(name = "planning_permission_reference")
    private String planningPermissionReference;

    @Column(name = "sap_id")
    private String sapId;

    @Column(name = "pcs_project_code")
    @NonJoin("Usually references the original project code in IMS or PCS")
    private Integer legacyProjectCode;

    // organisationGroupId and developingOrganisationId and orgSelected are stored on Project so transient here,
    @Transient
    private Integer organisationGroupId;

    @Column(name = "developing_organisation_id")
    @JoinData(targetTable = "organisation", targetColumn = "id", joinType = Join.JoinType.OneToOne,
            comment = "The organisation responsible for this project's development")
    private Integer developingOrganisationId;

    @Column(name = "dev_liability_org_id")
    @JoinData(targetTable = "organisation", targetColumn = "id", joinType = Join.JoinType.OneToOne,
            comment = "The organisation liable for this project's development")
    private Integer developmentLiabilityOrganisationId;

    @Column(name = "postcomp_liability_org_id")
    @JoinData(targetTable = "organisation", targetColumn = "id", joinType = Join.JoinType.OneToOne,
            comment = "The organisation liable for this project post completion")
    private Integer postCompletionLiabilityOrganisationId;

    @Column(name = "use_allocation")
    private Boolean useAllocation;

    @Transient
    private boolean orgSelected;

    @Transient
    private String boroughDelimiter = ", ";

    @Transient
    private boolean multipleOrganisations;

    public ProjectDetailsBlock() {
    }

    public ProjectDetailsBlock(Project project) {
        super(project);
        this.setTitle(project.getTitle());
    }

    @Override
    public void setProject(Project project) {
        super.setProject(project);
        this.setTitle(project.getTitle());
    }

    @Override
    public ProjectBlockType getBlockType() {
        return ProjectBlockType.Details;
    }

    @Override
    protected void copyBlockContentInto(NamedProjectBlock target) {
        super.copyBlockContentInto(target);
        ProjectDetailsBlock details = (ProjectDetailsBlock) target;
        details.setTitle(this.getTitle());
        details.setDescription(this.getDescription());
        details.setSapId(this.getSapId());
        details.setAddress(this.getAddress());
        details.setBorough(this.getBorough());
        details.setBoroughDelimiter(this.getBoroughDelimiter());
        details.setWardId(this.getWardId());
        details.setPostcode(this.getPostcode());
        details.setCoordX(this.getCoordX());
        details.setCoordY(this.getCoordY());
        details.setMainContact(this.getMainContact());
        details.setMainContactEmail(this.getMainContactEmail());
        details.setLegacyProjectCode(this.getLegacyProjectCode());
        details.setSiteOwner(this.getSiteOwner());
        details.setSiteStatus(this.getSiteStatus());
        details.setProjectManager(this.getProjectManager());
        details.setInterest(this.getInterest());
        details.setPlanningPermissionReference(this.getPlanningPermissionReference());
        details.setDevelopmentLiabilityOrganisationId(this.getDevelopmentLiabilityOrganisationId());
        details.setPostCompletionLiabilityOrganisationId(this.getPostCompletionLiabilityOrganisationId());
        details.setDevelopingOrganisationId(this.getDevelopingOrganisationId());
        details.setAddressRestricted(this.isAddressRestricted());
        details.setUseAllocation(this.useAllocation);
    }

    @Override
    public boolean isComplete() {
        DetailsTemplate config = project.getTemplate().getDetailsConfig();

        boolean addressFieldsValid = true;
        if (!addressRestricted) {
            addressFieldsValid = checkRequirement(this.getAddress(), config.getAddressRequirement())
                    && checkRequirement(this.getPostcode(), config.getPostcodeRequirement())
                    && checkRequirement(this.getCoordX(), config.getCoordsRequirement())
                    && checkRequirement(this.getCoordY(), config.getCoordsRequirement())
                    && checkRequirement(this.getWardId(), config.getWardIdRequirement())
                    && checkRequirement(this.getPlanningPermissionReference(),
                    config.getPlanningPermissionReferenceRequirement());

        }

        return config != null && addressFieldsValid
                && checkRequirement(this.getBorough(), config.getBoroughRequirement())
                && this.getTitle() != null && this.getTitle().length() > 0
                && checkRequirement(this.getMainContact(), config.getMaincontactRequirement())
                && checkRequirement(this.getMainContactEmail(), config.getMaincontactemailRequirement())
                && checkRequirement(this.getSecondaryContact(), config.getSecondaryContactRequirement())
                && checkRequirement(this.getSecondaryContactEmail(), config.getSecondaryContactEmailRequirement())
                && checkRequirement(this.getSiteOwner(), config.getSiteOwnerRequirement())
                && checkRequirement(this.getInterest(), config.getInterestRequirement())
                && checkRequirement(this.getProjectManager(), config.getProjectManagerRequirement())
                && checkRequirement(this.getSiteStatus(), config.getSiteStatusRequirement())
                && checkRequirement(this.getLegacyProjectCode(), config.getLegacyProjectCodeRequirement())
                && checkRequirement(this.getDescription(), config.getDescriptionRequirement())
                && requiresLiabilityOrgIds(config)
                && (project.getOrganisationGroupId() == null || this.getDevelopingOrganisationId() != null)
                && project.isOrgSelected();
    }

    private boolean requiresLiabilityOrgIds(DetailsTemplate config) {
        // GLA-39406 if question is mandatory causes completeness issues when question not displayed
        // Question is not displayed if there are not multiple organisations to choose from
        return !this.hasMultipleOrganisations()
                || (checkRequirement(this.getDevelopmentLiabilityOrganisationId(), config.getDevelopmentLiabilityOrganisationRequirement())
                    && checkRequirement(this.getPostCompletionLiabilityOrganisationId(), config.getPostCompletionLiabilityOrganisationRequirement()));
    }

    public boolean isCanEditAddressRestrictedField() {
        DetailsTemplate config = project.getTemplate().getDetailsConfig();

        return this.project.getStatusType().equals(ProjectStatus.Draft)
                && (!Requirement.hidden.equals(config.getAddressRequirement())
                || !Requirement.hidden.equals(config.getPostcodeRequirement()));
    }

    public boolean isShowAddressRestrictedField() {
        DetailsTemplate config = project.getTemplate().getDetailsConfig();

        return !Requirement.hidden.equals(config.getPostcodeRequirement())
                || !Requirement.hidden.equals(config.getAddressRequirement())
                || !Requirement.hidden.equals(config.getBoroughRequirement())
                || !Requirement.hidden.equals(config.getWardIdRequirement())
                || !Requirement.hidden.equals(config.getPlanningPermissionReferenceRequirement());
    }

    /**
     * Returns true if the value is specified or not required.
     */
    boolean checkRequirement(String value, Requirement requirement) {
        return !Requirement.isRequired(requirement) || !StringUtils.isEmpty(value);
    }

    /**
     * Returns true if the value is specified or not required.
     */
    boolean checkRequirement(Integer value, Requirement requirement) {
        return !Requirement.isRequired(requirement) || value != null;
    }

    public void merge(NamedProjectBlock block) {
        ProjectDetailsBlock projectDetailsBlock = (ProjectDetailsBlock) block;
        if (projectDetailsBlock.getTitle() == null) {
            this.setTitle("");
        } else {
            this.setTitle(projectDetailsBlock.getTitle());
        }
        this.setDescription(projectDetailsBlock.getDescription());
        this.setSapId(projectDetailsBlock.getSapId());
        this.setAddress(projectDetailsBlock.getAddress());
        this.setBorough(projectDetailsBlock.getBorough());
        this.setBoroughDelimiter(projectDetailsBlock.getBoroughDelimiter());
        this.setWardId(projectDetailsBlock.getWardId());
        this.setPostcode(projectDetailsBlock.getPostcode());
        this.setCoordX(projectDetailsBlock.getCoordX());
        this.setCoordY(projectDetailsBlock.getCoordY());
        this.setMainContact(projectDetailsBlock.getMainContact());
        this.setMainContactEmail(projectDetailsBlock.getMainContactEmail());
        this.setSecondaryContact(projectDetailsBlock.getSecondaryContact());
        this.setSecondaryContactEmail(projectDetailsBlock.getSecondaryContactEmail());
        this.setLegacyProjectCode(projectDetailsBlock.getLegacyProjectCode());
        this.setSiteOwner(projectDetailsBlock.getSiteOwner());
        this.setSiteStatus(projectDetailsBlock.getSiteStatus());
        this.setProjectManager(projectDetailsBlock.getProjectManager());
        this.setAddressRestricted(projectDetailsBlock.isAddressRestricted());
        this.setInterest(projectDetailsBlock.getInterest());
        this.setPlanningPermissionReference(projectDetailsBlock.getPlanningPermissionReference());
        this.setDevelopmentLiabilityOrganisationId(projectDetailsBlock.getDevelopmentLiabilityOrganisationId());
        this.setPostCompletionLiabilityOrganisationId(projectDetailsBlock.getPostCompletionLiabilityOrganisationId());
        this.setMultipleOrganisations(projectDetailsBlock.hasMultipleOrganisations());
        project.setOrganisationGroupId(projectDetailsBlock.organisationGroupId);
        this.setDevelopingOrganisationId(projectDetailsBlock.developingOrganisationId);
        project.setOrgSelected(projectDetailsBlock.orgSelected);
        this.setUseAllocation(projectDetailsBlock.useAllocation);
    }


    @Override
    protected void generateValidationFailures() {
        // do nothing for now.
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getBorough() {
        return borough;
    }

    public void setBorough(String borough) {
        this.borough = borough;
    }

    public Integer getWardId() {
        return wardId;
    }

    public void setWardId(Integer wardId) {
        this.wardId = wardId;
    }

    public String getPostcode() {
        return postcode;
    }

    public void setPostcode(String postcode) {
        this.postcode = postcode;
    }

    public String getCoordX() {
        return coordX;
    }

    public void setCoordX(String coordX) {
        this.coordX = coordX;
    }

    public String getCoordY() {
        return coordY;
    }

    public void setCoordY(String coordY) {
        this.coordY = coordY;
    }

    public String getMainContact() {
        return mainContact;
    }

    public void setMainContact(String mainContact) {
        this.mainContact = mainContact;
    }

    public String getMainContactEmail() {
        return mainContactEmail;
    }

    public void setMainContactEmail(String mainContactEmail) {
        this.mainContactEmail = mainContactEmail;
    }

    public String getSecondaryContact() {
        return secondaryContact;
    }

    public void setSecondaryContact(String secondaryContact) {
        this.secondaryContact = secondaryContact;
    }

    public String getSecondaryContactEmail() {
        return secondaryContactEmail;
    }

    public void setSecondaryContactEmail(String secondaryContactEmail) {
        this.secondaryContactEmail = secondaryContactEmail;
    }

    public String getSiteOwner() {
        return siteOwner;
    }

    public void setSiteOwner(String siteOwner) {
        this.siteOwner = siteOwner;
    }

    public String getInterest() {
        return interest;
    }

    public void setInterest(String interest) {
        this.interest = interest;
    }

    public String getProjectManager() {
        return projectManager;
    }

    public void setProjectManager(String projectManager) {
        this.projectManager = projectManager;
    }

    public String getSiteStatus() {
        return siteStatus;
    }

    public void setSiteStatus(String siteStatus) {
        this.siteStatus = siteStatus;
    }

    public boolean isAddressRestricted() {
        return addressRestricted;
    }

    public void setAddressRestricted(boolean addressRestricted) {
        this.addressRestricted = addressRestricted;
    }

    public String getPlanningPermissionReference() {
        return planningPermissionReference;
    }

    public void setPlanningPermissionReference(String planningPermissionReference) {
        this.planningPermissionReference = planningPermissionReference;
    }

    public Integer getLegacyProjectCode() {
        return legacyProjectCode;
    }

    public void setLegacyProjectCode(Integer legacyProjectCode) {
        this.legacyProjectCode = legacyProjectCode;
    }

    public Integer getOrganisationGroupId() {
        return project.getOrganisationGroupId();
    }

    public void setOrganisationGroupId(Integer organisationGroupId) {
        this.organisationGroupId = organisationGroupId;
    }

    public Integer getDevelopingOrganisationId() {
        return developingOrganisationId;
    }

    public void setDevelopingOrganisationId(Integer developingOrganisationId) {
        this.developingOrganisationId = developingOrganisationId;
    }

    public Integer getDevelopmentLiabilityOrganisationId() {
        return developmentLiabilityOrganisationId;
    }

    public void setDevelopmentLiabilityOrganisationId(Integer developmentLiabilityOrganisationId) {
        this.developmentLiabilityOrganisationId = developmentLiabilityOrganisationId;
    }

    public Integer getPostCompletionLiabilityOrganisationId() {
        return postCompletionLiabilityOrganisationId;
    }

    public void setPostCompletionLiabilityOrganisationId(Integer postCompletionLiabilityOrganisationId) {
        this.postCompletionLiabilityOrganisationId = postCompletionLiabilityOrganisationId;
    }

    public boolean isOrgSelected() {
        return project.isOrgSelected();
    }

    public void setOrgSelected(boolean orgSelected) {
        this.orgSelected = orgSelected;
    }

    public String getBoroughDelimiter() {
        return boroughDelimiter;
    }

    public void setBoroughDelimiter(String boroughDelimiter) {
        this.boroughDelimiter = boroughDelimiter;
    }

    public String getSapId() {
        return sapId;
    }

    public void setSapId(String sapId) {
        this.sapId = sapId;
    }

    public boolean hasMultipleOrganisations() {
        return multipleOrganisations;
    }

    public void setMultipleOrganisations(boolean multipleOrganisations) {
        this.multipleOrganisations = multipleOrganisations;
    }

    public Boolean getUseAllocation() {
        return useAllocation;
    }

    public void setUseAllocation(Boolean useAllocation) {
        this.useAllocation = useAllocation;
    }

    @Override
    protected void compareBlockSpecificContent(NamedProjectBlock otherBlock, ProjectDifferences differences) {
        super.compareBlockSpecificContent(otherBlock, differences);

        ProjectDetailsBlock other = (ProjectDetailsBlock) otherBlock;

        if (!Objects.equals(StringUtils.trimAllWhitespace(title), StringUtils.trimAllWhitespace(other.title))) {
            differences.add(new ProjectDifference(this, "title"));
        }
        if (!Objects.equals(StringUtils.trimAllWhitespace(description), StringUtils.trimAllWhitespace(other.description))) {
            differences.add(new ProjectDifference(this, "description"));
        }
        if (!Objects.equals(StringUtils.trimAllWhitespace(sapId), StringUtils.trimAllWhitespace(other.sapId))) {
            differences.add(new ProjectDifference(this, "sapId"));
        }
        if (!Objects.equals(StringUtils.trimAllWhitespace(address), StringUtils.trimAllWhitespace(other.address))) {
            differences.add(new ProjectDifference(this, "address"));
        }
        if (!Objects.equals(StringUtils.trimAllWhitespace(borough), StringUtils.trimAllWhitespace(other.borough))) {
            differences.add(new ProjectDifference(this, "borough"));
        }
        if (!Objects.equals(wardId, other.wardId)) {
            differences.add(new ProjectDifference(this, "wardId"));
        }
        if (!Objects.equals(StringUtils.trimAllWhitespace(postcode), StringUtils.trimAllWhitespace(other.postcode))) {
            differences.add(new ProjectDifference(this, "postcode"));
        }
        if (!Objects.equals(StringUtils.trimAllWhitespace(coordX), StringUtils.trimAllWhitespace(other.coordX))) {
            differences.add(new ProjectDifference(this, "coordX"));
        }
        if (!Objects.equals(StringUtils.trimAllWhitespace(coordY), StringUtils.trimAllWhitespace(other.coordY))) {
            differences.add(new ProjectDifference(this, "coordY"));
        }
        if (!Objects.equals(StringUtils.trimAllWhitespace(mainContact), StringUtils.trimAllWhitespace(other.mainContact))) {
            differences.add(new ProjectDifference(this, "mainContact"));
        }
        if (!Objects
                .equals(StringUtils.trimAllWhitespace(mainContactEmail), StringUtils.trimAllWhitespace(other.mainContactEmail))) {
            differences.add(new ProjectDifference(this, "mainContactEmail"));
        }
        if (!Objects.equals(StringUtils.trimAllWhitespace(secondaryContact), StringUtils.trimAllWhitespace(other.secondaryContact))) {
            differences.add(new ProjectDifference(this, "secondaryContact"));
        }
        if (!Objects
                .equals(StringUtils.trimAllWhitespace(secondaryContactEmail), StringUtils.trimAllWhitespace(other.secondaryContactEmail))) {
            differences.add(new ProjectDifference(this, "secondaryContactEmail"));
        }
        if (!Objects.equals(legacyProjectCode, other.legacyProjectCode)) {
            differences.add(new ProjectDifference(this, "legacyProjectCode"));
        }
        if (!Objects.equals(StringUtils.trimAllWhitespace(siteOwner), StringUtils.trimAllWhitespace(other.siteOwner))) {
            differences.add(new ProjectDifference(this, "siteOwner"));
        }
        if (!Objects.equals(StringUtils.trimAllWhitespace(siteStatus), StringUtils.trimAllWhitespace(other.siteStatus))) {
            differences.add(new ProjectDifference(this, "siteStatus"));
        }
        if (!Objects.equals(StringUtils.trimAllWhitespace(projectManager), StringUtils.trimAllWhitespace(other.projectManager))) {
            differences.add(new ProjectDifference(this, "projectManager"));
        }
        if (!Objects.equals(StringUtils.trimAllWhitespace(interest), StringUtils.trimAllWhitespace(other.interest))) {
            differences.add(new ProjectDifference(this, "interest"));
        }
        if (!Objects.equals(StringUtils.trimAllWhitespace(planningPermissionReference),
                StringUtils.trimAllWhitespace(other.planningPermissionReference))) {
            differences.add(new ProjectDifference(this, "planningPermissionReference"));
        }
        if (!Objects.equals(developingOrganisationId, other.developingOrganisationId)) {
            differences.add(new ProjectDifference(this, "developingOrganisationId"));
        }
        if (!Objects.equals(postCompletionLiabilityOrganisationId, other.postCompletionLiabilityOrganisationId)) {
            differences.add(new ProjectDifference(this, "postCompletionLiabilityOrganisationId"));
        }
        if (!Objects.equals(useAllocation, other.useAllocation)) {
            differences.add(new ProjectDifference(this, "useAllocation"));
        }
    }

    @Override
    public boolean isSelfContained() {
        return false;
    }



}
