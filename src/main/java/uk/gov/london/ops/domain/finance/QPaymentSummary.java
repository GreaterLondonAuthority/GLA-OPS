/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.domain.finance;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.*;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.querydsl.core.types.PathMetadataFactory.forVariable;
import static uk.gov.london.ops.util.GlaOpsUtils.parseInt;

// This HAS to be called QProjectLedgerEntry otherwise the JPA repository will not instantiate
public class QPaymentSummary extends EntityPathBase<PaymentSummary> {

    // This HAS to exist, otherwise the JPA repository will not instantiate
    public static final QPaymentSummary projectLedgerEntry = new QPaymentSummary();

    public final NumberPath<Integer> projectId = createNumber("projectId", Integer.class);

    public final StringPath projectName = createString("projectName");

    public final StringPath vendorName = createString("vendorName");
    public final StringPath organisationId = createString("organisationId");

    public final StringPath category = createString("category");

    public final StringPath programmeName = createString("programmeName");

    public final DatePath authorisedOn = createDate("authorisedOn", Date.class);

    public final EnumPath<LedgerStatus> ledgerStatus = createEnum("ledgerStatus", LedgerStatus.class);

    public final EnumPath<LedgerType> ledgerType = createEnum("ledgerType", LedgerType.class);

    private BooleanBuilder predicateBuilder = new BooleanBuilder();

    private BooleanPath reclaimOfPaymentId = createBoolean("reclaimOfPaymentId");

    public final NumberPath<Integer> organiationId = createNumber("organisationId", Integer.class);

    public final NumberPath<Integer> managingOrganisation = createNumber("managingOrganisation", Integer.class);


    public QPaymentSummary() {
        super(PaymentSummary.class, forVariable("paymentSummary"));
    }

    public void andSearch(String projectIdOrName, String organisationIdOrName) {
        List<Predicate> predicates = new ArrayList<>();

        if (projectIdOrName != null) {
            predicates.add(this.projectName.containsIgnoreCase(projectIdOrName));
            Integer projectId = getProjectId(projectIdOrName);
            if (projectId != null) {
                predicates.add(this.projectId.eq(projectId));
            }
        }
        else if (organisationIdOrName != null) {
            predicates.add(this.vendorName.containsIgnoreCase(organisationIdOrName));
            Integer organisationId = parseInt(organisationIdOrName);
            if (organisationId != null) {
                predicates.add(this.organiationId.eq(organisationId));
            }
        }
        predicateBuilder.andAnyOf(predicates.toArray(new Predicate[predicates.size()]));
    }

    private Integer getProjectId(String project) {
        if (project.startsWith("P") || project.startsWith("p")) {
            project = project.substring(1);
        }
        return parseInt(project);
    }

    public void andStatuses(List<LedgerStatus> statuses) {
        List<Predicate> predicates = new ArrayList<>();

        if (statuses != null) {
            predicates.add(this.ledgerStatus.in(statuses));
        }

        predicateBuilder.andAnyOf(predicates.toArray(new Predicate[predicates.size()]));
    }

    public Predicate getPredicate() {
        return predicateBuilder.getValue();
    }

    public void andSources(List<LedgerType> relevantSources) {
        List<Predicate> predicates = new ArrayList<>();

        if (relevantSources != null) {
            predicates.add(this.ledgerType.in(relevantSources));
        }

        predicateBuilder.andAnyOf(predicates.toArray(new Predicate[predicates.size()]));
    }

    public void andCategories(List<String> categories) {
        if (categories != null) {
            predicateBuilder.and(this.category.in(categories));
        }
    }

    public void andProgrammes(List<String> relevantProgrammes) {
        List<Predicate> predicates = new ArrayList<>();

        if (relevantProgrammes != null) {
            predicates.add(this.programmeName.in(relevantProgrammes));
        }

        predicateBuilder.andAnyOf(predicates.toArray(new Predicate[predicates.size()]));
    }

    public  void andAuthorisedDates(OffsetDateTime fromDate, OffsetDateTime toDate) {

        if (fromDate != null) {
            predicateBuilder.and(this.authorisedOn.goe(fromDate));
        }
        if (toDate != null) {
            predicateBuilder.and(this.authorisedOn.loe(toDate));
        }
    }

    public void andOrganisations(List<Integer> orgIds) {

        Predicate[] predicates = new Predicate[] {
                this.organiationId.in(orgIds),
                // OR
                this.managingOrganisation.in(orgIds)
        };

        predicateBuilder.andAnyOf(predicates);
    }


    public void andFilterReclaims(List<String> paymentDirection){
        if (paymentDirection != null && paymentDirection.size() == 1) {
            if(paymentDirection.get(0).equalsIgnoreCase("OUT")){
                predicateBuilder.and(this.reclaimOfPaymentId.isNull());
            } else if(paymentDirection.get(0).equalsIgnoreCase("IN")){
                predicateBuilder.and(this.reclaimOfPaymentId.isNotNull());
            }
        }
    }
}
