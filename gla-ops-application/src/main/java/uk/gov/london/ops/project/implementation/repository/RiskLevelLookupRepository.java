/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.project.implementation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uk.gov.london.ops.project.risk.RiskLevelID;
import uk.gov.london.ops.project.risk.RiskLevelLookup;

public interface RiskLevelLookupRepository extends JpaRepository<RiskLevelLookup, RiskLevelID> {


}
