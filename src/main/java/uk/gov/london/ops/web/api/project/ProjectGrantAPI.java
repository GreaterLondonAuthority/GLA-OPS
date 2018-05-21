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
import uk.gov.london.ops.domain.project.*;
import uk.gov.london.ops.domain.user.Role;
import uk.gov.london.ops.exception.ApiError;
import uk.gov.london.ops.exception.ValidationException;
import uk.gov.london.ops.service.project.ProjectGrantService;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/v1")
@Api(
        description = "managing Project grant blocks"
)
public class ProjectGrantAPI extends BaseProjectAPI {

    @Autowired
    ProjectGrantService projectGrantService;

    @Secured({Role.OPS_ADMIN, Role.GLA_ORG_ADMIN, Role.GLA_SPM, Role.GLA_PM, Role.ORG_ADMIN, Role.PROJECT_EDITOR})
    @RequestMapping(value = "/projects/{id}/developerLedGrant", method = RequestMethod.PUT)
    @ApiOperation(value = "set a project's developer-leg grant", notes = "")
    @ApiResponses(@ApiResponse(code = 400, message = "validation error", response = ApiError.class))
    public BaseGrantBlock updateProjectDeveloperLedGrantBlock(@PathVariable Integer id,
                                                              @RequestParam(name = "autosave", defaultValue = "false", required = false) boolean autosave,
                                                              @Valid @RequestBody DeveloperLedGrantBlock tenure,
                                                              BindingResult bindingResult) {
        verifyBinding("Invalid tenure details!", bindingResult);
        Project fromDB = service.get(id);
        validateTenure(tenure, fromDB);
        projectGrantService.updateProjectDeveloperLedGrant(fromDB, tenure, autosave);
        return fromDB.getDeveloperLedGrantBlock();
    }

    @Secured({Role.OPS_ADMIN, Role.GLA_ORG_ADMIN, Role.GLA_SPM, Role.GLA_PM, Role.ORG_ADMIN, Role.PROJECT_EDITOR})
    @RequestMapping(value = "/projects/{id}/indicativeGrant", method = RequestMethod.PUT)
    @ApiOperation(value = "set a project's tenure details for S106", notes = "")
    @ApiResponses(@ApiResponse(code = 400, message = "validation error", response = ApiError.class))
    public IndicativeGrantBlock updateProjectIndicativeGrantBlock(@PathVariable Integer id,
                                                                  @RequestParam(name = "autosave", defaultValue = "false", required = false) boolean autosave,
                                                                  @Valid @RequestBody IndicativeGrantBlock tenure, BindingResult bindingResult) {
        verifyBinding("Invalid tenure details!", bindingResult);
        Project fromDB = service.get(id);
        validateTenure(tenure, fromDB);
        projectGrantService.updateProjectIndicativeGrant(fromDB, tenure, autosave);
        return fromDB.getIndicativeGrantBlock();
    }

    @Secured({Role.OPS_ADMIN, Role.GLA_ORG_ADMIN, Role.GLA_SPM, Role.GLA_PM, Role.ORG_ADMIN, Role.PROJECT_EDITOR})
    @RequestMapping(value = "/projects/{id}/calculateGrant", method = RequestMethod.PUT)
    @ApiOperation(value = "set a project's tenure details", notes = "")
    @ApiResponses(@ApiResponse(code = 400, message = "validation error", response = ApiError.class))
    public CalculateGrantBlock updateProjectCalculateGrantBlock(@PathVariable Integer id,
                                                                @RequestParam(name = "autosave", defaultValue = "false", required = false) boolean autosave,
                                                                @Valid @RequestBody CalculateGrantBlock tenure, BindingResult bindingResult) {
        verifyBinding("Invalid tenure details!", bindingResult);
        Project fromDB = service.get(id);
        validateTenure(tenure, fromDB);
        projectGrantService.updateProjectCalculateGrant(fromDB, tenure, autosave);
        return fromDB.getCalculateGrantBlock();
    }

    @Secured({Role.OPS_ADMIN, Role.GLA_ORG_ADMIN, Role.GLA_SPM, Role.GLA_PM, Role.ORG_ADMIN, Role.PROJECT_EDITOR})
    @RequestMapping(value = "/projects/{id}/negotiatedGrant", method = RequestMethod.PUT)
    @ApiOperation(value = "set a project's tenure details", notes = "")
    @ApiResponses(@ApiResponse(code = 400, message = "validation error", response = ApiError.class))
    public NegotiatedGrantBlock updateProjectNegotiatedGrantBlock(@PathVariable Integer id,
                                                                  @RequestParam(name = "autosave", defaultValue = "false", required = false) boolean autosave,
                                                                  @Valid @RequestBody NegotiatedGrantBlock tenure, BindingResult bindingResult) {
        verifyBinding("Invalid tenure details!", bindingResult);
        Project fromDB = service.get(id);
        validateTenure(tenure, fromDB);
        projectGrantService.updateProjectNegotiatedGrant(fromDB, tenure, autosave);
        return fromDB.getNegotiatedGrantBlock();
    }

    private void validateTenure(BaseGrantBlock tenure, Project project) {
        if (project.getCalculateGrantBlock() != null && tenure instanceof CalculateGrantBlock) {
            if (!project.getCalculateGrantBlock().getId().equals(tenure.getId())) {
                throw new ValidationException("Must update Tenure with the correct ID.");
            }
        }
        if (tenure.getTenureTypeAndUnitsEntries() != null) {
            for (TenureTypeAndUnits tenureTypeAndUnits : tenure.getTenureTypeAndUnitsEntries()) {
                if (tenureTypeAndUnits.getTenureType() == null || tenureTypeAndUnits.getTenureType().getId() == null) {
                    throw new ValidationException("A valid Tenure Type must be supplied for tenure entries");
                }
            }
        }
    }

}
