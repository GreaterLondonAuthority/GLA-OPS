/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.annualsubmission;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

import static uk.gov.london.common.user.BaseRole.*;
import static uk.gov.london.ops.framework.OPSUtils.verifyBinding;

@RestController
@RequestMapping("/api/v1")
@Api("annual submissions api")
public class AnnualSubmissionAPI {

    @Autowired
    private AnnualSubmissionService annualSubmissionService;

    @Secured({OPS_ADMIN, GLA_ORG_ADMIN, GLA_SPM, GLA_PM, GLA_FINANCE, GLA_READ_ONLY, ORG_ADMIN, PROJECT_EDITOR, PROJECT_READER, TECH_ADMIN})
    @RequestMapping(value = "/annualSubmissionCategories", method = RequestMethod.GET)
    @ApiOperation(value = "gets the list of annual spend categories", notes = "gets the list of annual spend categories")
    public @ResponseBody List<AnnualSubmissionCategory> getCategories() {
        return annualSubmissionService.getCategories();
    }

    @Secured(OPS_ADMIN)
    @RequestMapping(value = "/annualSubmissionCategories/{id}", method = RequestMethod.PUT)
    @ApiOperation(value = "updates an annual spend categories", notes = "updates an annual spend categories")
    public void updateCategory(@PathVariable Integer id, @Valid @RequestBody AnnualSubmissionCategory category) {
        annualSubmissionService.updateCategory(id, category);
    }

    @Secured(OPS_ADMIN)
    @RequestMapping(value = "/annualSubmissionCategories", method = RequestMethod.POST)
    @ApiOperation(value = "creates an annual spend category", notes = "creates an annual spend category")
    public AnnualSubmissionCategory createCategory(@Valid @RequestBody AnnualSubmissionCategory category) {
        return annualSubmissionService.createCategory(category);
    }

    @Secured(OPS_ADMIN)
    @RequestMapping(value = "/annualSubmissionCategories/{id}/hide", method = RequestMethod.PUT)
    @ApiOperation(value = "sets the category hidden from the wizard", notes = "sets the category hidden from the wizard")
    public void setHidden(@PathVariable Integer id, @Valid @RequestBody String hidden) {
        annualSubmissionService.hideCategory(id, Boolean.parseBoolean(hidden));
    }

    @Secured({OPS_ADMIN, GLA_ORG_ADMIN, GLA_SPM, GLA_PM, GLA_FINANCE, ORG_ADMIN, PROJECT_EDITOR})
    @RequestMapping(value = "/availableYearsForCreation", method = RequestMethod.GET)
    @ApiOperation(value = "returns a list of years available for annual submission creation", notes = "returns a list of years available for annual submission creation")
    public List<Integer> getAvailableYearsForCreation(@RequestParam Integer organisationId) {
        return annualSubmissionService.getAvailableYearsForCreation(organisationId);
    }

    @Secured({OPS_ADMIN, GLA_ORG_ADMIN, GLA_SPM, GLA_PM, GLA_FINANCE, GLA_READ_ONLY, ORG_ADMIN, PROJECT_EDITOR, PROJECT_READER, TECH_ADMIN})
    @RequestMapping(value = "/annualSubmissions/{id}", method = RequestMethod.GET)
    @ApiOperation(value = "gets an annual submission", notes = "gets an annual submission")
    public @ResponseBody AnnualSubmission get(@PathVariable Integer id) {
        return annualSubmissionService.get(id);
    }

    @Secured({OPS_ADMIN, GLA_ORG_ADMIN, GLA_SPM, GLA_PM, GLA_FINANCE, ORG_ADMIN, PROJECT_EDITOR})
    @RequestMapping(value = "/annualSubmissions", method = RequestMethod.POST)
    @ApiOperation(value = "creates an annual submission", notes = "creates an annual submission")
    public @ResponseBody AnnualSubmission create(@Valid @RequestBody AnnualSubmission annualSubmission, BindingResult bindingResult) {
        verifyBinding("Invalid annual submission details!", bindingResult);
        return annualSubmissionService.create(annualSubmission);
    }

    @Secured({OPS_ADMIN, GLA_ORG_ADMIN, GLA_SPM, GLA_PM, GLA_FINANCE, ORG_ADMIN, PROJECT_EDITOR})
    @RequestMapping(value = "/annualSubmissions/{id}", method = RequestMethod.PUT)
    @ApiOperation(value = "updates an annual submission block", notes = "updates an annual submission block")
    public void update(@PathVariable Integer id, @RequestBody AnnualSubmission annualSubmission) {
        annualSubmissionService.update(id, annualSubmission);
    }

    @Secured({OPS_ADMIN, GLA_ORG_ADMIN, GLA_SPM, GLA_PM, GLA_FINANCE, ORG_ADMIN, PROJECT_EDITOR})
    @RequestMapping(value = "/annualSubmissions/{submissionId}/status", method = RequestMethod.PUT)
    @ApiOperation(value = "updates an annual submission block status", notes = "updates an annual submission block status")
    public void updateStatus(@PathVariable Integer submissionId, @RequestParam AnnualSubmissionStatus status, @RequestBody(required = false) String agreementText) {
        annualSubmissionService.updateStatus(submissionId, status, agreementText);
    }

    @Secured({OPS_ADMIN, GLA_ORG_ADMIN, GLA_SPM, GLA_PM, GLA_FINANCE, GLA_READ_ONLY, ORG_ADMIN, PROJECT_EDITOR, PROJECT_READER, TECH_ADMIN})
    @RequestMapping(value = "/annualSubmissions/{submissionId}/blocks/{blockId}", method = RequestMethod.GET)
    @ApiOperation(value = "returns an annual submission block", notes = "returns an annual submission block")
    public AnnualSubmissionBlock getBlock(@PathVariable Integer submissionId, @PathVariable Integer blockId) {
        return annualSubmissionService.getBlock(submissionId, blockId);
    }

    @Secured({OPS_ADMIN, GLA_ORG_ADMIN, GLA_SPM, GLA_PM, GLA_FINANCE, ORG_ADMIN, PROJECT_EDITOR})
    @RequestMapping(value = "/annualSubmissions/{submissionId}/blocks/{blockId}", method = RequestMethod.PUT)
    @ApiOperation(value = "updates an annual submission block", notes = "updates an annual submission block")
    public AnnualSubmissionBlock updateBlock(@PathVariable Integer submissionId,
                                             @PathVariable Integer blockId,
                                             @Valid @RequestBody AnnualSubmissionBlock block,
                                             BindingResult bindingResult) {
        verifyBinding("Invalid annual submission block!", bindingResult);
        annualSubmissionService.updateBlock(submissionId, blockId, block);
        return annualSubmissionService.getBlock(submissionId, blockId);
    }

    @Secured({OPS_ADMIN, GLA_ORG_ADMIN, GLA_SPM, GLA_PM, GLA_FINANCE, ORG_ADMIN, PROJECT_EDITOR})
    @RequestMapping(value = "/annualSubmissions/{submissionId}/blocks/{blockId}/entries", method = RequestMethod.POST)
    @ApiOperation(value = "creates an annual submission entry", notes = "creates an annual submission entry")
    public @ResponseBody
    AnnualSubmissionEntry createEntry(@PathVariable Integer submissionId,
                                      @PathVariable Integer blockId,
                                      @Valid @RequestBody AnnualSubmissionEntry entry,
                                      BindingResult bindingResult) {
        verifyBinding("Invalid annual submission entry!", bindingResult);
        return annualSubmissionService.createEntry(submissionId, blockId, entry);
    }

    @Secured({OPS_ADMIN, GLA_ORG_ADMIN, GLA_SPM, GLA_PM, GLA_FINANCE, ORG_ADMIN, PROJECT_EDITOR})
    @RequestMapping(value = "/annualSubmissions/{submissionId}/blocks/{blockId}/entries/{entryId}", method = RequestMethod.PUT)
    @ApiOperation(value = "updates an annual submission entry", notes = "updates an annual submission entry")
    public void updateEntry(@PathVariable Integer submissionId,
                            @PathVariable Integer blockId,
                            @PathVariable Integer entryId,
                            @Valid @RequestBody AnnualSubmissionEntry entry,
                            BindingResult bindingResult) {
        verifyBinding("Invalid annual submission entry!", bindingResult);
        annualSubmissionService.updateEntry(submissionId, blockId, entryId, entry);
    }

    @Secured({OPS_ADMIN, GLA_ORG_ADMIN, GLA_SPM, GLA_PM, GLA_FINANCE, ORG_ADMIN, PROJECT_EDITOR})
    @RequestMapping(value = "/annualSubmissions/{submissionId}/blocks/{blockId}/entries/{entryId}", method = RequestMethod.DELETE)
    @ApiOperation(value = "deletes an annual submission entry", notes = "deletes an annual submission entry")
    public void deleteEntry(@PathVariable Integer submissionId, @PathVariable Integer blockId, @PathVariable Integer entryId) {
        annualSubmissionService.deleteEntry(submissionId, blockId, entryId);
    }

}
