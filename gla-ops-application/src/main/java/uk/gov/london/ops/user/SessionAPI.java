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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import uk.gov.london.ops.framework.environment.Environment;
import uk.gov.london.ops.permission.PermissionServiceImpl;
import uk.gov.london.ops.user.domain.UserEntity;
import uk.gov.london.ops.user.domain.UserModel;
import uk.gov.london.ops.user.domain.UsernameAndPassword;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/v1")
@Api(description = "managing user sessions")
public class SessionAPI {

    public static final String CURRENT = "_current";

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UserMapper userMapper;

    @Autowired
    PermissionServiceImpl permissionService;

    @Autowired
    UserServiceImpl userService;

    @Autowired
    Environment environment;

    @Value("${session.idle.duration}")
    private Integer sessionIdleDuration;

    @Value("${session.timeout.duration}")
    private Integer sessionTimeoutDuration;

    @Value("${session.keep.alive.interval}")
    private Integer sessionKeepAliveInterval;


    @RequestMapping(value = "/sessions", method = RequestMethod.POST)
    @ResponseBody
    public Session create(@RequestBody UsernameAndPassword usernameAndPassword, HttpServletRequest request) {
        // force generation of a new session ID
        SecurityContextHolder.clearContext();
        request.getSession().invalidate();

        // in case of authentication failure, we want to be able to access the username from the request
        request.setAttribute("username", usernameAndPassword.getUsername());

        Authentication auth = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                usernameAndPassword.getUsername().toLowerCase(), usernameAndPassword.getPassword()));

        UserEntity user = (UserEntity) auth.getPrincipal();

        SecurityContextHolder.getContext().setAuthentication(auth);

        String id = validatedSessionId(CURRENT);

        userService.updateSuccessfulUserLogon(user);
        UserModel userModel = userMapper.toModel(user);
        userModel.setPermissions(permissionService.getPermissionsForUser(user));
        userModel.setIdleDuration(sessionIdleDuration);
        userModel.setTimeoutDuration(sessionTimeoutDuration);
        userModel.setKeepAliveInterval(sessionKeepAliveInterval);
        return new Session(id, userModel);
    }

    String validatedSessionId(String id) {
        String currentSessionId = RequestContextHolder.currentRequestAttributes().getSessionId();

        if (id == null) {
            throw new RuntimeException("Cannot determine valid session ID");
        }
        if (id.equals(CURRENT)) {
            return currentSessionId;
        }
        if (!currentSessionId.equals(id)) {
            throw new RuntimeException("Not authorised");
        }

        return id;
    }

    @RequestMapping(value = "/sessions/{id}", method = RequestMethod.GET)
    @ResponseBody
    public Session get(@PathVariable String id) {
        return new Session(validatedSessionId(id), null);
    }

    @RequestMapping(value = "/sessions/{id}", method = RequestMethod.DELETE)
    public void delete(@PathVariable String id) {
        if (validatedSessionId(id) == null) {
            throw new UnsupportedOperationException("API only currently supports deleting active session");
        } else {
            SecurityContextHolder.clearContext();
        }
    }

}
