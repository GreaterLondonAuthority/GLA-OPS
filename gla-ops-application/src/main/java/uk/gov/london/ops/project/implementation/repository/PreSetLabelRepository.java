/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.project.implementation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.gov.london.ops.organisation.model.OrganisationEntity;
import uk.gov.london.ops.project.label.PreSetLabelEntity;

import java.util.List;

/**
 * Spring JPA Data Repository for Pre-set Labels information.
 *
 * @author Carmina Matias
 */
@Repository
public interface PreSetLabelRepository extends JpaRepository<PreSetLabelEntity, Integer> {

    PreSetLabelEntity findByLabelNameAndManagingOrganisation(String labelName, OrganisationEntity managingOrganisation);

    List<PreSetLabelEntity> findAllByManagingOrganisation(OrganisationEntity organisation);
}