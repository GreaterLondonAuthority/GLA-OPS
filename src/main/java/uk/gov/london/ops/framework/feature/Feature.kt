/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.framework.feature

enum class Feature {

    OutputCSV,
    BoroughReport,
    MilestoneReport,
    TestOnlyStatusTransitions,
    Payments,
    AuthorisedPaymentsReport,
    SubmitAutoApprovalProject,
    ProjectRiskAndIssues,
    Notifications,
    ImsImport, // TODO : this should not be required anymore
    CMSDatePicker, // TODO : not sure this is required anymore
    StartOnSiteRestrictionText,
    ResolveInboundSAPRecordsByWBSCode,
    AffordableHousingReport,
    Dashboard,
    Reclaims,
    MonetaryValueReclaims,
    SpendAuthorityLimits,
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
    AllowChangeInUseAssessmentTemplate

}