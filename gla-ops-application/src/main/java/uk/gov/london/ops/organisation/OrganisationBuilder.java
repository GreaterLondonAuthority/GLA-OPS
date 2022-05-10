/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.organisation;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import uk.gov.london.ops.organisation.implementation.repository.SapIdRepository;
import uk.gov.london.ops.organisation.model.SapIdEntity;

import java.time.OffsetDateTime;

import static uk.gov.london.ops.organisation.Organisation.GLA_OPS_ORG_ID;
import static uk.gov.london.ops.organisation.OrganisationType.MANAGING_ORGANISATION;
import static uk.gov.london.ops.organisation.OrganisationType.TECHNICAL_SUPPORT;

/**
 * Factory class for building Organisation entities, primarily for testing.
 *
 * @author Steve Leach
 */
@Component
public class OrganisationBuilder {

    public static final Integer GLA_SKILLS_ORG_ID = 8003;
    public static final Integer GLA_CORPORATE_ORG_ID = 8004;
    public static final Integer MOPAC_ORG_ID = 8005;
    public static final Integer ACM_CLADDING_ORG_ID = 8006;
    public static final Integer OPDC_ORG_ID = 8007;
    public static final Integer SKILLS_TEST_ORG_1 = 9990;
    public static final Integer SKILLS_TEST_ORG_2 = 9989;
    public static final Integer MOPAC_TEST_ORG_1 = 9988;
    public static final Integer SMALL_BUSINESS_ORG = 9987;
    public static final Integer MULTI_ROLE_ORGANISATION = 9960;
    public static final Integer MULTI_ROLE_PROVIDER_ORGANISATION = 9961;
    public static final Integer ACM_CLADDING_TEST_ORG = 9950;
    public static final Integer OPDC_TEST_ORG = 9951;
    public static final Integer QA_ORG_ID_1 = 9001;
    public static final Integer CROMWOOD_ORG_ID = 9952;
    public static final Integer REJECTED_TEST_ORG_ID = 9953;

    final OrganisationServiceImpl organisationService;
    final SapIdRepository sapIdRepository;
    final JdbcTemplate jdbcTemplate;

    public OrganisationBuilder(OrganisationServiceImpl organisationService, SapIdRepository sapIdRepository,
                               JdbcTemplate jdbcTemplate) {
        this.organisationService = organisationService;
        this.sapIdRepository = sapIdRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    public void createOrganisation(Integer id, String name, String providerNumber, String ceoName, String email) {
        if (!organisationService.organisationExistsById(id)) {
            jdbcTemplate
                    .update("insert into ORGANISATION (id, name, provider_number, ceo_name, email, regulated, status) "
                                    + "values (?, ?, ?, ?, ?, ?, ?)", id, name, providerNumber, ceoName, email, false,
                            OrganisationStatus.Approved.name());
        }
    }

    public void createOrganisation(Integer id, String name, String providerNumber, String ceoName, String email, String sapVendorId,
            OrganisationType organisationType, String financeEmail) {
        if (!organisationService.organisationExistsById(id)) {
            jdbcTemplate.update("insert into ORGANISATION (id, name, provider_number, ceo_name, email, regulated, "
                            + "entity_type, status, finance_contact_email) "
                            + "values (?, ?, ?, ?, ?, ?, ?, ?, ?)", id, name, providerNumber, ceoName, email, false,
                    organisationType.getId(), OrganisationStatus.Approved.name(), financeEmail);
        }
        if (sapVendorId != null) {
            sapIdRepository.save(new SapIdEntity(sapVendorId, id, "test SAP id", OffsetDateTime.now(), true));
        }
    }

    public void createOrganisation(Integer id, String name, String providerNumber, String website, String contactNumber,
            String address1, String address5, String postcode, String ceoName, String email,
            String primaryContactFirstName, String primaryContactLastName, String primaryContactEmail,
            String primaryContactNumber, String financeEmail) {
        if (!organisationService.organisationExistsById(id)) {
            jdbcTemplate.update("insert into ORGANISATION (id, name, provider_number, website, contact_number, address1, address5,"
                            + " postcode, ceo_name, email, primary_contact_first_name, primary_contact_last_name, "
                            + "primary_contact_email, primary_contact_number, regulated, status, finance_contact_email) "
                            + "values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                    id, name, providerNumber, website, contactNumber, address1, address5, postcode, ceoName, email,
                    primaryContactFirstName, primaryContactLastName, primaryContactEmail, primaryContactNumber, false,
                    OrganisationStatus.Approved.name(), financeEmail);
        }
    }

    public void createManagingOrganisation(Integer id, String name, String providerNumber, String ceoName, String email,
            boolean registrationAllowed, boolean skillsGatewayAccessAllowed) {
        if (!organisationService.organisationExistsById(id)) {
            jdbcTemplate.update("insert into ORGANISATION (id, name, provider_number, ceo_name, email, entity_type, regulated, "
                            + "managing_organisation_id, status, registration_allowed, skills_gateway_access_allowed) "
                            + "values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", id, name, providerNumber, ceoName, email,
                    MANAGING_ORGANISATION.getId(), false, GLA_OPS_ORG_ID, OrganisationStatus.Approved.name(),
                    registrationAllowed, skillsGatewayAccessAllowed);
        }
    }

    public void createTechnicalSupportOrganisation(Integer id, String name, String ceoName, String email, String address,
                                                   String city, String postCode) {
        if (!organisationService.organisationExistsById(id)) {
            jdbcTemplate.update("insert into ORGANISATION (id, name, ceo_name, email, entity_type, regulated, status, "
                            + "address1, address5, postcode)  values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", id, name, ceoName,
                    email, TECHNICAL_SUPPORT.getId(), false, OrganisationStatus.Approved.name(), address, city, postCode);
        }
    }

}
