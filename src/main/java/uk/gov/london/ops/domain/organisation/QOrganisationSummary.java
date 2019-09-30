/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.domain.organisation;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.EntityPathBase;
import com.querydsl.core.types.dsl.EnumPath;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.StringPath;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.ArrayList;
import java.util.List;

import static com.querydsl.core.types.PathMetadataFactory.forVariable;

// This HAS to be called QOrganisationSummary otherwise the JPA repository will not instantiate
public class QOrganisationSummary extends EntityPathBase<OrganisationSummary> {

    // This HAS to exist, otherwise the JPA repository will not instantiate
    public static final QOrganisationSummary organisationSummary = new QOrganisationSummary();

    public final NumberPath<Integer> id = createNumber("id", Integer.class);

    public final StringPath name = createString("name");

    public final NumberPath<Integer> teamId = createNumber("teamId", Integer.class);

    public final NumberPath<Integer> entityType = createNumber("entityType", Integer.class);

    public final NumberPath<Integer> managingOrganisationId = createNumber("managingOrganisationId", Integer.class);

    public final EnumPath<OrganisationStatus> orgStatus = createEnum("status", OrganisationStatus.class);

    public final EnumPath<RegistrationStatus> userRegStatus = createEnum("userRegStatus", RegistrationStatus.class);

    private BooleanBuilder predicateBuilder = new BooleanBuilder();

    public QOrganisationSummary() {
        super(OrganisationSummary.class, forVariable("organisationSummary"));
    }

    public void build(List<Integer> organisations, String searchText, List<Integer> entityTypes, List<OrganisationStatus> orgStatuses, List<RegistrationStatus> userRegStatuses, List<OrganisationTeam> teams) {
        Predicate[] predicates = new Predicate[] {
                this.id.in(organisations),
                this.managingOrganisationId.in(organisations)
        };

        predicateBuilder.andAnyOf(predicates);

        if (StringUtils.isNotEmpty(searchText)) {
            List<Predicate> searchTextPredicates = new ArrayList<>();

            if (NumberUtils.isNumber(searchText)) {
                searchTextPredicates.add(this.id.eq(Integer.parseInt(searchText)));
            }

            searchTextPredicates.add(this.name.containsIgnoreCase(searchText));

            predicateBuilder.andAnyOf(searchTextPredicates.toArray(new Predicate[searchTextPredicates.size()]));
        }

        if (CollectionUtils.isNotEmpty(entityTypes)) {
            predicateBuilder.and(this.entityType.in(entityTypes));
        }

        if (CollectionUtils.isNotEmpty(orgStatuses)) {
            predicateBuilder.and(this.orgStatus.in(orgStatuses));
        }

        if (CollectionUtils.isNotEmpty(userRegStatuses)) {
            predicateBuilder.and(this.userRegStatus.in(userRegStatuses));
        }

        if (CollectionUtils.isNotEmpty(teams)) {
            List<Predicate> predicateList = new ArrayList<>();
            for (OrganisationTeam team : teams) {
                if (team.getTeamId() != null) {
                    predicateList.add(this.managingOrganisationId.eq(team.getOrganisationId()).and(this.teamId.eq(team.getTeamId())));
                } else {
                    predicateList.add(this.managingOrganisationId.eq(team.getOrganisationId()).and(this.teamId.isNull()));

                }
            }
            predicateBuilder.andAnyOf(predicateList.toArray(new Predicate[]{}));
        }

    }

    public Predicate getPredicate() {
        return predicateBuilder.getValue();
    }

}
