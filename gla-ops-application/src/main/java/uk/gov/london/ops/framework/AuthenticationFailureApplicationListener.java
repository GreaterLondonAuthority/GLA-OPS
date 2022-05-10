/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.framework;

import static uk.gov.london.common.GlaUtils.getRequestIp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.stereotype.Component;
import uk.gov.london.ops.audit.AuditService;

@Component
public class AuthenticationFailureApplicationListener implements ApplicationListener<AuthenticationFailureBadCredentialsEvent> {

    @Autowired
    private AuditService auditService;

    @Override
    public void onApplicationEvent(AuthenticationFailureBadCredentialsEvent event) {
        String sourceIp = getRequestIp();
        String summary = "failed to login" + (sourceIp != null ? (" " + sourceIp) : "");
        auditService.auditActivityForUser(event.getAuthentication().getName(), summary);
    }

}
