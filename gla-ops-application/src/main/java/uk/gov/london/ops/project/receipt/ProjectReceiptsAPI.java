/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.project.receipt;

import static uk.gov.london.common.user.BaseRole.GLA_FINANCE;
import static uk.gov.london.common.user.BaseRole.GLA_ORG_ADMIN;
import static uk.gov.london.common.user.BaseRole.GLA_PM;
import static uk.gov.london.common.user.BaseRole.GLA_PROGRAMME_ADMIN;
import static uk.gov.london.common.user.BaseRole.GLA_READ_ONLY;
import static uk.gov.london.common.user.BaseRole.GLA_SPM;
import static uk.gov.london.common.user.BaseRole.OPS_ADMIN;
import static uk.gov.london.common.user.BaseRole.ORG_ADMIN;
import static uk.gov.london.common.user.BaseRole.PROJECT_EDITOR;
import static uk.gov.london.common.user.BaseRole.PROJECT_READER;
import static uk.gov.london.ops.framework.OPSUtils.verifyBinding;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.math.BigDecimal;
import java.util.List;
import javax.validation.Valid;
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
import uk.gov.london.ops.payment.ProjectLedgerItemRequest;
import uk.gov.london.ops.project.SAPMetaData;

@RestController
@RequestMapping("/api/v1")
@Api(description = "managing Project receipts block")
public class ProjectReceiptsAPI {

    @Autowired
    ProjectReceiptsService projectReceiptsService;

    @Secured({OPS_ADMIN, GLA_ORG_ADMIN, GLA_SPM, GLA_PM, GLA_FINANCE, GLA_READ_ONLY, ORG_ADMIN, PROJECT_EDITOR, PROJECT_READER, GLA_PROGRAMME_ADMIN})
    @RequestMapping(value = "/projects/{id}/receipts/{blockId}", method = RequestMethod.GET)
    @ApiOperation(value = "get receipts block details for the given year ", notes = "")
    @ApiResponses(@ApiResponse(code = 400, message = "validation error", response = ApiError.class))
    public ReceiptsBlock getReceiptsBlockDetailsForYear(@PathVariable Integer id, @PathVariable Integer blockId,
            @RequestParam Integer year) {
        return projectReceiptsService.getProjectReceiptsBlock(id, blockId, year);
    }

    @Secured({OPS_ADMIN, GLA_ORG_ADMIN, GLA_SPM, GLA_PM, ORG_ADMIN, PROJECT_EDITOR, GLA_PROGRAMME_ADMIN})
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

    @Secured({OPS_ADMIN, GLA_ORG_ADMIN, GLA_SPM, GLA_PM, ORG_ADMIN, PROJECT_EDITOR, GLA_PROGRAMME_ADMIN})
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

    @Secured({OPS_ADMIN, GLA_ORG_ADMIN, GLA_SPM, GLA_PM, ORG_ADMIN, PROJECT_EDITOR, GLA_PROGRAMME_ADMIN})
    @RequestMapping(value = "/projects/{projectId}/receipts/{forecastId}", method = RequestMethod.PUT)
    @ApiOperation(value = "edit a receipt forecast value", notes = "")
    @ApiResponses(@ApiResponse(code = 400, message = "validation error", response = ApiError.class))
    public void editReceiptForecast(@PathVariable Integer projectId, @PathVariable Integer forecastId,
            @RequestBody BigDecimal value) {
        projectReceiptsService.editReceiptForecast(projectId, forecastId, value);
    }

    @Secured({OPS_ADMIN, GLA_ORG_ADMIN, GLA_SPM, GLA_PM, ORG_ADMIN, PROJECT_EDITOR, PROJECT_READER, GLA_FINANCE, GLA_READ_ONLY, GLA_PROGRAMME_ADMIN})
    @RequestMapping(value = "/projects/{projectId}/receiptsMetaData/{blockId}/categoryCode/{categoryId}/yearMonth/{yearMonth}",
            method = RequestMethod.GET)
    @ApiOperation(value = "get meta data for a receipt", notes = "")
    @ApiResponses(@ApiResponse(code = 400, message = "validation error", response = ApiError.class))
    public List<SAPMetaData> getReceiptsMetaData(@PathVariable Integer projectId, @PathVariable Integer blockId,
            @PathVariable Integer categoryId, @PathVariable Integer yearMonth) {
        return projectReceiptsService.getReceiptsMetaData(projectId, blockId, categoryId, yearMonth);
    }


}
