/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.user;

import uk.gov.london.ops.organisation.Organisation;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;

/* UserInterface is defined to return logged-in user details in UserUtils for File module.
   This interface should be removed once User entity is moved from gla-ops-application
   to its own module here (user-service-client).
 */
public interface User {

    String SYSTEM_DEACTIVATED_USERNAME = "System";
    String SGW_SYSTEM_USER = "ilr.system";

    String getUsername();

    String getFirstName();

    String getLastName();

    String getFullName();

    List<Integer> getOrganisationIds();

    /*
    TODO : rename this to getOrganisations and UserEntity getOrganisations to getOrganisationEntities
     */
    Set<Organisation> getOrgs();

    Set<Integer> getManagingOrganisationsIds();

    Integer getPrimaryOrganisationId();

    Set<String> getApprovedRolesForOrg(int organisationId);

    Set<String> getApprovedRolesNames();

    boolean inOrganisation(Integer organisationId);

    boolean isEnabled();

    boolean isGla();

    boolean isOpsAdmin();

    boolean isTechAdmin();

    boolean isOrgAdmin();

    boolean isReadOnly(Integer organisationId);

    boolean hasRole(String role);

    boolean isApproved();

    String getDeactivatedBy();

    boolean wasDeactivatedBySystem();

    // TODO: temporary while moving user related classes to the user module, remove once all user related class moved to module
    @Deprecated
    void setPassword(String password);

    // TODO: ttemporary while moving user related classes to the user module, remove once all user related class moved to module
    @Deprecated
    void setPasswordExpiry(OffsetDateTime passwordExpiry);

    /**
     * @return a list of organisation ID which the user has an approved role in or is an org admin of.
     */
    List<Integer> getAccessibleOrganisationIds();

}
