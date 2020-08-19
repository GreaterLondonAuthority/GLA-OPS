/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.audit.implementation;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import uk.gov.london.ops.audit.QAuditableActivityEntity;

import java.time.OffsetDateTime;

class AuditableActivityQueryBuilder {

    private final BooleanBuilder predicateBuilder = new BooleanBuilder();

    Predicate getPredicate() {
        return predicateBuilder.getValue();
    }

    AuditableActivityQueryBuilder withUsername(String username) {
        if (username != null) {
            predicateBuilder.and(QAuditableActivityEntity.auditableActivityEntity.userName.containsIgnoreCase(username));
        }
        return this;
    }


    AuditableActivityQueryBuilder withFromDate(OffsetDateTime fromDate) {
        if (fromDate != null) {
            predicateBuilder.and(QAuditableActivityEntity.auditableActivityEntity.timestamp.goe(fromDate));
        }
        return this;
    }

    AuditableActivityQueryBuilder withToDate(OffsetDateTime toDate) {
        if (toDate != null) {
            predicateBuilder.and(QAuditableActivityEntity.auditableActivityEntity.timestamp.loe(toDate));
        }
        return this;
    }

}
