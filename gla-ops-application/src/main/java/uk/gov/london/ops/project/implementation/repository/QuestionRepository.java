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
import uk.gov.london.ops.project.template.domain.Question;

import java.util.Set;

public interface QuestionRepository extends JpaRepository<Question, Integer> {

    @Query(value = "select tq.question_id from template t " +
            "inner join template_block tb on t.id = tb.TEMPLATE_ID " +
            "inner join TEMPLATE_BLOCK_QUESTION tqb on tqb.template_block_id = tb.id " +
            "inner join TEMPLATE_QUESTION tq on tq.id = tqb.question_id " +
            "where t.id = ?1 or upper(t.name) like upper(concat('%', ?2, '%'))", nativeQuery = true)
    Set<Integer> findQuestionsByTemplateIdOrText(Integer id, String templateText);

    Page<Question> findAllByIdOrTextContainingIgnoreCase(Integer id, String text, Pageable pageable);

    Page<Question> findByIdIn(Set<Integer> questionIds, Pageable pageable);
}
