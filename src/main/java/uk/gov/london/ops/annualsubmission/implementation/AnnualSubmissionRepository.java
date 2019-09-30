/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.annualsubmission.implementation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import uk.gov.london.ops.annualsubmission.AnnualSubmission;
import uk.gov.london.ops.annualsubmission.AnnualSubmissionStatus;

import java.util.List;
import java.util.Set;

public interface AnnualSubmissionRepository extends JpaRepository<AnnualSubmission, Integer> {

    List<AnnualSubmission> findAllByOrganisationId(Integer organisationId);

    AnnualSubmission findByOrganisationIdAndFinancialYear(Integer organisationId, Integer financialYear);

    List<AnnualSubmission> findAllByOrganisationIdAndStatus(Integer organisationId, AnnualSubmissionStatus status);

    List<AnnualSubmission> findAllByOrganisationIdAndFinancialYearLessThan(Integer organisationId, Integer financialYear);

    AnnualSubmission findByOrganisationIdAndStatusAndFinancialYearGreaterThan(Integer organisationId, AnnualSubmissionStatus status, Integer financialYear);

    @Query(value = "select a.* from annual_submission a inner join annual_submission_block asb on a.id = asb.annual_submission_id where asb.id = ?1", nativeQuery = true)
    AnnualSubmission findByBlockId(Integer blockId);

    @Query(value = "select an_sub.* From  annual_submission an_sub " +
            "left join annual_submission_block rcgf on an_sub.id= rcgf.annual_submission_id and rcgf.grant_Type = 'RCGF' " +
            "left join annual_submission_block dpf on an_sub.id= dpf.annual_submission_id and dpf.grant_Type = 'DPF' " +
            "where financial_year <= ?1 and dpf.grant_Type is null", nativeQuery = true)
    Set<AnnualSubmission> findAllMissingDPFSubmissionsUpTo(Integer financialYear);



}
