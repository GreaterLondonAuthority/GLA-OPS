/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.domain.project;

import uk.gov.london.ops.util.jpajoins.NonJoin;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import java.time.OffsetDateTime;

/**
 * Project summary for use by API.
 *
 * @author Steve Leach
 */
@Entity(name = "v_project_summaries")
@NonJoin("Summary entity, does not provide join information")
public class ProjectSummary implements Comparable<ProjectSummary> {

    public enum OrganisationType { Consortium, Partnership, Individual }

    @Id
    Integer id;

    @Column(name="title")
    String title;

    @Column
    Integer orgId;

    @Column(name="og_type")
    @Enumerated(EnumType.STRING)
    OrganisationType orgType;

    @Column(name="og_name")
    String organisationGroupName;

    @Column
    String orgName;

    @Column
    Integer programmeId;

    @Column(name = "prg_name")
    String programmeName;

    @Column
    Integer templateId;

    @Column(name = "template_name")
    String templateName;

    @Column(name = "organisation_group_id")
    Integer orgGroupId;

    @Column(name = "lead_organisation_id")
    Integer leadOrgId;

    @Column
    Boolean orgSelected;

    @Column(name = "unnapproved_changes")
    private Boolean unapprovedChanges;

    @Column
    OffsetDateTime createdOn;

    @Column
    OffsetDateTime lastModified;

    @Column
    @Enumerated(EnumType.STRING)
    Project.Status status;


    @Enumerated(EnumType.STRING)
    @Column(name = "substatus")
    Project.SubStatus subStatus;

    @Column(name = "recommendation")
    @Enumerated(EnumType.STRING)
    Project.Recommendation recommendation;

    @Column(name = "managing_organisation_id")
    private Integer managingOrganisationId;

    public String getTitle() {
        return title;
    }

    public Integer getOrgId() {
        if (orgGroupId != null) {
            return leadOrgId;
        } else {
            return orgId;
        }
    }

    public OrganisationType getOrgType() {
        if ((orgSelected == null) || (!orgSelected)) {
            return null;
        }
        if (orgType == null) {
            return OrganisationType.Individual;
        }
        return orgType;
    }

    public String getOrgName() {
        if (organisationGroupName != null) {
            return organisationGroupName;
        } else {
            return orgName;
        }
    }

    public Integer getProgrammeId() {
        return programmeId;
    }

    public String getProgrammeName() {
        return programmeName;
    }

    public Integer getTemplateId() {
        return templateId;
    }

    public String getTemplateName() {
        return templateName;
    }

    public OffsetDateTime getCreatedOn() {
        return createdOn;
    }

    public Integer getId() {
        return id;
    }

    public OffsetDateTime getLastModified() {
        return lastModified;
    }

    public Project.Status getStatus() {
        return status;
    }

    public Project.SubStatus getSubStatus() {
        return subStatus;
    }

    @Override
    public int compareTo(ProjectSummary o) {
        if (this.lastModified == null) {
            return 1;
        }
        else if (o.lastModified == null) {
            return -1;
        }
        else {
            return o.lastModified.compareTo(this.lastModified);
        }
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setOrgId(Integer orgId) {
        this.orgId = orgId;
    }

    public void setOrgType(OrganisationType orgType) {
        this.orgType = orgType;
    }

    public void setOrganisationGroupName(String organisationGroupName) {
        this.organisationGroupName = organisationGroupName;
    }

    public void setOrgName(String orgName) {
        this.orgName = orgName;
    }

    public void setProgrammeId(Integer programmeId) {
        this.programmeId = programmeId;
    }

    public void setProgrammeName(String programmeName) {
        this.programmeName = programmeName;
    }

    public void setTemplateId(Integer templateId) {
        this.templateId = templateId;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    public void setOrgGroupId(Integer orgGroupId) {
        this.orgGroupId = orgGroupId;
    }

    public void setLeadOrgId(Integer leadOrgId) {
        this.leadOrgId = leadOrgId;
    }

    public void setOrgSelected(Boolean orgSelected) {
        this.orgSelected = orgSelected;
    }

    public void setCreatedOn(OffsetDateTime createdOn) {
        this.createdOn = createdOn;
    }

    public void setLastModified(OffsetDateTime lastModified) {
        this.lastModified = lastModified;
    }

    public void setStatus(Project.Status status) {
        this.status = status;
    }

    public Project.Recommendation getRecommendation() {
        return recommendation;
    }

    public void setRecommendation(Project.Recommendation recommendation) {
        this.recommendation = recommendation;
    }

    public Boolean isUnapprovedChanges() {
        return unapprovedChanges;
    }

    public void setUnapprovedChanges(Boolean unapprovedChanges) {
        this.unapprovedChanges = unapprovedChanges;
    }

    public Integer getManagingOrganisationId() {
        return managingOrganisationId;
    }

    public void setManagingOrganisationId(Integer managingOrganisationId) {
        this.managingOrganisationId = managingOrganisationId;
    }

}
