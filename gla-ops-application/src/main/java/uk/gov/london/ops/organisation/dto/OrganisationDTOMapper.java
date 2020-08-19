/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.organisation.dto;

import org.springframework.stereotype.Component;
import uk.gov.london.ops.organisation.model.LegalStatus;
import uk.gov.london.ops.organisation.model.Organisation;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class OrganisationDTOMapper {

    public Organisation getOrganisationUserDTOFromOrg(OrganisationUserDTO dto) {
        Organisation org = new Organisation();
        org.setAddress(dto.getAddress());
        org.setContactNumber(dto.getContactNumber());
        org.setEntityType(dto.getEntityType());
        org.setManagingOrganisation(new Organisation(dto.managingOrganisationId, ""));
        org.setName(dto.name);
        org.setRegulated(dto.getRegulated());
        org.setSapVendorId(dto.getSapVendorId());
        org.setUkprn(dto.getUkprn());
        org.setWebsite(dto.getWebsite());
        org.setCeoName(dto.getCeoName());
        org.setEmail(dto.getEmail());
        org.setCeoTitle(dto.getCeoTitle());
        org.setRegistrationAllowed(dto.registrationAllowed);
        org.setContactEmail(dto.getContactEmail());
        org.setDefaultProgrammeId(dto.getDefaultProgrammeId());
        org.setCompanyCode(dto.getCompanyCode());
        org.setVatNumber(dto.getVatNumber());
        org.setSortCode(dto.getSortCode());
        org.setBankAccount(dto.getBankAccount());
        org.setLegalStatus(LegalStatus.valueOf(dto.getLegalStatus()));
        return org;
    }

    public OrganisationUserDTO getOrganisationUserDTOFromOrg(Organisation organisation) {
        OrganisationUserDTO dto = new OrganisationUserDTO();
        populateModelFromOrg(dto, organisation);
        dto.setAddress(organisation.getAddress());
        dto.setContactNumber(organisation.getContactNumber());
        dto.setEntityType(organisation.getEntityType());
        dto.setRegulated(organisation.isRegulated());
        dto.setSapVendorId(organisation.getsapVendorId());
        dto.setWebsite(organisation.getWebsite());
        dto.setEmail(organisation.getEmail());
        dto.setCeoName(organisation.getCeoName());
        dto.setCeoTitle(organisation.getCeoTitle());
        dto.registrationAllowed = organisation.getRegistrationAllowed();
        return dto;
    }

    public List<OrganisationModel> getOrganisationModelsFromOrgs(List<Organisation> organisations) {
        return organisations.stream().map(this::getOrganisationModelFromOrg).collect(Collectors.toList());
    }

    public OrganisationModel getOrganisationModelFromOrg(Organisation organisation) {
        OrganisationModel model = new OrganisationModel();
        populateModelFromOrg(model, organisation);
        return model;
    }

    private void populateModelFromOrg(OrganisationModel model, Organisation organisation) {
        model.id = organisation.getId();
        model.name = organisation.getName();
        model.managingOrganisationId = organisation.getManagingOrganisationId();
        model.status = organisation.getStatus();
        model.isTechOrg = organisation.isTechSupportOrganisation();
        model.isManagingOrganisation = organisation.isManagingOrganisation();
        model.registrationAllowed = organisation.getRegistrationAllowed();
        model.skillsGatewayAccessAllowed = organisation.isSkillsGatewayAccessAllowed();
    }

}
