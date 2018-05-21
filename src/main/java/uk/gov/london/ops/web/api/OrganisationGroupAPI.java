/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.web.api;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.london.ops.domain.organisation.Organisation;
import uk.gov.london.ops.domain.organisation.OrganisationGroup;
import uk.gov.london.ops.domain.user.Role;
import uk.gov.london.ops.exception.ValidationException;
import uk.gov.london.ops.mapper.OrganisationGroupMapper;
import uk.gov.london.ops.mapper.OrganisationMapper;
import uk.gov.london.ops.service.OrganisationGroupService;
import uk.gov.london.ops.service.OrganisationService;
import uk.gov.london.ops.web.model.OrganisationGroupModel;
import uk.gov.london.ops.web.model.OrganisationModel;
import uk.gov.london.ops.web.model.project.FileImportResult;

import javax.validation.Valid;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
@Api(description = "managing organisation groups")
public class OrganisationGroupAPI {

    @Autowired
    OrganisationGroupService organisationGroupService;

    @Autowired
    OrganisationService organisationService;

    @Autowired
    OrganisationGroupMapper organisationGroupMapper;

    @Autowired
    OrganisationMapper organisationMapper;

    @Secured({Role.OPS_ADMIN, Role.GLA_ORG_ADMIN, Role.GLA_SPM, Role.GLA_PM, Role.GLA_FINANCE, Role.GLA_READ_ONLY, Role.ORG_ADMIN, Role.PROJECT_EDITOR, Role.TECH_ADMIN})
    @RequestMapping(value = "/organisationGroups", method = RequestMethod.GET)
    public @ResponseBody List<OrganisationGroup> findAll() {
        return organisationGroupService.findAll();
    }

    @Secured({Role.OPS_ADMIN, Role.GLA_ORG_ADMIN, Role.GLA_SPM, Role.GLA_PM, Role.GLA_FINANCE, Role.GLA_READ_ONLY, Role.ORG_ADMIN, Role.PROJECT_EDITOR, Role.TECH_ADMIN})
    @RequestMapping(value = "/organisationGroups/{id}", method = RequestMethod.GET)
    public @ResponseBody OrganisationGroup get(@PathVariable Integer id) {
        return organisationGroupService.find(id);
    }

    @Secured({Role.OPS_ADMIN, Role.ORG_ADMIN})
    @RequestMapping(value = "/organisationGroups/organisation", method = RequestMethod.GET)
    @ApiOperation(
            value="lookup organisation by ID or IMS number and validate for consortium or partnership creation",
            notes="lookup organisation by ID or IMS number and validate for consortium or partnership creation"
    )
    public @ResponseBody OrganisationModel lookupOrganisation(@RequestParam String orgCode) {
        Organisation organisation = organisationService.findByOrgIdOrImsNumber(orgCode);
        organisationGroupService.validateForConsortiumCreation(organisation);
        return OrganisationModel.from(organisation);
    }

    @Secured({Role.OPS_ADMIN, Role.ORG_ADMIN})
    @RequestMapping(value = "/organisationGroups/{id}/organisationsInProjects", method = RequestMethod.GET)
    @ApiOperation("find a list of organisations which have created or are developers of projects within the given organisation group")
    public @ResponseBody List<OrganisationModel> groupOrganisationsInProjects(@PathVariable Integer id) {
        return organisationMapper.toModel(organisationGroupService.getGroupOrganisationsInProjects(id));
    }

    @Secured({Role.OPS_ADMIN, Role.GLA_ORG_ADMIN, Role.GLA_SPM, Role.GLA_PM, Role.GLA_FINANCE, Role.GLA_READ_ONLY, Role.ORG_ADMIN, Role.PROJECT_EDITOR, Role.TECH_ADMIN})
    @RequestMapping(value = "/organisationGroupsForOrg/{orgId}/programme/{programmeId}", method = RequestMethod.GET)
    @ApiOperation(
            value="Find all organisation group on the specified programme that include the given organisation",
            notes="Find all organisation group on the specified programme that include the given organisation"
    )
    public List<OrganisationGroupModel>  lookupOrganisationProgrammeId(@PathVariable Integer orgId, @PathVariable Integer programmeId) {
        return organisationGroupMapper.mapToModel(organisationGroupService.getOrganisationGroupsByProgrammeAndOrganisation(programmeId, orgId));
    }

    @Secured({Role.OPS_ADMIN, Role.ORG_ADMIN})
    @RequestMapping(value = "/organisationGroups", method = RequestMethod.POST)
    @ApiOperation("creates an organisation group")
    public @ResponseBody OrganisationGroup create(@Valid @RequestBody OrganisationGroup organisationGroup, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            throw new ValidationException("Invalid organisation group details!", bindingResult.getFieldErrors());
        }
        return organisationGroupService.save(organisationGroup);
    }

    @Secured({Role.OPS_ADMIN, Role.ORG_ADMIN})
    @RequestMapping(value = "/organisationGroups/{id}", method = RequestMethod.PUT)
    @ApiOperation("updates an organisation group")
    public @ResponseBody OrganisationGroup update(@PathVariable Integer id, @Valid @RequestBody OrganisationGroup organisationGroup, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            throw new ValidationException("Invalid organisation group details!", bindingResult.getFieldErrors());
        }
        return organisationGroupService.update(id, organisationGroup);
    }

    @PreAuthorize("authentication.name == 'test.admin@gla.com'")
    @RequestMapping(value = "/organisationGroups/{id}", method = RequestMethod.DELETE)
    @ApiOperation("deletes an organisation group")
    public void delete(@PathVariable Integer id) {
        organisationGroupService.delete(id);
    }

    @Secured(Role.OPS_ADMIN)
    @RequestMapping(value = "/organisationGroups/imsImport", method = RequestMethod.POST)
    @ApiOperation(value = "Internal API uploading the Consortiums csv file", hidden = true)
    public FileImportResult importOrganisationGroups(MultipartFile file) throws IOException {
        return organisationGroupService.importOrganisationGroupFile(file.getInputStream());
    }



}
