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
import uk.gov.london.ops.organisation.OrganisationStatus;
import uk.gov.london.ops.organisation.model.QTeamEntity;
import uk.gov.london.ops.organisation.model.TeamEntity;

import java.util.List;
import java.util.Set;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;

public interface TeamRepository extends ReadOnlyRepository<TeamEntity, Integer>, QuerydslPredicateExecutor<TeamEntity> {

    Set<TeamEntity> findByOrganisationId(Integer organisationId);

    default Page<TeamEntity> findAll(List<Integer> currentUserOrgIds, String searchText, List<Integer> managingOrgIds,
                                     List<OrganisationStatus> orgStatuses, Pageable pageable) {
        BooleanBuilder predicateBuilder = new BooleanBuilder(QTeamEntity.teamEntity.organisationId.in(currentUserOrgIds));
        if (isNotEmpty(searchText)) {
            predicateBuilder.and(QTeamEntity.teamEntity.name.containsIgnoreCase(searchText));
        }
        if (CollectionUtils.isNotEmpty(managingOrgIds)) {
            predicateBuilder.and(QTeamEntity.teamEntity.organisationId.in(managingOrgIds));
        }
        if (CollectionUtils.isNotEmpty(orgStatuses)) {
            predicateBuilder.and(QTeamEntity.teamEntity.status.in(orgStatuses));
        }
        return findAll(predicateBuilder.getValue(), pageable);
    }

}
