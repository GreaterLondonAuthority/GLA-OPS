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
import uk.gov.london.ops.annualsubmission.AnnualSubmissionEntry;

import java.util.List;

public interface AnnualSubmissionEntryRepository extends JpaRepository<AnnualSubmissionEntry, Integer> {

    @Modifying
    void deleteAnnualSubmissionEntriesByBlockIdIn(List<Integer> blockId);

}
