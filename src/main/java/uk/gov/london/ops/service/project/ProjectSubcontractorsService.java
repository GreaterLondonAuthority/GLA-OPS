/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.service.project;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import uk.gov.london.ops.domain.project.Project;
import uk.gov.london.ops.domain.project.ProjectBlockType;
import uk.gov.london.ops.domain.project.subcontracting.*;
import uk.gov.london.ops.domain.project.subcontracting.Subcontractor.IdentifierType;
import uk.gov.london.ops.framework.exception.ValidationException;

import javax.transaction.Transactional;
import java.math.BigDecimal;

@Service
@Transactional
public class ProjectSubcontractorsService extends BaseProjectService {

    Logger log = LoggerFactory.getLogger(getClass());

    public SubcontractingBlock updateProjectSubcontractor(Integer projectId, Integer blockId, Subcontractor subcontractor) {
        Project project = get(projectId);
        SubcontractingBlock block = (SubcontractingBlock) project.getSingleBlockByTypeAndId(ProjectBlockType.Subcontracting, blockId);
        this.checkForLock(block);

        for (Subcontractor existingSubcontractor : block.getSubcontractors()) {
            if (existingSubcontractor.getId().equals(subcontractor.getId())) {
                existingSubcontractor.setIdentifier(subcontractor.getIdentifier());
                existingSubcontractor.setOrganisationName(subcontractor.getOrganisationName());
            }
        }
        updateProject(project);
        return block;
    }

    public SubcontractingBlock createNewSubcontractor(Integer projectId, Integer blockId, Subcontractor subcontractor) {

        Project project = get(projectId);
        SubcontractingBlock block = (SubcontractingBlock) project.getSingleBlockByTypeAndId(ProjectBlockType.Subcontracting, blockId);
        this.checkForLock(block);

        if (subcontractor.getOrganisationName() == null) {
            throw new ValidationException("subcontractorOrganisationName", "Subcontractor organisation name is required when creating a subcontractor.");
        }
        if (subcontractor.getOrganisationName().length() > 50) {
            throw new ValidationException("subcontractorOrganisationName", "Maximum length for organisation name field is 50 characters.");
        }

        if (block.getSubcontractors().stream().anyMatch(s -> s.getOrganisationName().equals(subcontractor.getOrganisationName()))) {
            throw new ValidationException("subcontractor", "Subcontractor already exists.");
        }

        if(block.getSubcontractorType().equals(SubcontractorType.LearningProvider)){
            subcontractor.setIdentifierType(IdentifierType.UKPRN);
        }
        block.getSubcontractors().add(subcontractor);

        updateProject(project);

        return block;
    }

    public SubcontractingBlock deleteSubcontractor(Project project, Subcontractor subcontractor, Integer blockId) {
        SubcontractingBlock block = (SubcontractingBlock) project.getSingleBlockByTypeAndId(ProjectBlockType.Subcontracting, blockId);
        this.checkForLock(block);

        block.getSubcontractors().remove(subcontractor);

        updateProject(project);
        return block;
    }


    public Deliverable createNewSubcontractorDeliverable(Integer projectId, Integer blockId, Integer subcontractorId, Deliverable deliverable) {
        Project project = get(projectId);
        Subcontractor subcontractor = getSubcontractorForEdit(project, blockId, subcontractorId);

        subcontractor.getDeliverables().add(deliverable);
        this.updateProject(project);
        return deliverable;
    }

    private Subcontractor getSubcontractorForEdit(Project project, Integer blockId, Integer subcontractorId) {

        SubcontractingBlock block = (SubcontractingBlock) project.getSingleBlockByTypeAndId(ProjectBlockType.Subcontracting, blockId);
        this.checkForLock(block);

        Subcontractor subcontractor = block.getSubcontractors().stream().filter(s -> s.getId().equals(subcontractorId)).findFirst().orElse(null);
        if (subcontractor == null) {
            throw new ValidationException("Unable to find subcontractor with ID: " + subcontractorId);
        }
        return subcontractor;
    }

    public void updateSubcontractorDeliverable(Integer projectId, Integer blockId, Integer subcontractorId, Integer deliverableId, Deliverable deliverable) {
        Project project = get(projectId);
        Subcontractor subcontractor = getSubcontractorForEdit(project, blockId, subcontractorId);
        Deliverable deliverableToUpdate = subcontractor.getDeliverables().stream().filter(p -> p.getId().equals(deliverableId)).findFirst().orElse(null);
        if (deliverableToUpdate == null) {
            throw new ValidationException("Unable to find deliverable with ID: " + deliverableId);
        }
        deliverableToUpdate.merge(deliverable);
        this.updateProject(project);
    }

    public void deleteSubcontractorDeliverable(Integer projectId, Integer blockId, Integer subcontractorId, Integer deliverableId) {
        Project project = get(projectId);
        Subcontractor subcontractor = getSubcontractorForEdit(project, blockId, subcontractorId);
        boolean removed = subcontractor.getDeliverables().removeIf(p -> p.getId().equals(deliverableId));
        if (!removed) {
            throw new ValidationException("Unable to find deliverable with ID: " + deliverableId);
        }
        this.updateProject(project);
    }

    public DeliverableFeeCalculation getDeliverableFeeCalculation(Integer projectId, Integer blockId, BigDecimal value, BigDecimal fee) {
        Project project = get(projectId);
        SubcontractingBlock block = (SubcontractingBlock) project.getSingleBlockByTypeAndId(ProjectBlockType.Subcontracting, blockId);
        return block.getDeliverableFeeCalculation(value, fee);
    }

}
