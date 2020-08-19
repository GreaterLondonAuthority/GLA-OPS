/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.annualsubmission.implementation;

import org.springframework.data.jpa.repository.JpaRepository;
import uk.gov.london.ops.annualsubmission.AnnualSubmissionCategory;

public interface AnnualSubmissionCategoryRepository extends JpaRepository<AnnualSubmissionCategory, Integer> {

}
