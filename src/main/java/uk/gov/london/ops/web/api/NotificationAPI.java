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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;
import uk.gov.london.ops.domain.EntityType;
import uk.gov.london.ops.domain.notification.UserNotification;
import uk.gov.london.ops.domain.user.EntitySubscription;
import uk.gov.london.ops.domain.user.Role;
import uk.gov.london.ops.domain.user.User;
import uk.gov.london.ops.service.NotificationService;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/v1")
@Api(description="notifications api")
public class NotificationAPI {

    @Autowired
    private NotificationService notificationService;

    @RequestMapping(value = "/notifications", method = RequestMethod.GET)
    public Page<UserNotification> getCurrentUsersNotifications(Pageable pageable) {
        return notificationService.getCurrentUsersNotifications(pageable);
    }

    @Secured({Role.OPS_ADMIN, Role.GLA_ORG_ADMIN, Role.GLA_SPM, Role.GLA_PM, Role.GLA_FINANCE, Role.GLA_READ_ONLY, Role.ORG_ADMIN, Role.PROJECT_EDITOR})
    @RequestMapping(value = "/notifications/{id}/read", method = RequestMethod.PUT)
    public void markAsRead(@PathVariable Integer id) {
        notificationService.markAsRead(id);
    }

    @Secured({Role.OPS_ADMIN, Role.GLA_ORG_ADMIN, Role.GLA_SPM, Role.GLA_PM, Role.GLA_FINANCE, Role.GLA_READ_ONLY, Role.ORG_ADMIN, Role.PROJECT_EDITOR})
    @RequestMapping(value = "/notifications/{id}/status", method = RequestMethod.PUT)
    public void updateStatus(@PathVariable Integer id, @RequestBody String status) {
        notificationService.updateStatus(id, UserNotification.Status.valueOf(status));
    }

    @Secured({Role.OPS_ADMIN, Role.GLA_ORG_ADMIN, Role.GLA_SPM, Role.GLA_PM, Role.GLA_FINANCE, Role.GLA_READ_ONLY, Role.ORG_ADMIN, Role.PROJECT_EDITOR})
    @RequestMapping(value = "/subscriptions", method = RequestMethod.POST)
    public void subscribe(@Valid @RequestBody EntitySubscription subscription) {
        notificationService.subscribe(subscription);
    }

    @Secured({Role.OPS_ADMIN, Role.GLA_ORG_ADMIN, Role.GLA_SPM, Role.GLA_PM, Role.GLA_FINANCE, Role.GLA_READ_ONLY, Role.ORG_ADMIN, Role.PROJECT_EDITOR})
    @RequestMapping(value = "/subscriptions/{username}/{entityType}/{entityId}", method = RequestMethod.DELETE)
    public void unsubscribe(@PathVariable String username, @PathVariable EntityType entityType, @PathVariable Integer entityId) {
        notificationService.unsubscribe(username, entityType, entityId);
    }

}
