/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import uk.gov.london.ops.domain.project.Claim;

import java.util.List;


public interface ClaimRepository extends JpaRepository<Claim, Integer> {
    /**
     * Checks if there are previous entries which should have been claimed
     *
     * In the query there are 2 CASE statements to be able to join different type of columns:
     * For example claim vs output_table_entry: (2017/Q4 vs 2018/Jan, Q1 vs May)
     */
    @Query(value = "select count(*) from output_table_entry ote left join claim c on c.block_id = ote.block_id and c.claim_type = 'QUARTER' and (CASE WHEN ote.month < 4 THEN (ote.year - 1) = c.year ELSE ote.year = c.year  END) and (CASE WHEN ote.month < 4 THEN 4 ELSE (ote.month-1)/3  END) = c.claim_type_period where ote.block_id = ?1 and year_month < (?2*100 + ?3) and c.id is null", nativeQuery = true)
    Integer countPreviouslyUnclaimedOutputEntries(Integer blockId, Integer year, Integer month);

    List<Claim> findAllByBlockId(Integer blockId);

}
