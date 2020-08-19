/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.project.deliverypartner;

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
import java.math.BigDecimal;
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
        description = "managing Project delivery partner data"
)
public class ProjectDeliveryPartnersAPI {

    @Autowired
    private ProjectService service;

    @Autowired
    private ProjectDeliveryPartnersService projectDeliveryPartnersService;

    @Secured({OPS_ADMIN, GLA_ORG_ADMIN, GLA_SPM, GLA_PM, ORG_ADMIN, PROJECT_EDITOR})
    @RequestMapping(value = "/projects/{projectId}/deliveryPartners/{blockId}", method = RequestMethod.POST)
    @ApiOperation(value = "create a project ", notes = "")
    @ApiResponses(@ApiResponse(code = 400, message = "validation error", response = ApiError.class))
    public DeliveryPartnersBlock addProjectDeliveryPartner(@PathVariable Integer projectId, @PathVariable Integer blockId,
            @Valid @RequestBody DeliveryPartner deliveryPartner, BindingResult bindingResult) {
        verifyBinding("Invalid delivery partner details!", bindingResult);
        return projectDeliveryPartnersService.createNewDeliveryPartner(projectId, blockId, deliveryPartner);
    }

    @Secured({OPS_ADMIN, GLA_ORG_ADMIN, GLA_SPM, GLA_PM, ORG_ADMIN, PROJECT_EDITOR})
    @RequestMapping(value = "/projects/{id}/deliveryPartners/{blockId}", method = RequestMethod.PUT)
    @ApiOperation(value = "updates a project's delivery partner", notes = "")
    @ApiResponses(@ApiResponse(code = 400, message = "validation error", response = ApiError.class))
    public DeliveryPartnersBlock updateProjectDeliveryPartners(@PathVariable Integer id, @PathVariable Integer blockId,
            @Valid @RequestBody DeliveryPartner deliveryPartner, BindingResult bindingResult) {
        verifyBinding("Invalid delivery partner!", bindingResult);
        return projectDeliveryPartnersService.updateProjectDeliveryPartner(id, blockId, deliveryPartner);
    }

    @Secured({OPS_ADMIN, GLA_ORG_ADMIN, GLA_SPM, GLA_PM, ORG_ADMIN, PROJECT_EDITOR})
    @RequestMapping(value = "/projects/{id}/deliveryPartners/{blockId}/deliveryPartner/{deliveryPartnerId}",
            method = RequestMethod.DELETE)
    @ApiOperation(value = "create a project deilivery partner", notes = "")
    @ApiResponses(@ApiResponse(code = 400, message = "validation error", response = ApiError.class))
    public boolean deleteProjectDeliveryPartner(@PathVariable Integer id, @PathVariable Integer blockId,
            @PathVariable Integer deliveryPartnerId) {
        Project fromDB = service.get(id);
        DeliveryPartner toRemove = null;
        DeliveryPartnersBlock deliveryPartnersBlock = (DeliveryPartnersBlock) fromDB.getProjectBlockById(blockId);

        if (deliveryPartnersBlock.getDeliveryPartners() != null) {
            for (DeliveryPartner deliveryPartner : deliveryPartnersBlock.getDeliveryPartners()) {
                if (deliveryPartner.getId().equals(deliveryPartnerId)) {
                    toRemove = deliveryPartner;
                }
            }
        }
        if (toRemove == null) {
            throw new ValidationException(
                    String.format("Unable to remove delivery partner with ID '%d' from project: %d", deliveryPartnerId, id));
        }
        projectDeliveryPartnersService.deleteDeliveryPartner(fromDB, toRemove, blockId);

        return true;
    }

    @Secured({OPS_ADMIN, GLA_ORG_ADMIN, GLA_SPM, GLA_PM, ORG_ADMIN, PROJECT_EDITOR})
    @RequestMapping(value = "/projects/{projectId}/block/{blockId}/deliveryPartner/{deliveryPartnerId}/deliverable",
            method = RequestMethod.POST)
    @ApiOperation(value = "create a project delivery partner deliverable", notes = "")
    @ApiResponses(@ApiResponse(code = 400, message = "validation error", response = ApiError.class))
    public Deliverable addDeliverable(@PathVariable Integer projectId, @PathVariable Integer blockId,
            @PathVariable Integer deliveryPartnerId, @Valid @RequestBody Deliverable deliverable, BindingResult bindingResult) {
        verifyBinding("Invalid deliverable details!", bindingResult);
        return projectDeliveryPartnersService
                .createNewDeliveryPartnerDeliverable(projectId, blockId, deliveryPartnerId, deliverable);
    }

    @Secured({OPS_ADMIN, GLA_ORG_ADMIN, GLA_SPM, GLA_PM, ORG_ADMIN, PROJECT_EDITOR})
    @RequestMapping(value = "/projects/{projectId}/block/{blockId}/deliveryPartner/{deliveryPartnerId}/deliverable/{deliverableId}",
            method = RequestMethod.PUT)
    @ApiOperation(value = "update a project delivery partner deliverable", notes = "")
    @ApiResponses(@ApiResponse(code = 400, message = "validation error", response = ApiError.class))
    public void updateDeliverable(@PathVariable Integer projectId, @PathVariable Integer blockId,
            @PathVariable Integer deliveryPartnerId,
            @PathVariable Integer deliverableId, @Valid @RequestBody Deliverable deliverable, BindingResult bindingResult) {
        verifyBinding("Invalid deliverable details!", bindingResult);
        projectDeliveryPartnersService
                .updateDeliveryPartnerDeliverable(projectId, blockId, deliveryPartnerId, deliverableId, deliverable);
    }

    @Secured({OPS_ADMIN, GLA_ORG_ADMIN, GLA_SPM, GLA_PM, ORG_ADMIN, PROJECT_EDITOR})
    @RequestMapping(value = "/projects/{projectId}/block/{blockId}/deliveryPartner/{deliveryPartnerId}/deliverable/{deliverableId}",
            method = RequestMethod.DELETE)
    @ApiOperation(value = "delete a project delivery partner deliverable", notes = "")
    @ApiResponses(@ApiResponse(code = 400, message = "validation error", response = ApiError.class))
    public void deleteDeliverable(@PathVariable Integer projectId, @PathVariable Integer blockId,
            @PathVariable Integer deliveryPartnerId,
            @PathVariable Integer deliverableId) {
        projectDeliveryPartnersService.deleteDeliveryPartnerDeliverable(projectId, blockId, deliveryPartnerId, deliverableId);
    }

    @Secured({OPS_ADMIN, GLA_ORG_ADMIN, GLA_SPM, GLA_PM, ORG_ADMIN, PROJECT_EDITOR})
    @RequestMapping(value = "/projects/{projectId}/block/{blockId}/getDeliverableFeeCalculation", method = RequestMethod.GET)
    @ApiOperation(value = "calculates the deliverable fee percentage and if it exceeds the threshold defined in the template")
    public DeliverableFeeCalculation getDeliverableFeeCalculation(@PathVariable Integer projectId,
            @PathVariable Integer blockId,
            @RequestParam(required = false) BigDecimal value,
            @RequestParam(required = false) BigDecimal fee) {
        return projectDeliveryPartnersService.getDeliverableFeeCalculation(projectId, blockId, value, fee);
    }

}
