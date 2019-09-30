/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.notification.implementation;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import uk.gov.london.ops.notification.Notification;
import uk.gov.london.ops.notification.UserNotification;

import java.util.List;

public interface UserNotificationRepository extends JpaRepository<UserNotification, Integer> {

    List<UserNotification> findAllByUsername(String username);

    List<UserNotification> findAllByNotification(Notification notification);

    Page<UserNotification> findAllByUsernameAndStatus(String username, UserNotification.Status status, Pageable pageable);

    int countAllByUsernameAndTimeReadIsNullAndStatus(String username, UserNotification.Status status);
}
