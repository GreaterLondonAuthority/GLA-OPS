/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.project.implementation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import uk.gov.london.common.skills.SkillsGrantType;
import uk.gov.london.ops.project.skills.SkillsFundingGroupedSummary;
import uk.gov.london.ops.project.skills.SkillsFundingSummaryEntity;

import java.util.List;
import java.util.Set;

public interface SkillsFundingSummaryRepository extends JpaRepository<SkillsFundingSummaryEntity, Integer> {

    Set<SkillsFundingSummaryEntity> findAllByUkprnAndGrantType(Integer ukprn,  SkillsGrantType grantType);

    @Query(value = "select new uk.gov.london.ops.project.skills.SkillsFundingGroupedSummary(s.ukprn, s.academicYear, s.period, s.grantType, sum(s.totalPayment)) " +
            "from  uk.gov.london.ops.project.skills.SkillsFundingSummaryEntity s " +
            "where s.ukprn = ?1 and s.academicYear= ?2 and s.grantType = ?3 " +
            "group by s.ukprn, s.academicYear, s.period, s.grantType")
    List<SkillsFundingGroupedSummary> getGroupedSummariesByUkprnAndYearAndGrantType(Integer ukprn, Integer year, SkillsGrantType grantType);

    void deleteAllByAcademicYearAndPeriod(Integer academicYear, Integer period);

    @Query(value = "select count(c.id) from claim c " +
            "inner join learning_grant_entry lge on c.entity_id = lge.id " +
            "inner join learning_grant_block lgb on c.block_id=lgb.id " +
            "where lgb.grant_type='AEB_PROCURED' and lge.type = 'DELIVERY' and lge.academic_year = ?1 and lge.period = ?2", nativeQuery = true)
    Integer getNumberOfClaimsForEntity(Integer academicYear, Integer period);

}
