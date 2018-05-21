/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uk.gov.london.ops.domain.user.UserOrgFinanceThreshold;
import uk.gov.london.ops.domain.user.UserOrgKey;

import java.util.Set;

public interface UserOrgFinanceThresholdRepository extends JpaRepository<UserOrgFinanceThreshold, UserOrgKey> {

    Set<UserOrgFinanceThreshold> findByIdUsername(String username);

    void deleteAllByIdUsername(String username);

}
