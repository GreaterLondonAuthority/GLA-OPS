/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.annualsubmission

class AnnualSubmissionBlockTemplate(
        var financialYear: Int?,
        var rolloverEnabled: Boolean,
        var amountOfForecastYears: Int,
        var amountOfExpirationYears: Int
)
