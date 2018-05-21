/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import uk.gov.london.ops.Environment;
import uk.gov.london.ops.domain.EntityType;
import uk.gov.london.ops.domain.notification.Notification;
import uk.gov.london.ops.domain.notification.NotificationType;
import uk.gov.london.ops.domain.notification.UserNotification;
import uk.gov.london.ops.domain.organisation.Organisation;
import uk.gov.london.ops.domain.finance.PaymentGroup;
import uk.gov.london.ops.domain.project.Project;
import uk.gov.london.ops.domain.user.EntitySubscription;
import uk.gov.london.ops.domain.user.User;
import uk.gov.london.ops.exception.ValidationException;
import uk.gov.london.ops.repository.EntitySubscriptionRepository;
import uk.gov.london.ops.repository.NotificationRepository;
import uk.gov.london.ops.repository.UserNotificationRepository;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for managing notifications and user notifications.
 */
@Transactional
@Service
public class NotificationService {

    @Autowired
    private UserService userService;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private EntitySubscriptionRepository entitySubscriptionRepository;

    @Autowired
    private UserNotificationRepository userNotificationRepository;

    @Autowired
    private Environment environment;

    /**
     * Returns a paged list of the current user notification.
     */
    public Page<UserNotification> getCurrentUsersNotifications(Pageable pageable) {
        User currentUser = userService.currentUser();
        if (currentUser == null) {
            throw new AccessDeniedException("User session not found!");
        }
        return userNotificationRepository.findAllByUsernameAndStatus(currentUser.getUsername(), UserNotification.Status.Active, pageable);
    }

    /**
     * Marks a user notification as read.
     */
    public void markAsRead(Integer id) {
        UserNotification userNotification = userNotificationRepository.findOne(id);
        if (!userService.currentUser().getUsername().equals(userNotification.getUsername())) {
            throw new ValidationException("cannot edit a notification not own by the current user!");
        }
        userNotification.setTimeRead(environment.now());
        userNotificationRepository.save(userNotification);
    }

    /**
     * Mark a user notification as
     * @param id
     */
    public void updateStatus(Integer id, UserNotification.Status status) {
        UserNotification userNotification = userNotificationRepository.findOne(id);
        if (!userService.currentUser().getUsername().equals(userNotification.getUsername())) {
            throw new ValidationException("cannot edit a notification not own by the current user!");
        }
        userNotification.setStatus(status);
        userNotificationRepository.save(userNotification);
    }

    /**
     * Creates a notification for a project. Followers of the project will be notified.
     */
    public Notification createNotification(NotificationType type, String text, Project project) {
        List<String> users = getSubscribers(EntityType.project, project.getId());
        return createNotification(type, text, EntityType.project, project.getId(), users);
    }

    /**
     * Creates a notification for a project to the given user list.
     */
    public Notification createNotification(NotificationType type, String text, Project project, List<String> users) {
        return createNotification(type, text, EntityType.project, project.getId(), users);
    }

    /**
     * Creates a notification for an organisation. Followers of the organisation as well a its admins (this is temporary, will
     * be refined with new requirements) will be notified.
     */
    public Notification createNotification(NotificationType type, String text, Organisation organisation) {
        List<String> users = new ArrayList<>();

        for (User user: organisation.getUserEntities()) {
            if (user.isGla() || user.isOrgAdmin(organisation)) {
                users.add(user.getUsername());
            }
        }

        users.addAll(getSubscribers(EntityType.organisation, organisation.getId()));

        return createNotification(type, text, organisation, users);
    }

    /**
     * Creates a notification for an organisation for the given users.
     */
    public Notification createNotification(NotificationType type, String text, Organisation organisation, List<String> users) {
        return createNotification(type, text, EntityType.organisation, organisation.getId(), users);
    }

    /**
     * Creates a notification for a payment group. The group payments managing organisation users which have the given
     * role will be notified.
     */
    public Notification createNotification(NotificationType type, String text, PaymentGroup paymentGroup, String role) {
        Integer projectId = paymentGroup.getLedgerEntries().get(0).getProjectId();
        List<String> users = getSubscribers(EntityType.project, projectId, role);
        return createNotification(type, text, EntityType.paymentGroup, paymentGroup.getId(), users);
    }

    /**
     * Returns the list of users subscribed to the entity identified by the given parameters to send the notifications to.
     * If the current user is a follower / subscriber of that entity it will not be returned as we don't want to notify the
     * user generating the notification.
     */
    public List<String> getSubscribers(EntityType entityType, Integer id) {
        List<String> subscribers = new ArrayList<>();

        List<EntitySubscription> subscriptions = entitySubscriptionRepository.findAllByEntityTypeAndEntityId(entityType, id);
        for (EntitySubscription subscription: subscriptions) {
            subscribers.add(subscription.getUsername());
        }

        subscribers.removeIf(s -> s.equals(userService.currentUser().getUsername()));

        return subscribers;
    }

    /**
     * Returns the list of users subscribed to the entity identified by the given parameters to send the notifications to
     * filtered by role.
     * If the current user is a follower / subscriber of that entity it will not be returned as we don't want to notify the
     * user generating the notification.
     */
    List<String> getSubscribers(EntityType entityType, Integer id, String role) {
        List<String> filteredSubscribers = new ArrayList<>();
        for (String username: getSubscribers(entityType, id)) {
            User user = userService.find(username);
            if (user.hasRole(role)) {
                filteredSubscribers.add(username);
            }
        }
        return filteredSubscribers;
    }

    /**
     * Creates a notification without link.
     */
    public Notification createNotification(NotificationType type, String text, List<String> users) {
        return createNotification(type, text, null, null, users);
    }

    /**
     * Created the main notification entity as well as the user specific notifications from the given list of subscribers.
     */
    public Notification createNotification(NotificationType type, String text, EntityType entityType, Integer entityId, List<String> users) {
        Notification notification = new Notification(type, text, entityType, entityId);
        notificationRepository.save(notification);

        for (String username: users) {
            userNotificationRepository.save(new UserNotification(username, notification));
        }

        return notification;
    }

    public void subscribe(EntitySubscription subscription) {
        entitySubscriptionRepository.save(subscription);
    }

    public void unsubscribe(String username, EntityType entityType, Integer entityId) {
        entitySubscriptionRepository.deleteByUsernameAndEntityTypeAndEntityId(username, entityType, entityId);
    }
}
