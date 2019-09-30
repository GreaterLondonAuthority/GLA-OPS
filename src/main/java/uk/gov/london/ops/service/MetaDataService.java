/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.london.ops.domain.Message;
import uk.gov.london.ops.domain.metadata.MetaDataSummary;
import uk.gov.london.ops.notification.NotificationService;

@Service
/**
 * Service for user specific data that could be temporal in nature
 */
public class MetaDataService {

    @Autowired
    private MessageService messageService;

    @Autowired
    private NotificationService notificationService;

    public MetaDataSummary getMetaDataSummary(String username) {

        MetaDataSummary summary = new MetaDataSummary();
        int count = notificationService.getUnreadNotificationCountForUser(username);
        summary.setNumberOfUnreadNotifications(count);
        Message message = messageService.find(Message.system_outage_message_key);
        if (message != null && message.isEnabled()) {
            summary.setSystemOutageMessage(message.getText());
        }
        return summary;
    }

}
