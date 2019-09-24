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
import uk.gov.london.ops.domain.project.Project;
import uk.gov.london.ops.domain.project.subcontracting.Deliverable;
import uk.gov.london.ops.domain.project.subcontracting.DeliverableFeeCalculation;
import uk.gov.london.ops.domain.project.subcontracting.SubcontractingBlock;
import uk.gov.london.ops.domain.project.subcontracting.Subcontractor;
import uk.gov.london.ops.service.project.ProjectService;
import uk.gov.london.ops.service.project.ProjectSubcontractorsService;
import uk.gov.london.common.error.ApiError;
import uk.gov.london.ops.framework.exception.ValidationException;

import javax.validation.Valid;
import java.math.BigDecimal;

import static uk.gov.london.common.user.BaseRole.*;
import static uk.gov.london.ops.framework.web.APIUtils.verifyBinding;

@RestController
@RequestMapping("/api/v1")
@Api(
        description = "managing Project skills data"
)
public class ProjectSubcontractorsAPI {

    @Autowired
    private ProjectService service;

    @Autowired
    private ProjectSubcontractorsService projectSubcontractorsService;

    @Secured({OPS_ADMIN, GLA_ORG_ADMIN, GLA_SPM, GLA_PM, ORG_ADMIN, PROJECT_EDITOR})
    @RequestMapping(value = "/projects/{projectId}/subcontractors/{blockId}", method = RequestMethod.POST)
    @ApiOperation(value = "create a project subcontractor", notes = "")
    @ApiResponses(@ApiResponse(code = 400, message = "validation error", response = ApiError.class))
    public SubcontractingBlock addProjectSubcontractors(@PathVariable Integer projectId, @PathVariable Integer blockId,
        @Valid @RequestBody Subcontractor subcontractor, BindingResult bindingResult) {
        verifyBinding("Invalid subcontractor details!", bindingResult);
        return projectSubcontractorsService.createNewSubcontractor(projectId, blockId, subcontractor);
    }

    @Secured({OPS_ADMIN, GLA_ORG_ADMIN, GLA_SPM, GLA_PM, ORG_ADMIN, PROJECT_EDITOR})
    @RequestMapping(value = "/projects/{id}/subcontractors/{blockId}", method = RequestMethod.PUT)
    @ApiOperation(value = "updates a project's subcontractors", notes = "")
    @ApiResponses(@ApiResponse(code = 400, message = "validation error", response = ApiError.class))
    public SubcontractingBlock updateProjectSubcontractors(@PathVariable Integer id, @PathVariable Integer blockId,
        @Valid @RequestBody Subcontractor subcontractor, BindingResult bindingResult) {
        verifyBinding("Invalid subcontractors!", bindingResult);
        return projectSubcontractorsService.updateProjectSubcontractor(id, blockId, subcontractor);
    }

    @Secured({OPS_ADMIN, GLA_ORG_ADMIN, GLA_SPM, GLA_PM, ORG_ADMIN, PROJECT_EDITOR})
    @RequestMapping(value = "/projects/{id}/subcontractors/{blockId}/subcontractor/{subcontractorId}", method = RequestMethod.DELETE)
    @ApiOperation(value = "create a project subcontractors", notes = "")
    @ApiResponses(@ApiResponse(code = 400, message = "validation error", response = ApiError.class))
    public boolean deleteProjectSubcontractor(@PathVariable Integer id, @PathVariable Integer blockId,
        @PathVariable Integer subcontractorId){
        Project fromDB = service.get(id);
        Subcontractor toRemove = null;
        SubcontractingBlock subcontractingBlock = (SubcontractingBlock) fromDB.getProjectBlockById(blockId);

        if (subcontractingBlock.getSubcontractors() != null) {
            for (Subcontractor subcontractor : subcontractingBlock.getSubcontractors()) {
                if (subcontractor.getId().equals(subcontractorId)) {
                    toRemove = subcontractor;
                }
            }
        }
        if (toRemove == null) {
            throw new ValidationException(String.format("Unable to remove subcontractor with ID '%d' from project: %d", subcontractorId, id));
        }
        projectSubcontractorsService.deleteSubcontractor(fromDB,  toRemove, blockId);

        return true;
    }

    @Secured({OPS_ADMIN, GLA_ORG_ADMIN, GLA_SPM, GLA_PM, ORG_ADMIN, PROJECT_EDITOR})
    @RequestMapping(value = "/projects/{projectId}/block/{blockId}/subcontractor/{subcontractorId}/deliverable", method = RequestMethod.POST)
    @ApiOperation(value = "create a project subcontractor deliverable", notes = "")
    @ApiResponses(@ApiResponse(code = 400, message = "validation error", response = ApiError.class))
    public Deliverable addDeliverable(@PathVariable Integer projectId, @PathVariable Integer blockId,
                                            @PathVariable Integer  subcontractorId, @Valid @RequestBody Deliverable deliverable, BindingResult bindingResult) {
        verifyBinding("Invalid deliverable details!", bindingResult);
        return projectSubcontractorsService.createNewSubcontractorDeliverable(projectId, blockId, subcontractorId, deliverable);
    }

    @Secured({OPS_ADMIN, GLA_ORG_ADMIN, GLA_SPM, GLA_PM, ORG_ADMIN, PROJECT_EDITOR})
    @RequestMapping(value = "/projects/{projectId}/block/{blockId}/subcontractor/{subContractorId}/deliverable/{deliverableId}", method = RequestMethod.PUT)
    @ApiOperation(value = "update a project subcontractor deliverable", notes = "")
    @ApiResponses(@ApiResponse(code = 400, message = "validation error", response = ApiError.class))
    public void updateDeliverable(@PathVariable Integer projectId, @PathVariable Integer blockId, @PathVariable Integer  subContractorId,
                                            @PathVariable Integer  deliverableId, @Valid @RequestBody Deliverable deliverable, BindingResult bindingResult) {
        verifyBinding("Invalid deliverable details!", bindingResult);
        projectSubcontractorsService.updateSubcontractorDeliverable(projectId, blockId, subContractorId, deliverableId, deliverable);
    }

    @Secured({OPS_ADMIN, GLA_ORG_ADMIN, GLA_SPM, GLA_PM, ORG_ADMIN, PROJECT_EDITOR})
    @RequestMapping(value = "/projects/{projectId}/block/{blockId}/subcontractor/{subcontractorId}/deliverable/{deliverableId}", method = RequestMethod.DELETE)
    @ApiOperation(value = "delete a project subcontractor deliverable", notes = "")
    @ApiResponses(@ApiResponse(code = 400, message = "validation error", response = ApiError.class))
    public void deleteDeliverable(@PathVariable Integer projectId, @PathVariable Integer blockId, @PathVariable Integer  subcontractorId,
                                            @PathVariable Integer  deliverableId) {
        projectSubcontractorsService.deleteSubcontractorDeliverable(projectId, blockId, subcontractorId, deliverableId);
    }

    @Secured({OPS_ADMIN, GLA_ORG_ADMIN, GLA_SPM, GLA_PM, ORG_ADMIN, PROJECT_EDITOR})
    @RequestMapping(value = "/projects/{projectId}/block/{blockId}/getDeliverableFeeCalculation", method = RequestMethod.GET)
    @ApiOperation(value = "calculates the deliverable fee percentage and if it exceeds the threshold defined in the template")
    public DeliverableFeeCalculation getDeliverableFeeCalculation(@PathVariable Integer projectId,
                                                                  @PathVariable Integer blockId,
                                                                  @RequestParam(required = false) BigDecimal value,
                                                                  @RequestParam(required = false) BigDecimal fee) {
        return projectSubcontractorsService.getDeliverableFeeCalculation(projectId, blockId, value, fee);
    }

}
