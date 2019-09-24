/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import uk.gov.london.common.skills.SkillsGrantType;
import uk.gov.london.ops.domain.skills.SkillsPaymentProfile;


@Repository
public interface SkillsPaymentProfileRepository extends JpaRepository<SkillsPaymentProfile, Integer>  {

    List<SkillsPaymentProfile> findByType(SkillsGrantType type);

    List<SkillsPaymentProfile> findByTypeAndYear(SkillsGrantType type, Integer year);

    @Query(value = "SELECT count(1) > 0 AS used FROM project p "
        + "INNER JOIN project_block pb ON p.id = pb.project_id INNER JOIN learning_grant_block lgb ON lgb.id = pb.id "
        + "WHERE grant_type = ?1 AND (?2 >= start_year AND (?2 <= start_year + number_of_years -1)) AND (p.status = 'Active' "
        + "OR ( p.first_approved != null OR p.status = 'Submitted' OR (p.status = 'Stage 1' AND p.substatus = 'Submitted') OR p.status = 'Stage 2' OR p.status = 'Stage 3'))", nativeQuery = true)
    boolean isSkillsProfileUsedForActiveProjects(String type, Integer year);

}
