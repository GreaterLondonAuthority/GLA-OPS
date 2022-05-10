/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.user.implementation.di;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.london.ops.framework.di.DataInitialiserModule;
import uk.gov.london.ops.user.UserBuilder;
import uk.gov.london.ops.user.UserServiceImpl;
import uk.gov.london.ops.user.domain.UserEntity;

import static uk.gov.london.ops.user.UserBuilder.SYSTEM_SCHEDULER_USER;

/**
 * Initialises the system users
 */
@Component
public class SystemUserDataInitialiser implements DataInitialiserModule {

    @Autowired
    UserServiceImpl userService;

    @Autowired
    private UserBuilder userBuilder;

    @Override
    public String getName() {
        return "System User Data Initialiser";
    }

    public int executionOrder() {
        return 1050;
    }

    @Override
    public boolean runInAllEnvironments() {
        return true;
    }

    @Override
    public void beforeInitialisation() {}

    @Override
    public void addReferenceData() {
    }

    @Override
    public void addUsers() {
        UserEntity user = userService.get(SYSTEM_SCHEDULER_USER);

        if (user == null) {
            userBuilder.createUser(SYSTEM_SCHEDULER_USER, RandomStringUtils.random(10), "System", "Scheduler");
        }
    }

    @Override
    public void addOrganisations() {}

    @Override
    public void addTemplates() {}

    @Override
    public void addProgrammes() {}

    @Override
    public void addProjects() {}

    @Override
    public void addSupplementalData() {
    }

    @Override
    public void afterInitialisation() {
    }
}
