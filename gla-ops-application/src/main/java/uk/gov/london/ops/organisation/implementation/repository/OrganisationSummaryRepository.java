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
import uk.gov.london.ops.organisation.model.OrganisationStatus;
import uk.gov.london.ops.organisation.model.OrganisationSummary;
import uk.gov.london.ops.organisation.model.OrganisationTeam;
import uk.gov.london.ops.role.model.Role;
import uk.gov.london.ops.user.domain.User;

import java.util.ArrayList;
import java.util.List;

import static uk.gov.london.common.user.BaseRole.ORG_ADMIN;

public interface OrganisationSummaryRepository extends JpaRepository<OrganisationSummary, Integer>,
                                                          QuerydslPredicateExecutor<OrganisationSummary> {

    default Page<OrganisationSummary> findAll(User user, String searchText, List<Integer> entityTypes,
                                              List<OrganisationStatus> orgStatuses, List<OrganisationTeam> teams,
                                              Pageable pageable) {
        List<Integer> organisationIds = new ArrayList<>();
        for (Role role: user.getRoles()) {
            if (role.isApproved() || ORG_ADMIN.equals(role.getName())) {
                organisationIds.add(role.getOrganisation().getId());
            }
        }

        OrganisationSummaryPredicateBuilder query = new OrganisationSummaryPredicateBuilder();
        query.build(organisationIds, searchText, entityTypes, orgStatuses, teams);
        Predicate predicate = query.getPredicate();
        if (predicate != null) {
            return findAll(query.getPredicate(), pageable);
        } else {
            return findAll(pageable);
        }
    }

    List<OrganisationSummary> getOrganisationSummariesByEntityType(Integer org);

}
