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
import uk.gov.london.ops.framework.Environment;
import uk.gov.london.ops.framework.EntityType;
import uk.gov.london.ops.framework.OPSUtils;
import uk.gov.london.ops.notification.NotificationService;
import uk.gov.london.ops.organisation.OrganisationService;
import uk.gov.london.ops.organisation.model.Organisation;
import uk.gov.london.ops.user.domain.User;

import java.util.Date;

import static uk.gov.london.common.user.BaseRole.OPS_ADMIN;
import static uk.gov.london.ops.organisation.model.Organisation.GLA_HNL_ID;
import static uk.gov.london.ops.organisation.model.Organisation.GLA_OPS_ID;

/**
 * Factory class for building Users entities, primarily for testing.
 *
 * @author Steve Leach
 */
@Component
public class UserBuilder {

    Logger log = LoggerFactory.getLogger(getClass());

    public static final String DATA_INITIALISER_USER = "TestDataInitialiser@gla.org";
    public static final String SYSTEM_SCHEDULER_USER = "system.scheduler@gla.org";

    private final UserService userService;
    private final OrganisationService organisationService;
    private final NotificationService notificationService;
    private final Environment environment;

    public UserBuilder(UserService userService, OrganisationService organisationService, NotificationService notificationService, Environment environment) {
        this.userService = userService;
        this.organisationService = organisationService;
        this.notificationService = notificationService;
        this.environment = environment;
    }

    public void createUser(String email, String password, String firstName, String lastName) {
        User user = new User(email, password);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        userService.saveUser(user);
    }

    public void createTestUser(String firstName, String lastName, String email, Integer orgId, boolean approved, String role) {
        Organisation organisation = organisationService.findOne(orgId);
        createTestUser(firstName, lastName, email, organisation, approved, role);
    }

    public void createTestUser(String firstName, String lastName, String email, String imsNumber, boolean approved, String role) {
        Organisation organisation = organisationService.findByImsNumber(imsNumber);
        createTestUser(firstName, lastName, email, organisation, approved, role);
    }

    public void createTestUser(String firstName, String lastName, String email, Organisation organisation, boolean approved, String role) {
        User user = createBaseUser(firstName, lastName, email);
        if (approved) {
            if (user.getRoles().isEmpty()) {
                user.addApprovedRoleAndPrimaryOrganisationForUser(role,organisation, true);
            } else {
                user.addApprovedRole(role, organisation);
            }
            notificationService.subscribe(email, EntityType.organisation, organisation.getId());
        }
        else {
            user.addUnapprovedRole(role, organisation);
        }
        userService.saveUser(user);
    }

    public void createOpsAdminUser(String firstName, String lastName, String email) {
        User user = createBaseUser(firstName, lastName, email);
        user.addApprovedRoleAndPrimaryOrganisationForUser(OPS_ADMIN, organisationService.findOne(GLA_OPS_ID), false);
        user.addApprovedRoleAndPrimaryOrganisationForUser(OPS_ADMIN, organisationService.findOne(GLA_HNL_ID), true);
        userService.saveUser(user);

        notificationService.subscribe(email, EntityType.organisation, GLA_OPS_ID);
        notificationService.subscribe(email, EntityType.organisation, GLA_HNL_ID);
    }

    public void createDisabledUser(String firstName, String lastName, String email) {
        User user = createBaseUser(firstName, lastName, email);
        disableUser(user);
        userService.saveUser(user);
    }

    private User createBaseUser(String firstName, String lastName, String email) {
        User user = userService.get(email.toLowerCase());
        if (user == null) {
            user = new User(email.toLowerCase(), environment.defPwHash());
        }
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setRegisteredOn(Date.from(environment.now().toInstant()));
        return user;
    }

    public void disableUser(String name) {
        try {
            User user = userService.get(name.toLowerCase());
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

    private void disableUser(User user) {
        user.setEnabled(false);
        user.setPassword("-"); // This isn't a valid hash, so can never work
        user.getRoles().clear();
    }

    public void withLoggedInUser(String username) {
        User user = userService.get(username.toLowerCase());
        if (user == null) {
            throw new IllegalArgumentException("User not found: " + username);
        }
        userService.withLoggedInUser(user);
    }

}
