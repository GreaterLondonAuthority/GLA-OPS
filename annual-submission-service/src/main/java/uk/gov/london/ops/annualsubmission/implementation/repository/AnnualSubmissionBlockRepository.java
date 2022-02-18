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
import uk.gov.london.ops.annualsubmission.AnnualSubmissionBlockEntity;

import java.time.OffsetDateTime;
import java.util.List;

public interface AnnualSubmissionBlockRepository extends JpaRepository<AnnualSubmissionBlockEntity, Integer> {

    List<AnnualSubmissionBlockEntity> findAllByLockTimeoutTimeBefore(OffsetDateTime time);

    @Query(value = "select asb.* from annual_submission_block asb "
            + "inner join annual_submission asub on asub.id = asb.annual_submission_id "
            + "where asub.organisation_id = ?1 and asub.financial_year < ?2 and asb.status_type = ?3 and asb.grant_type = ?4 "
            + "order by asub.financial_year asc", nativeQuery = true)
    List<AnnualSubmissionBlockEntity> findAll(Integer organisationId, Integer financialYear, String statusType, String grantType);

}
