/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.programme.implementation;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import uk.gov.london.ops.organisation.model.OrganisationEntity;
import uk.gov.london.ops.programme.domain.Programme;
import uk.gov.london.ops.programme.domain.QProgrammeSummary;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.london.common.GlaUtils.parseInt;

class ProgrammeSummaryQueryBuilder {

    private final BooleanBuilder predicateBuilder = new BooleanBuilder();

    Predicate getPredicate() {
        return predicateBuilder.getValue();
    }

    ProgrammeSummaryQueryBuilder withEnabled(boolean enabled) {
        predicateBuilder.and(QProgrammeSummary.programmeSummary.enabled.eq(enabled));
        return this;
    }

    ProgrammeSummaryQueryBuilder withIncludeRestricted(boolean includeRestricted) {
        if (!includeRestricted) {
            predicateBuilder.and(QProgrammeSummary.programmeSummary.restricted.eq(false));
        }
        return this;
    }

    ProgrammeSummaryQueryBuilder withStatuses(Collection<Programme.Status> statuses) {
        if (isNotEmpty(statuses)) {
            predicateBuilder.and(QProgrammeSummary.programmeSummary.status.in(statuses));
        }
        return this;
    }

    ProgrammeSummaryQueryBuilder withOrganisations(Collection<OrganisationEntity> organisations) {
        if (isNotEmpty(organisations)) {
            predicateBuilder.and(QProgrammeSummary.programmeSummary.managingOrganisation.in(organisations));
        }
        return this;
    }

    ProgrammeSummaryQueryBuilder withProgrammeIdOrName(String programmeIdOrName) {
        List<Predicate> programmePredicates = new ArrayList<>();
        Integer programmeId = parseInt(programmeIdOrName);
        if (programmeId != null) {
            programmePredicates.add(QProgrammeSummary.programmeSummary.id.eq(programmeId));
        }
        if (programmeIdOrName != null) {
            programmePredicates.add(QProgrammeSummary.programmeSummary.name.containsIgnoreCase(programmeIdOrName));
        }
        predicateBuilder.andAnyOf(programmePredicates.toArray(new Predicate[programmePredicates.size()]));
        return this;
    }

}
