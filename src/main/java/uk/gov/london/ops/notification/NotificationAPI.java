/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.notification;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import uk.gov.london.ops.domain.EntityType;
import uk.gov.london.ops.notification.implementation.NotificationTypeEntity;
import uk.gov.london.ops.framework.annotations.PermissionRequired;

import javax.validation.Valid;
import java.util.List;

import static uk.gov.london.common.user.BaseRole.*;
import static uk.gov.london.ops.service.PermissionType.NOTIFICATION_LIST_VIEW;

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

    @Secured({OPS_ADMIN, GLA_ORG_ADMIN, GLA_SPM, GLA_PM, GLA_FINANCE, GLA_READ_ONLY, ORG_ADMIN, PROJECT_EDITOR, PROJECT_READER})
    @RequestMapping(value = "/notifications/{id}/read", method = RequestMethod.PUT)
    public void markAsRead(@PathVariable Integer id) {
        notificationService.markAsRead(id);
    }

    @Secured({OPS_ADMIN, GLA_ORG_ADMIN, GLA_SPM, GLA_PM, GLA_FINANCE, GLA_READ_ONLY, ORG_ADMIN, PROJECT_EDITOR, PROJECT_READER})
    @RequestMapping(value = "/notifications/{id}/status", method = RequestMethod.PUT)
    public void updateStatus(@PathVariable Integer id, @RequestBody String status) {
        notificationService.updateStatus(id, UserNotification.Status.valueOf(status));
    }

    @PreAuthorize("isAuthenticated()")
    @RequestMapping(value = "/subscriptions", method = RequestMethod.GET)
    public List<EntitySubscription> getCurrentUsersSubscriptions(@RequestParam EntityType entityType) {
        return notificationService.getCurrentUsersSubscriptions(entityType);
    }

    @Secured({OPS_ADMIN, GLA_ORG_ADMIN, GLA_SPM, GLA_PM, GLA_FINANCE, GLA_READ_ONLY, ORG_ADMIN, PROJECT_EDITOR, PROJECT_READER})
    @RequestMapping(value = "/subscriptions", method = RequestMethod.POST)
    public void subscribe(@Valid @RequestBody EntitySubscription subscription) {
        notificationService.subscribe(subscription);
    }

    @Secured({OPS_ADMIN, GLA_ORG_ADMIN, GLA_SPM, GLA_PM, GLA_FINANCE, GLA_READ_ONLY, ORG_ADMIN, PROJECT_EDITOR, PROJECT_READER})
    @RequestMapping(value = "/subscriptions/{username}/{entityType}/{entityId}", method = RequestMethod.DELETE)
    public void unsubscribe(@PathVariable String username, @PathVariable EntityType entityType, @PathVariable Integer entityId) {
        notificationService.unsubscribe(username, entityType, entityId);
    }

    @PermissionRequired(NOTIFICATION_LIST_VIEW)
    @RequestMapping(value = "/notificationTypes", method = RequestMethod.GET)
    @ApiOperation(value = "get all notification types", notes = "get all notification types")
    public List<NotificationTypeEntity> getNotificationTypes() {
        return notificationService.getAllNotificationTypes();
    }

}
