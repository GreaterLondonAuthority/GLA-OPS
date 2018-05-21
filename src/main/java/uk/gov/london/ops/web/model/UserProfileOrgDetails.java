/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.web.model;

import java.util.List;

public class UserProfileOrgDetails {

    private Integer orgId;
    private String orgName;
    private String role;
    private String roleName;
    private List<AssignableRole> assignableRoles;
    private boolean approved;
//    private boolean canHaveThreshold;



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

    public List<AssignableRole> getAssignableRoles() {
        return assignableRoles;
    }

    public void setAssignableRoles(List<AssignableRole> assignableRoles) {
        this.assignableRoles = assignableRoles;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

//    public boolean getCanHaveThreshold() {
//        return canHaveThreshold;
//    }
//    public void setCanHaveThreshold(boolean canHaveThreshold){
//        this.canHaveThreshold = canHaveThreshold;
//    }
}
