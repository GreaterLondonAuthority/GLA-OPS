/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import uk.gov.london.ops.domain.user.User;

import java.util.Set;

public interface UserRepository extends JpaRepository<User, String> {



    @Query(value = "select * from users us " +
            "left outer join user_roles ur on us.username = ur.username and ur.primary_org_for_user = true " +
            "and primary_org_for_user = null", nativeQuery = true)
    Set<User> findAllUsersWithoutPrimaryRole();
}
