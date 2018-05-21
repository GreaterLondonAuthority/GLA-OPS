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
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.annotation.Secured;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.london.ops.FeatureStatus;
import uk.gov.london.ops.domain.organisation.*;
import uk.gov.london.ops.domain.project.ProgrammeRequestedAndPaidRecord;
import uk.gov.london.ops.domain.user.Role;
import uk.gov.london.ops.exception.ApiError;
import uk.gov.london.ops.exception.ForbiddenAccessException;
import uk.gov.london.ops.exception.ValidationException;
import uk.gov.london.ops.mapper.OrganisationMapper;
import uk.gov.london.ops.service.AuditService;
import uk.gov.london.ops.service.OrganisationService;
import uk.gov.london.ops.service.PermissionService;
import uk.gov.london.ops.web.model.BulkUploadSummary;
import uk.gov.london.ops.web.model.ContractModel;

import javax.validation.Valid;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Spring MVC controller for the Organisations REST endpoint.
 *
 * Created by sleach on 17/08/2016.
 */
@RestController
@RequestMapping("/api/v1")
@Api(
    description = "managing Organisation data"
)
public class OrganisationAPI {

    @Autowired
    private OrganisationService organisationService;

    @Autowired
    private OrganisationMapper organisationMapper;

    @Autowired
    private PermissionService permissionService;

    @Autowired
    private AuditService auditService;

    @Autowired
    private FeatureStatus featureStatus;

    @Secured({Role.OPS_ADMIN, Role.ORG_ADMIN})
    @RequestMapping(value = "/organisations", method = RequestMethod.GET)
    @ApiOperation(  value="get all organisation data",
            notes="retrieves a list of all organisations")
    public List<Organisation> getAll(@RequestParam(required = false) List<Integer> entityTypes) {
        return organisationService.findAll(entityTypes);
    }

    @Secured({Role.PROJECT_EDITOR, Role.ORG_ADMIN, Role.GLA_PM, Role.GLA_FINANCE, Role.GLA_READ_ONLY, Role.GLA_ORG_ADMIN, Role.GLA_SPM, Role.OPS_ADMIN, Role.TECH_ADMIN})
    @RequestMapping(value = "/organisations/page", method = RequestMethod.GET)
    public Page getAllPaged(@RequestParam(required = false) String searchText,
                            @RequestParam(required = false) List<Integer> entityTypes,
                            @RequestParam(required = false) List<OrganisationStatus> orgStatuses,
                            @RequestParam(required = false) List<RegistrationStatus> userRegStatuses,
                            Pageable pageable) {
        if (featureStatus.isEnabled(FeatureStatus.Feature.ManagingOrgFilter)) {
            return organisationService.getSummaries(searchText, entityTypes, orgStatuses, userRegStatuses, pageable);
        }
        else {
            return organisationService.findAll(userRegStatuses, pageable);
        }
    }

    @RequestMapping(value = "/organisations/{id}", method = RequestMethod.GET)
    @ApiOperation(  value="get organisation by ID", notes="retrieves a single organisation")
    public Organisation getById(@PathVariable Integer id) {
        Organisation organisation = organisationService.find(id);
        organisation.setContracts(organisationService.getContracts(id));
        organisation.setProgrammes(organisationService.getProgrammes(id));
        return organisation;
    }

    @RequestMapping(value = "/organisations/{orgCode}/name", method = RequestMethod.GET, produces = "text/plain")
    public String getNameByOrgIdOrImsNumber(@PathVariable String orgCode) {
        return organisationService.findByOrgIdOrImsNumber(orgCode).getName();
    }

    @Secured({Role.OPS_ADMIN, Role.GLA_ORG_ADMIN, Role.GLA_SPM, Role.GLA_PM, Role.ORG_ADMIN, Role.PROJECT_EDITOR, Role.GLA_FINANCE, Role.GLA_READ_ONLY})
    @RequestMapping(value = "/checkOrganisationNameNotUsed", method = RequestMethod.GET)
    @ApiOperation(value = "returns a 404 if the given name is already used by another organisation, case insensitive.")
    public void checkOrganisationNameNotUsed(@RequestParam String name) {
        organisationService.checkOrganisationNameNotUsed(name);
    }

    @Secured({Role.OPS_ADMIN, Role.ORG_ADMIN})
    @RequestMapping(value = "/organisations", method = RequestMethod.POST)
    @ApiOperation(  value="creates an organisation", notes="creates an organisation")
    @ApiResponses(@ApiResponse(code=400, message="validation error", response=ApiError.class))
    public @ResponseBody Organisation create(@Valid @RequestBody Organisation organisation, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            throw new ValidationException("Invalid organisation details!", bindingResult.getFieldErrors());
        }
        return organisationService.create(organisation);
    }

    @RequestMapping(value = "/organisations/{orgCode}/users/{username:.+}", method = RequestMethod.POST)
    @ApiOperation(  value="user requests to be linked to an organisation", notes="user requests to be linked to an organisation")
    public void linkUser(@PathVariable String orgCode, @PathVariable String username) {
        organisationService.linkUserToOrganisation(orgCode, username);
    }

    @Secured(Role.OPS_ADMIN)
    @RequestMapping(value = "/organisations/upload", method = RequestMethod.POST)
    @ApiOperation(  value="bulk organisation upload",
                    notes="Endpoint for uploading multiple organisations from a CSV file")
    public BulkUploadSummary upload(MultipartFile file) throws IOException {
        BulkUploadSummary summary = new BulkUploadSummary();

        List<Organisation> organisations = organisationMapper.toEntities(file.getInputStream(), summary);

        int loaded = 0;
        for (Organisation organisation : organisations) {
            try {
                organisationService.create(organisation);
                loaded++;
            } catch (DataIntegrityViolationException e) {
                summary.addError(loaded, e.getMessage());
            }
        }

        summary.setEntitiesLoaded(loaded);
        summary.setAllLoaded(summary.getEntitiesLoaded() == summary.getSourceRows());

        return summary;
    }

    @Secured({Role.OPS_ADMIN, Role.GLA_ORG_ADMIN, Role.ORG_ADMIN})
    @RequestMapping(value = "/organisations/{id}", method = RequestMethod.PUT)
    @ApiOperation(  value="update an existing organisation", notes="updates organisation details")
    public void update(@PathVariable Integer id, @RequestBody Organisation organisation) {
        if ((organisation.getId() == null) || !organisation.getId().equals(id)) {
            throw new ValidationException("Can only update organisations with a valid ID");
        }
        if (!permissionService.currentUserHasPermissionForOrganisation(PermissionService.ORG_EDIT_DETAILS, id)) {
            throw new ForbiddenAccessException("User does not have permission to edit organisation " + id);
        }
        if (organisationService.nameOrIMSCOdeChanged(organisation)) {
            if (!permissionService.currentUserHasPermissionForOrganisation(PermissionService.ORG_EDIT_NAME, id)) {
                throw new ForbiddenAccessException("User does not have permission to edit organisation name or IMS code for " + id);
            }
            auditService.auditCurrentUserActivity("Organisation name and/or IMS Code changed: " + id);
        }
        organisationService.update(organisation);
    }

    @Secured(Role.OPS_ADMIN)
    @RequestMapping(value = "/organisations/{id}/users/{username:.+}", method = RequestMethod.PUT)
    @ApiOperation(  value="adds a user to an organisation", notes="adds a user to an organisation")
    public void addUser(@PathVariable Integer id, @PathVariable String username) {
        organisationService.addUserToOrganisation(id, username);
    }

    @Secured({Role.OPS_ADMIN, Role.GLA_ORG_ADMIN, Role.ORG_ADMIN})
    @RequestMapping(value = "/organisations/{id}/users/{username}/approved", method = RequestMethod.PUT)
    public void approve(@PathVariable Integer id, @PathVariable String username, @RequestBody String approved) {
        if (Boolean.parseBoolean(approved)) {
            organisationService.approve(id, username);
        }
        else {
            organisationService.unapprove(id, username);
        }
    }

    @Secured({Role.OPS_ADMIN, Role.GLA_ORG_ADMIN, Role.GLA_SPM, Role.GLA_PM})
    @RequestMapping(value = "/organisations/{organisationId}/contracts", method = RequestMethod.POST)
    public void createContract(@PathVariable Integer organisationId, @RequestBody ContractModel model) {
        organisationService.createContract(organisationId, model);
    }

    @Secured({Role.OPS_ADMIN, Role.GLA_ORG_ADMIN, Role.GLA_SPM, Role.GLA_PM})
    @RequestMapping(value = "/organisations/{organisationId}/contracts/{contractId}", method = RequestMethod.PUT)
    public void updateContract(@PathVariable Integer organisationId, @PathVariable Integer contractId, @RequestBody ContractModel model) {
        organisationService.updateContract(organisationId, model);
    }

    @Secured({Role.OPS_ADMIN, Role.GLA_ORG_ADMIN})
    @RequestMapping(value = "/organisations/{organisationId}/status", method = RequestMethod.PUT)
    public void updateStatus(@PathVariable Integer organisationId, @RequestBody String status) {
        organisationService.updateStatus(organisationId, OrganisationStatus.valueOf(status));
    }

    @Secured({Role.OPS_ADMIN, Role.GLA_ORG_ADMIN, Role.ORG_ADMIN})
    @RequestMapping(value = "/organisations/{id}/users/{username:.+}", method = RequestMethod.DELETE)
    public void removeUser(@PathVariable Integer id, @PathVariable String username) {
        organisationService.removeUserFromOrganisation(id, username);
    }

    @Secured({Role.OPS_ADMIN})
    @RequestMapping(value = "/organisations/{id}", method = RequestMethod.DELETE)
    public void removeOrganisation(@PathVariable Integer id) {
        organisationService.deleteOrganisation(id);
    }

    @Secured({Role.OPS_ADMIN, Role.GLA_ORG_ADMIN, Role.GLA_SPM, Role.GLA_PM, Role.ORG_ADMIN, Role.PROJECT_EDITOR, Role.GLA_FINANCE, Role.GLA_READ_ONLY, Role.TECH_ADMIN})
    @RequestMapping(value = "/organisations/{organisationId}/programmes/{programmeId}", method = RequestMethod.GET)
    public OrganisationProgramme getOrganisationProgramme(@PathVariable Integer organisationId, @PathVariable Integer programmeId) {
        return organisationService.getOrganisationProgramme(organisationId, programmeId);
    }

    @Secured({Role.OPS_ADMIN, Role.GLA_ORG_ADMIN, Role.GLA_SPM, Role.GLA_PM, Role.ORG_ADMIN, Role.PROJECT_EDITOR, Role.GLA_FINANCE, Role.GLA_READ_ONLY})
    @RequestMapping(value = "/organisations/{organisationId}/programmes/{programmeId}/paymentsAndRequests", method = RequestMethod.GET)
    public ProgrammeRequestedAndPaidRecord getPaymentsAndRequests(@PathVariable Integer organisationId, @PathVariable Integer programmeId) {
        return organisationService.getRequestedAndPaidRecord(programmeId, organisationId);
    }

    @Secured({Role.OPS_ADMIN, Role.GLA_ORG_ADMIN, Role.GLA_SPM, Role.GLA_PM})
    @RequestMapping(value = "/organisations/{organisationId}/programmes/{programmeId}/budget", method = RequestMethod.POST)
    public OrganisationBudgetEntry createBudgetEntry(@PathVariable Integer organisationId, @PathVariable Integer programmeId,
                                  @Valid @RequestBody OrganisationBudgetEntry entry) {
        return organisationService.saveBudgetEntry(organisationId, programmeId, entry);
    }

    @Secured({Role.OPS_ADMIN, Role.GLA_ORG_ADMIN, Role.GLA_SPM, Role.GLA_PM})
    @RequestMapping(value = "/organisations/{organisationId}/programmes/{programmeId}", method = RequestMethod.PUT)
    public void updateOrganisationProgramme(@PathVariable Integer organisationId, @PathVariable Integer programmeId,
                                  @Valid @RequestBody OrganisationProgramme organisationProgramme) {
        organisationService.updateOrganisationProgramme(organisationId, programmeId, organisationProgramme);
    }

    @Secured({Role.OPS_ADMIN, Role.GLA_ORG_ADMIN, Role.GLA_SPM, Role.GLA_PM})
    @RequestMapping(value = "/organisations/{organisationId}/programmes/{programmeId}/budget/{entryId}", method = RequestMethod.PUT)
    public void updateBudgetEntry(@PathVariable Integer organisationId, @PathVariable Integer programmeId, @PathVariable Integer entryId,
                                  @Valid @RequestBody OrganisationBudgetEntry entry) {
        organisationService.saveBudgetEntry(organisationId, programmeId, entry);
    }

    @Secured({Role.OPS_ADMIN, Role.GLA_ORG_ADMIN, Role.GLA_SPM, Role.GLA_PM})
    @RequestMapping(value = "/organisations/{organisationId}/programmes/{programmeId}/budget/{entryId}", method = RequestMethod.DELETE)
    public void deleteBudgetEntry(@PathVariable Integer organisationId, @PathVariable Integer programmeId, @PathVariable Integer entryId) {
        organisationService.deleteBudgetEntry(entryId);
    }

    @Secured({Role.OPS_ADMIN, Role.GLA_ORG_ADMIN, Role.GLA_SPM, Role.GLA_PM, Role.ORG_ADMIN, Role.PROJECT_EDITOR, Role.GLA_FINANCE, Role.GLA_READ_ONLY, Role.TECH_ADMIN})
    @RequestMapping(value = "/organisations/types", method = RequestMethod.GET)
    public Map<Integer, String> getOrganisationTypes() {
        return Arrays.stream(OrganisationType.values()).collect(Collectors.toMap(OrganisationType::id, OrganisationType::summary));
    }

}
