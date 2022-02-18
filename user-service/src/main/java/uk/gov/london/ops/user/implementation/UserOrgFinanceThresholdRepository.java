/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.user.implementation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.gov.london.ops.user.domain.UserOrgFinanceThreshold;
import uk.gov.london.ops.user.domain.UserOrgKey;

import java.util.Set;

@Repository
public interface UserOrgFinanceThresholdRepository extends JpaRepository<UserOrgFinanceThreshold, UserOrgKey> {

    Set<UserOrgFinanceThreshold> findByIdUsername(String username);

    Set<UserOrgFinanceThreshold> findByIdOrganisationId(Integer orgId);

}
