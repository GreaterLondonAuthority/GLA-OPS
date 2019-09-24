/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import uk.gov.london.ops.domain.organisation.OrganisationProgrammeSummary;

import java.util.List;

public interface OrganisationProgrammeSummaryRepository extends JpaRepository<OrganisationProgrammeSummary, Integer> {

    @Query(value = "select * from v_organisation_programmes_with_budgets where org_id = ?1", nativeQuery = true)
    List<OrganisationProgrammeSummary> findAllForOrganisation(Integer organisationId);

}
