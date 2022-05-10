/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.user;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import uk.gov.london.ops.framework.EntityType;
import uk.gov.london.ops.framework.environment.Environment;
import uk.gov.london.ops.notification.NotificationService;
import uk.gov.london.ops.organisation.OrganisationServiceImpl;
import uk.gov.london.ops.organisation.model.OrganisationEntity;
import uk.gov.london.ops.user.domain.UserEntity;
import uk.gov.london.ops.user.domain.UserOrgFinanceThreshold;

import java.util.Date;

import static uk.gov.london.common.user.BaseRole.OPS_ADMIN;
import static uk.gov.london.ops.organisation.Organisation.GLA_HNL_ORG_ID;
import static uk.gov.london.ops.organisation.Organisation.GLA_OPS_ORG_ID;

/**
 * Factory class for building Users entities, primarily for testing.
 *
 * @author Steve Leach
 */
@Component
public class UserBuilder {

    Logger log = LoggerFactory.getLogger(getClass());

    public static final String DATA_INITIALISER_USER = "TestDataInitialiser";
    public static final String SYSTEM_SCHEDULER_USER = "system.scheduler";

    private final UserServiceImpl userService;
    private final OrganisationServiceImpl organisationService;
    private final NotificationService notificationService;
    private final Environment environment;
    private final UserFinanceThresholdService userFinanceThresholdService;

    public UserBuilder(UserServiceImpl userService, OrganisationServiceImpl organisationService, NotificationService notificationService,
                       Environment environment, UserFinanceThresholdService userFinanceThresholdService) {
        this.userService = userService;
        this.organisationService = organisationService;
        this.notificationService = notificationService;
        this.environment = environment;
        this.userFinanceThresholdService = userFinanceThresholdService;
    }

    public void createUser(String email, String password, String firstName, String lastName) {
        UserEntity user = new UserEntity(email, password);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        userService.saveUser(user);
    }

    public void createTestUser(String firstName, String lastName, String email, Integer orgId, boolean approved, String role) {
        OrganisationEntity organisation = organisationService.findOne(orgId);
        createTestUser(firstName, lastName, email, organisation, approved, role, true);
    }

    public void createTestUser(String firstName, String lastName, String email, String providerNumber, boolean approved, String role) {
        OrganisationEntity organisation = organisationService.findByProviderNumber(providerNumber);
        createTestUser(firstName, lastName, email, organisation, approved, role, true);
    }

    public void createTestUser(String firstName, String lastName, String email, OrganisationEntity organisation, boolean approved,
                               String role, Boolean shouldHaveRole) {
        UserEntity user = createBaseUser(firstName, lastName, email);
        if (approved) {
            if (user.getRoles().isEmpty()) {
                user.addApprovedRoleAndPrimaryOrganisationForUser(role, organisation, true);
            } else {
                user.addApprovedRole(role, organisation);
            }
            notificationService.subscribe(email, EntityType.organisation, organisation.getId());
        } else if (shouldHaveRole){
            user.addUnapprovedRole(role, organisation);
        }
        userService.saveUser(user);
    }

    public void createOpsAdminUser(String firstName, String lastName, String email) {
        UserEntity user = createBaseUser(firstName, lastName, email);
        user.addApprovedRoleAndPrimaryOrganisationForUser(OPS_ADMIN, organisationService.findOne(GLA_OPS_ORG_ID), false);
        user.addApprovedRoleAndPrimaryOrganisationForUser(OPS_ADMIN, organisationService.findOne(GLA_HNL_ORG_ID), true);
        userService.saveUser(user);

        notificationService.subscribe(email, EntityType.organisation, GLA_OPS_ORG_ID);
        notificationService.subscribe(email, EntityType.organisation, GLA_HNL_ORG_ID);
    }

    public void createDisabledUser(String firstName, String lastName, String email) {
        UserEntity user = createBaseUser(firstName, lastName, email);
        disableUser(user);
        userService.saveUser(user);
    }

    private UserEntity createBaseUser(String firstName, String lastName, String email) {
        UserEntity user = userService.get(email.toLowerCase());
        if (user == null) {
            user = new UserEntity(email.toLowerCase(), environment.defPwHash());
        }
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setRegisteredOn(Date.from(environment.now().toInstant()));
        user.setPasswordExpiry(environment.now().plusDays(90));
        return user;
    }

    public void disableUser(String name) {
        try {
            UserEntity user = userService.get(name.toLowerCase());
            if (user != null) {
                disableUser(user);
                userService.saveUser(user);
                log.info("Disabled user " + name);
            } else {
                log.warn("Cannot find user for disabling: " + name);
            }
        } catch (Exception e) {
            log.error("Error disabling user " + name, e);
        }
    }

    private void disableUser(UserEntity user) {
        user.setEnabled(false);
        user.setPassword("-"); // This isn't a valid hash, so can never work
        user.getRoles().clear();
    }

    public void withLoggedInUser(String username) {
        UserEntity user = userService.get(username.toLowerCase());
        if (user == null) {
            throw new IllegalArgumentException("User not found: " + username);
        }
        UserServiceImpl.withLoggedInUser(user);
    }

    public void createSpendThreshold(String username, Integer orgId, Long approvedThreshold, Long pendingThreshold,
                                     String requester) {
        UserOrgFinanceThreshold threshold = new UserOrgFinanceThreshold(username, orgId, approvedThreshold, pendingThreshold,
                        requester, "finance.gla");
        userFinanceThresholdService.save(threshold);
    }

}
