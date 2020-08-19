/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.project.deliverypartner;

import java.math.BigDecimal;
import javax.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import uk.gov.london.ops.framework.exception.ValidationException;
import uk.gov.london.ops.project.BaseProjectService;
import uk.gov.london.ops.project.Project;
import uk.gov.london.ops.project.block.ProjectBlockType;

@Service
@Transactional
public class ProjectDeliveryPartnersService extends BaseProjectService {

    Logger log = LoggerFactory.getLogger(getClass());

    public DeliveryPartnersBlock updateProjectDeliveryPartner(Integer projectId, Integer blockId,
            DeliveryPartner deliveryPartner) {
        Project project = get(projectId);
        DeliveryPartnersBlock block = (DeliveryPartnersBlock) project
                .getSingleBlockByTypeAndId(ProjectBlockType.DeliveryPartners, blockId);
        this.checkForLock(block);

        for (DeliveryPartner existingDeliveryPartner : block.getDeliveryPartners()) {
            if (existingDeliveryPartner.getId().equals(deliveryPartner.getId())) {
                existingDeliveryPartner.setIdentifier(deliveryPartner.getIdentifier());
                existingDeliveryPartner.setOrganisationName(deliveryPartner.getOrganisationName());
            }
        }
        updateProject(project);
        return block;
    }

    public DeliveryPartnersBlock createNewDeliveryPartner(Integer projectId, Integer blockId, DeliveryPartner deliveryPartner) {

        Project project = get(projectId);
        DeliveryPartnersBlock block = (DeliveryPartnersBlock) project
                .getSingleBlockByTypeAndId(ProjectBlockType.DeliveryPartners, blockId);
        this.checkForLock(block);

        if (deliveryPartner.getOrganisationName() == null) {
            throw new ValidationException("deliveryPartnerOrganisationName",
                    "Partner organisation name is required when creating a partner.");
        }
        if (deliveryPartner.getOrganisationName().length() > 50) {
            throw new ValidationException("deliveryPartnerOrganisationName",
                    "Maximum length for organisation name field is 50 characters.");
        }

        if (block.getDeliveryPartners().stream()
                .anyMatch(s -> s.getOrganisationName().equals(deliveryPartner.getOrganisationName()))) {
            throw new ValidationException("deliveryPartner", "Partner already exists.");
        }

        block.getDeliveryPartners().add(deliveryPartner);

        updateProject(project);

        return block;
    }

    public DeliveryPartnersBlock deleteDeliveryPartner(Project project, DeliveryPartner deliveryPartner, Integer blockId) {
        DeliveryPartnersBlock block = (DeliveryPartnersBlock) project
                .getSingleBlockByTypeAndId(ProjectBlockType.DeliveryPartners, blockId);
        this.checkForLock(block);

        block.getDeliveryPartners().remove(deliveryPartner);

        updateProject(project);
        return block;
    }


    public Deliverable createNewDeliveryPartnerDeliverable(Integer projectId, Integer blockId, Integer deliveryPartnerId,
            Deliverable deliverable) {
        Project project = get(projectId);
        DeliveryPartner deliveryPartner = getDeliveryPartnerForEdit(project, blockId, deliveryPartnerId);

        deliveryPartner.getDeliverables().add(deliverable);
        this.updateProject(project);
        return deliverable;
    }

    private DeliveryPartner getDeliveryPartnerForEdit(Project project, Integer blockId, Integer deliveryPartnerId) {

        DeliveryPartnersBlock block = (DeliveryPartnersBlock) project
                .getSingleBlockByTypeAndId(ProjectBlockType.DeliveryPartners, blockId);
        this.checkForLock(block);

        DeliveryPartner deliveryPartner = block.getDeliveryPartners().stream().filter(s -> s.getId().equals(deliveryPartnerId))
                .findFirst().orElse(null);
        if (deliveryPartner == null) {
            throw new ValidationException("Unable to find partner with ID: " + deliveryPartnerId);
        }
        return deliveryPartner;
    }

    public void updateDeliveryPartnerDeliverable(Integer projectId, Integer blockId, Integer deliveryPartnerId,
            Integer deliverableId, Deliverable deliverable) {
        Project project = get(projectId);
        DeliveryPartner deliveryPartner = getDeliveryPartnerForEdit(project, blockId, deliveryPartnerId);
        Deliverable deliverableToUpdate = deliveryPartner.getDeliverables().stream().filter(p -> p.getId().equals(deliverableId))
                .findFirst().orElse(null);
        if (deliverableToUpdate == null) {
            throw new ValidationException("Unable to find deliverable with ID: " + deliverableId);
        }
        deliverableToUpdate.merge(deliverable);
        this.updateProject(project);
    }

    public void deleteDeliveryPartnerDeliverable(Integer projectId, Integer blockId, Integer deliveryPartnerId,
            Integer deliverableId) {
        Project project = get(projectId);
        DeliveryPartner deliveryPartner = getDeliveryPartnerForEdit(project, blockId, deliveryPartnerId);
        boolean removed = deliveryPartner.getDeliverables().removeIf(p -> p.getId().equals(deliverableId));
        if (!removed) {
            throw new ValidationException("Unable to find deliverable with ID: " + deliverableId);
        }
        this.updateProject(project);
    }

    public DeliverableFeeCalculation getDeliverableFeeCalculation(Integer projectId, Integer blockId, BigDecimal value,
            BigDecimal fee) {
        Project project = get(projectId);
        DeliveryPartnersBlock block = (DeliveryPartnersBlock) project
                .getSingleBlockByTypeAndId(ProjectBlockType.DeliveryPartners, blockId);
        return block.getDeliverableFeeCalculation(value, fee);
    }

}
