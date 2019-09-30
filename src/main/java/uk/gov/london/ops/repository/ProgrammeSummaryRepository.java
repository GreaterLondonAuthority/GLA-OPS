/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import uk.gov.london.common.GlaUtils;
import uk.gov.london.ops.domain.organisation.Organisation;
import uk.gov.london.ops.domain.template.Programme;
import uk.gov.london.ops.domain.template.ProgrammeSummary;
import uk.gov.london.ops.domain.template.QProgrammeSummary;

import java.util.Collection;

/**
 * Spring JPA Data Repository for Programme information.
 *
 * @author Steve Leach
 */
public interface ProgrammeSummaryRepository extends ReadOnlyRepository<ProgrammeSummary, Integer>, QuerydslPredicateExecutor<ProgrammeSummary> {

    Page<ProgrammeSummary> findAllByEnabledAndStatusIn(boolean enabled, Collection<Programme.Status> status, Pageable pageable);
    
    default Page<ProgrammeSummary> findAllByEnabledAndStatusInAndRestricted(boolean enabled, Collection<Programme.Status> status,boolean restricted,Pageable pageable){
        QProgrammeSummary query = new QProgrammeSummary();
        query.andSearch(enabled,restricted,status);
        return findAll(query.getPredicate(), pageable);
 }

    default Page<ProgrammeSummary> findAll(Collection<Organisation> organisations,
                                           Collection<Programme.Status> status,
                                           boolean includeRestricted,
                                           String programmeText,
                                           Pageable pageable) {

        QProgrammeSummary query = new QProgrammeSummary();

        Integer programmeId = GlaUtils.parseInt(programmeText);
        query.andSearch(programmeText,programmeId, organisations,status, includeRestricted);

        return findAll(query.getPredicate(), pageable);
    }
}
