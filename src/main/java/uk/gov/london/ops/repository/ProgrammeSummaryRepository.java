/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.repository;

import uk.gov.london.ops.domain.organisation.Organisation;
import uk.gov.london.ops.domain.template.ProgrammeSummary;

import java.util.Collection;
import java.util.List;

/**
 * Spring JPA Data Repository for Programme information.
 *
 * @author Steve Leach
 */
public interface ProgrammeSummaryRepository extends ReadOnlyRepository<ProgrammeSummary, Integer> {

    List<ProgrammeSummary> findAllByRestricted(boolean restricted);

    List<ProgrammeSummary> findAllByEnabled(boolean enabled);

    List<ProgrammeSummary> findAllByRestrictedAndEnabled(boolean restricted, boolean enabled);

    List<ProgrammeSummary> findAllByManagingOrganisationIn(Collection<Organisation> organisations);

}
