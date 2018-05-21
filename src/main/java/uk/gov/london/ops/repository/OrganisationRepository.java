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
import uk.gov.london.ops.domain.organisation.Organisation;
import uk.gov.london.ops.domain.organisation.RegistrationStatus;

import java.util.List;
import java.util.Set;

public interface OrganisationRepository extends JpaRepository<Organisation, Integer> {

    Organisation findFirstByImsNumber(String imsNumber);

    Set<Organisation> findAllByImsNumber(String imsNumber);

    Organisation findFirstByNameIgnoreCase(String name);

    Page findAll(Pageable pageable);

    Page findByUserRegStatus(RegistrationStatus registrationStatus, Pageable pageable);

    List<Organisation> findAllByEntityType(Integer entityType);

}
