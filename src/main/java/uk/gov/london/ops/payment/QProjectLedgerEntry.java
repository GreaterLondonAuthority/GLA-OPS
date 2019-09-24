/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.payment;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.*;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.querydsl.core.types.PathMetadataFactory.forVariable;
import static uk.gov.london.common.GlaUtils.parseInt;

// This HAS to be called QProjectLedgerEntry otherwise the JPA repository will not instantiate
public class QProjectLedgerEntry extends EntityPathBase<ProjectLedgerEntry> {

    // This HAS to exist, otherwise the JPA repository will not instantiate
    public static final QProjectLedgerEntry projectLedgerEntry = new QProjectLedgerEntry();

    public final NumberPath<Integer> projectId = createNumber("projectId", Integer.class);

    public final StringPath projectName = createString("projectName");

    public final StringPath vendorName = createString("vendorName");

    public final StringPath category = createString("category");

    public final StringPath programmeName = createString("programmeName");

    public final DatePath authorisedOn = createDate("authorisedOn", Date.class);

    public final EnumPath<LedgerStatus> ledgerStatus = createEnum("ledgerStatus", LedgerStatus.class);

    public final EnumPath<PaymentSource> paymentSource = createEnum("paymentSource", PaymentSource.class);

    private BooleanBuilder predicateBuilder = new BooleanBuilder();

    public QProjectLedgerEntry() {
        super(ProjectLedgerEntry.class, forVariable("projectLedgerEntry"));
    }

    public void andSearch(String projectIdOrName, String organisationName) {
        List<Predicate> predicates = new ArrayList<>();

        if (projectIdOrName != null) {
            predicates.add(this.projectName.containsIgnoreCase(projectIdOrName));
            Integer projectId = getProjectId(projectIdOrName);
            if (projectId != null) {
                predicates.add(this.projectId.eq(projectId));
            }
        }
        else if (organisationName != null) {
            predicates.add(this.vendorName.containsIgnoreCase(organisationName));
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

    public void andPaymentSources(List<PaymentSource> paymentSources) {
        List<Predicate> predicates = new ArrayList<>();

        if (paymentSources != null) {
            predicates.add(this.paymentSource.in(paymentSources));
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
}
