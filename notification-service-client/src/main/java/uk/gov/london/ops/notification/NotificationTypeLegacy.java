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

public enum NotificationTypeLegacy {

    Adhoc(
            NotificationType.Adhoc,
            "Manually triggered by an OPS admin",
            null,
            null,
            null,
            null,
            null,
            Collections.emptyList()
    ),

    OrganisationRegistration(
            NotificationType.OrganisationRegistration,
            "An external user registers an organisation",
            "{{name}} has registered against your managing organisation and requires approval",
            organisation,
            Action,
            Roles,
            "GLA Open Project System pending organisation registration",
            "organisation.registration.template.html",
            GLA_ORG_ADMIN),

    OrganisationApproval(
            NotificationType.OrganisationApproval,
            "A GLA user approves an organisation",
            "Your {{name}} registration for {{managingOrganisationName}} has been approved and you are now the admin. "
                    + "Give users this code to register their account with this organisation: {{id}}. "
                    + "You must approve their request on the all users page.",
            organisation,
            Info,
            Roles,
            "GLA Open Project System organisation approved",
            "organisation.approval.template.html",
            ORG_ADMIN
    ),

    OrganisationRejection(
            NotificationType.OrganisationRejection,
            "A GLA user rejects an organisation",
            "{{name}} registration has been rejected for the following reason: {{changeStatusReasonDetails}}.",
            organisation,
            Info,
            Roles,
            "GLA Open Project System organisation registration rejected",
            "organisation.rejection.template.html",
            ORG_ADMIN),

    OrganisationInactivation(
            NotificationType.OrganisationInactivation,
            "A GLA user marks an organisation as inactive",
            "{{name}} has been set to inactive for the following reason: {{changeStatusReasonDetails}}.",
            organisation,
            Info,
            "GLA Open Project System organisation inactivated",
            "organisation.inactivation.template.html",
            externalRoles()
    ),

    OrganisationReapproval(
            NotificationType.OrganisationReapproval,
            "A GLA user re-approves an organisation",
            "Your user role for {{name}} has been re-instated as {{name}} has been re-approved",
            organisation,
            Info,
            "GLA Open Project System organisation re-approved",
            "organisation.reapproved.template.html",
            externalRoles()
    ),
    OrganisationContractOffer(
            NotificationType.OrganisationContractOffer,
            "A GLA user offers a new organisation contact for signing",
            "",
            organisationSignatory,
            Info,
            Signatory,
            "Your organisation has received an offer from {{organisation.managingOrganisation.name}}",
            "organisation.contract.offer.template.html"
    ),

    OrganisationContractVariationOffer(
            NotificationType.OrganisationContractVariationOffer,
            "A GLA user offers a new organisation contact for signing",
            "",
            organisationSignatory,
            Info,
            Signatory,
            "Variation",
            "organisation.contract.variation.offer.template.html"
    ),

    UserRequestAccess(
            NotificationType.UserRequestAccess,
            "An user requests access to an organisation.",
            "A new registration against {{organisation.name}} is pending for {{fullName}}",
            user,
            Info,
            "GLA Open Project System pending registration request",
            "registration.request.template.html",
            Arrays.asList("ROLE_ORG_ADMIN", "ROLE_GLA_ORG_ADMIN")
    ),

    UserAccessApproval(
            NotificationType.UserAccessApproval,
            "An user role with an organisation is approved.",
            "Your {{organisation.name}} registration for {{organisation.managingOrganisation.name}} has been approved.",
            user,
            Info,
            "GLA Open Project System registration approved",
            "registration.approval.template.html",
            Arrays.asList("ROLE_USER")
    ),

    UserTeamAccessApproval(
            NotificationType.UserTeamAccessApproval,
            "An user is added to a team.",
            "You have been granted access to {{organisation.name}}, {{organisation.id}}, "
                    + "to manage projects in this Team under {{organisation.managingOrganisation.name}}.",
            user,
            Info,
            "GLA Open Project System team registration approved",
            "team.approval.template.html",
            Arrays.asList("ROLE_USER")
    ),

    UserAccessRejection(
            NotificationType.UserAccessRejection,
            "An user role with an organisation is rejected.",
            "Your request to access {{organisation.name}} on OPS has been rejected. Your login details are still valid,"
                    + " you can login and request access to an alternative organisation.",
            user,
            Info,
            "GLA Open Project System user registration rejected",
            "registration.rejection.template.html",
            Arrays.asList("ROLE_USER")
    ),

    ProjectSubmissionReminder(
            NotificationType.ProjectSubmissionReminder,
            "Project changes not submitted for more than 24 hrs.",
            "Submit your Project for approval",
            user,
            Info,
            "Submit your Project for approval",
            "project.submit.reminder.template.html",
            availableRoles()
    ),

    OrganisationRemoval(
            NotificationType.OrganisationRemoval,
            "User role has been removed.",
            "Your access to {{organisation.name}} on OPS has been removed. Your login details are still valid, "
                    + "you can request access to an alternative organisation.",
            user,
            Info,
            null,
            null,
            availableRoles()
    ),

    AnnualSubmissionAwaitingApproval(
            NotificationType.AnnualSubmissionAwaitingApproval,
            "An annual submission has been submitted",
            "An annual recoverable grant submission for {{financialyear}} has been made by {{organisation.name}}{{rollover}}. "
                    + "This submission is awaiting approval.",
            annualSubmission,
            Info,
            null,
            null,
            glaRoles()
    ),

    AnnualSubmissionApproval(
            NotificationType.AnnualSubmissionApproval,
            "An annual submission has been approved.",
            "The {{financialyear}} recoverable grant submission for {{organisation.name}} has been approved.",
            annualSubmission,
            Info,
            null,
            null,
            externalRoles()
    ),

    PendingSpendAuthorityThresholdApproval(
            NotificationType.PendingSpendAuthorityThresholdApproval,
            "A spend authority threshold has been updated and approved.",
            "Your spend authority threshold has been set at £{{approvedThreshold}} for {{organisation.name}}. "
                    + "You can now authorise payments up to this amount.",
            user,
            Action,
            null,
            null,
            glaRoles()
    ),

    ProjectTransfer(
            NotificationType.ProjectTransfer,
            "A project has been transferred to another organisation",
            "Project {{id}} has been transferred from {{fromOrganisation.name}} to {{toOrganisation.name}}. "
                    + "{{fromOrganisation.name}} no longer has access to this project. If you have any issues, "
                    + "email the OPS team at ops.uk.",
            project,
            Info,
            null,
            null,
            availableRoles()
    ),

    ProjectReturn(
            NotificationType.ProjectReturn,
            "A project has been returned",
            "Project P{{id}} for {{organisation.name}} has been returned and may require updates",
            project,
            Info,
            null,
            null,
            availableRoles()
    ),

    ProjectSubmitted(
            NotificationType.ProjectSubmitted,
            "A project has been submitted",
            "Project P{{id}} for {{organisation.name}} has been submitted",
            project,
            Action,
            Assignees,
            "Project submitted",
            "project.submitted.template.html"
    ),

    ProjectApprovalRequested(
            NotificationType.ProjectApprovalRequested,
            "A project has been updated and requires approval",
            "Project P{{id}} for {{organisation.name}} has updates requiring approval",
            project,
            Action,
            Assignees,
            "Project requires approval",
            "project.approval.requested.template.html"
    ),

    ApprovedForNextStage(
            NotificationType.ApprovedForNextStage,
            "Notification for approval resulting in a Stage change e.g. 1-2, 2-3.",
            "P{{id}} for {{organisation.name}} has been approved for {{fromStatus}} and is now ready for {{toStatus}}. "
                    + "There may be additional information to complete on this project.",
            project,
            Info,
            null,
            null,
            availableRoles()
    ),

    PaymentAwaitingAuthorisation(
            NotificationType.PaymentAwaitingAuthorisation,
            "A payment is awaiting authorisation",
            "A payment for project P{{projectId}} is awaiting authorisation",
            paymentGroup,
            Action,
            Assignees,
            "Payment Authorisation Requested",
            "payment.authorisation.requested.template.html",
            GLA_SPM, GLA_ORG_ADMIN
    ),

    PaymentAuthorisation(
            NotificationType.PaymentAuthorisation,
            "A payment has been authorised",
            "Payment(s) for project P{{projectId}} and organisation {{organisation.name}} has been authorised",
            payment,
            Info,
            null,
            null,
            availableRoles()
    ),
    PaymentSchedulerSummary(
            NotificationType.PaymentSchedulerSummary,
            "A payment scheduler has been run",
            "A payment scheduler run on date {{scheduledDate}}. A number of {{nbProjects}} Active Learning Grant projects found, "
                    + "{{failedDueToMissingWbsCode}} failed to missing WBS code, "
                    + "{{failedDueToDuplication}} failed due to duplication, "
                    + "{{failedDueToOtherReason}} failed due to other reason.",
            payment,
            Info,
            "GLA Open Project System Payment Scheduler Summary",
            "payment.scheduler.summary.template.html",
            Arrays.asList(OPS_ADMIN)
    );

    private final NotificationType type;
    private final String trigger;
    private final String text;
    private final EntityType entityType;
    private final NotificationSubType subType;
    private final NotificationTargetUsersType targetUsersType;
    private final String emailSubject;
    private final String emailTemplate;
    private final String[] rolesNotified;

    NotificationTypeLegacy(NotificationType type, String trigger, String text, EntityType entityType, NotificationSubType subType, String emailSubject,
                           String emailTemplate, Collection<String> rolesNotified) {
        this(type, trigger, text, entityType, subType, null, emailSubject, emailTemplate,
                rolesNotified.toArray(new String[rolesNotified.size()]));
    }

    NotificationTypeLegacy(NotificationType type, String trigger, String text, EntityType entityType, NotificationSubType subType,
                           NotificationTargetUsersType targetUsersType, String emailSubject, String emailTemplate,
                           String... rolesNotified) {
        this.type = type;
        this.trigger = trigger;
        this.text = text;
        this.entityType = entityType;
        this.subType = subType;
        this.targetUsersType = targetUsersType;
        this.emailSubject = emailSubject;
        this.emailTemplate = emailTemplate;
        this.rolesNotified = rolesNotified;
    }

    public String getTrigger() {
        return trigger;
    }

    public String getText() {
        return text;
    }

    public EntityType getEntityType() {
        return entityType;
    }

    public NotificationSubType getSubType() {
        return subType;
    }

    public NotificationTargetUsersType getTargetUsersType() {
        return targetUsersType;
    }

    public String getEmailSubject() {
        return emailSubject;
    }

    public String getEmailTemplate() {
        return emailTemplate;
    }

    public String[] getRolesNotified() {
        return rolesNotified;
    }

    public NotificationType getType() {
        return type;
    }

    public boolean generatesEmails() {
        return StringUtils.isNotEmpty(emailSubject) && StringUtils.isNotEmpty(emailTemplate);
    }

}
