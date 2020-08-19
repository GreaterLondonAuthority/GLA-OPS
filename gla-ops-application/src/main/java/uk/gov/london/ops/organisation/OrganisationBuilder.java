/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.organisation;

import static uk.gov.london.common.organisation.OrganisationType.MANAGING_ORGANISATION;
import static uk.gov.london.common.organisation.OrganisationType.TECHNICAL_SUPPORT;
import static uk.gov.london.ops.organisation.model.Organisation.GLA_OPS_ID;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import uk.gov.london.common.organisation.OrganisationType;
import uk.gov.london.ops.organisation.model.LegalStatus;
import uk.gov.london.ops.organisation.model.OrganisationStatus;

/**
 * Factory class for building Organisation entities, primarily for testing.
 *
 * @author Steve Leach
 */
@Component
public class OrganisationBuilder {

    public static final Integer GLA_CULTURE_ORG_ID = 8001;
    public static final Integer GLA_REGEN_ORG_ID = 8002;
    public static final Integer GLA_SKILLS_ORG_ID = 8003;
    public static final Integer GLA_HOUSING_AND_LAND_ORG_ID = 10000;
    public static final Integer GLA_CORPORATE_ORG_ID = 8004;
    public static final Integer MOPAC_ORG_ID = 8005;
    public static final Integer ACM_CLADDING_ORG_ID = 8006;
    public static final Integer OPDC_ORG_ID = 8007;
    public static final Integer TEST_ORG_ID_1 = 9999;
    public static final Integer TEST_ORG_ID_2 = 9998;
    public static final Integer TEST_ORG_ID_3 = 9997;
    public static final Integer TEST_ORG_ID_4 = 9996;
    public static final Integer TEST_ORG_ID_5 = 9995;
    public static final Integer TEST_ORG_ID_6 = 9994;
    public static final Integer TEST_ORG_ID_7 = 9993;
    public static final Integer TEST_ORG_ID_8 = 9992;
    public static final Integer TEST_ORG_ID_9 = 9991;
    public static final Integer TEST_ORG_ID_10 = 9970;
    public static final Integer TEST_ORG_ID_11 = 9969;
    public static final Integer SKILLS_TEST_ORG_1 = 9990;
    public static final Integer SKILLS_TEST_ORG_2 = 9989;
    public static final Integer MOPAC_TEST_ORG_1 = 9988;
    public static final Integer SMALL_BUSINESS_ORG = 9987;
    public static final Integer MULTI_ROLE_ORGANISATION = 9960;
    public static final Integer MULTI_ROLE_PROVIDER_ORGANISATION = 9961;
    public static final Integer ACM_CLADDING_TEST_ORG = 9950;
    public static final Integer OPDC_TEST_ORG = 9951;
    public static final Integer LOAD_IMPACT_ORG = 10002;
    public static final Integer QA_ORG_ID_1 = 9001;

    final OrganisationService organisationService;
    final JdbcTemplate jdbcTemplate;

    public OrganisationBuilder(OrganisationService organisationService, JdbcTemplate jdbcTemplate) {
        this.organisationService = organisationService;
        this.jdbcTemplate = jdbcTemplate;
    }

    public void createOrganisation(Integer id, String name, String imsNumber, String ceoName, String email,
            LegalStatus legalStatus) {
        if (!organisationService.organisationExistsById(id)) {
            jdbcTemplate
                    .update("insert into ORGANISATION (id, name, ims_number, ceo_name, email, regulated, status, legal_status) "
                                    + "values (?, ?, ?, ?, ?, ?, ?, ?)", id, name, imsNumber, ceoName, email, false,
                            OrganisationStatus.Approved.name(), legalStatus.name());
        }
    }

    public void createOrganisation(Integer id, String name, String imsNumber, String ceoName, String email, String sapVendorId,
            OrganisationType organisationType, LegalStatus legalStatus) {
        if (!organisationService.organisationExistsById(id)) {
            jdbcTemplate.update("insert into ORGANISATION (id, name, ims_number, ceo_name, email, regulated, sap_vendor_id, "
                            + "entity_type, status, legal_status) "
                            + "values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", id, name, imsNumber, ceoName, email, false, sapVendorId,
                    organisationType.id(), OrganisationStatus.Approved.name(), legalStatus.getName());
        }
    }

    public void createOrganisation(Integer id, String name, String imsNumber, String website, String contactNumber,
            String address1, String address5, String postcode, String ceoName, String email,
            String primaryContactFirstName, String primaryContactLastName, String primaryContactEmail,
            String primaryContactNumber, LegalStatus legalStatus) {
        if (!organisationService.organisationExistsById(id)) {
            jdbcTemplate.update("insert into ORGANISATION (id, name, ims_number, website, contact_number, address1, address5,"
                            + " postcode, ceo_name, email, primary_contact_first_name, primary_contact_last_name, "
                            + "primary_contact_email, primary_contact_number, regulated, status, legal_status) "
                            + "values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                    id, name, imsNumber, website, contactNumber, address1, address5, postcode, ceoName, email,
                    primaryContactFirstName, primaryContactLastName, primaryContactEmail, primaryContactNumber, false,
                    OrganisationStatus.Approved.name(), legalStatus.name());
        }
    }

    public void createManagingOrganisation(Integer id, String name, String imsNumber, String ceoName, String email,
            boolean registrationAllowed, boolean skillsGatewayAccessAllowed) {
        if (!organisationService.organisationExistsById(id)) {
            jdbcTemplate.update("insert into ORGANISATION (id, name, ims_number, ceo_name, email, entity_type, regulated, "
                            + "managing_organisation_id, status, registration_allowed, skills_gateway_access_allowed) "
                            + "values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", id, name, imsNumber, ceoName, email,
                    MANAGING_ORGANISATION.id(), false, GLA_OPS_ID, OrganisationStatus.Approved.name(),
                    registrationAllowed, skillsGatewayAccessAllowed);
        }
    }

    public void createTechnicalSupportOrganisation(Integer id, String name, String ceoName, String email, String address,
                                                   String city, String postCode) {
        if (!organisationService.organisationExistsById(id)) {
            jdbcTemplate.update("insert into ORGANISATION (id, name, ceo_name, email, entity_type, regulated, status, " +
                            "address1, address5, postcode)  values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", id, name, ceoName,
                    email, TECHNICAL_SUPPORT.id(), false, OrganisationStatus.Approved.name(), address, city, postCode);
        }
    }

}
