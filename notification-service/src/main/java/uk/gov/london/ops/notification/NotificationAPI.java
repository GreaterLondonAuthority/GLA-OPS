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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import uk.gov.london.ops.framework.EntityType;
import uk.gov.london.ops.framework.annotations.PermissionRequired;
import uk.gov.london.ops.notification.broadcast.Broadcast;
import uk.gov.london.ops.notification.broadcast.BroadcastService;
import uk.gov.london.ops.notification.broadcast.BroadcastSummary;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.MimeMessage;
import javax.validation.Valid;
import java.io.IOException;
import java.util.List;

import static uk.gov.london.common.user.BaseRole.*;
import static uk.gov.london.ops.permission.PermissionType.*;

@RestController
@RequestMapping("/api/v1")
@Api
public class NotificationAPI {

    private final NotificationServiceImpl notificationService;
    private final EmailServiceImpl emailService;
    private final BroadcastService broadcastService;

    public NotificationAPI(NotificationServiceImpl notificationService, EmailServiceImpl emailService,
            BroadcastService broadcastService) {
        this.notificationService = notificationService;
        this.emailService = emailService;
        this.broadcastService = broadcastService;
    }

    @RequestMapping(value = "/notifications", method = RequestMethod.GET)
    public Page<UserNotificationEntity> getCurrentUsersNotifications(Pageable pageable) {
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
        notificationService.updateStatus(id, UserNotificationStatus.valueOf(status));
    }

    @PreAuthorize("isAuthenticated()")
    @RequestMapping(value = "/notifications/statuses", method = RequestMethod.PUT)
    public void updateStatuses(@RequestBody List<UpdateNotificationStatusRequest> updateStatusesRequest) {
        notificationService.updateStatuses(updateStatusesRequest);
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
    public List<ScheduledNotificationEntity> getScheduledNotifications() {
        return notificationService.getScheduledNotifications();
    }

    @Secured(OPS_ADMIN)
    @RequestMapping(value = "/scheduledNotifications/{id}", method = RequestMethod.GET)
    public ScheduledNotificationEntity getScheduledNotifications(@PathVariable Integer id) {
        return notificationService.getScheduledNotification(id);
    }

    @Secured(OPS_ADMIN)
    @RequestMapping(value = "/scheduledNotifications", method = RequestMethod.POST)
    public ScheduledNotificationEntity createScheduledNotification(
            @RequestBody ScheduledNotificationEntity scheduledNotification) {
        return notificationService.createScheduledNotification(scheduledNotification);
    }

    @Secured(OPS_ADMIN)
    @RequestMapping(value = "/scheduledNotifications/{id}", method = RequestMethod.PUT)
    public void updateScheduledNotification(@PathVariable Integer id,
            @RequestBody ScheduledNotificationEntity scheduledNotification) {
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
    public EmailEntity get(@PathVariable Integer id) {
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

        MimeMessage mail = emailService.toJavaMail(new EmailEntity(to, subject, body, null), session);
        transport.sendMessage(mail, mail.getAllRecipients());
        transport.close();

        return "Sent synchronously";
    }

    @PermissionRequired(BROADCAST)
    @RequestMapping(value = "/broadcasts", method = RequestMethod.GET)
    List<BroadcastSummary> getBroadcasts() {
        return broadcastService.getBroadcasts();
    }

    @PermissionRequired(BROADCAST)
    @RequestMapping(value = "/broadcasts/{id}", method = RequestMethod.GET)
    Broadcast getBroadcast(@PathVariable Integer id) {
        return broadcastService.getBroadcast(id);
    }

    @PermissionRequired(BROADCAST_CREATE)
    @RequestMapping(value = "/broadcasts", method = RequestMethod.POST)
    void createBroadcast(@RequestBody Broadcast broadcast) {
        broadcastService.createBroadcast(broadcast);
    }

    // using create permission then service method checks delete permission for the specific org
    @PermissionRequired(BROADCAST_CREATE)
    @RequestMapping(value = "/broadcasts/{id}", method = RequestMethod.DELETE)
    void deleteBroadcast(@PathVariable Integer id) {
        broadcastService.deleteBroadcast(id);
    }

    @PermissionRequired(BROADCAST_APPROVE)
    @RequestMapping(value = "/approveBroadcast/{id}", method = RequestMethod.PUT)
    void approveBroadcast(@PathVariable Integer id) {
        broadcastService.approveBroadcast(id);
    }

    @PermissionRequired(EMAIL_REPORTS)
    @RequestMapping(value = "/emails", method = RequestMethod.GET)
    @ApiOperation(value = "get all email summaries", notes = "get all emails")
    public Page<EmailSummary> getEmails(@RequestParam(name = "recipient", required = false) String recipient,
            @RequestParam(name = "subject", required = false) String subject,
            @RequestParam(name = "body", required = false) String body,
            Pageable pageable) {
        return emailService.getEmails(recipient, subject, body, pageable);
    }
}
