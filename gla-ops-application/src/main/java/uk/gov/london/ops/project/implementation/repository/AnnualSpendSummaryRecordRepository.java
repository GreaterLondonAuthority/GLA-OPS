/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.project.implementation.repository;

import uk.gov.london.ops.framework.jpa.ReadOnlyRepository;
import uk.gov.london.ops.project.budget.AnnualSpendSummaryRecord;

import java.util.List;

/**
 * Created by chris on 02/02/2017.
 */
public interface AnnualSpendSummaryRecordRepository extends ReadOnlyRepository<AnnualSpendSummaryRecord, Integer> {

    List<AnnualSpendSummaryRecord> findByProjectIdAndBlockIdAndFinancialYearBetweenOrderByFinancialYearAsc(Integer projectId,
                                                                            Integer blockId,
                                                                            Integer financialYearFrom,
                                                                            Integer financialYearTo);

}
