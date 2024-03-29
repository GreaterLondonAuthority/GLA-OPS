/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.programme.implementation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import uk.gov.london.ops.domain.EntityCount;
import uk.gov.london.ops.programme.domain.Programme;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;

/**
 * Spring JPA Data Repository for Programme information.
 *
 * @author Steve Leach
 */
@Repository
public interface ProgrammeRepository extends JpaRepository<Programme, Integer> {

    Programme findByName(String name);

    List<Programme> findAllByOpeningDatetime(OffsetDateTime openingDatetime);

    List<Programme> findAllByClosingDatetime(OffsetDateTime closingDatetime);

    List<Programme> findAllByNameContainingIgnoreCase(String name);

    @Query(value = "select  new uk.gov.london.ops.domain.EntityCount( p.template.id, count(p)) "
            + "from uk.gov.london.ops.project.Project p "
            + "where p.programme.id = ?1  group by p.template.id ")
    Set<EntityCount> getProjectsPerTemplateCountByProgramme(Integer programmeId);


    @Query("select new Programme(p.id, p.name) from Programme p join p.templatesByProgramme tp"
            + " where tp.id.templateId = ?1 order by p.name")
    List<Programme> findAllProgrammesForTemplate(Integer templateId);

    @Query(value = "select count(p) from Programme p join p.templatesByProgramme tp join tp.assessmentTemplates at "
            + "where at.assessmentTemplate.id = ?1")
    Integer countByAssessmentTemplateId(Integer assessmentTemplateId);

}
