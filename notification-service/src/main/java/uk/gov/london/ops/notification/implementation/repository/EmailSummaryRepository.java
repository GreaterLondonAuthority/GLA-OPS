/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.notification.implementation.repository;

import com.querydsl.core.types.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import uk.gov.london.ops.notification.EmailSummary;

public interface EmailSummaryRepository extends JpaRepository<EmailSummary, Integer>,
        QuerydslPredicateExecutor<EmailSummary> {

    default Page<EmailSummary> findAll(String recipient,
                                        String subject,
                                        String body,
                                        Pageable pageable) {
        Predicate predicate = new EmailSummaryPredicateBuilder()
                .andSearchBy(recipient, subject, body)
                .getPredicate();
        if(predicate != null) {
            return findAll(predicate, pageable);
        } else {
            return findAll(pageable);
        }
    }

}
