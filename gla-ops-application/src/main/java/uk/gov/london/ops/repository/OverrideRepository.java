/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uk.gov.london.ops.domain.DeliveryOverride;

import java.util.List;

/**
 * Spring JPA Data Repository for DeliveryOverride information.
 *
 * @author Carmina Matias
 */
public interface OverrideRepository extends JpaRepository<DeliveryOverride, Integer> {

    List<DeliveryOverride> findAllByProjectId(Integer projectId);
}