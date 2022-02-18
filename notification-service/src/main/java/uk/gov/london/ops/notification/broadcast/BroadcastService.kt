package uk.gov.london.ops.notification.broadcast

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.london.ops.audit.AuditService
import uk.gov.london.ops.framework.environment.Environment
import uk.gov.london.ops.framework.exception.ValidationException
import uk.gov.london.ops.organisation.OrganisationService
import uk.gov.london.ops.permission.PermissionService
import uk.gov.london.ops.permission.PermissionType
import uk.gov.london.ops.user.User
import uk.gov.london.ops.user.UserService
import uk.gov.london.ops.user.UserUtils.currentUser
import javax.transaction.Transactional

@Transactional
@Service
class BroadcastService @Autowired constructor(
    val broadcastRepository: BroadcastRepository,
    val environment: Environment,
    val organisationService: OrganisationService,
    val permissionService: PermissionService,
    val userService: UserService,
    val auditService: AuditService
) {

    fun getBroadcasts(): List<BroadcastSummary> {
        val currentUser = currentUser()
        return broadcastRepository.findAll()
            .filter { currentUser.managingOrganisationsIds.contains(it.managingOrganisationId) }
            .map { toSummary(it) }
    }

    private fun toSummary(entity: BroadcastEntity): BroadcastSummary {
        val createdByName = userService.getUserFullName(entity.createdBy)
        val managingOrganisationName = organisationService.getOrganisationName(entity.managingOrganisationId)
        return BroadcastSummary(
            id = entity.id,
            managingOrganisationName = managingOrganisationName,
            createdByName = createdByName,
            modifiedOn = entity.modifiedOn,
            status = entity.status?.displayText,
            subject = entity.subject
        )
    }

    fun getBroadcast(id: Int): Broadcast {
        return toModel(broadcastRepository.getOne(id))
    }

    fun deleteBroadcast(id: Int) {
        val broadcast = broadcastRepository.getOne(id)
        val currentUser = currentUser()

        if (!canDeleteThisBroadcast(currentUser, broadcast)) {
            throw ValidationException(
                "Unable to delete this message as user is not an Ops Admin, or a member of the same managing org"
            )
        }
        broadcastRepository.delete(broadcast)
        auditService.auditCurrentUserActivity("Deleted Broadcast ID $id")
    }

    fun approveBroadcast(id: Int) {
        val broadcast = broadcastRepository.getOne(id)
        val currentUser = currentUser()

        if (!canApproveThisBroadcast(currentUser, broadcast)) {
            throw ValidationException(
                "Unable to approve this message as user does not have relevant permissions."
            )
        }
        broadcast.status = BroadcastStatus.Approved
        save(broadcast)
        auditService.auditCurrentUserActivity("Approved Broadcast ID $id")
    }

    private fun toModel(entity: BroadcastEntity): Broadcast {
        val managingOrganisationName = organisationService.getOrganisationName(entity.managingOrganisationId)
        val createdByName = userService.getUserFullName(entity.createdBy)
        val modifiedByName = userService.getUserFullName(entity.modifiedBy)
        val templateIds = entity.templateIds?.split(",")?.map { it.toInt() }
        val currentUser = currentUser()
        var approverPrimaryOrg : String? = null
        if (entity.status == BroadcastStatus.Approved) {
            val id = userService.get(entity.modifiedBy).primaryOrganisationId
            approverPrimaryOrg = organisationService.getOrganisationName(id)
        }
        return Broadcast(
            id = entity.id,
            managingOrganisationId = entity.managingOrganisationId,
            managingOrganisationName = managingOrganisationName,
            createdByName = createdByName,
            createdOn = entity.createdOn,
            modifiedByName = modifiedByName,
            modifiedOn = entity.modifiedOn,
            status = entity.status?.displayText,
            mainProjectContacts = entity.mainProjectContacts,
            secondaryProjectContacts = entity.secondaryProjectContacts,
            organisationAdmins = entity.organisationAdmins,
            programmeId = entity.programmeId,
            templateIds = templateIds,
            projectStatus = entity.projectStatus,
            subject = entity.subject,
            body = entity.body,
            signOff = entity.signOff,
            approverPrimaryOrg = approverPrimaryOrg,
            canDelete = canDeleteThisBroadcast(currentUser, entity),
            canApprove = canApproveThisBroadcast(currentUser, entity)
        )
    }

    private fun canDeleteThisBroadcast(currentUser: User, entity: BroadcastEntity) =
        currentUser.isOpsAdmin || permissionService.currentUserHasPermissionForOrganisation(
            PermissionType.BROADCAST_DELETE, entity.managingOrganisationId)

    private fun canApproveThisBroadcast(currentUser: User, entity: BroadcastEntity) =
        currentUser.username != entity.createdBy
                && permissionService.currentUserHasPermission(PermissionType.BROADCAST_APPROVE)


    fun createBroadcast(broadcast: Broadcast) {
        val broadcastEntity = toEntity(broadcast)
        save(broadcastEntity)
        auditService.auditCurrentUserActivity("Requested approval for Broadcast ID "+ broadcastEntity.id)
    }

    private fun save(broadcastEntity: BroadcastEntity) {
        if (broadcastEntity.id == null) {
            broadcastEntity.createdBy = currentUser().username
            broadcastEntity.createdOn = environment.now()
        }
        broadcastEntity.modifiedBy = currentUser().username
        broadcastEntity.modifiedOn = environment.now()
        broadcastRepository.save(broadcastEntity)
    }

    fun getBroadcastsToSend() : Set<BroadcastEntity> {
        return broadcastRepository.findAllByEmailSentAndStatus(false, BroadcastStatus.Approved)
    }

    fun markAsProcessed(id: Int) {
        val broadcast = broadcastRepository.getOne(id)
        broadcast.emailSent = true
        broadcastRepository.save(broadcast)
    }

    private fun toEntity(model: Broadcast): BroadcastEntity {
        val templateIds = model.templateIds?.joinToString(",")
        return BroadcastEntity(
            managingOrganisationId = model.managingOrganisationId,
            mainProjectContacts = model.mainProjectContacts,
            secondaryProjectContacts = model.secondaryProjectContacts,
            organisationAdmins = model.organisationAdmins,
            programmeId = model.programmeId,
            templateIds = templateIds,
            projectStatus = model.projectStatus,
            subject = model.subject,
            body = model.body,
            signOff = model.signOff
        )
    }

}
