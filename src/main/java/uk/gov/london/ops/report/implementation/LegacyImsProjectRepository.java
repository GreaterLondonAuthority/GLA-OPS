/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.report.implementation;

import org.springframework.data.jpa.repository.JpaRepository;
import uk.gov.london.ops.report.LegacyImsProject;

public interface LegacyImsProjectRepository extends JpaRepository<LegacyImsProject, Integer> {

}
