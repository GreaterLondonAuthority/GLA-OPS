/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.organisation.implementation;

import org.springframework.stereotype.Component;
import uk.gov.london.ops.domain.organisation.Organisation;
import uk.gov.london.ops.web.model.OrganisationModel;
import uk.gov.london.ops.web.model.OrganisationUserDTO;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class OrganisationDTOMapper {

    public Organisation getOrganisationUserDTOFromOrg(OrganisationUserDTO dto) {
        Organisation org = new Organisation();
        org.setAddress(dto.getAddress());
        org.setContactNumber(dto.getContactNumber());
        org.setEntityType(dto.getEntityType());
        org.setManagingOrganisation(new Organisation(dto.getManagingOrganisationId(), ""));
        org.setName(dto.getName());
        org.setRegulated(dto.getRegulated());
        org.setSapVendorId(dto.getSapVendorId());
        org.setUkprn(dto.getUkprn());
        org.setWebsite(dto.getWebsite());
        org.setCeoName(dto.getCeoName());
        org.setEmail(dto.getEmail());
        org.setCeoTitle(dto.getCeoTitle());
        org.setTeam(dto.getTeam());
        org.setRegistrationAllowed(dto.getRegistrationAllowed());
        org.setContactEmail(dto.getContactEmail());
        return org;
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
        model.setId(organisation.getId());
        model.setName(organisation.getName());
        model.setManagingOrganisationId(organisation.getManagingOrganisationId());
        model.setStatus(organisation.getStatus());
        model.setIsTechOrg(organisation.isTechSupportOrganisation());
        model.setIsManagingOrganisation(organisation.isManagingOrganisation());
        model.setRegistrationAllowed(organisation.getRegistrationAllowed());
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
        dto.setTeam(organisation.getTeam());
        dto.setRegistrationAllowed(organisation.getRegistrationAllowed());
        return dto;
    }

}
