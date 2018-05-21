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
import uk.gov.london.ops.domain.project.NamedProjectBlock;
import uk.gov.london.ops.domain.project.Project;
import uk.gov.london.ops.domain.project.ProjectBudgetsBlock;
import uk.gov.london.ops.domain.project.SAPMetaData;
import uk.gov.london.ops.domain.user.Role;
import uk.gov.london.ops.exception.ApiError;
import uk.gov.london.ops.exception.ValidationException;
import uk.gov.london.ops.service.finance.FinanceService;
import uk.gov.london.ops.service.project.ProjectBudgetsService;
import uk.gov.london.ops.web.model.AnnualSpendSummary;
import uk.gov.london.ops.web.model.ProjectLedgerItemRequest;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
@Api(
        description = "managing Project Budgets"
)
public class ProjectBudgetsAPI extends BaseProjectAPI {

    @Autowired
    ProjectBudgetsService projectBudgetsService;

    @Autowired
    FinanceService financeService;

    @Secured({Role.OPS_ADMIN, Role.GLA_ORG_ADMIN, Role.GLA_SPM, Role.GLA_PM, Role.GLA_FINANCE, Role.GLA_READ_ONLY, Role.ORG_ADMIN, Role.PROJECT_EDITOR})
    @RequestMapping(value = "/projects/{id}/projectBudgets/{blockId}", method = RequestMethod.GET)
    @ApiOperation(value = "get a project's project Budgets block, incl table totals", notes = "")
    public ProjectBudgetsBlock getProjectBudgets(@PathVariable Integer id, @PathVariable Integer blockId) {
        // calling this method includes the table calculations
        return projectBudgetsService.getProjectBudgets(service.get(id), blockId);
    }

    @Secured({Role.OPS_ADMIN, Role.GLA_ORG_ADMIN, Role.GLA_SPM, Role.GLA_PM, Role.ORG_ADMIN, Role.PROJECT_EDITOR})
    @RequestMapping(value = "/projects/{id}/projectBudgets", method = RequestMethod.PUT)
    @ApiOperation(value = "set a project's project Budgets details", notes = "")
    @ApiResponses(@ApiResponse(code = 400, message = "validation error", response = ApiError.class))
    public ProjectBudgetsBlock updateProjectBudgetsBlock(@PathVariable Integer id,
                                                     @RequestParam(name = "releaseLock", defaultValue = "false", required = false) boolean releaseLock,
                                                     @Valid @RequestBody ProjectBudgetsBlock projectBudgetsBlock, BindingResult bindingResult) {
        verifyBinding("Invalid Project Budgets Block details!", bindingResult);
        Project fromDB = service.get(id);
        projectBudgetsService.updateProjectBudgets(fromDB, projectBudgetsBlock, releaseLock);
        return projectBudgetsService.getProjectBudgets(service.get(id),fromDB.getProjectBudgetsBlock().getId());
    }


    @Secured({Role.OPS_ADMIN, Role.GLA_ORG_ADMIN, Role.GLA_SPM, Role.GLA_PM, Role.GLA_FINANCE, Role.GLA_READ_ONLY, Role.ORG_ADMIN, Role.PROJECT_EDITOR})
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

    @Secured({Role.OPS_ADMIN, Role.GLA_ORG_ADMIN, Role.GLA_SPM, Role.GLA_PM, Role.ORG_ADMIN, Role.PROJECT_EDITOR})
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

    @Secured({Role.OPS_ADMIN, Role.GLA_ORG_ADMIN, Role.GLA_SPM, Role.GLA_PM, Role.ORG_ADMIN, Role.PROJECT_EDITOR})
    @RequestMapping(value = "/projects/{id}/payments", method = RequestMethod.POST)
    @ApiOperation(value = "creates a payment in the project Budgets block for specific year")
    @ApiResponses(@ApiResponse(code = 400, message = "validation error", response = ApiError.class))
    public AnnualSpendSummary createOrUpdateProjectLedgerEntry(@PathVariable Integer id,
                                                             @RequestParam(defaultValue = "true", required = false) boolean releaseLock,
                                                             @RequestParam(required = false) Integer year,
                                                             @Valid @RequestBody ProjectLedgerItemRequest lineItem,
                                                             BindingResult bindingResult) {
        verifyBinding("Invalid Ledger Details!", bindingResult);
        return service.createLedgerEntry(id, lineItem, year);
    }

    @Secured({Role.OPS_ADMIN, Role.GLA_ORG_ADMIN, Role.GLA_SPM, Role.GLA_PM, Role.ORG_ADMIN, Role.PROJECT_EDITOR})
    @RequestMapping(value = "/projects/{projectId}/blocks/{blockId}/ledgerEntry/{ledgerEntryId}", method = RequestMethod.DELETE)
    @ApiOperation(value = "delete a project's block ledger entry", notes = "")
    @ApiResponses(@ApiResponse(code = 400, message = "validation error", response = ApiError.class))
    public void deleteProjectLedgerEntry(@PathVariable Integer projectId, @PathVariable Integer blockId, @PathVariable Integer ledgerEntryId) {
        Project project = service.get(projectId);

        NamedProjectBlock block = project.getProjectBlockById(blockId);
        service.checkForLock(block);

        financeService.deleteProjectLedgerEntry(ledgerEntryId);
    }

    @Secured({Role.OPS_ADMIN, Role.GLA_ORG_ADMIN, Role.GLA_SPM, Role.GLA_PM, Role.ORG_ADMIN, Role.PROJECT_EDITOR})
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

    @Secured({Role.OPS_ADMIN, Role.GLA_ORG_ADMIN, Role.GLA_SPM, Role.GLA_PM, Role.ORG_ADMIN, Role.PROJECT_EDITOR, Role.GLA_FINANCE, Role.GLA_READ_ONLY})
    @RequestMapping(value = "/projects/{projectId}/projectBudgetsMetaData/{blockId}/categoryCode/{categoryId}/yearMonth/{yearMonth}", method = RequestMethod.GET)
    @ApiOperation(value = "get meta data for a receipt", notes = "")
    @ApiResponses(@ApiResponse(code = 400, message = "validation error", response = ApiError.class))
    public List<SAPMetaData> getPaymentMetaData(@PathVariable Integer projectId, @PathVariable Integer blockId, @PathVariable Integer categoryId, @PathVariable Integer yearMonth) {
        return service.getPaymentMetaData(projectId, blockId, categoryId, yearMonth);
    }
}
