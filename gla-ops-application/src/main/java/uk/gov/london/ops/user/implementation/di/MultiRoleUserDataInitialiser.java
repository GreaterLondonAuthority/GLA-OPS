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
import uk.gov.london.ops.organisation.OrganisationBuilder;
import uk.gov.london.ops.organisation.OrganisationServiceImpl;
import uk.gov.london.ops.organisation.OrganisationType;
import uk.gov.london.ops.organisation.model.OrganisationEntity;
import uk.gov.london.ops.programme.ProgrammeBuilder;
import uk.gov.london.ops.programme.domain.Programme;
import uk.gov.london.ops.project.ProjectBuilder;
import uk.gov.london.ops.project.template.TemplateServiceImpl;
import uk.gov.london.ops.project.template.domain.Template;
import uk.gov.london.ops.user.UserBuilder;
import uk.gov.london.ops.user.UserFinanceThresholdService;
import uk.gov.london.ops.user.UserServiceImpl;
import uk.gov.london.ops.user.domain.UserEntity;

import javax.transaction.Transactional;
import java.math.BigDecimal;

import static uk.gov.london.common.user.BaseRole.*;
import static uk.gov.london.ops.organisation.Organisation.GLA_OPS_ORG_ID;
import static uk.gov.london.ops.organisation.OrganisationBuilder.MULTI_ROLE_ORGANISATION;
import static uk.gov.london.ops.organisation.OrganisationBuilder.MULTI_ROLE_PROVIDER_ORGANISATION;
import static uk.gov.london.ops.project.ProjectBuilder.STATUS_ACTIVE;

/**
 * Initialises the system users
 */
@Component
@Transactional
public class MultiRoleUserDataInitialiser implements DataInitialiserModule {

    public static final String MULTI_ROLE_ORG_USER = "multi_role_org@gla.org";
    public static final String MULTI_ROLE_RP_ORG_USER = "multi_role_rp_org@gla.org";

    @Autowired
    UserServiceImpl userService;

    @Autowired
    UserFinanceThresholdService userFinanceThresholdService;

    @Autowired
    TemplateServiceImpl templateService;

    @Autowired
    private OrganisationBuilder organisationBuilder;

    @Autowired
    private ProgrammeBuilder programmeBuilder;

    @Autowired
    private UserBuilder userBuilder;

    @Autowired
    private ProjectBuilder projectBuilder;

    @Autowired
    private OrganisationServiceImpl organisationService;

    private OrganisationEntity managingOrg;

    private OrganisationEntity providerOrg;

    private Programme programme;

    private Template template;


    @Override
    public String getName() {
        return "Multi Role Data Initialiser";
    }

    public int executionOrder() {
        return 1060;
    }

    @Override
    public void beforeInitialisation() {}

    @Override
    public void addReferenceData() {
    }

    @Override
    public void addOrganisations() {
        organisationBuilder.createManagingOrganisation(
                MULTI_ROLE_ORGANISATION,
                "Org with multi role user",
                "MULTI_ORG",
                "User Alpha",
                "user.alpha@gla.org",
                true,
                false);

        managingOrg = organisationService.findOne(MULTI_ROLE_ORGANISATION);

        organisationBuilder.createOrganisation(
                 MULTI_ROLE_PROVIDER_ORGANISATION,
                "Provider Org",
                "",
                "User Alpha",
                "user.alpha@gla.org",
                null,
                OrganisationType.PROVIDER,
                "finance@gla.com");

        providerOrg = organisationService.findOne(MULTI_ROLE_PROVIDER_ORGANISATION);
        providerOrg.setManagingOrganisation(managingOrg);
        providerOrg.setRegulated(true);
        organisationService.save(providerOrg);
    }

    @Override
    public void addUsers() {
        OrganisationEntity glaPrimeOrg = organisationService.findOne(GLA_OPS_ORG_ID);

        UserEntity user = userService.get(MULTI_ROLE_ORG_USER);
        if (user == null) {
            userBuilder.createTestUser("Multi", "Role", MULTI_ROLE_ORG_USER, MULTI_ROLE_ORGANISATION, true, GLA_FINANCE);
            user = userService.find(MULTI_ROLE_ORG_USER);
            user.addApprovedRole(GLA_ORG_ADMIN, managingOrg);
            userService.saveUser(user);
        }

        UserEntity rp = userService.get(MULTI_ROLE_RP_ORG_USER);
        if (rp == null) {
            userBuilder.createTestUser("RP", "MultiRole", MULTI_ROLE_RP_ORG_USER,
                    MULTI_ROLE_PROVIDER_ORGANISATION, true, PROJECT_EDITOR);
            rp = userService.find(MULTI_ROLE_RP_ORG_USER);
            rp.addApprovedRole(ORG_ADMIN, providerOrg);
            userService.saveUser(rp);
        }

        UserEntity opsAdmin = userService.get("test.admin@gla.com");
        if (opsAdmin != null) {
            opsAdmin.addApprovedRole(GLA_FINANCE, glaPrimeOrg);
            userService.saveUser(opsAdmin);
        }
    }

    @Override
    public void addTemplates() {
        template = templateService.findByName("E2E Minimal Template");

    }

    @Override
    public void addProgrammes() {
        programme = programmeBuilder.createTestProgramme("Multi Role Programme", false, true, MULTI_ROLE_ORGANISATION, template);

        programmeBuilder.addPublicProfileDetails(programme, "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do "
                        + "eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam.",
                new BigDecimal("750075007500.00"), null);
    }

    @Override
    public void addProjects() {
        projectBuilder.createPopulatedTestProject("Multi Role Test Project", programme, template,
                MULTI_ROLE_PROVIDER_ORGANISATION, STATUS_ACTIVE);
    }

    @Override
    public void addSupplementalData() {

    }

    @Override
    public void afterInitialisation() {
    }
}
