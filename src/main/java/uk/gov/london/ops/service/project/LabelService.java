/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.service.project;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.london.ops.domain.project.Label;
import uk.gov.london.ops.repository.LabelRepository;

import java.util.Set;

@Service
/**
 * service for managing project / block labels
 */
public class LabelService {

    @Autowired
    private LabelRepository labelRepository;


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
     * Deletes a label by project Id
     */
    public void deleteLabelByProjectId(Integer projectId) {
        labelRepository.deleteByProjectId(projectId);
    }


}
