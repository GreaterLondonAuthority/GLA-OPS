/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.notification;

import uk.gov.london.ops.framework.EntityType;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

/**
 * Service for managing notifications and user notifications.
 */
public interface NotificationService {

    List<UserNotification> findAllByUsername(String username);

    void deleteUserNotification(Integer userNotificationId);

    void createNotificationForUser(NotificationType notificationType,
                                   NotificationTargetEntity targetEntity,
                                   Map<String, Object> model,
                                   String username);

    void createNotification(NotificationType notificationType, NotificationTargetEntity targetEntity, Map<String, Object> model);

    void createEmailNotification(NotificationType notificationType, NotificationTargetEntity targetEntity,
                                 Map<String, Object> model);

    List<EntitySubscription> findAllByEntityTypeAndEntityId(EntityType entityType, Integer entityId);

    boolean isUserSubscribed(String username, EntityType entityType, Integer entityId);

    Integer countByEntityTypeAndEntityId(EntityType entityType, Integer entityId);

    void subscribe(String username, EntityType entityType, Integer entityId);

    void unsubscribe(String username, EntityType entityType, Integer entityId);

    void unsubscribeFromOrganisation(String username, Integer orgId);

    void cloneEntitySubscriptions(EntityType entityType, Integer sourceEntityId, Integer cloneEntityId);

    int getUnreadNotificationCountForUser(String user);

    boolean isSubscribed(String username, EntityType entityType, Integer entityId);

    void createScheduledNotification(String text, OffsetDateTime scheduledDateTime, List<String> targetRoles,
                                     List<Integer> targetOrgIds);

    void setEmailService(EmailService emailService);

}
