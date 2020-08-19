/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.notification;

import static uk.gov.london.common.user.BaseRole.GLA_FINANCE;
import static uk.gov.london.common.user.BaseRole.GLA_ORG_ADMIN;
import static uk.gov.london.common.user.BaseRole.GLA_PM;
import static uk.gov.london.common.user.BaseRole.GLA_READ_ONLY;
import static uk.gov.london.common.user.BaseRole.GLA_SPM;
import static uk.gov.london.common.user.BaseRole.OPS_ADMIN;
import static uk.gov.london.common.user.BaseRole.ORG_ADMIN;
import static uk.gov.london.common.user.BaseRole.PROJECT_EDITOR;
import static uk.gov.london.common.user.BaseRole.PROJECT_READER;
import static uk.gov.london.ops.permission.PermissionType.NOTIFICATION_LIST_VIEW;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import java.io.IOException;
import java.util.List;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.MimeMessage;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.london.ops.framework.EntityType;
import uk.gov.london.ops.framework.annotations.PermissionRequired;
import uk.gov.london.ops.notification.implementation.NotificationTypeEntity;

@RestController
@RequestMapping("/api/v1")
@Api
public class NotificationAPI {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private EmailService emailService;

    @RequestMapping(value = "/notifications", method = RequestMethod.GET)
    public Page<UserNotification> getCurrentUsersNotifications(Pageable pageable) {
        return notificationService.getCurrentUsersNotifications(pageable);
    }

    @PreAuthorize("isAuthenticated()")
    @RequestMapping(value = "/notifications/{id}/read", method = RequestMethod.PUT)
    public void markAsRead(@PathVariable Integer id) {
        notificationService.markAsRead(id);
    }

    @PreAuthorize("isAuthenticated()")
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

    @Secured(OPS_ADMIN)
    @RequestMapping(value = "/scheduledNotifications", method = RequestMethod.GET)
    public List<ScheduledNotification> getScheduledNotifications() {
        return notificationService.getScheduledNotifications();
    }

    @Secured(OPS_ADMIN)
    @RequestMapping(value = "/scheduledNotifications/{id}", method = RequestMethod.GET)
    public ScheduledNotification getScheduledNotifications(@PathVariable Integer id) {
        return notificationService.getScheduledNotification(id);
    }

    @Secured(OPS_ADMIN)
    @RequestMapping(value = "/scheduledNotifications", method = RequestMethod.POST)
    public ScheduledNotification createScheduledNotification(@RequestBody ScheduledNotification scheduledNotification) {
        return notificationService.createScheduledNotification(scheduledNotification);
    }

    @Secured(OPS_ADMIN)
    @RequestMapping(value = "/scheduledNotifications/{id}", method = RequestMethod.PUT)
    public void updateScheduledNotification(@PathVariable Integer id, @RequestBody ScheduledNotification scheduledNotification) {
        notificationService.updateScheduledNotification(id, scheduledNotification);
    }

    @Secured(OPS_ADMIN)
    @RequestMapping(value = "/scheduledNotifications/{id}", method = RequestMethod.DELETE)
    public void deleteScheduledNotification(@PathVariable Integer id) {
        notificationService.deleteScheduledNotification(id);
    }

    @Secured(OPS_ADMIN)
    @RequestMapping(value = "/sendScheduledNotifications", method = RequestMethod.POST)
    public void sendScheduledNotifications() {
        notificationService.sendScheduledNotifications();
    }

    @Secured(OPS_ADMIN)
    @RequestMapping(value = "/emails/{id}", method = RequestMethod.GET)
    public Email get(@PathVariable Integer id) {
        return emailService.find(id);
    }

    @Secured(OPS_ADMIN)
    @RequestMapping(value = "/emails/test", method = RequestMethod.GET)
    public String test(@RequestParam String to, @RequestParam String subject, @RequestParam String body,
                       @RequestParam(required = false) boolean sendSync) throws Exception {
        if (sendSync) {
            return sendMailSynchronously(to, subject, body);
        } else {
            emailService.saveEmail(to, subject, body);
            return "Request accepted";
        }
    }

    private String sendMailSynchronously(String to, String subject, String body) throws IOException, MessagingException {
        Session session = emailService.createSession();
        Transport transport = session.getTransport("smtp");
        transport.connect();

        MimeMessage mail = emailService.toJavaMail(new Email(to, subject, body, null), session);
        transport.sendMessage(mail, mail.getAllRecipients());
        transport.close();

        return "Sent synchronously";
    }

}
