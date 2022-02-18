/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.notification;

import uk.gov.london.ops.organisation.Organisation;
import uk.gov.london.ops.user.User;

import java.time.OffsetDateTime;
import java.util.Map;

public interface EmailService {

    void sendEmail(NotificationType notificationType, NotificationTargetEntity targetEntity, User user, Organisation organisation);

    void sendRegistrationRequestEmail(NotificationType notificationType, User requester, User recipient, Organisation organisation);

    void sendPaymentAcknowledgementEmail(Map<String, Object> model);

    void sendEmailNotification(NotificationType notificationType, Map<String, Object> model);

    void sendPaymentSchedulerSummaryEmail(NotificationType notificationType, User user, Map<String, Object> model);

    Integer sendPasswordResetEmail(User user, Integer id, String token);

    void sendUserStatusUpdatedEmail(User user);

    void sendOrgAdminRequestedEmail(User toUser, User aboutUser, Organisation organisation);

    Integer sendDisabledUserPasswordResetAttemptEmail(User user);

    void sendAuthorisedPaymentsReportEmail(String financeGroupEmail, OffsetDateTime day, String csvReport);

    void sendNoAuthorisedPaymentsReportEmail(String financeGroupEmail, OffsetDateTime day);

    void sendBroadcastEmail(String recipient, String recipientName, String subHeading,
                            String body, String signOff, String subject);
}
