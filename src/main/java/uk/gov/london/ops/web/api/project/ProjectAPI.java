/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.web.api.project;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.london.ops.FeatureStatus;
import uk.gov.london.ops.domain.importdata.ImportErrorLog;
import uk.gov.london.ops.domain.importdata.ImportJobType;
import uk.gov.london.ops.domain.project.*;
import uk.gov.london.ops.domain.user.Role;
import uk.gov.london.ops.exception.ApiError;
import uk.gov.london.ops.exception.ForbiddenAccessException;
import uk.gov.london.ops.exception.ValidationException;
import uk.gov.london.ops.service.ImportLogService;
import uk.gov.london.ops.service.finance.FinanceService;
import uk.gov.london.ops.service.project.state.ProjectState;
import uk.gov.london.ops.service.project.state.StateTransitionResult;
import uk.gov.london.ops.web.model.LockRequestStatus;
import uk.gov.london.ops.web.model.project.*;

import javax.validation.Valid;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;

/**
 * REST API for managing Projects.
 *
 * @author Steve Leach
 */
@RestController
@RequestMapping("/api/v1")
@Api(
        description = "managing Project data"
)
public class ProjectAPI extends BaseProjectAPI {

    Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    FinanceService financeService;

    @Autowired
    FeatureStatus featureStatus;

    @Autowired
    private ImportLogService importLogService;

    @Secured({Role.OPS_ADMIN, Role.GLA_ORG_ADMIN, Role.GLA_SPM, Role.GLA_PM, Role.GLA_FINANCE, Role.GLA_READ_ONLY, Role.ORG_ADMIN, Role.PROJECT_EDITOR, Role.TECH_ADMIN})
    @RequestMapping(value = "/projects", method = RequestMethod.GET, produces = "application/json")
    @ApiOperation(value = "get all project data", notes = "retrieves a list of all projects that the user has access to")
    @Transactional(readOnly = true)
    /**
     * Test API to demonstrate the "fast lane reader" pattern, bypassing JPA for queries
     */
    public List<ProjectSummary> getProjectSummaries(
            @RequestParam(name = "title", required = false) String title,
            @RequestParam(name = "projectId", required = false) String projectIdStr,
            @RequestParam(name = "organisationId", required = false) Integer organisationId,
            @RequestParam(name = "programmeId", required = false) Integer programmeId,
            @RequestParam(name = "programmeName", required = false) String programmeName) {

        Integer projectId = projectIdFromString(projectIdStr != null ? projectIdStr : title);

        return service.getProjectSummaries(title, projectId, organisationId, programmeId, programmeName);
    }

    @Secured({Role.PROJECT_EDITOR, Role.ORG_ADMIN, Role.GLA_PM, Role.GLA_FINANCE, Role.GLA_READ_ONLY, Role.GLA_ORG_ADMIN, Role.GLA_SPM, Role.OPS_ADMIN, Role.TECH_ADMIN})
    @RequestMapping(value = "/projects/page", method = RequestMethod.GET, produces = "application/json")
    @ApiOperation(value = "get all project data", notes = "retrieves a list of all projects that the user has access to")
    @Transactional(readOnly = true)
    public Page<ProjectSummary> getAllPaged(@RequestParam(name = "project", required = false) String project,
                                            @RequestParam(name = "organisationId", required = false) Integer organisationId,
                                            @RequestParam(name = "programmeId", required = false) Integer programmeId,
                                            @RequestParam(name = "programmeName", required = false) String programmeName,
                                            @RequestParam(name = "statuses", required = false) List<Project.Status> statuses,
                                            @RequestParam(name = "subStatuses", required = false) List<Project.SubStatus> subStatuses,
                                            Pageable pageable) {
        return service.findAll(project, organisationId, programmeId, programmeName, statuses, subStatuses, pageable);
    }

    private Integer projectIdFromString(String projectId) {
        if (projectId == null) {
            return null;
        }

        if (projectId.startsWith("P") || projectId.startsWith("p")) {
            projectId = projectId.substring(1);
        }

        return getIdStringAsIntOrNull(projectId);
    }

    private Integer getIdStringAsIntOrNull(String valueToCheck) {
        if (valueToCheck == null) {
            return null;
        }

        if (valueToCheck.length() > 8) {
            return null;
        }

        Integer id;
        try {
            id = Integer.parseInt(valueToCheck);
        } catch (NumberFormatException e) {
            return null;
        }
        return id;

    }

    @Secured({Role.OPS_ADMIN, Role.GLA_ORG_ADMIN, Role.GLA_SPM, Role.GLA_PM, Role.GLA_FINANCE, Role.GLA_READ_ONLY, Role.ORG_ADMIN, Role.PROJECT_EDITOR, Role.TECH_ADMIN})
    @RequestMapping(value = "/projects/{id}", method = RequestMethod.GET)
    public Project get(@PathVariable Integer id,
                       @RequestParam(required = false, defaultValue = "true") boolean unapprovedChanges,
                       @RequestParam(required = false) NamedProjectBlock.BlockStatus compareToStatus,
                       @RequestParam(required = false) String comparisonDate,
                       @RequestParam(required = false, defaultValue = "false") boolean forComparison) {
        return service.getEnrichedProject(id, unapprovedChanges, true, compareToStatus, comparisonDate, forComparison);
    }

    @Secured({Role.OPS_ADMIN, Role.GLA_ORG_ADMIN, Role.GLA_SPM, Role.GLA_PM, Role.GLA_FINANCE, Role.GLA_READ_ONLY, Role.ORG_ADMIN, Role.PROJECT_EDITOR, Role.TECH_ADMIN})
    @RequestMapping(value = "/projects/{legacyProjectCode}/id", method = RequestMethod.GET)
    public Integer lookupProjectIdByLegacyProjectCode(@PathVariable Integer legacyProjectCode) {
        Project project = service.getByLegacyProjectCode(legacyProjectCode);
        return project != null ? project.getId() : null;
    }

    @PreAuthorize("authentication.name == 'test.admin@gla.com'")
    @RequestMapping(value = "/projects/{id}/clone", method = RequestMethod.POST)
    @ApiOperation(value = "clones a project", notes = "")
    public Project clone(@PathVariable Integer id, @RequestParam(name = "title", required = false) String title) {
        return service.cloneProject(id, title);
    }

    @Secured({Role.OPS_ADMIN})
    @RequestMapping(value = "/projects/move/{id}/toProgramme/{progId}/template/{templateId}", method = RequestMethod.PUT)
    @ApiOperation(value = "moves a project from one programmee to another", notes = "")
    public Project moveProject(@PathVariable Integer id, @PathVariable Integer progId, @PathVariable Integer templateId) {
        return service.moveProjectToProgrammeAndTemplate(id, progId, templateId);
    }

    @Secured({Role.OPS_ADMIN, Role.GLA_ORG_ADMIN, Role.GLA_SPM, Role.GLA_PM})
    @RequestMapping(value = "/projects/bulkOperation", method = RequestMethod.PUT)
    @ApiOperation(value = "performs the operation on all projects specifies", notes = "")
    @ApiResponses(@ApiResponse(code = 400, message = "validation error", response = ApiError.class))
    public BulkUpdateResult bulkOperation(@Valid @RequestBody BulkProjectUpdateOperation updateOperation) {

        return service.handleBulkOperation(updateOperation);

    }

    @Secured({Role.OPS_ADMIN, Role.GLA_ORG_ADMIN, Role.GLA_SPM, Role.GLA_PM, Role.ORG_ADMIN, Role.PROJECT_EDITOR})
    @RequestMapping(value = "/projects/{id}/details", method = RequestMethod.PUT)
    @ApiOperation(value = "updates an existing named project detail block", notes = "")
    @ApiResponses(@ApiResponse(code = 400, message = "validation error", response = ApiError.class))
    public ProjectDetailsBlock updateProjectDetails(@PathVariable Integer id, @Valid @RequestBody ProjectDetailsBlock detailsBlock,
                                                    BindingResult bindingResult) {
        verifyBinding("Invalid project!", bindingResult);

        return service.updateProjectDetails(id, detailsBlock);
    }


    @Secured({Role.OPS_ADMIN,Role.GLA_PM})
    @RequestMapping(value = "/projects/{id}/recommendation/{recommendation}", method = RequestMethod.PUT)
    @ApiOperation(value = "updates an existing project", notes = "")
    @ApiResponses(@ApiResponse(code = 400, message = "validation error", response = ApiError.class))
    public Project recordRecommendation(@PathVariable Integer id, @PathVariable Project.Recommendation recommendation, @RequestBody(required = false) String comments) {
        return service.recordRecommendation(id, recommendation, comments);

    }

    @Secured({Role.OPS_ADMIN, Role.GLA_ORG_ADMIN, Role.GLA_SPM, Role.GLA_PM, Role.GLA_FINANCE, Role.GLA_READ_ONLY, Role.ORG_ADMIN, Role.PROJECT_EDITOR, Role.TECH_ADMIN})
    @RequestMapping(value = "/projects/{id}/design", method = RequestMethod.GET)
    @ApiOperation(value = "get a project's design details", notes = "")
    @ApiResponses(@ApiResponse(code = 400, message = "validation error", response = ApiError.class))
    public DesignStandardsBlock getDesignStandards(@PathVariable Integer id) {
        return service.get(id).getDesignStandardsBlock();
    }

    @Secured({Role.OPS_ADMIN, Role.GLA_ORG_ADMIN, Role.GLA_SPM, Role.GLA_PM, Role.ORG_ADMIN, Role.PROJECT_EDITOR})
    @RequestMapping(value = "/projects/{projectId}/design", method = RequestMethod.PUT)
    @ApiOperation(value = "set a project's design standards details", notes = "")
    @ApiResponses(@ApiResponse(code = 400, message = "validation error", response = ApiError.class))
    public DesignStandardsBlock updateDesignStandards(@PathVariable Integer projectId, @Valid @RequestBody DesignStandardsBlock designStandardsBlock, BindingResult bindingResult) {
        verifyBinding("Invalid Design Standards details!", bindingResult);
        return service.updateProjectDesignStandards(projectId, designStandardsBlock);
    }

    @Secured({Role.OPS_ADMIN, Role.GLA_ORG_ADMIN, Role.GLA_SPM, Role.GLA_PM, Role.GLA_FINANCE, Role.GLA_READ_ONLY, Role.ORG_ADMIN, Role.PROJECT_EDITOR, Role.TECH_ADMIN})
    @RequestMapping(value = "/projects/{projectId}/grant", method = RequestMethod.GET)
    @ApiOperation(value = "gets a project's design standards details", notes = "")
    public GrantSourceBlock getGrantSource(@PathVariable Integer projectId) {
        return service.getProjectGrantSource(projectId);
    }

    @Secured({Role.OPS_ADMIN, Role.GLA_ORG_ADMIN, Role.GLA_SPM, Role.GLA_PM, Role.ORG_ADMIN, Role.PROJECT_EDITOR})
    @RequestMapping(value = "/projects/{projectId}/grant", method = RequestMethod.PUT)
    @ApiOperation(value = "set a project's design standards details", notes = "")
    @ApiResponses(@ApiResponse(code = 400, message = "validation error", response = ApiError.class))
    public GrantSourceBlock updateGrantSource(@PathVariable Integer projectId, @Valid @RequestBody GrantSourceBlock grantSourceBlock, BindingResult bindingResult) {
        verifyBinding("Invalid Grant Source Block details!", bindingResult);
        return service.updateProjectGrantSource(projectId, grantSourceBlock);
    }

    @Secured({Role.OPS_ADMIN})
    @RequestMapping(value = "/projects/{id}/deactivate", method = RequestMethod.PUT)
    @ApiOperation(value = "submits a project and optionally update comments")
    @ApiResponses(@ApiResponse(code = 400, message = "validation error", response = ApiError.class))
    public void deactivateTestOnlyFunction(@PathVariable Integer id, @RequestBody(required = false) String comments) {
        Project project = service.get(id);
        service.testOnlyMoveProjectToStatus(project, Project.Status.Assess);
    }

    @Secured({Role.OPS_ADMIN, Role.GLA_ORG_ADMIN, Role.GLA_SPM, Role.GLA_PM, Role.ORG_ADMIN, Role.PROJECT_EDITOR})
    @RequestMapping(value = "/projects/{id}/status", method = RequestMethod.PUT)
    @ApiOperation(value = "updates a project state and optionally update substatus and comments")
    @ApiResponses(@ApiResponse(code = 400, message = "validation error", response = ApiError.class))
    public void updateStatus(@PathVariable Integer id,
                             @RequestParam(defaultValue = "false") boolean validateOnly,
                             @RequestBody UpdateStatusRequest request) {
        Project project = service.getEnrichedProject(id);

        if (validateOnly) {
            service.validateTransitionProjectToStatus(project, new ProjectState(request.getStatus(), request.getSubStatus()));
        }
        else {
            StateTransitionResult result = service.transitionProjectToStatus(project, new ProjectState(request.getStatus(), request.getSubStatus()), request.getComments());
            if (!result.wasSuccessful()) {
                throw new ValidationException(result.getFailureMessage());
            }
        }
    }

    @Secured({Role.OPS_ADMIN, Role.GLA_ORG_ADMIN, Role.GLA_SPM, Role.GLA_PM, Role.ORG_ADMIN, Role.PROJECT_EDITOR})
    @RequestMapping(value = "/projects/{id}/reinstate", method = RequestMethod.PUT)
    @ApiOperation(value = "reinstates a closed project state with comments")
    @ApiResponses(@ApiResponse(code = 400, message = "validation error", response = ApiError.class))
    public void reinstateClosedProject(@PathVariable Integer id,
                             @RequestBody String comments) {
        Project project = service.getEnrichedProject(id);
        StateTransitionResult result = service.reinstateProject(project, comments);
        if (!result.wasSuccessful()) {
            throw new ValidationException(result.getFailureMessage());
        }
    }

    @Secured({Role.OPS_ADMIN, Role.GLA_ORG_ADMIN, Role.GLA_SPM, Role.GLA_PM, Role.ORG_ADMIN, Role.PROJECT_EDITOR})
    @RequestMapping(value = "/projects/{id}/draftcomment", method = RequestMethod.PUT)
    @ApiOperation(value = "saves a project's comments as draft")
    @ApiResponses(@ApiResponse(code = 400, message = "validation error", response = ApiError.class))
    public void saveDraftComments(@PathVariable Integer id, @RequestBody(required = false) String comments) {
        service.saveDraftComments(id, comments);
    }

    @Secured({Role.OPS_ADMIN, Role.GLA_ORG_ADMIN, Role.GLA_SPM, Role.GLA_PM, Role.GLA_FINANCE, Role.GLA_READ_ONLY, Role.ORG_ADMIN, Role.PROJECT_EDITOR, Role.TECH_ADMIN})
    @RequestMapping(value = "/projects/{id}/history", method = RequestMethod.GET)
    @ApiOperation(value = "get a project's history", notes = "")
    @ApiResponses(@ApiResponse(code = 400, message = "validation error", response = ApiError.class))
    public List<ProjectHistory> getProjectHistory(@PathVariable Integer id) {
        return service.getProjectHistory(id);
    }

    @PreAuthorize("authentication.name == 'test.admin@gla.com'")
    @RequestMapping(value = "/projects/{id}", method = RequestMethod.DELETE)
    public String delete(@PathVariable Integer id) {
        service.deleteProject(id);
        return "Deleted project " + id;
    }

    @PreAuthorize("authentication.name == 'test.admin@gla.com'")
    @RequestMapping(value = "/projects/{id}/removeLocks", method = RequestMethod.DELETE)
    public String deleteAllLocksOnProject(@PathVariable Integer id) {
        Project project = service.get(id);
        for (NamedProjectBlock namedProjectBlock : project.getProjectBlocks()) {
            namedProjectBlock.setLockDetails(null);
        }
        service.updateProject(project);
        return "Locks cleared";
    }

    @Secured({Role.OPS_ADMIN, Role.GLA_ORG_ADMIN, Role.GLA_SPM, Role.GLA_PM, Role.GLA_FINANCE, Role.GLA_READ_ONLY, Role.ORG_ADMIN, Role.PROJECT_EDITOR})
    @RequestMapping(value = "/projects/{id}/lock/{blockId}", method = RequestMethod.GET)
    @ApiOperation(value = "lock a project block", notes = "")
    @ApiResponses(@ApiResponse(code = 400, message = "validation error", response = ApiError.class))
    public LockRequestStatus lockProjectBlock(@PathVariable Integer id, @PathVariable Integer blockId) {
        Project fromDB = service.get(id);
        NamedProjectBlock block = fromDB.getProjectBlockById(blockId);

        if (block == null) {
            throw new ValidationException(String.format("Unable to locate block with id: %d on project: %d", blockId, id));
        }

        return service.tryLock(fromDB, block);
    }


    @Secured({Role.OPS_ADMIN, Role.GLA_ORG_ADMIN, Role.GLA_SPM, Role.GLA_PM, Role.ORG_ADMIN, Role.PROJECT_EDITOR})
    @RequestMapping(value = "/projects/{id}/unlock/{blockId}", method = RequestMethod.PUT)
    @ApiOperation(value = "simple unlock on a project block", notes = "")
    @ApiResponses(@ApiResponse(code = 400, message = "validation error", response = ApiError.class))
    public void unlockProjectBlock(@PathVariable Integer id, @PathVariable Integer blockId) {
        Project fromDB = service.get(id);
        NamedProjectBlock block = fromDB.getProjectBlockById(blockId);

        if (block == null) {
            throw new ValidationException(String.format("Unable to locate block with id: %d on project: %d", blockId, id));
        }

        service.deleteLock(block);
        service.updateProject(fromDB);
    }

    @Secured({Role.OPS_ADMIN, Role.GLA_ORG_ADMIN, Role.GLA_SPM, Role.GLA_PM, Role.GLA_FINANCE, Role.GLA_READ_ONLY, Role.ORG_ADMIN, Role.PROJECT_EDITOR, Role.TECH_ADMIN})
    @RequestMapping(value = "/projects/{id}/{blockId}", method = RequestMethod.GET)
    @ApiOperation(value = "get a project block by ID", notes = "")
    @ApiResponses(@ApiResponse(code = 400, message = "validation error", response = ApiError.class))
    public NamedProjectBlock getProjectBlock(@PathVariable Integer id,
                                             @PathVariable Integer blockId,
                                             @RequestParam(name = "tryLock", required = false) boolean lock) {
        Project project = service.getEnrichedProject(id);
        NamedProjectBlock block = project.getProjectBlockById(blockId);

        if (block == null) {
            throw new ValidationException(String.format("Unable to locate block with id: %d on project: %d", blockId, id));
        }

        if (lock && block.isEditable()) {
            block = service.getBlockAndLock(project, block);
        }

        return block;
    }

    @Secured({Role.OPS_ADMIN, Role.GLA_ORG_ADMIN, Role.GLA_SPM, Role.GLA_PM, Role.GLA_FINANCE, Role.GLA_READ_ONLY, Role.ORG_ADMIN, Role.PROJECT_EDITOR, Role.TECH_ADMIN})
    @RequestMapping(value = "/projects/{id}/{blockId}/history", method = RequestMethod.GET)
    @ApiOperation(value = "get a project block history", notes = "")
    @ApiResponses(@ApiResponse(code = 400, message = "validation error", response = ApiError.class))
    public List<ProjectBlockHistoryItem> getProjectBlockHistory(@PathVariable Integer id,
                                                                @PathVariable Integer blockId) {
        Project fromDB = service.get(id);
        if (!Project.Status.Active.equals(fromDB.getStatus())) {
            return Collections.emptyList();
        }
        return service.getHistoryForBlock(fromDB, blockId);

    }

    @Secured(Role.OPS_ADMIN)
    @RequestMapping(value = "/projects/pcsImport", method = RequestMethod.POST)
    @ApiOperation(value = "Internal API updloading the PCS csv file", hidden = true)
    public FileImportResult importPcsProjectFile(MultipartFile file) throws IOException {
        return service.importPcsProjectFile(file.getInputStream());
    }

    @Secured(Role.OPS_ADMIN)
    @RequestMapping(value = "/projects/imsImport", method = RequestMethod.POST)
    @ApiOperation(value = "Internal API uploading the IMS csv file", hidden = true)
    public FileImportResult importImsProjectFile(MultipartFile file) throws IOException {
        if (featureStatus.isEnabled(FeatureStatus.Feature.ImsImport)) {
            FileImportResult fileImportResult = new FileImportResult();
            try {
                fileImportResult = service.importImsProjectFile(file.getInputStream());
            } catch (IOException e) {
                log.error("Error during import" , e );
            } finally {
                List<ImportErrorLog> errors = importLogService.findAllErrorsByImportType(ImportJobType.IMS_PROJECT_IMPORT);
                fileImportResult.setErrors(errors);
                return fileImportResult;
            }
        } else {
            throw new ForbiddenAccessException("This feature is currently disabled.");
        }
    }

    @Secured(Role.OPS_ADMIN)
    @RequestMapping(value = "/projects/imsUnitDetailsImport", method = RequestMethod.POST)
    @ApiOperation(value = "Internal API uploading the IMS unit details file", hidden = true)
    public FileImportResult importImsUnitDetailsFile(MultipartFile file) throws IOException {
        if (featureStatus.isEnabled(FeatureStatus.Feature.ImsImport)) {
            FileImportResult fileImportResult = new FileImportResult();
            try {
                fileImportResult =service.importImsUnitDetailsFile(file.getInputStream());
            } catch (IOException e) {
                log.error("Error during import" , e);
            } finally {
                List<ImportErrorLog> errors = importLogService.findAllErrorsByImportType(ImportJobType.IMS_UNIT_DETAILS_IMPORT);
                fileImportResult.setErrors(errors);
                return fileImportResult;
            }
        } else {
            throw new ForbiddenAccessException("This feature is currently disabled.");
        }
    }

    @Secured(Role.OPS_ADMIN)
    @RequestMapping(value = "/projects/imsClaimedUnitImport", method = RequestMethod.POST)
    @ApiOperation(value = "Internal API uploading the IMS claimed units file", hidden = true)
    public FileImportResult importImsClaimedUnitsFile(MultipartFile file) {
        if (featureStatus.isEnabled(FeatureStatus.Feature.ImsImport)) {
            FileImportResult fileImportResult = new FileImportResult();
            try {
                fileImportResult = service.importImsClaimedUnitsFile(file.getInputStream());
            } catch (IOException e) {
                log.error("Error during import" , e);
            } finally {
                List<ImportErrorLog> errors = importLogService.findAllErrorsByImportType(ImportJobType.IMS_CLAIMED_UNITS_IMPORT);
                fileImportResult.setErrors(errors);
                return fileImportResult;
            }
        } else {
            throw new ForbiddenAccessException("This feature is currently disabled.");
        }
    }

    @Secured({Role.OPS_ADMIN, Role.GLA_ORG_ADMIN, Role.GLA_SPM, Role.GLA_PM, Role.ORG_ADMIN, Role.PROJECT_EDITOR})
    @RequestMapping(value = "/projects", method = RequestMethod.POST)
    @ApiOperation(value = "creates a project", notes = "")
    @ApiResponses(@ApiResponse(code = 400, message = "validation error", response = ApiError.class))
    public Integer create(@Valid @RequestBody Project project, BindingResult bindingResult) {
        verifyBinding("Invalid project!", bindingResult);

        return service.createProject(project).getId();
    }

    @Secured({Role.OPS_ADMIN, Role.ORG_ADMIN, Role.PROJECT_EDITOR})
    @RequestMapping(value = "/projects/{projectId}/blocks/{blockId}", method = RequestMethod.DELETE)
    public void deleteUnapprovedBlock(@PathVariable Integer projectId, @PathVariable Integer blockId) {
        service.deleteUnapprovedBlock(projectId, blockId);
    }

    @PreAuthorize("authentication.name == 'test.admin@gla.com'")
    @RequestMapping(value = "/projects/{projectId}/blocks/{blockId}/lastModified", method = RequestMethod.PUT)
    public void updateProjectBlockLastModified(@PathVariable Integer projectId, @PathVariable Integer blockId, @RequestBody String day) {
        service.updateProjectBlockLastModified(projectId, blockId, OffsetDateTime.parse(day+"T00:00:00+00:00"));
    }

    @Secured(Role.OPS_ADMIN)
    @RequestMapping(value = "/projects/{projectId}/organisation", method = RequestMethod.PUT)
    @ApiOperation(value = "transfer a project to another organisation", notes = "transfer a project to another organisation")
    public void transfer(@PathVariable Integer projectId, @RequestBody Integer organisationId) {
        service.transfer(projectId, organisationId);
    }

    @Secured(Role.OPS_ADMIN)
    @RequestMapping(value = "/projects/moveAnswerValueToGrantSource", method = RequestMethod.POST)
    public void moveAnswerValueToGrantSource(@RequestParam Integer questionId) {
        service.moveAnswerValueToGrantSource(questionId);
    }

}
