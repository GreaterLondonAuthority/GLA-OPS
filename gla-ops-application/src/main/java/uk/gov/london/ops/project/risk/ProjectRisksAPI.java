/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.project.risk;

import static uk.gov.london.common.user.BaseRole.GLA_ORG_ADMIN;
import static uk.gov.london.common.user.BaseRole.GLA_PM;
import static uk.gov.london.common.user.BaseRole.GLA_SPM;
import static uk.gov.london.common.user.BaseRole.OPS_ADMIN;
import static uk.gov.london.common.user.BaseRole.ORG_ADMIN;
import static uk.gov.london.common.user.BaseRole.PROJECT_EDITOR;
import static uk.gov.london.ops.framework.OPSUtils.verifyBinding;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import javax.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.london.common.error.ApiError;
import uk.gov.london.ops.project.ProjectService;

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
public class ProjectRisksAPI {

    Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    private ProjectService service;

    @Secured({OPS_ADMIN, GLA_ORG_ADMIN, GLA_SPM, GLA_PM, ORG_ADMIN, PROJECT_EDITOR})
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

    @Secured({OPS_ADMIN, GLA_ORG_ADMIN, GLA_SPM, GLA_PM, ORG_ADMIN, PROJECT_EDITOR})
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

    @Secured({OPS_ADMIN, GLA_ORG_ADMIN, GLA_SPM, GLA_PM, ORG_ADMIN, PROJECT_EDITOR})
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


    @Secured({OPS_ADMIN, GLA_ORG_ADMIN, GLA_SPM, GLA_PM, ORG_ADMIN, PROJECT_EDITOR})
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

    @Secured({OPS_ADMIN, GLA_ORG_ADMIN, GLA_SPM, GLA_PM, ORG_ADMIN, PROJECT_EDITOR})
    @RequestMapping(value = "/projects/{projectId}/risks/{blockId}/risk/{riskId}/close", method = RequestMethod.PUT)
    @ApiOperation(value = "deleted mitigation/action ", notes = "")
    @ApiResponses(@ApiResponse(code = 400, message = "validation error", response = ApiError.class))
    public void closeProjectRisk(@PathVariable Integer projectId,
            @PathVariable Integer blockId,
            @PathVariable Integer riskId,
            @RequestParam(name = "releaseLock", defaultValue = "false", required = false) boolean releaseLock) {
        service.closeProjectRisk(projectId, blockId, riskId, releaseLock);
    }

    @Secured({OPS_ADMIN, GLA_ORG_ADMIN, GLA_SPM, GLA_PM, ORG_ADMIN, PROJECT_EDITOR})
    @RequestMapping(value = "/projects/{projectId}/risks/{blockId}/risk/{riskId}", method = RequestMethod.DELETE)
    @ApiOperation(value = "deletes a project risk ", notes = "")
    public void deleteProjectRisk(@PathVariable Integer projectId,
            @PathVariable Integer blockId,
            @PathVariable Integer riskId,
            @RequestParam(name = "releaseLock", defaultValue = "false", required = false) boolean releaseLock) {
        service.deleteProjectRisk(projectId, blockId, riskId, releaseLock);
    }

}
