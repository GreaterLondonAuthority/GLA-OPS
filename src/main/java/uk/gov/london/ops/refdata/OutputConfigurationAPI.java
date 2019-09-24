/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.refdata;

import static uk.gov.london.common.user.BaseRole.GLA_FINANCE;
import static uk.gov.london.common.user.BaseRole.GLA_ORG_ADMIN;
import static uk.gov.london.common.user.BaseRole.GLA_PM;
import static uk.gov.london.common.user.BaseRole.GLA_READ_ONLY;
import static uk.gov.london.common.user.BaseRole.GLA_SPM;
import static uk.gov.london.common.user.BaseRole.OPS_ADMIN;
import static uk.gov.london.common.user.BaseRole.ORG_ADMIN;
import static uk.gov.london.common.user.BaseRole.PROJECT_EDITOR;
import static uk.gov.london.common.user.BaseRole.PROJECT_READER;
import static uk.gov.london.ops.framework.web.APIUtils.verifyBinding;

import io.swagger.annotations.Api;
import java.util.List;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.london.ops.web.model.RenameRequestDTO;

@RestController
@RequestMapping("/api/v1")
@Api(description = "managing project output groups")
public class OutputConfigurationAPI {

    @Autowired
    private OutputConfigurationService outputConfigurationService;

    @Secured({OPS_ADMIN, GLA_ORG_ADMIN, GLA_SPM, GLA_PM, GLA_FINANCE, GLA_READ_ONLY, ORG_ADMIN, PROJECT_EDITOR, PROJECT_READER})
    @RequestMapping(value = "/outputGroup/{groupId}", method = RequestMethod.GET)
    public OutputConfigurationGroup getOutputGroup(@PathVariable Integer groupId)  {

        return outputConfigurationService.getGroup(groupId);
    }

    @Secured({OPS_ADMIN})
    @RequestMapping(value = "/outputGroup/", method = RequestMethod.POST)
    public OutputConfigurationGroup createOutputGroup(@Valid @RequestBody OutputConfigurationGroup group, BindingResult bindingResult) {
        verifyBinding("Invalid Group!", bindingResult);

        return outputConfigurationService.createOutputConfigurationGroup(group);
    }

    @Secured({OPS_ADMIN})
    @RequestMapping(value = "/outputGroup/{groupId}/outputCategory", method = RequestMethod.POST)
    public OutputConfigurationGroup addCategoryToOutputGroup(@PathVariable Integer groupId, @RequestBody Integer categoryId) {
        return outputConfigurationService.addCategoryToOutputGroup(groupId, categoryId);
    }

    @Secured({OPS_ADMIN})
    @RequestMapping(value = "/outputCategory/", method = RequestMethod.GET)
    public List<OutputCategoryConfiguration> listOutputCategories() {
        return outputConfigurationService.getAllOutputCategories();
    }

    @Secured({OPS_ADMIN})
    @RequestMapping(value = "/outputCategory/{id}", method = RequestMethod.GET)
    public OutputCategoryConfiguration get(@PathVariable Integer id) {
        return outputConfigurationService.getOutputCategory(id);
    }

    @Secured({OPS_ADMIN})
    @RequestMapping(value = "/outputCategory/", method = RequestMethod.POST)
    public OutputCategoryConfiguration createOutputCategory(@Valid @RequestBody OutputCategoryConfiguration categoryConfiguration, BindingResult bindingResult) {
        verifyBinding("Invalid Category!", bindingResult);
        return outputConfigurationService.createOutputCategory(categoryConfiguration);
    }

    @Secured({OPS_ADMIN})
    @RequestMapping(value = "/outputCategory/{groupId}/hide", method = RequestMethod.PUT)
    public OutputCategoryConfiguration hideOutputCategory(@PathVariable Integer groupId, @Valid @RequestBody String hidden) {

        return outputConfigurationService.hideOutputCategory(groupId ,  Boolean.valueOf(hidden));
    }

    @Secured({OPS_ADMIN})
    @RequestMapping(value = "/outputCategory/{id}", method = RequestMethod.PUT)
    public void updateOutputCategory(@PathVariable Integer id, @Valid @RequestBody OutputCategoryConfiguration categoryConfiguration, BindingResult bindingResult) {
        verifyBinding("Invalid Category!", bindingResult);
        outputConfigurationService.updateOutputCategory(categoryConfiguration);
    }

    @Secured({OPS_ADMIN})
    @RequestMapping(value = "/outputGroup/{id}/renameCategory", method = RequestMethod.PUT)
    public void renameOutputCategory(@PathVariable Integer id,
                                     @Valid @RequestBody RenameRequestDTO renameRequestDTO, BindingResult bindingResult) {
        verifyBinding("Invalid Request!", bindingResult);

        outputConfigurationService.updateOutputCategoryName(id, renameRequestDTO.getOldName(), renameRequestDTO.getNewName());
    }

    @Secured({OPS_ADMIN})
    @RequestMapping(value = "/outputType/", method = RequestMethod.POST)
    public OutputType createOutputType(@Valid @RequestBody OutputType outputType, BindingResult bindingResult) {
        verifyBinding("Invalid Category!", bindingResult);

        return outputConfigurationService.createOutputType(outputType);
    }

    @Secured({OPS_ADMIN})
    @RequestMapping(value = "/outputType/", method = RequestMethod.GET)
    public List<OutputType> listOutputTypes() {
        return outputConfigurationService.getAllOutputTypes();
    }

    @Secured({OPS_ADMIN})
    @RequestMapping(value = "/outputConfigurationGroup/", method = RequestMethod.GET)
    public List<OutputConfigurationGroup> listOutputConfigurationGroup() {
        return outputConfigurationService.getAllOutputConfigurationGroup();
    }

}
