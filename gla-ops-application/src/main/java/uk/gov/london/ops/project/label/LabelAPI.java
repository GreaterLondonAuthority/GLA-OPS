/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.project.label;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;
import uk.gov.london.ops.framework.annotations.PermissionRequired;

import javax.validation.Valid;
import java.util.List;

import static uk.gov.london.common.user.BaseRole.*;
import static uk.gov.london.ops.permission.PermissionType.LABELS_MANAGE;

/**
 * Spring MVC controller for the Pre-set Labels REST endpoint.
 *
 * Created by cmatias on 11/01/2019.
 */
@RestController
@RequestMapping("/api/v1")
@Api
public class LabelAPI {

    @Autowired
    private LabelServiceImpl labelService;

    @Secured({OPS_ADMIN, GLA_ORG_ADMIN, GLA_SPM, GLA_PM, GLA_FINANCE, GLA_PROGRAMME_ADMIN, GLA_READ_ONLY, ORG_ADMIN, PROJECT_EDITOR,
            PROJECT_READER,
            TECH_ADMIN})
    @RequestMapping(value = "/preSetLabels", method = RequestMethod.GET)
    @ApiOperation(value = "get a filtered list of pre-set labels data",
            notes = "retrieves a filtered list of pre-sets labels")
    public List<PreSetLabelEntity> getAllPreSetLabels(
            @RequestParam(name = "managingOrganisationId", required = false) Integer managingOrganisationId,
            @RequestParam(name = "markedForCorporate", required = false) boolean markedForCorporate) {
        return labelService.getPreSetLabels(managingOrganisationId, markedForCorporate);
    }

    @Secured({OPS_ADMIN, GLA_ORG_ADMIN, GLA_SPM, GLA_PM, GLA_FINANCE, GLA_READ_ONLY, ORG_ADMIN, PROJECT_EDITOR, PROJECT_READER,
            TECH_ADMIN})
    @RequestMapping(value = "/preSetLabels/{id}", method = RequestMethod.GET)
    public PreSetLabelEntity get(@PathVariable Integer id) {
        return labelService.find(id);
    }

    @PermissionRequired(LABELS_MANAGE)
    @RequestMapping(value = "/preSetLabels", method = RequestMethod.POST)
    @ApiOperation(value = "create a new pre-set label", notes = "creates a new pre-set label and assigns it an ID")
    public PreSetLabelEntity create(@Valid @RequestBody PreSetLabelEntity preSetLabel) {
        return labelService.create(preSetLabel);
    }

    @PermissionRequired(LABELS_MANAGE)
    @RequestMapping(value = "/preSetLabels/{id}", method = RequestMethod.PUT)
    @ApiOperation(value = "update an existing pre-set label", notes = "updates an existing pre-set label by ID")
    public PreSetLabelEntity update(final @PathVariable Integer id, final @RequestBody PreSetLabelEntity preSetLabel) {
        return labelService.update(id, preSetLabel);
    }

    @PermissionRequired(LABELS_MANAGE)
    @RequestMapping(value = "/preSetLabels/{id}", method = RequestMethod.DELETE)
    @ApiOperation(value = "delete an existing pre-set label", notes = "deletes an existing pre-set label by ID")
    public void delete(@PathVariable Integer id) {
        labelService.deletePreSetLabel(id);
    }
}
