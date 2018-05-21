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
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;
import uk.gov.london.ops.FeatureStatus;
import uk.gov.london.ops.domain.EntityCount;
import uk.gov.london.ops.domain.project.Project;
import uk.gov.london.ops.domain.project.ProjectDetailsBlock;
import uk.gov.london.ops.domain.template.Programme;
import uk.gov.london.ops.domain.template.ProgrammeSummary;
import uk.gov.london.ops.domain.user.Role;
import uk.gov.london.ops.exception.ForbiddenAccessException;
import uk.gov.london.ops.exception.ValidationException;
import uk.gov.london.ops.service.ProgrammeService;
import uk.gov.london.ops.service.SdeService;
import uk.gov.london.ops.service.project.ProjectService;
import uk.gov.london.ops.spe.SimpleProjectExportConstants;
import uk.gov.london.ops.util.CSVFile;
import uk.gov.london.ops.util.ExporterUtils;
import uk.gov.london.ops.util.GlaOpsUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.OutputStreamWriter;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * REST API for programme data.
 *
 * @author Steve Leach
 */
@RestController
@RequestMapping("/api/v1")
@Api(description = "managing Programme data")
public class ProgrammeAPI {

    private final ProgrammeService service;
    private final FeatureStatus featureStatus;
    private final ProjectService projectService;
    private final SdeService sdeService;

    ProgrammeAPI(final ProgrammeService service,
                 final ProjectService projectService,
                 final SdeService sdeService,
                 final FeatureStatus featureStatus) {
        this.service = service;
        this.projectService = projectService;
        this.sdeService = sdeService;
        this.featureStatus = featureStatus;
    }

    @Secured({Role.OPS_ADMIN, Role.GLA_ORG_ADMIN, Role.GLA_SPM, Role.GLA_PM, Role.GLA_FINANCE, Role.GLA_READ_ONLY, Role.ORG_ADMIN, Role.PROJECT_EDITOR, Role.TECH_ADMIN})
    @RequestMapping(value = "/programmes", method = RequestMethod.GET)
    @ApiOperation(value="get all programme data", notes="retrieves a list of all programmes")
    public List<ProgrammeSummary> getAll(@RequestParam(name = "enabled", required = false) boolean enabled) {
        if (featureStatus.isEnabled(FeatureStatus.Feature.ManagingOrgFilter)) {
            return service.getSummaries(enabled);
        }
        else {
            return service.findAllEnabled(enabled);
        }
    }

    @Secured({Role.OPS_ADMIN, Role.GLA_ORG_ADMIN, Role.GLA_SPM, Role.GLA_PM, Role.GLA_FINANCE, Role.GLA_READ_ONLY, Role.TECH_ADMIN})
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

    @Secured({Role.OPS_ADMIN, Role.GLA_ORG_ADMIN, Role.GLA_SPM, Role.GLA_PM, Role.GLA_FINANCE, Role.GLA_READ_ONLY, Role.TECH_ADMIN})
    @GetMapping(value = "/programmes/{id}/status")
    public Collection<String> getProgrammeStatuses(@PathVariable Integer id) {
        final Collection<Project> projects = projectService.getProjectbyProgrammeId(id);
        return projects != null
                ? projects.stream()
                .filter(GlaOpsUtils::notNull)
                .map(Project::getStatus)
                .map(Project.Status::name)
                .sorted()//Sorts it with natural order: Alphabetic
                .collect(Collectors.toSet())
                : Collections.emptyList();
    }


    @Secured({Role.OPS_ADMIN, Role.GLA_ORG_ADMIN, Role.GLA_SPM, Role.GLA_PM, Role.GLA_FINANCE, Role.GLA_READ_ONLY, Role.TECH_ADMIN})
    @RequestMapping(value = "/programmes/{id}", method = RequestMethod.GET)
    public Programme get(@PathVariable Integer id) {
        return service.getById(id);
    }

    @Secured({Role.OPS_ADMIN})
    @RequestMapping(value = "/programmes/{id}/projectCountPerTemplate", method = RequestMethod.GET)
    public Set<EntityCount> getProjectCountPerTemplate(@PathVariable Integer id) {
        return service.getProjectCountPerTemplateForProgramme(id);
    }


    /**
     * Provides a list of items, where each of them represents a project
     * containing the information of all the blocks within the projects.
     *
     * @param response HttpResponse to attach the file.
     * @param id programme Id
     * @throws Exception
     */
    @Secured({Role.OPS_ADMIN, Role.GLA_ORG_ADMIN, Role.GLA_SPM, Role.GLA_PM, Role.GLA_FINANCE, Role.GLA_READ_ONLY, Role.TECH_ADMIN})
    @GetMapping("/programmes/{id}/csvexport")
    @ApiOperation(
            value="file download",
            notes="Endpoint for downloading an file")
    public void downloadCsvExport(HttpServletResponse response ,
                                  @PathVariable Integer id) throws Exception {

        if (featureStatus.isEnabled(FeatureStatus.Feature.OutputCSV)) {

            //Retrieving data and generating headers from it
            final List<Map<String, Object>> projectsAsMap = sdeService
                    .simpleDataExtract(id);
            final Set<String> finalHeaders = headersFromDataMap(projectsAsMap);

            //Prepare Response to return csv file
            final String dateS = OffsetDateTime.now()
                    .format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
            final String fileN= String.format("%d_projects_%s.csv", id, dateS);
            ExporterUtils.csvResponse(response, fileN);

            try (OutputStreamWriter out =
                         new OutputStreamWriter(response.getOutputStream())) {
                CSVFile csvFile = new CSVFile(finalHeaders, out);
                for (Map<String, Object> projectAsMap: projectsAsMap) {
                    csvFile.writeValues(projectAsMap);
                }
            }

            response.flushBuffer();
        } else {
            throw new ForbiddenAccessException(
                    "This feature is currently disabled.");
        }
    }


    /**
     * Method to ensure some fields(project_id, project_name, etc.) are
     * first and then the rest of the headers in alphabetic order.
     * @return Sorted Set starting with the fields listed.
     */
    private Set<String> headersFromDataMap(
            final List<Map<String, Object>> projectsAsMap) {
        final Set<String> finalHeaderSet = new LinkedHashSet<>();
        finalHeaderSet.add(
                SimpleProjectExportConstants.FieldNames.project_id.name());
        finalHeaderSet.add(
                SimpleProjectExportConstants.FieldNames.project_name.name());
        finalHeaderSet.add(
                SimpleProjectExportConstants.FieldNames.programme_id.name());
        finalHeaderSet.add(
                SimpleProjectExportConstants.FieldNames.programme_name.name());
        finalHeaderSet.add(
                SimpleProjectExportConstants.FieldNames.org_id.name());
        finalHeaderSet.add(
                SimpleProjectExportConstants.FieldNames.org_name.name());
        finalHeaderSet.add(
                SimpleProjectExportConstants.FieldNames.template_id.name());
        finalHeaderSet.add(
                SimpleProjectExportConstants.FieldNames.template_name.name());
        //Tree set to provides alphabetic order
        final Set<String> tempHeaders = new TreeSet<>(
                projectsAsMap
                        .stream()
                        .map(Map::keySet)
                        .flatMap(Collection::stream)
                        .collect(Collectors.toList()));


        finalHeaderSet.addAll(tempHeaders);
        return finalHeaderSet;
    }


    @Secured(Role.OPS_ADMIN)
    @RequestMapping(value = "/programmes", method = RequestMethod.POST)
    @ApiOperation(
            value="create a new programme",
            notes="creates a new programme and assigns it an ID")
    public Programme create(final @Valid @RequestBody Programme programme,
                            final HttpServletRequest request) {
        if (programme.getId() != null) {
            throw new ValidationException(
                    "id",
                    "New programmes must not have an ID");
        }
        return service.create(programme);
    }

    @Secured(Role.OPS_ADMIN)
    @RequestMapping(value = "/programmes/{id}", method = RequestMethod.PUT)
    @ApiOperation(
            value="update an existing programme",
            notes="updates an existing programme by ID")
    public Programme update(final @PathVariable Integer id, final @RequestBody Programme programme) {

        return service.update(id, programme);
    }

    @Secured(Role.OPS_ADMIN)
    @RequestMapping(value = "/programmes/{id}/managingOrg/{orgId}", method = RequestMethod.PUT)
    @ApiOperation(
            value="update an existing programme's managing org",
            notes="updates an existing programme managing org ID")
    public Programme updateManagingOrg(final @PathVariable Integer id, final @PathVariable Integer orgId) {
        if (featureStatus.isEnabled(FeatureStatus.Feature.ManagingOrgFilter)) {
            // can't update managing org for programme if the filter is currently enabled
            throw new ForbiddenAccessException();
        }
        return service.updateManagingOrg(id, orgId);

    }

    @Secured(Role.OPS_ADMIN)
    @RequestMapping(
            value = "/programmes/{id}/enabled",
            method = RequestMethod.PUT)
    @ApiOperation(
            value="updates a programme's enabled value",
            notes="updates a programme's enabled value")
    public void updateEnabled(final @PathVariable Integer id,
                              final @RequestBody String enabled) {
        service.updateEnabled(id, Boolean.parseBoolean(enabled));
    }

    @Secured(Role.OPS_ADMIN)
    @RequestMapping(value = "/programmes/{id}", method = RequestMethod.DELETE)
    public String delete(@PathVariable Integer id) {
        service.deleteProgramme(id);
        return "Deleted programme " + id;
    }

    @Secured(Role.OPS_ADMIN)
    @RequestMapping(value = "/programmes/{id}/supportedReports",method = RequestMethod.PUT)
    @ApiOperation(value="updates a programme's supported reports list", notes="updates a programme's supported reports list")
    public void updateSupportedReports(@PathVariable Integer id, @RequestBody List<String> supportedReports) {
        service.updateSupportedReports(id, supportedReports);
    }

}
