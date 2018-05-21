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
import org.springframework.security.access.annotation.Secured;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import uk.gov.london.ops.domain.project.ProjectAction;
import uk.gov.london.ops.domain.project.ProjectRiskAndIssue;
import uk.gov.london.ops.domain.project.ProjectRisksBlock;
import uk.gov.london.ops.domain.user.Role;
import uk.gov.london.ops.exception.ApiError;

import javax.validation.Valid;

/**
 * REST API for managing Projects.
 *
 * @author Steve Leach
 */
@RestController
@RequestMapping("/api/v1")
@Api(
        description = "managing Project risk data"
)
public class ProjectRisksAPI extends BaseProjectAPI {

    Logger log = LoggerFactory.getLogger(getClass());


    @Secured({Role.OPS_ADMIN, Role.GLA_ORG_ADMIN, Role.GLA_SPM, Role.GLA_PM, Role.ORG_ADMIN, Role.PROJECT_EDITOR})
    @RequestMapping(value = "/projects/{projectId}/risks/{blockId}", method = RequestMethod.PUT)
    @ApiOperation(value = "set a project's risk level", notes = "")
    @ApiResponses(@ApiResponse(code = 400, message = "validation error", response = ApiError.class))
    public ProjectRisksBlock updateProjectRisk(@PathVariable Integer projectId,
                                                  @PathVariable Integer blockId,
                                                  @Valid @RequestBody ProjectRisksBlock risksBlock,
                                                  @RequestParam(name = "releaseLock", defaultValue = "false", required = false) boolean releaseLock,
                                                  BindingResult bindingResult) {
        verifyBinding("Invalid Risk details!", bindingResult);
        return service.updateProjectRisks(projectId, blockId, risksBlock, releaseLock);
    }

    @Secured({Role.OPS_ADMIN, Role.GLA_ORG_ADMIN, Role.GLA_SPM, Role.GLA_PM, Role.ORG_ADMIN, Role.PROJECT_EDITOR})
    @RequestMapping(value = "/projects/{projectId}/risks/{blockId}/risk", method = RequestMethod.POST)
    @ApiOperation(value = "create a project risk ", notes = "")
    @ApiResponses(@ApiResponse(code = 400, message = "validation error", response = ApiError.class))
    public ProjectRisksBlock createProjectRisk(@PathVariable Integer projectId,
                                                  @PathVariable Integer blockId,
                                                  @Valid @RequestBody ProjectRiskAndIssue risk,
                                                  @RequestParam(name = "releaseLock", defaultValue = "false", required = false) boolean releaseLock,
                                                  BindingResult bindingResult) {
        verifyBinding("Invalid Risk details!", bindingResult);
        return service.createProjectRisk(projectId, blockId, risk, releaseLock);
    }

    @Secured({Role.OPS_ADMIN, Role.GLA_ORG_ADMIN, Role.GLA_SPM, Role.GLA_PM, Role.ORG_ADMIN, Role.PROJECT_EDITOR})
    @RequestMapping(value = "/projects/{projectId}/risks/{blockId}/risk/{riskId}/action", method = RequestMethod.POST)
    @ApiOperation(value = "add a mitigation/action ", notes = "")
    @ApiResponses(@ApiResponse(code = 400, message = "validation error", response = ApiError.class))
    public ProjectRisksBlock addAction(@PathVariable Integer projectId,
                                                  @PathVariable Integer blockId,
                                                  @PathVariable Integer riskId,
                                                  @Valid @RequestBody ProjectAction action,
                                                  @RequestParam(name = "releaseLock", defaultValue = "false", required = false) boolean releaseLock,
                                                  BindingResult bindingResult) {
        verifyBinding("Invalid Risk details!", bindingResult);
        return service.addActionToRisk(projectId, blockId, riskId, action, releaseLock);
    }

    @Secured({Role.OPS_ADMIN, Role.GLA_ORG_ADMIN, Role.GLA_SPM, Role.GLA_PM, Role.ORG_ADMIN, Role.PROJECT_EDITOR})
    @RequestMapping(value = "/projects/{projectId}/risks/{blockId}/risk/{riskId}/action/{actionId}", method = RequestMethod.DELETE)
    @ApiOperation(value = "deleted mitigation/action ", notes = "")
    @ApiResponses(@ApiResponse(code = 400, message = "validation error", response = ApiError.class))
    public void deleteAction(@PathVariable Integer projectId,
                                                  @PathVariable Integer blockId,
                                                  @PathVariable Integer riskId,
                                                  @PathVariable Integer actionId,
                                                  @RequestParam(name = "releaseLock", defaultValue = "false", required = false) boolean releaseLock) {
        service.deleteActionFromRisk(projectId, blockId, riskId, actionId, releaseLock);
    }


    @Secured({Role.OPS_ADMIN, Role.GLA_ORG_ADMIN, Role.GLA_SPM, Role.GLA_PM, Role.ORG_ADMIN, Role.PROJECT_EDITOR})
    @RequestMapping(value = "/projects/{projectId}/risks/{blockId}/risk/{riskId}", method = RequestMethod.PUT)
    @ApiOperation(value = "updates a project risk ", notes = "")
    @ApiResponses(@ApiResponse(code = 400, message = "validation error", response = ApiError.class))
    public void updateProjectRisk(@PathVariable Integer projectId,
                                               @PathVariable Integer blockId,
                                               @PathVariable Integer riskId,
                                               @Valid @RequestBody ProjectRiskAndIssue risk,
                                               @RequestParam(name = "releaseLock", defaultValue = "false", required = false) boolean releaseLock,
                                               BindingResult bindingResult) {
        verifyBinding("Invalid Risk details!", bindingResult);
        service.updateProjectRisk(projectId, blockId, riskId, risk, releaseLock);
    }

    @Secured({Role.OPS_ADMIN, Role.GLA_ORG_ADMIN, Role.GLA_SPM, Role.GLA_PM, Role.ORG_ADMIN, Role.PROJECT_EDITOR})
    @RequestMapping(value = "/projects/{projectId}/risks/{blockId}/risk/{riskId}/close", method = RequestMethod.PUT)
    @ApiOperation(value = "deleted mitigation/action ", notes = "")
    @ApiResponses(@ApiResponse(code = 400, message = "validation error", response = ApiError.class))
    public void closeProjectRisk(@PathVariable Integer projectId,
                                        @PathVariable Integer blockId,
                                        @PathVariable Integer riskId,
                                        @RequestParam(name = "releaseLock", defaultValue = "false", required = false) boolean releaseLock) {
        service.closeProjectRisk(projectId, blockId, riskId, releaseLock);
    }

    @Secured({Role.OPS_ADMIN, Role.GLA_ORG_ADMIN, Role.GLA_SPM, Role.GLA_PM, Role.ORG_ADMIN, Role.PROJECT_EDITOR})
    @RequestMapping(value = "/projects/{projectId}/risks/{blockId}/risk/{riskId}", method = RequestMethod.DELETE)
    @ApiOperation(value = "deletes a project risk ", notes = "")
    public void deleteProjectRisk(@PathVariable Integer projectId,
                                  @PathVariable Integer blockId,
                                  @PathVariable Integer riskId,
                                  @RequestParam(name = "releaseLock", defaultValue = "false", required = false) boolean releaseLock) {
        service.deleteProjectRisk(projectId, blockId, riskId, releaseLock);
    }

}
