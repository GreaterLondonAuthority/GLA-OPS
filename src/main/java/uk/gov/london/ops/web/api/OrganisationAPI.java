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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.london.common.organisation.OrganisationType;
import uk.gov.london.ops.domain.organisation.*;
import uk.gov.london.ops.domain.project.ProgrammeRequestedAndPaidRecord;
import uk.gov.london.ops.organisation.implementation.OrganisationMapper;
import uk.gov.london.ops.service.OrganisationService;
import uk.gov.london.ops.service.PermissionType;
import uk.gov.london.ops.framework.annotations.PermissionRequired;
import uk.gov.london.common.error.ApiError;
import uk.gov.london.ops.framework.exception.ValidationException;
import uk.gov.london.ops.web.model.*;

import javax.validation.Valid;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static uk.gov.london.common.user.BaseRole.*;
import static uk.gov.london.ops.framework.web.APIUtils.verifyBinding;

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

    @Secured({OPS_ADMIN, ORG_ADMIN})
    @RequestMapping(value = "/organisations", method = RequestMethod.GET)
    @ApiOperation(value = "get all organisation data",
            notes = "retrieves a list of all organisations")
    public List<Organisation> getAll(@RequestParam(required = false) List<Integer> entityTypes) {
        return organisationService.findAll(entityTypes);
    }

    @Secured({PROJECT_EDITOR, PROJECT_READER, ORG_ADMIN, GLA_PM, GLA_FINANCE, GLA_READ_ONLY, GLA_ORG_ADMIN, GLA_SPM, OPS_ADMIN, TECH_ADMIN})
    @RequestMapping(value = "/organisations/page", method = RequestMethod.GET)
    public Page getAllPaged(@RequestParam(required = false) String searchText,
                            @RequestParam(required = false) List<Integer> entityTypes,
                            @RequestParam(required = false) List<OrganisationStatus> orgStatuses,
                            @RequestParam(required = false) List<RegistrationStatus> userRegStatuses,
                            @RequestParam(required = false) List<String> teams,
                            Pageable pageable) {
        List<OrganisationTeam> teamsList = new ArrayList<>();
        if (teams != null) {
            for (String team : teams) {
                String[] split = team.split("\\|");
                if (split.length < 1) {
                    throw new ValidationException("Teams param must be split by '|' symbol");
                }
                if (split.length == 1) {
                    teamsList.add(new OrganisationTeam(Integer.parseInt(split[0]), (Integer) null));
                } else {
                    teamsList.add(new OrganisationTeam(Integer.parseInt(split[0]), Integer.parseInt(split[1])));
                }
            }
        }
        return organisationService.getSummaries(searchText, entityTypes, orgStatuses, userRegStatuses, teamsList, pageable);
    }

    @PreAuthorize("isAuthenticated()")
    @RequestMapping(value = "/organisations/{id}", method = RequestMethod.GET)
    @ApiOperation(value = "get organisation by ID", notes = "retrieves a single organisation")
    public Organisation getById(@PathVariable Integer id) {
        return organisationService.getEnrichedOrganisation(id);
    }

    @RequestMapping(value = "/organisations/{orgCode}/name", method = RequestMethod.GET, produces = "text/plain")
    public String getNameByOrgCode(@PathVariable String orgCode) {
        return organisationService.findByOrgCode(orgCode).getName();
    }

    @RequestMapping(value = "/checkOrganisationNameNotUsed", method = RequestMethod.GET)
    @ApiOperation(value = "returns a 404 if the given name is already used by another organisation, case insensitive.")
    public void checkOrganisationNameNotUsed(@RequestParam String name, @RequestParam Integer managingOrganisationId) {
        organisationService.checkOrganisationNameNotUsed(name, managingOrganisationId);
    }

    @RequestMapping(value = "/organisations", method = RequestMethod.POST)
    @ApiOperation(value = "creates an organisation", notes = "creates an organisation")
    @ApiResponses(@ApiResponse(code = 400, message = "validation error", response = ApiError.class))
    public @ResponseBody
    OrganisationUserDTO createUnsecured(@Valid @RequestBody OrganisationUserDTO organisation, BindingResult bindingResult) {
        verifyBinding("Invalid organisation details!", bindingResult);
        return organisationService.create(organisation);
    }

    @RequestMapping(value = "/managingOrganisations", method = RequestMethod.GET)
    @ApiOperation(value = "retrieves basic list of managing organisation", notes = "retrieves managing organisations")
    @ApiResponses(@ApiResponse(code = 400, message = "validation error", response = ApiError.class))
    public List<OrganisationSummary> getManagingOrganisations(
            @RequestParam(value = "allowedRegistrationOnly", required = false, defaultValue = "false") Boolean allowedRegistrationOnly
    ) {

        List<OrganisationSummary> summaries = organisationService.findAllByType(OrganisationType.MANAGING_ORGANISATION);


        // as open URL remove any other data.
        List<OrganisationSummary> organisationSummaries = new ArrayList<>();
        for (OrganisationSummary summary : summaries) {
            Boolean registrationAllowed = summary.getRegistrationAllowed() != null && summary.getRegistrationAllowed();
            if (!allowedRegistrationOnly || registrationAllowed) {
                OrganisationSummary sum = new OrganisationSummary();
                sum.setId(summary.getId());
                sum.setRegistrationAllowed(registrationAllowed);
                sum.setName(summary.getName());
                organisationSummaries.add(sum);
            }
        }
        return organisationSummaries;
    }

    @Secured({GLA_PM, GLA_FINANCE, GLA_READ_ONLY, GLA_ORG_ADMIN, GLA_SPM, OPS_ADMIN})
    @RequestMapping(value = "/managingOrganisationsAndTeams", method = RequestMethod.GET)
    @ApiOperation(value = "retrieves basic list of managing organisation", notes = "retrieves managing organisations")
    @ApiResponses(@ApiResponse(code = 400, message = "validation error", response = ApiError.class))
    public Set<OrganisationTeam> getManagingOrganisationsAndTeams() {

        return organisationService.getManagedOrganisationAndTeams();
    }


    @PermissionRequired(PermissionType.ORG_REQUEST_ACCESS)
    @RequestMapping(value = "/organisations/{orgCode}/users/{username:.+}", method = RequestMethod.POST)
    @ApiOperation(value = "user requests to be linked to an organisation", notes = "user requests to be linked to an organisation")
    public void linkUser(@PathVariable String orgCode, @PathVariable String username) {
        organisationService.linkUserToOrganisation(orgCode, username);
    }

    @Secured(OPS_ADMIN)
    @RequestMapping(value = "/organisations/upload", method = RequestMethod.POST)
    @ApiOperation(value = "bulk organisation upload",
            notes = "Endpoint for uploading multiple organisations from a CSV file")
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

    @Secured({OPS_ADMIN, GLA_ORG_ADMIN, ORG_ADMIN})
    @RequestMapping(value = "/organisations/{id}", method = RequestMethod.PUT)
    @ApiOperation(value = "update an existing organisation", notes = "updates organisation details")
    public void update(@PathVariable Integer id, @RequestBody Organisation organisation) {
        if ((organisation.getId() == null) || !organisation.getId().equals(id)) {
            throw new ValidationException("Can only update organisations with a valid ID");
        }
        organisationService.update(organisation);
    }

    @Secured(OPS_ADMIN)
    @RequestMapping(value = "/organisations/{id}/users/{username:.+}", method = RequestMethod.PUT)
    @ApiOperation(value = "adds a user to an organisation", notes = "adds a user to an organisation")
    public void addUser(@PathVariable Integer id, @PathVariable String username) {
        organisationService.addUserToOrganisation(id, username);
    }

    @Secured({OPS_ADMIN, GLA_ORG_ADMIN, ORG_ADMIN})
    @RequestMapping(value = "/organisations/{id}/users/{username}/approved", method = RequestMethod.PUT)
    public void approve(@PathVariable Integer id, @PathVariable String username, @RequestBody String approved) {
        if (Boolean.parseBoolean(approved)) {
            organisationService.approve(id, username);
        } else {
            organisationService.unapprove(id, username);
        }
    }

    @Secured({OPS_ADMIN, GLA_ORG_ADMIN, GLA_SPM, GLA_PM})
    @RequestMapping(value = "/organisations/{organisationId}/contracts", method = RequestMethod.POST)
    public void createContract(@PathVariable Integer organisationId, @RequestBody ContractModel model) {
        organisationService.createContract(organisationId, model);
    }

    @Secured({OPS_ADMIN, GLA_ORG_ADMIN, GLA_SPM, GLA_PM})
    @RequestMapping(value = "/organisations/{organisationId}/contracts/{contractId}", method = RequestMethod.PUT)
    public void updateContract(@PathVariable Integer organisationId, @PathVariable Integer contractId, @RequestBody ContractModel model) {
        organisationService.updateContract(organisationId, model);
    }


    @Secured({OPS_ADMIN, GLA_ORG_ADMIN})
    @RequestMapping(value = "/organisations/{organisationId}/status", method = RequestMethod.PUT)
    public void updateStatus(@PathVariable Integer organisationId,
                             @RequestBody UpdateOrganisationStatusRequest request) {
        organisationService.changeStatus(organisationId, request.getStatus(), request.getReason(), request.getDetails(), request.getDuplicateOrgId());
    }

    @Secured({OPS_ADMIN, GLA_ORG_ADMIN, ORG_ADMIN})
    @RequestMapping(value = "/organisations/{id}/users/{username:.+}", method = RequestMethod.DELETE)
    public void removeUser(@PathVariable Integer id, @PathVariable String username) {
        organisationService.removeUserFromOrganisation(id, username);
    }

    @Secured({OPS_ADMIN})
    @RequestMapping(value = "/organisations/{id}", method = RequestMethod.DELETE)
    public void removeOrganisation(@PathVariable Integer id) {
        organisationService.deleteOrganisation(id);
    }

    @Secured({OPS_ADMIN, GLA_ORG_ADMIN, GLA_SPM, GLA_PM, ORG_ADMIN, PROJECT_EDITOR, PROJECT_READER, GLA_FINANCE, GLA_READ_ONLY, TECH_ADMIN})
    @RequestMapping(value = "/organisations/{organisationId}/programmes/{programmeId}", method = RequestMethod.GET)
    public OrganisationProgramme getOrganisationProgramme(@PathVariable Integer organisationId, @PathVariable Integer programmeId) {
        return organisationService.getOrganisationProgramme(organisationId, programmeId);
    }

    @Secured({OPS_ADMIN, GLA_ORG_ADMIN, GLA_SPM, GLA_PM, ORG_ADMIN, PROJECT_EDITOR, PROJECT_READER, GLA_FINANCE, GLA_READ_ONLY})
    @RequestMapping(value = "/organisations/{organisationId}/programmes/{programmeId}/paymentsAndRequests", method = RequestMethod.GET)
    public ProgrammeRequestedAndPaidRecord getPaymentsAndRequests(@PathVariable Integer organisationId, @PathVariable Integer programmeId) {
        return organisationService.getRequestedAndPaidRecord(programmeId, organisationId);
    }

    @Secured({OPS_ADMIN, GLA_ORG_ADMIN, GLA_SPM, GLA_PM})
    @RequestMapping(value = "/organisations/{organisationId}/programmes/{programmeId}/budget", method = RequestMethod.POST)
    public OrganisationBudgetEntry createBudgetEntry(@PathVariable Integer organisationId, @PathVariable Integer programmeId,
                                                     @Valid @RequestBody OrganisationBudgetEntry entry) {
        return organisationService.saveBudgetEntry(organisationId, programmeId, entry);
    }

    @Secured({OPS_ADMIN, GLA_ORG_ADMIN, GLA_SPM, GLA_PM})
    @RequestMapping(value = "/organisations/{organisationId}/programmes/{programmeId}", method = RequestMethod.PUT)
    public void updateOrganisationProgramme(@PathVariable Integer organisationId, @PathVariable Integer programmeId,
                                            @Valid @RequestBody OrganisationProgramme organisationProgramme) {
        organisationService.updateOrganisationProgramme(organisationId, programmeId, organisationProgramme);
    }

    @Secured({OPS_ADMIN, GLA_ORG_ADMIN, GLA_SPM, GLA_PM})
    @RequestMapping(value = "/organisations/{organisationId}/programmes/{programmeId}/tenure/{tenureExtId}/plannedUnits/", method = RequestMethod.PUT)
    public ProgrammeRequestedAndPaidRecord recordPlannedUnits(@PathVariable Integer organisationId, @PathVariable Integer programmeId, @PathVariable Integer tenureExtId,
                                                              @Valid @RequestBody Integer plannedUnits) {
        return organisationService.updatePlannedUnits(organisationId, programmeId, tenureExtId, plannedUnits);
    }

    @Secured({OPS_ADMIN, GLA_ORG_ADMIN, GLA_SPM, GLA_PM})
    @RequestMapping(value = "/organisations/{organisationId}/programmes/{programmeId}/tenure/{tenureExtId}/plannedUnits/", method = RequestMethod.DELETE)
    public ProgrammeRequestedAndPaidRecord deletePlannedUnits(@PathVariable Integer organisationId, @PathVariable Integer programmeId, @PathVariable Integer tenureExtId) {
        return organisationService.deletePlannedUnits(organisationId, programmeId, tenureExtId);
    }

    @Secured({OPS_ADMIN, GLA_ORG_ADMIN, GLA_SPM, GLA_PM})
    @RequestMapping(value = "/organisations/{organisationId}/programmes/{programmeId}/budget/{entryId}", method = RequestMethod.PUT)
    public void updateBudgetEntry(@PathVariable Integer organisationId, @PathVariable Integer programmeId, @PathVariable Integer entryId,
                                  @Valid @RequestBody OrganisationBudgetEntry entry) {
        organisationService.saveBudgetEntry(organisationId, programmeId, entry);
    }

    @Secured({OPS_ADMIN, GLA_ORG_ADMIN, GLA_SPM, GLA_PM})
    @RequestMapping(value = "/organisations/{organisationId}/programmes/{programmeId}/budget/{entryId}", method = RequestMethod.DELETE)
    public void deleteBudgetEntry(@PathVariable Integer organisationId, @PathVariable Integer programmeId, @PathVariable Integer entryId) {
        organisationService.deleteBudgetEntry(entryId);
    }

    @RequestMapping(value = "/organisations/types", method = RequestMethod.GET)
    public Map<Integer, String> getAssignableOrganisationTypes() {
        return organisationService.getAssignableOrganisationTypes();
    }

    @Secured({OPS_ADMIN, GLA_ORG_ADMIN, GLA_SPM, GLA_PM, GLA_READ_ONLY, GLA_FINANCE})
    @RequestMapping(value = "/teams", method = RequestMethod.GET)
    public Set<Team> getTeams() {
        return organisationService.getTeams();
    }


    @Secured({OPS_ADMIN, GLA_ORG_ADMIN})
    @RequestMapping(value = "/organisations/{organisationId}/managedTeams", method = RequestMethod.GET)
    public Set<Team> getTeamsByOrganisation(@PathVariable Integer organisationId) {
        return organisationService.getTeams(organisationId);
    }

    @Secured({OPS_ADMIN, GLA_ORG_ADMIN})
    @RequestMapping(value = "/organisations/{organisationId}/managedTeams", method = RequestMethod.POST)
    public void createTeam(@PathVariable Integer organisationId, @Valid @RequestBody Team team) {
        organisationService.createTeam(organisationId, team);
    }

    @Secured({OPS_ADMIN, GLA_ORG_ADMIN})
    @RequestMapping(value = "/organisations/{organisationId}/managedTeams/{teamId}", method = RequestMethod.PUT)
    public void updateTeam(@PathVariable Integer organisationId, @PathVariable Integer teamId, @Valid @RequestBody Team team) {
        organisationService.updateTeam(organisationId, teamId, team);
    }

    @Secured({OPS_ADMIN, GLA_ORG_ADMIN})
    @RequestMapping(value = "/organisations/{organisationId}/managedTeams/{teamId}", method = RequestMethod.DELETE)
    public void deleteTeam(@PathVariable Integer organisationId, @PathVariable Integer teamId) {
        organisationService.deleteTeam(organisationId, teamId);
    }

    @PreAuthorize("isAuthenticated()")
    @RequestMapping(value = "/organisations/{organisationId}/users", method = RequestMethod.GET)
    public List<UserModel> getUsersForOrganisation(@PathVariable Integer organisationId) {
        return organisationService.getUsersForOrganisation(organisationId);
    }

    @RequestMapping(value = "/organisations/countOccuranceOfUkprn", method = RequestMethod.GET)
    public int countOccuranceOfUkprn(@RequestParam Integer ukprn) {
        return organisationService.countOccuranceOfUkprn(ukprn);
    }
}
