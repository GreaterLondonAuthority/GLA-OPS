/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.organisation.implementation.repository;

import com.querydsl.core.types.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import uk.gov.london.ops.organisation.OrganisationStatus;
import uk.gov.london.ops.organisation.model.OrganisationSummary;
import uk.gov.london.ops.organisation.model.OrganisationTeam;
import uk.gov.london.ops.user.User;

import java.util.List;

public interface OrganisationSummaryRepository extends JpaRepository<OrganisationSummary, Integer>,
                                                          QuerydslPredicateExecutor<OrganisationSummary> {

    default Page<OrganisationSummary> findAll(User user, String orgIdOrName, String sapVendorId, List<Integer> entityTypes,
                                              List<OrganisationStatus> orgStatuses, List<OrganisationTeam> teams,
                                              Pageable pageable) {
        List<Integer> organisationIds = user.getAccessibleOrganisationIds();

        Predicate predicate = new OrganisationSummaryPredicateBuilder()
                .withOrganisations(organisationIds)
                .withSearchText(orgIdOrName)
                .withSapVendorId(sapVendorId)
                .withEntityTypes(entityTypes)
                .withOrgStatuses(orgStatuses)
                .withTeams(teams)
                .getPredicate();
        if (predicate != null) {
            return findAll(predicate, pageable);
        } else {
            return findAll(pageable);
        }
    }

    List<OrganisationSummary> getOrganisationSummariesByEntityType(Integer org);

}
