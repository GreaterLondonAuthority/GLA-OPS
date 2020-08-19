/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.project.grant;

import static uk.gov.london.common.user.BaseRole.GLA_ORG_ADMIN;
import static uk.gov.london.common.user.BaseRole.GLA_PM;
import static uk.gov.london.common.user.BaseRole.GLA_SPM;
import static uk.gov.london.common.user.BaseRole.OPS_ADMIN;
import static uk.gov.london.common.user.BaseRole.ORG_ADMIN;
import static uk.gov.london.common.user.BaseRole.PROJECT_EDITOR;
import static uk.gov.london.ops.framework.OPSUtils.verifyBinding;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
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
import uk.gov.london.ops.framework.exception.ValidationException;
import uk.gov.london.ops.project.Project;
import uk.gov.london.ops.project.ProjectService;

@RestController
@RequestMapping("/api/v1")
@Api(
        description = "managing Project grant blocks"
)
public class ProjectGrantAPI {

    @Autowired
    private ProjectService service;

    @Autowired
    ProjectGrantService projectGrantService;

    @Secured({OPS_ADMIN, GLA_ORG_ADMIN, GLA_SPM, GLA_PM, ORG_ADMIN, PROJECT_EDITOR})
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

    @Secured({OPS_ADMIN, GLA_ORG_ADMIN, GLA_SPM, GLA_PM, ORG_ADMIN, PROJECT_EDITOR})
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

    @Secured({OPS_ADMIN, GLA_ORG_ADMIN, GLA_SPM, GLA_PM, ORG_ADMIN, PROJECT_EDITOR})
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

    @Secured({OPS_ADMIN, GLA_ORG_ADMIN, GLA_SPM, GLA_PM, ORG_ADMIN, PROJECT_EDITOR})
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
            for (ProjectTenureDetails projectTenureDetails : tenure.getTenureTypeAndUnitsEntries()) {
                if (projectTenureDetails.getTenureType() == null || projectTenureDetails.getTenureType().getId() == null) {
                    throw new ValidationException("A valid Tenure Type must be supplied for tenure entries");
                }
            }
        }
    }

}
