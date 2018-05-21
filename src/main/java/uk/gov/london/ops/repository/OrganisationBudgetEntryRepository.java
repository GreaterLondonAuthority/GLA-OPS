/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uk.gov.london.ops.domain.organisation.OrganisationBudgetEntry;
import uk.gov.london.ops.domain.project.GrantType;

import java.util.List;

import static uk.gov.london.ops.domain.organisation.OrganisationBudgetEntry.Type.Initial;

public interface OrganisationBudgetEntryRepository extends JpaRepository<OrganisationBudgetEntry, Integer> {

    List<OrganisationBudgetEntry> findAllByOrganisationIdAndProgrammeId(Integer organisationId, Integer programmeId);

    default OrganisationBudgetEntry findInitial(Integer organisationId, Integer programmeId, GrantType grantType, boolean strategic) {
        return findByOrganisationIdAndProgrammeIdAndTypeAndGrantTypeAndStrategic(organisationId, programmeId, Initial, grantType, strategic);
    }

    OrganisationBudgetEntry findByOrganisationIdAndProgrammeIdAndTypeAndGrantTypeAndStrategic(Integer organisationId,
                                                                                              Integer programmeId,
                                                                                              OrganisationBudgetEntry.Type type,
                                                                                              GrantType grantType,
                                                                                              boolean strategic);

    default List<OrganisationBudgetEntry> findAllLike(OrganisationBudgetEntry entry) {
        return findAllByOrganisationIdAndProgrammeIdAndGrantTypeAndStrategic(entry.getOrganisationId(), entry.getProgrammeId(),
                entry.getGrantType(), entry.isStrategic());
    }

    List<OrganisationBudgetEntry> findAllByOrganisationIdAndProgrammeIdAndGrantTypeAndStrategic(Integer organisationId,
                                                                                                Integer programmeId,
                                                                                                GrantType grantType,
                                                                                                boolean strategic);

}
