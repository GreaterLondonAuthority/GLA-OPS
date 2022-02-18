/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.notification.broadcast

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import uk.gov.london.ops.notification.*
import javax.transaction.Transactional

@Transactional
@Service
class BroadcastScheduledService @Autowired constructor(
    val broadcastService: BroadcastService,
    val emailService: EmailService,
    val services: Set<BroadcastTargetService>
) {

    var log = LoggerFactory.getLogger(javaClass)


    @Scheduled(fixedDelay = 1000 * 60 * 5)
    @Transactional
    fun processApprovedBroadcasts() {
        val entries = broadcastService.getBroadcastsToSend();

        for (entry in entries) {
            when (entry.broadcastType ) {
                BroadcastType.Project -> handleProjectBroadcast(entry)
                else -> log.error("No matching broadcast handler for type: " + entry.broadcastType)

            }
        }
    }

    fun handleProjectBroadcast(entry: BroadcastEntity) {
        val details = ProjectBroadcastDetails(
            entry.id,
            entry.programmeId,
            getTemplateIds(entry.templateIds!!),
            entry.projectStatus,
            entry.mainProjectContacts,
            entry.secondaryProjectContacts,
            entry.organisationAdmins,
        )

        sendBroadcastEmails(entry, details)
    }

    fun sendBroadcastEmails(entry: BroadcastEntity, details: BroadcastDetail) {
        services.stream()
            .filter { f -> f.canHandleType(entry.broadcastType) }
            .forEach { s ->
                s.getEmailDetails(details).forEach {
                    emailService.sendBroadcastEmail(
                        it.recipientEmail, it.recipientName, it.subheading,
                        entry.body, entry.signOff, entry.subject
                    )
                }
            }
        broadcastService.markAsProcessed(entry.id!!)
    }

    fun getTemplateIds(templateIds: String): Set<Int>? {
        val ids = templateIds.split(",")
        val templateIdList = mutableSetOf<Int>()
        for (id in ids) {
            templateIdList.add(id.trim().toInt())
        }
        return templateIdList
    }
}
