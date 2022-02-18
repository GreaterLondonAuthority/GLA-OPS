/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.project.implementation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import uk.gov.london.ops.project.state.StateModel;
import uk.gov.london.ops.project.template.domain.Template;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Set;

@Repository
public interface TemplateRepository extends JpaRepository<Template, Integer> {

    Template findByName(String name);

    @Query(value = "select * from template t inner join template_block tb on t.id = tb.template_id "
            + "inner join TEMPLATE_BLOCK_QUESTION tbq on tb.id = tbq.template_block_id "
            + "inner join template_question tq on tq.id = tbq.question_id where tq.question_id = ?1", nativeQuery = true)
    List<Template> findAllForQuestion(Integer questionId);

    @Query(value = "select count(*) from template t inner join template_block tb on t.id = tb.template_id "
            + "inner join TEMPLATE_BLOCK_QUESTION tbq on tb.id = tbq.template_block_id "
            + "inner join template_question tq on tq.id = tbq.question_id where tq.question_id = ?1", nativeQuery = true)
    Integer countByQuestion(Integer questionId);

    @Query(value = "select t.* from template t inner join template_block tb on t.id = tb.template_id and"
            + " output_config_group_id = ?1", nativeQuery = true)
    Set<Template> findAllUsingConfigGroup(Integer groupId);

    @Query(value = "select t.id from template t "
            + " where t.project_submission_reminder = ?1 ", nativeQuery = true)
    Set<Integer> findAllByTemplateServiceReminder(boolean reminder);

    @Query(value = "select distinct t.state_model from template t "
            + "inner join programme_template pt on pt.template_id = t.id "
            + "inner join programme p on p.id = pt.programme_id "
            + "inner join organisation mo on mo.id = p.managing_organisation_id "
            + "where mo.id in ?1", nativeQuery = true)
    Set<StateModel> getAvailableStateModelsForManagingOrgIds(Set<Integer> managingOrgIds);

    @Query(value = "select t.template_id from template_tenure_type t " +
            "where t.external_id = ?1", nativeQuery = true)
    Set<Integer> getUsagesOfTenureType(Integer external);

    @Transactional
    @Modifying
    @Query(value = "update template_block set detached_block_template_id = template_id, template_id = null "
            + "where template_id = ?1 and display_order = ?2", nativeQuery = true)
    int copyTemplateIdToDetachedID(Integer projectId, Integer displayOrder);

    @Transactional
    @Modifying
    @Query(value = "update internal_template_block set detached_block_template_id = template_id, template_id = null "
            + "where template_id = ?1 and display_order = ?2", nativeQuery = true)
    int copyTemplateIdToDetachedIDForInternalBlock(Integer projectId, Integer displayOrder);

    @Transactional
    @Modifying
    @Query(value = "update template_block set template_id = detached_block_template_id, detached_block_template_id =null "
            + "where detached_block_template_id = ?1 and id = ?2", nativeQuery = true)
    int reattachRemovedBlock(Integer templateId, Integer blockId);


    @Transactional
    @Modifying
    @Query(value = "update internal_template_block set template_id = detached_block_template_id, detached_block_template_id =null "
            + "where detached_block_template_id = ?1 and id = ?2", nativeQuery = true)
    int reattachRemovedInternalBlock(Integer templateId, Integer blockId);

}
