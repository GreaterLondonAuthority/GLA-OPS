/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.user.domain;

import com.querydsl.core.annotations.QueryEntity;
import uk.gov.london.common.user.BaseRole;
import uk.gov.london.ops.organisation.OrganisationType;
import uk.gov.london.ops.role.model.RoleNameAndDescription;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

@Entity(name = "v_user_role_summaries")
@QueryEntity
@IdClass(UserRoleSummaryID.class)
public class UserRoleSummary {

    @Column(name = "firstname")
    private String firstName;

    @Column(name = "lastname")
    private String lastName;

    @Id
    @Column(name = "username")
    private String username;

    @Column(name = "user_id")
    private Integer userId;

    @Column(name = "approved_threshold")
    private Long approvedThreshold;

    @Column(name = "pending_threshold")
    private Long pendingThreshold;

    @Id
    @Column(name = "organisation_id")
    private Integer organisationId;

    @Column(name = "managing_organisation_id")
    private Integer managingOrganisationId;

    @Column(name = "org_name")
    private String orgName;

    @Column(name = "entity_type")
    private Integer entityTypeId;

    @Id
    @Column(name = "role")
    private String role;

    @Column(name = "approved")
    private Boolean approved;

    @Column(name = "can_have_threshold")
    private boolean canHaveThreshold;

    @Column(name = "created_on")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdOn;

    @Column(name = "authorised_signatory")
    private Boolean authorisedSignatory;

    @Transient
    private List<RoleNameAndDescription> assignableRoles;

    public UserRoleSummary() {}

    public UserRoleSummary(String username) {
        this.username = username;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Integer getOrganisationId() {
        return organisationId;
    }

    public void setOrganisationId(Integer organisationId) {
        this.organisationId = organisationId;
    }

    public Integer getManagingOrganisationId() {
        return managingOrganisationId;
    }

    public void setManagingOrganisationId(Integer managingOrganisationId) {
        this.managingOrganisationId = managingOrganisationId;
    }

    public String getOrgName() {
        return orgName;
    }

    public void setOrgName(String orgName) {
        this.orgName = orgName;
    }

    public Integer getEntityTypeId() {
        return entityTypeId;
    }

    public void setEntityTypeId(Integer entityTypeId) {
        this.entityTypeId = entityTypeId;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Boolean getApproved() {
        return approved;
    }

    public void setApproved(Boolean approved) {
        this.approved = approved;
    }

    public String getRoleDescription() {
        return BaseRole.getDescription(role);
    }

    public String getEntityType() {
        OrganisationType type = OrganisationType.fromId(entityTypeId);
        return type != null ? type.getSummary() : null;
    }

    public Long getApprovedThreshold() {
        return approvedThreshold;
    }

    public void setApprovedThreshold(Long approvedThreshold) {
        this.approvedThreshold = approvedThreshold;
    }

    public Long getPendingThreshold() {
        return pendingThreshold;
    }

    public void setPendingThreshold(Long pendingThreshold) {
        this.pendingThreshold = pendingThreshold;
    }

    public boolean getCanHaveThreshold() {
        return this.canHaveThreshold;
    }

    public Date getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(Date createdOn) {
        this.createdOn = createdOn;
    }

    public List<RoleNameAndDescription> getAssignableRoles() {
        return assignableRoles;
    }

    public void setAssignableRoles(List<RoleNameAndDescription> assignableRoles) {
        this.assignableRoles = assignableRoles;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public boolean isPending() {
        return pendingThreshold != null;
    }

    public Boolean getAuthorisedSignatory() {
        return authorisedSignatory;
    }

    public void setAuthorisedSignatory(Boolean authorisedSignatory) {
        this.authorisedSignatory = authorisedSignatory;
    }

}
