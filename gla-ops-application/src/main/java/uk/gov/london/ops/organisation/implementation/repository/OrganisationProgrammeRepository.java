/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.organisation.implementation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uk.gov.london.ops.domain.ProgrammeOrganisationID;
import uk.gov.london.ops.organisation.model.OrganisationProgramme;

/**
 * Spring JPA Data Repository for Programme information.
 *
 * @author Steve Leach
 */
public interface OrganisationProgrammeRepository extends JpaRepository<OrganisationProgramme, ProgrammeOrganisationID> {

}
