/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.notification.implementation.repository;


import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import java.util.ArrayList;
import java.util.List;
import uk.gov.london.ops.notification.QEmailSummary;

class EmailSummaryPredicateBuilder {

    private final BooleanBuilder predicateBuilder = new BooleanBuilder();

    Predicate getPredicate() {
        return predicateBuilder.getValue();
    }

    EmailSummaryPredicateBuilder andSearchBy(String recipient, String subject, String bodyText) {
        List<Predicate> predicates = new ArrayList<>();

        if (recipient != null) {
            predicates.add(QEmailSummary.emailSummary.recipient.containsIgnoreCase(recipient));
        } else if (subject != null) {
            predicates.add(QEmailSummary.emailSummary.subject.containsIgnoreCase(subject));
        } else if (bodyText != null) {
            predicates.add(QEmailSummary.emailSummary.body.containsIgnoreCase(bodyText));
        }

        predicateBuilder.andAnyOf(predicates.toArray(new Predicate[predicates.size()]));
        return this;
    }
}

