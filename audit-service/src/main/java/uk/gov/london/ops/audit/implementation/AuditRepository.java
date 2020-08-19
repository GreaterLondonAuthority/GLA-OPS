/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.audit.implementation;

import com.querydsl.core.types.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import uk.gov.london.ops.audit.AuditableActivityEntity;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Spring Data repository for Audit records.
 *
 * @author Steve Leach
 */
public interface AuditRepository extends JpaRepository<AuditableActivityEntity, Integer>,
        QuerydslPredicateExecutor<AuditableActivityEntity> {

    List<AuditableActivityEntity> findAllBySummaryContainingIgnoreCase(String summary);

    default Page<AuditableActivityEntity> findAll(String username,
                                                  OffsetDateTime fromDate,
                                                  OffsetDateTime toDate,
                                                  Pageable pageable) {
        Predicate predicate = new AuditableActivityQueryBuilder()
                .withUsername(username)
                .withFromDate(fromDate)
                .withToDate(toDate)
                .getPredicate();
        if (predicate != null) {
            return findAll(predicate, pageable);
        } else {
            return findAll(pageable);
        }
    }

}
