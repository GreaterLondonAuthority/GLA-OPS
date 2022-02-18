/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.project.label;

import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import uk.gov.london.ops.framework.exception.NotFoundException;
import uk.gov.london.ops.framework.exception.ValidationException;
import uk.gov.london.ops.organisation.OrganisationServiceImpl;
import uk.gov.london.ops.organisation.model.OrganisationEntity;
import uk.gov.london.ops.project.implementation.repository.LabelRepository;
import uk.gov.london.ops.project.implementation.repository.PreSetLabelRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
/**
 * service for managing project / block labels
 */
public class LabelServiceImpl implements LabelService {

    private final LabelRepository labelRepository;
    private final PreSetLabelRepository preSetLabelRepository;
    private final OrganisationServiceImpl organisationService;

    public LabelServiceImpl(LabelRepository labelRepository, PreSetLabelRepository preSetLabelRepository,
                            OrganisationServiceImpl organisationService) {
        this.labelRepository = labelRepository;
        this.preSetLabelRepository = preSetLabelRepository;
        this.organisationService = organisationService;
    }

    /**
     * creates a new label
     */
    public Label createLabel(Label label) {
        return labelRepository.save(label);
    }

    /**
     * Returns all labels used by specified block
     */
    public Set<Label> getLabelsForBlock(Integer blockId) {
        return labelRepository.findLabelsForBlock(blockId);
    }

    /**
     * Returns all labels used by specified project
     */
    public Set<Label> getLabelsByProjectId(Integer projectId) {
        return labelRepository.getAllByProjectId(projectId);
    }

    /**
     * Deletes a label by project Id
     */
    public void deleteLabelByProjectId(Integer projectId) {
        labelRepository.deleteByProjectId(projectId);
    }

    public PreSetLabelEntity find(Integer id) {
        PreSetLabelEntity preSetLabel = preSetLabelRepository.findById(id).orElse(null);
        if (preSetLabel == null) {
            throw new NotFoundException();
        }
        return preSetLabel;
    }

    public List<PreSetLabelEntity> getPreSetLabels(Integer managingOrganisationId, boolean markedForCorporate) {
        List<PreSetLabelEntity> preSetLabels;

        if (managingOrganisationId == null) {
            preSetLabels = preSetLabelRepository.findAll();
        } else {
            OrganisationEntity managingOrganisation = organisationService.findOne(managingOrganisationId);
            List<PreSetLabelEntity> preSetLabelsManagingOrg = getPreSetLabelsManagedBy(managingOrganisation);

            if (markedForCorporate) {
                preSetLabels = ListUtils.union(getCorporatePreSetLabels(), preSetLabelsManagingOrg);
            } else {
                preSetLabels = preSetLabelsManagingOrg;
            }
        }

        return preSetLabels;
    }

    private List<PreSetLabelEntity> getCorporatePreSetLabels() {
        return preSetLabelRepository.findAll().stream()
                .filter(label -> label.getManagingOrganisation().isCorporateOrganisation())
                .collect(Collectors.toList());
    }

    public Map<String, String> getCorporateLabels() {
        Map<String, String> labelsMap = new HashMap<>();
        List<PreSetLabelEntity> labels = preSetLabelRepository.findAll().stream()
                .filter(label -> label.getManagingOrganisation().isCorporateOrganisation())
                .filter(l -> l.getStatus().equals(PreSetLabelEntity.Status.Active)).collect(Collectors.toList());
        for (PreSetLabelEntity label : labels) {
            labelsMap.put(label.getId().toString(), label.getLabelName());
        }
        return labelsMap;
    }

    public List<PreSetLabelEntity> getPreSetLabelsManagedBy(OrganisationEntity organisation) {
        return preSetLabelRepository.findAllByManagingOrganisation(organisation);
    }

    private void validatePreSetLabel(PreSetLabelEntity preSetLabel) {
        if (StringUtils.isEmpty(preSetLabel.getLabelName())) {
            throw new ValidationException("name", "Pre-set label must have a name");
        }

        if (preSetLabel.getManagingOrganisation() == null) {
            throw new ValidationException("managingOrganisation", "Pre-set label must have an managing organisation");
        }

    }

    public PreSetLabelEntity create(PreSetLabelEntity preSetLabel) {
        if (preSetLabel.getId() != null) {
            throw new ValidationException("id", "New pre-set labels must not have an ID");
        }

        validatePreSetLabel(preSetLabel);

        return preSetLabelRepository.save(preSetLabel);
    }

    public PreSetLabelEntity update(Integer id, PreSetLabelEntity preSetLabelUpdates) {
        PreSetLabelEntity existing = preSetLabelRepository.findById(id).orElse(null);
        if (existing == null) {
            throw new ValidationException("Unable to find pre-set label with specified ID");
        }

        validatePreSetLabel(preSetLabelUpdates);

        existing.setLabelName(preSetLabelUpdates.getLabelName());
        existing.setStatus(preSetLabelUpdates.getStatus());

        return preSetLabelRepository.save(existing);

    }

    public void deletePreSetLabel(Integer id) {
        preSetLabelRepository.deleteById(id);
    }

}
