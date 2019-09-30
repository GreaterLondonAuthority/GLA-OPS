/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

package uk.gov.london.ops.service;

import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.london.ops.domain.PreSetLabel;
import uk.gov.london.ops.domain.organisation.Organisation;
import uk.gov.london.ops.repository.OrganisationRepository;
import uk.gov.london.ops.repository.PreSetLabelRepository;
import uk.gov.london.ops.framework.exception.NotFoundException;
import uk.gov.london.ops.framework.exception.ValidationException;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Spring JPA Service for Pre-set Labels information.
 *
 * @author Carmina Matias
 */
@Service
public class PreSetLabelService {

  @Autowired
  private PreSetLabelRepository preSetLabelRepository;

  @Autowired
  private OrganisationRepository organisationRepository;

  public PreSetLabel find(Integer id) {
    PreSetLabel preSetLabel = preSetLabelRepository.findById(id).orElse(null);
    if (preSetLabel == null) {
      throw new NotFoundException();
    }
    return preSetLabel;
  }

  public List<PreSetLabel> getPreSetLabels(Integer managingOrganisationId, boolean markedForCorporate) {
    List<PreSetLabel> preSetLabels;

    if (managingOrganisationId == null) {
      preSetLabels = preSetLabelRepository.findAll();
    } else {
    Organisation managingOrganisation = organisationRepository.getOne(managingOrganisationId);
      List<PreSetLabel> preSetLabelsManagingOrg = getPreSetLabelsManagedBy(managingOrganisation);

      if (markedForCorporate == true) {
         preSetLabels = ListUtils.union(getCorporateLabels(), preSetLabelsManagingOrg);
      } else {
        preSetLabels = preSetLabelsManagingOrg;
      }
    }

    return preSetLabels;
  }


  public List<PreSetLabel> getCorporateLabels(){
    return preSetLabelRepository.findAll().stream()
            .filter(label -> label.getManagingOrganisation().isCorporateOrganisation())
            .collect(Collectors.toList());
  }

  public List<PreSetLabel> getPreSetLabelsManagedBy(Organisation organisation){
    return preSetLabelRepository.findAllByManagingOrganisation(organisation);
  }

  private void validatePreSetLabel(PreSetLabel preSetLabel) {
    if (StringUtils.isEmpty(preSetLabel.getLabelName())) {
      throw new ValidationException("name", "Pre-set label must have a name");
    }

    if (preSetLabel.getManagingOrganisation() == null) {
      throw new ValidationException("managingOrganisation", "Pre-set label must have an managing organisation");
    }

  }

  public PreSetLabel create(PreSetLabel preSetLabel) {
    if (preSetLabel.getId() != null) {
      throw new ValidationException("id", "New pre-set labels must not have an ID");
    }

    validatePreSetLabel(preSetLabel);

    return preSetLabelRepository.save(preSetLabel);
  }

  public PreSetLabel update(Integer id, PreSetLabel preSetLabelUpdates) {
    PreSetLabel existing = preSetLabelRepository.findById(id).orElse(null);
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
