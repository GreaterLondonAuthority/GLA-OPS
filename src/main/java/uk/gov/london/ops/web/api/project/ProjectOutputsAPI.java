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
import uk.gov.london.ops.domain.project.OutputCategoryAssumption;
import uk.gov.london.ops.domain.project.OutputTableEntry;
import uk.gov.london.ops.domain.project.OutputsBlock;
import uk.gov.london.ops.service.project.ProjectOutputsService;

import javax.validation.Valid;
import java.util.Set;

import static uk.gov.london.common.user.BaseRole.*;
import static uk.gov.london.ops.framework.web.APIUtils.verifyBinding;

@RestController
@RequestMapping("/api/v1")
@Api(
        description = "managing Project outputs"
)
public class ProjectOutputsAPI {

    @Autowired
    ProjectOutputsService projectOutputsService;

    @Secured({OPS_ADMIN, GLA_ORG_ADMIN, GLA_SPM, GLA_PM, GLA_FINANCE, GLA_READ_ONLY, ORG_ADMIN, PROJECT_EDITOR, PROJECT_READER})
    @RequestMapping(value = "/projects/{id}/block/{blockId}/outputs/{year}", method = RequestMethod.GET)
    @ApiOperation(value = "get a project's outputs block, by financial year", notes = "")
    public OutputsBlock getOutputs(@PathVariable Integer id, @PathVariable Integer blockId, @PathVariable("year") Integer year) {
        return projectOutputsService.getOutputsForFinancialYear(id, blockId, year);
    }

    @Secured({OPS_ADMIN, GLA_ORG_ADMIN, GLA_SPM, GLA_PM, GLA_FINANCE, GLA_READ_ONLY, ORG_ADMIN, PROJECT_EDITOR, PROJECT_READER})
    @RequestMapping(value = "/projects/{id}/block/{blockId}/baselines", method = RequestMethod.GET)
    @ApiOperation(value = "get a project's outputs baseline data", notes = "")
    public Set<OutputTableEntry> getBaselineOutputs(@PathVariable Integer id, @PathVariable Integer blockId) {
        return projectOutputsService.getOutputsForBaseline(id, blockId);
    }

    @Secured({OPS_ADMIN, GLA_ORG_ADMIN, GLA_SPM, GLA_PM, GLA_FINANCE, GLA_READ_ONLY, ORG_ADMIN, PROJECT_EDITOR, PROJECT_READER})
    @RequestMapping(value = "/projects/{id}/block/{blockId}/assumptions/year/{year}", method = RequestMethod.GET)
    @ApiOperation(value = "get a project's output assumptions", notes = "")
    public Set<OutputCategoryAssumption> getAssumptions(@PathVariable Integer id, @PathVariable Integer blockId, @PathVariable Integer year) {
        return projectOutputsService.getOutputAssumptions(blockId, year);
    }

    @Secured({OPS_ADMIN, GLA_ORG_ADMIN, GLA_SPM, GLA_PM, GLA_FINANCE, ORG_ADMIN, PROJECT_EDITOR})
    @RequestMapping(value = "/projects/{id}/block/{blockId}/assumptions", method = RequestMethod.POST)
    @ApiOperation(value = "add a project output assumptions", notes = "")
    public void addAssumption(@PathVariable Integer id, @PathVariable Integer blockId, @Valid @RequestBody OutputCategoryAssumption assumption, BindingResult bindingResult) {
        verifyBinding("Invalid assumption details!", bindingResult);

        projectOutputsService.addAssumption(id, blockId, assumption);
    }

    @Secured({OPS_ADMIN, GLA_ORG_ADMIN, GLA_SPM, GLA_PM, GLA_FINANCE, ORG_ADMIN, PROJECT_EDITOR})
    @RequestMapping(value = "/projects/{id}/block/{blockId}/assumptions/{assumptionID}", method = RequestMethod.DELETE)
    @ApiOperation(value = "delete a project output assumption", notes = "")
    public void deleteAssumption(@PathVariable Integer id, @PathVariable Integer blockId, @PathVariable Integer assumptionID) {
        projectOutputsService.deleteAssumption(id, blockId, assumptionID);
    }

    @Secured({OPS_ADMIN, GLA_ORG_ADMIN, GLA_SPM, GLA_PM, GLA_FINANCE, ORG_ADMIN, PROJECT_EDITOR})
    @RequestMapping(value = "/projects/{id}/block/{blockId}/assumptions/{assumptionID}", method = RequestMethod.PUT)
    @ApiOperation(value = "update a project output assumptions", notes = "")
    public void updateAssumption(@PathVariable Integer id, @PathVariable Integer blockId, @Valid @RequestBody OutputCategoryAssumption assumption, BindingResult bindingResult) {
        verifyBinding("Invalid assumption details!", bindingResult);

        projectOutputsService.updateAssumption(id, blockId, assumption);
    }

    @Secured({OPS_ADMIN, GLA_ORG_ADMIN, GLA_SPM, GLA_PM, ORG_ADMIN, PROJECT_EDITOR})
    @RequestMapping(value = "/projects/{id}/outputs", method = RequestMethod.POST)
    @ApiOperation(value = "create (or if necessary update) a project's outputs block, by financial year", notes = "")
    public OutputTableEntry createOutputEntry(@PathVariable Integer id, @Valid @RequestBody OutputTableEntry ote, BindingResult bindingResult) {
        verifyBinding("Invalid outputs details!", bindingResult);
        return  projectOutputsService.createOutputEntry(id, ote);
    }

    @Secured({OPS_ADMIN, GLA_ORG_ADMIN, GLA_SPM, GLA_PM, ORG_ADMIN, PROJECT_EDITOR})
    @RequestMapping(value = "/projects/{id}/outputs", method = RequestMethod.PUT)
    @ApiOperation(value = "update a project's outputs block, by financial year", notes = "")
    public OutputTableEntry updateOutputEntry(@PathVariable Integer id, @Valid @RequestBody OutputTableEntry ote, BindingResult bindingResult) {
        verifyBinding("Invalid outputs details!", bindingResult);
        return projectOutputsService.updateOutputEntry(id, ote);
    }

    @Secured({OPS_ADMIN, GLA_ORG_ADMIN, GLA_SPM, GLA_PM, ORG_ADMIN, PROJECT_EDITOR})
    @RequestMapping(value = "/projects/{id}/outputs/{outputId}", method = RequestMethod.DELETE)
    @ApiOperation(value = "delete a project's outputs block, by financial year", notes = "")
    public void deleteOutput(@PathVariable Integer id, @PathVariable Integer outputId) {
        projectOutputsService.deleteOutputEntry(id,outputId);
    }
}
