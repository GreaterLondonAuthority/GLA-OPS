/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.notification;

import com.github.jknack.handlebars.Context;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.context.JavaBeanValueResolver;
import com.github.jknack.handlebars.context.MapValueResolver;
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
import uk.gov.london.ops.framework.EntityType;
import uk.gov.london.ops.framework.clusterlock.ClusterLock;
import uk.gov.london.ops.framework.clusterlock.ClusterLockService;
import uk.gov.london.ops.framework.environment.Environment;
import uk.gov.london.ops.framework.exception.ValidationException;
import uk.gov.london.ops.notification.implementation.repository.*;
import uk.gov.london.ops.organisation.Organisation;
import uk.gov.london.ops.project.ProjectFacade;
import uk.gov.london.ops.user.User;
import uk.gov.london.ops.user.UserService;

import javax.transaction.Transactional;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static uk.gov.london.common.user.BaseRole.GLA_ORG_ADMIN;
import static uk.gov.london.common.user.BaseRole.ORG_ADMIN;
import static uk.gov.london.ops.framework.OPSUtils.currentUsername;
import static uk.gov.london.ops.notification.NotificationType.UserAccessApproval;
import static uk.gov.london.ops.notification.NotificationType.UserAccessRejection;
import static uk.gov.london.ops.user.UserUtils.currentUser;

/**
 * Service for managing notifications and user notifications.
 */
@Transactional
@Service
public class NotificationServiceImpl implements NotificationService {

    private final Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    private EmailService emailService;

    @Autowired
    private ProjectFacade projectFacade;

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

    public List<UserNotification> findAllByUsername(String username) {
        return userNotificationRepository.findAllByUsername(username)
                .stream()
                .map(this::toModel)
                .collect(Collectors.toList());
    }

    public void deleteUserNotification(Integer userNotificationId) {
        userNotificationRepository.deleteById(userNotificationId);
    }

    /**
     * Returns a paged list of the current user notification.
     */
    public Page<UserNotificationEntity> getCurrentUsersNotifications(Pageable pageable) {
        User currentUser = currentUser();
        if (currentUser == null) {
            throw new AccessDeniedException("User session not found!");
        }
        return userNotificationRepository.findAllByUsernameAndStatus(currentUser.getUsername(),
                UserNotificationStatus.Active, pageable);
    }

    /**
     * Marks a user notification as read.
     */
    public void markAsRead(Integer id) {
        UserNotificationEntity userNotification = userNotificationRepository.getOne(id);
        if (!Objects.equals(currentUsername(), userNotification.getUsername())) {
            throw new ValidationException("cannot edit a notification not own by the current user!");
        }
        userNotification.setTimeRead(environment.now());
        userNotificationRepository.save(userNotification);
    }

    /**
     * Mark a user notification as
     */
    public void updateStatus(Integer id, UserNotificationStatus status) {
        UserNotificationEntity userNotification = userNotificationRepository.getOne(id);
        if (!Objects.equals(currentUsername(), userNotification.getUsername())) {
            throw new ValidationException("cannot edit a notification not own by the current user!");
        }
        userNotification.setStatus(status);
        userNotificationRepository.save(userNotification);
    }

    public void updateStatuses(List<UpdateNotificationStatusRequest> updateStatusesRequest) {
        for (UpdateNotificationStatusRequest updateStatusRequest : updateStatusesRequest) {
            updateStatus(updateStatusRequest.getId(), updateStatusRequest.getStatus());
        }
    }

    public void createNotificationForUser(NotificationType notificationType, NotificationTargetEntity targetEntity,
            Map<String, Object> model, String username) {
        NotificationTypeEntity notificationTypeEntity = notificationTypeRepository.findByType(notificationType);
        try {
            String text = generateNotificationText(notificationTypeEntity.getText(), targetEntity, model);
            createNotification(notificationTypeEntity, text, targetEntity.getIdAsString(), Collections.singletonList(username));
            generateEmailForNotification(notificationTypeEntity, targetEntity, model, Collections.singleton(userService.get(username)));

        } catch (IOException e) {
            log.error("failed to generate notification " + notificationType, e);
        }
    }

    public void createNotification(NotificationType notificationType, NotificationTargetEntity targetEntity,
            Map<String, Object> model) {
        NotificationTypeEntity notificationTypeEntity = notificationTypeRepository.findByType(notificationType);
        try {
            String text = generateNotificationText(notificationTypeEntity.getText(), targetEntity, model);

            Set<User> users = getUsersToBeNotified(notificationTypeEntity, targetEntity, model);

            createNotification(notificationTypeEntity, text, targetEntity.getIdAsString(),
                    users.stream().map(User::getUsername).collect(Collectors.toSet()));

            generateEmailForNotification(notificationTypeEntity, targetEntity, model, users);
        } catch (Exception e) {
            log.error("failed to generate notification " + notificationType, e);
        }
    }

    @Override
    public void createEmailNotification(NotificationType notificationType, NotificationTargetEntity targetEntity,
                                         Map<String, Object> model) {
        NotificationTypeEntity notificationTypeEntity = notificationTypeRepository.findByType(notificationType);
        try {
            for (User user :  getUsersToBeNotified(notificationTypeEntity, targetEntity, model)) {
                model.put("recipient", user.getUsername());
                model.put("user", user);
                emailService.sendEmailNotification(notificationType, model);
            }
        } catch (Exception e) {
            log.error("failed to generate emails notification " + notificationType, e);
        }
    }

    /**
     * Created the main notification entity as well as the user specific notifications from the given list of subscribers.
     */
    void createNotification(NotificationTypeEntity type, String text, String entityId, Collection<String> users) {
        NotificationEntity notification = new NotificationEntity(type, text, entityId);
        notificationRepository.save(notification);

        for (String username : users) {
            userNotificationRepository.save(new UserNotificationEntity(username, notification));
        }
    }

    private void generateEmailForNotification(NotificationTypeEntity notificationType, NotificationTargetEntity targetEntity,
            Map<String, Object> model, Set<User> users) {
        if (notificationType.generatesEmails()) {
            Organisation organisation;

            if (notificationType.getEntityType().equals(EntityType.organisation)) {
                organisation = (Organisation) targetEntity;
            } else {
                organisation = (Organisation) model.get("organisation");
            }

            for (User user : users) {
                if (model.get("requester") != null) {
                    emailService.sendRegistrationRequestEmail(notificationType.getType(), (User) model.get("requester"), user,
                            organisation);
                } else if (model.get("scheduledDate") != null) {
                    emailService.sendPaymentSchedulerSummaryEmail(notificationType.getType(), user, model);
                } else if (model.get("paymentIds") != null) {
                    model.put("recipient", user.getUsername());
                    model.put("user", user);
                    emailService.sendEmailNotification(notificationType.getType(), model);
                } else {
                    emailService.sendEmail(notificationType.getType(), targetEntity, user, organisation);
                }
            }
        }
    }

    private Set<User> getUsersToBeNotified(NotificationTypeEntity notificationType, NotificationTargetEntity targetEntity,
            Map<String, Object> model) {
        switch (notificationType.getEntityType()) {
            case organisation:
                return getUsersToBeNotifiedForOrganisation(notificationType, targetEntity, model);

            case user:
                return getUsersToBeNotifiedForUserNotification(notificationType, targetEntity, model);

            case annualSubmission:
                return getUsersToBeNotifiedForAnnualSubmission(notificationType, model);

            case project:
                return getUsersToBeNotifiedForProject(notificationType, targetEntity, model);

            case payment:
                return getUsersToBeNotifiedForPayment(notificationType, model);

            case paymentGroup:
                return getUsersToBeNotifiedForPaymentGroup(notificationType, model);

            case organisationSignatory:
                return getAuthorisedOrgSignatory(notificationType, model);

            default:
                log.warn("no users to be notified for entity type: " + notificationType.getEntityType());
                return Collections.emptySet();
        }
    }

    private Set<User> getUsersToBeNotifiedForOrganisation(NotificationTypeEntity notificationType,
            NotificationTargetEntity targetEntity,
            Map<String, Object> model) {
        Set<User> users = new HashSet<>();
        if (model != null && model.get("managingOrgId") != null) {
            Set<User> orgUsers = userService
                    .getOrganisationUsersWithRoles(((Organisation) targetEntity).getId(), notificationType.getRolesNotified());
            users.addAll(orgUsers);

            Set<User> managingOrgUsers = userService
                    .getOrganisationUsersWithRoles((Integer) model.get("managingOrgId"), notificationType.getRolesNotified());
            users.addAll(managingOrgUsers);
        }
        return users;
    }

    private Set<User> getUsersToBeNotifiedForUserNotification(NotificationTypeEntity notificationType,
            NotificationTargetEntity targetEntity,
            Map<String, Object> model) {
        Set<User> users = new HashSet<>();
        if (model != null && model.get("organisation") != null) {
            if (UserAccessApproval.equals(notificationType.getType()) || UserAccessRejection.equals(notificationType.getType())) {
                users.add((User) targetEntity);
            } else {
                Organisation org = (Organisation) model.get("organisation");
                if (org != null && org.isApproved()) {
                    Set<User> orgUsers = userService.getOrganisationUsersWithRoles(org.getId(),
                            notificationType.getRolesNotified());
                    if (orgUsers != null && !orgUsers.isEmpty()) {
                        users.addAll(orgUsers);
                        model.put("requester", targetEntity);
                    }
                }
            }
        }
        return users;
    }

    private Set<User> getUsersToBeNotifiedForAnnualSubmission(NotificationTypeEntity notificationType, Map<String, Object> model) {
        Organisation org = (Organisation) model.get("organisation");
        Set<String> subscribers = entitySubscriptionRepository.getSubscribers(EntityType.organisation, org.getId(),
                Arrays.asList(notificationType.getRolesNotified()));
        return getUsersFromUsernames(subscribers);
    }

    private Set<User> getUsersToBeNotifiedForProject(NotificationTypeEntity notificationType, NotificationTargetEntity targetEntity,
            Map<String, Object> model) {
        Set<String> usernames = null;
        if (NotificationTargetUsersType.Assignees.equals(notificationType.getTargetUsersType())) {
            usernames = projectFacade.getProjectAssignees(targetEntity.getId());
        } else {
            usernames = this.getSubscribers(EntityType.project, targetEntity.getId());
        }

        if (NotificationType.ProjectTransfer.equals(notificationType.getType())) {
            // Add users from both organisations with admin roles
            Set<String> usersToBeNotified = new HashSet<>();
            Organisation fromOrganisation = (Organisation) model.get("fromOrganisation");
            Organisation toOrganisation = (Organisation) model.get("toOrganisation");
            usersToBeNotified.addAll(userService.getOrganisationUsersWithRolesUsernames(fromOrganisation.getId(), GLA_ORG_ADMIN, ORG_ADMIN));
            usersToBeNotified.addAll(userService.getOrganisationUsersWithRolesUsernames(toOrganisation.getId(), GLA_ORG_ADMIN, ORG_ADMIN));
            usernames.addAll(usersToBeNotified);
        }
        return getUsersFromUsernames(usernames);
    }

    private Set<User> getUsersToBeNotifiedForPayment(NotificationTypeEntity notificationType, Map<String, Object> model) {

        Set<String> projectSubscribers = getSubscribers(EntityType.project, (Integer) model.get("projectId"),
                Arrays.asList(notificationType.getRolesNotified()).get(0));

        if (NotificationType.PaymentAuthorisation.equals(notificationType.getType()) && projectSubscribers.isEmpty()) {
            projectSubscribers = getSubscribers(EntityType.project, (Integer) model.get("projectId"));
        }

        return getUsersFromUsernames(projectSubscribers);
    }

    private Set<User> getUsersToBeNotifiedForPaymentGroup(NotificationTypeEntity notificationType, Map<String, Object> model) {
        //watchers on the project
        Set<String> projectSubscribers = getSubscribers(EntityType.project, (Integer) model.get("projectId"),
                                               Arrays.asList(notificationType.getRolesNotified()).get(0));

        // + project assignees
        if (NotificationTargetUsersType.Assignees.equals(notificationType.getTargetUsersType())) {
            projectSubscribers.addAll(projectFacade.getProjectAssignees((Integer) model.get("projectId"),
                                           Arrays.stream(notificationType.getRolesNotified()).collect(Collectors.toSet())));
        }
        projectSubscribers.removeIf(s -> s.equals(currentUsername()));

        return getUsersFromUsernames(projectSubscribers);
    }

    private Set<User> getAuthorisedOrgSignatory(NotificationTypeEntity notificationType, Map<String, Object> model) {
        if (NotificationTargetUsersType.Signatory.equals(notificationType.getTargetUsersType())) {
            return  userService.getAuthorisedSignatories((Integer) model.get("organisationId"));
        }
        return null;
    }

    /**
     * Receives a set of username with all subscribers and find the user to add it to the users to be notified set. In case the
     * user cannot be find, it will log an error.
     */
    private Set<User> getUsersFromUsernames(Set<String> usernames) {
        Set<User> users = new HashSet<>();
        for (String username : usernames) {
            User userToBeNotified = userService.find(username);
            if (userToBeNotified != null) {
                users.add(userToBeNotified);
            } else {
                log.error("failed to generate notification because user to be notified doesn't exist. " + username);
            }
        }
        return users;
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
     * Returns the list of users subscribed to the entity identified by the given parameters to send the notifications to. If the
     * current user is a follower / subscriber of that entity it will not be returned as we don't want to notify the user
     * generating the notification.
     */
    public Set<String> getSubscribers(EntityType entityType, Integer id) {
        Set<String> subscribers = new HashSet<>();

        List<EntitySubscriptionEntity> subscriptions = entitySubscriptionRepository
                .findAllByEntityTypeAndEntityId(entityType, id);
        for (EntitySubscriptionEntity subscription : subscriptions) {
            subscribers.add(subscription.getUsername());
        }

        subscribers.removeIf(s -> s.equals(currentUsername()));

        return subscribers;
    }

    /**
     * Returns the list of users subscribed to the entity identified by the given parameters to send the notifications to filtered
     * by role. If the current user is a follower / subscriber of that entity it will not be returned as we don't want to notify
     * the user generating the notification.
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
        return entitySubscriptionRepository.findAllByEntityTypeAndUsername(entityType, currentUsername())
                .stream()
                .map(this::toModel)
                .collect(Collectors.toList());
    }

    public List<EntitySubscription> findAllByEntityTypeAndEntityId(EntityType entityType, Integer entityId) {
        return entitySubscriptionRepository.findAllByEntityTypeAndEntityId(entityType, entityId)
                .stream()
                .map(this::toModel)
                .collect(Collectors.toList());
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
        entitySubscriptionRepository.save(toEntity(subscription));
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

    public void cloneEntitySubscriptions(EntityType entityType, Integer sourceEntityId, Integer cloneEntityId) {
        List<EntitySubscription> subscriptions = findAllByEntityTypeAndEntityId(entityType, sourceEntityId);
        for (EntitySubscription subscription : subscriptions) {
            subscribe(subscription.getUsername(), entityType, cloneEntityId);
        }
    }

    private UserNotification toModel(UserNotificationEntity entity) {
        return new UserNotification(entity.getId(), entity.getNotification().getText());
    }

    private EntitySubscription toModel(EntitySubscriptionEntity entity) {
        return new EntitySubscription(entity.getUsername(), entity.getEntityType(), entity.getEntityId());
    }

    private EntitySubscriptionEntity toEntity(EntitySubscription model) {
        return new EntitySubscriptionEntity(model.getUsername(), model.getEntityType(), model.getEntityId());
    }

    public int getUnreadNotificationCountForUser(String user) {
        return userNotificationRepository.countAllByUsernameAndTimeReadIsNullAndStatus(user, UserNotificationStatus.Active);
    }

    public boolean isSubscribed(String username, EntityType entityType, Integer entityId) {
        return entitySubscriptionRepository.findFirstByUsernameAndEntityTypeAndEntityId(username, entityType, entityId) != null;
    }

    public List<NotificationTypeEntity> getAllNotificationTypes() {
        return notificationTypeRepository.findAll();
    }

    public List<ScheduledNotificationEntity> getScheduledNotifications() {
        return scheduledNotificationRepository.findAllByOrderByScheduledDateTimeDesc();
    }

    public ScheduledNotificationEntity getScheduledNotification(Integer id) {
        return scheduledNotificationRepository.findById(id).orElse(null);
    }

    public ScheduledNotificationEntity createScheduledNotification(ScheduledNotificationEntity scheduledNotification) {
        return scheduledNotificationRepository.save(scheduledNotification);
    }

    public void createScheduledNotification(String text, OffsetDateTime scheduledDateTime, List<String> targetRoles,
            List<Integer> targetOrgIds) {
        createScheduledNotification(new ScheduledNotificationEntity(text, scheduledDateTime,
                ScheduledNotificationStatus.Scheduled, targetRoles, targetOrgIds));
    }

    public void updateScheduledNotification(Integer id, ScheduledNotificationEntity scheduledNotification) {
        scheduledNotificationRepository.save(scheduledNotification);
    }

    public void deleteScheduledNotification(Integer id) {
        scheduledNotificationRepository.deleteById(id);
    }

    @Scheduled(fixedDelay = 600000)
    public void sendScheduledNotifications() {
        try {
            if (clusterLockService.takeLock(GlaUtils.generateRandomId(), ClusterLock.Type.SCHEDULED_NOTIFICATION)) {
                List<ScheduledNotificationEntity> scheduledNotificationsToBeSent = scheduledNotificationRepository
                        .findAllByStatusAndScheduledDateTimeBefore(ScheduledNotificationStatus.Scheduled, environment.now());
                for (ScheduledNotificationEntity scheduledNotification : scheduledNotificationsToBeSent) {
                    Collection<String> users = userService.findAllUsernamesFor(scheduledNotification.getTargetOrgIds(),
                            scheduledNotification.getTargetRoles());

                    NotificationTypeEntity adhocNotificationType = notificationTypeRepository.findByType(NotificationType.Adhoc);
                    createNotification(adhocNotificationType, scheduledNotification.getText(), null, users);

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
