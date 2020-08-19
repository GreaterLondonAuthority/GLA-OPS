/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.annualsubmission.implementation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import uk.gov.london.ops.annualsubmission.AnnualSubmissionBlock;

import java.time.OffsetDateTime;
import java.util.List;

public interface AnnualSubmissionBlockRepository extends JpaRepository<AnnualSubmissionBlock, Integer> {

    List<AnnualSubmissionBlock> findAllByLockTimeoutTimeBefore(OffsetDateTime time);

    @Query(value = "select asb.* from annual_submission_block asb inner join annual_submission asub on asub.id = asb.annual_submission_id " +
            "where asub.organisation_id = ?1 and asub.financial_year < ?2 and asb.status_type = ?3 and asb.grant_type = ?4 order by asub.financial_year asc", nativeQuery = true)
    List<AnnualSubmissionBlock> findAll(Integer organisationId, Integer financialYear, String statusType, String grantType);

    @Modifying
    @Query(value = "delete from annual_submission_block where annual_submission_id in ?1 and grant_type= 'DPF'", nativeQuery = true)
    void deleteDpfAnnualSubmissionBlockByAnnualSubmissionId(List<Integer> annualSubmissionId);

}
