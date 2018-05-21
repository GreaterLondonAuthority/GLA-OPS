/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.stereotype.Component;
import uk.gov.london.ops.domain.FeatureEntity;
import uk.gov.london.ops.repository.FeatureRepository;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.TreeMap;

/**
 * Status of feature toggles.
 *
 * @author Steve Leach
 */
@Component
public class FeatureStatus implements InfoContributor {

    Logger log = LoggerFactory.getLogger(getClass());

    public enum Feature {
        OutputCSV,
        BoroughReport,
        MilestoneReport,
        TestOnlyStatusTransitions,
        Payments,
        AuthorisedPaymentsReport,
        SubmitAutoApprovalProject,
        ChangeReporting,
        ProjectRiskAndIssues,
        Notifications,
        ImsImport,
        CMSDatePicker,
        ManagingOrgFilter,
        StartOnSiteRestrictionText,
        ResolveInboundSAPRecordsByWBSCode,
        AffordableHousingReport,
        Dashboard,
        Reclaims
    }

    @Autowired
    FeatureRepository featureRepository;

    @Value("${feature.enabled.outputcsv}")
    boolean outputCsvEnabled;

    @Value("${feature.enabled.boroughReport}")
    boolean boroughReportEnabled;

    @Value("${feature.enabled.milestoneReport}")
    boolean milestoneReportEnabled;

    @Value("${feature.enabled.testStatusTransitions}")
    boolean testStatusTransitionsEnabled;

    @Value("${feature.enabled.payments}")
    boolean paymentsEnabled;

    @Value("${feature.enabled.authorisedPaymentsReport}")
    boolean authorisedPaymentsReportEnabled;

    @Value("${feature.enabled.submitAutoApprovalProject}")
    boolean submitAutoApprovalProject;

    @Value("${feature.enabled.changeReporting}")
    boolean changeReportingEnabled;

    @Value("${feature.enabled.ProjectRiskAndIssues}")
    boolean projectRiskAndIssues;

    @Value("${feature.enabled.notifications}")
    boolean notificationsEnabled;

    @Value("${feature.enabled.imsImport}")
    boolean imsImportEnabled;

    @Value("${feature.enabled.cmsDatePicker}")
    boolean cmsDatePicker;

    @Value("${feature.enabled.managingOrgFilter}")
    boolean managingOrgFilter;

    @Value("${feature.enabled.startOnSiteRestrictionText}")
    boolean startOnSiteRestrictionText;

    @Value("${feature.enabled.resolveInboundSAPRecordsByWBSCode}")
    boolean resolveInboundSAPRecordsByWBSCode;

    @Value("${feature.enabled.affordableHousingReport}")
    boolean affordableHousingReport;

    @Value("${feature.enabled.dashboard}")
    boolean dashboard;

    @Value("${feature.enabled.reclaims}")
    boolean reclaims;

    @PostConstruct
    public void initFeatureStatuses() {
        initEnabled(Feature.OutputCSV, outputCsvEnabled);
        initEnabled(Feature.BoroughReport, boroughReportEnabled);
        initEnabled(Feature.MilestoneReport, milestoneReportEnabled);
        initEnabled(Feature.TestOnlyStatusTransitions, testStatusTransitionsEnabled);
        initEnabled(Feature.Payments, paymentsEnabled);
        initEnabled(Feature.AuthorisedPaymentsReport, authorisedPaymentsReportEnabled);
        initEnabled(Feature.SubmitAutoApprovalProject, submitAutoApprovalProject);
        initEnabled(Feature.ChangeReporting, changeReportingEnabled);
        initEnabled(Feature.ProjectRiskAndIssues, projectRiskAndIssues);
        initEnabled(Feature.Notifications, notificationsEnabled);
        initEnabled(Feature.ImsImport, imsImportEnabled);
        initEnabled(Feature.CMSDatePicker, cmsDatePicker);
        initEnabled(Feature.ManagingOrgFilter, managingOrgFilter);
        initEnabled(Feature.StartOnSiteRestrictionText, startOnSiteRestrictionText);
        initEnabled(Feature.ResolveInboundSAPRecordsByWBSCode, resolveInboundSAPRecordsByWBSCode);
        initEnabled(Feature.AffordableHousingReport, affordableHousingReport);
        initEnabled(Feature.Dashboard, dashboard);
        initEnabled(Feature.Reclaims, reclaims);
    }

    private void initEnabled(Feature feature, boolean enabled) {
        FeatureEntity featureEntity = featureRepository.findOne(feature.name());
        if (featureEntity == null) {
            featureRepository.save(new FeatureEntity(feature.name(), enabled));
        }
    }

    public boolean isEnabled(Feature feature) {
        FeatureEntity featureEntity = featureRepository.findOne(feature.name());
        if (featureEntity != null) {
            return featureEntity.isEnabled();
        }
        return true;
    }

    public void setEnabled(Feature feature, boolean enabled) {
        FeatureEntity featureEntity = featureRepository.findOne(feature.name());
        if (featureEntity != null) {
            featureEntity.setEnabled(enabled);
            featureRepository.save(featureEntity);
        }
    }

    public void contribute(Info.Builder builder) {
        Map<String,Object> data = new TreeMap<>();

        for (Feature feature: Feature.values()) {
            data.put(feature.name(), isEnabled(feature));
        }

        builder.withDetail("featureToggles", data);
    }

}
