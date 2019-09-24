/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.web.api;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;
import uk.gov.london.ops.domain.DeliveryOverride;
import uk.gov.london.ops.service.OverrideService;
import uk.gov.london.ops.framework.annotations.PermissionRequired;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

import static uk.gov.london.common.user.BaseRole.OPS_ADMIN;
import static uk.gov.london.ops.service.PermissionType.OVERRIDES_MANAGE;

/**
 * Spring MVC controller for the Overrides Information REST endpoint.
 *
 * Created by cmatias on 14/03/2019.
 */
@RestController
@RequestMapping("/api/v1")
@Api(description = "managing override data")
public class OverrideAPI {

    @Autowired
    private OverrideService overrideService;

    @Secured({OPS_ADMIN})
    @RequestMapping(value = "/allOverrides", method = RequestMethod.GET)
    @ApiOperation(value = "get a list of all overrides data",
            notes = "retrieves a list of all overrides information")
    public List<DeliveryOverride> getAllOverrides() {
        return overrideService.getOverrides();

    }

    @Secured({OPS_ADMIN})
    @RequestMapping(value = "/overrides", method = RequestMethod.GET)
    @ApiOperation(value = "get a filtered list of overrides data",
        notes = "retrieves a filtered list of overrides information")
    public List<DeliveryOverride> getOverridesByProjectId(@RequestParam(name = "projectId", required = true) Integer projectId) {
        return overrideService.getOverridesByProjectId(projectId);
    }

    @Secured({OPS_ADMIN})
    @RequestMapping(value = "/overrides/metadata", method = RequestMethod.GET)
    @ApiOperation(value = "get a filtered list of overrides data",
        notes = "retrieves a filtered list of overrides information")
    public Map<String, List<String>> getOverridesMetadata() {
        return overrideService.getOverridesMetadata();
    }

    @Secured({OPS_ADMIN})
    @RequestMapping(value = "/override/{id}", method = RequestMethod.GET)
    public DeliveryOverride get(@PathVariable Integer id) {
        return overrideService.find(id);
    }

    @PermissionRequired(OVERRIDES_MANAGE)
    @RequestMapping(value = "/override", method = RequestMethod.POST)
    @ApiOperation(value="create a new deliveryOverride data", notes="creates a new deliveryOverride and assigns it an ID")
    public DeliveryOverride create(@Valid @RequestBody DeliveryOverride deliveryOverride) {
        return overrideService.create(deliveryOverride);
    }

    @PermissionRequired(OVERRIDES_MANAGE)
    @RequestMapping(value = "/override/{id}", method = RequestMethod.PUT)
    @ApiOperation(value="update an existing override data", notes="updates an existing override by ID")
    public DeliveryOverride update(final @PathVariable Integer id, final @RequestBody DeliveryOverride deliveryOverrideUpdates) {
        return overrideService.update(id, deliveryOverrideUpdates);
    }

    @PermissionRequired(OVERRIDES_MANAGE)
    @RequestMapping(value = "/override/{id}", method = RequestMethod.DELETE)
    @ApiOperation(value="delete an existing override", notes="deletes an existing override by ID")
    public void delete(@PathVariable Integer id) {
        overrideService.deleteOverride(id);
    }
}
