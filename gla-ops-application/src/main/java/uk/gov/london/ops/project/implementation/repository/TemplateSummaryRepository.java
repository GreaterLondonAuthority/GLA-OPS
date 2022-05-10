/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.project.implementation.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import uk.gov.london.ops.project.template.domain.Template;
import uk.gov.london.ops.project.template.domain.TemplateSummary;

import java.util.List;
import java.util.Set;

public interface TemplateSummaryRepository extends JpaRepository<TemplateSummary, Integer> {

    Page<TemplateSummary> findAllByTemplateStatusIn(List<Template.TemplateStatus> templateStatusList, Pageable pageable);

    Page<TemplateSummary> findAllByIdOrNameContainingIgnoreCase(Integer id, String name, Pageable pageable);

    Page<TemplateSummary> findAllByIdOrNameContainingIgnoreCaseAndTemplateStatusIn(Integer id,
                                                                                   String name,
                                                                                   List<Template.TemplateStatus> templateStatuses,
                                                                                   Pageable pageable);

    @Query(value = "select t.id from template t inner join programme_template pt on t.id = pt.template_id "
            + "inner join programme p on p.id = pt.programme_id where p.id = ?1 or lower(p.name) like concat('%', ?2, '%')",
            nativeQuery = true)
    Set<Integer> findAllByProgrammeIdOrName(Integer id, String name);

    Page<TemplateSummary> findByIdIn(Set<Integer> templateIds, Pageable pageable);
}
