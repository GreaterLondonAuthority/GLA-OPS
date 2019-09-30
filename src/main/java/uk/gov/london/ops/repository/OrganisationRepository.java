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
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import uk.gov.london.ops.domain.organisation.Organisation;
import uk.gov.london.ops.domain.organisation.OrganisationStatus;
import uk.gov.london.ops.domain.organisation.RegistrationStatus;
import uk.gov.london.ops.domain.organisation.Team;

import java.util.List;
import java.util.Set;

public interface OrganisationRepository extends JpaRepository<Organisation, Integer> {

    Organisation findFirstByImsNumber(String imsNumber);

    Set<Organisation> findAllByImsNumber(String imsNumber);

    Organisation findFirstByNameIgnoreCase(String name);

    @Query(value = "select * from organisation where upper(name) = upper(?1) and managing_organisation_id = ?2", nativeQuery = true)
    List<Organisation> findByNameIgnoreCaseAndManagingOrganisation(String name, Integer managingOrganisationId);

    Page findAll(Pageable pageable);

    Page findByUserRegStatus(RegistrationStatus registrationStatus, Pageable pageable);

    List<Organisation> findAllByEntityType(Integer entityType);

    Organisation findFirstByTeam(Team team);

    long countByRegistrationKey(String registrationKey);

    Integer countByUkprnAndStatusNotIn(Integer ukprn, OrganisationStatus[] organisationStatuses);

    Organisation findFirstByRegistrationKeyIgnoreCase(String registrationKey);

    List<Organisation> findAllByRegistrationKeyNull();

    @Modifying
    @Query(value = "update organisation set contact = NULL where contact = ?1 and managing_organisation_id = ?2", nativeQuery = true)
    int clearUserContactForOrganisationsManagedBy(String username, Integer managingOrganisationId);

}
