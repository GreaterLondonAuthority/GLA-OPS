/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.notification;

import static uk.gov.london.common.user.BaseRole.GLA_ORG_ADMIN;
import static uk.gov.london.common.user.BaseRole.GLA_SPM;
import static uk.gov.london.common.user.BaseRole.ORG_ADMIN;
import static uk.gov.london.common.user.BaseRole.availableRoles;
import static uk.gov.london.common.user.BaseRole.externalRoles;
import static uk.gov.london.common.user.BaseRole.glaRoles;
import static uk.gov.london.ops.framework.EntityType.annualSubmission;
import static uk.gov.london.ops.framework.EntityType.organisation;
import static uk.gov.london.ops.framework.EntityType.payment;
import static uk.gov.london.ops.framework.EntityType.paymentGroup;
import static uk.gov.london.ops.framework.EntityType.project;
import static uk.gov.london.ops.framework.EntityType.user;
import static uk.gov.london.ops.notification.NotificationSubType.Action;
import static uk.gov.london.ops.notification.NotificationSubType.Info;

import java.util.Arrays;
import java.util.Collection;
import org.apache.commons.lang3.StringUtils;
import uk.gov.london.ops.framework.EntityType;

public enum NotificationType {

    Adhoc(
            "Manually triggered by an OPS admin",
            null,
            null,
            null,
            null,
            null
    ),

    OrganisationRegistration(
            "An external user registers an organisation",
            "{{name}} has registered against your managing organisation and requires approval",
            organisation,
            Action,
            "GLA Open Project System pending organisation registration",
            "organisation.registration.template.html",
            GLA_ORG_ADMIN),

    OrganisationApproval(
            "A GLA user approves an organisation",
            "Your {{name}} registration for {{managingOrganisationName}} has been approved and you are now the admin. "
                    + "Give users this code to register their account with this organisation: {{id}}. "
                    + "You must approve their request on the all users page.",
            organisation,
            Info,
            "GLA Open Project System organisation approved",
            "organisation.approval.template.html",
            ORG_ADMIN
    ),

    OrganisationRejection(
            "A GLA user rejects an organisation",
            "{{name}} registration has been rejected for the following reason: {{changeStatusReasonDetails}}.",
            organisation,
            Info,
            "GLA Open Project System organisation registration rejected",
            "organisation.rejection.template.html",
            ORG_ADMIN),

    OrganisationInactivation(
            "A GLA user marks an organisation as inactive",
            "{{name}} has been set to inactive for the following reason: {{changeStatusReasonDetails}}.",
            organisation,
            Info,
            "GLA Open Project System organisation inactivated",
            "organisation.inactivation.template.html",
            externalRoles()
    ),

    OrganisationReapproval(
            "A GLA user re-approves an organisation",
            "Your user role for {{name}} has been re-instated as {{name}} has been re-approved",
            organisation,
            Info,
            "GLA Open Project System organisation re-approved",
            "organisation.reapproved.template.html",
            externalRoles()
    ),

    UserRequestAccess(
            "An user requests access to an organisation.",
            "A new registration against {{organisation.name}} is pending for {{fullName}}",
            user,
            Info,
            "GLA Open Project System pending registration request",
            "registration.request.template.html",
            Arrays.asList("ROLE_ORG_ADMIN", "ROLE_GLA_ORG_ADMIN")
    ),

    UserAccessApproval(
            "An user role with an organisation is approved.",
            "Your {{organisation.name}} registration for {{organisation.managingOrganisation.name}} has been approved.",
            user,
            Info,
            "GLA Open Project System registration approved",
            "registration.approval.template.html",
            Arrays.asList("ROLE_USER")
    ),

    UserTeamAccessApproval(
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
            "An user role with an organisation is rejected.",
            "Your request to access {{organisation.name}} on OPS has been rejected. Your login details are still valid,"
                    + " you can login and request access to an alternative organisation.",
            user,
            Info,
            "GLA Open Project System user registration rejected",
            "registration.rejection.template.html",
            Arrays.asList("ROLE_USER")
    ),

    OrganisationRemoval(
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
            "An annual submission has been approved.",
            "The {{financialyear}} recoverable grant submission for {{organisation.name}} has been approved.",
            annualSubmission,
            Info,
            null,
            null,
            externalRoles()
    ),

    PendingSpendAuthorityThresholdApproval(
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
            "A project has been transferred to another organisation",
            "Project {{id}} has been transferred from {{fromOrganisation.name}} to {{toOrganisation.name}}. "
                    + "{{fromOrganisation.name}} no longer has access to this project. If you have any issues, "
                    + "email the OPS team at ops@london.gov.uk.",
            project,
            Info,
            null,
            null,
            availableRoles()
    ),

    ProjectReturn(
            "A project has been returned",
            "Project P{{id}} for {{organisation.name}} has been returned and may require updates",
            project,
            Info,
            null,
            null,
            availableRoles()
    ),

    ProjectApprovalRequested(
            "A project has been updated and requires approval",
            "Project P{{id}} for {{organisation.name}} has updates requiring approval",
            project,
            Action,
            null,
            null,
            availableRoles()
    ),

    ApprovedForNextStage(
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
            "A payment is awaiting authorisation",
            "A payment for project P{{projectId}} is awaiting authorisation",
            paymentGroup,
            Action,
            null,
            null,
            Arrays.asList(GLA_SPM)
    ),

    PaymentAuthorisation(
            "A payment has been authorised",
            "Payment(s) for project P{{projectId}} and organisation {{organisation.name}} has been authorised",
            payment,
            Info,
            null,
            null,
            availableRoles()
    );

    private final String trigger;
    private final String text;
    private final EntityType entityType;
    private final NotificationSubType subType;
    private final String emailSubject;
    private final String emailTemplate;
    private final String[] rolesNotified;

    NotificationType(String trigger, String text, EntityType entityType, NotificationSubType subType, String emailSubject,
            String emailTemplate, Collection<String> rolesNotified) {
        this(trigger, text, entityType, subType, emailSubject, emailTemplate,
                rolesNotified.toArray(new String[rolesNotified.size()]));
    }

    NotificationType(String trigger, String text, EntityType entityType, NotificationSubType subType, String emailSubject,
            String emailTemplate, String... rolesNotified) {
        this.trigger = trigger;
        this.text = text;
        this.entityType = entityType;
        this.subType = subType;
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

    public String getEmailSubject() {
        return emailSubject;
    }

    public String getEmailTemplate() {
        return emailTemplate;
    }

    public String[] getRolesNotified() {
        return rolesNotified;
    }

    public boolean generatesEmails() {
        return StringUtils.isNotEmpty(emailSubject) && StringUtils.isNotEmpty(emailTemplate);
    }

}
