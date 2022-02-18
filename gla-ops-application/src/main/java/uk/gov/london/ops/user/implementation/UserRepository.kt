package uk.gov.london.ops.user.implementation

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import uk.gov.london.ops.user.domain.UserEntity
import java.util.*

interface UserRepository : JpaRepository<UserEntity?, String?> {
    fun findByUserId(userId: Int?): UserEntity?

    @Query(value = "select distinct u.* from users u "
            + " join user_roles ur on u.username = ur.username "
            + " where ur.name in :roles",  nativeQuery = true)
    fun findByUserRoles(@Param("roles") roles: List<String>) : List<UserEntity>


    @Query(value = "select u.username from users u where u.enabled = true and u.password_expiry < ?1",  nativeQuery = true)
    fun findAllUsernamesExpiredBefore(cutoffDate: Date): Set<String>

    @Query(value = "select u.* from users u inner join user_roles ur on u.username = ur.username " +
            "where ur.organisation_id = ?1 and ur.name in ?2", nativeQuery = true)
    fun findByOrganisationIdAndUserRoles(organisationId: Int?, vararg roles: String?): Set<UserEntity?>?
}
