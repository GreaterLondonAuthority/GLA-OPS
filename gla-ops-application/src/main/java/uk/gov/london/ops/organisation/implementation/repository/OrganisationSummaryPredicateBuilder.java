/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.organisation.implementation.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import uk.gov.london.ops.organisation.model.OrganisationStatus;
import uk.gov.london.ops.organisation.model.OrganisationTeam;
import uk.gov.london.ops.organisation.model.QOrganisationSummary;

import java.util.ArrayList;
import java.util.List;

public class OrganisationSummaryPredicateBuilder {

    private final BooleanBuilder predicateBuilder = new BooleanBuilder();

    public void build(List<Integer> organisations, String searchText, List<Integer> entityTypes,
                                        List<OrganisationStatus> orgStatuses, List<OrganisationTeam> teams) {
        Predicate[] predicates = new Predicate[] {
                QOrganisationSummary.organisationSummary.id.in(organisations),
                QOrganisationSummary.organisationSummary.managingOrganisationId.in(organisations)
        };

        predicateBuilder.andAnyOf(predicates);

        if (StringUtils.isNotEmpty(searchText)) {
            List<Predicate> searchTextPredicates = new ArrayList<>();

            if (NumberUtils.isNumber(searchText)) {
                searchTextPredicates.add(QOrganisationSummary.organisationSummary.id.eq(Integer.parseInt(searchText)));
            }

            searchTextPredicates.add(QOrganisationSummary.organisationSummary.name.containsIgnoreCase(searchText));

            predicateBuilder.andAnyOf(searchTextPredicates.toArray(new Predicate[searchTextPredicates.size()]));
        }

        if (CollectionUtils.isNotEmpty(entityTypes)) {
            predicateBuilder.and(QOrganisationSummary.organisationSummary.entityType.in(entityTypes));
        }

        if (CollectionUtils.isNotEmpty(orgStatuses)) {
            predicateBuilder.and(QOrganisationSummary.organisationSummary.status.in(orgStatuses));
        }

        if (CollectionUtils.isNotEmpty(teams)) {
            List<Predicate> predicateList = new ArrayList<>();
            for (OrganisationTeam team : teams) {
                if (team.getTeamId() != null) {
                    predicateList.add(QOrganisationSummary.organisationSummary.managingOrganisationId.eq(team.getOrganisationId())
                            .and(QOrganisationSummary.organisationSummary.teamId.eq(team.getTeamId())));
                } else {
                    predicateList.add(QOrganisationSummary.organisationSummary.managingOrganisationId.eq(team.getOrganisationId())
                            .and(QOrganisationSummary.organisationSummary.teamId.isNull()));

                }
            }
            predicateBuilder.andAnyOf(predicateList.toArray(new Predicate[]{}));
        }

    }

    public Predicate getPredicate() {
        return predicateBuilder.getValue();
    }

}
