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
import uk.gov.london.ops.organisation.model.Organisation;
import uk.gov.london.ops.user.domain.User;
import uk.gov.london.ops.user.domain.UsernameAndPassword;

import static uk.gov.london.common.organisation.OrganisationType.LEARNING_PROVIDER;
import static uk.gov.london.ops.di.ProductionOrganisationInitialiser.SGW_SYSTEM_USER;

@RestController
@RequestMapping("/api/v1")
@Api(
        description = "Authentication API"
)
public class AuthenticationAPI {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserService userService;

    @Autowired
    private FeatureStatus featureStatus;

    // TODO : IP restriction to access this API
    @PreAuthorize("authentication.name == '"+SGW_SYSTEM_USER+"'")
    @RequestMapping(value = "/authenticate", method = RequestMethod.POST)
    @ResponseBody
    public User authenticate(@RequestBody UsernameAndPassword usernameAndPassword) {
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
        return isUserAllowedToAccessSkillsGateway(userService.currentUser());
    }

    private boolean authenticationForSkillsGateway() {
        return SGW_SYSTEM_USER.equals(userService.currentUsername());
    }

    private boolean isUserAllowedToAccessSkillsGateway(User user) {
        return user.isOpsAdmin() || user.isTechAdmin() || user.getOrganisations().stream().anyMatch(this::orgAllowedToAccessSkillsGateway);
    }

    private boolean orgAllowedToAccessSkillsGateway(Organisation organisation) {
        return organisation.isSkillsGatewayAccessAllowed() ||
                (organisation.getEntityType() == LEARNING_PROVIDER.id() && featureStatus.isEnabled(Feature.AllowLearningProvidersSkillsGatewayAccess));
    }

}
