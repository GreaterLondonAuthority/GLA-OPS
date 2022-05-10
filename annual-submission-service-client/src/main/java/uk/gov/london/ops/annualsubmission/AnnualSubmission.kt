package uk.gov.london.ops.annualsubmission

/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

class AnnualSubmission (
    var id: Int?,
    var organisationId: Int?,
    var financialYear: Int?,
    var status: AnnualSubmissionStatus
)
