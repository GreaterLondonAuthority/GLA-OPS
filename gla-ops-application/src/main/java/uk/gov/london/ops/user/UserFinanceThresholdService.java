/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.user;

import org.springframework.stereotype.Service;
import uk.gov.london.ops.audit.ActivityType;
import uk.gov.london.ops.audit.AuditService;
import uk.gov.london.ops.framework.exception.ValidationException;
import uk.gov.london.ops.notification.NotificationService;
import uk.gov.london.ops.organisation.Organisation;
import uk.gov.london.ops.organisation.OrganisationService;
import uk.gov.london.ops.permission.PermissionServiceImpl;
import uk.gov.london.ops.project.accesscontrol.AccessControlRelationshipType;
import uk.gov.london.ops.project.accesscontrol.ProjectAccessControlSummary;
import uk.gov.london.ops.role.model.Role;
import uk.gov.london.ops.user.domain.UserEntity;
import uk.gov.london.ops.user.domain.UserOrgFinanceThreshold;
import uk.gov.london.ops.user.domain.UserOrgKey;
import uk.gov.london.ops.user.implementation.UserOrgFinanceThresholdRepository;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static uk.gov.london.ops.notification.NotificationType.PendingSpendAuthorityThresholdApproval;

@Service
public class UserFinanceThresholdService {

    final AuditService auditService;
    final NotificationService notificationService;
    final OrganisationService organisationService;
    final PermissionServiceImpl permissionService;
    final UserServiceImpl userService;
    final UserOrgFinanceThresholdRepository userOrgFinanceThresholdRepository;

    public UserFinanceThresholdService(AuditService auditService, NotificationService notificationService,
                                       OrganisationService organisationService, PermissionServiceImpl permissionService,
                                       UserServiceImpl userService,
                                       UserOrgFinanceThresholdRepository userOrgFinanceThresholdRepository) {
        this.auditService = auditService;
        this.notificationService = notificationService;
        this.organisationService = organisationService;
        this.permissionService = permissionService;
        this.userService = userService;
        this.userOrgFinanceThresholdRepository = userOrgFinanceThresholdRepository;
    }

    public Set<UserOrgFinanceThreshold> getFinanceThresholds(String userIdOrName) {
        UserEntity user = userService.find(userIdOrName);
        Set<UserOrgFinanceThreshold> thresholds = userOrgFinanceThresholdRepository.findByIdUsername(user.getUsername());

        Set<Role> approvedRoles = user.getApprovedRoles();
        for (Role approvedRole : approvedRoles) {
            if (canThresholdBeSetOn(approvedRole.getOrganisation()) && approvedRole.isThresholdRole()) {
                boolean found = false;
                for (UserOrgFinanceThreshold threshold : thresholds) {
                    if (threshold.getId().getOrganisationId().equals(approvedRole.getOrganisation().getId())) {
                        found = true;
                        break;
                    }
                }

                if (!found) { // if none existing create placeholder
                    thresholds.add(new UserOrgFinanceThreshold(user.getUsername(), approvedRole.getOrganisation().getId()));
                }
            }
        }
        return thresholds;
    }

    public Set<UserOrgFinanceThreshold> getFinanceThresholdsByOrgId(Integer orgId) {
        Set<UserOrgFinanceThreshold> thresholds = userOrgFinanceThresholdRepository.findByIdOrganisationId(orgId);
        return thresholds;
    }

    public UserOrgFinanceThreshold getFinanceThreshold(String username, Integer orgId) {
        return userOrgFinanceThresholdRepository.findById(new UserOrgKey(username, orgId)).orElse(null);
    }

    public UserOrgFinanceThreshold getFinanceThresholdForProject(String username, Integer projectId) {
        Map<Integer, UserOrgFinanceThreshold> thresholds = userOrgFinanceThresholdRepository.findByIdUsername(username).stream()
                .collect(Collectors.toMap(u -> u.getId().getOrganisationId(), Function.identity()));
        return getFinanceThresholdForProject(thresholds, projectId);
    }

    public UserOrgFinanceThreshold getFinanceThresholdForProject(Map<Integer, UserOrgFinanceThreshold> userThresholds,
                                                                 Integer projectId) {
        Collection<ProjectAccessControlSummary> accessControlList = permissionService.getProjectAccessControlList(projectId);
        UserOrgFinanceThreshold threshold = getMaxThresholdForProject(accessControlList, isNonManagingOrgAcl(), userThresholds);
        if (threshold == null) {
            threshold = getMaxThresholdForProject(accessControlList, isManagingOrgAcl(), userThresholds);
        }
        return threshold;
    }

    public static Predicate<ProjectAccessControlSummary> isManagingOrgAcl() {
        return e -> AccessControlRelationshipType.MANAGING.equals(e.getRelationshipType());
    }

    public static Predicate<ProjectAccessControlSummary> isNonManagingOrgAcl() {
        return e -> !AccessControlRelationshipType.MANAGING.equals(e.getRelationshipType());
    }

    private UserOrgFinanceThreshold getMaxThresholdForProject(Collection<ProjectAccessControlSummary> accessControlList,
                                                              Predicate<ProjectAccessControlSummary> aclFilter,
                                                              Map<Integer, UserOrgFinanceThreshold> userThresholds) {
        UserOrgFinanceThreshold threshold = null;
        Collection<ProjectAccessControlSummary> filteredAcl = accessControlList.stream()
                .filter(aclFilter)
                .collect(Collectors.toList());
        for (ProjectAccessControlSummary accessControlEntry: filteredAcl) {
            if (userThresholds.containsKey(accessControlEntry.getOrganisationId())) {
                UserOrgFinanceThreshold newThresholdCandidate = userThresholds.get(accessControlEntry.getOrganisationId());
                if (threshold == null || threshold.getApprovedThreshold() < newThresholdCandidate.getApprovedThreshold()) {
                    threshold = userThresholds.get(accessControlEntry.getOrganisationId());
                }
            }
        }
        return threshold;
    }

    public UserOrgFinanceThreshold createPendingThreshold(String userIdOrName, Integer orgId, Long pendingThreshold) {
        UserEntity user = userService.find(userIdOrName);
        Organisation organisation = organisationService.findOne(orgId);

        if (!user.getOrganisations().contains(organisation)) {
            throw new ValidationException("Unable to assign threshold to organisation the user is not a member of");
        }

        if (!canThresholdBeSetOn(organisation)) {
            throw new ValidationException("Threshold can only be assigned to roles in managing organisations or teams");
        }

        UserOrgFinanceThreshold existing = getFinanceThreshold(user.getUsername(), orgId);

        if (existing == null) {
            existing = new UserOrgFinanceThreshold(user.getUsername(), orgId);
        }

        UserEntity currentUser = userService.currentUser();
        auditService.auditCurrentUserActivity(String.format("User %s created a pending threshold of %d for org: %d for user %s",
                currentUser.getUsername(), pendingThreshold, orgId, user.getUsername()),
                ActivityType.Requested, user.getUsername(), orgId, new BigDecimal(pendingThreshold));

        existing.setPendingThreshold(pendingThreshold);
        existing.setRequesterUsername(currentUser.getUsername());

        existing = save(existing);

        return existing;
    }

    boolean canThresholdBeSetOn(Organisation organisation) {
        return organisation.isManaging() || organisation.isTeamOrganisation();
    }

    public UserOrgFinanceThreshold approvePendingThreshold(String userIdOrName, Integer orgId) {
        UserEntity user = userService.find(userIdOrName);
        UserOrgFinanceThreshold existing = getFinanceThreshold(user.getUsername(), orgId);
        UserEntity currentUser = userService.currentUser();

        validateThresholdApproval(existing, currentUser);

        auditService.auditCurrentUserActivity(String.format(
                "Approved a pending threshold for %s of %d for org: %d, that was originally requested by %s",
                user.getUsername(), existing.getPendingThreshold(), orgId, existing.getRequesterUsername()),
                ActivityType.Approved,
                user.getUsername(), orgId, new BigDecimal(existing.getPendingThreshold()));

        existing.setApprovedThreshold(existing.getPendingThreshold());
        existing.setApproverUsername(currentUser.getUsername());
        existing.setRequesterUsername(null);
        existing.setPendingThreshold(null);

        UserEntity requester = userService.get(existing.getId().getUsername());
        Organisation organisation = organisationService.findOne(orgId);

        Map<String, Object> model = new HashMap<String, Object>() {{
            put("organisation", organisation);
            put("approvedThreshold", String.format("%,d", existing.getApprovedThreshold()));
        }};

        notificationService.createNotificationForUser(PendingSpendAuthorityThresholdApproval, requester, model, user.getUsername());

        return save(existing);
    }

    private void validateThresholdApproval(UserOrgFinanceThreshold existing, UserEntity currentUser) {
        if (existing == null || existing.getPendingThreshold() == null) {
            throw new ValidationException("Unable to approve threshold as no existing threshold found.");
        }

        if (existing.getRequesterUsername() == null) {
            throw new ValidationException("Unable to approve threshold as no requester found.");
        }

        UserEntity requester = userService.get(existing.getRequesterUsername());
        if (requester == null) {
            throw new ValidationException("Unable to approve threshold requester not found.");
        }

        if (currentUser == requester) {
            throw new ValidationException("Requester and approver must be different users.");
        }

        if (currentUser.getUsername().equals(existing.getId().getUsername())) {
            throw new ValidationException("User cannot approve changes to their own pending record.");
        }
    }

    public UserOrgFinanceThreshold declineThreshold(String userIdOrName, Integer orgId) {
        UserEntity user = userService.find(userIdOrName);
        UserOrgFinanceThreshold existing = getFinanceThreshold(user.getUsername(), orgId);
        if (existing == null || existing.getPendingThreshold() == null) {
            throw new ValidationException("Unable to decline pending threshold as no existing threshold found.");
        }

        auditService.auditCurrentUserActivity(String.format(
                "Declined a pending threshold for %s of %d for org: %d, that was originally requested by %s",
                user.getUsername(), existing.getPendingThreshold(), orgId, existing.getRequesterUsername()),
                ActivityType.Declined, user.getUsername(), orgId, null);

        existing.setPendingThreshold(null);
        existing.setRequesterUsername(null);
        return save(existing);
    }

    public void clearFinanceThreshold(String username, Integer orgId) {
        UserOrgFinanceThreshold existing = getFinanceThreshold(username, orgId);
        if (existing != null) {
            existing.clear();
            save(existing);
        }
    }

    public UserOrgFinanceThreshold save(UserOrgFinanceThreshold threshold) {
        return userOrgFinanceThresholdRepository.save(threshold);
    }

    public void deleteAll() {
        userOrgFinanceThresholdRepository.deleteAll();
    }
}
