/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.programme;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import uk.gov.london.common.GlaUtils;
import uk.gov.london.ops.domain.EntityCount;
import uk.gov.london.ops.framework.annotations.PermissionRequired;
import uk.gov.london.ops.programme.domain.Programme;
import uk.gov.london.ops.programme.domain.ProgrammePublicProfileSummary;
import uk.gov.london.ops.programme.domain.ProgrammeSummary;
import uk.gov.london.ops.project.Project;
import uk.gov.london.ops.project.ProjectService;
import uk.gov.london.ops.project.accesscontrol.DefaultAccessControlSummary;
import uk.gov.london.ops.project.block.ProjectDetailsBlock;

import javax.validation.Valid;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static uk.gov.london.common.user.BaseRole.*;
import static uk.gov.london.ops.permission.PermissionType.GIVE_ACCESS_TO_ORG_VIA_TEMPLATE;
import static uk.gov.london.ops.permission.PermissionType.PROG_MANAGE;

/**
 * REST API for programme data.
 *
 * @author Steve Leach
 */
@RestController
@RequestMapping("/api/v1")
@Api("managing Programme data")
public class ProgrammeAPI {

    private final ProgrammeServiceImpl service;
    private final ProjectService projectService;

    ProgrammeAPI(ProgrammeServiceImpl service, ProjectService projectService) {
        this.service = service;
        this.projectService = projectService;
    }

    @PreAuthorize("isAuthenticated()")
    @RequestMapping(value = "/programmes", method = RequestMethod.GET)
    @ApiOperation(value = "get a list of filtered programme data", notes =
            "retrieves a list of all programmes, unless a filtered list is required specified by existence of searchText param")
    public Page<ProgrammeSummary> getAllPaged(@RequestParam(name = "enabled", required = false) boolean enabled,
            @RequestParam(name = "statuses", required = false) List<Programme.Status> statuses,
            @RequestParam(name = "searchText", required = false) String programmeText,
            @RequestParam(name = "managingOrganisations", required = false) List<Integer> managingOrganisations,
            Pageable pageable) {
        return service.getAllPaged(enabled, statuses, programmeText, managingOrganisations, pageable);
    }

    @RequestMapping(value = "/programmes/public-profiles", method = RequestMethod.GET)
    @ApiOperation(value = "get a list of public programmes", notes = "retrieves a list of all public programmes")
    public List<ProgrammePublicProfileSummary> getPublicProgrammes() {
        return service.getPublicProgrammes();
    }

    @Secured({OPS_ADMIN, GLA_ORG_ADMIN, GLA_SPM, GLA_PM, GLA_FINANCE, GLA_PROGRAMME_ADMIN, GLA_READ_ONLY,
            ORG_ADMIN, PROJECT_EDITOR, PROJECT_READER, TECH_ADMIN, INTERNAL_BLOCK_EDITOR})
    @RequestMapping(value = "/filters/programmes", method = RequestMethod.GET)
    @ApiOperation(value = "get a list of values available for the current user to use in a programmes filter",
            notes = "get a list of values available for the current user to use in a programmes filter")
    public Collection<ProgrammeFilterItem> getProgrammesFilters() {
        return service.getProgrammesFilters();
    }

    @Secured({OPS_ADMIN, GLA_ORG_ADMIN, GLA_SPM, GLA_PM, GLA_FINANCE, GLA_PROGRAMME_ADMIN, GLA_READ_ONLY, ORG_ADMIN,
            PROJECT_EDITOR, PROJECT_READER, TECH_ADMIN})
    @GetMapping(value = "/programmes/{id}/borough")
    public Collection<String> getProgrammeBoroughs(@PathVariable Integer id) {
        final Collection<Project> projects = projectService.getProjectbyProgrammeId(id);
        return projects != null
                ? projects.stream()
                .filter(p -> p.getDetailsBlock() != null && p.getDetailsBlock().getBorough() != null)
                .map(Project::getDetailsBlock)
                .map(ProjectDetailsBlock::getBorough)
                .sorted()//Sorts it with natural order: Alphabetic
                .collect(Collectors.toSet())
                : Collections.emptyList();
    }

    @Secured({OPS_ADMIN, GLA_ORG_ADMIN, GLA_SPM, GLA_PM, GLA_FINANCE, GLA_PROGRAMME_ADMIN, GLA_READ_ONLY, ORG_ADMIN,
            PROJECT_EDITOR, PROJECT_READER, TECH_ADMIN})
    @GetMapping(value = "/programmes/{id}/status")
    public Collection<String> getProgrammeStatuses(@PathVariable Integer id) {
        final Collection<Project> projects = projectService.getProjectbyProgrammeId(id);
        return projects != null
                ? projects.stream()
                .filter(GlaUtils::notNull)
                .map(Project::getStatusName)
                .sorted()//Sorts it with natural order: Alphabetic
                .collect(Collectors.toSet())
                : Collections.emptyList();
    }

    @Secured({OPS_ADMIN, GLA_ORG_ADMIN, GLA_SPM, GLA_PM, GLA_REGISTRATION_APPROVER, GLA_PROGRAMME_ADMIN, GLA_FINANCE,
            GLA_READ_ONLY, TECH_ADMIN})
    @RequestMapping(value = "/programmes/{id}", method = RequestMethod.GET)
    public Programme get(@PathVariable Integer id, @RequestParam(required = false) boolean enrich) {
        return service.getById(id, enrich);
    }

    @PermissionRequired(PROG_MANAGE)
    @RequestMapping(value = "/programmes/{id}/projectCountPerTemplate", method = RequestMethod.GET)
    public Set<EntityCount> getProjectCountPerTemplate(@PathVariable Integer id) {
        return service.getProjectCountPerTemplateForProgramme(id);
    }

    @Secured({OPS_ADMIN, GLA_PROGRAMME_ADMIN})
    @RequestMapping(value = "/programmes", method = RequestMethod.POST)
    @ApiOperation(value = "create a new programme", notes = "creates a new programme and assigns it an ID")
    public Programme create(@Valid @RequestBody Programme programme) {
        return service.create(programme);
    }

    @Secured({OPS_ADMIN, GLA_REGISTRATION_APPROVER, GLA_PROGRAMME_ADMIN})
    @RequestMapping(value = "/programmes/{id}", method = RequestMethod.PUT)
    @ApiOperation(
            value = "update an existing programme",
            notes = "updates an existing programme by ID")
    public Programme update(final @PathVariable Integer id, final @RequestBody Programme programme) {
        return service.update(id, programme);
    }

    @Secured({OPS_ADMIN, GLA_PROGRAMME_ADMIN})
    @RequestMapping(
            value = "/programmes/{id}/enabled",
            method = RequestMethod.PUT)
    @ApiOperation(
            value = "updates a programme's enabled value",
            notes = "updates a programme's enabled value")
    public void updateEnabled(final @PathVariable Integer id,
            final @RequestBody String enabled) {
        service.updateEnabled(id, Boolean.parseBoolean(enabled));
    }

    @Secured({OPS_ADMIN, GLA_PROGRAMME_ADMIN})
    @RequestMapping(value = "/programmes/{id}", method = RequestMethod.DELETE)
    public String delete(@PathVariable Integer id) {
        service.deleteProgramme(id);
        return "Deleted programme " + id;
    }

    @Secured(OPS_ADMIN)
    @RequestMapping(value = "/programmes/{id}/supportedReports", method = RequestMethod.PUT)
    @ApiOperation(value = "updates a programme's supported reports list", notes = "updates a programme's supported reports list")
    public void updateSupportedReports(@PathVariable Integer id, @RequestBody List<String> supportedReports) {
        service.updateSupportedReports(id, supportedReports);
    }

    @PermissionRequired(GIVE_ACCESS_TO_ORG_VIA_TEMPLATE)
    @RequestMapping(value = "/programmes/{id}/template/{templateId}/organisationAccess", method = RequestMethod.PUT)
    public void grantOrganisationAccess(@PathVariable Integer id, @PathVariable Integer templateId,
            @RequestParam(name = "organisationId") Integer organisationId) {
        service.grantOrganisationAccess(id, templateId, organisationId);
    }

    @PermissionRequired(GIVE_ACCESS_TO_ORG_VIA_TEMPLATE)
    @RequestMapping(value = "/programmes/{id}/template/{templateId}/organisationAccess", method = RequestMethod.DELETE)
    public void revokeOrganisationAccess(@PathVariable Integer id, @PathVariable Integer templateId,
            @RequestParam(name = "organisationId") Integer organisationId) {
        service.removeOrganisationAccess(id, templateId, organisationId);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping(value = "/programmes/{id}/defaultOrganisationAccess")
    public List<DefaultAccessControlSummary> getAllDefaultOrganisationAccess(@PathVariable Integer id) {
        return service.getDefaultAccess(id);
    }

    @PreAuthorize("authentication.name == 'system.scheduler' or hasRole('OPS_ADMIN')")
    @RequestMapping(value = "/runProgrammeScheduler/onDate", method = RequestMethod.PUT)
    public void runProgrammeScheduler(@RequestParam(name = "datetime") String datetime) {
        DateTimeFormatter dtf = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
        OffsetDateTime targetDatetime = OffsetDateTime.parse(datetime, dtf);
        service.scheduleProgammeEnabled(targetDatetime);
    }

    @PreAuthorize("authentication.name == 'system.scheduler' or hasRole('OPS_ADMIN')")
    @RequestMapping(value = "/programmes/{id}/openCloseDates", method = RequestMethod.PUT)
    public void setProgrammeScheduledDates(@PathVariable Integer id,
                                           @RequestParam(name = "open", required = false) String openDatestring,
                                           @RequestParam(name = "close", required = false) String closeDatestring) {
        DateTimeFormatter dtf = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
        OffsetDateTime openDatetime = openDatestring != null ? OffsetDateTime.parse(openDatestring, dtf) : null;
        OffsetDateTime closeDatetime = closeDatestring != null ? OffsetDateTime.parse(closeDatestring, dtf) : null;
        service.setProgrammeOpenCloseDates(id, openDatetime, closeDatetime);
    }
}
