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
import uk.gov.london.ops.domain.refdata.Borough;
import uk.gov.london.ops.domain.refdata.CategoryValue;
import uk.gov.london.ops.domain.refdata.TenureType;
import uk.gov.london.ops.domain.user.Role;
import uk.gov.london.ops.exception.ValidationException;
import uk.gov.london.ops.service.RefDataService;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
@Api(description = "managing reference data")
public class RefDataAPI {

    @Autowired
    private RefDataService refDataService;

    @Secured({Role.OPS_ADMIN})
    @RequestMapping(value = "/tenures", method = RequestMethod.GET)
    public List<TenureType> getTenureTypes() {
        return refDataService.getTenureTypes();
    }

    @Secured({Role.OPS_ADMIN})
    @RequestMapping(value = "/tenures/{id}", method = RequestMethod.PUT)
    public void createTenureType(@PathVariable Integer id, @Valid @RequestBody TenureType tenureType, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            throw new ValidationException("Invalid tenure type!", bindingResult.getFieldErrors());
        }
        refDataService.createTenureType(tenureType);
    }

    @Secured({Role.OPS_ADMIN, Role.GLA_PM, Role.GLA_FINANCE, Role.GLA_READ_ONLY, Role.GLA_ORG_ADMIN, Role.GLA_SPM, Role.ORG_ADMIN, Role.PROJECT_EDITOR, Role.TECH_ADMIN})
    @RequestMapping(value = "/categoryValues/{category}", method = RequestMethod.GET)
    public List<CategoryValue> getCategoryValues(@PathVariable CategoryValue.Category category) {
        return refDataService.getCategoryValues(category);
    }

    @Secured({Role.OPS_ADMIN, Role.GLA_PM, Role.GLA_FINANCE, Role.GLA_READ_ONLY, Role.GLA_ORG_ADMIN, Role.GLA_SPM, Role.ORG_ADMIN, Role.PROJECT_EDITOR, Role.TECH_ADMIN})
    @RequestMapping(value = "/boroughs", method = RequestMethod.GET)
    public List<Borough> getBoroughs() {
        return refDataService.getBoroughs();
    }
}
