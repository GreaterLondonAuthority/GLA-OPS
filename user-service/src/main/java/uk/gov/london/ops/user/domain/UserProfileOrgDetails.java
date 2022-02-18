/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.user.domain;

import uk.gov.london.ops.role.model.RoleNameAndDescription;

import java.util.List;

public class UserProfileOrgDetails {

    private Integer orgId;
    private String orgName;
    private Integer managingOrgId;
    private String managingOrgName;
    private String role;
    private String roleName;
    private List<RoleNameAndDescription> assignableRoles;
    private boolean approved;
    private boolean primary;
    private boolean authorisedSignatory;
    private boolean hasSingleRoleInThisOrg = true;
    private boolean hasOrgAdminRoleRequested = false;

    public Integer getOrgId() {
        return orgId;
    }

    public void setOrgId(Integer orgId) {
        this.orgId = orgId;
    }

    public String getOrgName() {
        return orgName;
    }

    public void setOrgName(String orgName) {
        this.orgName = orgName;
    }

    public String getManagingOrgName() {
        return managingOrgName;
    }

    public void setManagingOrgName(String managingOrgName) {
        this.managingOrgName = managingOrgName;
    }

    public Integer getManagingOrgId() {
        return managingOrgId;
    }

    public void setManagingOrgId(Integer managingOrgId) {
        this.managingOrgId = managingOrgId;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public boolean isApproved() {
        return approved;
    }

    public void setApproved(boolean approved) {
        this.approved = approved;
    }

    public List<RoleNameAndDescription> getAssignableRoles() {
        return assignableRoles;
    }

    public void setAssignableRoles(List<RoleNameAndDescription> assignableRoles) {
        this.assignableRoles = assignableRoles;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public boolean isPrimary() {
        return primary;
    }

    public void setPrimary(boolean primary) {
        this.primary = primary;
    }

    public boolean isAuthorisedSignatory() {
        return authorisedSignatory;
    }

    public void setAuthorisedSignatory(boolean authorisedSignatory) {
        this.authorisedSignatory = authorisedSignatory;
    }

    public boolean isHasSingleRoleInThisOrg() {
        return hasSingleRoleInThisOrg;
    }

    public void setHasSingleRoleInThisOrg(boolean hasSingleRoleInThisOrg) {
        this.hasSingleRoleInThisOrg = hasSingleRoleInThisOrg;
    }

    public boolean isHasOrgAdminRoleRequested() {
        return hasOrgAdminRoleRequested;
    }

    public void setHasOrgAdminRoleRequested(boolean hasOrgAdminRoleRequested) {
        this.hasOrgAdminRoleRequested = hasOrgAdminRoleRequested;
    }
}
