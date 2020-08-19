/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.permission.implementation;

public class PermissionDetails {

    public enum PermissionApplicability {
        ALL,
        MY_ORG,
        NON_SPECIFIC
    }

    private String permissionDescription;

    private PermissionApplicability permissionApplicability;

    public PermissionDetails(String permissionDescription, PermissionApplicability permissionApplicability) {
        this.permissionDescription = permissionDescription;
        this.permissionApplicability = permissionApplicability;
    }

    public String getPermissionDescription() {
        return permissionDescription;
    }

    public void setPermissionDescription(String permissionDescription) {
        this.permissionDescription = permissionDescription;
    }

    public PermissionApplicability getPermissionApplicability() {
        return permissionApplicability;
    }

    public void setPermissionApplicability(PermissionApplicability permissionApplicability) {
        this.permissionApplicability = permissionApplicability;
    }
}
