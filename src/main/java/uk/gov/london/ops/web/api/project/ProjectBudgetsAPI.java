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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import uk.gov.london.ops.domain.project.NamedProjectBlock;
import uk.gov.london.ops.domain.project.Project;
import uk.gov.london.ops.domain.project.ProjectBudgetsBlock;
import uk.gov.london.ops.domain.project.SAPMetaData;
import uk.gov.london.ops.payment.FinanceService;
import uk.gov.london.ops.service.project.ProjectBudgetsService;
import uk.gov.london.ops.framework.exception.ValidationException;
import uk.gov.london.ops.service.project.ProjectService;
import uk.gov.london.ops.web.model.AnnualSpendSummary;
import uk.gov.london.ops.web.model.ProjectLedgerItemRequest;

import javax.validation.Valid;
import java.util.List;

import static uk.gov.london.common.user.BaseRole.*;
import static uk.gov.london.ops.framework.web.APIUtils.verifyBinding;

@RestController
@RequestMapping("/api/v1")
@Api(
        description = "managing Spend"
)
public class ProjectBudgetsAPI {

    @Autowired
    private ProjectService service;

    @Autowired
    ProjectBudgetsService projectBudgetsService;

    @Autowired
    FinanceService financeService;

    @Secured({OPS_ADMIN, GLA_ORG_ADMIN, GLA_SPM, GLA_PM, GLA_FINANCE, GLA_READ_ONLY, ORG_ADMIN, PROJECT_EDITOR, PROJECT_READER})
    @RequestMapping(value = "/projects/{id}/projectBudgets/{blockId}", method = RequestMethod.GET)
    @ApiOperation(value = "get a project's project Spend block, incl table totals", notes = "")
    public ProjectBudgetsBlock getProjectBudgets(@PathVariable Integer id, @PathVariable Integer blockId) {
        // calling this method includes the table calculations
        return projectBudgetsService.getProjectBudgets(service.get(id), blockId);
    }

    @Secured({OPS_ADMIN, GLA_ORG_ADMIN, GLA_SPM, GLA_PM, ORG_ADMIN, PROJECT_EDITOR})
    @RequestMapping(value = "/projects/{id}/projectBudgets", method = RequestMethod.PUT)
    @ApiOperation(value = "set a project's project Spend details", notes = "")
    @ApiResponses(@ApiResponse(code = 400, message = "validation error", response = ApiError.class))
    public ProjectBudgetsBlock updateProjectBudgetsBlock(@PathVariable Integer id,
                                                     @RequestParam(name = "releaseLock", defaultValue = "false", required = false) boolean releaseLock,
                                                     @Valid @RequestBody ProjectBudgetsBlock projectBudgetsBlock, BindingResult bindingResult) {
        verifyBinding("Invalid Spend Block details!", bindingResult);
        Project fromDB = service.get(id);
        projectBudgetsService.updateProjectBudgets(fromDB, projectBudgetsBlock, releaseLock);
        return projectBudgetsService.getProjectBudgets(service.get(id),fromDB.getProjectBudgetsBlock().getId());
    }


    @Secured({OPS_ADMIN, GLA_ORG_ADMIN, GLA_SPM, GLA_PM, GLA_FINANCE, GLA_READ_ONLY, ORG_ADMIN, PROJECT_EDITOR, PROJECT_READER})
    @RequestMapping(value = "/projects/{id}/{blockId}/annualSpendFor/{year}", method = RequestMethod.GET)
    @ApiOperation(value = "get annual spend details for the given year ", notes = "")
    @ApiResponses(@ApiResponse(code = 400, message = "validation error", response = ApiError.class))
    public AnnualSpendSummary getAnnualSpendDetailsForYear(@PathVariable Integer id,
                                                           @PathVariable Integer blockId,
                                                           @PathVariable Integer year) {
        Project project = service.get(id);

        NamedProjectBlock projectBlockById = project.getProjectBlockById(blockId);

        if (projectBlockById == null || !(projectBlockById instanceof ProjectBudgetsBlock)) {
            throw new ValidationException("Specified block is not an ProjectBudgetsBlock");
        }

        return projectBudgetsService.getAnnualSpendForSpecificYear(projectBlockById.getId(), year);
    }

    @Secured({OPS_ADMIN, GLA_ORG_ADMIN, GLA_SPM, GLA_PM, ORG_ADMIN, PROJECT_EDITOR})
    @RequestMapping(value = "/projects/{id}/{blockId}/annualSpend/{year}", method = RequestMethod.PUT)
    @ApiOperation(value = "create annual spend details for the given year ", notes = "")
    @ApiResponses(@ApiResponse(code = 400, message = "validation error", response = ApiError.class))
    public AnnualSpendSummary createAnnualSpendDetails(@PathVariable Integer id, @PathVariable Integer blockId, @PathVariable Integer year,
                                                     @RequestParam(name = "autosave", defaultValue = "false", required = false) boolean autosave,
                                                     @Valid @RequestBody AnnualSpendSummary spendSummary,
                                                     BindingResult bindingResult) {
        verifyBinding("Invalid Annual Spend Details!", bindingResult);

        Project project = service.get(id);

        NamedProjectBlock projectBlockById = project.getProjectBlockById(blockId);
        if (projectBlockById == null || !(projectBlockById instanceof ProjectBudgetsBlock)) {
            throw new ValidationException("Specified block is not a ProjectBudgets Block");
        }

        return service.updateAnnualSpendAndBudgetLedgerEntries(project,  year,
                spendSummary.getAnnualBudgetRevenue(), spendSummary.getAnnualBudgetCapital(), autosave);
    }

    @Secured({OPS_ADMIN, GLA_ORG_ADMIN, GLA_SPM, GLA_PM, ORG_ADMIN, PROJECT_EDITOR})
    @RequestMapping(value = "/projects/{id}/payments", method = RequestMethod.POST)
    @ApiOperation(value = "creates a payment in the project Spend block for specific year")
    @ApiResponses(@ApiResponse(code = 400, message = "validation error", response = ApiError.class))
    public AnnualSpendSummary createOrUpdateSpendEntry(@PathVariable Integer id,
                                                       @RequestParam(defaultValue = "true", required = false) boolean releaseLock,
                                                       @RequestParam(required = false) Integer year,
                                                       @Valid @RequestBody ProjectLedgerItemRequest lineItem,
                                                       BindingResult bindingResult) {
        verifyBinding("Invalid Ledger Details!", bindingResult);
        return service.createOrUpdateSpendEntry(id, lineItem, year);
    }

    @Secured({OPS_ADMIN, GLA_ORG_ADMIN, GLA_SPM, GLA_PM, GLA_FINANCE, GLA_READ_ONLY, ORG_ADMIN, PROJECT_EDITOR, PROJECT_READER, TECH_ADMIN})
    @RequestMapping(value = "/projects/{projectId}/blocks/{blockId}/ledgerEntries", method = RequestMethod.POST)
    @ApiOperation(value = "creates a project ledger entry", notes = "creates a project ledger entry")
    public void createOrUpdateProjectLedgerEntry(@PathVariable Integer projectId, @PathVariable Integer blockId, @Valid @RequestBody ProjectLedgerItemRequest entry, BindingResult bindingResult) {
        verifyBinding("Invalid Ledger Details!", bindingResult);
        service.createOrUpdateProjectLedgerEntry(projectId, blockId, entry);
    }

    @Secured({OPS_ADMIN, GLA_ORG_ADMIN, GLA_SPM, GLA_PM, ORG_ADMIN, PROJECT_EDITOR})
    @RequestMapping(value = "/projects/{projectId}/blocks/{blockId}/ledgerEntries/{ledgerEntryId}", method = RequestMethod.DELETE)
    @ApiOperation(value = "delete a project's block ledger entry", notes = "")
    @ApiResponses(@ApiResponse(code = 400, message = "validation error", response = ApiError.class))
    public void deleteProjectLedgerEntry(@PathVariable Integer projectId, @PathVariable Integer blockId, @PathVariable Integer ledgerEntryId) {
        Project project = service.get(projectId);

        NamedProjectBlock block = project.getProjectBlockById(blockId);
        service.checkForLock(block);

        financeService.deleteProjectLedgerEntry(ledgerEntryId);
    }

    @Secured({OPS_ADMIN, GLA_ORG_ADMIN, GLA_SPM, GLA_PM, ORG_ADMIN, PROJECT_EDITOR})
    @RequestMapping(value = "/projects/{id}/{blockId}/annualSpendForecast/{year}", method = RequestMethod.DELETE)
    @ApiOperation(value = "delete annual spend details for the given year, sap code and expenditure type ", notes = "")
    @ApiResponses(@ApiResponse(code = 400, message = "validation error", response = ApiError.class))
    public AnnualSpendSummary deleteAnnualSpendRow(@PathVariable Integer id, @PathVariable Integer blockId, @PathVariable Integer year,
                                                 @RequestParam Integer month, @RequestParam("actualYear") Integer actualYear, @RequestParam String entityType, @RequestParam Integer categoryId) {
        Project project = service.get(id);

        NamedProjectBlock block = project.getProjectBlockById(blockId);
        service.checkForLock(block);

        ProjectLedgerItemRequest lineItem = new ProjectLedgerItemRequest();
        lineItem.setBlockId(blockId);
        lineItem.setProjectId(id);
        lineItem.setYear(actualYear);
        lineItem.setMonth(month);
        lineItem.setEntryType(ProjectLedgerItemRequest.LedgerEntryType.valueOf(entityType));
        lineItem.setCategoryId(categoryId);
        financeService.deleteProjectLedgerEntry(project, lineItem);

        return service.getAnnualSpendSummaryForSpecificYear(project, year);


    }

    @Secured({OPS_ADMIN, GLA_ORG_ADMIN, GLA_SPM, GLA_PM, ORG_ADMIN, PROJECT_EDITOR, PROJECT_READER, GLA_FINANCE, GLA_READ_ONLY})
    @RequestMapping(value = "/projects/{projectId}/projectBudgetsMetaData/{blockId}/categoryCode/{categoryId}/yearMonth/{yearMonth}", method = RequestMethod.GET)
    @ApiOperation(value = "get meta data for a receipt", notes = "")
    @ApiResponses(@ApiResponse(code = 400, message = "validation error", response = ApiError.class))
    public List<SAPMetaData> getPaymentMetaData(@PathVariable Integer projectId, @PathVariable Integer blockId, @PathVariable Integer categoryId, @PathVariable Integer yearMonth) {
        return service.getPaymentMetaData(projectId, blockId, categoryId, yearMonth);
    }
}
