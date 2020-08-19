/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.project.unit;

import static uk.gov.london.common.user.BaseRole.GLA_FINANCE;
import static uk.gov.london.common.user.BaseRole.GLA_ORG_ADMIN;
import static uk.gov.london.common.user.BaseRole.GLA_PM;
import static uk.gov.london.common.user.BaseRole.GLA_READ_ONLY;
import static uk.gov.london.common.user.BaseRole.GLA_SPM;
import static uk.gov.london.common.user.BaseRole.OPS_ADMIN;
import static uk.gov.london.common.user.BaseRole.ORG_ADMIN;
import static uk.gov.london.common.user.BaseRole.PROJECT_EDITOR;
import static uk.gov.london.common.user.BaseRole.PROJECT_READER;
import static uk.gov.london.ops.framework.OPSUtils.verifyBinding;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.london.ops.framework.exception.ValidationException;

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
    public UnitDetailsTableEntry createUnitEntry(@PathVariable Integer id, @PathVariable Integer blockId,
            @Valid @RequestBody UnitDetailsTableEntry unitTableEntry, BindingResult bindingResult) {
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
    public UnitDetailsTableEntry updateUnitEntry(@PathVariable Integer projectId, @PathVariable Integer blockId,
            @PathVariable Integer entryId,
            @Valid @RequestBody UnitDetailsTableEntry unitTableEntry, BindingResult bindingResult) {
        verifyBinding("Invalid unit details!", bindingResult);
        return unitDetailsService.updateUnitsEntry(projectId, blockId, entryId, unitTableEntry);
    }

    @Secured({OPS_ADMIN, GLA_ORG_ADMIN, GLA_SPM, GLA_PM, ORG_ADMIN, PROJECT_EDITOR})
    @ApiOperation(value = "Remove a project units table entry", notes = "")
    @RequestMapping(value = "/projects/{id}/units/{blockId}/{rowToDelete}", method = RequestMethod.DELETE)
    public boolean deleteTableEntry(@PathVariable final Integer id,
            @PathVariable final Integer blockId,
            @PathVariable final Integer rowToDelete) {
        if (!unitDetailsService.deleteTableEntry(id, blockId, rowToDelete)) {
            throw new ValidationException(String.format(
                    "Unable to find row with ID %d on block with ID %d",
                    rowToDelete,
                    blockId));
        }
        return true;
    }

}
