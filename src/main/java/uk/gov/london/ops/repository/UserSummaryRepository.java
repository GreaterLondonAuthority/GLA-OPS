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
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import uk.gov.london.ops.domain.user.QUserSummary;
import uk.gov.london.ops.domain.user.User;
import uk.gov.london.ops.domain.user.UserSummary;

import java.util.List;

public interface UserSummaryRepository extends ReadOnlyRepository<UserSummary, Integer>, QueryDslPredicateExecutor<UserSummary> {

    default Page<UserSummary> findAll(User currentUser,
                                      String organisationNameOrId,
                                      String userNameOrEmail,
                                      List<String> registrationStatus,
                                      List<String> roles,
                                      List<Integer> orgTypes,
                                      List<String> spendAuthority,
                                      Pageable pageable) {
        QUserSummary query = new QUserSummary();

        query.build(currentUser.getUsername(), currentUser.getOrganisationIds());

        query.andSearchBy(organisationNameOrId, userNameOrEmail);
        query.andRegistrationStatus(registrationStatus);
        query.andUserRole(roles);
        query.andOrgTypes(orgTypes);
        query.andSpendAuthority(spendAuthority);

        return findAll(query.getPredicate(), pageable);
    }




}
