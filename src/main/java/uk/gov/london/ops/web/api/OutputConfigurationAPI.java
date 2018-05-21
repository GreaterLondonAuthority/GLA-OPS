/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.web.api;

import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import uk.gov.london.ops.domain.outputs.OutputCategoryConfiguration;
import uk.gov.london.ops.domain.outputs.OutputConfigurationGroup;
import uk.gov.london.ops.domain.outputs.OutputType;
import uk.gov.london.ops.domain.user.Role;
import uk.gov.london.ops.service.OutputConfigurationService;
import uk.gov.london.ops.web.api.project.BaseProjectAPI;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
@Api(description = "managing project output groups")
public class OutputConfigurationAPI extends BaseProjectAPI {

    @Autowired
    private OutputConfigurationService outputConfigurationService;

    @Secured({Role.OPS_ADMIN, Role.GLA_ORG_ADMIN, Role.GLA_SPM, Role.GLA_PM, Role.GLA_FINANCE, Role.GLA_READ_ONLY, Role.ORG_ADMIN, Role.PROJECT_EDITOR})
    @RequestMapping(value = "/outputGroup/{groupId}", method = RequestMethod.GET)
    public OutputConfigurationGroup getOutputGroup(@PathVariable Integer groupId)  {

        return outputConfigurationService.getGroup(groupId);
    }

    @Secured({Role.OPS_ADMIN})
    @RequestMapping(value = "/outputGroup/", method = RequestMethod.POST)
    public OutputConfigurationGroup createOutputGroup(@Valid @RequestBody OutputConfigurationGroup group, BindingResult bindingResult) {
        verifyBinding("Invalid Group!", bindingResult);

        return outputConfigurationService.createOutputConfigurationGroup(group);
    }

    @Secured({Role.OPS_ADMIN})
    @RequestMapping(value = "/outputCategory/", method = RequestMethod.POST)
    public OutputCategoryConfiguration createOutputCategory(@Valid @RequestBody OutputCategoryConfiguration categoryConfiguration, BindingResult bindingResult) {
        verifyBinding("Invalid Category!", bindingResult);

        return outputConfigurationService.createOutputCategory(categoryConfiguration);
    }

    @Secured({Role.OPS_ADMIN})
    @RequestMapping(value = "/outputCategory/", method = RequestMethod.GET)
    public List<OutputCategoryConfiguration> listOutputCategories() {

        return outputConfigurationService.getAllOutputCategories();
    }


    @Secured({Role.OPS_ADMIN})
    @RequestMapping(value = "/outputType/", method = RequestMethod.POST)
    public OutputType createOutputType(@Valid @RequestBody OutputType outputType, BindingResult bindingResult) {
        verifyBinding("Invalid Category!", bindingResult);

        return outputConfigurationService.createOutputType(outputType);
    }

    @Secured({Role.OPS_ADMIN})
    @RequestMapping(value = "/outputType/", method = RequestMethod.GET)
    public List<OutputType> listOutputTypes() {
        return outputConfigurationService.getAllOutputTypes();
    }

}
