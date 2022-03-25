/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.payment.implementation.di;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.london.ops.contracts.*;
import uk.gov.london.ops.framework.di.DataInitialiserModule;
import uk.gov.london.ops.framework.enums.OrganisationContractStatus;
import uk.gov.london.ops.framework.environment.Environment;
import uk.gov.london.ops.organisation.OrganisationServiceImpl;
import uk.gov.london.ops.organisation.model.OrganisationEntity;
import uk.gov.london.ops.payment.*;
import uk.gov.london.ops.payment.implementation.repository.PaymentGroupRepository;
import uk.gov.london.ops.payment.implementation.repository.ProjectLedgerRepository;
import uk.gov.london.ops.payment.implementation.repository.SapDataRepository;
import uk.gov.london.ops.programme.ProgrammeDetailsSummary;
import uk.gov.london.ops.programme.ProgrammeService;
import uk.gov.london.ops.project.Project;
import uk.gov.london.ops.project.ProjectBuilder;
import uk.gov.london.ops.project.ProjectService;
import uk.gov.london.ops.project.block.ProjectBlockType;
import uk.gov.london.ops.project.claim.ClaimStatus;
import uk.gov.london.ops.project.implementation.di.ProjectDataInitialiser;
import uk.gov.london.ops.project.milestone.Milestone;
import uk.gov.london.ops.project.milestone.MilestoneStatus;
import uk.gov.london.ops.project.milestone.ProjectMilestonesBlock;
import uk.gov.london.ops.project.skills.LearningGrantBlock;
import uk.gov.london.ops.project.skills.LearningGrantEntry;
import uk.gov.london.ops.project.skills.LearningGrantEntryType;
import uk.gov.london.ops.project.state.ProjectStatus;
import uk.gov.london.ops.project.state.ProjectSubStatus;
import uk.gov.london.ops.project.template.TemplateServiceImpl;
import uk.gov.london.ops.project.template.domain.Template;
import uk.gov.london.ops.project.template.domain.TemplateTenureType;
import uk.gov.london.ops.project.unit.UnitDetailsBlock;
import uk.gov.london.ops.project.unit.UnitDetailsTableEntry;
import uk.gov.london.ops.refdata.CategoryValue;
import uk.gov.london.ops.refdata.RefDataServiceImpl;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static uk.gov.london.ops.organisation.Organisation.TEST_ORG_ID_1;
import static uk.gov.london.ops.payment.ProjectLedgerEntry.DEFAULT_LEDGER_CE_CODE;
import static uk.gov.london.ops.project.ProjectBuilder.STATUS_ACTIVE;
import static uk.gov.london.ops.refdata.PaymentSourceKt.GRANT;

@Transactional
@Component
public class PaymentDataInitialiser implements DataInitialiserModule {

    public static final String TEST_INVALID_SAP_DATA_CONTENT = "Hello, world!";

    @Autowired
    private ProjectDataInitialiser projectDataInitialiser;

    @Autowired
    private ProjectLedgerRepository projectLedgerRepository;

    @Autowired
    private PaymentGroupRepository paymentGroupRepository;

    @Autowired
    private ProgrammeService programmeService;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private SapDataRepository sapDataRepository;

    @Autowired
    private TemplateServiceImpl templateService;

    @Autowired
    private OrganisationServiceImpl organisationService;

    @Autowired
    private RefDataServiceImpl refDataService;

//    @Autowired
//    private ContractRepository contractRepository;

    @Autowired
    private ContractService contractService;

    @Autowired
    private Environment environment;

    @Autowired
    private ProjectBuilder projectBuilder;

    @Autowired
    private PaymentAuditService paymentAuditService;

    private Template housingTemplate;
    private Template smallProjects;
    private Project activeProjectWithPayments;
    private Project closedProjectWithPayments;
    private Project noWBSReclaims;
    private Project monetary;

    @Override
    public String getName() {
        return "Payment data initialiser";
    }

    @Override
    public void addTemplates() {
        housingTemplate = templateService.findByName("Approved Provider Route");
        smallProjects = templateService.findByName("Small Projects and Equipment Fund");
    }

    @Override
    public void addProjects() {
        ProgrammeDetailsSummary bucketProgramme = programmeService.getProgrammeDetailsSummary("Bucket programme");
        ProgrammeDetailsSummary mainstream = programmeService.getProgrammeDetailsSummary("Mainstream housing programme test");
        ProgrammeDetailsSummary disabledPaymentProgramme = programmeService.getProgrammeDetailsSummary("Disabled Payments Programme");
        ProgrammeDetailsSummary noWBSCode = programmeService.getProgrammeDetailsSummary("Programme without WBS");
        ProgrammeDetailsSummary noCECode = programmeService.getProgrammeDetailsSummary("Programme without CE");

        activeProjectWithPayments = projectBuilder.createPopulatedTestProject("Active project with payments",
                bucketProgramme.getId(), housingTemplate, TEST_ORG_ID_1, STATUS_ACTIVE);
        projectBuilder.approveProject(activeProjectWithPayments);
        projectService.save(activeProjectWithPayments);

        Project project = createCompletedMilestoneProject(disabledPaymentProgramme.getId(), housingTemplate, 9999,
                 "Active Housing project disabled payments", 200005L);
        for (Milestone milestone : project.getMilestonesBlock().getMilestones()) {
            milestone.setClaimStatus(ClaimStatus.Pending);
        }
        project.setStatus(ProjectStatus.Active);
        project.setSubStatus(ProjectSubStatus.UnapprovedChanges);
        projectService.save(project);
        createCompletedMilestoneProject(disabledPaymentProgramme.getId(), smallProjects, 9999,
                "Active Regen project disabled payments", 200005L);

        createCompletedMilestoneProject(noWBSCode.getId(), housingTemplate, 9997, "Active project no wbs code",
                200005L);
        createCompletedMilestoneProject(noCECode.getId(), housingTemplate, 9997, "Active project no ce code",
                200005L);
        createCompletedMilestoneProject(bucketProgramme.getId(), "Active project ready for payments", 200005L);
        createCompletedMilestoneProject(bucketProgramme.getId(), "Project required round down", 200006L);

        closedProjectWithPayments = projectBuilder.createPopulatedTestProject("Closed project with payments",
                bucketProgramme.getId(), housingTemplate, TEST_ORG_ID_1, STATUS_ACTIVE);
        projectBuilder.approveProject(closedProjectWithPayments);
        closedProjectWithPayments.setStatus(ProjectStatus.Closed);
        closedProjectWithPayments.setSubStatus(ProjectSubStatus.Completed);
        projectService.save(closedProjectWithPayments);

        noWBSReclaims = projectBuilder.createPopulatedTestProject("Pending reclaim no wbs", mainstream.getId(),
                housingTemplate, TEST_ORG_ID_1, STATUS_ACTIVE);
        projectBuilder.approveProject(noWBSReclaims);
        noWBSReclaims.setStatus(ProjectStatus.Closed);
        noWBSReclaims.setSubStatus(ProjectSubStatus.Completed);
        projectService.save(noWBSReclaims);
    }

    private void createCompletedMilestoneProject(Integer programmeId, String title, Long grantSource) {
        createCompletedMilestoneProject(programmeId, housingTemplate, TEST_ORG_ID_1, title, grantSource);
    }

    private Project createCompletedMilestoneProject(Integer programmeId, Template template, Integer orgId, String title,
                                                    Long grantSource) {
        Project activeProjectReadyForPayments = projectBuilder.createPopulatedTestProject(title, programmeId, template, orgId,
                STATUS_ACTIVE);
        activeProjectReadyForPayments.getGrantSourceBlock().setZeroGrantRequested(false);
        activeProjectReadyForPayments.getGrantSourceBlock().setGrantValue(grantSource);

        Milestone m = new Milestone();
        m.setMilestoneDate(LocalDate.of(2012, 1, 10));
        m.setMonetary(true);
        m.setMilestoneStatus(MilestoneStatus.ACTUAL);
        m.setSummary("Added Milestone");
        activeProjectReadyForPayments.getMilestonesBlock().getMilestones().add(m);
        projectBuilder.approveProject(activeProjectReadyForPayments);
        projectService.save(activeProjectReadyForPayments);
        projectBuilder.cloneBlock(activeProjectReadyForPayments, ProjectBlockType.Milestones);
        Set<Milestone> milestones = activeProjectReadyForPayments.getMilestonesBlock().getMilestones();
        for (Milestone milestone : milestones) {
            if (milestone.getMonetary()) {
                if (milestone.getSummary().equals("Completion")) {
                    milestone.setMonetarySplit(70);
                } else {
                    milestone.setMonetarySplit(10);
                }
            }
            milestone.setClaimStatus(ClaimStatus.Claimed);
        }
        activeProjectReadyForPayments.setSubStatus(ProjectSubStatus.ApprovalRequested);

        UnitDetailsBlock unitDetailsBlock = activeProjectReadyForPayments.getUnitDetailsBlock();
        if (unitDetailsBlock != null) {
            unitDetailsBlock.setLastModified(environment.now());
            unitDetailsBlock.setNbWheelchairUnits(10);
            unitDetailsBlock.setGrossInternalArea(10);
            unitDetailsBlock.setType2Units(300);
            unitDetailsBlock.setNewBuildUnits(300);
            UnitDetailsTableEntry entry = new UnitDetailsTableEntry();
            entry.setNbUnits(300);
            entry.setType(UnitDetailsTableEntry.Type.Rent);
            entry.setWeeklyMarketRent(new BigDecimal("123.12"));
            entry.setWeeklyServiceCharge(new BigDecimal("23.12"));
            entry.setNetWeeklyRent(new BigDecimal("1123.12"));
            TemplateTenureType templateTenureType =
                    activeProjectReadyForPayments.getTemplate().getTenureTypes().stream()
                            .filter(p -> p.getTenureType().getId().equals(4002)).findFirst().get();
            entry.setTenureId(templateTenureType.getTenureType().getId());
            entry.setMarketType(refDataService.getMarketType(templateTenureType.getMarketTypes().get(0).getId()));
            entry.setNbBeds(new CategoryValue(2, null, null, null));
            entry.setUnitType(new CategoryValue(10, null, null, null));
            entry.setProjectId(activeProjectReadyForPayments.getId());
            unitDetailsBlock.getTableEntries().add(entry);
        }

        projectBuilder.generateProjectApprovalHistory(activeProjectReadyForPayments);

        projectService.save(activeProjectReadyForPayments);
        final String contractName = activeProjectReadyForPayments.getTemplate().getName();

        ContractModel housingContractModel = contractService.findByName(contractName);
        if (housingContractModel == null) {
            housingContractModel = contractService.create(new ContractModel(contractName));
        }
        OrganisationEntity organisation = activeProjectReadyForPayments.getOrganisation();
        organisationService.enrichOrganisationContracts(organisation);
        if (!organisation.getContractEntities().stream()
                .anyMatch(c -> c.getContract().getName().equals(contractName))) {
            organisationService.createContract(organisation.getId(),
                    new ContractSummary(housingContractModel.getId(), OrganisationContractStatus.Signed));
        }
        return activeProjectReadyForPayments;
    }

    private void populateDefaultPaymentData(ProjectLedgerEntry grant, Project project1) {
        if (project1.getSingleLatestBlockOfType(ProjectBlockType.LearningGrant) != null) {
            grant.setBlockId(project1.getSingleLatestBlockOfType(ProjectBlockType.LearningGrant).getId());
        } else {
            grant.setBlockId(project1.getSingleLatestBlockOfType(ProjectBlockType.Milestones).getId());
        }
        OrganisationEntity payOrg = project1.getOrganisation();
        grant.setOrganisationId(payOrg.getId());
        grant.setVendorName(payOrg.getName());
        grant.setLedgerType(LedgerType.PAYMENT);
        grant.setPaymentSource(GRANT);
        grant.setSapVendorId(payOrg.getDefaultSapVendorId());
        grant.setProjectName(project1.getTitle());
        grant.setProgrammeName(project1.getProgramme().getName());
        grant.setManagingOrganisation(project1.getManagingOrganisation());
    }

    @Override
    public void addSupplementalData() {
        addLedgerEntries();
        addSapData();
        addAuditHistory();
    }

    private void addAuditHistory() {
        List<ProjectLedgerEntry> allByProjectId = projectLedgerRepository.findAllByProjectId(monetary.getId());
        allByProjectId.addAll(projectLedgerRepository.findAllByProjectId(
                projectService.findAllByTitle("Active project with payments").get(0).getId()));
        allByProjectId.stream().filter(payment -> payment.getLedgerType().equals(LedgerType.PAYMENT)
                && !payment.getLedgerStatus().equals(LedgerStatus.FORECAST)).forEach(p -> createPaymentHistory(p));
    }

    private void createPaymentHistory(ProjectLedgerEntry payment) {
        for (int i = 0; i < PaymentAuditItemType.values().length; i++) {
            PaymentAuditItemType value = PaymentAuditItemType.values()[i];
            PaymentAuditItem auditItem = new PaymentAuditItem(
                    payment.getId(),
                    OffsetDateTime.of(2020, 1, 2, 1 + i, 12, 12,
                            12, ZoneOffset.UTC),
                    value);
            auditItem.setUsername("test.admin");
            paymentAuditService.savePaymentAuditItem(auditItem);
        }
    }

    private void addLedgerEntries() {
        // Strategic Payments
        Project strategicProjectTestingPayments = projectDataInitialiser.getStrategicProjectToTestPaymentRequests();
        ProjectLedgerEntry grantStategic = createPayment(strategicProjectTestingPayments, 2017, 4,
                "Milestone", "Start on site", BigDecimal.valueOf(-16000.00), LedgerStatus.Authorised);
        grantStategic.setExternalId(3003);
        populateDefaultPaymentData(grantStategic, strategicProjectTestingPayments);

        ProjectLedgerEntry grantStategicSupplemental = createPayment(strategicProjectTestingPayments, 2017, 4,
                "Supplementary", "Start on site", BigDecimal.valueOf(-44000.00), LedgerStatus.Authorised);
        grantStategicSupplemental.setExternalId(3003);
        grantStategicSupplemental.setXmlFile("<xml>some random content</xml>");
        grantStategicSupplemental.setWbsCode("WBS_123");
        populateDefaultPaymentData(grantStategicSupplemental, strategicProjectTestingPayments);

        ProjectLedgerEntry grantStategic2 = createPayment(strategicProjectTestingPayments, 2017, 6,
                "Milestone", "Completion", BigDecimal.valueOf(-46000.00), LedgerStatus.Authorised);
        populateDefaultPaymentData(grantStategic2, strategicProjectTestingPayments);

        ProjectLedgerEntry dpfStategic = createPayment(strategicProjectTestingPayments, 2017, 4,
                "Milestone", "Start on site", BigDecimal.valueOf(-102000.00), LedgerStatus.Authorised);
        dpfStategic.setExternalId(3003);
        populateDefaultPaymentData(dpfStategic, strategicProjectTestingPayments);
        dpfStategic.setLedgerType(LedgerType.DPF);

        //Payments for projectTestingPayments (Three payment types)
        Project projectTestingPayments = projectDataInitialiser.getProjectToTestPaymentRequests();
        ProjectLedgerEntry grant = createPayment(projectTestingPayments, 2017, 4, "Milestone",
                "Start on site", BigDecimal.valueOf(-4000.00), LedgerStatus.Authorised);
        grant.setExternalId(3003);
        populateDefaultPaymentData(grant, projectTestingPayments);

        ProjectLedgerEntry grant2 = createPayment(projectTestingPayments, 2017, 6, "Milestone",
                "Completion", BigDecimal.valueOf(-12000.00), LedgerStatus.Authorised);
        populateDefaultPaymentData(grant2, projectTestingPayments);

        ProjectLedgerEntry grant3 = createPayment(projectTestingPayments, 2017, 5, "Milestone",
                "Start on site", BigDecimal.valueOf(-3000.00), LedgerStatus.Authorised);
        grant.setExternalId(3003);
        populateDefaultPaymentData(grant3, projectTestingPayments);

        ProjectLedgerEntry grant4 = createPayment(projectTestingPayments, 2017, 7, "Milestone",
                "Completion", BigDecimal.valueOf(-11000.00), LedgerStatus.Authorised);
        populateDefaultPaymentData(grant4, projectTestingPayments);

        ProjectLedgerEntry dpf = createPayment(projectTestingPayments, 2017, 4, "Milestone",
                "Start on site", BigDecimal.valueOf(-2000.00), LedgerStatus.Authorised);
        populateDefaultPaymentData(dpf, projectTestingPayments);
        dpf.setLedgerType(LedgerType.DPF);

        ProjectLedgerEntry dpf2 = createPayment(projectTestingPayments, 2017, 6, "Milestone",
                "Completion", BigDecimal.valueOf(-4000.00), LedgerStatus.Authorised);
        populateDefaultPaymentData(dpf2, projectTestingPayments);
        dpf2.setLedgerType(LedgerType.DPF);

        ProjectLedgerEntry dpf3 = createPayment(projectTestingPayments, 2017, 5, "Milestone",
                "Start on site", BigDecimal.valueOf(-1000.00), LedgerStatus.Authorised);
        populateDefaultPaymentData(dpf3, projectTestingPayments);
        dpf3.setLedgerType(LedgerType.DPF);

        ProjectLedgerEntry dpf4 = createPayment(projectTestingPayments, 2017, 7, "Milestone",
                "Completion", BigDecimal.valueOf(-3000.00), LedgerStatus.Authorised);
        populateDefaultPaymentData(dpf4, projectTestingPayments);
        dpf4.setLedgerType(LedgerType.DPF);

        ProjectLedgerEntry rcgf = createPayment(projectTestingPayments, 2017, 4, "Milestone",
                "Start on site", BigDecimal.valueOf(-1000.00), LedgerStatus.Authorised);
        rcgf.setExternalId(3003);
        populateDefaultPaymentData(rcgf, projectTestingPayments);
        rcgf.setLedgerType(LedgerType.RCGF);

        ProjectLedgerEntry rcgf2 = createPayment(projectTestingPayments, 2017, 4, "Milestone",
                "Start on site", BigDecimal.valueOf(-6000.00), LedgerStatus.Authorised);
        rcgf2.setExternalId(3003);
        populateDefaultPaymentData(rcgf2, projectTestingPayments);
        rcgf2.setLedgerType(LedgerType.RCGF);

        ProjectLedgerEntry rcgf3 = createPayment(projectTestingPayments, 2017, 5, "Milestone",
                "Start on site", BigDecimal.valueOf(-2000.00), LedgerStatus.Authorised);
        rcgf3.setExternalId(3003);
        populateDefaultPaymentData(rcgf3, projectTestingPayments);
        rcgf3.setLedgerType(LedgerType.RCGF);

        ProjectLedgerEntry rcgf4 = createPayment(projectTestingPayments, 2017, 5, "Milestone",
                "Start on site", BigDecimal.valueOf(-76000.00), LedgerStatus.Authorised);
        rcgf4.setExternalId(3003);
        populateDefaultPaymentData(rcgf4, projectTestingPayments);
        rcgf4.setLedgerType(LedgerType.RCGF);

        ProjectLedgerEntry receipt = createPayment(projectTestingPayments, 2017, 4, "Milestone",
                "Start on site", BigDecimal.valueOf(-1000.00), LedgerStatus.Authorised);
        receipt.setExternalId(3003);
        populateDefaultPaymentData(receipt, projectTestingPayments);
        receipt.setLedgerType(LedgerType.RECEIPT);

        // Payment for Skills Project
        Project skillsPaymentProject = projectService.findAllByTitle("Project for testing skills payments").get(0);
        ProjectLedgerEntry skillsPaymentCleared = addSkillsPaymentForProject(skillsPaymentProject, 2018, 1,
                LearningGrantEntryType.DELIVERY, 2018, 8, "Scheduled",
                "Aug 2018/19", BigDecimal.valueOf(144400).negate(), LedgerStatus.Cleared);
        ProjectLedgerEntry skillsPaymentAuthorised = addSkillsPaymentForProject(skillsPaymentProject, 2018,
                2, LearningGrantEntryType.DELIVERY, 2018, 9, "Scheduled",
                "Sep 2018/19", BigDecimal.valueOf(85800).negate(), LedgerStatus.Authorised);

        // Payment for Active for cancel claimed milestone
        Project cancelProject = projectService.findAllByTitle("Active for cancel claimed milestone").get(0);
        ProjectLedgerEntry cancelGrant = createPayment(cancelProject, 2017, 5, "Milestone", "Land acquired",
                BigDecimal.valueOf(-500000.00), LedgerStatus.Authorised);
        populateDefaultPaymentData(cancelGrant, projectTestingPayments);
        cancelGrant.setLedgerType(LedgerType.PAYMENT);

        ProjectLedgerEntry cancelRcgf = createPayment(cancelProject, 2017, 5, "Milestone", "Land acquired",
                BigDecimal.valueOf(-25000.00), LedgerStatus.Authorised);
        populateDefaultPaymentData(cancelRcgf, projectTestingPayments);
        cancelRcgf.setLedgerType(LedgerType.RCGF);


        // Skills AEB Procured Active with payments
        Project skillsAEBProcuredActiveWithPayments = projectService.findAllByTitle("Skills AEB Procured Active with payments")
                .get(0);
        ProjectLedgerEntry skillProcuredSupportPayment1 = addSkillsPaymentForProject(skillsAEBProcuredActiveWithPayments,
                2018, 1, LearningGrantEntryType.SUPPORT, 2018, 8,
                "Support", "Aug 2018/19", BigDecimal.valueOf(60000.00).negate(), LedgerStatus.Cleared);
        ProjectLedgerEntry skillProcuredSupportPayment2 = addSkillsPaymentForProject(skillsAEBProcuredActiveWithPayments,
                2020, 1, LearningGrantEntryType.DELIVERY, 2018, 8,
                "Delivery", "Aug 2018/19", BigDecimal.valueOf(600000.00).negate(), LedgerStatus.Cleared);
        ProjectLedgerEntry skillProcuredSupportPayment3 = addSkillsPaymentForProject(skillsAEBProcuredActiveWithPayments,
                2020, 3, LearningGrantEntryType.DELIVERY, 2018, 8,
                "Delivery", "Aug 2018/19", BigDecimal.valueOf(900000.00).negate(), LedgerStatus.Cleared);
        ProjectLedgerEntry skillProcuredSupportPayment4 = addSkillsPaymentForProject(skillsAEBProcuredActiveWithPayments,
                2020, 4, LearningGrantEntryType.DELIVERY, 2018, 8,
                "Delivery", "Aug 2018/19", BigDecimal.valueOf(600000.00).negate(), LedgerStatus.Cleared);
        ProjectLedgerEntry skillProcuredSupportPayment5 = addSkillsPaymentForProject(skillsAEBProcuredActiveWithPayments,
                2020, 5, LearningGrantEntryType.DELIVERY, 2018, 8,
                "Delivery", "Aug 2018/19", BigDecimal.valueOf(1000000.00).negate(), LedgerStatus.Cleared);
        ProjectLedgerEntry skillProcuredSupportPayment6 = addSkillsPaymentForProject(skillsAEBProcuredActiveWithPayments,
                2020, 6, LearningGrantEntryType.DELIVERY, 2018, 8,
                "Delivery", "Aug 2018/19", BigDecimal.valueOf(100000.00).negate(), LedgerStatus.Cleared);
        ProjectLedgerEntry skillProcuredSupportPayment7 = addSkillsPaymentForProject(skillsAEBProcuredActiveWithPayments,
                2020, 9, LearningGrantEntryType.DELIVERY, 2018, 8,
                "Delivery", "Aug 2018/19", BigDecimal.valueOf(1000000.00).negate(), LedgerStatus.Cleared);

        //Payments for Project1 (Single milestone)
        Project project1 = projectDataInitialiser.getProjectWithGrantToAuthorise();
        ProjectLedgerEntry cleared = createPayment(project1, 2017, 4, "Milestone",
                "Start on site", BigDecimal.valueOf(-4000.00), LedgerStatus.Cleared);
        cleared.setExternalId(3003);
        populateDefaultPaymentData(cleared, project1);

        ProjectLedgerEntry sent = createPayment(project1, 2017, 4, "Milestone",
                "Completion", BigDecimal.valueOf(-4000.00), LedgerStatus.Sent);
        populateDefaultPaymentData(sent, project1);
        sent.setXmlFile("<xml>some random content</xml>");
        sent.setSentOn(environment.now());

        ProjectLedgerEntry acknowledged = createPayment(project1, 2017, 4, "Milestone",
                "Completion", BigDecimal.valueOf(-4000.00), LedgerStatus.Acknowledged);
        populateDefaultPaymentData(acknowledged, project1);
        acknowledged.setSentOn(environment.now());

        ProjectLedgerEntry supplierError = createPayment(project1, 2017, 4, "Milestone",
                "Completion", BigDecimal.valueOf(-4000.00), LedgerStatus.SupplierError);
        populateDefaultPaymentData(supplierError, project1);
        supplierError.setSentOn(environment.now());

        ProjectLedgerEntry received = createPayment(project1, 2017, 4, "Milestone",
                "Start on site", BigDecimal.valueOf(-4000.00), LedgerStatus.UnderReview);
        populateDefaultPaymentData(received, project1);
        received.setAcknowledgedOn(environment.now());

        /**
         * Second set
         */
        Project project2 = projectDataInitialiser.getSecondProjectWithGrantToAuthorise();
        ProjectLedgerEntry pending1 = createPayment(project2, 2017, 4, "Milestone",
                "To Authorise", BigDecimal.valueOf(-4000.00), LedgerStatus.Pending);
        populateDefaultPaymentData(pending1, project2);

        ProjectLedgerEntry pending2 = createPayment(project2, 2017, 4, "Milestone",
                "To Authorise", BigDecimal.valueOf(-4000.00), LedgerStatus.Pending);
        populateDefaultPaymentData(pending2, project2);

        ProjectLedgerEntry pending3 = createPayment(project2, 2017, 4, "Milestone",
                "Completion", BigDecimal.valueOf(-4000.00), LedgerStatus.Pending);
        populateDefaultPaymentData(pending3, project2);
        pending3.setSentOn(environment.now());

        // project 3
        Project project3 = projectDataInitialiser.getThirdProjectWithGrantToAuthorise();
        ProjectLedgerEntry pending4 = createPayment(project3, 2017, 4, "Milestone",
                "Start on site", BigDecimal.valueOf(-4000.00), LedgerStatus.Pending);
        pending4.setExternalId(3003);

        populateDefaultPaymentData(pending4, project3);
        pending4.setSentOn(environment.now());

        // project 4
        Project project4 = projectDataInitialiser.getFourthProjectWithGrantToAuthorise();
        ProjectLedgerEntry pending5 = createGrantPaymentForProject(project4);

        // project 5
        Project project5 = projectDataInitialiser.getFifthProjectWithGrantToAuthorise();
        ProjectLedgerEntry pending6 = createGrantPaymentForProject(project5);

        Project projectToTestPayments = projectDataInitialiser.getProjectToTestPayments();

        ProjectLedgerEntry pendingNoSap = createPayment(projectToTestPayments, 2017, 4,
                "Milestone", "NoSapId", BigDecimal.valueOf(-12345.67), LedgerStatus.Pending);
        populateDefaultPaymentData(pendingNoSap, projectToTestPayments);
        pendingNoSap.setSapVendorId(null);

        ProjectLedgerEntry pending2NoSap = createPayment(projectToTestPayments, 2017, 4,
                "Milestone", "NoSapId", BigDecimal.valueOf(-12345.67), LedgerStatus.Pending);
        populateDefaultPaymentData(pending2NoSap, projectToTestPayments);
        pending2NoSap.setSapVendorId(null);

        Project declined = projectDataInitialiser.getProjectToTestDeclinedPayments();

        //Payments for declined project (Single milestone)
        ProjectLedgerEntry m1 = createPayment(declined, 2017, 4, "Milestone",
                "Start on site", BigDecimal.valueOf(-1000.00), LedgerStatus.Declined);
        m1.setExternalId(3003);

        populateDefaultPaymentData(m1, declined);
        m1.setModifiedOn(OffsetDateTime.of(2017, 5, 19, 4, 0, 0, 0,
                ZoneOffset.UTC));

        ProjectLedgerEntry m2 = createPayment(declined, 2017, 4, "Milestone", "Completion",
                BigDecimal.valueOf(-2000.00), LedgerStatus.Declined);
        populateDefaultPaymentData(m2, declined);
        m2.setSentOn(environment.now());
        m2.setModifiedOn(OffsetDateTime.of(2017, 5, 19, 4, 0, 0, 0,
                ZoneOffset.UTC));

        Project anotherDeclined = projectDataInitialiser.getAnotherProjectToTestDeclinedPayments();

        //Payments for another anotherDeclined project (two milestone with different payment types)
        ProjectLedgerEntry m3 = createPayment(anotherDeclined, 2017, 4, "Milestone",
                "Start on site", BigDecimal.valueOf(-1030.00), LedgerStatus.Declined);
        m2.setExternalId(3003);
        populateDefaultPaymentData(m3, anotherDeclined);
        m3.setLedgerType(LedgerType.RCGF);
        m3.setModifiedOn(OffsetDateTime.of(2017, 5, 20, 4, 0, 0,
                0, ZoneOffset.UTC));

        ProjectLedgerEntry m3b = createPayment(anotherDeclined, 2017, 4, "Milestone",
                "Start on site", BigDecimal.valueOf(-5030.00), LedgerStatus.Declined);
        m3b.setExternalId(3003);
        populateDefaultPaymentData(m3b, anotherDeclined);
        m3b.setModifiedOn(OffsetDateTime.of(2017, 5, 20, 4, 0, 0,
                0, ZoneOffset.UTC));

        ProjectLedgerEntry m4 = createPayment(anotherDeclined, 2017, 4, "Milestone",
                "Completion", BigDecimal.valueOf(-2030.00), LedgerStatus.Declined);
        populateDefaultPaymentData(m4, anotherDeclined);
        m4.setSentOn(environment.now());
        m4.setLedgerType(LedgerType.DPF);
        m4.setModifiedOn(OffsetDateTime.of(2017, 5, 20, 4, 0, 0,
                0, ZoneOffset.UTC));

        //Payments for project with one paid milestone
        OrganisationEntity org1 = project1.getOrganisation();
        Project supplementalPaymentProject = projectDataInitialiser.getProjectWithSupplementalPayment();
        ProjectLedgerEntry suplemental = createMilestonePayment(supplementalPaymentProject, org1, LedgerType.PAYMENT,
                new BigDecimal(-250000), "Start on site");

        //projectWithReclaimPayment
        Project projectWithReclaimPayment = projectService.findAllByTitle("Project needing Reclaim").get(0);
        ProjectLedgerEntry reclaim = createMilestonePayment(projectWithReclaimPayment, org1, LedgerType.PAYMENT,
                new BigDecimal(-400000L), "Start on site");
        ProjectLedgerEntry reclaimDPF = createMilestonePayment(projectWithReclaimPayment, org1, LedgerType.DPF,
                new BigDecimal(-220000L), "Start on site");
        ProjectLedgerEntry reclaimRCGF = createMilestonePayment(projectWithReclaimPayment, org1, LedgerType.RCGF,
                new BigDecimal(-280000L), "Start on site");

        // projectWithReclaimPaymentInConsortium
        Project projectWithReclaimPaymentInConsortium = projectService.findAllByTitle("Project needing Reclaim in consortium")
                .get(0);
        ProjectLedgerEntry reclaimInConsortium = createMilestonePayment(projectWithReclaimPaymentInConsortium, org1,
                LedgerType.PAYMENT, new BigDecimal(-400000L), "Start on site");
        ProjectLedgerEntry reclaimDPFInConsortium = createMilestonePayment(projectWithReclaimPaymentInConsortium, org1,
                LedgerType.DPF, new BigDecimal(-220000L), "Start on site");
        ProjectLedgerEntry reclaimRCGFInConsortium = createMilestonePayment(projectWithReclaimPaymentInConsortium, org1,
                LedgerType.RCGF, new BigDecimal(-280000L), "Start on site");

        //projectWithMultipleReclaimPayment
        Project projectWithMultipleReclaimPayment = projectService.findAllByTitle("Project needing multiple Reclaims").get(0);
        ProjectLedgerEntry paymentReclaim = createMilestonePayment(projectWithMultipleReclaimPayment, org1, LedgerType.PAYMENT,
                new BigDecimal(-400000), "Start on site");
        ProjectLedgerEntry paymentReclaimDpf = createMilestonePayment(projectWithMultipleReclaimPayment, org1, LedgerType.DPF,
                new BigDecimal(-100000L), "Start on site");
        ProjectLedgerEntry paymentReclaimRcgf = createMilestonePayment(projectWithMultipleReclaimPayment, org1, LedgerType.RCGF,
                new BigDecimal(-180000L), "Start on site");

        //nilGrantProject
        Project nilGrantProject = projectService.findAllByTitle("Project moving to nil grant").get(0);
        ProjectLedgerEntry nilReclaim = createMilestonePayment(nilGrantProject, org1, LedgerType.PAYMENT,
                new BigDecimal(-400000L), "Start on site");
        ProjectLedgerEntry nilReclaimDPF = createMilestonePayment(nilGrantProject, org1, LedgerType.DPF,
                new BigDecimal(-160000L), "Start on site");
        ProjectLedgerEntry nilReclaimRCGF = createMilestonePayment(nilGrantProject, org1, LedgerType.RCGF,
                new BigDecimal(-180000L), "Start on site");

        ProjectLedgerEntry paymentReclaimCompletion = createMilestonePayment(projectWithMultipleReclaimPayment, org1,
                LedgerType.PAYMENT, new BigDecimal(-400000L), "Completion");
        ProjectLedgerEntry paymentReclaimDpfCompletion = createMilestonePayment(projectWithMultipleReclaimPayment, org1,
                LedgerType.DPF, new BigDecimal(-120000L), "Completion");
        ProjectLedgerEntry paymentReclaimRcgfCompletion = createMilestonePayment(projectWithMultipleReclaimPayment, org1,
                LedgerType.RCGF, new BigDecimal(-280000L), "Completion");

        ProjectLedgerEntry nilReclaimCompletion = createMilestonePayment(nilGrantProject, org1, LedgerType.PAYMENT,
                new BigDecimal(-400000L), "Completion");
        ProjectLedgerEntry nilReclaimDPFCompletion = createMilestonePayment(nilGrantProject, org1, LedgerType.DPF,
                new BigDecimal(-120000L), "Completion");
        ProjectLedgerEntry nilReclaimRCGFCompletion = createMilestonePayment(nilGrantProject, org1, LedgerType.RCGF,
                new BigDecimal(-280000L), "Completion");

        ProjectLedgerEntry paymentReclaimDpfLandAcquired = createMilestonePayment(projectWithMultipleReclaimPayment, org1,
                LedgerType.DPF, new BigDecimal(-100000L), "Land acquired");
        ProjectLedgerEntry nilReclaimDPFLandAcquired = createMilestonePayment(nilGrantProject, org1, LedgerType.DPF,
                new BigDecimal(-100000L), "Land acquired");

        Project oddProject = projectService.findAllByTitle("Odd Grant Value").get(0);
        ProjectLedgerEntry oddProjectPayment = createPayment(oddProject, 2017, 4, "Milestone",
                "Start on site", BigDecimal.valueOf(-50000), LedgerStatus.Cleared);
        oddProjectPayment.setExternalId(3003);
        populateDefaultPaymentData(oddProjectPayment, oddProject);
        oddProjectPayment.setModifiedOn(OffsetDateTime.of(2017, 5, 20, 4, 0, 0,
                0, ZoneOffset.UTC));
        projectLedgerRepository.saveAndFlush(oddProjectPayment);

        List<ProjectLedgerEntry> payments = Arrays.asList(skillsPaymentCleared, skillsPaymentAuthorised,
                skillProcuredSupportPayment1, skillProcuredSupportPayment2,
                skillProcuredSupportPayment3, skillProcuredSupportPayment4, skillProcuredSupportPayment5,
                skillProcuredSupportPayment6, skillProcuredSupportPayment7,
                cleared/*, pendingWithWrongVendorId*/, grantStategic, grantStategicSupplemental,
                grantStategic2, dpfStategic, grant, rcgf, dpf, grant2, grant3, grant4, rcgf2, rcgf3, rcgf4, dpf2, dpf3, dpf4,
                receipt, sent, acknowledged,
                supplierError, received, pending1, pending2, pending3, pending4, pending5, pending6, pendingNoSap, pending2NoSap,
                suplemental, reclaim, reclaimDPF, oddProjectPayment,
                reclaimRCGF, reclaimInConsortium, reclaimDPFInConsortium, reclaimRCGFInConsortium,
                paymentReclaim, paymentReclaimDpf, paymentReclaimRcgf, paymentReclaimCompletion, paymentReclaimDpfCompletion,
                paymentReclaimRcgfCompletion, paymentReclaimDpfLandAcquired, nilReclaim, nilReclaimDPF, nilReclaimRCGF,
                nilReclaimCompletion, nilReclaimDPFCompletion, nilReclaimRCGFCompletion, nilReclaimDPFLandAcquired,
                cancelRcgf, cancelGrant);
        List<ProjectLedgerEntry> declinedPayments = Arrays.asList(m1, m2, m3, m3b, m4);

        for (ProjectLedgerEntry payment : payments) {
            payment.setAuthorisedOn(OffsetDateTime.now().minusDays(1));
            payment.setAuthorisedBy("test.admin");
        }

        grantStategicSupplemental.setAuthorisedOn(OffsetDateTime.of(2013, 12, 12, 12, 12,
                12, 12, ZoneOffset.UTC));

        for (ProjectLedgerEntry payment : declinedPayments) {
            payment.setLedgerStatus(LedgerStatus.Declined);
            payment.setModifiedBy("test.admin");
        }

        projectLedgerRepository.saveAll(payments);
        projectLedgerRepository.saveAll(declinedPayments);

        // save this one first to allow last mod time be before the others.
        createPaymentGroup("Bob Jones", skillsPaymentCleared, skillsPaymentAuthorised);
        createPaymentGroup("Bob Jones", skillProcuredSupportPayment1, skillProcuredSupportPayment2,
                skillProcuredSupportPayment3, skillProcuredSupportPayment4, skillProcuredSupportPayment5,
                skillProcuredSupportPayment6, skillProcuredSupportPayment7);
        createPaymentGroup("Bob Jones", grantStategic, grantStategicSupplemental, grantStategic2, dpfStategic);
        createPaymentGroup("Bob Jones", grant, rcgf, receipt, dpf, grant2, rcgf2, dpf2, grant3, rcgf3, dpf3,
                grant4, rcgf4, dpf4);
        createPaymentGroup("Bob Jones", sent, acknowledged, supplierError, received, cleared);
        createPaymentGroup("Fred Simpson", pending1, pending2, pending3);
        createPaymentGroup("Fred Simpson", pending4);
        createPaymentGroup("Bob Smith", pending5);
        createPaymentGroup("Bob Smith", pending6);
        createPaymentGroup("Paul Jenkins", pendingNoSap, pending2NoSap);
        createPaymentGroup("Data Initialiser", nilReclaim, nilReclaimDPF, nilReclaimRCGF,
                nilReclaimCompletion, nilReclaimDPFCompletion, nilReclaimRCGFCompletion, nilReclaimDPFLandAcquired);
        createPaymentGroup("Data Initialiser", oddProjectPayment);
        createPaymentGroup("Paul Jenkins", cancelGrant, cancelRcgf);

        PaymentGroupEntity group3 = createPaymentGroup("Smith Watkins", m1, m2);
        group3.setDeclineComments("Incorrect amount");
        group3.setDeclineReason(refDataService.getCategoryValue(15));
        paymentGroupRepository.save(group3);

        PaymentGroupEntity group4 = createPaymentGroup("Tony Jackal", m3, m3b, m4);
        group4.setDeclineComments("Incorrect funding source");
        group4.setDeclineReason(refDataService.getCategoryValue(16));
        paymentGroupRepository.save(group4);

        createPaymentGroup(suplemental);
        createPaymentGroup(reclaim, reclaimDPF, reclaimRCGF);
        createPaymentGroup(reclaimInConsortium, reclaimDPFInConsortium, reclaimRCGFInConsortium);
        createPaymentGroup(paymentReclaim, paymentReclaimDpf, paymentReclaimRcgf, paymentReclaimCompletion,
                paymentReclaimDpfCompletion, paymentReclaimDpfCompletion, paymentReclaimDpfCompletion);

        PaymentGroupEntity monetaryGroup = new PaymentGroupEntity();
        monetary = projectService.findAllByTitle("Monetary Project").get(0);
        ProjectLedgerEntry monetaryPayment = createPayment(
                monetary, 2012, 5, "Milestone",
                "Bespoke Manual", BigDecimal.valueOf(-100000.00), LedgerStatus.Authorised);
        monetaryPayment.setAuthorisedOn(OffsetDateTime.of(2013, 1, 1, 12, 12, 12,
                12, ZoneOffset.UTC));
        monetaryPayment.setAuthorisedBy("test.admin");
        populateDefaultPaymentData(monetaryPayment, monetary);
        projectLedgerRepository.save(monetaryPayment);

        monetaryGroup.getLedgerEntries().add(monetaryPayment);
        paymentGroupRepository.save(monetaryGroup);

        monetaryGroup = new PaymentGroupEntity();
        monetary = projectService.findAllByTitle("All Grant Monetary Type Project").get(0);
        monetaryPayment = createPayment(
                monetary, 2012, 5, "Milestone",
                "Bespoke Manual", BigDecimal.valueOf(-100000.00), LedgerStatus.Authorised);
        monetaryPayment.setAuthorisedOn(OffsetDateTime.of(2013, 1, 1, 12, 12, 12,
                12, ZoneOffset.UTC));
        monetaryPayment.setAuthorisedBy("test.admin");
        populateDefaultPaymentData(monetaryPayment, monetary);
        projectLedgerRepository.save(monetaryPayment);
        monetaryGroup.getLedgerEntries().add(monetaryPayment);

        monetaryPayment = createPayment(
                monetary, 2014, 5, "Milestone",
                "Bespoke Manual", BigDecimal.valueOf(-120000L), LedgerStatus.Authorised);
        monetaryPayment.setAuthorisedOn(OffsetDateTime.of(2013, 1, 1, 12, 12, 12,
                12, ZoneOffset.UTC));
        monetaryPayment.setAuthorisedBy("test.admin");
        populateDefaultPaymentData(monetaryPayment, monetary);
        monetaryPayment.setLedgerType(LedgerType.RCGF);
        projectLedgerRepository.save(monetaryPayment);
        monetaryGroup.getLedgerEntries().add(monetaryPayment);

        monetaryPayment = createPayment(
                monetary, 2016, 5, "Milestone",
                "Bespoke Manual", BigDecimal.valueOf(-80000L), LedgerStatus.Authorised);
        monetaryPayment.setAuthorisedOn(OffsetDateTime.of(2013, 1, 1, 12, 12, 12,
                12, ZoneOffset.UTC));
        monetaryPayment.setAuthorisedBy("test.admin");
        populateDefaultPaymentData(monetaryPayment, monetary);
        monetaryPayment.setLedgerType(LedgerType.DPF);
        projectLedgerRepository.save(monetaryPayment);
        monetaryGroup.getLedgerEntries().add(monetaryPayment);

        paymentGroupRepository.save(monetaryGroup);

        // data for "Active project with payments"
        // payments should be negative, and have to be negative for reclaim testing
        ProjectLedgerEntry apwpEntry1 = createPayment(activeProjectWithPayments, 2017, 4, "Milestone",
                "Start on site", BigDecimal.valueOf(-1000.00), LedgerStatus.Authorised);
        populateDefaultPaymentData(apwpEntry1, activeProjectWithPayments);
        apwpEntry1.setAuthorisedOn(OffsetDateTime.now().minusDays(1));
        apwpEntry1.setAuthorisedBy("test.admin");
        projectLedgerRepository.save(apwpEntry1);

        PaymentGroupEntity activeProjectWithPaymentsGroup = new PaymentGroupEntity();
        activeProjectWithPaymentsGroup.getLedgerEntries().add(apwpEntry1);
        paymentGroupRepository.save(activeProjectWithPaymentsGroup);

        // data for "No WBS  project with payments"
        ProjectLedgerEntry nowbsEntry1 = createPayment(noWBSReclaims, 2017, 4, "Milestone",
                "Start on site", BigDecimal.valueOf(-1000.00), LedgerStatus.Authorised);
        populateDefaultPaymentData(nowbsEntry1, noWBSReclaims);
        nowbsEntry1.setAuthorisedOn(OffsetDateTime.now().minusDays(1));
        nowbsEntry1.setAuthorisedBy("test.admin");
        projectLedgerRepository.save(nowbsEntry1);

        PaymentGroupEntity noWBSPaymentGroup = new PaymentGroupEntity();
        noWBSPaymentGroup.getLedgerEntries().add(nowbsEntry1);
        paymentGroupRepository.save(noWBSPaymentGroup);

        ProjectLedgerEntry nowbsReclaim = createPayment(noWBSReclaims, 2017, 5, "Milestone",
                "Reclaimed Start on site", BigDecimal.valueOf(100.00), LedgerStatus.Pending);
        populateDefaultPaymentData(nowbsReclaim, noWBSReclaims);
        nowbsReclaim.setLedgerType(LedgerType.PAYMENT);
        nowbsReclaim.setReclaimOfPaymentId(nowbsEntry1.getId());
        projectLedgerRepository.save(nowbsReclaim);

        PaymentGroupEntity reclaimForNoWBSGroup = new PaymentGroupEntity();
        reclaimForNoWBSGroup.getLedgerEntries().add(nowbsReclaim);
        paymentGroupRepository.save(reclaimForNoWBSGroup);

        // data for "Closed project with payments"
        ProjectLedgerEntry cpwpEntry1 = createPayment(closedProjectWithPayments, 2017, 4, "Milestone",
                "Start on site", BigDecimal.valueOf(-1000.00), LedgerStatus.Authorised);
        populateDefaultPaymentData(cpwpEntry1, closedProjectWithPayments);
        cpwpEntry1.setAuthorisedOn(OffsetDateTime.now().minusDays(1));
        cpwpEntry1.setAuthorisedBy("test.admin");
        projectLedgerRepository.save(cpwpEntry1);

        PaymentGroupEntity closedProjectWithPaymentsGroup = new PaymentGroupEntity();
        closedProjectWithPaymentsGroup.getLedgerEntries().add(cpwpEntry1);
        paymentGroupRepository.save(closedProjectWithPaymentsGroup);

        ProjectLedgerEntry cpwpReclaim = createPayment(closedProjectWithPayments, 2017, 5, "Milestone",
                "Reclaimed Start on site", BigDecimal.valueOf(100.00), LedgerStatus.Pending);
        populateDefaultPaymentData(cpwpReclaim, closedProjectWithPayments);
        cpwpReclaim.setLedgerType(LedgerType.PAYMENT);
        cpwpReclaim.setReclaimOfPaymentId(cpwpEntry1.getId());
        projectLedgerRepository.save(cpwpReclaim);

        PaymentGroupEntity reclaimForClosedProjectWithPaymentsGroup = new PaymentGroupEntity();
        reclaimForClosedProjectWithPaymentsGroup.getLedgerEntries().add(cpwpReclaim);
        paymentGroupRepository.save(reclaimForClosedProjectWithPaymentsGroup);
    }

    private ProjectLedgerEntry createGrantPaymentForProject(Project project) {
        ProjectLedgerEntry payment = createPayment(project, 2017, 4, "Milestone", "Start on site",
                BigDecimal.valueOf(-4000.00), LedgerStatus.Pending);
        payment.setExternalId(3003);
        populateDefaultPaymentData(payment, project);
        return payment;
    }

    private ProjectLedgerEntry addSkillsPaymentForProject(Project project, int academicYear, int period,
                                                          LearningGrantEntryType entryType, int actualYear, int actualMonth,
                                                          String category, String subCategory, BigDecimal amount,
                                                          LedgerStatus ledgerStatus) {
        LearningGrantBlock learningGrantBlock = project.getLearningGrantBlock();

        LearningGrantEntry learningGrantEntry = learningGrantBlock.getLearningGrantEntry(academicYear, period, entryType);

        ProjectLedgerEntry payment = createPayment(project, actualYear, actualMonth, category, subCategory, amount, ledgerStatus);
        payment.setExternalId(learningGrantEntry.getOriginalId());
        populateDefaultPaymentData(payment, project);

        return payment;
    }

    private PaymentGroupEntity createPaymentGroup(ProjectLedgerEntry... entries) {
        return createPaymentGroup(null, entries);
    }

    private PaymentGroupEntity createPaymentGroup(String approvalRequestedBy, ProjectLedgerEntry... entries) {
        PaymentGroupEntity paymentGroup = new PaymentGroupEntity();
        paymentGroup.setApprovalRequestedBy(approvalRequestedBy);
        paymentGroup.getLedgerEntries().addAll(Arrays.asList(entries));
        return paymentGroupRepository.save(paymentGroup);
    }

    private ProjectLedgerEntry createMilestonePayment(Project project, OrganisationEntity org1, LedgerType type,
                                                      BigDecimal amount, String milestoneSummary) {
        ProjectLedgerEntry suppemental = createPayment(project, 2017, 4, "Milestone", milestoneSummary,
                amount, LedgerStatus.Cleared);
        ProjectMilestonesBlock milestonesBlock =
                (ProjectMilestonesBlock) project.getSingleLatestBlockOfType(ProjectBlockType.Milestones);
        for (Milestone milestone : milestonesBlock.getMilestones()) {
            if (milestoneSummary.equals(milestone.getSummary())) {
                suppemental.setExternalId(milestone.getExternalId());
            }
        }
        suppemental.setBlockId(milestonesBlock.getId());
        suppemental.setOrganisationId(org1.getId());
        suppemental.setVendorName(org1.getName());
        suppemental.setLedgerType(type);
        suppemental.setSapVendorId(org1.getDefaultSapVendorId());
        suppemental.setProjectName(project.getTitle());
        suppemental.setProgrammeName(project.getProgramme().getName());
        suppemental.setManagingOrganisation(project.getManagingOrganisation());
        return suppemental;
    }

    private ProjectLedgerEntry createPayment(Project project, int year, int month, String category, String subType,
                                             BigDecimal amount, LedgerStatus status) {
        ProjectLedgerEntry ple = new ProjectLedgerEntry(project.getId(), year, month, category, subType, amount, status);
        ple.setCeCode(DEFAULT_LEDGER_CE_CODE);
        ple.setWbsCode("wbs-test");
        ple.setCompanyName(project.getProgramme().getCompanyName());
        ple.setCreatedBy("testdatainitialiser");
        ple.setModifiedBy("testdatainitialiser");
        return ple;
    }

    private void addSapData() {
        // payment
        SapData entry1 = new SapData();
        entry1.setContent("<data><paymentReference>1100001</paymentReference><PCSProjectNumber>112233</PCSProjectNumber>"
                + "<PCSPhaseNumber>01</PCSPhaseNumber><payeeName>TEST Andrew </payeeName><paymentDate>28/02/2017</paymentDate>"
                + "<paidAmount>111</paidAmount><accountCode>0000461027</accountCode><accountDescription>"
                + "EU Grants</accountDescription><activityCode></activityCode><activityDescription></activityDescription>"
                + "<paymentDescription>Invoice</paymentDescription><costCenterCode>H1550</costCenterCode><orderNumber>3100835690"
                + "</orderNumber></data>");
        entry1.setFileName("a_payment_file.xml");
        entry1.setInterfaceType(SapData.TYPE_PAYMENT);
        entry1.setCreatedOn(OffsetDateTime.parse("2017-10-17T12:01:50.13Z"));
        entry1.setErrorDescription("ignored as date (28/02/2017) before cutoff date");
        entry1.setProcessed(true);
        entry1.setProcessedOn(OffsetDateTime.parse("2017-10-17T12:01:50.156Z"));
        sapDataRepository.save(entry1);

        entry1 = new SapData();
        entry1.setContent("<data><paymentReference>1100001</paymentReference><PCSProjectNumber>112233</PCSProjectNumber>"
                + "<PCSPhaseNumber>01</PCSPhaseNumber><payeeName>TEST Andrew </payeeName><paymentDate>28/02/2017</paymentDate>"
                + "<paidAmount>111</paidAmount><accountCode>0000461027</accountCode><accountDescription>EU Grants"
                + "</accountDescription><activityCode></activityCode><activityDescription></activityDescription>"
                + "<paymentDescription>Invoice</paymentDescription><costCenterCode>H1550</costCenterCode><orderNumber>3100835690"
                + "</orderNumber></data>");
        entry1.setFileName("a_payment_unprocessed_file.xml");
        entry1.setInterfaceType(SapData.TYPE_PAYMENT);
        entry1.setCreatedOn(OffsetDateTime.parse("2017-10-17T12:01:50.13Z"));
        entry1.setErrorDescription("ignored as date (28/02/2017) before cutoff date");
        entry1.setProcessed(false);
        entry1.setProcessedOn(OffsetDateTime.parse("2017-10-17T12:01:50.156Z"));
        sapDataRepository.save(entry1);

        entry1 = new SapData();
        entry1.setContent("<data><paymentReference>99999999</paymentReference><PCSProjectNumber>898989</PCSProjectNumber>"
                + "<PCSPhaseNumber>01</PCSPhaseNumber><payeeName>TEST TEST </payeeName><paymentDate>28/02/2016</paymentDate>"
                + "<paidAmount>111</paidAmount><accountCode>0000461027</accountCode><accountDescription>EU Grants"
                + "</accountDescription><activityCode></activityCode><activityDescription></activityDescription>"
                + "<paymentDescription>Invoice</paymentDescription><costCenterCode>H1550</costCenterCode><orderNumber>3100835690"
                + "</orderNumber></data>");
        entry1.setFileName("deletion_test_file.xml");
        entry1.setInterfaceType(SapData.TYPE_PAYMENT);
        entry1.setCreatedOn(OffsetDateTime.parse("2016-10-17T12:01:50.13Z"));
        entry1.setErrorDescription("ignored as date (28/02/2017) before cutoff date");
        entry1.setProcessed(false);
        entry1.setProcessedOn(OffsetDateTime.parse("2017-10-17T12:01:50.156Z"));
        sapDataRepository.save(entry1);

        // receipt
        SapData entry2 = new SapData();
        entry2.setContent("<data><PCSProjectNumber>00021224</PCSProjectNumber><payerName>Dentons UKMEA LLP</payerName>"
                + "<receiptDate>01/03/2017</receiptDate><receiptReference>1800000675</receiptReference><receiptAmount>-470000.00"
                + "</receiptAmount><costCenterCode>H510C</costCenterCode><accountCode>0000470020</accountCode>"
                + "<accountDescription>Other Joint Funding Contributions</accountDescription><activityCode/>"
                + "<activityDescription/><invoiceDate>01/09/2016</invoiceDate><invoiceNumber>20049738</invoiceNumber></data>");
        entry2.setFileName("a_receipt_file.xml");
        entry2.setInterfaceType(SapData.TYPE_RECEIPT);
        entry2.setCreatedOn(OffsetDateTime.parse("2017-10-03T14:52:30.955Z"));
        entry2.setErrorDescription("Could not find project with PCS ID 00021224");
        entry2.setProcessed(true);
        entry2.setProcessedOn(OffsetDateTime.parse("2018-05-08T10:31:44.154Z"));
        sapDataRepository.save(entry2);

        entry2 = new SapData();
        entry2.setContent("<data><PCSProjectNumber>00021224</PCSProjectNumber><payerName>Dentons UKMEA LLP</payerName>"
                + "<receiptDate>01/03/2017</receiptDate><receiptReference>1800000675</receiptReference><receiptAmount>-470000.00"
                + "</receiptAmount><costCenterCode>H510C</costCenterCode><accountCode>0000470020</accountCode>"
                + "<accountDescription>Other Joint Funding Contributions</accountDescription><activityCode/>"
                + "<activityDescription/><invoiceDate>01/09/2016</invoiceDate><invoiceNumber>20049738</invoiceNumber></data>");
        entry2.setFileName("a_receipt_unprocessed_file_file.xml");
        entry2.setInterfaceType(SapData.TYPE_RECEIPT);
        entry2.setCreatedOn(OffsetDateTime.parse("2017-10-03T14:52:30.955Z"));
        entry2.setErrorDescription("Could not find project with PCS ID 00021224");
        entry2.setProcessed(false);
        entry2.setProcessedOn(OffsetDateTime.parse("2018-05-08T10:31:44.154Z"));
        sapDataRepository.save(entry2);
        // acknowledgment
        SapData entry3 = new SapData();
        entry3.setContent("<data><PCSProjectNumber>00021224</PCSProjectNumber><payerName>Dentons UKMEA LLP</payerName>"
                + "<receiptDate>01/03/2017</receiptDate><receiptReference>1800000675</receiptReference><receiptAmount>-470000.00"
                + "</receiptAmount><costCenterCode>H510C</costCenterCode><accountCode>0000470020</accountCode>"
                + "<accountDescription>Other Joint Funding Contributions</accountDescription><activityCode/>"
                + "<activityDescription/><invoiceDate>01/09/2016</invoiceDate><invoiceNumber>20049738</invoiceNumber></data>");
        entry3.setFileName("an_acknowledgment_file.xml");
        entry3.setInterfaceType(SapData.TYPE_INV_RESP);
        entry3.setCreatedOn(OffsetDateTime.parse("2017-10-04T16:14:39.92Z"));
        entry3.setErrorDescription("No payment matching supplier invoice number P10738-1009");
        entry3.setProcessed(true);
        entry3.setProcessedOn(OffsetDateTime.parse("2017-10-04T16:14:39.92Z"));
        sapDataRepository.save(entry3);

        entry3 = new SapData();
        entry3.setContent("<data><PCSProjectNumber>00021224</PCSProjectNumber><payerName>Dentons UKMEA LLP</payerName>"
                + "<receiptDate>01/03/2017</receiptDate><receiptReference>1800000675</receiptReference><receiptAmount>-470000.00"
                + "</receiptAmount><costCenterCode>H510C</costCenterCode><accountCode>0000470020</accountCode>"
                + "<accountDescription>Other Joint Funding Contributions</accountDescription><activityCode/>"
                + "<activityDescription/><invoiceDate>01/09/2016</invoiceDate><invoiceNumber>20049738</invoiceNumber></data>");
        entry3.setFileName("an_acknowledgment_unprocessed_file_file.xml");
        entry3.setInterfaceType(SapData.TYPE_INV_RESP);
        entry3.setCreatedOn(OffsetDateTime.parse("2017-10-04T16:14:39.92Z"));
        entry3.setErrorDescription("No payment matching supplier invoice number P10738-1009");
        entry3.setProcessed(false);
        entry3.setProcessedOn(OffsetDateTime.parse("2017-10-04T16:14:39.92Z"));
        sapDataRepository.save(entry3);

        SapData entry5 = new SapData();
        entry5.setContent(TEST_INVALID_SAP_DATA_CONTENT);
        entry5.setFileName("xml_not_valid.xml");
        entry5.setInterfaceType(SapData.TYPE_RECEIPT);
        entry5.setCreatedOn(OffsetDateTime.parse("2017-10-04T16:14:39.92Z"));
        entry5.setErrorDescription("Unable to parse XML");
        entry5.setProcessed(false);
        entry5.setProcessedOn(OffsetDateTime.parse("2017-10-04T16:14:39.92Z"));
        sapDataRepository.save(entry5);
    }

}
