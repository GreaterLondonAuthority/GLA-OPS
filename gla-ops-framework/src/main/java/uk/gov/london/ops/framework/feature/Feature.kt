/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.framework.feature

enum class Feature {
    TestOnlyStatusTransitions,
    Payments,
    AuthorisedPaymentsReport,
    SubmitAutoApprovalProject,
    Notifications,
    CMSDatePicker, // TODO : not sure this is required anymore
    StartOnSiteRestrictionText,
    ResolveInboundSAPRecordsByWBSCode,
    Dashboard,
    Reclaims,
    MonetaryValueReclaims,
    SqlEditor,
    StrategicUnitsSummary,
    AllowNonGLAReportingAccess,
    EmailSending,
    OrgIdLookup,
    AllowBlockRevert,
    MarkProjectCorporate,
    Labels,
    SkillsPayments,
    SkillsPaymentsScheduler,
    AllowChangeInUseQuestion,
    CreateAnnualReturn,
    FastProjectSummary,
    AllowChangeInUseAssessmentTemplate,
    AllowAllFileDownload,
    AllowLearningProvidersSkillsGatewayAccess,
    AllowMultipleRolesProcess,
    ProjectSharing,
    AllowProjectCloning,
    AllowExternalFileStorage,
    AllowCreateAnnualReturnInTheFuture,
    AllowLegalStatusOnRegistration,
    PostLoginLegalStatusNotification,
    ShowAccessibilityUrl,
    ProjectSubmissionReminder,
    PreventClaimsWithoutFinanceEmail,
    AllowPaymentsWithoutApproval,
    CancelApprovedActivities,
    ProgrammeAllocationsPage,
    UseFastPendingPayments
}
