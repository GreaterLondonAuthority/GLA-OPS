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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import uk.gov.london.ops.domain.project.Milestone;
import uk.gov.london.ops.domain.project.NamedProjectBlock;
import uk.gov.london.ops.domain.project.Project;
import uk.gov.london.ops.domain.project.ProjectMilestonesBlock;
import uk.gov.london.ops.domain.user.Role;
import uk.gov.london.ops.exception.ApiError;
import uk.gov.london.ops.exception.ValidationException;
import uk.gov.london.ops.service.project.ProjectMilestonesService;

import javax.validation.Valid;

/**
 * Created by chris on 09/02/2017.
 */
@RestController
@RequestMapping("/api/v1")
@Api(
        description = "managing Project Milestone data"
)
public class ProjectMilestonesAPI extends BaseProjectAPI {

    @Autowired
    ProjectMilestonesService projectMilestonesService;

    @Secured({Role.OPS_ADMIN, Role.GLA_ORG_ADMIN, Role.GLA_SPM, Role.GLA_PM, Role.ORG_ADMIN, Role.PROJECT_EDITOR})
    @RequestMapping(value = "/projects/{id}/milestones/{blockId}", method = RequestMethod.PUT)
    @ApiOperation(value = "updates a project's milestones", notes = "")
    @ApiResponses(@ApiResponse(code = 400, message = "validation error", response = ApiError.class))
    public ProjectMilestonesBlock updateProjectMilestones(@PathVariable Integer id,
                                                          @PathVariable Integer blockId,
                                                          @RequestParam(name = "autosave", defaultValue = "false", required = false) boolean autosave,
                                                          @RequestParam(name = "releaseLock", defaultValue = "true", required = false) boolean releaseLock,
                                                          @Valid @RequestBody ProjectMilestonesBlock milestonesBlock,
                                                          BindingResult bindingResult) {
        verifyBinding("Invalid milestones!", bindingResult);
        Project project = service.get(id);
        project = projectMilestonesService.updateProjectMilestones(project, milestonesBlock, blockId, autosave || !releaseLock);
        ProjectMilestonesBlock projectBlockById = (ProjectMilestonesBlock) project.getProjectBlockById(blockId);
        projectBlockById.isComplete(); // doing this to ensure milestones claims exceeded is calculated correct prior to serialisation
        return projectBlockById;
    }

    @Secured({Role.OPS_ADMIN, Role.GLA_ORG_ADMIN, Role.GLA_SPM, Role.GLA_PM, Role.ORG_ADMIN, Role.PROJECT_EDITOR})
    @RequestMapping(value = "/projects/{id}/processingRoute/{blockId}", method = RequestMethod.PUT)
    @ApiOperation(value = "updates a project's processing route", notes = "")
    public ProjectMilestonesBlock updateProjectProcessingRoute(@PathVariable Integer id,@PathVariable Integer blockId,
                                                               @RequestParam(name = "autosave", defaultValue = "true", required = false) boolean autosave,
                                                               @RequestParam(name = "releaseLock", defaultValue = "false", required = false) boolean releaseLock,
                                                               @RequestBody Integer processingRouteId) {
        Project project = service.get(id);
        project = projectMilestonesService.updateProcessingRoute(project, processingRouteId, blockId, autosave || !releaseLock);
        return (ProjectMilestonesBlock) project.getProjectBlockById(blockId);
    }

    @Secured({Role.OPS_ADMIN, Role.GLA_ORG_ADMIN, Role.GLA_SPM, Role.GLA_PM, Role.ORG_ADMIN, Role.PROJECT_EDITOR})
    @RequestMapping(value = "/projects/{projectId}/milestones/{blockId}", method = RequestMethod.POST)
    @ApiOperation(value = "create a project milestones", notes = "")
    @ApiResponses(@ApiResponse(code = 400, message = "validation error", response = ApiError.class))
    public ProjectMilestonesBlock addProjectMilestones(@PathVariable Integer projectId,
                                                       @PathVariable Integer blockId,
                                                       @Valid @RequestBody Milestone milestone,
                                                       @RequestParam(name = "autosave", defaultValue = "true", required = false) boolean autosave,
                                                       @RequestParam(name = "releaseLock", defaultValue = "false", required = false) boolean releaseLock,
                                                       BindingResult bindingResult) {
        verifyBinding("Invalid milestone details!", bindingResult);
        return projectMilestonesService.createNewMilestone(projectId, blockId, milestone, autosave || !releaseLock);
    }

    @Secured({Role.OPS_ADMIN, Role.GLA_ORG_ADMIN, Role.GLA_SPM, Role.GLA_PM, Role.ORG_ADMIN, Role.PROJECT_EDITOR})
    @RequestMapping(value = "/projects/{projectId}/milestones/{blockId}/milestone/{milestoneId}/file/{fileId}", method = RequestMethod.PUT)
    @ApiOperation(value = "create a project attachment", notes = "")
    @ApiResponses(@ApiResponse(code = 400, message = "validation error", response = ApiError.class))
    public ProjectMilestonesBlock addProjectMilestoneEvidence(@PathVariable Integer projectId,
                                                       @PathVariable Integer blockId,
                                                       @PathVariable Integer milestoneId,
                                                       @PathVariable Integer fileId,
                                                       @RequestParam(name = "releaseLock", defaultValue = "false", required = false) boolean releaseLock) {
        return projectMilestonesService.attachMilestoneEvidence(projectId, blockId, milestoneId, fileId, releaseLock);
    }

    @Secured({Role.OPS_ADMIN, Role.GLA_ORG_ADMIN, Role.GLA_SPM, Role.GLA_PM, Role.ORG_ADMIN, Role.PROJECT_EDITOR})
    @RequestMapping(value = "/projects/{projectId}/milestones/{blockId}/milestone/{milestoneId}/attachment/{attachmentId}", method = RequestMethod.DELETE)
    @ApiOperation(value = "remove a project attachment", notes = "")
    @ApiResponses(@ApiResponse(code = 400, message = "validation error", response = ApiError.class))
    public ProjectMilestonesBlock removeProjectMilestoneEvidence(@PathVariable Integer projectId,
                                                       @PathVariable Integer blockId,
                                                       @PathVariable Integer milestoneId,
                                                       @PathVariable Integer attachmentId,
                                                       @RequestParam(name = "releaseLock", defaultValue = "false", required = false) boolean releaseLock) {
        return projectMilestonesService.removeMilestoneEvidence(projectId, blockId, milestoneId, attachmentId, releaseLock);
    }

    @Secured({Role.OPS_ADMIN, Role.GLA_ORG_ADMIN, Role.GLA_SPM, Role.GLA_PM, Role.ORG_ADMIN, Role.PROJECT_EDITOR})
    @RequestMapping(value = "/projects/{id}/milestones/{blockId}/milestone/{milestoneId}", method = RequestMethod.DELETE)
    @ApiOperation(value = "create a project milestones", notes = "")
    @ApiResponses(@ApiResponse(code = 400, message = "validation error", response = ApiError.class))
    public boolean deleteProjectMilestone(@PathVariable Integer id,
                                          @PathVariable Integer blockId,
                                          @PathVariable Integer milestoneId,
                                          @RequestParam(name = "autosave", defaultValue = "true", required = false) boolean autosave,
                                          @RequestParam(name = "releaseLock", defaultValue = "false", required = false) boolean releaseLock) {
        Project fromDB = service.get(id);
        Milestone toRemove = null;
        ProjectMilestonesBlock milestonesBlock = (ProjectMilestonesBlock) fromDB.getProjectBlockById(blockId);

        if (milestonesBlock.getMilestones() != null) {
            for (Milestone milestone : milestonesBlock.getMilestones()) {
                if (milestone.getId().equals(milestoneId)) {
                    toRemove = milestone;
                }
            }
        }
        if (toRemove == null) {
            throw new ValidationException(String.format("Unable to remove milestones with ID '%d' from project: %d", milestoneId, id));
        }
        projectMilestonesService.deleteMilestoneFromProject(fromDB,  toRemove, blockId, autosave || !releaseLock);

        return true;
    }

    @Secured({Role.OPS_ADMIN, Role.GLA_ORG_ADMIN, Role.GLA_SPM, Role.GLA_PM, Role.GLA_FINANCE, Role.GLA_READ_ONLY, Role.ORG_ADMIN, Role.PROJECT_EDITOR})
    @RequestMapping(value = "/projects/{projectId}/milestones/{blockId}", method = RequestMethod.GET)
    @ApiOperation(value = "get a project milestones block by id", notes = "")
    @ApiResponses(@ApiResponse(code = 400, message = "validation error", response = ApiError.class))
    public NamedProjectBlock getMilestonesBlock(@PathVariable Integer projectId,@PathVariable Integer blockId) {
        Project fromDB = service.get(projectId);
        return fromDB.getProjectBlockById(blockId);
    }

    @Secured({Role.OPS_ADMIN, Role.GLA_ORG_ADMIN, Role.GLA_SPM, Role.GLA_PM, Role.ORG_ADMIN, Role.PROJECT_EDITOR})
    @RequestMapping(value = "/projects/{projectId}/milestones/{milestoneId}/claim", method = RequestMethod.PUT)
    @ApiOperation(value = "claim a milestone", notes = "")
    @ApiResponses(@ApiResponse(code = 400, message = "validation error", response = ApiError.class))
    public void claim(@PathVariable Integer projectId, @PathVariable Integer milestoneId, @Valid @RequestBody Milestone milestone, BindingResult bindingResult) {
        verifyBinding("Invalid milestone!", bindingResult);
        projectMilestonesService.claim(projectId, milestoneId, milestone);
    }

    @Secured({Role.OPS_ADMIN, Role.GLA_ORG_ADMIN, Role.GLA_SPM, Role.GLA_PM, Role.ORG_ADMIN, Role.PROJECT_EDITOR})
    @RequestMapping(value = "/projects/{projectId}/milestones/{milestoneId}/cancelClaim", method = RequestMethod.PUT)
    @ApiOperation(value = "cancel the claim of an existing milestone", notes = "")
    @ApiResponses(@ApiResponse(code = 400, message = "validation error", response = ApiError.class))
    public void cancelClaim(@PathVariable Integer projectId, @PathVariable Integer milestoneId) {
        projectMilestonesService.cancelClaim(projectId, milestoneId);
    }

}
