/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.notification.implementation.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import uk.gov.london.ops.notification.*
import java.time.OffsetDateTime

@Repository
interface EmailRepository : JpaRepository<EmailEntity, Int> {

    fun findAllByStatus(status: EmailStatus, pageable: Pageable): List<EmailEntity>

    fun countAllByStatus(status: EmailStatus): Int

}

@Repository
interface NotificationRepository : JpaRepository<NotificationEntity, Int>

@Repository
interface ScheduledNotificationRepository : JpaRepository<ScheduledNotificationEntity, Int> {

    fun findAllByOrderByScheduledDateTimeDesc(): List<ScheduledNotificationEntity>

    fun findAllByStatusAndScheduledDateTimeBefore(status: ScheduledNotificationStatus, now: OffsetDateTime): List<ScheduledNotificationEntity>

}

@Repository
interface UserNotificationRepository : JpaRepository<UserNotificationEntity, Int> {

    fun findAllByUsername(username: String): List<UserNotificationEntity>

    fun findAllByUsernameAndStatus(username: String, status: UserNotificationStatus, pageable: Pageable): Page<UserNotificationEntity>

    fun countAllByUsernameAndTimeReadIsNullAndStatus(username: String, status: UserNotificationStatus): Int

}
