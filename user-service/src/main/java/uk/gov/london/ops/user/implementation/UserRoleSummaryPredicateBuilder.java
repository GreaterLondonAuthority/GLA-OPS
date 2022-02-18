/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.user.implementation;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import uk.gov.london.ops.organisation.OrganisationType;
import uk.gov.london.ops.user.User;
import uk.gov.london.ops.user.domain.QUserRoleSummary;

import java.util.ArrayList;
import java.util.List;

import static uk.gov.london.common.GlaUtils.parseInt;

class UserRoleSummaryPredicateBuilder {

    private final BooleanBuilder predicateBuilder = new BooleanBuilder();
    
    void build(User currentUser, List<Integer> organisationIds) {
        List<Predicate> predicates = new ArrayList<>();
        predicates.add(QUserRoleSummary.userRoleSummary.username.eq(currentUser.getUsername()));
        predicates.add(QUserRoleSummary.userRoleSummary.organisationId.in(organisationIds));
        predicates.add(QUserRoleSummary.userRoleSummary.managingOrganisationId.in(organisationIds));
        if (currentUser.isGla()) {
            predicates.add(QUserRoleSummary.userRoleSummary.entityTypeId.in(OrganisationType.getInternalOrganisationTypesIds()));
        }

        predicateBuilder.andAnyOf(predicates.toArray(new Predicate[predicates.size()]));
    }

    void andSearchBy(String organisationNameOrId, String userNameOrEmail) {
        List<Predicate> predicates = new ArrayList<>();

        if (organisationNameOrId != null) {
            predicates.add(QUserRoleSummary.userRoleSummary.orgName.containsIgnoreCase(organisationNameOrId));
            Integer organisationId = parseInt(organisationNameOrId);
            if (organisationId != null) {
                predicates.add(QUserRoleSummary.userRoleSummary.organisationId.eq(organisationId));
            }
        } else if (userNameOrEmail != null) {
            predicates.add(QUserRoleSummary.userRoleSummary.username.containsIgnoreCase(userNameOrEmail));
            String[] split = userNameOrEmail.split(" ");
            if (split.length == 1) {
                predicates.add(QUserRoleSummary.userRoleSummary.firstName.startsWithIgnoreCase(split[0]));
                predicates.add(QUserRoleSummary.userRoleSummary.lastName.startsWithIgnoreCase(split[0]));
            } else if (split.length == 2) {
                List<Predicate> predicates2 = new ArrayList<>();
                BooleanBuilder bb = new BooleanBuilder();
                predicates2.add(QUserRoleSummary.userRoleSummary.firstName.startsWithIgnoreCase(split[0]));
                predicates2.add(QUserRoleSummary.userRoleSummary.lastName.startsWithIgnoreCase(split[1]));
                bb.orAllOf(predicates2.toArray(new Predicate[predicates2.size()]));
                predicates.add(bb);
            }
        }
        predicateBuilder.andAnyOf(predicates.toArray(new Predicate[predicates.size()]));
    }

    void andRegistrationStatus(List<String> registrationStatus) {
        List<Predicate> predicates = new ArrayList<>();

        if (registrationStatus != null && registrationStatus.size() == 1) {
            if (registrationStatus.contains("Approved")) {
                predicates.add(QUserRoleSummary.userRoleSummary.approved.eq(true));
            } else if (registrationStatus.contains("Pending")) {
                predicates.add(QUserRoleSummary.userRoleSummary.approved.isNull());
                predicates.add(QUserRoleSummary.userRoleSummary.approved.eq(false));
            }
        }

        predicateBuilder.andAnyOf(predicates.toArray(new Predicate[predicates.size()]));
    }

    void andOrganisationIds(List<Integer> organisationIds) {
        List<Predicate> predicates = new ArrayList<>();
        if (organisationIds != null && !organisationIds.isEmpty()) {
            predicates.add(QUserRoleSummary.userRoleSummary.organisationId.in(organisationIds));
            predicateBuilder.andAnyOf(predicates.toArray(new Predicate[predicates.size()]));
        }
    }

    void andUserRole(List<String> roles) {
        List<Predicate> predicates = new ArrayList<>();
        if (roles != null && !roles.isEmpty()) {
            predicates.add(QUserRoleSummary.userRoleSummary.role.in(roles));
            predicateBuilder.andAnyOf(predicates.toArray(new Predicate[predicates.size()]));
        }
    }

    void andOrgTypes(List<Integer> orgTypes) {
        List<Predicate> predicates = new ArrayList<>();
        if (orgTypes != null) {
            predicates.add(QUserRoleSummary.userRoleSummary.entityTypeId.in(orgTypes));
            predicateBuilder.andAnyOf(predicates.toArray(new Predicate[predicates.size()]));
        }
    }

    void andSpendAuthority(List<String> spendAuthority) {
        List<Predicate> predicates = new ArrayList<>();
        List<Predicate> predicates2;
        BooleanBuilder bb;
        if (spendAuthority != null && spendAuthority.contains("pendingChanges")) {
            bb = new BooleanBuilder();
            predicates2 = new ArrayList<>();
            predicates2.add(QUserRoleSummary.userRoleSummary.pendingThreshold.isNotNull());
            bb.orAllOf(predicates2.toArray(new Predicate[predicates2.size()]));
            predicates.add(bb);
        }
        if (spendAuthority != null && spendAuthority.contains("notSet")) {
            bb = new BooleanBuilder();
            predicates2 = new ArrayList<>();
            predicates2.add(QUserRoleSummary.userRoleSummary.pendingThreshold.isNull());
            predicates2.add(QUserRoleSummary.userRoleSummary.approvedThreshold.isNull());
            predicates2.add(QUserRoleSummary.userRoleSummary.canHaveThreshold.eq(true));
            bb.orAllOf(predicates2.toArray(new Predicate[predicates2.size()]));
            predicates.add(bb);
        }
        if (spendAuthority != null && spendAuthority.contains("usersWithSpendAuthority")) {
            bb = new BooleanBuilder();
            predicates2 = new ArrayList<>();
            predicates2.add(QUserRoleSummary.userRoleSummary.pendingThreshold.isNull());
            predicates2.add(QUserRoleSummary.userRoleSummary.approvedThreshold.isNotNull());
            bb.orAllOf(predicates2.toArray(new Predicate[predicates2.size()]));
            predicates.add(bb);
        }
        predicateBuilder.andAnyOf(predicates.toArray(new Predicate[predicates.size()]));
    }

    Predicate getPredicate() {
        return predicateBuilder.getValue();
    }

}

