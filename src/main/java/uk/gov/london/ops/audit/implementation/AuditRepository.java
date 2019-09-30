/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.audit.implementation;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import uk.gov.london.ops.audit.AuditableActivity;
import uk.gov.london.ops.audit.QAuditableActivity;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Spring Data repository for Audit records.
 *
 * @author Steve Leach
 */
public interface AuditRepository extends JpaRepository<AuditableActivity, Integer>, QuerydslPredicateExecutor<AuditableActivity> {

    List<AuditableActivity> findAllBySummaryContainingIgnoreCase(String summary);

    Page<AuditableActivity> findAll(Pageable pageable);

    @Query(value = "select * from audit_activity order by activity_time desc", nativeQuery = true)
    List<AuditableActivity> findMostRecent();

    default Page<AuditableActivity> findAll(String username,
                                            OffsetDateTime fromDate,
                                            OffsetDateTime toDate,
                                            Pageable pageable) {
        QAuditableActivity query = new QAuditableActivity();
        query.andUsername(username);
        query.andTimestamp(fromDate, toDate);
        return findAll(query.getPredicateBuilder(), pageable);
    }
}
