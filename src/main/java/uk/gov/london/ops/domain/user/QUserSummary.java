/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.domain.user;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static com.querydsl.core.types.PathMetadataFactory.forVariable;
import static uk.gov.london.common.GlaUtils.parseInt;

// This HAS to be called QUserSummary otherwise the JPA repository will not instantiate
public class QUserSummary extends EntityPathBase<UserSummary> {

    // This HAS to exist, otherwise the JPA repository will not instantiate
    public static final QUserSummary userSummary = new QUserSummary();

    public final StringPath username = createString("username");
    public final StringPath firstName = createString("firstName");
    public final StringPath lastName = createString("lastName");

    public final BooleanPath approved = createBoolean("approved");
    public final StringPath role = createString("role");
    public final NumberPath<Integer> entityTypeId = createNumber("entityTypeId", Integer.class);

    public final NumberPath<Integer> pendingThreshold = createNumber("pendingThreshold", Integer.class);
    public final NumberPath<Integer> approvedThreshold = createNumber("approvedThreshold", Integer.class);
    public final BooleanPath canHaveThreshold = createBoolean("canHaveThreshold");

    public final NumberPath<Integer> organisationId = createNumber("organisationId", Integer.class);
    public final StringPath orgName = createString("orgName");

    public final NumberPath<Integer> managingOrganisationId = createNumber("managingOrganisationId", Integer.class);

    private BooleanBuilder predicateBuilder = new BooleanBuilder();

    public QUserSummary() {
        super(UserSummary.class, forVariable("userSummary"));
    }

    public void build(String currentUsername, List<Integer> organisationIds) {
        Predicate[] predicates = new Predicate[]{
                this.username.eq(currentUsername),
                this.organisationId.in(organisationIds),
                this.managingOrganisationId.in(organisationIds)
        };

        predicateBuilder.andAnyOf(predicates);
    }

    public void andSearchBy(String organisationNameOrId, String userNameOrEmail) {
        List<Predicate> predicates = new ArrayList<>();

        if (organisationNameOrId != null) {
            predicates.add(this.orgName.containsIgnoreCase(organisationNameOrId));
            Integer organisationId = parseInt(organisationNameOrId);
            if (organisationId != null) {
                predicates.add(this.organisationId.eq(organisationId));
            }
        }
        else if (userNameOrEmail != null) {
            predicates.add(this.username.containsIgnoreCase(userNameOrEmail));
            String[] split = userNameOrEmail.split(" ");
            if(split.length == 1) {
                predicates.add(this.firstName.startsWithIgnoreCase(split[0]));
                predicates.add(this.lastName.startsWithIgnoreCase(split[0]));
            }else if(split.length == 2){
                List<Predicate> predicates2 = new ArrayList<>();
                BooleanBuilder bb = new BooleanBuilder();
                predicates2.add(this.firstName.startsWithIgnoreCase(split[0]));
                predicates2.add(this.lastName.startsWithIgnoreCase(split[1]));
                bb.orAllOf(predicates2.toArray(new Predicate[predicates2.size()]));
                predicates.add(bb);
            }
        }
        predicateBuilder.andAnyOf(predicates.toArray(new Predicate[predicates.size()]));
    }

    public void andRegistrationStatus(List<String> registrationStatus){
        List<Predicate> predicates = new ArrayList<>();

        if (registrationStatus != null && registrationStatus.size() == 1) {
            if(registrationStatus.contains("Approved")){
                predicates.add(this.approved.eq(true));
            } else if(registrationStatus.contains("Pending")){
                predicates.add(this.approved.isNull());
                predicates.add(this.approved.eq(false));
            }
        }

        predicateBuilder.andAnyOf(predicates.toArray(new Predicate[predicates.size()]));
    }

    public void andUserRole(List<String> roles){
        List<Predicate> predicates = new ArrayList<>();
        if(roles != null){
            predicates.add(this.role.in(roles));
            predicateBuilder.andAnyOf(predicates.toArray(new Predicate[predicates.size()]));
        }
    }

    public void andOrgTypes(List<Integer> orgTypes){
        List<Predicate> predicates = new ArrayList<>();
        if(orgTypes != null){
            predicates.add(this.entityTypeId.in(orgTypes));
            predicateBuilder.andAnyOf(predicates.toArray(new Predicate[predicates.size()]));
        }
    }

    public void andSpendAuthority(List<String> spendAuthority){
        List<Predicate> predicates = new ArrayList<>();
        List<Predicate> predicates2;
        BooleanBuilder bb;
        if(spendAuthority != null && spendAuthority.contains("pendingChanges")){
            bb = new BooleanBuilder();
            predicates2 = new ArrayList<>();
            predicates2.add(this.pendingThreshold.isNotNull());
//            predicates2.add(this.approvedThreshold.isNotNull());
            bb.orAllOf(predicates2.toArray(new Predicate[predicates2.size()]));
            predicates.add(bb);
        }

        if(spendAuthority != null && spendAuthority.contains("notSet")){
            bb = new BooleanBuilder();
            predicates2 = new ArrayList<>();
            predicates2.add(this.pendingThreshold.isNull());
            predicates2.add(this.approvedThreshold.isNull());
            predicates2.add(this.canHaveThreshold.eq(true));
            bb.orAllOf(predicates2.toArray(new Predicate[predicates2.size()]));
            predicates.add(bb);
        }
        if(spendAuthority != null && spendAuthority.contains("usersWithSpendAuthority")){
            bb = new BooleanBuilder();
            predicates2 = new ArrayList<>();
            predicates2.add(this.pendingThreshold.isNull());
            predicates2.add(this.approvedThreshold.isNotNull());
            bb.orAllOf(predicates2.toArray(new Predicate[predicates2.size()]));
            predicates.add(bb);
        }


        predicateBuilder.andAnyOf(predicates.toArray(new Predicate[predicates.size()]));
    }

    public Predicate getPredicate() {
        return predicateBuilder.getValue();
    }

}

