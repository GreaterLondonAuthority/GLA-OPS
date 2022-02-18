/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.refdata;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static uk.gov.london.common.user.BaseRole.GLA_FINANCE;
import static uk.gov.london.common.user.BaseRole.GLA_ORG_ADMIN;
import static uk.gov.london.common.user.BaseRole.GLA_PM;
import static uk.gov.london.common.user.BaseRole.GLA_PROGRAMME_ADMIN;
import static uk.gov.london.common.user.BaseRole.GLA_READ_ONLY;
import static uk.gov.london.common.user.BaseRole.GLA_SPM;
import static uk.gov.london.common.user.BaseRole.OPS_ADMIN;
import static uk.gov.london.common.user.BaseRole.ORG_ADMIN;
import static uk.gov.london.common.user.BaseRole.PROJECT_EDITOR;
import static uk.gov.london.common.user.BaseRole.PROJECT_READER;
import static uk.gov.london.common.user.BaseRole.TECH_ADMIN;
import static uk.gov.london.ops.framework.OPSUtils.verifyBinding;

@RestController
@RequestMapping("/api/v1")
@Api("managing reference data")
public class RefDataAPI {

    @Autowired
    private RefDataServiceImpl refDataService;

    @Autowired
    private FinanceCategoryService financeCategoryService;

    @Autowired
    private OutputConfigurationService outputConfigurationService;

    @Secured({OPS_ADMIN})
    @RequestMapping(value = "/tenures", method = RequestMethod.GET)
    public List<TenureType> getTenureTypes() {
        return refDataService.getTenureTypes();
    }

    @Secured({OPS_ADMIN})
    @RequestMapping(value = "/tenures/{id}", method = RequestMethod.PUT)
    public void createTenureType(@PathVariable Integer id, @Valid @RequestBody TenureType tenureType,
            BindingResult bindingResult) {
        verifyBinding("Invalid tenure type!", bindingResult);
        refDataService.createTenureType(tenureType);
    }

    @Secured({OPS_ADMIN, GLA_PM, GLA_FINANCE, GLA_READ_ONLY, GLA_ORG_ADMIN, GLA_SPM, ORG_ADMIN, PROJECT_EDITOR, PROJECT_READER,
            TECH_ADMIN})
    @RequestMapping(value = "/categoryValues/{category}", method = RequestMethod.GET)
    public List<CategoryValue> getCategoryValues(@PathVariable CategoryValue.Category category) {
        return refDataService.getCategoryValues(category);
    }

    @Secured({OPS_ADMIN, GLA_PM, GLA_FINANCE, GLA_READ_ONLY, GLA_ORG_ADMIN, GLA_PROGRAMME_ADMIN, GLA_SPM, ORG_ADMIN,
            PROJECT_EDITOR, PROJECT_READER, TECH_ADMIN})
    @RequestMapping(value = "/boroughs", method = RequestMethod.GET)
    public List<Borough> getBoroughs() {
        return refDataService.getBoroughs();
    }

    @Secured({OPS_ADMIN, GLA_PM, GLA_FINANCE, GLA_READ_ONLY, GLA_ORG_ADMIN, GLA_SPM, ORG_ADMIN, PROJECT_EDITOR, PROJECT_READER,
            TECH_ADMIN})
    @RequestMapping(value = "/marketTypes", method = RequestMethod.GET)
    public List<MarketType> getMarketTypes() {
        return refDataService.getMarketTypes();
    }

    @Secured({OPS_ADMIN, GLA_PM, GLA_FINANCE, GLA_READ_ONLY, GLA_ORG_ADMIN, GLA_SPM, ORG_ADMIN, PROJECT_EDITOR, PROJECT_READER,
            TECH_ADMIN})
    @RequestMapping(value = "/configItemsGroups", method = RequestMethod.GET)
    public List<ConfigurableListItemGroup> getConfigurableItemsGroups(@RequestParam(required = false) String type,
            @RequestParam(name = "category", required = false) String categoryName) {
        return refDataService.getConfigurableItemsGroups(type, categoryName);
    }

    @Secured({OPS_ADMIN, GLA_PM, GLA_FINANCE, GLA_READ_ONLY, GLA_ORG_ADMIN, GLA_SPM, ORG_ADMIN, PROJECT_EDITOR, PROJECT_READER,
            TECH_ADMIN})
    @RequestMapping(value = "/configItems", method = RequestMethod.GET)
    public Map<Integer, List<ConfigurableListItemEntity>> getConfigurableGroupIds(@RequestParam(required = false) String type) {
        return refDataService.getConfigurableGroupIds();
    }

    @Secured({OPS_ADMIN, GLA_PM, GLA_FINANCE, GLA_READ_ONLY, GLA_ORG_ADMIN, GLA_SPM, ORG_ADMIN, PROJECT_EDITOR, PROJECT_READER,
            TECH_ADMIN})
    @RequestMapping(value = "/configItems/{externalId}", method = RequestMethod.GET)
    public List<ConfigurableListItem> getConfigurableListItems(@PathVariable Integer externalId) {
        return refDataService.getConfigurableListItemsByExtID(externalId);
    }

    @Secured({OPS_ADMIN})
    @RequestMapping(value = "/configItems/{externalId}/{categoryId}", method = RequestMethod.DELETE)
    public void deleteConfigurableListItem(@PathVariable Integer externalId, @PathVariable Integer categoryId) {
        refDataService.deleteConfigurableListItem(externalId, categoryId);
    }

    @Secured({OPS_ADMIN})
    @RequestMapping(value = "/configItems/{externalId}", method = RequestMethod.DELETE)
    public void deleteConfigurableListItemGroup(@PathVariable Integer externalId) {
        refDataService.deleteConfigurableListItemGroup(externalId);
    }

    @Secured({OPS_ADMIN, GLA_PM, GLA_FINANCE, GLA_READ_ONLY, GLA_ORG_ADMIN, GLA_SPM, ORG_ADMIN, PROJECT_EDITOR, PROJECT_READER,
            TECH_ADMIN})
    @RequestMapping(value = "/paymentSources", method = RequestMethod.GET)
    public Set<PaymentSourceEntity> getAvailablePaymentSources() {
        return refDataService.getAvailablePaymentSources();
    }

    @Secured({OPS_ADMIN})
    @RequestMapping(value = "/paymentSources", method = RequestMethod.POST)
    public PaymentSourceEntity createPaymentSource(@Valid @RequestBody PaymentSourceEntity paymentSource) {
        return refDataService.createPaymentSource(paymentSource);
    }

    @Secured({OPS_ADMIN})
    @RequestMapping(value = "/configItems", method = RequestMethod.POST)
    public void createConfigurableListItems(@Valid @RequestBody List<ConfigurableListItemEntity> configItems,
            BindingResult bindingResult) {
        verifyBinding("Invalid config items!", bindingResult);
        refDataService.createConfigurableListItems(configItems);
    }

    @Secured({OPS_ADMIN})
    @RequestMapping(value = "/configItems/{id}", method = RequestMethod.PUT)
    public void updateConfigurableListItem(@PathVariable Integer id,
                                           @Valid @RequestBody ConfigurableListItemEntity configItems,
                                           BindingResult bindingResult) {
        verifyBinding("Invalid config items!", bindingResult);
        refDataService.updateConfigurableListItem(id, configItems);
    }

    @Secured({OPS_ADMIN, GLA_ORG_ADMIN, GLA_SPM, GLA_PM, GLA_FINANCE, GLA_READ_ONLY, ORG_ADMIN, PROJECT_EDITOR, PROJECT_READER})
    @RequestMapping(value = "/outputGroup/{groupId}", method = RequestMethod.GET)
    public OutputConfigurationGroup getOutputGroup(@PathVariable Integer groupId) {

        return outputConfigurationService.getGroup(groupId);
    }

    @Secured({OPS_ADMIN})
    @RequestMapping(value = "/outputGroup/", method = RequestMethod.POST)
    public OutputConfigurationGroup createOutputGroup(@Valid @RequestBody OutputConfigurationGroup group,
            BindingResult bindingResult) {
        verifyBinding("Invalid Group!", bindingResult);

        return outputConfigurationService.createOutputConfigurationGroup(group);
    }

    @Secured({OPS_ADMIN})
    @RequestMapping(value = "/outputGroup/{groupId}/outputCategory", method = RequestMethod.POST)
    public OutputConfigurationGroup addCategoryToOutputGroup(@PathVariable Integer groupId, @RequestBody Integer categoryId) {
        return outputConfigurationService.addCategoryToOutputGroup(groupId, categoryId);
    }

    @Secured({OPS_ADMIN})
    @RequestMapping(value = "/outputCategory", method = RequestMethod.GET)
    public List<OutputCategoryConfigurationSummary> listOutputCategories(
            @RequestParam(name = "category", required = false) String categoryName) {
        return outputConfigurationService.getAllOutputCategories(categoryName);
    }

    @Secured({OPS_ADMIN})
    @RequestMapping(value = "/outputCategory/{id}", method = RequestMethod.GET)
    public OutputCategoryConfiguration get(@PathVariable Integer id) {
        return outputConfigurationService.getOutputCategory(id);
    }

    @Secured({OPS_ADMIN})
    @RequestMapping(value = "/outputCategory", method = RequestMethod.POST)
    public OutputCategoryConfiguration createOutputCategory(
            @RequestParam(name = "groupId", required = false) Integer groupId,
            @Valid @RequestBody OutputCategoryConfiguration categoryConfiguration,
            BindingResult bindingResult) {
        verifyBinding("Invalid Category!", bindingResult);

        return outputConfigurationService.createOutputCategoryConfiguration(groupId, categoryConfiguration);

    }

    @Secured({OPS_ADMIN})
    @RequestMapping(value = "/outputCategory/{categoryId}/hide", method = RequestMethod.PUT)
    public OutputCategoryConfiguration hideOutputCategory(@PathVariable Integer categoryId, @Valid @RequestBody String hidden) {
        return outputConfigurationService.hideOutputCategory(categoryId, Boolean.parseBoolean(hidden));
    }

    @Secured({OPS_ADMIN})
    @RequestMapping(value = "/outputCategory/{id}", method = RequestMethod.PUT)
    public void updateOutputCategory(@PathVariable Integer id,
            @Valid @RequestBody OutputCategoryConfiguration categoryConfiguration, BindingResult bindingResult) {
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

    @Secured({OPS_ADMIN, GLA_ORG_ADMIN, GLA_SPM, GLA_PM, GLA_FINANCE, GLA_READ_ONLY, ORG_ADMIN, PROJECT_EDITOR, PROJECT_READER,
            TECH_ADMIN, GLA_PROGRAMME_ADMIN})
    @RequestMapping(value = "/finance/categories", method = RequestMethod.GET)
    @ApiOperation(value = "get the finance categories")
    public List<FinanceCategoryEntity> getFinanceCategories(
            @RequestParam(name = "category", required = false) String categoryName) {
        return financeCategoryService.getFinanceCategories(categoryName);
    }

    @Secured({OPS_ADMIN, TECH_ADMIN})
    @RequestMapping(value = "/finance/categories", method = RequestMethod.POST)
    @ApiOperation(value = "create new finance category")
    public FinanceCategoryEntity createFinanceCategory(@RequestBody FinanceCategoryEntity category) {
        return financeCategoryService.createFinanceCategory(category);
    }

    @Secured({OPS_ADMIN, TECH_ADMIN})
    @RequestMapping(value = "/finance/categories/{id}", method = RequestMethod.PUT)
    @ApiOperation(value = "edit an existing finance category")
    public FinanceCategoryEntity editFinanceCategory(@PathVariable Integer id, @RequestBody FinanceCategoryEntity category) {
        return financeCategoryService.updateFinanceCategory(id, category);
    }

}
