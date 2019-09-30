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
import uk.gov.london.ops.domain.project.UnitDetailsTableEntry;
import uk.gov.london.ops.service.project.UnitDetailsService;
import uk.gov.london.ops.framework.exception.ValidationException;
import uk.gov.london.ops.web.model.project.UnitDetailsMetaData;

import javax.validation.Valid;

import static uk.gov.london.common.user.BaseRole.*;
import static uk.gov.london.ops.framework.web.APIUtils.verifyBinding;

@RestController
@RequestMapping("/api/v1")
@Api(
        description = "managing Project units details"
)
public class UnitDetailsAPI {

    @Autowired
    UnitDetailsService unitDetailsService;

    @Secured({OPS_ADMIN, GLA_ORG_ADMIN, GLA_SPM, GLA_PM, ORG_ADMIN, PROJECT_EDITOR})
    @RequestMapping(value = "/projects/{id}/units/{blockId}", method = RequestMethod.POST)
    @ApiOperation(value = "create a project units table entry", notes = "")
    public UnitDetailsTableEntry createUnitEntry(@PathVariable Integer id,@PathVariable Integer blockId, @Valid @RequestBody UnitDetailsTableEntry unitTableEntry, BindingResult bindingResult) {
        verifyBinding("Invalid unit details!", bindingResult);
        return unitDetailsService.createUnitsEntry(id, blockId, unitTableEntry);
    }

    @Secured({OPS_ADMIN, GLA_ORG_ADMIN, GLA_SPM, GLA_PM, GLA_FINANCE, GLA_READ_ONLY, ORG_ADMIN, PROJECT_EDITOR, PROJECT_READER})
    @RequestMapping(value = "/projects/{id}/units/metadata", method = RequestMethod.GET)
    @ApiOperation(value = "retrieves meta data for the ui to create the wizard", notes = "")
    public UnitDetailsMetaData getTenureWizardDetails(@PathVariable Integer id) {
        return unitDetailsService.getUnitDetailsMetaDataForProject(id);
    }

    @Secured({OPS_ADMIN, GLA_ORG_ADMIN, GLA_SPM, GLA_PM, ORG_ADMIN, PROJECT_EDITOR})
    @RequestMapping(value = "/projects/{projectId}/units/{blockId}/entries/{entryId}", method = RequestMethod.PUT)
    @ApiOperation(value = "create a project units table entry", notes = "")
    public UnitDetailsTableEntry updateUnitEntry(@PathVariable Integer projectId, @PathVariable Integer blockId, @PathVariable Integer entryId,
                                                 @Valid @RequestBody UnitDetailsTableEntry unitTableEntry, BindingResult bindingResult) {
        verifyBinding("Invalid unit details!", bindingResult);
        return unitDetailsService.updateUnitsEntry(projectId, blockId, entryId,  unitTableEntry);
    }

    @Secured({OPS_ADMIN, GLA_ORG_ADMIN, GLA_SPM, GLA_PM, ORG_ADMIN, PROJECT_EDITOR})
    @ApiOperation(value = "Remove a project units table entry", notes = "")
    @RequestMapping(value = "/projects/{id}/units/{blockId}/{rowToDelete}", method = RequestMethod.DELETE)
    public boolean deleteTableEntry(@PathVariable final  Integer id,
                                    @PathVariable final Integer blockId,
                                    @PathVariable final Integer rowToDelete) {
        if(!unitDetailsService.deleteTableEntry(id, blockId, rowToDelete)) {
            throw new ValidationException(String.format(
                    "Unable to find row with ID %d on block with ID %d",
                    rowToDelete,
                    blockId));
        }
        return true;
    }

}
