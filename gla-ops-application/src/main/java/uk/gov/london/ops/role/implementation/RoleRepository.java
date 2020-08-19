/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.role.implementation;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uk.gov.london.ops.role.model.Role;

public interface RoleRepository extends JpaRepository<Role, Integer> {

    @Query("select count(distinct username) from user_roles where name = 'ROLE_OPS_ADMIN'")
    Integer getNumberOfAdmins();

    List<Role> findAllByName(String roleName);

    @Modifying
    @Query("UPDATE user_roles SET name = :toRole   WHERE organisation_id in "
        + "(select id from Organisation where entity_type = :entityType) AND name = :fromRole")
    int updateRoles(@Param("fromRole") String fromRole,
                    @Param("toRole") String toRole,
                    @Param("entityType") int entityType);
}
