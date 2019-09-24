/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

package uk.gov.london.ops.service;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.london.ops.domain.DeliveryOverride;
import uk.gov.london.ops.domain.project.Project;
import uk.gov.london.ops.domain.user.User;
import uk.gov.london.ops.repository.OverrideRepository;
import uk.gov.london.ops.repository.ProjectRepository;
import uk.gov.london.ops.framework.exception.NotFoundException;
import uk.gov.london.ops.framework.exception.ValidationException;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Spring JPA Service for Pre-set Labels information.
 *
 * @author Carmina Matias
 */
@Service
public class OverrideService {

    @Autowired
    private OverrideRepository overrideRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserService userService;

    public DeliveryOverride find(Integer id) {
      DeliveryOverride deliveryOverride = overrideRepository.findById(id).orElse(null);
      if (deliveryOverride == null) {
        throw new NotFoundException();
      }
      return deliveryOverride;
    }

    public List<DeliveryOverride> getOverrides(){
      return overrideRepository.findAll();
    }

    public Map<String, List<String>> getOverridesMetadata(){
        //TODO should it be enums?
        Map<String, List<String>> metadata = new HashMap<>();
        metadata.put("reasons", Stream.of("Adjustment", "Correction", "Project Restart").collect(Collectors.toList()));
        metadata.put("types", Stream.of("Starts", "Completions", "Acquisition", "RCGF", "DPF", "Grant").collect(Collectors.toList()));
        metadata.put("tenures", Stream.of("Affordable Tenure TBC", "London Living Rent / Shared Ownership", "Other Intermediate", "Social Rent (and LAR at benchmarks)", "Other Affordable Rent").collect(Collectors.toList()));
        return metadata;
    }

    public List<DeliveryOverride> getOverridesByProjectId(Integer projectId){
      return overrideRepository.findAllByProjectId(projectId);
    }

    private void validateOverride(DeliveryOverride deliveryOverride) {

      if (deliveryOverride.getProjectId() != null) {
        Project project = projectRepository.findById(deliveryOverride.getProjectId()).orElse(null);
        if(project == null) {
          throw new ValidationException("projectId", "DeliveryOverride must have an existing project id");
        }
      } else {
        throw new ValidationException("projectId", "DeliveryOverride must have a project id");
      }

      if (StringUtils.isEmpty(deliveryOverride.getOverrideReason())) {
        throw new ValidationException("overrideReason", "DeliveryOverride must have an deliveryOverride reason");
      }

      if (StringUtils.isEmpty(deliveryOverride.getOverrideType())) {
        throw new ValidationException("overrideType", "DeliveryOverride must have an deliveryOverride type");
      }

    }

    public DeliveryOverride create(DeliveryOverride deliveryOverride) {
      if (deliveryOverride.getId() != null) {
        throw new ValidationException("id", "New deliveryOverride must not have an ID");
      }

      validateOverride(deliveryOverride);

      return getOverrideUser(deliveryOverride);
    }

    public DeliveryOverride update(Integer id, DeliveryOverride deliveryOverrideUpdates) {
      DeliveryOverride existing = overrideRepository.findById(id).orElse(null);
      if (existing == null) {
        throw new ValidationException("Unable to find override with specified ID");
      }

      validateOverride(deliveryOverrideUpdates);

      existing.setProjectId(deliveryOverrideUpdates.getProjectId());
      existing.setOverrideReason(deliveryOverrideUpdates.getOverrideReason());
      existing.setOverrideType(deliveryOverrideUpdates.getOverrideType());
      existing.setReportedDate(deliveryOverrideUpdates.getReportedDate());
      existing.setReportedValue(deliveryOverrideUpdates.getReportedValue());
      existing.setTenure(deliveryOverrideUpdates.getTenure());
      existing.setComments(deliveryOverrideUpdates.getComments());

      return getOverrideUser(existing);

    }

    private DeliveryOverride getOverrideUser(DeliveryOverride deliveryOverride) {
      User currentUser = userService.currentUser();
      if(currentUser == null) {
        throw new ValidationException("overriddenBy", "DeliveryOverride must have an overridden user");
      } else {
        deliveryOverride.setOverriddenBy(currentUser);
        deliveryOverride.setOverriddenOn(OffsetDateTime.now());
      }

      return overrideRepository.save(deliveryOverride);
    }

    public void deleteOverride(Integer id) {
        overrideRepository.deleteById(id);
    }
}
