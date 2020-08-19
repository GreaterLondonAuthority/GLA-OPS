/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.framework.feature;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface FeatureRepository extends JpaRepository<FeatureEntity, String> {

    @Query(value = "select enabled as permitted  from feature  where  name = ?1 ", nativeQuery = true)
    Boolean isFeatureEnabled(String feature);

}
