/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.organisation.implementation.repository;

import com.querydsl.core.BooleanBuilder;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import uk.gov.london.ops.framework.jpa.ReadOnlyRepository;
import uk.gov.london.ops.organisation.model.OrganisationStatus;
import uk.gov.london.ops.organisation.model.QTeam;
import uk.gov.london.ops.organisation.model.Team;

import java.util.List;
import java.util.Set;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;

public interface TeamRepository extends ReadOnlyRepository<Team, Integer>, QuerydslPredicateExecutor<Team> {

    Set<Team> findByOrganisationId(Integer organisationId);

    default Page<Team> findAll(List<Integer> currentUserOrgIds, String searchText, List<Integer> managingOrgIds,
                               List<OrganisationStatus> orgStatuses, Pageable pageable) {
        BooleanBuilder predicateBuilder = new BooleanBuilder(QTeam.team.organisationId.in(currentUserOrgIds));
        if (isNotEmpty(searchText)) {
            predicateBuilder.and(QTeam.team.name.containsIgnoreCase(searchText));
        }
        if (CollectionUtils.isNotEmpty(managingOrgIds)) {
            predicateBuilder.and(QTeam.team.organisationId.in(managingOrgIds));
        }
        if (CollectionUtils.isNotEmpty(orgStatuses)) {
            predicateBuilder.and(QTeam.team.status.in(orgStatuses));
        }
        return findAll(predicateBuilder.getValue(), pageable);
    }

}
