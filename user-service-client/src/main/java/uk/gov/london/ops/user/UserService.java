/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.user;

import uk.gov.london.ops.framework.OpsEntity;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface UserService {

    Set<User> getOrganisationUsersWithRoles(Integer orgId, String... roles);

    Set<String> getOrganisationUsersWithRolesUsernames(Integer orgId, String... roles);

    Set<User> getAuthorisedSignatories(Integer orgId);

    User get(String username);

    User find(String username);

    Collection<String> findAllUsernamesFor(List<Integer> targetOrgIds, List<String> targetRoles);

    String getUserFullName(String username);

    void assignRole(String username, String roleName, Integer organisationId, boolean isNewRole);

    Set<String> findAllUsernamesWithPasswordExpiryExceedingMonths(int expiryMonths);

    void updateUserStatus(String userIdOrName, boolean enabled, boolean bySystem);

    void enrich(OpsEntity opsEntity);

    //TODO: temporary while moving user related classes to the user module, remove once all user related class moved to module
    @Deprecated
    void save(User user);

    boolean canResetUserPassword(String username);

    String currentUsername();

}
