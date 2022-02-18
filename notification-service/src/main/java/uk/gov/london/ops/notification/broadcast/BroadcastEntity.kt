package uk.gov.london.ops.notification.broadcast

import uk.gov.london.ops.notification.BroadcastType
import java.time.OffsetDateTime
import javax.persistence.*

@Entity(name = "broadcast")
class BroadcastEntity(

        @Id
        @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "broadcast_seq_gen")
        @SequenceGenerator(name = "broadcast_seq_gen", sequenceName = "broadcast_seq", initialValue = 100, allocationSize = 1)
        var id: Int? = null,

        @Column(name = "managing_organisation_id")
        var managingOrganisationId: Int? = null,

        @Column(name = "created_by")
        var createdBy: String? = null,

        @Column(name = "created_on")
        var createdOn: OffsetDateTime? = null,

        @Column(name = "modified_by")
        var modifiedBy: String? = null,

        @Column(name = "modified_on")
        var modifiedOn: OffsetDateTime? = null,

        @Enumerated(EnumType.STRING)
        @Column(name = "status")
        var status: BroadcastStatus? = BroadcastStatus.PendingApproval,

        @Column(name = "main_project_contacts")
        var mainProjectContacts: Boolean? = null,

        @Column(name = "secondary_project_contacts")
        var secondaryProjectContacts: Boolean? = null,

        @Column(name = "organisation_admins")
        var organisationAdmins: Boolean? = null,

        @Column(name = "programme_id")
        var programmeId: Int? = null,

        @Column(name = "template_ids")
        var templateIds: String? = null,

        @Column(name = "project_status")
        var projectStatus: String? = null,

        @Column(name = "subject")
        var subject: String? = null,

        @Column(name = "body")
        var body: String? = null,

        @Column(name = "sign_off")
        var signOff: String? = null,

        @Column(name = "email_sent")
        var emailSent: Boolean = false,

        @Transient
        var broadcastType: BroadcastType = BroadcastType.Project

)
