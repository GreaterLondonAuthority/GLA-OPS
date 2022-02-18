/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.role.implementation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uk.gov.london.ops.role.model.Role;

import java.util.List;
import java.util.Set;

@Repository
public interface RoleRepository extends JpaRepository<Role, Integer> {

    @Query("select count(distinct username) from user_roles where name = 'ROLE_OPS_ADMIN'")
    Integer getNumberOfAdmins();

    @Query("select username from user_roles where organisation_id = ?1 and authorised_signatory = true ")
    Set<String> getAuthorisedSignatories(Integer orgId);

    List<Role> findAllByName(String roleName);

    @Modifying
    @Query(value = "UPDATE user_roles SET name = ?2 WHERE organisation_id in "
        + "(select id from organisation where entity_type = ?3) AND name = ?1", nativeQuery = true)
    int updateRoles(@Param("fromRole") String fromRole,
                    @Param("toRole") String toRole,
                    @Param("entityType") int entityType);
}
