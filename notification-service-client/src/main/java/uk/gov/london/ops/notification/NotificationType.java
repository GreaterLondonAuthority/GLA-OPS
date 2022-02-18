/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.notification;

import org.apache.commons.lang3.StringUtils;
import uk.gov.london.ops.framework.EntityType;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import static uk.gov.london.common.user.BaseRole.*;
import static uk.gov.london.ops.framework.EntityType.*;
import static uk.gov.london.ops.notification.NotificationSubType.Action;
import static uk.gov.london.ops.notification.NotificationSubType.Info;
import static uk.gov.london.ops.notification.NotificationTargetUsersType.*;

public enum NotificationType {

    Adhoc,
    OrganisationRegistration,
    OrganisationApproval,
    OrganisationRejection,
    OrganisationInactivation,
    OrganisationReapproval,
    OrganisationContractOffer,
    OrganisationContractVariationOffer,
    UserRequestAccess,
    UserAccessApproval,
    UserTeamAccessApproval,
    UserAccessRejection,
    ProjectSubmissionReminder,
    OrganisationRemoval,
    AnnualSubmissionAwaitingApproval,
    AnnualSubmissionApproval,
    PendingSpendAuthorityThresholdApproval,
    ProjectTransfer,
    ProjectReturn,
    ProjectSubmitted,
    ProjectApprovalRequested,
    ApprovedForNextStage,
    PaymentAwaitingAuthorisation,
    PaymentAuthorisation,
    PaymentSchedulerSummary

}
