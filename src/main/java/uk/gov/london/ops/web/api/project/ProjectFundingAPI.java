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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import uk.gov.london.ops.domain.attachment.StandardAttachment;
import uk.gov.london.ops.domain.project.funding.FundingActivity;
import uk.gov.london.ops.domain.project.funding.FundingBlock;
import uk.gov.london.ops.service.project.ProjectFundingService;
import uk.gov.london.ops.web.model.project.FundingActivityLineItem;

import javax.validation.Valid;
import java.util.List;

import static uk.gov.london.common.user.BaseRole.*;
import static uk.gov.london.ops.framework.web.APIUtils.verifyBinding;

@RestController
@RequestMapping("/api/v1")
@Api(
        description = "managing project funding block"
)
public class ProjectFundingAPI {

    @Autowired
    private ProjectFundingService projectFundingService;

    @Secured({OPS_ADMIN, GLA_ORG_ADMIN, GLA_SPM, GLA_PM, GLA_FINANCE, GLA_READ_ONLY, ORG_ADMIN, PROJECT_EDITOR, PROJECT_READER, TECH_ADMIN})
    @RequestMapping(value = "/projects/{projectId}/funding/{blockId}", method = RequestMethod.GET)
    @ApiOperation(value = "returns a project funding block", notes = "returns a project funding block")
    public FundingBlock getProjectFundingBlock(@PathVariable Integer projectId, @PathVariable Integer blockId, @RequestParam(required = false) Integer year) {
        return projectFundingService.getProjectFundingBlock(projectId, blockId, year);
    }

    @Secured({OPS_ADMIN, GLA_ORG_ADMIN, GLA_SPM, GLA_PM, GLA_FINANCE, ORG_ADMIN, PROJECT_EDITOR})
    @RequestMapping(value = "/projects/{projectId}/funding/{blockId}/activities", method = RequestMethod.POST)
    @ApiOperation(value = "creates a funding activity", notes = "creates a funding activity")
    public FundingActivity createOrUpdateFundingActivity(@PathVariable Integer projectId,
                                                         @PathVariable Integer blockId,
                                                         @Valid @RequestBody FundingActivityLineItem fundingActivityRequest,
                                                         BindingResult bindingResult) {
        verifyBinding("Invalid funding activity details!", bindingResult);
        return projectFundingService.createOrUpdateFundingActivity(projectId, blockId, fundingActivityRequest);
    }

    @Secured({OPS_ADMIN, GLA_ORG_ADMIN, GLA_SPM, GLA_PM, GLA_FINANCE, ORG_ADMIN, PROJECT_EDITOR})
    @RequestMapping(value = "/projects/{projectId}/funding/{blockId}/activities/{activityId}/attachments", method = RequestMethod.POST)
    @ApiOperation(value = "creates a funding activity attachment", notes = "creates a funding activity attachment")
    public List<StandardAttachment> addFundingActivityEvidence(@PathVariable Integer projectId,
                                                               @PathVariable Integer blockId,
                                                               @PathVariable Integer activityId,
                                                               @RequestParam Integer fileId) {
        return projectFundingService.addFundingActivityEvidence(projectId, blockId, activityId, fileId);
    }

    @Secured({OPS_ADMIN, GLA_ORG_ADMIN, GLA_SPM, GLA_PM, GLA_FINANCE, ORG_ADMIN, PROJECT_EDITOR})
    @RequestMapping(value = "/projects/{projectId}/funding/{blockId}/activities/{activityId}", method = RequestMethod.DELETE)
    @ApiOperation(value = "deletes a funding activity", notes = "deletes a funding activity")
    public void deleteFundingActivity(@PathVariable Integer projectId,
                                      @PathVariable Integer blockId,
                                      @PathVariable Integer activityId) {
        projectFundingService.deleteFundingActivity(projectId, blockId, activityId);
    }

    @Secured({OPS_ADMIN, GLA_ORG_ADMIN, GLA_SPM, GLA_PM, GLA_FINANCE, ORG_ADMIN, PROJECT_EDITOR})
    @RequestMapping(value = "/projects/{projectId}/funding/{blockId}/activities/{activityId}/attachments/{attachmentId}", method = RequestMethod.DELETE)
    @ApiOperation(value = "deletes a funding activity attachment", notes = "deletes a funding activity attachment")
    public void removeFundingActivityEvidence(@PathVariable Integer projectId,
                                              @PathVariable Integer blockId,
                                              @PathVariable Integer activityId,
                                              @PathVariable Integer attachmentId) {
        projectFundingService.removeFundingActivityEvidence(projectId, blockId, activityId, attachmentId);
    }

    @Secured({OPS_ADMIN, GLA_ORG_ADMIN, GLA_SPM, GLA_PM, GLA_FINANCE, ORG_ADMIN, PROJECT_EDITOR})
    @RequestMapping(value = "/projects/{projectId}/funding/{blockId}/claim/{financialYear}/quarter/{quarter}", method = RequestMethod.POST)
    @ApiOperation(value = "creates a claim", notes = "creates a claim")
    public void createClaim(@PathVariable Integer projectId,
                                                               @PathVariable Integer blockId,
                                                               @PathVariable Integer financialYear,
                                                               @PathVariable Integer quarter) {
        projectFundingService.addClaim(projectId, blockId, financialYear, quarter);
    }

    @Secured({OPS_ADMIN, GLA_ORG_ADMIN, GLA_SPM, GLA_PM, GLA_FINANCE, ORG_ADMIN, PROJECT_EDITOR})
    @RequestMapping(value = "/projects/{projectId}/funding/{blockId}/claim/{claimId}", method = RequestMethod.DELETE)
    @ApiOperation(value = "deletes a pending claim", notes = "deletes a pending claim")
    public void deleteClaim(@PathVariable Integer projectId, @PathVariable Integer claimId) {
        projectFundingService.deleteClaim(projectId, claimId);
    }


}
