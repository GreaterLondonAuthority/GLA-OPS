/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.permission;

public interface PermissionService {

    boolean currentUserHasPermission(PermissionType permission);

    boolean currentUserHasPermission(String permission);

    boolean currentUserHasPermissionForOrganisation(PermissionType permission, Integer orgId);

    boolean currentUserHasPermissionForOrganisation(String permission, Integer orgId);

}
