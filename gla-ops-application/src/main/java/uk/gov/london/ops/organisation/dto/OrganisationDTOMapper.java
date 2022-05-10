/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.organisation.dto;

import org.springframework.stereotype.Component;
import uk.gov.london.ops.organisation.SapId;
import uk.gov.london.ops.organisation.model.OrganisationEntity;
import uk.gov.london.ops.organisation.model.SapIdEntity;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class OrganisationDTOMapper {

    public OrganisationEntity getOrganisationUserDTOFromOrg(OrganisationUserDTO dto) {
        OrganisationEntity org = new OrganisationEntity();
        org.setAddress(dto.getAddress());
        org.setContactNumber(dto.getContactNumber());
        org.setEntityType(dto.getEntityType());
        org.setManagingOrganisation(new OrganisationEntity(dto.managingOrganisationId, ""));
        org.setName(dto.name);
        org.setRegulated(dto.getRegulated());
        org.setIsLearningProvider(dto.getIsLearningProvider());
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
        org.setProviderNumber(dto.getProviderNumber());
        org.setTeam(dto.getTeam());
        org.setParentOrganisation(dto.getParentOrganisation());
        org.setViability(dto.getViability());
        org.setGovernance(dto.getGovernance());
        if (dto.getSapIds() != null) {
            org.getSapIds().addAll(dto.getSapIds().stream().map(this::toSapIdEntity).collect(Collectors.toSet()));
        }
        org.setKnownAs(dto.getKnownAs());
        org.setSocietyNumber(dto.getSocietyNumber());
        org.setIsCharityCommission(dto.getIsCharityCommission());
        org.setCharityNumber(dto.getCharityNumber());
        return org;
    }

    public OrganisationUserDTO getOrganisationUserDTOFromOrg(OrganisationEntity organisation) {
        OrganisationUserDTO dto = new OrganisationUserDTO();
        populateModelFromOrg(dto, organisation);
        dto.setAddress(organisation.getAddress());
        dto.setContactNumber(organisation.getContactNumber());
        dto.setEntityType(organisation.getEntityType());
        dto.setRegulated(organisation.isRegulated());
        dto.setWebsite(organisation.getWebsite());
        dto.setEmail(organisation.getEmail());
        dto.setCeoName(organisation.getCeoName());
        dto.setCeoTitle(organisation.getCeoTitle());
        dto.registrationAllowed = organisation.getRegistrationAllowed();
        if (organisation.getSapIds() != null) {
            dto.getSapIds().addAll(organisation.getSapIds().stream().map(this::toSapIdModel).collect(Collectors.toSet()));
        }
        dto.setKnownAs(organisation.getKnownAs());
        dto.setSocietyNumber(organisation.getSocietyNumber());
        dto.setIsLearningProvider(organisation.getIsLearningProvider());
        dto.setUkprn(organisation.getUkprn());
        dto.setProviderNumber(organisation.getProviderNumber());
        dto.setIsCharityCommission(organisation.getIsCharityCommission());
        dto.setCharityNumber(organisation.getCharityNumber());
        return dto;
    }

    private SapIdEntity toSapIdEntity(SapId model) {
        return new SapIdEntity(model.getSapId(), model.getOrganisationId(), model.getDescription(), OffsetDateTime.now(),
                model.isDefaultSapId());
    }

    private SapId toSapIdModel(SapIdEntity entity) {
        return new SapId(entity.getSapId(), entity.getOrganisationId(), entity.getDescription(), entity.getCreatedOn(),
                entity.getDefaultSapId());
    }

    public List<OrganisationModel> getOrganisationModelsFromOrgs(List<OrganisationEntity> organisations) {
        return organisations.stream().map(this::getOrganisationModelFromOrg).collect(Collectors.toList());
    }

    public OrganisationModel getOrganisationModelFromOrg(OrganisationEntity organisation) {
        OrganisationModel model = new OrganisationModel();
        populateModelFromOrg(model, organisation);
        return model;
    }

    private void populateModelFromOrg(OrganisationModel model, OrganisationEntity organisation) {
        model.id = organisation.getId();
        model.name = organisation.getName();
        model.managingOrganisationId = organisation.getManagingOrganisationId();
        model.status = organisation.getStatus();
        model.isTechOrg = organisation.isTechSupportOrganisation();
        model.isManagingOrganisation = organisation.isManaging();
        model.registrationAllowed = organisation.getRegistrationAllowed();
        model.skillsGatewayAccessAllowed = organisation.isSkillsGatewayAccessAllowed();
    }

}
