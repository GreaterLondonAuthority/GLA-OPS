/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.user.implementation;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import uk.gov.london.ops.framework.jpa.ReadOnlyRepository;
import uk.gov.london.ops.user.User;
import uk.gov.london.ops.user.domain.UserRoleSummary;

import java.util.List;

public interface UserRoleSummaryRepository extends ReadOnlyRepository<UserRoleSummary, Integer>,
        QuerydslPredicateExecutor<UserRoleSummary> {

    default Page<UserRoleSummary> findAll(User currentUser,
                                          String organisationNameOrId,
                                          String userNameOrEmail,
                                          List<String> registrationStatus,
                                          List<String> roles,
                                          List<Integer> orgTypes,
                                          List<String> spendAuthority,
                                          Pageable pageable) {
        UserRoleSummaryPredicateBuilder query = new UserRoleSummaryPredicateBuilder();

        query.build(currentUser, currentUser.getOrganisationIds());

        query.andSearchBy(organisationNameOrId, userNameOrEmail);
        query.andRegistrationStatus(registrationStatus);
        query.andUserRole(roles);
        query.andOrgTypes(orgTypes);
        query.andSpendAuthority(spendAuthority);

        return findAll(query.getPredicate(), pageable);
    }

    default Iterable<UserRoleSummary> findAll(List<Integer> organisationIds, List<String> roles) {
        UserRoleSummaryPredicateBuilder query = new UserRoleSummaryPredicateBuilder();
        query.andOrganisationIds(organisationIds);
        query.andUserRole(roles);
        return findAll(query.getPredicate());
    }

}
