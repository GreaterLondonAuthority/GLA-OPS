/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.user;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import uk.gov.london.common.error.ApiError;
import uk.gov.london.ops.framework.EntityType;
import uk.gov.london.ops.framework.annotations.PermissionRequired;
import uk.gov.london.ops.role.model.RoleModel;
import uk.gov.london.ops.service.DataAccessControlService;
import uk.gov.london.ops.user.domain.*;
import uk.gov.london.ops.user.implementation.UserScheduledService;

import javax.validation.Valid;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;

import static uk.gov.london.common.user.BaseRole.*;
import static uk.gov.london.ops.framework.OPSUtils.verifyBinding;
import static uk.gov.london.ops.permission.PermissionType.*;

/**
 * SpringMVC controller for User API endpoint.
 */
@RestController
@RequestMapping("/api/v1")
@Api("managing user data")
public class UserAPI {

    private final UserServiceImpl userService;
    private final UserFinanceThresholdService userFinanceThresholdService;
    private final UserPasswordService userPasswordService;
    private final DataAccessControlService dataAccessControlService;
    private final UserScheduledService userScheduledService;

    public UserAPI(UserServiceImpl userService, UserFinanceThresholdService userFinanceThresholdService,
                   UserPasswordService userPasswordService, DataAccessControlService dataAccessControlService,
                   UserScheduledService userScheduledService) {
        this.userService = userService;
        this.userFinanceThresholdService = userFinanceThresholdService;
        this.userPasswordService = userPasswordService;
        this.dataAccessControlService = dataAccessControlService;
        this.userScheduledService = userScheduledService;
    }

    @PreAuthorize("isAuthenticated()")
    @RequestMapping(value = "/user-roles", method = RequestMethod.GET)
    @ApiOperation(value = "get all user data", notes = "retrieves a list of all registered users")
    public Page<UserRoleSummary> getAllUserRoles(
            @RequestParam(name = "username", required = false) String userNameOrEmail,
            @RequestParam(name = "organisation", required = false) String organisationNameOrId,
            @RequestParam(required = false) List<String> registrationStatus,
            @RequestParam(required = false) List<String> roles,
            @RequestParam(required = false) List<Integer> orgTypes,
            @RequestParam(required = false) List<String> spendAuthority,
            Pageable pageable) {

        return userService.findAll(organisationNameOrId,
                userNameOrEmail,
                registrationStatus,
                roles,
                orgTypes,
                spendAuthority,
                pageable);
    }

    @PreAuthorize("isAuthenticated()")
    @RequestMapping(value = "/users", method = RequestMethod.GET)
    @ApiOperation(value = "get all user data", notes = "retrieves a list of all registered users")
    public Page<UserSummary> getAllUsers(
            @RequestParam(name = "username", required = false) String userNameOrEmail,
            @RequestParam(name = "organisation", required = false) String organisationNameOrId,
            @RequestParam(required = false) List<String> registrationStatus,
            @RequestParam(required = false) List<Boolean> userStatus,
            @RequestParam(required = false) List<String> roles,
            @RequestParam(required = false) List<Integer> orgTypes,
            @RequestParam(required = false) List<String> spendAuthority,
            Pageable pageable) {

        return userService.findAllUsers(organisationNameOrId,
                userNameOrEmail,
                registrationStatus,
                userStatus,
                roles,
                orgTypes,
                spendAuthority,
                pageable);
        }

    @RequestMapping(value = "/users", method = RequestMethod.POST)
    @ApiResponses(@ApiResponse(code = 400, message = "validation error", response = ApiError.class))
    public void register(@Valid @RequestBody UserRegistration registration, BindingResult bindingResult) {
        verifyBinding("Invalid registration request!", bindingResult);
        userService.register(registration);
    }

    @PreAuthorize("isAuthenticated()") // users without roles should still access their profile
    @RequestMapping(value = "/users/{userIdOrName}/", method = RequestMethod.GET)
    public UserProfile getUser(@PathVariable String userIdOrName) {
        return userService.getUserProfile(userIdOrName);
    }

    @PermissionRequired(USER_CHANGE_STATUS)
    @RequestMapping(value = "/users/{userIdOrName}/status", method = RequestMethod.PUT)
    public void updateUserStatus(@PathVariable String userIdOrName, @RequestParam boolean enabled) {
        userService.updateUserStatus(userIdOrName, enabled);
    }

    @PreAuthorize("isAuthenticated()")
    @RequestMapping(value = "/users/changes", method = RequestMethod.PUT)
    public void updateUserDetails(@RequestBody UserProfile userProfile) {
        userService.updateUserDetails(userProfile);
    }

    @PermissionRequired(USER_REQUEST_ORG_ADMIN)
    @RequestMapping(value = "/user/{username}/organisation/{organisationId}/requestOrgAdminRole", method = RequestMethod.PUT)
    public void requestOrgAdminRole(@PathVariable String username,
                                  @PathVariable Integer organisationId) {
        userService.requestOrgAdminRole(username, organisationId, true);
    }

    @PermissionRequired(CLOSE_REQUEST_ORG_ADMIN)
    @RequestMapping(value = "/user/{username}/organisation/{organisationId}/closeOrgAdminRequest", method = RequestMethod.PUT)
    public void closeOrgAdminRequest(@PathVariable String username,
                                  @PathVariable Integer organisationId) {
        userService.requestOrgAdminRole(username, organisationId, false);
    }

    @PermissionRequired(USER_VIEW_THRESHOLD)
    @RequestMapping(value = "/userThresholds/{userIdOrName}/", method = RequestMethod.GET)
    public Set<UserOrgFinanceThreshold> getUserThresholds(@PathVariable String userIdOrName) {
        return userFinanceThresholdService.getFinanceThresholds(userIdOrName);
    }

    @PreAuthorize("isAuthenticated()")
    @RequestMapping(value = "/userThresholds/organisation/{organisationId}", method = RequestMethod.GET)
    public Set<UserOrgFinanceThreshold> getUserThresholdsByOrgId(@PathVariable Integer organisationId) {
        return userFinanceThresholdService.getFinanceThresholdsByOrgId(organisationId);
    }

    @Secured({OPS_ADMIN,  GLA_FINANCE})
    @RequestMapping(value = "/userThresholds/{userIdOrName}/organisation/{orgId}/pendingThreshold/", method = RequestMethod.PUT)
    public UserOrgFinanceThreshold createPendingThreshold(@PathVariable String userIdOrName,
                                                          @PathVariable Integer orgId,
                                                          @Valid @RequestBody Long pendingAmount) {
        return userFinanceThresholdService.createPendingThreshold(userIdOrName, orgId, pendingAmount);
    }

    @Secured({OPS_ADMIN,  GLA_FINANCE})
    @RequestMapping(value = "/userThresholds/{userIdOrName}/organisation/{orgId}/approve/", method = RequestMethod.PUT)
    public UserOrgFinanceThreshold approvePendingThreshold(@PathVariable String userIdOrName, @PathVariable Integer orgId) {
        return userFinanceThresholdService.approvePendingThreshold(userIdOrName, orgId);
    }

    @Secured({OPS_ADMIN,  GLA_FINANCE})
    @RequestMapping(value = "/userThresholds/{userIdOrName}/organisation/{orgId}/decline/", method = RequestMethod.PUT)
    public UserOrgFinanceThreshold declineThreshold(@PathVariable String userIdOrName, @PathVariable Integer orgId) {
        return userFinanceThresholdService.declineThreshold(userIdOrName, orgId);
    }

    @PermissionRequired(USERS_ASSIGN_PRIMARY)
    @RequestMapping(value = "/users/{userIdOrName}/makePrimaryOrganisation/{orgId}/roleName/{roleName}", method = RequestMethod.PUT)
    public void setPrimaryOrgForUser(@PathVariable String userIdOrName,
                                     @PathVariable Integer orgId,
                                     @PathVariable String roleName) {
        userService.setPrimaryOrgForUser(userIdOrName, orgId, roleName);
    }

    @RequestMapping(value = "/password-reset-token", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.ACCEPTED)
    public @ResponseBody
    CreatePasswordResetTokenResponse createPasswordResetToken(@RequestBody String username) {
        return userPasswordService.createPasswordResetToken(username);
    }

    @RequestMapping(value = "/password-reset-token/{id}/{token}", method = RequestMethod.GET)
    public void getPasswordResetToken(@PathVariable Integer id, @PathVariable String token) {
        userPasswordService.getPasswordResetToken(id, token);
    }

    @RequestMapping(value = "/users/{username}/password", method = RequestMethod.PUT)
    public void resetPassword(@PathVariable String username, @Valid @RequestBody UserPasswordReset userPasswordReset,
                              BindingResult bindingResult) {
        verifyBinding("Invalid user password reset request!", bindingResult);
        userPasswordService.resetUserPassword(username, userPasswordReset);
    }

    @Secured({OPS_ADMIN})
    @RequestMapping(value = "/users/{username}/specifyPassword", method = RequestMethod.PUT)
    public void specifyPassword(@PathVariable String username, @Valid @RequestBody String newPassword) {
        userPasswordService.specifyUserPassword(username, newPassword);
    }

    @Secured({OPS_ADMIN})
    @RequestMapping(value = "/users/{username}/passwordExpiry", method = RequestMethod.PUT)
    public void expirePassword(@PathVariable String username, @RequestParam String date) {
        userPasswordService.setUserPasswordExpiry(username, OffsetDateTime.parse(date + "T00:00:00+01:00"));
    }

    @Secured({OPS_ADMIN, GLA_ORG_ADMIN, ORG_ADMIN})
    @RequestMapping(value = "/users/{username}/role", method = RequestMethod.POST)
    @ApiOperation(value = "assigns a second role to the given user")
    public void addRole(@PathVariable String username, @RequestBody RoleModel role) {
        userService.assignRole(username, role.getName(), role.getOrganisationId(), true);
    }

    @Secured({OPS_ADMIN, GLA_ORG_ADMIN, ORG_ADMIN})
    @RequestMapping(value = "/users/{username}/role", method = RequestMethod.PUT)
    @ApiOperation(value = "assigns a new role to the given user")
    public void assignRole(@PathVariable String username, @RequestBody RoleModel role) {
        userService.assignRole(username, role.getName(), role.getOrganisationId(), false);
    }

    @PermissionRequired(TEAM_ADD)
    @RequestMapping(value = "/users/team/{teamId}", method = RequestMethod.PUT)
    @ApiOperation(value = "assigns users to given team")
    public Integer assignTeam(@PathVariable Integer teamId, @RequestBody Set<UserRoleRequest> roleRequests) {
        return userService.assignRoleToUsers(roleRequests, teamId);
    }

    @Secured({OPS_ADMIN, GLA_ORG_ADMIN})
    @RequestMapping(value = "/admin/users/{username}/password", method = RequestMethod.PUT)
    public void setPassword(@PathVariable String username, @RequestBody String password) {
        userPasswordService.setPassword(username, password);
    }

    @ApiOperation(value = "calculates strength of a password", notes = "Value is in the range 0 (least secure) to 4 (most secure)")
    @RequestMapping(value = "/admin/passwordstrength", method = RequestMethod.POST)
    public int passwordStrength(@RequestBody String password) {
        return userPasswordService.passwordStrength(password).getScore();
    }

    @Secured({OPS_ADMIN, GLA_ORG_ADMIN,  GLA_READ_ONLY, PROJECT_READER, GLA_SPM, GLA_PM, ORG_ADMIN, PROJECT_EDITOR, TECH_ADMIN})
    @RequestMapping(value = "/checkCurrentUserAccess", method = RequestMethod.GET)
    public void checkCurrentUserAccess(@RequestParam EntityType entityType, @RequestParam String entityId) {
        dataAccessControlService.checkAccess(entityType, entityId);
    }

    @Secured({OPS_ADMIN})
    @RequestMapping(value = "/deactivateAllExpiredUserAccounts", method = RequestMethod.PUT)
    public void deactivateAllExpiredUserAccounts() {
        userScheduledService.deactivateExpiredUsers();
    }

    @PermissionRequired(USERS_ASSIGN_SIGNATORY)
    @RequestMapping(value = "/users/{userIdOrName}/authorisedSignatory/{orgId}/roleName/{roleName}/signatory/{signatory}",
            method = RequestMethod.PUT)
    public void setAuthorisedSignatory(@PathVariable String userIdOrName,
                                       @PathVariable Integer orgId,
                                       @PathVariable String roleName,
                                       @PathVariable Boolean signatory
    ) {
        userService.setAuthorisedSignatory(userIdOrName, orgId, roleName, signatory);
    }

}
