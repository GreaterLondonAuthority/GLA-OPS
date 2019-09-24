/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.framework.feature

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component
import uk.gov.london.ops.Environment
import uk.gov.london.ops.di.DataInitialiserModule

@Component
class FeatureDataInitialiser @Autowired constructor(val environment: Environment,
                                                    val featureRepository: FeatureRepository,
                                                    val jdbcTemplate: JdbcTemplate) : DataInitialiserModule {

    @Value("\${feature.enabled.sqlEditor}")
    internal var sqlEditorEnabled: Boolean = false

    @Value("\${feature.enabled.outputcsv}")
    internal var outputCsvEnabled: Boolean = false

    @Value("\${feature.enabled.boroughReport}")
    internal var boroughReportEnabled: Boolean = false

    @Value("\${feature.enabled.milestoneReport}")
    internal var milestoneReportEnabled: Boolean = false

    @Value("\${feature.enabled.testStatusTransitions}")
    internal var testStatusTransitionsEnabled: Boolean = false

    @Value("\${feature.enabled.payments}")
    internal var paymentsEnabled: Boolean = false

    @Value("\${feature.enabled.authorisedPaymentsReport}")
    internal var authorisedPaymentsReportEnabled: Boolean = false

    @Value("\${feature.enabled.submitAutoApprovalProject}")
    internal var submitAutoApprovalProject: Boolean = false

    @Value("\${feature.enabled.ProjectRiskAndIssues}")
    internal var projectRiskAndIssues: Boolean = false

    @Value("\${feature.enabled.notifications}")
    internal var notificationsEnabled: Boolean = false

    @Value("\${feature.enabled.imsImport}")
    internal var imsImportEnabled: Boolean = false

    @Value("\${feature.enabled.cmsDatePicker}")
    internal var cmsDatePicker: Boolean = false

    @Value("\${feature.enabled.startOnSiteRestrictionText}")
    internal var startOnSiteRestrictionText: Boolean = false

    @Value("\${feature.enabled.resolveInboundSAPRecordsByWBSCode}")
    internal var resolveInboundSAPRecordsByWBSCode: Boolean = false

    @Value("\${feature.enabled.affordableHousingReport}")
    internal var affordableHousingReport: Boolean = false

    @Value("\${feature.enabled.dashboard}")
    internal var dashboard: Boolean = false

    @Value("\${feature.enabled.reclaims}")
    internal var reclaims: Boolean = false

    @Value("\${feature.enabled.monetaryValueReclaims}")
    internal var monetaryValueReclaims: Boolean = false

    @Value("\${feature.enabled.spendAuthorityLimits}")
    internal var spendAuthorityLimits: Boolean = false

    @Value("\${feature.enabled.strategicUnitsSummary}")
    internal var strategicUnitsSummary: Boolean = false

    @Value("\${feature.enabled.allowNonGLAReportingAccess}")
    internal var allowNonGLAReportingAccess: Boolean = false

    @Value("\${feature.enabled.emailSending}")
    internal var emailSendingEnabled: Boolean = false

    @Value("\${feature.enabled.orgIdLookup}")
    internal var orgIdLookup: Boolean = false

    @Value("\${feature.enabled.allowBlockRevert}")
    internal var allowBlockRevert: Boolean = false

    @Value("\${feature.enabled.projectMenu.MarkForCorporate}")
    internal var markProjectCorporate: Boolean = false

    @Value("\${feature.enabled.projectMenu.labels}")
    internal var labels: Boolean = false

    @Value("\${feature.enabled.skillsPayments}")
    internal var skillsPayments: Boolean = false

    @Value("\${feature.enabled.skillsPaymentsScheduler}")
    internal var skillsPaymentsScheduler: Boolean = false

    @Value("\${feature.enabled.allowChangeInUseQuestion}")
    internal var allowChangeInUseQuestion: Boolean = false

    @Value("\${feature.enabled.createAnnualReturn}")
    internal var createAnnualReturn: Boolean = false

    @Value("\${feature.enabled.fastProjectSummary}")
    internal var fastProjectSummary: Boolean = false

    @Value("\${feature.enabled.allowChangeInUseAssessmentTemplate}")
    internal var allowChangeInUseAssessmentTemplate: Boolean = false

    override fun getName(): String {
        return "Feature data initialiser"
    }

    override fun runInAllEnvironments(): Boolean {
        return true
    }

    override fun beforeInitialisation() {}

    override fun cleanupOldData() {
        if (environment.isTestEnvironment) {
            jdbcTemplate.update("delete from feature")
        }
    }

    override fun addReferenceData() {
        initFeatureStatuses()
    }

    override fun addUsers() {}

    override fun addOrganisations() {}

    override fun addTemplates() {}

    override fun addProgrammes() {}

    override fun addProjects() {}

    override fun addSupplementalData() {}

    override fun afterInitialisation() {}

    fun initFeatureStatuses() {
        initEnabled(Feature.OutputCSV, outputCsvEnabled)
        initEnabled(Feature.BoroughReport, boroughReportEnabled)
        initEnabled(Feature.MilestoneReport, milestoneReportEnabled)
        initEnabled(Feature.TestOnlyStatusTransitions, testStatusTransitionsEnabled)
        initEnabled(Feature.Payments, paymentsEnabled)
        initEnabled(Feature.AuthorisedPaymentsReport, authorisedPaymentsReportEnabled)
        initEnabled(Feature.SubmitAutoApprovalProject, submitAutoApprovalProject)
        initEnabled(Feature.ProjectRiskAndIssues, projectRiskAndIssues)
        initEnabled(Feature.Notifications, notificationsEnabled)
        initEnabled(Feature.ImsImport, imsImportEnabled)
        initEnabled(Feature.CMSDatePicker, cmsDatePicker)
        initEnabled(Feature.StartOnSiteRestrictionText, startOnSiteRestrictionText)
        initEnabled(Feature.ResolveInboundSAPRecordsByWBSCode, resolveInboundSAPRecordsByWBSCode)
        initEnabled(Feature.AffordableHousingReport, affordableHousingReport)
        initEnabled(Feature.Dashboard, dashboard)
        initEnabled(Feature.Reclaims, reclaims)
        initEnabled(Feature.MonetaryValueReclaims, monetaryValueReclaims)
        initEnabled(Feature.SpendAuthorityLimits, spendAuthorityLimits)
        initEnabled(Feature.SqlEditor, sqlEditorEnabled)
        initEnabled(Feature.StrategicUnitsSummary, strategicUnitsSummary)
        initEnabled(Feature.AllowNonGLAReportingAccess, allowNonGLAReportingAccess)
        initEnabled(Feature.EmailSending, emailSendingEnabled)
        initEnabled(Feature.OrgIdLookup, orgIdLookup)
        initEnabled(Feature.AllowBlockRevert, allowBlockRevert)
        initEnabled(Feature.MarkProjectCorporate, markProjectCorporate)
        initEnabled(Feature.Labels, labels)
        initEnabled(Feature.SkillsPayments, skillsPayments)
        initEnabled(Feature.SkillsPaymentsScheduler, skillsPaymentsScheduler)
        initEnabled(Feature.AllowChangeInUseQuestion, allowChangeInUseQuestion)
        initEnabled(Feature.CreateAnnualReturn, createAnnualReturn)
        initEnabled(Feature.FastProjectSummary, fastProjectSummary)
        initEnabled(Feature.AllowChangeInUseAssessmentTemplate, allowChangeInUseAssessmentTemplate)
    }

    private fun initEnabled(feature: Feature, enabled: Boolean) {
        val featureEntity = featureRepository.findById(feature.name).orElse(null)
        if (featureEntity == null) {
            featureRepository.save(FeatureEntity(feature.name, enabled))
        } else if (environment.isTestEnvironment) {
            //reset feature toggle in test envs to default.
            featureEntity.isEnabled = enabled
            featureRepository.save(featureEntity)
        }
    }

}
