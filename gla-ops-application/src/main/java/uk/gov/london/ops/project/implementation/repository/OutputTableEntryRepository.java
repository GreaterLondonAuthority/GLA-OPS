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
import uk.gov.london.ops.project.outputs.OutputTableEntry;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

public interface OutputTableEntryRepository extends JpaRepository<OutputTableEntry, Integer> {

    @Query("select  p from uk.gov.london.ops.project.outputs.OutputTableEntry p "
            + "where p.blockId = ?1 and p.yearMonth >= (?2 * 100 + 4) and p.yearMonth < ((?2 +1) * 100 + 4)")
    Set<OutputTableEntry> findAllByBlockIdAndFinancialYear(Integer blockId, Integer year);

    @Query("select  p from uk.gov.london.ops.project.outputs.OutputTableEntry p where p.blockId = ?1 and p.yearMonth = 0")
    Set<OutputTableEntry> findAllBaselineData(Integer blockId);

    @Query("select  p from uk.gov.london.ops.project.outputs.OutputTableEntry p "
            + "where p.blockId = ?1 and p.year = ?2 and p.month = ?3 and p.config.id = ?4 and p.outputType.key=?5")
    OutputTableEntry findOneByDateAndTypeInformation(Integer blockId, Integer year, Integer month, Integer occId,
                                                     String outputTypeKey);

    @Query("select  p from uk.gov.london.ops.project.outputs.OutputTableEntry p "
            + "where p.blockId = ?1 and p.config.id = ?2 and p.yearMonth = 0")
    OutputTableEntry findBaselineBy(Integer blockId, Integer occId);

    long countByProjectIdAndSource(int projectId, OutputTableEntry.Source source);

    void deleteBySource(OutputTableEntry.Source source);

    Set<OutputTableEntry> findAllByBlockId(Integer blockId);

    @Query(value = "select distinct year_month from output_table_entry o where o.block_id = ?1", nativeQuery = true)
    List<Integer> findPopulatedYearsForBlock(Integer blockId);

    @Query(value = "select sum(actual) from output_table_entry "
            + "where block_id = ?3 and configuration_id = ?1 and (year * 100) + month < (?2 * 100) + 4", nativeQuery = true)
    BigDecimal getCumulativeAdvancePayment(Integer selectedRecoveryId, Integer financialYear, Integer blockId);

}
