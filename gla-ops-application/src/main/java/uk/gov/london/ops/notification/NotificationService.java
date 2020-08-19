/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.notification;

import static uk.gov.london.common.user.BaseRole.GLA_ORG_ADMIN;
import static uk.gov.london.common.user.BaseRole.ORG_ADMIN;
import static uk.gov.london.ops.notification.NotificationType.UserAccessApproval;
import static uk.gov.london.ops.notification.NotificationType.UserAccessRejection;

import com.github.jknack.handlebars.Context;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.context.JavaBeanValueResolver;
import com.github.jknack.handlebars.context.MapValueResolver;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import uk.gov.london.common.GlaUtils;
import uk.gov.london.ops.annualsubmission.AnnualSubmission;
import uk.gov.london.ops.framework.EntityType;
import uk.gov.london.ops.framework.Environment;
import uk.gov.london.ops.framework.clusterlock.ClusterLock;
import uk.gov.london.ops.framework.clusterlock.ClusterLockService;
import uk.gov.london.ops.framework.exception.ValidationException;
import uk.gov.london.ops.notification.implementation.EntitySubscriptionRepository;
import uk.gov.london.ops.notification.implementation.NotificationRepository;
import uk.gov.london.ops.notification.implementation.NotificationTypeEntity;
import uk.gov.london.ops.notification.implementation.NotificationTypeRepository;
import uk.gov.london.ops.notification.implementation.ScheduledNotificationRepository;
import uk.gov.london.ops.notification.implementation.UserNotificationRepository;
import uk.gov.london.ops.organisation.OrganisationService;
import uk.gov.london.ops.organisation.model.Organisation;
import uk.gov.london.ops.payment.PaymentGroup;
import uk.gov.london.ops.project.Project;
import uk.gov.london.ops.user.UserService;
import uk.gov.london.ops.user.domain.User;

/**
 * Service for managing notifications and user notifications.
 */
@Transactional
@Service
public class NotificationService {

    private final Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    private EmailService emailService;

    @Autowired
    private OrganisationService organisationService;

    @Autowired
    private UserService userService;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private NotificationTypeRepository notificationTypeRepository;

    @Autowired
    private EntitySubscriptionRepository entitySubscriptionRepository;

    @Autowired
    private ScheduledNotificationRepository scheduledNotificationRepository;

    @Autowired
    private UserNotificationRepository userNotificationRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private Environment environment;

    @Autowired
    private ClusterLockService clusterLockService;

    public void setEmailService(EmailService emailService) {
        this.emailService = emailService;
    }

    public List<Notification> findAll() {
        return notificationRepository.findAll();
    }

    public List<UserNotification> findAllByUsername(String username) {
        return userNotificationRepository.findAllByUsername(username);
    }

    public void delete(UserNotification userNotification) {
        userNotificationRepository.delete(userNotification);
    }

    /**
     * Returns a paged list of the current user notification.
     */
    public Page<UserNotification> getCurrentUsersNotifications(Pageable pageable) {
        User currentUser = userService.currentUser();
        if (currentUser == null) {
            throw new AccessDeniedException("User session not found!");
        }
        return userNotificationRepository.findAllByUsernameAndStatus(currentUser.getUsername(),
            UserNotification.Status.Active, pageable);
    }

    /**
     * Marks a user notification as read.
     */
    public void markAsRead(Integer id) {
        UserNotification userNotification = userNotificationRepository.getOne(id);
        if (!userService.currentUser().getUsername().equals(userNotification.getUsername())) {
            throw new ValidationException("cannot edit a notification not own by the current user!");
        }
        userNotification.setTimeRead(environment.now());
        userNotificationRepository.save(userNotification);
    }

    /**
     * Mark a user notification as
     */
    public void updateStatus(Integer id, UserNotification.Status status) {
        UserNotification userNotification = userNotificationRepository.getOne(id);
        if (!userService.currentUser().getUsername().equals(userNotification.getUsername())) {
            throw new ValidationException("cannot edit a notification not own by the current user!");
        }
        userNotification.setStatus(status);
        userNotificationRepository.save(userNotification);
    }

    public void createNotificationForUser(NotificationType notificationType, NotificationTargetEntity targetEntity,
        Map<String, Object> model, String username) {
        try {
            String text = generateNotificationText(notificationType.getText(), targetEntity, model);
            createNotification(notificationType, text, targetEntity.getIdAsString(), Collections.singletonList(username));
            generateEmailForNotification(notificationType, targetEntity, model, userService.get(username));

        } catch (IOException e) {
            log.error("failed to generate notification " + notificationType, e);
        }
    }

    public void createNotification(NotificationType notificationType, NotificationTargetEntity targetEntity,
        Map<String, Object> model) {
        try {
            String text = generateNotificationText(notificationType.getText(), targetEntity, model);

            Set<User> users = getUsersToBeNotified(notificationType, targetEntity, model);

            createNotification(notificationType, text, targetEntity.getIdAsString(),
                users.stream().map(User::getUsername).collect(Collectors.toSet()));

            generateEmailForNotification(notificationType, targetEntity, model, users.toArray(new User[users.size()]));
        } catch (Exception e) {
            log.error("failed to generate notification " + notificationType, e);
        }
    }

    /**
     * Created the main notification entity as well as the user specific notifications from the given list of subscribers.
     */
    void createNotification(NotificationType type, String text, String entityId, Collection<String> users) {
        Notification notification = new Notification(type, text, entityId);
        notificationRepository.save(notification);

        for (String username : users) {
            userNotificationRepository.save(new UserNotification(username, notification));
        }
    }

    private void generateEmailForNotification(NotificationType notificationType, NotificationTargetEntity targetEntity,
        Map<String, Object> model, User... users) {
        if (notificationType.generatesEmails()) {
            Organisation organisation;

            if (targetEntity instanceof Organisation) {
                organisation = (Organisation) targetEntity;
            } else {
                organisation = (Organisation) model.get("organisation");
            }

            for (User user : users) {
                if (model.get("requester") != null) {
                    emailService.sendRegistrationRequestEmail(notificationType, (User) model.get("requester"), user, organisation);
                } else {
                    emailService.sendEmail(notificationType, user, organisation);
                }
            }
        }
    }

    private Set<User> getUsersToBeNotified(NotificationType notificationType, NotificationTargetEntity targetEntity,
        Map<String, Object> model) {
        Set<User> users = new HashSet<>();

        if (targetEntity instanceof Organisation && model != null && model.get("managingOrgId") != null) {
            users.addAll(((Organisation) targetEntity).getUsers(notificationType.getRolesNotified()));

            Organisation managingOrganisation = organisationService.findOne((Integer) model.get("managingOrgId"));
            if (managingOrganisation != null) {
                users.addAll(managingOrganisation.getUsers(notificationType.getRolesNotified()));
            }
        }

        if (targetEntity instanceof User && model != null && model.get("organisation") != null) {
            if (UserAccessApproval.equals(notificationType) || UserAccessRejection.equals(notificationType)) {
                users.add((User) targetEntity);
            } else {
                Organisation org = (Organisation) model.get("organisation");
                if (org != null) {
                    Set<User> orgUsers = org.getUsers(notificationType.getRolesNotified());
                    if (orgUsers != null && !orgUsers.isEmpty()) {
                        users.addAll(orgUsers);
                        model.put("requester", targetEntity);
                    }
                }
            }
        }

        if (targetEntity instanceof AnnualSubmission) {
            Organisation org = (Organisation) model.get("organisation");
            Set<String> subscribers = entitySubscriptionRepository.getSubscribers(EntityType.organisation, org.getId(),
                Arrays.asList(notificationType.getRolesNotified()));
            getUsersFromProjectSubscribers(users, subscribers);
        }

        if (targetEntity instanceof Project) {
            Set<String> projectSubscribers = new HashSet<>();
            projectSubscribers.addAll(this.getSubscribers(EntityType.project, ((Project) targetEntity).getId()));

            if (NotificationType.ProjectTransfer.equals(notificationType)) {
                // Add users from both organisations with admin roles
                Set<String> usersToBeNotified = new HashSet<>();
                Organisation fromOrganisation = (Organisation) model.get("fromOrganisation");
                Organisation toOrganisation = (Organisation) model.get("toOrganisation");
                usersToBeNotified.addAll(fromOrganisation.getUsernames(GLA_ORG_ADMIN, ORG_ADMIN));
                usersToBeNotified.addAll(toOrganisation.getUsernames(GLA_ORG_ADMIN, ORG_ADMIN));
                projectSubscribers.addAll(usersToBeNotified);
            }
            getUsersFromProjectSubscribers(users, projectSubscribers);
        }

        if (targetEntity instanceof PaymentGroup) {
            Set<String> projectSubscribers = getSubscribers(EntityType.project, (Integer) model.get("projectId"),
                Arrays.asList(notificationType.getRolesNotified()).get(0));

            if (NotificationType.PaymentAuthorisation.equals(notificationType) && projectSubscribers.isEmpty()) {
                projectSubscribers = getSubscribers(EntityType.project, (Integer) model.get("projectId"));
            }
            getUsersFromProjectSubscribers(users, projectSubscribers);
        }

        return users;
    }

    /**
     * Receives a set of username with all subscribers and find the user to add it to the users
     * to be notified set. In case the user cannot be find, it will log an error.
     */
    private void getUsersFromProjectSubscribers(Set<User> users, Set<String> projectSubscribers) {
        for (String username : projectSubscribers) {
            User userToBeNotified = userService.find(username);
            if (userToBeNotified != null) {
                users.add(userToBeNotified);
            } else {
                log.error("failed to generate notification because user to be notified doesn't exist. " + username);
            }
        }
    }

    private String generateNotificationText(String notificationTextTemplate, NotificationTargetEntity targetEntity,
        Map<String, Object> model) throws IOException {
        Template template = new Handlebars().compileInline(notificationTextTemplate);

        Context.Builder context = Context.newBuilder(targetEntity);
        if (model != null) {
            for (String key : model.keySet()) {
                context.combine(key, model.get(key));
            }
        }
        context.resolver(JavaBeanValueResolver.INSTANCE, MapValueResolver.INSTANCE);

        return template.apply(context.build());
    }

    /**
     * Returns the list of users subscribed to the entity identified by the given parameters to send the notifications to.
     * If the current user is a follower / subscriber of that entity it will not be
     * returned as we don't want to notify the user generating the notification.
     */
    public Set<String> getSubscribers(EntityType entityType, Integer id) {
        Set<String> subscribers = new HashSet<>();

        List<EntitySubscription> subscriptions = entitySubscriptionRepository.findAllByEntityTypeAndEntityId(entityType, id);
        for (EntitySubscription subscription : subscriptions) {
            subscribers.add(subscription.getUsername());
        }

        subscribers.removeIf(s -> s.equals(userService.currentUser().getUsername()));

        return subscribers;
    }

    /**
     * Returns the list of users subscribed to the entity identified by the given parameters to send the notifications
     * to filtered by role. If the current user is a follower / subscriber of that entity it will not be returned
     * as we don't want to notify the user generating the notification.
     */
    Set<String> getSubscribers(EntityType entityType, Integer id, String role) {
        Set<String> filteredSubscribers = new HashSet<>();
        for (String username : getSubscribers(entityType, id)) {
            User user = userService.find(username);
            if (user.hasRole(role)) {
                filteredSubscribers.add(username);
            }
        }
        return filteredSubscribers;
    }

    public List<EntitySubscription> getCurrentUsersSubscriptions(EntityType entityType) {
        return entitySubscriptionRepository.findAllByEntityTypeAndUsername(entityType, userService.currentUser().getUsername());
    }

    public List<EntitySubscription> findAllByEntityTypeAndEntityId(EntityType entityType, Integer entityId) {
        return entitySubscriptionRepository.findAllByEntityTypeAndEntityId(entityType, entityId);
    }

    public boolean isUserSubscribed(String username, EntityType entityType, Integer entityId) {
        return entitySubscriptionRepository.findFirstByUsernameAndEntityTypeAndEntityId(username, entityType, entityId) != null;
    }

    public Integer countByEntityTypeAndEntityId(EntityType entityType, Integer entityId) {
        return entitySubscriptionRepository.countByEntityTypeAndEntityId(entityType, entityId);
    }

    public void subscribe(String username, EntityType entityType, Integer entityId) {
        subscribe(new EntitySubscription(username, entityType, entityId));
    }

    public void subscribe(EntitySubscription subscription) {
        entitySubscriptionRepository.save(subscription);
    }

    public void unsubscribe(String username, EntityType entityType, Integer entityId) {
        entitySubscriptionRepository.deleteByUsernameAndEntityTypeAndEntityId(username, entityType, entityId);
    }

    public void unsubscribeFromOrganisation(String username, Integer orgId) {
        unsubscribe(username, EntityType.organisation, orgId);
        jdbcTemplate.update("DELETE FROM entity_subscription es "
            + "WHERE es.entity_type = 'project' "
            + "AND es.username = ? "
            + "AND es.entity_id in (SELECT p.id FROM project p WHERE p.org_id = ?)", username, orgId);
    }

    public int getUnreadNotificationCountForUser(String user) {
        return userNotificationRepository.countAllByUsernameAndTimeReadIsNullAndStatus(user, UserNotification.Status.Active);
    }

    public boolean isSubscribed(String username, EntityType entityType, Integer entityId) {
        return entitySubscriptionRepository.findFirstByUsernameAndEntityTypeAndEntityId(username, entityType, entityId) != null;
    }

    public List<NotificationTypeEntity> getAllNotificationTypes() {
        return notificationTypeRepository.findAll();
    }

    public List<ScheduledNotification> getScheduledNotifications() {
        return scheduledNotificationRepository.findAllByOrderByScheduledDateTimeDesc();
    }

    public ScheduledNotification getScheduledNotification(Integer id) {
        return scheduledNotificationRepository.findById(id).orElse(null);
    }

    public ScheduledNotification createScheduledNotification(ScheduledNotification scheduledNotification) {
        return scheduledNotificationRepository.save(scheduledNotification);
    }

    public void updateScheduledNotification(Integer id, ScheduledNotification scheduledNotification) {
        scheduledNotificationRepository.save(scheduledNotification);
    }

    public void deleteScheduledNotification(Integer id) {
        scheduledNotificationRepository.deleteById(id);
    }

    @Scheduled(fixedDelay = 600000)
    public void sendScheduledNotifications() {
        try {
            if (clusterLockService.takeLock(GlaUtils.generateRandomId(), ClusterLock.Type.SCHEDULED_NOTIFICATION)) {
                List<ScheduledNotification> scheduledNotificationsToBeSent = scheduledNotificationRepository
                    .findAllByStatusAndScheduledDateTimeBefore(ScheduledNotificationStatus.Scheduled, environment.now());
                for (ScheduledNotification scheduledNotification : scheduledNotificationsToBeSent) {
                    Collection<String> users = userService.findAllUsernamesFor(scheduledNotification.getTargetOrgIds(),
                        scheduledNotification.getTargetRoles());

                    createNotification(NotificationType.Adhoc, scheduledNotification.getText(), null, users);

                    scheduledNotification.setStatus(ScheduledNotificationStatus.Sent);
                    scheduledNotificationRepository.save(scheduledNotification);
                }
            } else {
                log.warn("failed to acquire lock");
            }
        } finally {
            clusterLockService.releaseLock(ClusterLock.Type.SCHEDULED_NOTIFICATION);
        }
    }

}
