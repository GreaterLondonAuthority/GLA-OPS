/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.user;

import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import uk.gov.london.ops.framework.exception.ForbiddenAccessException;
import uk.gov.london.ops.framework.feature.Feature;
import uk.gov.london.ops.framework.feature.FeatureStatus;
import uk.gov.london.ops.organisation.Organisation;
import uk.gov.london.ops.user.domain.UsernameAndPassword;

import javax.servlet.http.HttpServletRequest;

import static uk.gov.london.ops.framework.OPSUtils.currentUsername;
import static uk.gov.london.ops.user.User.SGW_SYSTEM_USER;
import static uk.gov.london.ops.user.UserUtils.currentUser;

@RestController
@RequestMapping("/api/v1")
@Api
public class AuthenticationAPI {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private FeatureStatus featureStatus;

    // TODO : IP restriction to access this API
    @PreAuthorize("authentication.name == '" + SGW_SYSTEM_USER + "'")
    @RequestMapping(value = "/authenticate", method = RequestMethod.POST)
    @ResponseBody
    public User authenticate(@RequestBody UsernameAndPassword usernameAndPassword, HttpServletRequest request) {
        // store username in session so is available for pw reset process
        request.setAttribute("username", usernameAndPassword.getUsername());
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                usernameAndPassword.getUsername().toLowerCase(), usernameAndPassword.getPassword()));

        User user = (User) authentication.getPrincipal();

        if (authenticationForSkillsGateway() && !isUserAllowedToAccessSkillsGateway(user)) {
            throw new ForbiddenAccessException();
        }

        return user;
    }

    @PreAuthorize("isAuthenticated()")
    @RequestMapping(value = "/isCurrentUserAllowedToAccessSkillsGateway", method = RequestMethod.GET)
    @ResponseBody
    public boolean isCurrentUserAllowedToAccessSkillsGateway() {
        return isUserAllowedToAccessSkillsGateway(currentUser());
    }

    private boolean authenticationForSkillsGateway() {
        return SGW_SYSTEM_USER.equals(currentUsername());
    }

    private boolean isUserAllowedToAccessSkillsGateway(User user) {
        return user.isOpsAdmin() || user.isTechAdmin()
                || user.getOrgs().stream().anyMatch(this::orgAllowedToAccessSkillsGateway);
    }

    private boolean orgAllowedToAccessSkillsGateway(Organisation organisation) {
        return organisation.isApproved()
                && (organisation.isSkillsGatewayAccessAllowed() || isExternalOrganisationAllowedSGWAccess(organisation));
    }

    private boolean isExternalOrganisationAllowedSGWAccess(Organisation organisation) {
        return organisation.getIsLearningProvider() != null && organisation.getIsLearningProvider()
                && featureStatus.isEnabled(Feature.AllowLearningProvidersSkillsGatewayAccess)
                && organisation.getUkprn() != null;
    }

}
