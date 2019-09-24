/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.refdata;

import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

import static uk.gov.london.common.user.BaseRole.*;
import static uk.gov.london.ops.framework.web.APIUtils.verifyBinding;

@RestController
@RequestMapping("/api/v1")
@Api(description = "managing reference data")
public class RefDataAPI {

    @Autowired
    private RefDataService refDataService;

    @Secured({OPS_ADMIN})
    @RequestMapping(value = "/tenures", method = RequestMethod.GET)
    public List<TenureType> getTenureTypes() {
        return refDataService.getTenureTypes();
    }

    @Secured({OPS_ADMIN})
    @RequestMapping(value = "/tenures/{id}", method = RequestMethod.PUT)
    public void createTenureType(@PathVariable Integer id, @Valid @RequestBody TenureType tenureType, BindingResult bindingResult) {
        verifyBinding("Invalid tenure type!", bindingResult);
        refDataService.createTenureType(tenureType);
    }

    @Secured({OPS_ADMIN, GLA_PM, GLA_FINANCE, GLA_READ_ONLY, GLA_ORG_ADMIN, GLA_SPM, ORG_ADMIN, PROJECT_EDITOR, PROJECT_READER, TECH_ADMIN})
    @RequestMapping(value = "/categoryValues/{category}", method = RequestMethod.GET)
    public List<CategoryValue> getCategoryValues(@PathVariable CategoryValue.Category category) {
        return refDataService.getCategoryValues(category);
    }

    @Secured({OPS_ADMIN, GLA_PM, GLA_FINANCE, GLA_READ_ONLY, GLA_ORG_ADMIN, GLA_SPM, ORG_ADMIN, PROJECT_EDITOR, PROJECT_READER, TECH_ADMIN})
    @RequestMapping(value = "/boroughs", method = RequestMethod.GET)
    public List<Borough> getBoroughs() {
        return refDataService.getBoroughs();
    }

    @Secured({OPS_ADMIN, GLA_PM, GLA_FINANCE, GLA_READ_ONLY, GLA_ORG_ADMIN, GLA_SPM, ORG_ADMIN, PROJECT_EDITOR, PROJECT_READER, TECH_ADMIN})
    @RequestMapping(value = "/marketTypes", method = RequestMethod.GET)
    public List<MarketType> getMarketTypes() {
        return refDataService.getMarketTypes();
    }

    @Secured({OPS_ADMIN, GLA_PM, GLA_FINANCE, GLA_READ_ONLY, GLA_ORG_ADMIN, GLA_SPM, ORG_ADMIN, PROJECT_EDITOR, PROJECT_READER, TECH_ADMIN})
    @RequestMapping(value = "/configItems/{externalId}", method = RequestMethod.GET)
    public List<ConfigurableListItem> getConfigurableListItems(@PathVariable Integer externalId) {
        return refDataService.getConfigurableListItemsByExtID(externalId);
    }

    @Secured({OPS_ADMIN})
    @RequestMapping(value = "/configItems", method = RequestMethod.POST)
    public void createConfigurableListItems(@Valid @RequestBody List<ConfigurableListItem> configItems, BindingResult bindingResult) {
        verifyBinding("Invalid config items!", bindingResult);
        refDataService.createConfigurableListItems(configItems);
    }

}
