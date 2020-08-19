/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.refdata.implementation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import uk.gov.london.ops.refdata.OutputCategoryConfigurationSummary;

import java.util.List;

public interface OutputCategoryConfigurationSummaryRepository extends JpaRepository<OutputCategoryConfigurationSummary, Integer> {

    @Query(value = "SELECT id, category, subcategory, value_type, tenure_type_id, hidden, string_agg(CAST(output_group_id AS text), ', ') output_group_ids FROM ( "
        + "                  SELECT OUTPUT_CAT_CONFIG.category category, OUTPUT_CAT_CONFIG.subcategory subcategory, "
        + "                         OUTPUT_CAT_CONFIG.id id, OUTPUT_CAT_CONFIG.value_type value_type, OUTPUT_CAT_CONFIG.tenure_type_id tenure_type_id, "
        + "                         OUTPUT_CAT_CONFIG.hidden hidden, OUTPUT_GROUP_OUTPUT_CONFIG.OUTPUT_GROUP_ID output_group_id "
        + "                  FROM OUTPUT_CAT_CONFIG "
        + "                         LEFT JOIN OUTPUT_GROUP_OUTPUT_CONFIG ON OUTPUT_CONFIG_ID = OUTPUT_CAT_CONFIG.ID ) sub "
        + "GROUP BY sub.id, sub.category, sub.subcategory, sub.value_type, sub.tenure_type_id, sub.hidden ", nativeQuery = true)
    List<OutputCategoryConfigurationSummary> findAll();

}
