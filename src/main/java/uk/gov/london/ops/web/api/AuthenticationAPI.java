/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.web.api;

import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import uk.gov.london.ops.domain.user.User;
import uk.gov.london.ops.web.model.UsernameAndPassword;

@RestController
@RequestMapping("/api/v1")
@Api(
        description = "Authentication API"
)
public class AuthenticationAPI {

    @Autowired
    private AuthenticationManager authenticationManager;

    // TODO : IP restriction to access this API
    @PreAuthorize("authentication.name == 'ilr.system@gla.ops'")
    @RequestMapping(value = "/authenticate", method = RequestMethod.POST)
    @ResponseBody
    public User authenticate(@RequestBody UsernameAndPassword usernameAndPassword) {
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                usernameAndPassword.getUsername().toLowerCase(), usernameAndPassword.getPassword()));
        return (User) authentication.getPrincipal();
    }

}
