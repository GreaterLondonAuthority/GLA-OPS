/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.project.skills;

import static uk.gov.london.common.user.BaseRole.GLA_FINANCE;
import static uk.gov.london.common.user.BaseRole.GLA_ORG_ADMIN;
import static uk.gov.london.common.user.BaseRole.GLA_PM;
import static uk.gov.london.common.user.BaseRole.GLA_READ_ONLY;
import static uk.gov.london.common.user.BaseRole.GLA_SPM;
import static uk.gov.london.common.user.BaseRole.OPS_ADMIN;
import static uk.gov.london.common.user.BaseRole.ORG_ADMIN;
import static uk.gov.london.common.user.BaseRole.PROJECT_EDITOR;
import static uk.gov.london.common.user.BaseRole.PROJECT_READER;
import static uk.gov.london.common.user.BaseRole.TECH_ADMIN;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import javax.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.london.common.error.ApiError;

@RestController
@RequestMapping("/api/v1")
@Api(
        description = "managing Project skills data"
)
public class ProjectSkillsAPI {

    Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    private ProjectSkillsService projectSkillsService;

    @Secured({OPS_ADMIN, GLA_ORG_ADMIN, GLA_SPM, GLA_PM, GLA_FINANCE, GLA_READ_ONLY, ORG_ADMIN, PROJECT_EDITOR, PROJECT_READER,
            TECH_ADMIN})
    @RequestMapping(value = "/projects/{projectId}/learningGrant/{blockId}", method = RequestMethod.GET)
    @ApiOperation(value = "get a project's learning grant block", notes = "")
    public LearningGrantBlock getLearningGrant(@PathVariable Integer projectId,
            @PathVariable Integer blockId) {
        return projectSkillsService.getLearningGrant(projectId, blockId);
    }

    @Secured({OPS_ADMIN, GLA_ORG_ADMIN, GLA_SPM, GLA_PM, ORG_ADMIN, PROJECT_EDITOR, TECH_ADMIN})
    @RequestMapping(value = "/projects/{projectId}/learningGrant/{blockId}/years/{year}", method = RequestMethod.PUT)
    public LearningGrantBlock updateLearningGrant(@PathVariable Integer projectId,
            @PathVariable Integer blockId,
            @PathVariable Integer year,
            @Valid @RequestBody LearningGrantBlock block,
            @RequestParam(name = "releaseLock", defaultValue = "false", required = false) boolean releaseLock) {
        return projectSkillsService.updateLearningGrant(projectId, blockId, year, block, releaseLock);
    }

    @Secured({OPS_ADMIN, GLA_ORG_ADMIN, GLA_SPM, GLA_PM, GLA_FINANCE, GLA_READ_ONLY, ORG_ADMIN, PROJECT_EDITOR, PROJECT_READER,
            TECH_ADMIN})
    @RequestMapping(value = "/projects/{projectId}/fundingClaims", method = RequestMethod.GET)
    @ApiOperation(value = "get a project's funding claims block", notes = "")
    public FundingClaimsBlock getFundingClaims(@PathVariable Integer projectId) {
        return projectSkillsService.getFundingClaims(projectId);
    }

    @Secured({OPS_ADMIN, GLA_ORG_ADMIN, GLA_SPM, GLA_PM, ORG_ADMIN, PROJECT_EDITOR, TECH_ADMIN})
    @RequestMapping(value = "/projects/{projectId}/fundingClaims/{blockId}/entry", method = RequestMethod.PUT)
    public FundingClaimsBlock updateFundingClaimsEntry(@PathVariable Integer projectId,
            @PathVariable Integer blockId,
            @Valid @RequestBody FundingClaimsEntry entry,
            @RequestParam(name = "releaseLock", defaultValue = "false", required = false) boolean releaseLock) {
        return projectSkillsService.updateFundingClaimsEntry(projectId, entry, releaseLock);
    }

    @Secured({OPS_ADMIN, GLA_ORG_ADMIN, GLA_SPM, GLA_PM, ORG_ADMIN, PROJECT_EDITOR, TECH_ADMIN})
    @RequestMapping(value = "/projects/{projectId}/fundingClaims/{blockId}", method = RequestMethod.PUT)
    public FundingClaimsBlock updateFundingClaimsBlock(@PathVariable Integer projectId,
            @PathVariable Integer blockId,
            @Valid @RequestBody FundingClaimsBlock block,
            @RequestParam(name = "releaseLock", defaultValue = "false", required = false) boolean releaseLock) {
        return projectSkillsService.updateFundingClaimsBlock(projectId, block, releaseLock);
    }

    @PreAuthorize("authentication.name == 'system.scheduler' or hasRole('OPS_ADMIN')")
    @RequestMapping(value = "/runScheduledPayments/onDate", method = RequestMethod.PUT)
    @ApiOperation(value = "run the scheduler on the specified date", notes = "")
    @ApiResponses(@ApiResponse(code = 400, message = "validation error", response = ApiError.class))
    public void runScheduledPaymentsAs(@RequestParam("date") String date) {
        projectSkillsService.runScheduler(date);
    }

}
