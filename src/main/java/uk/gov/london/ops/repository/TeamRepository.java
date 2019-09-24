/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uk.gov.london.ops.domain.organisation.Organisation;
import uk.gov.london.ops.domain.organisation.Team;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface TeamRepository extends JpaRepository<Team, Integer> {
    Set<Team> findByOrganisationIn(Collection<Organisation> organisations);
}
