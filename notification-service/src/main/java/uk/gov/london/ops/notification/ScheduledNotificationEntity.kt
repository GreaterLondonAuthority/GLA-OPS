/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.notification

import uk.gov.london.common.GlaUtils.csStringToList
import uk.gov.london.common.GlaUtils.listToCsString
import java.time.OffsetDateTime
import javax.persistence.*

enum class ScheduledNotificationStatus {
    Scheduled,
    Sent
}

@Entity(name = "scheduled_notification")
class ScheduledNotificationEntity (

        @Id
        @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "scheduled_notification_seq_gen")
        @SequenceGenerator(name = "scheduled_notification_seq_gen", sequenceName = "scheduled_notification_seq", initialValue = 100, allocationSize = 1)
        var id: Int? = null,

        var text: String,

        var scheduledDateTime: OffsetDateTime,

        @Enumerated(EnumType.STRING)
        var status: ScheduledNotificationStatus = ScheduledNotificationStatus.Scheduled,

        @Column(name = "target_roles")
        var targetRolesString: String? = null,

        @Column(name = "target_org_ids")
        var targetOrgIdsString: String? = null

) {

    constructor(text: String, scheduledDateTime: OffsetDateTime, status: ScheduledNotificationStatus, targetRoles: List<String>? = null, targetOrgIds: List<Int>? = null) : this(
            null,
            text,
            scheduledDateTime,
            status,
            listToCsString(targetRoles),
            listToCsString(targetOrgIds?.map { it.toString() })
    )

    fun getTargetRoles() : List<String> {
        return csStringToList(targetRolesString)
    }

    fun setTargetRoles(roles: List<String> ?) {
        this.targetRolesString = listToCsString(roles)
    }

    fun getTargetOrgIds() : List<Int> {
        return csStringToList(targetOrgIdsString).map { it.toInt() }
    }

}
