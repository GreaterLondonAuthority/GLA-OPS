/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.assessment.implementation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import uk.gov.london.ops.assessment.AssessmentTemplate;

import java.util.List;

public interface AssessmentTemplateRepository extends JpaRepository<AssessmentTemplate, Integer> {

    @Query(value = "select * from assessment_template where managing_organisation_id = ?1", nativeQuery = true)
    List<AssessmentTemplate> findAllByManagingOrganisation(Integer managingOrgId);

    @Query(value = "select new uk.gov.london.ops.assessment.AssessmentTemplate(at.id, at.name, at.status, at.managingOrganisation.id, at.managingOrganisation.name) from uk.gov.london.ops.assessment.AssessmentTemplate at ")
    List<AssessmentTemplate> findAllSummaries();

    @Query(value = "select * from assessment_template a where a.id in" +
            " (select assessment_template_id from programme_template_assessment_template where programme_id = ?1 and template_id = ?2)", nativeQuery = true)
    List<AssessmentTemplate> findAll(Integer programmeId, Integer templateId);

    AssessmentTemplate findByName(String name);

}
