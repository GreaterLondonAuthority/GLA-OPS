/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import uk.gov.london.ops.domain.organisation.*;
import uk.gov.london.ops.domain.user.Role;
import uk.gov.london.ops.domain.user.User;

import java.util.ArrayList;
import java.util.List;

import static uk.gov.london.common.user.BaseRole.ORG_ADMIN;

public interface OrganisationSummaryRepository extends JpaRepository<OrganisationSummary, Integer> , QuerydslPredicateExecutor<OrganisationSummary> {

    default Page<OrganisationSummary> findAll(User user, String searchText, List<Integer> entityTypes, List<OrganisationStatus> orgStatuses, List<RegistrationStatus> userRegStatuses, List<OrganisationTeam> teams, Pageable pageable) {
        List<Integer> organisationIds = new ArrayList<>();
        for (Role role: user.getRoles()) {
            if (role.isApproved() || ORG_ADMIN.equals(role.getName())) {
                organisationIds.add(role.getOrganisation().getId());
            }
        }

        QOrganisationSummary query = new QOrganisationSummary();
        query.build(organisationIds, searchText, entityTypes, orgStatuses, userRegStatuses, teams);
        return findAll(query.getPredicate(), pageable);
    }

    List<OrganisationSummary> getOrganisationSummariesByEntityType(Integer org);

}
