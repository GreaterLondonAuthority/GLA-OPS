/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.project;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.querydsl.core.annotations.QueryEntity;
import uk.gov.london.ops.framework.jpa.NonJoin;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * Project summary for use by API.
 *
 * @author Steve Leach
 */
@Entity(name = "v_project_summaries_with_acl_users")
@QueryEntity
@NonJoin("Summary entity, does not provide join information")
public class ProjectSummary implements Comparable<ProjectSummary> {

    public enum OrganisationType {
        Consortium, Partnership, Individual
    }

    @Id
    Integer id;

    @Column(name = "title")
    String title;

    @Column
    Integer orgId;

    @Column(name = "og_name")
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

    @Column(name = "unnapproved_changes")
    private Boolean unapprovedChanges;

    @Column
    OffsetDateTime createdOn;

    @Column
    OffsetDateTime lastModified;

    @Column(name = "status")
    String statusName;

    @Column(name = "substatus")
    String subStatusName;

    @Column(name = "recommendation")
    @Enumerated(EnumType.STRING)
    Project.Recommendation recommendation;

    @Column(name = "state")
    private String state;

    @Column(name = "managing_organisation_id")
    private Integer managingOrganisationId;

    @JsonIgnore
    @Column(name = "subscriptions")
    private String subscriptions;

    @Column(name = "acl_user")
    private String aclUser;

    @Column(name = "assignee_username")
    private String assignee;

    @Column(name = "assignee_name")
    private String assigneeName;

    @Column(name = "is_programme_allocation")
    private Boolean isProgrammeAllocation;

    @Transient
    private BigDecimal allocationTotal;

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

    public String getStatusName() {
        return statusName;
    }

    public void setStatusName(String statusName) {
        this.statusName = statusName;
    }

    public String getSubStatusName() {
        return subStatusName;
    }

    public void setSubStatusName(String subStatusName) {
        this.subStatusName = subStatusName;
    }

    @Override
    public int compareTo(ProjectSummary o) {
        if (this.lastModified == null) {
            return 1;
        } else if (o.lastModified == null) {
            return -1;
        } else {
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

    public void setCreatedOn(OffsetDateTime createdOn) {
        this.createdOn = createdOn;
    }

    public void setLastModified(OffsetDateTime lastModified) {
        this.lastModified = lastModified;
    }

    public Project.Recommendation getRecommendation() {
        return recommendation;
    }

    public void setRecommendation(Project.Recommendation recommendation) {
        this.recommendation = recommendation;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
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

    public String getSubscriptions() {
        return subscriptions;
    }

    public void setSubscriptions(String subscriptions) {
        this.subscriptions = subscriptions;
    }

    public String getAclUser() {
        return aclUser;
    }

    public void setAclUser(String aclUser) {
        this.aclUser = aclUser;
    }

    public String getAssignee() {
        return assignee;
    }

    public void setAssignee(String assignee) {
        this.assignee = assignee;
    }

    public String getAssigneeName() {
        return assigneeName;
    }

    public void setAssigneeName(String assigneeName) {
        this.assigneeName = assigneeName;
    }

    public Boolean getProgrammeAllocation() {
        return isProgrammeAllocation;
    }

    public void setProgrammeAllocation(Boolean programmeAllocation) {
        isProgrammeAllocation = programmeAllocation;
    }

    public BigDecimal getAllocationTotal() {
        return allocationTotal;
    }

    public void setAllocationTotal(BigDecimal allocationTotal) {
        this.allocationTotal = allocationTotal;
    }
}
