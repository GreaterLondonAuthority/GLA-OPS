/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uk.gov.london.ops.domain.project.funding.FundingActivityGroup;

import java.util.List;

public interface FundingActivityGroupRepository extends JpaRepository<FundingActivityGroup, Integer> {

    List<FundingActivityGroup> findAllByBlockId(Integer blockId);

    FundingActivityGroup findByBlockIdAndYearAndQuarter(Integer blockId, Integer year, Integer quarter);

}
