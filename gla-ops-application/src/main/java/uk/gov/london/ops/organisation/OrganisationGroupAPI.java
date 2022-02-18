/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.organisation;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import uk.gov.london.ops.organisation.dto.OrganisationDTOMapper;
import uk.gov.london.ops.organisation.dto.OrganisationGroupMapper;
import uk.gov.london.ops.organisation.dto.OrganisationGroupModel;
import uk.gov.london.ops.organisation.dto.OrganisationModel;
import uk.gov.london.ops.organisation.model.OrganisationEntity;
import uk.gov.london.ops.organisation.model.OrganisationGroup;

import javax.validation.Valid;
import java.util.List;

import static uk.gov.london.common.user.BaseRole.*;
import static uk.gov.london.ops.framework.OPSUtils.verifyBinding;

@RestController
@RequestMapping("/api/v1")
@Api(description = "managing organisation groups")
public class OrganisationGroupAPI {

    @Autowired
    OrganisationGroupService organisationGroupService;

    @Autowired
    OrganisationServiceImpl organisationService;

    @Autowired
    OrganisationGroupMapper organisationGroupMapper;

    @Autowired
    OrganisationDTOMapper organisationDTOMapper;

    @Secured({OPS_ADMIN, GLA_ORG_ADMIN, GLA_SPM, GLA_PM, GLA_FINANCE, GLA_READ_ONLY, ORG_ADMIN, PROJECT_EDITOR, PROJECT_READER,
            TECH_ADMIN})
    @RequestMapping(value = "/organisationGroups", method = RequestMethod.GET)
    public @ResponseBody
    List<OrganisationGroup> findAll() {
        return organisationGroupService.findAll();
    }

    @Secured({OPS_ADMIN, GLA_ORG_ADMIN, GLA_SPM, GLA_PM, GLA_FINANCE, GLA_READ_ONLY, ORG_ADMIN, PROJECT_EDITOR, PROJECT_READER,
            TECH_ADMIN})
    @RequestMapping(value = "/organisationGroups/{id}", method = RequestMethod.GET)
    public @ResponseBody
    OrganisationGroup get(@PathVariable Integer id) {
        return organisationGroupService.find(id);
    }

    @Secured({OPS_ADMIN, ORG_ADMIN})
    @RequestMapping(value = "/organisationGroups/organisation", method = RequestMethod.GET)
    @ApiOperation(
            value = "lookup organisation by ID or provider number and validate for consortium or partnership creation",
            notes = "lookup organisation by ID or provider number and validate for consortium or partnership creation"
    )
    public @ResponseBody
    OrganisationModel lookupOrganisation(@RequestParam String orgCode) {
        OrganisationEntity organisation = organisationService.findByOrgCode(orgCode, true);
        organisationGroupService.validateForConsortiumCreation(organisation);
        return organisationDTOMapper.getOrganisationModelFromOrg(organisation);
    }

    @Secured({OPS_ADMIN, ORG_ADMIN})
    @RequestMapping(value = "/organisationGroups/{id}/organisationsInProjects", method = RequestMethod.GET)
    @ApiOperation("find organisations which have created or are developers of projects within the given organisation group")
    public @ResponseBody
    List<OrganisationModel> groupOrganisationsInProjects(@PathVariable Integer id) {
        return organisationDTOMapper.getOrganisationModelsFromOrgs(organisationGroupService.getGroupOrganisationsInProjects(id));
    }

    @Secured({OPS_ADMIN, GLA_ORG_ADMIN, GLA_SPM, GLA_PM, GLA_FINANCE, GLA_PROGRAMME_ADMIN, GLA_READ_ONLY, ORG_ADMIN,
            PROJECT_EDITOR, PROJECT_READER,
            TECH_ADMIN})
    @RequestMapping(value = "/organisationGroupsForOrg/{orgId}/programme/{programmeId}", method = RequestMethod.GET)
    @ApiOperation(
            value = "Find all organisation group on the specified programme that include the given organisation",
            notes = "Find all organisation group on the specified programme that include the given organisation"
    )
    public List<OrganisationGroupModel> lookupOrganisationProgrammeId(@PathVariable Integer orgId,
                                                                      @PathVariable Integer programmeId) {
        return organisationGroupMapper
                .mapToModel(organisationGroupService.getOrganisationGroupsByProgrammeAndOrganisation(programmeId, orgId));
    }

    @Secured({OPS_ADMIN, ORG_ADMIN})
    @RequestMapping(value = "/organisationGroups", method = RequestMethod.POST)
    @ApiOperation("creates an organisation group")
    public @ResponseBody
    OrganisationGroup create(@Valid @RequestBody OrganisationGroup organisationGroup, BindingResult bindingResult) {
        verifyBinding("Invalid organisation group details!", bindingResult);
        return organisationGroupService.save(organisationGroup);
    }

    @Secured({OPS_ADMIN, ORG_ADMIN})
    @RequestMapping(value = "/organisationGroups/{id}", method = RequestMethod.PUT)
    @ApiOperation("updates an organisation group")
    public @ResponseBody
    OrganisationGroup update(@PathVariable Integer id, @Valid @RequestBody OrganisationGroup organisationGroup,
                             BindingResult bindingResult) {
        verifyBinding("Invalid organisation group details!", bindingResult);
        return organisationGroupService.update(id, organisationGroup);
    }

    @PreAuthorize("authentication.name == 'test.admin@gla.com'")
    @RequestMapping(value = "/organisationGroups/{id}", method = RequestMethod.DELETE)
    @ApiOperation("deletes an organisation group")
    public void delete(@PathVariable Integer id) {
        organisationGroupService.delete(id);
    }

}
