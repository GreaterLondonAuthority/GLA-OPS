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
import uk.gov.london.ops.domain.project.ReceiptsBlock;
import uk.gov.london.ops.domain.project.SAPMetaData;
import uk.gov.london.ops.domain.user.Role;
import uk.gov.london.ops.exception.ApiError;
import uk.gov.london.ops.service.project.ProjectReceiptsService;
import uk.gov.london.ops.web.model.ProjectLedgerItemRequest;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
@Api(description = "managing Project receipts block")
public class ProjectReceiptsAPI extends BaseProjectAPI {

    @Autowired
    ProjectReceiptsService projectReceiptsService;

    @Secured({Role.OPS_ADMIN, Role.GLA_ORG_ADMIN, Role.GLA_SPM, Role.GLA_PM, Role.GLA_FINANCE, Role.GLA_READ_ONLY, Role.ORG_ADMIN, Role.PROJECT_EDITOR})
    @RequestMapping(value = "/projects/{id}/receipts/{blockId}", method = RequestMethod.GET)
    @ApiOperation(value = "get receipts block details for the given year ", notes = "")
    @ApiResponses(@ApiResponse(code = 400, message = "validation error", response = ApiError.class))
    public ReceiptsBlock getReceiptsBlockDetailsForYear(@PathVariable Integer id,@PathVariable Integer blockId,
                                                        @RequestParam Integer year) {
        return projectReceiptsService.getProjectReceiptsBlock(id, blockId, year);
    }

    @Secured({Role.OPS_ADMIN, Role.GLA_ORG_ADMIN, Role.GLA_SPM, Role.GLA_PM, Role.ORG_ADMIN, Role.PROJECT_EDITOR})
    @RequestMapping(value = "/projects/{id}/receipts", method = RequestMethod.PUT)
    @ApiOperation(value = "set a project's receipts block", notes = "")
    @ApiResponses(@ApiResponse(code = 400, message = "validation error", response = ApiError.class))
    public ReceiptsBlock updateReceiptsBlock(@PathVariable Integer id,
                                             @RequestParam(name = "autosave", defaultValue = "false", required = false) boolean autosave,
                                             @Valid @RequestBody ReceiptsBlock receiptsBlock,
                                             BindingResult bindingResult) {
        verifyBinding("Invalid Receipts Block details!", bindingResult);
        return projectReceiptsService.updateProjectReceiptsBlock(id, receiptsBlock, autosave);
    }

    @Secured({Role.OPS_ADMIN, Role.GLA_ORG_ADMIN, Role.GLA_SPM, Role.GLA_PM, Role.ORG_ADMIN, Role.PROJECT_EDITOR})
    @RequestMapping(value = "/projects/{id}/receipts", method = RequestMethod.POST)
    @ApiOperation(value = "create receipt entry")
    @ApiResponses(@ApiResponse(code = 400, message = "validation error", response = ApiError.class))
    public ReceiptsBlock addReceiptEntry(@PathVariable Integer id,
                                         @RequestParam Integer year,
                                         @Valid @RequestBody ProjectLedgerItemRequest itemRequest,
                                         BindingResult bindingResult) {
        verifyBinding("Invalid Ledger Details!", bindingResult);
        return projectReceiptsService.addReceiptEntry(id, year, itemRequest);
    }

    @Secured({Role.OPS_ADMIN, Role.GLA_ORG_ADMIN, Role.GLA_SPM, Role.GLA_PM, Role.ORG_ADMIN, Role.PROJECT_EDITOR})
    @RequestMapping(value = "/projects/{projectId}/receipts/{forecastId}", method = RequestMethod.PUT)
    @ApiOperation(value = "edit a receipt forecast value", notes = "")
    @ApiResponses(@ApiResponse(code = 400, message = "validation error", response = ApiError.class))
    public void editReceiptForecast(@PathVariable Integer projectId, @PathVariable Integer forecastId, @RequestBody BigDecimal value) {
        projectReceiptsService.editReceiptForecast(projectId, forecastId, value);
    }

    @Secured({Role.OPS_ADMIN, Role.GLA_ORG_ADMIN, Role.GLA_SPM, Role.GLA_PM, Role.ORG_ADMIN, Role.PROJECT_EDITOR, Role.GLA_FINANCE, Role.GLA_READ_ONLY})
    @RequestMapping(value = "/projects/{projectId}/receiptsMetaData/{blockId}/categoryCode/{categoryId}/yearMonth/{yearMonth}", method = RequestMethod.GET)
    @ApiOperation(value = "get meta data for a receipt", notes = "")
    @ApiResponses(@ApiResponse(code = 400, message = "validation error", response = ApiError.class))
    public List<SAPMetaData> getReceiptsMetaData(@PathVariable Integer projectId, @PathVariable Integer blockId, @PathVariable Integer categoryId, @PathVariable Integer yearMonth) {
        return projectReceiptsService.getReceiptsMetaData(projectId, blockId, categoryId, yearMonth);
    }



}
