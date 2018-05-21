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
import uk.gov.london.ops.domain.project.OutputTableEntry;
import uk.gov.london.ops.domain.project.OutputsBlock;
import uk.gov.london.ops.domain.user.Role;
import uk.gov.london.ops.service.project.ProjectOutputsService;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/v1")
@Api(
        description = "managing Project outputs"
)
public class ProjectOutputsAPI extends BaseProjectAPI {

    @Autowired
    ProjectOutputsService projectOutputsService;

    @Secured({Role.OPS_ADMIN, Role.GLA_ORG_ADMIN, Role.GLA_SPM, Role.GLA_PM, Role.GLA_FINANCE, Role.GLA_READ_ONLY, Role.ORG_ADMIN, Role.PROJECT_EDITOR})
    @RequestMapping(value = "/projects/{id}/block/{blockId}/outputs/{year}", method = RequestMethod.GET)
    @ApiOperation(value = "get a project's outputs block, by financial year", notes = "")
    public OutputsBlock getOutputs(@PathVariable Integer id, @PathVariable Integer blockId, @PathVariable("year") Integer year) {
        return projectOutputsService.getOutputsForFinancialYear(id, blockId, year);
    }

    @Secured({Role.OPS_ADMIN, Role.GLA_ORG_ADMIN, Role.GLA_SPM, Role.GLA_PM, Role.ORG_ADMIN, Role.PROJECT_EDITOR})
    @RequestMapping(value = "/projects/{id}/outputs", method = RequestMethod.POST)
    @ApiOperation(value = "create (or if necessary update) a project's outputs block, by financial year", notes = "")
    public OutputTableEntry createOutputEntry(@PathVariable Integer id, @Valid @RequestBody OutputTableEntry ote, BindingResult bindingResult) {
        verifyBinding("Invalid outputs details!", bindingResult);
        return  projectOutputsService.createOutputEntry(id, ote);
    }

    @Secured({Role.OPS_ADMIN, Role.GLA_ORG_ADMIN, Role.GLA_SPM, Role.GLA_PM, Role.ORG_ADMIN, Role.PROJECT_EDITOR})
    @RequestMapping(value = "/projects/{id}/outputs", method = RequestMethod.PUT)
    @ApiOperation(value = "update a project's outputs block, by financial year", notes = "")
    public OutputTableEntry updateOutputEntry(@PathVariable Integer id, @Valid @RequestBody OutputTableEntry ote, BindingResult bindingResult) {
        verifyBinding("Invalid outputs details!", bindingResult);
        return projectOutputsService.updateOutputEntry(id, ote);
    }

    @Secured({Role.OPS_ADMIN, Role.GLA_ORG_ADMIN, Role.GLA_SPM, Role.GLA_PM, Role.ORG_ADMIN, Role.PROJECT_EDITOR})
    @RequestMapping(value = "/projects/{id}/outputs/{outputId}", method = RequestMethod.DELETE)
    @ApiOperation(value = "delete a project's outputs block, by financial year", notes = "")
    public void deleteOutput(@PathVariable Integer id, @PathVariable Integer outputId) {
        projectOutputsService.deleteOutputEntry(id,outputId);
    }

}
