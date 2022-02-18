/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.project;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import uk.gov.london.common.error.ApiError;
import uk.gov.london.ops.framework.annotations.PermissionRequired;
import uk.gov.london.ops.framework.environment.Environment;
import uk.gov.london.ops.framework.exception.ForbiddenAccessException;
import uk.gov.london.ops.framework.exception.ValidationException;
import uk.gov.london.ops.framework.feature.Feature;
import uk.gov.london.ops.framework.feature.FeatureStatus;
import uk.gov.london.ops.project.accesscontrol.GrantAccessTrigger;
import uk.gov.london.ops.project.block.*;
import uk.gov.london.ops.project.claim.Claim;
import uk.gov.london.ops.project.claim.ClaimService;
import uk.gov.london.ops.project.grant.GrantSourceBlock;
import uk.gov.london.ops.project.internalblock.InternalProjectBlock;
import uk.gov.london.ops.project.label.Label;
import uk.gov.london.ops.project.state.ProjectState;
import uk.gov.london.ops.project.state.ProjectStateServiceImpl;
import uk.gov.london.ops.project.state.ProjectStatus;
import uk.gov.london.ops.project.state.StateTransitionResult;
import uk.gov.london.ops.user.UserIdAndName;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static uk.gov.london.common.user.BaseRole.*;
import static uk.gov.london.ops.framework.OPSUtils.verifyBinding;
import static uk.gov.london.ops.permission.PermissionType.PROJ_ASSIGN;
import static uk.gov.london.ops.permission.PermissionType.VIEW_PROGRAMME_ALLOCATIONS;

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
public class ProjectAPI {

    Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    private ProjectService service;

    @Autowired
    private ProjectBlockService projectBlockService;

    @Autowired
    FeatureStatus featureStatus;

    @Autowired
    ProjectStateServiceImpl projectStateService;

    @Autowired
    ClaimService claimService;

    @Autowired
    protected Environment environment;

    @Autowired
    protected ProjectNotificationScheduler projectNotificationScheduler;

    @Secured({PROJECT_EDITOR, PROJECT_READER, ORG_ADMIN, GLA_PM, GLA_FINANCE, GLA_PROGRAMME_ADMIN, GLA_READ_ONLY, GLA_ORG_ADMIN,
            GLA_SPM, OPS_ADMIN, TECH_ADMIN})
    @RequestMapping(value = "/projects", method = GET, produces = "application/json")
    @ApiOperation(value = "get all project data", notes = "retrieves a list of all projects that the user has access to")
    @Transactional(readOnly = true)
    public Page<ProjectSummary> getAllProjects(@RequestParam(name = "project", required = false) String project,
                                               @RequestParam(name = "organisation", required = false) String organisation,
                                               @RequestParam(name = "programme", required = false) String programme,
                                               @RequestParam(name = "assignee", required = false) String assignee,
                                               @RequestParam(name = "programmes", required = false) List<Integer> programmes,
                                               @RequestParam(name = "templates", required = false) List<Integer> templates,
                                               @RequestParam(name = "states", required = false) List<String> states,
                                               @RequestParam(name = "watchingProject", required = false, defaultValue = "false")
                                                       boolean watchingProject,
                                               Pageable pageable) {
        return service.findAll(project, organisation, programme, assignee, programmes, templates, states, watchingProject,
                false, pageable);
    }

    @PermissionRequired(VIEW_PROGRAMME_ALLOCATIONS)
    @RequestMapping(value = "/programmeAllocations", method = GET, produces = "application/json")
    @ApiOperation(value = "get all project data", notes = "retrieves a list of all projects that the user has access to")
    @Transactional(readOnly = true)
    public Page<ProjectSummary> getAllProgrammeAllocations(@RequestParam(name = "project", required = false) String project,
                                               @RequestParam(name = "organisation", required = false) String organisation,
                                               @RequestParam(name = "programme", required = false) String programme,
                                               @RequestParam(name = "assignee", required = false) String assignee,
                                               @RequestParam(name = "programmes", required = false) List<Integer> programmes,
                                               @RequestParam(name = "templates", required = false) List<Integer> templates,
                                               @RequestParam(name = "states", required = false) List<String> states,
                                               @RequestParam(name = "watchingProject", required = false, defaultValue = "false")
                                                       boolean watchingProject,
                                               Pageable pageable) {
        return service.findAll(project, organisation, programme, assignee, programmes, templates, states, watchingProject,
                true, pageable);
    }

    @Secured({OPS_ADMIN, GLA_ORG_ADMIN, GLA_SPM, GLA_PM, GLA_PROGRAMME_ADMIN, GLA_FINANCE, GLA_READ_ONLY, ORG_ADMIN,
            PROJECT_EDITOR, PROJECT_READER, TECH_ADMIN})
    @RequestMapping(value = "/projects/{id}", method = GET)
    public Project get(@PathVariable Integer id,
                       @RequestParam(required = false, defaultValue = "true") boolean unapprovedChanges,
                       @RequestParam(required = false) ProjectBlockStatus compareToStatus,
                       @RequestParam(required = false) String comparisonDate,
                       @RequestParam(required = false, defaultValue = "false") boolean forComparison) {
        return service.getEnrichedProject(id, unapprovedChanges, true, compareToStatus, comparisonDate, forComparison);
    }

    @Secured({OPS_ADMIN, GLA_ORG_ADMIN, GLA_SPM, GLA_PM, GLA_PROGRAMME_ADMIN, GLA_FINANCE, GLA_READ_ONLY, ORG_ADMIN,
            PROJECT_EDITOR, PROJECT_READER, TECH_ADMIN})
    @RequestMapping(value = "/projectOverview/{id}", method = GET)
    public BaseProject getOverview(@PathVariable Integer id) {
        return service.projectOverview(id);
    }

    @Secured({OPS_ADMIN, GLA_ORG_ADMIN, GLA_SPM, GLA_PM, GLA_FINANCE, GLA_READ_ONLY, ORG_ADMIN, PROJECT_EDITOR, PROJECT_READER,
        TECH_ADMIN})
    @RequestMapping(value = "/projects/{legacyProjectCode}/id", method = GET)
    public Integer lookupProjectIdByLegacyProjectCode(@PathVariable Integer legacyProjectCode) {
        Project project = service.getByLegacyProjectCode(legacyProjectCode);
        return project != null ? project.getId() : null;
    }

    @Secured({OPS_ADMIN})
    @RequestMapping(value = "/projects/{id}/clone", method = RequestMethod.POST)
    @ApiOperation(value = "clones a project", notes = "")
    public Project clone(@PathVariable Integer id, @RequestParam(name = "title", required = false) String title) {
        return service.cloneProject(id, title);
    }

    @Secured({OPS_ADMIN})
    @RequestMapping(value = "/projects/clone", method = RequestMethod.POST)
    @ApiOperation(value = "clones a project", notes = "")
    public Project clone(@RequestParam(name = "existingProjectTitle") String existingProjectTitle,
        @RequestParam(name = "clonedProjectTitle", required = false) String clonedProjectTitle) {
        return service.cloneProject(existingProjectTitle, clonedProjectTitle);
    }

    @Secured({OPS_ADMIN})
    @RequestMapping(value = "/projects/move/{id}/toProgramme/{progId}/template/{templateId}", method = RequestMethod.PUT)
    @ApiOperation(value = "moves a project from one programmee to another", notes = "")
    public Project moveProject(@PathVariable Integer id, @PathVariable Integer progId, @PathVariable Integer templateId) {
        return service.moveProjectToProgrammeAndTemplate(id, progId, templateId);
    }

    @Secured({OPS_ADMIN, GLA_ORG_ADMIN, GLA_SPM, GLA_PM})
    @RequestMapping(value = "/projects/bulkOperation", method = RequestMethod.PUT)
    @ApiOperation(value = "performs the operation on all projects specifies", notes = "")
    @ApiResponses(@ApiResponse(code = 400, message = "validation error", response = ApiError.class))
    public BulkUpdateResult bulkOperation(@Valid @RequestBody BulkProjectUpdateOperation updateOperation) {
        return service.handleBulkOperation(updateOperation);
    }

    @Secured({OPS_ADMIN, GLA_ORG_ADMIN, GLA_SPM, GLA_PM, GLA_PROGRAMME_ADMIN, ORG_ADMIN, PROJECT_EDITOR})
    @RequestMapping(value = "/projects/{id}/details", method = RequestMethod.PUT)
    @ApiOperation(value = "updates an existing named project detail block", notes = "")
    @ApiResponses(@ApiResponse(code = 400, message = "validation error", response = ApiError.class))
    public ProjectDetailsBlock updateProjectDetails(@PathVariable Integer id, @Valid @RequestBody ProjectDetailsBlock detailsBlock,
                                                    BindingResult bindingResult) {
        verifyBinding("Invalid project!", bindingResult);

        return service.updateProjectDetails(id, detailsBlock);
    }


    @Secured({OPS_ADMIN, GLA_PM})
    @RequestMapping(value = "/projects/{id}/recommendation/{recommendation}", method = RequestMethod.PUT)
    @ApiOperation(value = "updates an existing project", notes = "")
    @ApiResponses(@ApiResponse(code = 400, message = "validation error", response = ApiError.class))
    public Project recordRecommendation(@PathVariable Integer id,
                                        @PathVariable Project.Recommendation recommendation,
                                        @RequestBody(required = false) String comments) {
        return service.recordRecommendation(id, recommendation, comments);
    }

    @Secured({OPS_ADMIN, GLA_ORG_ADMIN, GLA_SPM, GLA_PM, GLA_FINANCE, GLA_PROGRAMME_ADMIN, GLA_READ_ONLY, ORG_ADMIN,
            PROJECT_EDITOR, PROJECT_READER, TECH_ADMIN})
    @RequestMapping(value = "/projects/{id}/design", method = GET)
    @ApiOperation(value = "get a project's design details", notes = "")
    @ApiResponses(@ApiResponse(code = 400, message = "validation error", response = ApiError.class))
    public DesignStandardsBlock getDesignStandards(@PathVariable Integer id) {
        return service.get(id).getDesignStandardsBlock();
    }

    @Secured({OPS_ADMIN, GLA_ORG_ADMIN, GLA_SPM, GLA_PM, GLA_FINANCE, GLA_PROGRAMME_ADMIN, GLA_READ_ONLY,
            ORG_ADMIN, PROJECT_EDITOR, PROJECT_READER, TECH_ADMIN})
    @RequestMapping(value = "/projects/{projectId}/grant", method = GET)
    @ApiOperation(value = "gets a project's design standards details", notes = "")
    public GrantSourceBlock getGrantSource(@PathVariable Integer projectId) {
        return service.getProjectGrantSource(projectId);
    }

    @Secured({OPS_ADMIN, GLA_ORG_ADMIN, GLA_SPM, GLA_PM, GLA_PROGRAMME_ADMIN, ORG_ADMIN, PROJECT_EDITOR})
    @RequestMapping(value = "/projects/{projectId}/grant", method = RequestMethod.PUT)
    @ApiOperation(value = "set a project's design standards details", notes = "")
    @ApiResponses(@ApiResponse(code = 400, message = "validation error", response = ApiError.class))
    public GrantSourceBlock updateGrantSource(@PathVariable Integer projectId,
                                              @Valid @RequestBody GrantSourceBlock grantSourceBlock, BindingResult bindingResult) {
        verifyBinding("Invalid Grant Source Block details!", bindingResult);
        return service.updateProjectGrantSource(projectId, grantSourceBlock);
    }

    @Secured({OPS_ADMIN, GLA_ORG_ADMIN, GLA_SPM, GLA_PM})
    @RequestMapping(value = "/projects/{projectId}/markedForCorporate", method = RequestMethod.PUT)
    public void updateMarkedForCorporate(@PathVariable Integer projectId, @RequestBody String markedForCorporate) {
        if (featureStatus.isEnabled(Feature.MarkProjectCorporate)) {
            service.setMarkedForCorporate(projectId, Boolean.parseBoolean(markedForCorporate));
        }  else {
            throw new ForbiddenAccessException("This feature is currently disabled.");
        }
    }



    @Secured({OPS_ADMIN})
    @RequestMapping(value = "/projects/{id}/deactivate", method = RequestMethod.PUT)
    @ApiOperation(value = "submits a project and optionally update comments")
    @ApiResponses(@ApiResponse(code = 400, message = "validation error", response = ApiError.class))
    public void deactivateTestOnlyFunction(@PathVariable Integer id, @RequestBody(required = false) String comments) {
        Project project = service.get(id);
        service.testOnlyMoveProjectToStatus(project, ProjectStatus.Assess);
    }

    @Secured({OPS_ADMIN, GLA_ORG_ADMIN, GLA_SPM, GLA_PM, ORG_ADMIN, PROJECT_EDITOR})
    @RequestMapping(value = "/projects/{id}/status", method = RequestMethod.PUT)
    @ApiOperation(value = "updates a project state and optionally update substatus and comments")
    @ApiResponses(@ApiResponse(code = 400, message = "validation error", response = ApiError.class))
    public void updateStatus(@PathVariable Integer id,
                             @RequestParam(defaultValue = "false") boolean validateOnly,
                             @RequestBody UpdateStatusRequest request) {
        Project project = service.getEnrichedProject(id);

        if (validateOnly) {
            service.validateTransitionProjectToStatus(project, new ProjectState(request.getStatus(), request.getSubStatus()));
        } else {
            StateTransitionResult result = service.transitionProjectToStatus(project, new ProjectState(request.getStatus(),
                request.getSubStatus()), request.getComments(), request.isApprovePaymentsOnly(), request.getReason());
            if (!result.wasSuccessful()) {
                throw new ValidationException(result.getFailureMessage());
            }
        }
    }

    @Secured({OPS_ADMIN, GLA_ORG_ADMIN, GLA_PROGRAMME_ADMIN, GLA_SPM, GLA_PM, ORG_ADMIN, PROJECT_EDITOR})
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

    @Secured({OPS_ADMIN, GLA_ORG_ADMIN, GLA_SPM, GLA_PM, ORG_ADMIN, PROJECT_EDITOR})
    @RequestMapping(value = "/projects/{id}/draftcomment", method = RequestMethod.PUT)
    @ApiOperation(value = "saves a project's comments as draft")
    @ApiResponses(@ApiResponse(code = 400, message = "validation error", response = ApiError.class))
    public void saveDraftComments(@PathVariable Integer id, @RequestBody(required = false) String comments) {
        service.saveDraftComments(id, comments);
    }

    @Secured({OPS_ADMIN, GLA_ORG_ADMIN, GLA_SPM, GLA_PM, GLA_FINANCE, GLA_PROGRAMME_ADMIN, GLA_READ_ONLY, ORG_ADMIN,
            PROJECT_EDITOR, PROJECT_READER,
        TECH_ADMIN})
    @RequestMapping(value = "/projects/{id}/history", method = GET)
    @ApiOperation(value = "get a project's history", notes = "")
    @ApiResponses(@ApiResponse(code = 400, message = "validation error", response = ApiError.class))
    public List<ProjectHistoryEntity> getProjectHistory(@PathVariable Integer id) {
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

    @Secured({OPS_ADMIN, GLA_ORG_ADMIN, GLA_SPM, GLA_PM, GLA_FINANCE, GLA_PROGRAMME_ADMIN, GLA_READ_ONLY,
            ORG_ADMIN, PROJECT_EDITOR})
    @RequestMapping(value = "/projects/{id}/lock/{blockId}", method = GET)
    @ApiOperation(value = "lock a project block", notes = "")
    @ApiResponses(@ApiResponse(code = 400, message = "validation error", response = ApiError.class))
    public LockRequestStatus lockProjectBlock(@PathVariable Integer id, @PathVariable Integer blockId) {
        NamedProjectBlock block = projectBlockService.getProjectBlock(blockId);
        return service.tryLock(block);
    }


    @Secured({OPS_ADMIN, GLA_ORG_ADMIN, GLA_SPM, GLA_PM, ORG_ADMIN, GLA_PROGRAMME_ADMIN, PROJECT_EDITOR})
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

    @Secured({OPS_ADMIN, GLA_ORG_ADMIN, GLA_SPM, GLA_PM, GLA_FINANCE, GLA_PROGRAMME_ADMIN, GLA_READ_ONLY,
            ORG_ADMIN, PROJECT_EDITOR, PROJECT_READER, TECH_ADMIN})
    @RequestMapping(value = "/projects/{id}/block/{blockId}/revert", method = RequestMethod.PUT)
    @ApiOperation(value = "get a project block by ID", notes = "")
    @ApiResponses(@ApiResponse(code = 400, message = "validation error", response = ApiError.class))
    public NamedProjectBlock revertProjectBlock(@PathVariable Integer id,
                                             @PathVariable Integer blockId) {
        if (featureStatus.isEnabled(Feature.AllowBlockRevert)) {
            return service.revertProjectBlock(id, blockId);
        }  else {
            throw new ForbiddenAccessException(
                    "This feature is currently disabled.");
        }
    }


    @Secured({OPS_ADMIN, GLA_ORG_ADMIN, GLA_SPM, GLA_PM, GLA_FINANCE, GLA_PROGRAMME_ADMIN, GLA_READ_ONLY,
            ORG_ADMIN, PROJECT_EDITOR, PROJECT_READER, TECH_ADMIN})
    @RequestMapping(value = "/projects/{id}/{blockId}", method = GET)
    @ApiOperation(value = "get a project block by ID", notes = "")
    @ApiResponses(@ApiResponse(code = 400, message = "validation error", response = ApiError.class))
    public NamedProjectBlock getProjectBlock(@PathVariable Integer id,
                                             @PathVariable Integer blockId,
                                             @RequestParam(name = "tryLock", required = false) boolean lock) {

        if (!lock) {
            log.debug("about to get project block");
            NamedProjectBlock blockLoadedDirectlyFromDB = projectBlockService.getProjectBlock(blockId);
            if (blockLoadedDirectlyFromDB.isSelfContained()) {
                log.debug("about to enrich block");
                return projectBlockService.enrichedBlock(blockLoadedDirectlyFromDB);
            }
        }

        log.debug("about to enrich the project");
        Project project = service.getEnrichedProject(id);
        NamedProjectBlock block = project.getProjectBlockById(blockId);

        if (block == null) {
            throw new ValidationException(String.format("Unable to locate block with id: %d on project: %d", blockId, id));
        }

        if (block.dependsOnAnotherBlock()) {
            log.debug("about to enrich from dependant block");
            projectBlockService.enrichFromDependantBlock(block, project.getId());
        }

        if (lock && block.isEditable()) {
            try {
                log.debug("about to get block and lock");
                block = service.getBlockAndLock(project, block);
            } catch (Exception e) {
                // probably caused by a duplicated block
                log.error("Unable to update the specified project, see additional debug logging below for project:  "
                    + project.getId(), e);
                StringBuilder builder = new StringBuilder("Latest Project Blocks for  " + project.getId());
                for (NamedProjectBlock npb : project.getLatestProjectBlocks()) {
                    builder.append(String.format("\n\t LatestBlock:: Block %s Block Id %d Block Display Order %d Version Number %d",
                            npb.getBlockType(), npb.getId(), npb.getDisplayOrder(), npb.getVersionNumber()));
                }
                builder.append("\nProject Blocks for " + project.getId());
                for (NamedProjectBlock npb : project.getProjectBlocks()) {
                    builder.append(
                        String.format("\n\t ProjectBlock:: Block %s Block Id %d Block Display Order %d Version Number %d",
                            npb.getBlockType(), npb.getId(), npb.getDisplayOrder(), npb.getVersionNumber()));

                }
                log.debug(builder.toString());
            }

        }

        log.debug("returning block");
        return block;
    }

    @Secured({OPS_ADMIN, GLA_ORG_ADMIN, GLA_SPM, GLA_PM, GLA_FINANCE, GLA_PROGRAMME_ADMIN, GLA_READ_ONLY, ORG_ADMIN,
            PROJECT_EDITOR, PROJECT_READER, TECH_ADMIN})
    @RequestMapping(value = "/projects/{projectId}/displayOrder/{displayOrder}/history", method = GET)
    @ApiOperation(value = "get a project block history")
    @ApiResponses(@ApiResponse(code = 400, message = "validation error", response = ApiError.class))
    public List<ProjectBlockHistoryItem> getProjectBlockHistory(@PathVariable Integer projectId,
                                                                @PathVariable Integer displayOrder) {
        return service.getHistoryForBlock(projectId, displayOrder);
    }

    @Secured({OPS_ADMIN, GLA_ORG_ADMIN, GLA_SPM, GLA_PROGRAMME_ADMIN, GLA_PM, ORG_ADMIN, PROJECT_EDITOR})
    @RequestMapping(value = "/projects", method = RequestMethod.POST)
    @ApiOperation(value = "creates a project")
    @ApiResponses(@ApiResponse(code = 400, message = "validation error", response = ApiError.class))
    public Integer create(@Valid @RequestBody Project project, BindingResult bindingResult) {
        verifyBinding("Invalid project!", bindingResult);

        return service.createProject(project).getId();
    }

    @Secured({OPS_ADMIN, GLA_ORG_ADMIN, GLA_SPM, GLA_PROGRAMME_ADMIN, GLA_PM, ORG_ADMIN, PROJECT_EDITOR, TECH_ADMIN})
    @RequestMapping(value = "/projects/{projectId}/blocks/{blockId}", method = RequestMethod.PUT)
    public NamedProjectBlock updateProjectBlock(@PathVariable Integer projectId,
                                                @PathVariable Integer blockId,
                                                @Valid @RequestBody NamedProjectBlock block,
                                                @RequestParam(name = "releaseLock", defaultValue = "false", required = false)
                                                    boolean releaseLock) {
        return service.updateProjectBlock(projectId, blockId, block, releaseLock);
    }

    @Secured({OPS_ADMIN, ORG_ADMIN, PROJECT_EDITOR})
    @RequestMapping(value = "/projects/{projectId}/blocks/{blockId}", method = RequestMethod.DELETE)
    public void deleteUnapprovedBlock(@PathVariable Integer projectId, @PathVariable Integer blockId) {
        service.deleteUnapprovedBlock(projectId, blockId);
    }

    @PreAuthorize("authentication.name == 'test.admin@gla.com'")
    @RequestMapping(value = "/projects/{projectId}/blocks/{blockId}/lastModified", method = RequestMethod.PUT)
    public void updateProjectBlockLastModified(@PathVariable Integer projectId,
                                               @PathVariable Integer blockId,
                                               @RequestBody String day) {
        service.updateProjectBlockLastModified(projectId, blockId, OffsetDateTime.parse(day + "T00:00:00+00:00"));
    }

    @Secured(OPS_ADMIN)
    @RequestMapping(value = "/projects/transfer", method = RequestMethod.PUT)
    @ApiOperation(value = "transfer projects to another organisation", notes = "transfer projects to another organisation")
    public ProjectsTransferResult transfer(@RequestBody List<Integer> projectIds, @RequestParam Integer organisationId) {
        return service.transfer(projectIds, organisationId);
    }

    @Secured(OPS_ADMIN)
    @RequestMapping(value = "/projects/transferTestProject/{organisationId}", method = RequestMethod.PUT)
    @ApiOperation(value = "transfer projects to another organisation", notes = "transfer projects to another organisation "
        + "in DEV/QAS. This doesn't check if the action is allowed and exists to facilitate e2e")
    public ProjectsTransferResult transferTestProject(@RequestBody List<Integer> projectIds, @PathVariable Integer organisationId) {
        return service.transferTestProject(projectIds, organisationId);
    }

    @Secured(OPS_ADMIN)
    @RequestMapping(value = "/projects/moveAnswerValueToGrantSource", method = RequestMethod.POST)
    public void moveAnswerValueToGrantSource(@RequestParam Integer questionId) {
        service.moveAnswerValueToGrantSource(questionId);
    }

    @Secured({OPS_ADMIN, GLA_ORG_ADMIN, GLA_SPM, GLA_PM})
    @RequestMapping(value = "/projects/{projectId}/internalBlocks/{blockId}", method = RequestMethod.PUT)
    @ApiOperation(value = "updates a project internal block", notes = "updates a project internal block")
    @ApiResponses(@ApiResponse(code = 400, message = "validation error", response = ApiError.class))
    public void updateInternalProjectBlock(@PathVariable Integer projectId,
                                           @PathVariable Integer blockId,
                                           @Valid @RequestBody InternalProjectBlock block,
                                           BindingResult bindingResult) {
        verifyBinding("Invalid block details!", bindingResult);
        service.updateInternalProjectBlock(projectId, blockId, block);
    }

    @Secured({OPS_ADMIN, GLA_ORG_ADMIN, GLA_SPM, GLA_PM, GLA_FINANCE, GLA_READ_ONLY, ORG_ADMIN, PROJECT_EDITOR, PROJECT_READER,
        TECH_ADMIN})
    @RequestMapping(value = "/projects/wbsLookup", method = GET)
    @ApiOperation(value = "look up a list of project IDs given a WBS code",
        notes = "look up a list of project IDs given a WBS code")
    public Set<Integer> findAllProjectIdsByWBSCode(@RequestParam String wbsCode) {
        return service.findAllProjectIdsByWBSCode(wbsCode);
    }

    @Secured({OPS_ADMIN, GLA_ORG_ADMIN, GLA_SPM, GLA_PM})
    @RequestMapping(value = "/projects/{projectId}/labels", method = RequestMethod.POST)
    @ApiOperation(value = "creates a project label", notes = "creates a project label")
    @ApiResponses(@ApiResponse(code = 400, message = "validation error", response = ApiError.class))
    public Label createLabel(@PathVariable Integer projectId, @Valid @RequestBody Label label) {
        if (featureStatus.isEnabled(Feature.Labels)) {
            return service.createProjectLabel(projectId, label);
        }  else {
            throw new ForbiddenAccessException("This feature is currently disabled.");
        }
    }

    @Secured({OPS_ADMIN, GLA_ORG_ADMIN, GLA_SPM, GLA_PM, GLA_FINANCE, GLA_PROGRAMME_ADMIN, GLA_READ_ONLY,
            ORG_ADMIN, PROJECT_EDITOR, PROJECT_READER, TECH_ADMIN, INTERNAL_BLOCK_EDITOR})
    @RequestMapping(value = "/projects/filters/statuses", method = GET)
    @ApiOperation(value = "Get available statuses depending on logged in user",
        notes = "Get available statuses depending on logged in user")
    public Set<ProjectState> getAvailableFilterStatuses() {
        return projectStateService.getAvailableProjectStatesForUser();
    }

    @Secured({OPS_ADMIN, GLA_ORG_ADMIN, GLA_SPM, GLA_PM, GLA_FINANCE, ORG_ADMIN, PROJECT_EDITOR})
    @RequestMapping(value = "/projects/{projectId}/block/{blockId}/claim", method = RequestMethod.POST)
    @ApiOperation(value = "create a claim for a block", notes = "")
    @ApiResponses(@ApiResponse(code = 400, message = "validation error", response = ApiError.class))
    public void createClaim(@PathVariable Integer projectId,
                            @PathVariable Integer blockId,
                            @RequestBody Claim claim) {
        claimService.createClaim(projectId, blockId, claim);
    }

    @Secured({OPS_ADMIN, GLA_ORG_ADMIN, GLA_SPM, GLA_PM, GLA_FINANCE, ORG_ADMIN, PROJECT_EDITOR})
    @RequestMapping(value = "/projects/{projectId}/block/{blockId}/claim/{claimId}", method = RequestMethod.DELETE)
    @ApiOperation(value = "deletes a pending payment claim", notes = "deletes a pending payment claim")
    public void deleteClaim(@PathVariable Integer projectId, @PathVariable Integer blockId, @PathVariable Integer claimId) {
        claimService.deleteClaim(projectId, blockId,  claimId);
    }

    @Secured({OPS_ADMIN, GLA_ORG_ADMIN, GLA_PROGRAMME_ADMIN, GLA_SPM, GLA_PM, GLA_FINANCE, ORG_ADMIN, PROJECT_EDITOR, TECH_ADMIN})
    @RequestMapping(value = "/projects/template/{templateId}//organisation/{organisationId}/createAllowed", method = GET)
    public boolean canProjectBeAssignedToTemplate(@PathVariable Integer templateId, @PathVariable Integer organisationId) {
        return service.canProjectBeAssignedToTemplate(templateId, organisationId);
    }

    @Secured(OPS_ADMIN)
    @RequestMapping(value = "/projects/{projectId}/restricted", method = RequestMethod.PUT)
    @ApiOperation(value = "mark a project as restricted", notes = "mark a project as restricted")
    public void setRestricted(@PathVariable Integer projectId, @RequestParam(value = "restricted") Boolean restricted) {
         service.setRestricted(projectId, restricted);
    }

    @Secured(OPS_ADMIN)
    @RequestMapping(value = "/projects/{projectId}/accessControlList", method = RequestMethod.POST)
    @ApiOperation(value = "shares a project", notes = "shares a project")
    public void shareProject(@PathVariable Integer projectId, @RequestParam Integer orgId) {
        service.shareProject(projectId, orgId, GrantAccessTrigger.PROJECT);
    }

    @Secured(OPS_ADMIN)
    @RequestMapping(value = "/projects/{projectId}/accessControlList", method = RequestMethod.DELETE)
    @ApiOperation(value = "unshares a project", notes = "unshares a project")
    public void unshareProject(@PathVariable Integer projectId, @RequestParam Integer orgId) {
        service.unshareProject(projectId, orgId, GrantAccessTrigger.PROJECT);
    }

    @Secured(OPS_ADMIN)
    @RequestMapping(value = "/projects/updateTitleHistory", method = RequestMethod.PUT)
    @ApiOperation(value = "update project title history", notes = "updates project title history")
    public void updateProjectTitleHistory(@RequestBody List<Integer> projectIds) {
        service.updateProjectTitleHistory(projectIds);
    }

    @Secured({OPS_ADMIN, GLA_ORG_ADMIN, GLA_SPM})
    @RequestMapping(value = "/projects/{projectId}/suspendPayments", method = RequestMethod.PUT)
    @ApiOperation(value = "suspend or resume payments on a project", notes = "suspend or resume payments on a project")
    public void suspendPayments(@PathVariable Integer projectId,
                                @RequestParam (name = "paymentsSuspended", required = true)  boolean paymentsSuspended,
                                @RequestBody String comments) {
        service.suspendProjectPayments(projectId, paymentsSuspended, comments);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/project/{projectId}/file/{fileId}")
    @ApiOperation(value = "file download", notes = "Endpoint for downloading an file")
    public void download(@PathVariable Integer projectId,
                         @PathVariable Integer fileId,
                         HttpServletResponse response) throws IOException {
        service.getFileForProject(fileId, projectId, response);
    }

    @Secured({OPS_ADMIN, GLA_ORG_ADMIN, GLA_SPM, GLA_PM})
    @RequestMapping(value = "/projects/{projectId}/downloadAllAnswers", produces = "application/zip", method = RequestMethod.GET)
    public void downloadAllAnswers(@PathVariable Integer projectId, HttpServletResponse response) throws IOException {
        if (featureStatus.isEnabled(Feature.AllowAllFileDownload)) {
            response.addHeader(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + "P" + projectId + ".zip\"");
            response.addHeader(HttpHeaders.CONTENT_TYPE, "application/zip");
            response.flushBuffer();
            service.getZipFileForProject(projectId, response.getOutputStream());
            response.flushBuffer();
        }  else {
            throw new ForbiddenAccessException("This feature is currently disabled.");
        }
    }

    @PermissionRequired(PROJ_ASSIGN)
    @GetMapping("/projects/{projectId}/assignableUsers")
    Set<UserIdAndName> getProjectAssignableUsers(@PathVariable Integer projectId) {
        return service.getProjectAssignableUsers(projectId);
    }

    @PermissionRequired(PROJ_ASSIGN)
    @GetMapping("/projects/assignableUsers")
    Set<UserIdAndName> getProjectAssignableUsers(@RequestParam String projectIds) {
        List<Integer> parsedIds = Arrays.stream(projectIds.split(",")).map(Integer::valueOf).collect(Collectors.toList());
        return service.getProjectAssignableUsers(parsedIds);
    }

    @PermissionRequired(PROJ_ASSIGN)
    @PutMapping("/projects/{projectId}/assignee")
    public void assignProject(@PathVariable Integer projectId, @RequestBody List<String> assignee) {
        service.assignProject(projectId, assignee);
    }

    @PermissionRequired(PROJ_ASSIGN)
    @PutMapping("/projects/assignUsers")
    public void assignProject(@RequestBody List<ProjectAssigneesSummary> projectIdsMapping) {
        service.assignMultipleProjects(projectIdsMapping);
    }

    @PermissionRequired(PROJ_ASSIGN)
    @PutMapping("/projects/unassignUsers")
    public void unassignProject(@RequestBody ProjectAssigneesSummary summary) {
        service.unassignMultipleProjects(summary.getProjectIds(), summary.getAssignees());
    }

}
