/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.organisation.implementation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uk.gov.london.ops.organisation.model.Organisation;
import uk.gov.london.ops.organisation.model.OrganisationGroup;

import java.util.List;
import java.util.Set;

public interface OrganisationGroupRepository extends JpaRepository<OrganisationGroup, Integer> {

    OrganisationGroup findFirstByName(String name);

    Set<OrganisationGroup> findAllByName(String name);

    List<OrganisationGroup> findAllByTypeAndProgrammeIdAndLeadOrganisationId(OrganisationGroup.Type type, Integer programmeId,
                                                                             Integer leadOrganisationId);

    List<OrganisationGroup> findAllByTypeAndProgrammeIdAndOrganisations(OrganisationGroup.Type type, Integer programmeId,
                                                                        Organisation organisation);

    List<OrganisationGroup> findAllByManagingOrganisation(Organisation managingOrganisationId);

}
