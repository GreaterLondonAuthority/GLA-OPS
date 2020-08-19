/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.project.implementation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uk.gov.london.ops.project.funding.FundingActivity;

import java.util.List;

public interface FundingActivityRepository extends JpaRepository<FundingActivity, Integer> {

    List<FundingActivity> findAllByBlockId(Integer blockId);

    List<FundingActivity> findAllByBlockIdAndYear(Integer blockId, Integer year);

}
