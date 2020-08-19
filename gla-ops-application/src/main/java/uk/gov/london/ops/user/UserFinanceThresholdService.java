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
import uk.gov.london.ops.organisation.OrganisationService;
import uk.gov.london.ops.organisation.model.Organisation;
import uk.gov.london.ops.role.model.Role;
import uk.gov.london.ops.user.domain.User;
import uk.gov.london.ops.user.domain.UserOrgFinanceThreshold;
import uk.gov.london.ops.user.domain.UserOrgKey;
import uk.gov.london.ops.user.implementation.UserOrgFinanceThresholdRepository;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static uk.gov.london.ops.notification.NotificationType.PendingSpendAuthorityThresholdApproval;

@Service
public class UserFinanceThresholdService {

    final AuditService auditService;
    final NotificationService notificationService;
    final OrganisationService organisationService;
    final UserService userService;
    final UserOrgFinanceThresholdRepository userOrgFinanceThresholdRepository;

    public UserFinanceThresholdService(AuditService auditService, NotificationService notificationService,
                                       OrganisationService organisationService, UserService userService,
                                       UserOrgFinanceThresholdRepository userOrgFinanceThresholdRepository) {
        this.auditService = auditService;
        this.notificationService = notificationService;
        this.organisationService = organisationService;
        this.userService = userService;
        this.userOrgFinanceThresholdRepository = userOrgFinanceThresholdRepository;
    }

    public Set<UserOrgFinanceThreshold> getFinanceThresholds(String userIdOrName) {
        User user = userService.find(userIdOrName);
        Set<UserOrgFinanceThreshold> thresholds = userOrgFinanceThresholdRepository.findByIdUsername(user.getUsername());

        Set<Role> approvedRoles = user.getApprovedRoles();
        for (Role approvedRole : approvedRoles) {
            if (approvedRole.getOrganisation().isManagingOrganisation() && approvedRole.isThresholdRole()) {
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

    public UserOrgFinanceThreshold getFinanceThreshold(String username, Integer orgId) {
        return userOrgFinanceThresholdRepository.findById(new UserOrgKey(username, orgId)).orElse(null);
    }

    public UserOrgFinanceThreshold createPendingThreshold(String userIdOrName, Integer orgId, Long pendingThreshold) {
        User user = userService.find(userIdOrName);
        Organisation organisation = organisationService.findOne(orgId);

        if (!user.getOrganisations().contains(organisation)) {
            throw new ValidationException("Unable to assign threshold to organisation the user is not a member of");
        }

        if (!organisation.isManagingOrganisation()) {
            throw new ValidationException("Unable to assign threshold to organisation that is not a managing organisation");
        }

        UserOrgFinanceThreshold existing = getFinanceThreshold(user.getUsername(), orgId);

        if (existing == null) {
            existing = new UserOrgFinanceThreshold(user.getUsername(), orgId);
        }

        User currentUser = userService.currentUser();
        auditService.auditCurrentUserActivity(String.format("User %s created a pending threshold of %d for org: %d for user %s",
                currentUser.getUsername(), pendingThreshold, orgId, user.getUsername()),
                ActivityType.Requested, user.getUsername(), orgId, new BigDecimal(pendingThreshold));

        existing.setPendingThreshold(pendingThreshold);
        existing.setRequesterUsername(currentUser.getUsername());

        existing = save(existing);

        return existing;
    }

    public UserOrgFinanceThreshold approvePendingThreshold(String userIdOrName, Integer orgId) {
        User user = userService.find(userIdOrName);
        UserOrgFinanceThreshold existing = getFinanceThreshold(user.getUsername(), orgId);
        User currentUser = userService.currentUser();

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

        User requester = userService.get(existing.getId().getUsername());
        Organisation organisation = organisationService.findOne(orgId);

        Map<String, Object> model = new HashMap<String, Object>() {{
            put("organisation", organisation);
            put("approvedThreshold", String.format("%,d", existing.getApprovedThreshold()));
        }};

        notificationService.createNotificationForUser(PendingSpendAuthorityThresholdApproval, requester, model, user.getUsername());

        return save(existing);
    }

    private void validateThresholdApproval(UserOrgFinanceThreshold existing, User currentUser) {
        if (existing == null || existing.getPendingThreshold() == null) {
            throw new ValidationException("Unable to approve threshold as no existing threshold found.");
        }

        if (existing.getRequesterUsername() == null) {
            throw new ValidationException("Unable to approve threshold as no requester found.");
        }

        User requester = userService.get(existing.getRequesterUsername());
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
        User user = userService.find(userIdOrName);
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
