/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.notification.implementation

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.london.ops.notification.*
import uk.gov.london.ops.project.repeatingentity.ProjectObjective
import java.time.OffsetDateTime

interface EmailRepository : JpaRepository<Email, Int> {

    fun findAllByStatus(status: Email.Status, pageable: Pageable): List<Email>

    fun countAllByStatus(status: Email.Status): Int

}


interface NotificationRepository : JpaRepository<Notification, Int>


interface NotificationTypeRepository : JpaRepository<NotificationTypeEntity, NotificationType>


interface ScheduledNotificationRepository : JpaRepository<ScheduledNotification, Int> {

    fun findAllByOrderByScheduledDateTimeDesc(): List<ScheduledNotification>

    fun findAllByStatusAndScheduledDateTimeBefore(status: ScheduledNotificationStatus, now: OffsetDateTime): List<ScheduledNotification>

}


interface UserNotificationRepository : JpaRepository<UserNotification, Int> {

    fun findAllByUsername(username: String): List<UserNotification>

    fun findAllByUsernameAndStatus(username: String, status: UserNotification.Status, pageable: Pageable): Page<UserNotification>

    fun countAllByUsernameAndTimeReadIsNullAndStatus(username: String, status: UserNotification.Status): Int

}

interface ProjectObjectiveRepository : JpaRepository<ProjectObjective, Int>

