/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.annualsubmission;


import java.util.List;

public interface AnnualSubmissionService {

    List<AnnualSubmission> getAnnualSubmissions(Integer organisationId);

    List<AnnualSubmissionBlock> getAnnualSubmissionBlocks(Integer organisationId, Integer financialYear);
}
