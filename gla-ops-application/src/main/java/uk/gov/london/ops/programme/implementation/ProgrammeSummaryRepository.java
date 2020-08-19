/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.programme.implementation;

import com.querydsl.core.types.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import uk.gov.london.ops.framework.jpa.ReadOnlyRepository;
import uk.gov.london.ops.organisation.model.Organisation;
import uk.gov.london.ops.programme.domain.Programme;
import uk.gov.london.ops.programme.domain.ProgrammeSummary;

import java.util.Collection;

/**
 * Spring JPA Data Repository for Programme information.
 *
 * @author Steve Leach
 */
public interface ProgrammeSummaryRepository extends ReadOnlyRepository<ProgrammeSummary, Integer>, QuerydslPredicateExecutor<ProgrammeSummary> {

    default Page<ProgrammeSummary> findAllEnabled(Collection<Programme.Status> statuses, boolean includeRestricted,
                                                  Pageable pageable) {
        Predicate predicate = new ProgrammeSummaryQueryBuilder()
                .withEnabled(true)
                .withIncludeRestricted(includeRestricted)
                .withStatuses(statuses)
                .getPredicate();
        return findAll(predicate, pageable);
    }

    default Page<ProgrammeSummary> findAll(Collection<Organisation> organisations,
                                           Collection<Programme.Status> statuses,
                                           boolean includeRestricted,
                                           String programmeIdOrName,
                                           Pageable pageable) {
        Predicate predicate = new ProgrammeSummaryQueryBuilder()
                .withIncludeRestricted(includeRestricted)
                .withOrganisations(organisations)
                .withStatuses(statuses)
                .withProgrammeIdOrName(programmeIdOrName)
                .getPredicate();
        return findAll(predicate, pageable);
    }

}
