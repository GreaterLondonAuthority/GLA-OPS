/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.audit;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.*;

import java.time.OffsetDateTime;
import java.util.Date;

import static com.querydsl.core.types.PathMetadataFactory.forVariable;

public class QAuditableActivity extends EntityPathBase<AuditableActivity> {

    // This HAS to exist, otherwise the JPA repository will not instantiate
    public static final QAuditableActivity auditableActivity = new QAuditableActivity();

    public final StringPath userName = createString("userName");

    public final DatePath timestamp = createDate("timestamp", Date.class);

    private BooleanBuilder predicateBuilder = new BooleanBuilder();

    public QAuditableActivity() {
        super(AuditableActivity.class, forVariable("auditableActivity"));
    }

    public void andUsername(String username) {
        if (username != null) {
            predicateBuilder.and(this.userName.containsIgnoreCase(username));
        }
    }

    public  void andTimestamp(OffsetDateTime fromDate, OffsetDateTime toDate) {
        if (fromDate != null) {
            predicateBuilder.and(this.timestamp.goe(fromDate));
        }
        if (toDate != null) {
            predicateBuilder.and(this.timestamp.loe(toDate));
        }
    }

    public BooleanBuilder getPredicateBuilder() {
        return predicateBuilder;
    }
}
