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
import uk.gov.london.ops.assessment.Assessment;

import java.util.List;

public interface AssessmentRepository extends JpaRepository<Assessment, Integer> {
    @Query(value = "select count(*) "
            + "from assessment a inner join internal_project_block ipb on a.block_id = ipb.id "
            + "inner join project p on ipb.project_id = p.id "
            + "inner join template t on p.template_id = t.id "
            + "inner join programme prg on p.programme_id = prg.id "
            + "where prg.id = ?1 and t.id = ?2 and a.assessment_template_id = ?3", nativeQuery = true)
    Integer countAssessments(Integer programmeId, Integer templateId, Integer assessmentId);

    @Query(value = "select a.* from assessment a "
            + "inner join internal_project_block pb on pb.id = a.block_id "
            + "inner join project p on pb.project_id = p.id "
            + "inner join organisation o on p.org_id = o.id "
            + "inner join organisation mo on mo.id = o.MANAGING_ORGANISATION_ID  "
            + "where mo.id in (?1) order by created_by", nativeQuery = true)
    List<Assessment> getAssessments(Integer... orgIds);

    @Query(value = "select a.* from assessment a "
            + "inner join internal_project_block pb on pb.id = a.block_id "
            + "where pb.project_id = ?1 and a.project_status = ?2 and a.assessment_template_id = ?3 order by a.created_on",
            nativeQuery = true)
    List<Assessment> findByProjectIdAndTemplateId(Integer projectId, String projectStatus, Integer outcomeOfAssessmentTemplateId);

    List<Assessment> findAllByProjectId(Integer projectId);
}
