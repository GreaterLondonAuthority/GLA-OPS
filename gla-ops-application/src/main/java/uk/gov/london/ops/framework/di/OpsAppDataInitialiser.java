/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.framework.di;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import uk.gov.london.ops.audit.AuditService;
import uk.gov.london.ops.organisation.OrganisationBuilder;
import uk.gov.london.ops.organisation.OrganisationServiceImpl;
import uk.gov.london.ops.user.UserBuilder;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;

import static uk.gov.london.ops.organisation.Organisation.GLA_HNL_ORG_ID;
import static uk.gov.london.ops.organisation.Organisation.GLA_OPS_ORG_ID;
import static uk.gov.london.ops.user.UserBuilder.DATA_INITIALISER_USER;

/**
 * Data initialiser. Runs in all environment to populate the database.
 *
 * The framework distinguishes between test environments and live-like environments,
 * and populates test environments with standard test data.
 *
 * This DataInitialiser framework class does not create any data itself, it simply
 * manages a set of DataInitialiserModule implementations that do the actual setup.
 */
@Transactional
@Component
public class OpsAppDataInitialiser extends DataInitialiser {

    @Autowired private AuditService auditService;
    @Autowired private UserBuilder userBuilder;
    @Autowired protected JdbcTemplate jdbcTemplate;
    @Autowired protected OrganisationServiceImpl organisationService;
    @Autowired protected OrganisationBuilder organisationBuilder;
    @Autowired protected EntityManager entityManager;
    @Autowired protected DataCleanser dataCleanser;

    /**
     * Entry point for the DataInitialiser framework.
     *
     * Executes all the DataInitialiserModule components, step-by-step.
     *
     * This should be the only @PostConstruct method used to initialise data in the database; the Spring
     * framework will call this method after the dependency injection framework has created an instance.
     */
    @PostConstruct
    public void initiliseEnvironmentData() {
        if (environment.isTestEnvironment()) {
            dataCleanser.clearDatabase();
        }
        setupBaseOrganisations();

        setupAndSwitchToDataInitialiserUser();

        super.initiliseEnvironmentData();

        auditDataInitialisation();

        disableDataInitialiserUser();
    }


    private void setupBaseOrganisations() {
        if (!organisationService.organisationExistsById(GLA_OPS_ORG_ID)) {
            organisationBuilder.createManagingOrganisation(GLA_OPS_ORG_ID, "GLA OPS organisation", "", "", "", false, false);
        }

        // Make sure we have the GLA organisation for these users
        if (!organisationService.organisationExistsById(GLA_HNL_ORG_ID)) {
            organisationBuilder.createManagingOrganisation(GLA_HNL_ORG_ID, "GLA Housing and Land", "", "", "", true, false);
        }
    }

    /**
     * Creates a user for the data initialiser to act as, and makes it active.
     */
    private void setupAndSwitchToDataInitialiserUser() {
        userBuilder.createOpsAdminUser("Data", "Initialiser", DATA_INITIALISER_USER);
        userBuilder.withLoggedInUser(DATA_INITIALISER_USER);
    }

    /**
     * Disables the data initialiser user once it has done it's work.
     */
    private void disableDataInitialiserUser() {
        userBuilder.disableUser(DATA_INITIALISER_USER);
    }

    /**
     * Creates an audit record of the data initialisation.
     */
    private void auditDataInitialisation() {
        auditService.auditActivityForUser(DATA_INITIALISER_USER, "Data initialiser framework completed.");
    }

}
