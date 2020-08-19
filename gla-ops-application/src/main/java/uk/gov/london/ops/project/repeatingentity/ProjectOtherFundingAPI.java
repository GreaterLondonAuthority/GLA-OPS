/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.project.repeatingentity;

import static uk.gov.london.common.user.BaseRole.GLA_ORG_ADMIN;
import static uk.gov.london.common.user.BaseRole.GLA_PM;
import static uk.gov.london.common.user.BaseRole.GLA_SPM;
import static uk.gov.london.common.user.BaseRole.OPS_ADMIN;
import static uk.gov.london.common.user.BaseRole.ORG_ADMIN;
import static uk.gov.london.common.user.BaseRole.PROJECT_EDITOR;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.london.common.error.ApiError;

@RestController
@RequestMapping("/api/v1/otherFunding")
@Api(description = "managing project other funding data")
public class ProjectOtherFundingAPI extends RepeatingEntityAPI<OtherFunding> {

    @Autowired
    private ProjectOtherFundingService projectOtherFundingService;

    @Autowired
    public ProjectOtherFundingAPI(ProjectOtherFundingService service) {
        super(service);
    }

    @Secured({OPS_ADMIN, GLA_ORG_ADMIN, GLA_SPM, GLA_PM, ORG_ADMIN, PROJECT_EDITOR})
    @RequestMapping(value = "/project/{projectId}/block/{blockId}/item/{otherFundingId}/file/{fileId}", method = RequestMethod.PUT)
    @ApiOperation(value = "create an attachment", notes = "")
    @ApiResponses(@ApiResponse(code = 400, message = "validation error", response = ApiError.class))
    public OtherFundingBlock addEvidence(@PathVariable Integer projectId,
            @PathVariable Integer blockId,
            @PathVariable Integer otherFundingId,
            @PathVariable Integer fileId,
            @RequestParam(name = "releaseLock", defaultValue = "false", required = false) boolean releaseLock) {
        return projectOtherFundingService.attachEvidence(projectId, blockId, otherFundingId, fileId, releaseLock);
    }

    @Secured({OPS_ADMIN, GLA_ORG_ADMIN, GLA_SPM, GLA_PM, ORG_ADMIN, PROJECT_EDITOR})
    @RequestMapping(value = "/project/{projectId}/block/{blockId}/item/{otherFundingId}/file/{fileId}",
            method = RequestMethod.DELETE)
    @ApiOperation(value = "remove an attachment", notes = "")
    @ApiResponses(@ApiResponse(code = 400, message = "validation error", response = ApiError.class))
    public OtherFundingBlock removeEvidence(@PathVariable Integer projectId,
            @PathVariable Integer blockId,
            @PathVariable Integer otherFundingId,
            @PathVariable Integer fileId,
            @RequestParam(name = "releaseLock", defaultValue = "false", required = false) boolean releaseLock) {
        return projectOtherFundingService.removeEvidence(projectId, blockId, otherFundingId, fileId, releaseLock);
    }
}
