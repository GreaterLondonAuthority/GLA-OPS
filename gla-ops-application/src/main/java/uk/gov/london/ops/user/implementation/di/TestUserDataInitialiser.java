/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.user.implementation.di;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.london.ops.framework.di.DataInitialiserModule;
import uk.gov.london.ops.framework.environment.Environment;
import uk.gov.london.ops.organisation.Organisation;
import uk.gov.london.ops.organisation.OrganisationService;
import uk.gov.london.ops.organisation.OrganisationServiceImpl;
import uk.gov.london.ops.role.RoleService;
import uk.gov.london.ops.user.UserBuilder;
import uk.gov.london.ops.user.UserServiceImpl;
import uk.gov.london.ops.user.domain.UserEntity;

import static uk.gov.london.common.user.BaseRole.*;
import static uk.gov.london.ops.organisation.Organisation.GLA_CULTURE_ORG_ID;
import static uk.gov.london.ops.organisation.Organisation.GLA_HNL_ORG_ID;
import static uk.gov.london.ops.organisation.OrganisationBuilder.CROMWOOD_ORG_ID;
import static uk.gov.london.ops.organisation.OrganisationBuilder.GLA_SKILLS_ORG_ID;

@Component
public class TestUserDataInitialiser implements DataInitialiserModule {

    @Autowired
    OrganisationServiceImpl organisationService;

    final UserBuilder userBuilder;
    final UserServiceImpl userService;
    final RoleService roleService;
    final Environment environment;

    public TestUserDataInitialiser(UserBuilder userBuilder, UserServiceImpl userService, RoleService roleService,
                                   Environment environment) {
        this.userBuilder = userBuilder;
        this.userService = userService;
        this.roleService = roleService;
        this.environment = environment;
    }

    @Override
    public String getName() {
        return "Test user data initialiser";
    }

    @Override
    public void addUsers() {
        userBuilder.createDisabledUser("Disabled", "User", "disabled_user@gla.ops");
        userBuilder.createTestUser("HNL", "Registration Approver",
                "hnl.registration.approver@gla.com", GLA_HNL_ORG_ID, true, GLA_REGISTRATION_APPROVER);
        userBuilder.createTestUser("programme", "admin",
                "programme.admin@gla.com", GLA_HNL_ORG_ID, true, GLA_PROGRAMME_ADMIN);
        userBuilder.createTestUser("Disabled", "User", "user_to_be_disabled@gla.ops", "FAKE1", true, PROJECT_EDITOR);
        userBuilder.createTestUser("ExpiredPW", "User", "expired_pw@gla.ops", "FAKE1", true, PROJECT_EDITOR);
        userBuilder.createTestUser("LongExpiredPW", "User", "long_expired_pw@gla.ops", "FAKE1", true, PROJECT_EDITOR);
        userBuilder.createTestUser("Cromwood", "editor", "cromwood.editor@gla.com", CROMWOOD_ORG_ID, true, PROJECT_EDITOR);
        userBuilder.createTestUser("Without", "Role", "without.role@gla.com", null, false, "",
            false);
        userBuilder.createTestUser("Multi", "MO SPM", "spm.multi.managing.orgs@gla.com", GLA_CULTURE_ORG_ID, true, GLA_SPM);
        UserEntity multiMOUser = userService.get("spm.multi.managing.orgs@gla.com");
        multiMOUser.addApprovedRole(GLA_SPM, organisationService.findOne(GLA_SKILLS_ORG_ID));
        userService.saveUser(multiMOUser);
        userBuilder.createTestUser("Multi", "MO PM", "pm.multi.managing.orgs@gla.com", GLA_CULTURE_ORG_ID, true, GLA_PM);
        UserEntity multiMOUserPM = userService.get("pm.multi.managing.orgs@gla.com");
        multiMOUserPM.addApprovedRole(GLA_PM, organisationService.findOne(GLA_SKILLS_ORG_ID));
        userService.saveUser(multiMOUserPM);
        expireUserPassword("expired_pw@gla.ops", 1);
        expireUserPassword("long_expired_pw@gla.ops", 370);

        setupTestUsersFinanceThresholds();
    }

    private void expireUserPassword(String username, int expiryDays) {
        UserEntity user;
        user = userService.get(username);
        user.setPasswordExpiry(environment.now().minusDays(expiryDays));
        userService.saveUser(user);
    }

    private void setupTestUsersFinanceThresholds() {
        userBuilder.createSpendThreshold("less_senior_pm@gla.com", GLA_HNL_ORG_ID, 1000L, null, "");
        userBuilder.createSpendThreshold("less_senior_pm@gla.com", GLA_HNL_ORG_ID, 1000L, null, "");
        userBuilder.createSpendThreshold("senior.pm@gla.com", GLA_HNL_ORG_ID, 1000000L, null, "test.admin@gla.com");
        userBuilder.createSpendThreshold("user.alpha@gla.org", GLA_HNL_ORG_ID, 5000000L, 4000000L, "user.alpha@gla.org");
        userBuilder.createSpendThreshold("junk.user@gla.org", GLA_HNL_ORG_ID, 5000000L, 4000000L, "junk.user@gla.org");
        userBuilder.createSpendThreshold("test.admin@gla.com", GLA_HNL_ORG_ID, 4000000L, null, null);
        userBuilder.createSpendThreshold("hnl.admin@gla.com", GLA_HNL_ORG_ID, 4000000L, 5000000L, "user.alpha@gla.org");
        userBuilder.createSpendThreshold("hnl.admin@gla.com", GLA_HNL_ORG_ID, 4000000L, 5000000L, "user.alpha@gla.org");
        userBuilder.createSpendThreshold("skills.admin@gla.com", GLA_SKILLS_ORG_ID, 30000000L, null, null);
        userBuilder.createSpendThreshold("test.admin@gla.com", GLA_SKILLS_ORG_ID, 30000000L, null, null);
        userBuilder.createSpendThreshold("test.admin@gla.com", Organisation.GLA_REGEN_ORG_ID, 4000000L, null, null);
    }

    @Override
    public int executionOrder() {
        return 10;
    }
}
