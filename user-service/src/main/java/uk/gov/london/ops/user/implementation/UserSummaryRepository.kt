package uk.gov.london.ops.user.implementation

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import uk.gov.london.ops.framework.jpa.ReadOnlyRepository
import uk.gov.london.ops.user.domain.UserSummary

interface UserSummaryRepository : ReadOnlyRepository<UserSummary?, Int?> {

    companion object {
        // "-1 in :someArray or .." is a workaround to skip 'in :someArray' when empty array is passed as a parameter
        //similar workaround with by appending 'or (o.id is null and 'Orphaned' in :registrationStatus and (:isOpsAdmin or u.username = :currentUser))'
        //to pass conditions that 'orphaned' users would fail when searching for them with an admin user / themselves
        const val USERS_QUERY = """
            from users u 
            left join user_roles r on u.username = r.username 
            left join organisation o on r.organisation_id = o.id 
            left join user_org_finance_threshold t on r.username = t.username and r.organisation_id = t.organisation_id 
            where ((u.username = :currentUser) or ((-1 not in :orgIds) and (o.id in :orgIds or o.managing_organisation_id in :orgIds or o.entity_type in :managingOrgTypes or (o.id is null and 'Orphaned' in :registrationStatus and :isOpsAdmin))))
            and u.enabled in :userEnabledStatus 
            and (upper(o.name) like upper(concat('%', :orgName, '%')) or o.id = :orgId or (o.id is null and 'Orphaned' in :registrationStatus and (:isOpsAdmin or u.username = :currentUser))) 
            and (-1 in :orgTypes or o.entity_type in :orgTypes) 
            and (upper(u.username) like upper(concat('%', :userNameOrEmail, '%')) or upper(concat(u.firstname, ' ', u.lastname)) like upper(concat('%', :userNameOrEmail, '%'))) 
            and ('-1' in :roles or r.name in :roles or ('NO_ROLES' in :roles and (r.approved is null or r.approved = false))) 
            and (case when t.pending_threshold is not null then 'pendingChanges' 
            when (r.name = 'ROLE_OPS_ADMIN' or r.name='ROLE_GLA_SPM' or r.name='ROLE_GLA_ORG_ADMIN') and t.pending_threshold is null and t.approved_threshold is null then 'notSet' 
            when t.pending_threshold is null and t.approved_threshold is not null then 'usersWithSpendAuthority' 
            else 'N/A' end) in :spendAuthority 
            and (r.approved = true and 'Approved' in :registrationStatus or (o.id is null and 'Orphaned' in :registrationStatus and (:isOpsAdmin or u.username = :currentUser)) or ((r.approved is null or r.approved = false) and ('Unapproved' in :registrationStatus or 'Pending' in :registrationStatus) and (-1 in :orgTypes)))
            and (o.id is not null or :orgName = '')
        """
    }

    @Query(value = "select distinct u.* $USERS_QUERY", countQuery = "select count(distinct u.username)  $USERS_QUERY", nativeQuery = true)
    fun findAll(@Param("currentUser") currentUser: String,
                @Param("isOpsAdmin") isOpsAdmin: Boolean,
                @Param("orgIds") orgIds: List<Int>,
                @Param("orgName") orgName: String,
                @Param("orgId") orgId: Int,
                @Param("orgTypes") orgTypes: List<Int>,
                @Param("managingOrgTypes") managingOrgTypes: List<Int>,
                @Param("userNameOrEmail") userNameOrEmail: String,
                @Param("userEnabledStatus") userEnabledStatus: List<Boolean?>,
                @Param("roles") roles: List<String>,
                @Param("spendAuthority") spendAuthority: List<String>,
                @Param("registrationStatus") registrationStatus: List<String>,
                pageable: Pageable): Page<UserSummary>
}
