/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.notification.implementation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.london.ops.di.DataInitialiserModule;
import uk.gov.london.ops.notification.NotificationType;

import javax.transaction.Transactional;

@Transactional
@Component
public class NotificationTypesDataInitialiser implements DataInitialiserModule {

    @Autowired
    private NotificationTypeRepository notificationTypeRepository;

    @Override
    public String getName() {
        return "Notification types data initialiser";
    }

    @Override
    public boolean runInAllEnvironments() {
        return true;
    }

    @Override
    public void beforeInitialisation() {}

    @Override
    public void cleanupOldData() {}

    @Override
    public void addReferenceData() {
        for (NotificationType notificationType: NotificationType.values()) {
            if (!notificationTypeRepository.existsById(notificationType)) {
                notificationTypeRepository.save(new NotificationTypeEntity(notificationType));
            }
        }
    }

    @Override
    public void addUsers() {}

    @Override
    public void addOrganisations() {}

    @Override
    public void addTemplates() {}

    @Override
    public void addProgrammes() {}

    @Override
    public void addProjects() {}

    @Override
    public void addSupplementalData() {}

    @Override
    public void afterInitialisation() {}

}
