/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.web.api;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import uk.gov.london.ops.domain.EntityType;
import uk.gov.london.ops.domain.user.Role;
import uk.gov.london.ops.domain.user.UserOrgFinanceThreshold;
import uk.gov.london.ops.domain.user.UserSummary;
import uk.gov.london.ops.exception.ApiError;
import uk.gov.london.ops.exception.ValidationException;
import uk.gov.london.ops.service.DataAccessControlService;
import uk.gov.london.ops.service.UserService;
import uk.gov.london.ops.web.model.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Set;

/**
 * SpringMVC controller for User API endpoint.
 */
@RestController
@RequestMapping("/api/v1")
@Api(
    description = "managing user data"
)
public class UserAPI {

    Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    private UserService userService;

    @Autowired
    private DataAccessControlService dataAccessControlService;

    @Secured({Role.OPS_ADMIN, Role.GLA_ORG_ADMIN, Role.GLA_SPM, Role.GLA_PM, Role.GLA_FINANCE, Role.GLA_READ_ONLY, Role.ORG_ADMIN, Role.PROJECT_EDITOR, Role.TECH_ADMIN})
    @RequestMapping(value = "/users", method = RequestMethod.GET)
    @ApiOperation(  value="get all user data",
            notes="retrieves a list of all registered users")
    public Page<UserSummary> getAll(
            @RequestParam(name = "username", required = false) String userNameOrEmail,
            @RequestParam(name = "organisation", required = false) String organisationNameOrId,
            @RequestParam(required = false) List<String> registrationStatus,
            @RequestParam(required = false) List<String> roles,
            @RequestParam(required = false) List<Integer> orgTypes,
            @RequestParam(required = false) List<String> spendAuthority,

            Pageable pageable) {

        //organisationNameOrId, userNameOrEmail
        return userService.findAll(organisationNameOrId,
                userNameOrEmail,
                registrationStatus,
                roles,
                orgTypes,
                spendAuthority,
                pageable);
    }

    @RequestMapping(value = "/users", method = RequestMethod.POST)
    @ApiResponses(@ApiResponse(code=400, message="validation error", response=ApiError.class))
    public void register(@Valid @RequestBody UserRegistration registration, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            throw new ValidationException("Invalid registration request!", bindingResult.getFieldErrors());
        }

        userService.register(registration);
    }

    @Secured({Role.OPS_ADMIN, Role.GLA_ORG_ADMIN, Role.GLA_SPM, Role.GLA_PM, Role.GLA_FINANCE, Role.GLA_READ_ONLY, Role.ORG_ADMIN, Role.PROJECT_EDITOR, Role.TECH_ADMIN})
    @RequestMapping(value = "/users/{username}/", method = RequestMethod.GET)
    public UserProfile getUser(@PathVariable String username) {
        return userService.getUserProfile(username);
    }


    @Secured({Role.OPS_ADMIN, Role.GLA_ORG_ADMIN, Role.GLA_SPM, Role.GLA_PM, Role.GLA_FINANCE, Role.GLA_READ_ONLY})
    @RequestMapping(value = "/userThresholds/{username}/", method = RequestMethod.GET)
    public Set<UserOrgFinanceThreshold> getUserThresholds(@PathVariable String username) {
        return userService.getFinanceThresholds(username);
    }

    @Secured({Role.OPS_ADMIN,  Role.GLA_FINANCE})
    @RequestMapping(value = "/userThresholds/{username}/organisation/{orgId}/pendingThreshold/", method = RequestMethod.PUT)
    public UserOrgFinanceThreshold createPendingThreshold(@PathVariable String username, @PathVariable Integer orgId,@Valid  @RequestBody Long pendingAmount  ) {
        return userService.createPendingThreshold(username, orgId, pendingAmount);
    }

    @Secured({Role.OPS_ADMIN,  Role.GLA_FINANCE})
    @RequestMapping(value = "/userThresholds/{username}/organisation/{orgId}/approve/", method = RequestMethod.PUT)
    public UserOrgFinanceThreshold approveThreshold(@PathVariable String username, @PathVariable Integer orgId) {
        return userService.approvePendingThreshold(username, orgId);
    }

    @Secured({Role.OPS_ADMIN,  Role.GLA_FINANCE})
    @RequestMapping(value = "/userThresholds/{username}/organisation/{orgId}/decline/", method = RequestMethod.PUT)
    public UserOrgFinanceThreshold declineThreshold(@PathVariable String username, @PathVariable Integer orgId) {
        return userService.declineThreshold(username, orgId);
    }

    @RequestMapping(value = "/password-reset-token", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.ACCEPTED)
    public @ResponseBody
    CreatePasswordResetTokenResponse createPasswordResetToken(@RequestBody String username) {
        return userService.createPasswordResetToken(username);
    }

    @RequestMapping(value = "/password-reset-token/{id}/{token}", method = RequestMethod.GET)
    public void getPasswordResetToken(@PathVariable Integer id, @PathVariable String token) {
        userService.getPasswordResetToken(id, token);
    }

    @RequestMapping(value = "/users/{username}/password", method = RequestMethod.PUT)
    public void resetPassword(@PathVariable String username, @Valid @RequestBody UserPasswordReset userPasswordReset,
                              BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            throw new ValidationException("Invalid user password reset request!", bindingResult.getFieldErrors());
        }
        userService.resetUserPassword(username, userPasswordReset);
    }

    @Secured({Role.OPS_ADMIN, Role.GLA_ORG_ADMIN, Role.ORG_ADMIN})
    @RequestMapping(value = "/users/{username}/role", method = RequestMethod.PUT)
    @ApiOperation(value="assigns a new role to the given user")
    public void assignRole(@PathVariable String username, @RequestBody RoleModel role) {
        userService.assignRole(username, role.getName(), role.getOrganisationId());
    }

    @Secured(Role.OPS_ADMIN)
    @RequestMapping(value = "/admin/users/{username}/password", method = RequestMethod.PUT)
    public String setPassword(@PathVariable String username, @RequestBody String password) {
        userService.setPassword(username,password);
        return "Password set for user " + username;
    }

    @ApiOperation(  value="calculates strength of a password",
            notes="Value is in the range 0 (least secure) to 4 (most secure)")
    @RequestMapping(value = "/admin/passwordstrength", method = RequestMethod.POST)
    public int passwordStrength(@RequestBody String password) {
        return userService.passwordStrength(password).getScore();
    }

    @Secured({Role.OPS_ADMIN, Role.GLA_ORG_ADMIN, Role.GLA_SPM, Role.GLA_PM, Role.ORG_ADMIN, Role.PROJECT_EDITOR})
    @RequestMapping(value = "/checkCurrentUserAccess", method = RequestMethod.GET)
    public void checkCurrentUserAccess(@RequestParam EntityType entityType, @RequestParam Integer entityId) {
        dataAccessControlService.checkAccess(entityType, entityId);
    }

}
