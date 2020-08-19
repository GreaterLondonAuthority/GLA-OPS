/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.framework.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import uk.gov.london.ops.audit.AuditService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class OPSAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    AuditService auditService;

    /**
     * How long to delay (in ms) before sending response to a failed logon request.
     */
    @Value("${failed-logon-delay-ms}")
    int failedLogonDelayMs;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {
        delayBeforeSendingAuthenticationFailure();

        String errorMessage;
        if (authException instanceof DisabledException) {
            auditDisabledUserLoginAttempt(request);
            errorMessage = "Your account has been deactivated. Please contact your Organisation Admin.";
        }
        else {
            errorMessage = "Sorry, your email and password combination is not recognised";
        }

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.getWriter().print(errorMessage);
    }

    private void auditDisabledUserLoginAttempt(HttpServletRequest request) {
        String username = (String) request.getAttribute("username");
        if (username != null) {
            auditService.auditActivityForUser((String) request.getAttribute("username"), "Disabled user attempted to login");
        }
        else {
            log.warn("username attribute not present in the request!");
        }
    }

    private void delayBeforeSendingAuthenticationFailure() {
        try {
            log.debug("Sleeping for {} ms after failed logon attempt", failedLogonDelayMs);
            Thread.sleep(failedLogonDelayMs);
        }
        catch (InterruptedException ignored) {
        }
    }

}
