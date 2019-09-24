/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.repository;

import io.swagger.models.auth.In;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import uk.gov.london.ops.domain.template.Template;
import uk.gov.london.ops.domain.template.TemplateSummary;

import java.util.List;
import java.util.Set;

public interface TemplateRepository extends JpaRepository<Template, Integer> {

    Template findByName(String name);

    //List<Template> findAllByJsonNull();

    @Query(value = "select * from template t inner join template_block tb on t.id = tb.template_id " +
            "inner join TEMPLATE_BLOCK_QUESTION tbq on tb.id = tbq.template_block_id " +
            "inner join template_question tq on tq.id = tbq.question_id where tq.question_id = ?1", nativeQuery = true)
    List<Template> findAllForQuestion(Integer questionId);

    @Query(value = "select count(*) from template t inner join template_block tb on t.id = tb.template_id " +
            "inner join TEMPLATE_BLOCK_QUESTION tbq on tb.id = tbq.template_block_id " +
            "inner join template_question tq on tq.id = tbq.question_id where tq.question_id = ?1", nativeQuery = true)
    Integer countByQuestion(Integer questionId);

    @Query(value = "select t.* from template t inner join template_block tb on t.id = tb.template_id and" +
            " output_config_group_id = ?1" , nativeQuery = true)
    Set<Template> findAllUsingConfigGroup(Integer groupId);
}
