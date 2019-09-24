/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.service;

public class PermissionDetails {

    public enum PermisisonApplicability {
        ALL,
        MY_ORG,
        NON_SPECIFIC
    }

    private String permissionDescription;

    private PermisisonApplicability permissionApplicability;

    public PermissionDetails(String permissionDescription, PermisisonApplicability permissionApplicability) {
        this.permissionDescription = permissionDescription;
        this.permissionApplicability = permissionApplicability;
    }

    public String getPermissionDescription() {
        return permissionDescription;
    }

    public void setPermissionDescription(String permissionDescription) {
        this.permissionDescription = permissionDescription;
    }

    public PermisisonApplicability getPermissionApplicability() {
        return permissionApplicability;
    }

    public void setPermissionApplicability(PermisisonApplicability permissionApplicability) {
        this.permissionApplicability = permissionApplicability;
    }
}
