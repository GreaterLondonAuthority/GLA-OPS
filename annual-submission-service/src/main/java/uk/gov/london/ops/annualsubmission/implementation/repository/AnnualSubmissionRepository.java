/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.annualsubmission.implementation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import uk.gov.london.ops.annualsubmission.AnnualSubmissionEntity;
import uk.gov.london.ops.annualsubmission.AnnualSubmissionStatus;

import java.util.List;

public interface AnnualSubmissionRepository extends JpaRepository<AnnualSubmissionEntity, Integer> {

    List<AnnualSubmissionEntity> findAllByOrganisationId(Integer organisationId);

    AnnualSubmissionEntity findByOrganisationIdAndFinancialYear(Integer organisationId, Integer financialYear);

    List<AnnualSubmissionEntity> findAllByOrganisationIdAndStatus(Integer organisationId, AnnualSubmissionStatus status);

    AnnualSubmissionEntity findByOrganisationIdAndStatusAndFinancialYearGreaterThan(Integer organisationId,
                                                                              AnnualSubmissionStatus status,
                                                                              Integer financialYear);

    @Query(value = "select a.* from annual_submission a "
            + "inner join annual_submission_block asb on a.id = asb.annual_submission_id where asb.id = ?1", nativeQuery = true)
    AnnualSubmissionEntity findByBlockId(Integer blockId);

}
