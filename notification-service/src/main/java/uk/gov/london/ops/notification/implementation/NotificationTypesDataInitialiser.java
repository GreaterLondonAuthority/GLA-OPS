/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.notification.implementation;

import org.springframework.stereotype.Component;
import uk.gov.london.ops.framework.di.DataInitialiserModule;
import uk.gov.london.ops.notification.NotificationType;
import uk.gov.london.ops.notification.NotificationTypeEntity;
import uk.gov.london.ops.notification.NotificationTypeLegacy;
import uk.gov.london.ops.notification.implementation.repository.NotificationTypeRepository;

import javax.transaction.Transactional;

@Transactional
@Component
public class NotificationTypesDataInitialiser implements DataInitialiserModule {

    private final NotificationTypeRepository notificationTypeRepository;

    public NotificationTypesDataInitialiser(NotificationTypeRepository notificationTypeRepository) {
        this.notificationTypeRepository = notificationTypeRepository;
    }

    @Override
    public String getName() {
        return "Notification types data initialiser";
    }

    @Override
    public boolean runInAllEnvironments() {
        return true;
    }

    @Override
    public void addReferenceData() {
        for (NotificationTypeLegacy notificationType: NotificationTypeLegacy.values()) {
            notificationTypeRepository.save(new NotificationTypeEntity(notificationType));
        }
    }

}
