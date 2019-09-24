/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.web.api.project;

import uk.gov.london.common.error.ApiError;
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
import uk.gov.london.ops.framework.feature.Feature;
import uk.gov.london.ops.framework.feature.FeatureStatus;
import uk.gov.london.ops.domain.project.*;
import uk.gov.london.ops.domain.project.state.ProjectStatus;
import uk.gov.london.ops.service.project.ClaimService;
import uk.gov.london.ops.service.project.ProjectService;
import uk.gov.london.ops.service.project.ProjectStateService;
import uk.gov.london.ops.service.project.state.ProjectState;
import uk.gov.london.ops.service.project.state.StateTransitionResult;
import uk.gov.london.ops.framework.exception.ForbiddenAccessException;
import uk.gov.london.ops.framework.exception.ValidationException;
import uk.gov.london.ops.web.model.LockRequestStatus;
import uk.gov.london.ops.web.model.ProjectsTransferResult;
import uk.gov.london.ops.web.model.project.BulkProjectUpdateOperation;
import uk.gov.london.ops.web.model.project.BulkUpdateResult;
import uk.gov.london.ops.web.model.project.ProjectBlockHistoryItem;
import uk.gov.london.ops.web.model.project.UpdateStatusRequest;

import javax.validation.Valid;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;

import static uk.gov.london.common.user.BaseRole.*;
import static uk.gov.london.ops.framework.web.APIUtils.verifyBinding;

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
    FeatureStatus featureStatus;

    @Autowired
    ProjectStateService projectStateService;

    @Autowired
    ClaimService claimService;

    @Secured({PROJECT_EDITOR, PROJECT_READER, ORG_ADMIN, GLA_PM, GLA_FINANCE, GLA_READ_ONLY, GLA_ORG_ADMIN, GLA_SPM, OPS_ADMIN, TECH_ADMIN})
    @RequestMapping(value = "/projects", method = RequestMethod.GET, produces = "application/json")
    @ApiOperation(value = "get all project data", notes = "retrieves a list of all projects that the user has access to")
    @Transactional(readOnly = true)
    public Page<ProjectSummary> getAll(@RequestParam(name = "project", required = false) String project,
                                       @RequestParam(name = "organisation", required = false) String organisation,
                                       @RequestParam(name = "programme", required = false) String programme,
                                       @RequestParam(name = "programmes", required = false) List<Integer> programmes,
                                       @RequestParam(name = "templates", required = false) List<Integer> templates,
                                       @RequestParam(name = "states", required = false) List<String> states,
                                       @RequestParam(name= "watchingProject", required = false, defaultValue = "false") boolean watchingProject,
                                       Pageable pageable) {
        return service.findAll(project, organisation, programme, programmes, templates, states, watchingProject, pageable);
    }

    @Secured({OPS_ADMIN, GLA_ORG_ADMIN, GLA_SPM, GLA_PM, GLA_FINANCE, GLA_READ_ONLY, ORG_ADMIN, PROJECT_EDITOR, PROJECT_READER, TECH_ADMIN})
    @RequestMapping(value = "/projects/{id}", method = RequestMethod.GET)
    public Project get(@PathVariable Integer id,
                       @RequestParam(required = false, defaultValue = "true") boolean unapprovedChanges,
                       @RequestParam(required = false) NamedProjectBlock.BlockStatus compareToStatus,
                       @RequestParam(required = false) String comparisonDate,
                       @RequestParam(required = false, defaultValue = "false") boolean forComparison) {
        return service.getEnrichedProject(id, unapprovedChanges, true, compareToStatus, comparisonDate, forComparison);
    }
    @Secured({OPS_ADMIN, GLA_ORG_ADMIN, GLA_SPM, GLA_PM, GLA_FINANCE, GLA_READ_ONLY, ORG_ADMIN, PROJECT_EDITOR, PROJECT_READER, TECH_ADMIN})
    @RequestMapping(value = "/projectOverview/{id}", method = RequestMethod.GET)
    public BaseProject getOverview(@PathVariable Integer id) {
        return service.projectOverview(id);
    }

    @Secured({OPS_ADMIN, GLA_ORG_ADMIN, GLA_SPM, GLA_PM, GLA_FINANCE, GLA_READ_ONLY, ORG_ADMIN, PROJECT_EDITOR, PROJECT_READER, TECH_ADMIN})
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

    @Secured({OPS_ADMIN, GLA_ORG_ADMIN, GLA_SPM, GLA_PM, ORG_ADMIN, PROJECT_EDITOR})
    @RequestMapping(value = "/projects/{id}/details", method = RequestMethod.PUT)
    @ApiOperation(value = "updates an existing named project detail block", notes = "")
    @ApiResponses(@ApiResponse(code = 400, message = "validation error", response = ApiError.class))
    public ProjectDetailsBlock updateProjectDetails(@PathVariable Integer id, @Valid @RequestBody ProjectDetailsBlock detailsBlock,
                                                    BindingResult bindingResult) {
        verifyBinding("Invalid project!", bindingResult);

        return service.updateProjectDetails(id, detailsBlock);
    }


    @Secured({OPS_ADMIN,GLA_PM})
    @RequestMapping(value = "/projects/{id}/recommendation/{recommendation}", method = RequestMethod.PUT)
    @ApiOperation(value = "updates an existing project", notes = "")
    @ApiResponses(@ApiResponse(code = 400, message = "validation error", response = ApiError.class))
    public Project recordRecommendation(@PathVariable Integer id, @PathVariable Project.Recommendation recommendation, @RequestBody(required = false) String comments) {
        return service.recordRecommendation(id, recommendation, comments);

    }

    @Secured({OPS_ADMIN, GLA_ORG_ADMIN, GLA_SPM, GLA_PM, GLA_FINANCE, GLA_READ_ONLY, ORG_ADMIN, PROJECT_EDITOR, PROJECT_READER, TECH_ADMIN})
    @RequestMapping(value = "/projects/{id}/design", method = RequestMethod.GET)
    @ApiOperation(value = "get a project's design details", notes = "")
    @ApiResponses(@ApiResponse(code = 400, message = "validation error", response = ApiError.class))
    public DesignStandardsBlock getDesignStandards(@PathVariable Integer id) {
        return service.get(id).getDesignStandardsBlock();
    }

    @Secured({OPS_ADMIN, GLA_ORG_ADMIN, GLA_SPM, GLA_PM, GLA_FINANCE, GLA_READ_ONLY, ORG_ADMIN, PROJECT_EDITOR, PROJECT_READER, TECH_ADMIN})
    @RequestMapping(value = "/projects/{projectId}/grant", method = RequestMethod.GET)
    @ApiOperation(value = "gets a project's design standards details", notes = "")
    public GrantSourceBlock getGrantSource(@PathVariable Integer projectId) {
        return service.getProjectGrantSource(projectId);
    }

    @Secured({OPS_ADMIN, GLA_ORG_ADMIN, GLA_SPM, GLA_PM, ORG_ADMIN, PROJECT_EDITOR})
    @RequestMapping(value = "/projects/{projectId}/grant", method = RequestMethod.PUT)
    @ApiOperation(value = "set a project's design standards details", notes = "")
    @ApiResponses(@ApiResponse(code = 400, message = "validation error", response = ApiError.class))
    public GrantSourceBlock updateGrantSource(@PathVariable Integer projectId, @Valid @RequestBody GrantSourceBlock grantSourceBlock, BindingResult bindingResult) {
        verifyBinding("Invalid Grant Source Block details!", bindingResult);
        return service.updateProjectGrantSource(projectId, grantSourceBlock);
    }

    @Secured({OPS_ADMIN, GLA_ORG_ADMIN, GLA_SPM, GLA_PM})
    @RequestMapping(value = "/projects/{projectId}/markedForCorporate", method = RequestMethod.PUT)
    public void updateMarkedForCorporate(@PathVariable Integer projectId, @RequestBody String markedForCorporate) {

        if (featureStatus.isEnabled(Feature.MarkProjectCorporate)) {
            service.setMarkedForCorporate(projectId,Boolean.valueOf(markedForCorporate));
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
        }
        else {
            StateTransitionResult result = service.transitionProjectToStatus(project, new ProjectState(request.getStatus(), request.getSubStatus()), request.getComments());
            if (!result.wasSuccessful()) {
                throw new ValidationException(result.getFailureMessage());
            }
        }
    }

    @Secured({OPS_ADMIN, GLA_ORG_ADMIN, GLA_SPM, GLA_PM, ORG_ADMIN, PROJECT_EDITOR})
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

    @Secured({OPS_ADMIN, GLA_ORG_ADMIN, GLA_SPM, GLA_PM, GLA_FINANCE, GLA_READ_ONLY, ORG_ADMIN, PROJECT_EDITOR, PROJECT_READER, TECH_ADMIN})
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

    @Secured({OPS_ADMIN, GLA_ORG_ADMIN, GLA_SPM, GLA_PM, GLA_FINANCE, GLA_READ_ONLY, ORG_ADMIN, PROJECT_EDITOR})
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


    @Secured({OPS_ADMIN, GLA_ORG_ADMIN, GLA_SPM, GLA_PM, ORG_ADMIN, PROJECT_EDITOR})
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

    @Secured({OPS_ADMIN, GLA_ORG_ADMIN, GLA_SPM, GLA_PM, GLA_FINANCE, GLA_READ_ONLY, ORG_ADMIN, PROJECT_EDITOR, PROJECT_READER, TECH_ADMIN})
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


    @Secured({OPS_ADMIN, GLA_ORG_ADMIN, GLA_SPM, GLA_PM, GLA_FINANCE, GLA_READ_ONLY, ORG_ADMIN, PROJECT_EDITOR, PROJECT_READER, TECH_ADMIN})
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
            try {
                block = service.getBlockAndLock(project, block);
            } catch (Exception e) {
                // probably caused by a duplicated block
                log.error("Unable to update the specified project, see additional debug logging below for project:  " + project.getId(), e);
                StringBuilder builder = new StringBuilder("Latest Project Blocks for  " + project.getId());
                for (NamedProjectBlock npb : project.getLatestProjectBlocks()) {
                    builder.append(String.format("\n\t LatestBlock:: Block %s Block Id %d Block Display Order %d Version Number %d",
                            npb.getBlockType(), npb.getId(), npb.getDisplayOrder(), npb.getVersionNumber()));
                }
                builder.append("\nProject Blocks for " + project.getId());
                for (NamedProjectBlock npb : project.getProjectBlocks()) {
                    builder.append(String.format("\n\t ProjectBlock:: Block %s Block Id %d Block Display Order %d Version Number %d",
                            npb.getBlockType(), npb.getId(), npb.getDisplayOrder(), npb.getVersionNumber()));

                }
                log.debug(builder.toString());
            }

        }

        return block;
    }

    @Secured({OPS_ADMIN, GLA_ORG_ADMIN, GLA_SPM, GLA_PM, GLA_FINANCE, GLA_READ_ONLY, ORG_ADMIN, PROJECT_EDITOR, PROJECT_READER, TECH_ADMIN})
    @RequestMapping(value = "/projects/{projectId}/displayOrder/{displayOrder}/history", method = RequestMethod.GET)
    @ApiOperation(value = "get a project block history", notes = "")
    @ApiResponses(@ApiResponse(code = 400, message = "validation error", response = ApiError.class))
    public List<ProjectBlockHistoryItem> getProjectBlockHistory(@PathVariable Integer projectId,
                                                                @PathVariable Integer displayOrder) {
        return service.getHistoryForBlock(projectId, displayOrder);
    }

    @Secured({OPS_ADMIN, GLA_ORG_ADMIN, GLA_SPM, GLA_PM, ORG_ADMIN, PROJECT_EDITOR})
    @RequestMapping(value = "/projects", method = RequestMethod.POST)
    @ApiOperation(value = "creates a project", notes = "")
    @ApiResponses(@ApiResponse(code = 400, message = "validation error", response = ApiError.class))
    public Integer create(@Valid @RequestBody Project project, BindingResult bindingResult) {
        verifyBinding("Invalid project!", bindingResult);

        return service.createProject(project).getId();
    }

    @Secured({OPS_ADMIN, GLA_ORG_ADMIN, GLA_SPM, GLA_PM, ORG_ADMIN, PROJECT_EDITOR, TECH_ADMIN})
    @RequestMapping(value = "/projects/{projectId}/blocks/{blockId}", method = RequestMethod.PUT)
    public NamedProjectBlock updateProjectBlock(@PathVariable Integer projectId,
                                                @PathVariable Integer blockId,
                                                @Valid @RequestBody NamedProjectBlock block,
                                                @RequestParam(name = "releaseLock", defaultValue = "false", required = false) boolean releaseLock) {
        return service.updateProjectBlock(projectId, blockId, block, releaseLock);
    }

    @Secured({OPS_ADMIN, ORG_ADMIN, PROJECT_EDITOR})
    @RequestMapping(value = "/projects/{projectId}/blocks/{blockId}", method = RequestMethod.DELETE)
    public void deleteUnapprovedBlock(@PathVariable Integer projectId, @PathVariable Integer blockId) {
        service.deleteUnapprovedBlock(projectId, blockId);
    }

    @PreAuthorize("authentication.name == 'test.admin@gla.com'")
    @RequestMapping(value = "/projects/{projectId}/blocks/{blockId}/lastModified", method = RequestMethod.PUT)
    public void updateProjectBlockLastModified(@PathVariable Integer projectId, @PathVariable Integer blockId, @RequestBody String day) {
        service.updateProjectBlockLastModified(projectId, blockId, OffsetDateTime.parse(day+"T00:00:00+00:00"));
    }

    @Secured(OPS_ADMIN)
    @RequestMapping(value = "/projects/transfer", method = RequestMethod.PUT)
    @ApiOperation(value = "transfer projects to another organisation", notes = "transfer projects to another organisation")
    public ProjectsTransferResult transfer(@RequestBody List<Integer> projectIds, @RequestParam Integer organisationId) {
        return service.transfer(projectIds, organisationId);
    }

    @Secured(OPS_ADMIN)
    @RequestMapping(value = "/projects/transferTestProject/{organisationId}", method = RequestMethod.PUT)
    @ApiOperation(value = "transfer projects to another organisation", notes = "transfer projects to another organisation in DEV/QAS. This doesn't check if the action is allowed and exists to facilitate e2e")
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
    public void updateInternalProjectBlock(@PathVariable Integer projectId, @PathVariable Integer blockId, @Valid @RequestBody InternalProjectBlock block, BindingResult bindingResult) {
        verifyBinding("Invalid block details!", bindingResult);
        service.updateInternalProjectBlock(projectId, blockId, block);
    }

    @Secured({OPS_ADMIN, GLA_ORG_ADMIN, GLA_SPM, GLA_PM, GLA_FINANCE, GLA_READ_ONLY, ORG_ADMIN, PROJECT_EDITOR, PROJECT_READER, TECH_ADMIN})
    @RequestMapping(value = "/projects/wbsLookup", method = RequestMethod.GET)
    @ApiOperation(value = "look up a list of project IDs given a WBS code", notes = "look up a list of project IDs given a WBS code")
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

    @Secured({OPS_ADMIN, GLA_ORG_ADMIN, GLA_SPM, GLA_PM, GLA_FINANCE, GLA_READ_ONLY, ORG_ADMIN, PROJECT_EDITOR, PROJECT_READER, TECH_ADMIN})
    @RequestMapping(value = "/projects/filters/statuses", method = RequestMethod.GET)
    @ApiOperation(value = "Get available statuses depending on logged in user", notes = "Get available statuses depending on logged in user")
    public Set<ProjectState> getAvailableFilterStatuses() {
        return projectStateService.getAvailableProjectStatesForUser();
    }

    @Secured({OPS_ADMIN, GLA_ORG_ADMIN, GLA_SPM, GLA_PM, GLA_FINANCE, ORG_ADMIN, PROJECT_EDITOR})
    @RequestMapping(value = "/projects/{projectId}/block/{blockId}/claim", method = RequestMethod.POST)
    @ApiOperation(value = "create a claim for a block", notes = "")
    @ApiResponses(@ApiResponse(code = 400, message = "validation error", response = ApiError.class))
    public void createClaim(@PathVariable Integer projectId,
                            @PathVariable Integer blockId,
                            @RequestBody Claim claim){
        claimService.createClaim(projectId, blockId, claim);
    }

    @Secured({OPS_ADMIN, GLA_ORG_ADMIN, GLA_SPM, GLA_PM, GLA_FINANCE, ORG_ADMIN, PROJECT_EDITOR})
    @RequestMapping(value = "/projects/{projectId}/block/{blockId}/claim/{claimId}", method = RequestMethod.DELETE)
    @ApiOperation(value = "deletes a pending payment claim", notes = "deletes a pending payment claim")
    public void deleteClaim(@PathVariable Integer projectId, @PathVariable Integer blockId ,@PathVariable Integer claimId) {
        claimService.deleteClaim(projectId, blockId,  claimId);
    }

    @Secured({OPS_ADMIN, GLA_ORG_ADMIN, GLA_SPM, GLA_PM, GLA_FINANCE, ORG_ADMIN, PROJECT_EDITOR, TECH_ADMIN})
    @RequestMapping(value = "/projects/template/{templateId}//organisation/{organisationId}/createAllowed", method = RequestMethod.GET)
    public boolean canProjectBeAssignedToTemplate(@PathVariable Integer templateId,@PathVariable Integer organisationId){
        return service.canProjectBeAssignedToTemplate(templateId, organisationId);
    }

}
