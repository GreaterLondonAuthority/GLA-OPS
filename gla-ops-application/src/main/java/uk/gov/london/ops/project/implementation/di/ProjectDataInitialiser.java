/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.project.implementation.di;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.london.ops.framework.calendar.FinancialCalendar;
import uk.gov.london.ops.framework.di.DataInitialiserModule;
import uk.gov.london.ops.framework.environment.Environment;
import uk.gov.london.ops.payment.*;
import uk.gov.london.ops.programme.ProgrammeServiceImpl;
import uk.gov.london.ops.programme.domain.Programme;
import uk.gov.london.ops.project.*;
import uk.gov.london.ops.project.block.*;
import uk.gov.london.ops.project.budget.ProjectBudgetsAttachment;
import uk.gov.london.ops.project.claim.ClaimStatus;
import uk.gov.london.ops.project.grant.*;
import uk.gov.london.ops.project.implementation.mapper.MilestoneMapper;
import uk.gov.london.ops.project.implementation.repository.LockDetailsRepository;
import uk.gov.london.ops.project.implementation.repository.OutputTableEntryRepository;
import uk.gov.london.ops.project.implementation.repository.ProjectRepository;
import uk.gov.london.ops.project.milestone.Milestone;
import uk.gov.london.ops.project.milestone.MilestoneStatus;
import uk.gov.london.ops.project.milestone.ProjectMilestonesBlock;
import uk.gov.london.ops.project.outputs.OutputTableEntry;
import uk.gov.london.ops.project.question.ProjectQuestion;
import uk.gov.london.ops.project.question.ProjectQuestionsBlock;
import uk.gov.london.ops.project.state.ProjectStatus;
import uk.gov.london.ops.project.state.ProjectSubStatus;
import uk.gov.london.ops.project.template.TemplateServiceImpl;
import uk.gov.london.ops.project.template.domain.*;
import uk.gov.london.ops.project.unit.UnitDetailsBlock;
import uk.gov.london.ops.project.unit.UnitDetailsTableEntry;
import uk.gov.london.ops.refdata.*;
import uk.gov.london.ops.user.UserBuilder;
import uk.gov.london.ops.user.UserServiceImpl;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static uk.gov.london.ops.organisation.Organisation.*;
import static uk.gov.london.ops.project.ProjectBuilder.*;
import static uk.gov.london.ops.project.block.ProjectBlockType.*;
import static uk.gov.london.ops.project.milestone.Milestone.COMPLETION_ID;
import static uk.gov.london.ops.project.milestone.Milestone.START_ON_SITE_ID;
import static uk.gov.london.ops.project.state.ProjectSubStatus.*;
import static uk.gov.london.ops.project.template.TemplateBuilder.*;
import static uk.gov.london.ops.refdata.TenureType.LONDON_AFFORDABLE_RENT;
import static uk.gov.london.ops.user.UserBuilder.DATA_INITIALISER_USER;

/**
 * @deprecated - use a feature-aligned data initialiser module instead.
 */
@Transactional
@Component
public class ProjectDataInitialiser implements DataInitialiserModule {

    Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    private OutputTableEntryRepository outputTableEntryRepository;
    @Autowired
    private FinancialCalendar financialCalendar;
    @Autowired
    private RefDataServiceImpl refDataService;
    @Autowired
    private TemplateServiceImpl templateService;
    @Autowired
    private ProjectRepository projectRepository;
    @Autowired
    private ProgrammeServiceImpl programmeService;
    @Autowired
    private ProjectBuilder pb;
    @Autowired
    private UserBuilder userBuilder;
    @Autowired
    private Environment environment;
    @Autowired
    private FinanceService financeService;
    @Autowired
    private OutputConfigurationService outputConfigurationService;
    @Autowired
    private ProjectService projectService;
    @Autowired
    private UserServiceImpl userService;
    @Autowired
    private LockDetailsRepository lockDetailsRepository;
    @Autowired
    MilestoneMapper milestoneMapper;


    private Project submittedProject;
    private Project projectWithGrantToAuthorise;
    private Project projectWithSupplementalPayment;
    private Project secondProjectWithGrantToAuthorise;
    private Project thirdProjectWithGrantToAuthorise;
    private Project fourthProjectWithGrantToAuthorise;
    private Project fifthProjectWithGrantToAuthorise;
    private Project projectToTestPayments;
    private Project projectToTestPaymentRequests;
    private Project strategicProjectToTestPaymentRequests;
    private Project projectToTestDeclinedPayments;
    private Project anotherProjectToTestDeclinedPayments;

    private Programme bucketProgramme;
    private Programme paymentTestProg;

    private Template housingTemplate;
    private Template indicativeStartsTemplate;
    private Template landDisposal;
    private Template allBlocksTemplate;
    private Template autoApprovalTemplateToAddBlockTo;

    public Project getSubmittedProject() {
        return submittedProject;
    }

    public Project getProjectToTestPayments() {
        return projectToTestPayments;
    }

    public Project getProjectToTestPaymentRequests() {
        return projectToTestPaymentRequests;
    }

    public Project getStrategicProjectToTestPaymentRequests() {
        return strategicProjectToTestPaymentRequests;
    }

    public Project getProjectToTestDeclinedPayments() {
        return projectToTestDeclinedPayments;
    }

    public Project getAnotherProjectToTestDeclinedPayments() {
        return anotherProjectToTestDeclinedPayments;
    }

    public Project getProjectWithGrantToAuthorise() {
        return projectWithGrantToAuthorise;
    }

    public Project getSecondProjectWithGrantToAuthorise() {
        return secondProjectWithGrantToAuthorise;
    }

    public Project getThirdProjectWithGrantToAuthorise() {
        return thirdProjectWithGrantToAuthorise;
    }

    public Project getFourthProjectWithGrantToAuthorise() {
        return fourthProjectWithGrantToAuthorise;
    }

    public Project getFifthProjectWithGrantToAuthorise() {
        return fifthProjectWithGrantToAuthorise;
    }

    public Project getProjectWithSupplementalPayment() {
        return projectWithSupplementalPayment;
    }

    @Override
    public String getName() {
        return "Project data initialiser";
    }

    @Override
    public void addProjects() {
        try {
            Programme mainsteamTestProg = programmeService.findByName("Mainstream housing programme test");
            bucketProgramme = programmeService.findByName("Bucket programme");
            Programme closedHousingProg = programmeService.findByName("Test (Closed) housing programme");
            paymentTestProg = programmeService.findByName("Payment Test Programme");
            Template indicativeTemplate = templateService.findByName(INDICATIVE_TEMPLATE_NAME);
            Template clonedIndicativeTemplate = templateService.findByName("Cloned Indicative");
            Template mainstreamTemplate = templateService.findByName("Mainstream housing test template");
            Template mainstreamExeptionsTemplate = templateService.findByName("Affordable Housing Exceptions");
            Template s106TopUpTemplate = templateService.findByName("S106 Top Up");
            Template milestoneStatusTemplate = templateService.findByName(TEST_HOUSING_TEMPLATE_WITH_MILESTONE);
            Template landTemplate = templateService.findByName("Test Land Disposal Template");
            housingTemplate = templateService.findByName(TEST_HOUSING_TEMPLATE_NAME);
            Template fullTemplate = templateService.findByName("Full Template for testing");
            Template multipleBlockTemplate = templateService.findByName("Multiple block test template");
            landDisposal = templateService.findByName("Land Disposal");
            allBlocksTemplate = templateService.findByName("All Blocks Template");
            Template negotiatedRouteTemplate = templateService.findByName("Negotiated Route");
            Template negotiatedRouteLegacyTemplate = templateService.findByName("Negotiated Route - Legacy Shared Ownership");
            Template developerLedTemplate = templateService.findByName(DEVELOPER_LED_ROUTE_TEMPLATE_NAME);
            Template approvedProviderRouteTemplate = templateService.findByName(APPROVED_PROVIDER_ROUTE_TEMPLATE_NAME);
            Template autoApprovalTemplate = templateService.findByName(AUTO_APPROVAL_TEMPLATE_NAME);
            autoApprovalTemplateToAddBlockTo = templateService.findByName(AUTO_APPROVAL_TEMPLATE_ADD_NAME);
            Template autoApprovalMonetarySplitTemplate = templateService.findByName("Auto Approval Monetary Split");
            Template hiddenBlocksTemplate = templateService.findByName(HIDDEN_BLOCKS_TEMPLATE_NAME);
            Template templateWithCustomTenureTypes = templateService.findByName("Custom Market Types");

            pb.createTestProject("Sample Mainstream Project", TEST_ORG_ID_1, mainsteamTestProg, mainstreamTemplate, STATUS_EMPTY);

            pb.createTestProject("Sample Mainstream Exceptions Project", TEST_ORG_ID_1, mainsteamTestProg,
                    mainstreamExeptionsTemplate, STATUS_EMPTY);
            pb.createTestProject("Sample Mainstream S106 Project", TEST_ORG_ID_1, mainsteamTestProg, s106TopUpTemplate,
                    STATUS_EMPTY);

            pb.createTestProject("Test land disposal project", GLA_HNL_ORG_ID, bucketProgramme, landTemplate, STATUS_EMPTY);
            pb.createTestProject("Another Mainstream Project", GLA_HNL_ORG_ID, bucketProgramme, housingTemplate, STATUS_EMPTY);
            pb.createTestProject("Empty Test Project", TEST_ORG_ID_1, bucketProgramme, housingTemplate, STATUS_EMPTY);
            pb.createTestProject("Editable Test Project", TEST_ORG_ID_1, bucketProgramme, housingTemplate, STATUS_EMPTY);
            pb.createTestProject("Test Project with everything", TEST_ORG_ID_1, bucketProgramme, fullTemplate, STATUS_EMPTY);
            pb.createTestProject("Exceptions Project", GLA_HNL_ORG_ID, bucketProgramme, mainstreamExeptionsTemplate,
                    STATUS_EMPTY);
            pb.createTestProject("Multiple Questions Project", GLA_HNL_ORG_ID, bucketProgramme, multipleBlockTemplate,
                    STATUS_EMPTY);
            createTestLandProject("Test Land Project");
            // TODO remove 2 projects bellow once cloning of ProjectBudgets & Receipts block works as expected and use clone of Test Land Project instead
            Project testLandProject = createTestLandProject("Land Project Save to Active");
            ProjectDetailsBlock block = (ProjectDetailsBlock) testLandProject
                    .getSingleLatestBlockOfType(ProjectBlockType.Details);
            block.setLegacyProjectCode(500022);
            projectRepository.save(testLandProject);

            createTestLandProject("Land Project Edit Active Block");
            pb.createPopulatedTestProject("Project To Move", bucketProgramme, negotiatedRouteTemplate, GLA_HNL_ORG_ID,
                    STATUS_COMPLETE);

            pb.createTestProject("Housing with Milestone Status", GLA_HNL_ORG_ID, bucketProgramme, milestoneStatusTemplate,
                    STATUS_EMPTY);

            pb.createTestProject("All Block Project", GLA_HNL_ORG_ID, bucketProgramme, allBlocksTemplate, STATUS_EMPTY);

            Project lockProject = pb
                    .createTestProject("Project with edit lock on Details", GLA_HNL_ORG_ID, bucketProgramme, housingTemplate,
                            STATUS_EMPTY);

            LockDetails lock1 = new LockDetails(userService.get("test.admin@gla.com"), 60);
            lock1.setBlock(lockProject.getSingleBlockByType(ProjectBlockType.Details));
            lockDetailsRepository.save(lock1);

            lockProject.getSingleBlockByType(ProjectBlockType.Details).setLockDetails(lock1);

            projectRepository.save(lockProject);

            Project aboutToExpireProject = pb
                    .createTestProject("Project with about to expire lock on Milestones", GLA_HNL_ORG_ID, bucketProgramme,
                            housingTemplate, STATUS_EMPTY);

            LockDetails lock2 = new LockDetails(userService.get("test.admin@gla.com"), 5);
            lock2.setBlock(aboutToExpireProject.getSingleBlockByType(Milestones));
            lockDetailsRepository.save(lock2);

            aboutToExpireProject.getSingleBlockByType(Milestones).setLockDetails(lock2);

            projectRepository.save(aboutToExpireProject);

            userBuilder.withLoggedInUser(DATA_INITIALISER_USER);
            pb.createPopulatedTestProject("Ready to submit", bucketProgramme, housingTemplate, TEST_ORG_ID_1, STATUS_COMPLETE);
            pb.createPopulatedTestProject("Ready for submit (Closed Programme)", closedHousingProg, housingTemplate,
                    TEST_ORG_ID_1, STATUS_COMPLETE);
            pb.createPopulatedTestProject("Already Submitted (Closed Programme)", closedHousingProg, housingTemplate,
                    TEST_ORG_ID_1, STATUS_SUBMITTED);
            submittedProject = pb.createPopulatedTestProject("Submitted Project", bucketProgramme, housingTemplate, TEST_ORG_ID_1,
                    STATUS_SUBMITTED);

            Project recommendedProject = pb
                    .createPopulatedTestProject("Recommended Project", bucketProgramme, housingTemplate, TEST_ORG_ID_1,
                            STATUS_SUBMITTED);
            recommendedProject.setStatus(ProjectStatus.Assess);
            recommendedProject.setRecommendation(Project.Recommendation.RecommendApproval);
            projectRepository.save(recommendedProject);

            Project assessedProjectStatus = pb
                    .createPopulatedTestProject("Assessed Housing with Status", bucketProgramme, milestoneStatusTemplate,
                            TEST_ORG_ID_1, STATUS_ASSESS);
            assessedProjectStatus.setStatus(ProjectStatus.Assess);
            projectRepository.save(assessedProjectStatus);

            pb.createPopulatedTestProject("Ready For Assessing 1", bucketProgramme, housingTemplate, TEST_ORG_ID_1,
                    STATUS_SUBMITTED);
            pb.createPopulatedTestProject("Ready For Assessing 2", bucketProgramme, housingTemplate, TEST_ORG_ID_1,
                    STATUS_SUBMITTED);
            Project assessedProject = pb
                    .createPopulatedTestProject("Assessed Project", bucketProgramme, housingTemplate, TEST_ORG_ID_1,
                            STATUS_ASSESS);
            assessedProject.setStatus(ProjectStatus.Assess);
            projectRepository.save(assessedProject);

            Project recApprove = pb
                    .createPopulatedTestProject("Recommend for Approve to be approved", bucketProgramme, housingTemplate,
                            TEST_ORG_ID_1, STATUS_ASSESS);
            Project recReject = pb
                    .createPopulatedTestProject("Recommend for Reject to be approved", bucketProgramme, housingTemplate,
                            TEST_ORG_ID_1, STATUS_ASSESS);
            recApprove.setRecommendation(Project.Recommendation.RecommendApproval);
            recReject.setRecommendation(Project.Recommendation.RecommendRejection);
            recApprove.setStatus(ProjectStatus.Assess);
            recReject.setStatus(ProjectStatus.Assess);
            projectRepository.save(recApprove);
            projectRepository.save(recReject);

            Project readyForReturnProject = pb
                    .createPopulatedTestProject("Ready for return", bucketProgramme, housingTemplate, TEST_ORG_ID_1,
                            STATUS_ASSESS);
            readyForReturnProject.setStatus(ProjectStatus.Assess);
            projectRepository.save(readyForReturnProject);

            Project readyForReturnClosedProject = pb
                    .createPopulatedTestProject("Ready to return (Closed Programme)", closedHousingProg, housingTemplate,
                            TEST_ORG_ID_1, STATUS_ASSESS);
            readyForReturnClosedProject.setStatus(ProjectStatus.Assess);
            projectRepository.save(readyForReturnClosedProject);

            Project assessedReadyForReject = pb
                    .createPopulatedTestProject("Assessed ready for reject", bucketProgramme, housingTemplate, TEST_ORG_ID_1,
                            STATUS_ASSESS);
            assessedReadyForReject.setStatus(ProjectStatus.Assess);
            projectRepository.save(assessedReadyForReject);

            Project returnedReadyForReject = pb
                    .createPopulatedTestProject("Returned ready for reject", bucketProgramme, housingTemplate, TEST_ORG_ID_1,
                            STATUS_ASSESS);
            returnedReadyForReject.setStatus(ProjectStatus.Returned);
            projectRepository.save(returnedReadyForReject);

            Project returnedProject = pb
                    .createPopulatedTestProject("Returned Project", bucketProgramme, housingTemplate, TEST_ORG_ID_1,
                            STATUS_RETURNED);
            returnedProject.setStatus(ProjectStatus.Returned);
            projectRepository.save(returnedProject);

            Project activeProject = pb
                    .createPopulatedTestProject("Active Project", bucketProgramme, allBlocksTemplate, TEST_ORG_ID_1,
                            STATUS_ACTIVE);
            pb.approveProject(activeProject);
            projectRepository.save(activeProject);

            activeProject = pb.createPopulatedTestProject("DO NOT EDIT test active state", bucketProgramme, allBlocksTemplate,
                    TEST_ORG_ID_1, STATUS_ACTIVE);
            pb.approveProject(activeProject);
            projectRepository.save(activeProject);

            Project activeForUnapproved = pb
                    .createPopulatedTestProject("Ready for unapproved version", bucketProgramme, allBlocksTemplate, TEST_ORG_ID_1,
                            STATUS_ACTIVE);
            pb.approveProject(activeForUnapproved);
            projectRepository.save(activeForUnapproved);

            Project negotiatedActive = pb
                    .createPopulatedTestProject("Negotiated Grant Active", bucketProgramme, negotiatedRouteTemplate,
                            TEST_ORG_ID_1, STATUS_ACTIVE);

            ProcessingRoute processingRoute = ((MilestonesTemplateBlock) negotiatedActive.getTemplate()
                    .getSingleBlockByType(Milestones))
                    .getProcessingRoutes().stream().filter(pr -> pr.getName().equalsIgnoreCase("Lease & Repair")).findFirst()
                    .get();
            ProjectMilestonesBlock milestonesBlock = negotiatedActive.getMilestonesBlock();
            milestonesBlock.setProcessingRouteId(processingRoute.getId());
            milestonesBlock.getMilestones().clear();
            milestonesBlock.getMilestones()
                    .addAll(milestoneMapper.toProjectMilestones(processingRoute.getMilestones(), negotiatedActive.getTemplate()));
            milestonesBlock.setLastModified(OffsetDateTime.now());
            milestonesBlock.setModifiedBy("testapproved@gla.com");

            for (Milestone milestone : milestonesBlock.getMilestones()) {
                milestone.setMilestoneDate(LocalDate.of(2018, 12, 12));
                milestone.setMilestoneStatus(MilestoneStatus.ACTUAL);
            }

            pb.approveProject(negotiatedActive);
            UnitDetailsBlock ngaUnitDetailsBlock = (UnitDetailsBlock) negotiatedActive.getSingleLatestBlockOfType(UnitDetails);
            ngaUnitDetailsBlock.setLastModified(environment.now());
            ngaUnitDetailsBlock.getTableEntries()
                    .add(createUnitDetailsTableEntry(negotiatedActive, ngaUnitDetailsBlock, refDataService.getMarketType(2),
                            300));
            ngaUnitDetailsBlock.setNewBuildUnits(300);
            ngaUnitDetailsBlock.setType1Units(300);
            ngaUnitDetailsBlock.setNbWheelchairUnits(300);
            ngaUnitDetailsBlock.setGrossInternalArea(100);
            projectRepository.save(negotiatedActive);

            Project negotiatedLegacyActive = pb
                    .createPopulatedTestProject("Negotiated Grant Active - Legacy Shared Ownership", bucketProgramme,
                            negotiatedRouteLegacyTemplate, TEST_ORG_ID_1, STATUS_ACTIVE);
            pb.approveProject(negotiatedLegacyActive);
            UnitDetailsBlock nglaUnitDetailsBlock = (UnitDetailsBlock) negotiatedLegacyActive
                    .getSingleLatestBlockOfType(UnitDetails);
            nglaUnitDetailsBlock.setLastModified(environment.now());
            nglaUnitDetailsBlock.getTableEntries().add(createUnitDetailsTableEntry(negotiatedLegacyActive, nglaUnitDetailsBlock,
                    refDataService.getMarketType(2), 300));
            nglaUnitDetailsBlock.setNewBuildUnits(300);
            nglaUnitDetailsBlock.setType1Units(300);
            nglaUnitDetailsBlock.setNbWheelchairUnits(300);
            nglaUnitDetailsBlock.setGrossInternalArea(100);
            projectRepository.save(negotiatedLegacyActive);

            Project devActive = pb
                    .createPopulatedTestProject("DeveloperLed Active", bucketProgramme, developerLedTemplate, TEST_ORG_ID_1,
                            STATUS_ACTIVE);
            pb.approveProject(devActive);
            UnitDetailsBlock unitDetailsBlock = (UnitDetailsBlock) devActive.getSingleLatestBlockOfType(UnitDetails);
            UnitDetailsTableEntry unitDetailsTableEntry = createUnitDetailsTableEntry(devActive, unitDetailsBlock,
                    refDataService.getMarketType(1));

            unitDetailsBlock.getTableEntries().add(unitDetailsTableEntry);
            projectRepository.save(devActive);

            Project devActiveWizard = pb
                    .createPopulatedTestProject("DeveloperLed Active for wizard", bucketProgramme, developerLedTemplate,
                            TEST_ORG_ID_1, STATUS_ACTIVE);
            pb.approveProject(devActiveWizard);
            UnitDetailsBlock udBlock = (UnitDetailsBlock) devActiveWizard.getSingleLatestBlockOfType(UnitDetails);
            UnitDetailsTableEntry udTableEntry = createUnitDetailsTableEntry(devActiveWizard, unitDetailsBlock,
                    refDataService.getMarketType(1));

            udBlock.getTableEntries().add(udTableEntry);
            projectRepository.save(devActiveWizard);

            Project approvedActive = pb
                    .createPopulatedTestProject("Calculate Grant Active", bucketProgramme, approvedProviderRouteTemplate,
                            TEST_ORG_ID_1, STATUS_ACTIVE);
            pb.initialiseCalcGrantTenure(approvedActive);
            pb.approveProject(approvedActive);
            unitDetailsBlock = (UnitDetailsBlock) approvedActive.getSingleLatestBlockOfType(UnitDetails);
            unitDetailsTableEntry = createUnitDetailsTableEntry(approvedActive, unitDetailsBlock,
                    refDataService.getMarketType(2));
            unitDetailsBlock.getTableEntries().add(unitDetailsTableEntry);
            unitDetailsBlock.setRefurbishedUnits(40);
            unitDetailsBlock.setNewBuildUnits(60);
            projectRepository.save(approvedActive);

            projectToTestPaymentRequests = pb
                    .createPopulatedTestProject("Payment Project", paymentTestProg, housingTemplate, TEST_ORG_ID_1,
                            STATUS_ACTIVE);
            ProjectMilestonesBlock milestones = (ProjectMilestonesBlock) projectToTestPaymentRequests
                    .getSingleLatestBlockOfType(ProjectBlockType.Milestones);

            for (Milestone milestone : milestones.getMilestones()) {
                if (milestone.getExternalId().equals(3003)) {
                    milestone.setClaimStatus(ClaimStatus.Approved);
                }
            }
            milestones.setBlockStatus(ProjectBlockStatus.APPROVED);
            milestones.setApprovalTime(OffsetDateTime.now());

            GrantSourceBlock grantSourceBlock = (GrantSourceBlock) projectToTestPaymentRequests
                    .getSingleLatestBlockOfType(GrantSource);
            grantSourceBlock.setDisposalProceedsFundValue(11111L);
            grantSourceBlock.setRecycledCapitalGrantFundValue(22222L);
            grantSourceBlock.setGrantValue(33333L);
            pb.initialiseCalcGrantTenure(projectToTestPaymentRequests);
            CalculateGrantBlock calcGrant = (CalculateGrantBlock) projectToTestPaymentRequests
                    .getSingleLatestBlockOfType(CalculateGrant);
            calcGrant.startOnSiteMilestoneApproved();

            pb.approveProject(projectToTestPaymentRequests);
            projectRepository.save(projectToTestPaymentRequests);

            Project completionProjectToTestPaymentRequests = pb
                    .createPopulatedTestProject("Payment Project with Completion Milestone", bucketProgramme, housingTemplate,
                            TEST_ORG_ID_1, STATUS_ACTIVE);
            milestones = (ProjectMilestonesBlock) completionProjectToTestPaymentRequests
                    .getSingleLatestBlockOfType(ProjectBlockType.Milestones);

            for (Milestone milestone : milestones.getMilestones()) {
                if (milestone.getExternalId().equals(3003) || milestone.getExternalId().equals(3004)) {
                    milestone.setClaimStatus(ClaimStatus.Approved);
                }
            }
            milestones.setBlockStatus(ProjectBlockStatus.APPROVED);
            milestones.setApprovalTime(OffsetDateTime.now());

            grantSourceBlock = (GrantSourceBlock) completionProjectToTestPaymentRequests.getSingleLatestBlockOfType(GrantSource);
            grantSourceBlock.setDisposalProceedsFundValue(11111L);
            grantSourceBlock.setRecycledCapitalGrantFundValue(22222L);
            grantSourceBlock.setGrantValue(33333L);
            pb.initialiseCalcGrantTenure(completionProjectToTestPaymentRequests);
            calcGrant = (CalculateGrantBlock) completionProjectToTestPaymentRequests.getSingleLatestBlockOfType(CalculateGrant);
            calcGrant.startOnSiteMilestoneApproved();

            pb.approveProject(completionProjectToTestPaymentRequests);

            calcGrant = (CalculateGrantBlock) projectService.getBlockAndLock(completionProjectToTestPaymentRequests,
                    completionProjectToTestPaymentRequests.getSingleLatestBlockOfType(ProjectBlockType.CalculateGrant), true);
            Set<ProjectTenureDetails> projectTenureDetailsEntries = calcGrant.getTenureTypeAndUnitsEntries();
            for (ProjectTenureDetails projectTenureDetailsEntry : projectTenureDetailsEntries) {
                projectTenureDetailsEntry.setTotalUnits(projectTenureDetailsEntry.getTotalUnits() + 5);
            }
            calcGrant.completionMilestoneApproved();

            calcGrant.setApprovalTime(OffsetDateTime.now().plus(10, ChronoUnit.SECONDS));
            calcGrant.setApproverUsername("test.admin@gla.com");
            calcGrant.setBlockStatus(ProjectBlockStatus.LAST_APPROVED);
            calcGrant.setLockDetails(null);

            projectRepository.save(completionProjectToTestPaymentRequests);
            lockDetailsRepository.deleteAllByProjectId(completionProjectToTestPaymentRequests.getId());

            strategicProjectToTestPaymentRequests = createActivePaymentProject();

            createPaymentProjectToAsses();

            Project grantPayment = pb
                    .createPopulatedTestProject("Grant Payment Project To Approve", paymentTestProg, housingTemplate,
                            TEST_ORG_ID_1, STATUS_SUBMITTED);
            grantSourceBlock = (GrantSourceBlock) grantPayment.getSingleLatestBlockOfType(GrantSource);
            grantSourceBlock.setDisposalProceedsFundValue(400000L);
            grantSourceBlock.setRecycledCapitalGrantFundValue(500000L);
            grantSourceBlock.setGrantValue(60000L);
            pb.initialiseCalcGrantTenure(grantPayment);
            grantPayment.setStatus(ProjectStatus.Submitted);
            projectRepository.save(grantPayment);

            pb.createPopulatedTestProject("Cloned Indicative Project", bucketProgramme, clonedIndicativeTemplate, TEST_ORG_ID_1,
                    STATUS_ACTIVE);

            pb.createPopulatedTestProject("Indicative Project", bucketProgramme, indicativeTemplate, TEST_ORG_ID_1,
                    STATUS_ACTIVE);
            Project indicativePaymentProject = pb
                    .createPopulatedTestProject("Indicative Payment Project", paymentTestProg, indicativeTemplate, TEST_ORG_ID_1,
                            STATUS_ACTIVE);
            IndicativeGrantBlock indicativeGrantBlock = (IndicativeGrantBlock) indicativePaymentProject
                    .getSingleLatestBlockOfType(ProjectBlockType.IndicativeGrant);
            IndicativeTenureValue indicativeTenureValue = indicativeGrantBlock.getTenureTypeAndUnitsEntriesSorted().get(0)
                    .getIndicativeTenureValuesSorted().get(0);
            indicativeTenureValue.setUnits(100);
            indicativeTenureValue.setYear(2017);
            pb.approveProject(indicativePaymentProject);
            projectRepository.save(indicativePaymentProject);

            indicativePaymentProject = pb
                    .createPopulatedTestProject("Indicative Payment Project To Approve", paymentTestProg, indicativeTemplate,
                            TEST_ORG_ID_1, STATUS_ACTIVE);
            indicativeGrantBlock = (IndicativeGrantBlock) indicativePaymentProject
                    .getSingleLatestBlockOfType(ProjectBlockType.IndicativeGrant);
            indicativeTenureValue = indicativeGrantBlock.getTenureTypeAndUnitsEntriesSorted().get(0)
                    .getIndicativeTenureValuesSorted().get(0);
            indicativeTenureValue.setUnits(10);
            indicativeTenureValue.setYear(2017);
            indicativePaymentProject.setStatus(ProjectStatus.Submitted);
            projectRepository.save(indicativePaymentProject);

            //'Ready for unapproved version' does not have milestone routes
            Project milestonesReadyForUnapproved = pb
                    .createPopulatedTestProject("Ready for unapproved milestones version", bucketProgramme,
                            negotiatedRouteTemplate, TEST_ORG_ID_1, STATUS_ACTIVE);
            pb.approveProject(milestonesReadyForUnapproved);
            grantSourceBlock = ((GrantSourceBlock) milestonesReadyForUnapproved
                    .getSingleBlockByType(ProjectBlockType.GrantSource));
            grantSourceBlock.setZeroGrantRequested(false);
            grantSourceBlock.setRecycledCapitalGrantFundValue(1200000L);
            projectRepository.save(milestonesReadyForUnapproved);

            createUnapprovedProject("Active: Unapproved Project");

            projectToTestPayments = pb
                    .createPopulatedTestProject("Project to test Payments", bucketProgramme, allBlocksTemplate, TEST_ORG_ID_2,
                            STATUS_ACTIVE);
            projectToTestPayments.getMilestonesBlock().getClaims().clear();
            pb.approveProject(projectToTestPayments);
            cloneBlock(projectToTestPayments, ProjectBlockType.Details);
            cloneBlock(projectToTestPayments, Milestones);
            cloneBlock(projectToTestPayments, ProjectBlockType.CalculateGrant);
            cloneBlock(projectToTestPayments, ProjectBlockType.DeveloperLedGrant);
            cloneBlock(projectToTestPayments, ProjectBlockType.NegotiatedGrant);
            cloneBlock(projectToTestPayments, ProjectBlockType.IndicativeGrant);
            cloneBlock(projectToTestPayments, ProjectBlockType.GrantSource);
            cloneBlock(projectToTestPayments, ProjectBlockType.DesignStandards);
            cloneBlock(projectToTestPayments, ProjectBlockType.Questions);
            projectToTestPayments.getGrantSourceBlock().setZeroGrantRequested(false);
            projectToTestPayments.getGrantSourceBlock().setGrantValue(9999L);
            projectToTestPayments.setSubStatus(PaymentAuthorisationPending);
            projectRepository.save(projectToTestPayments);

            Project paymentTester = pb
                    .createPopulatedTestProject("Payment Defect Test", bucketProgramme, developerLedTemplate, TEST_ORG_ID_1,
                            STATUS_ACTIVE);
            paymentTester.getGrantSourceBlock().setZeroGrantRequested(false);
            paymentTester.getGrantSourceBlock().setGrantValue(28000L);
            pb.approveProject(paymentTester);
            ngaUnitDetailsBlock = (UnitDetailsBlock) paymentTester.getSingleLatestBlockOfType(UnitDetails);
            ngaUnitDetailsBlock.setLastModified(environment.now());
            ngaUnitDetailsBlock.getTableEntries()
                    .add(createUnitDetailsTableEntry(paymentTester, ngaUnitDetailsBlock, refDataService.getMarketType(2), 32));
            ngaUnitDetailsBlock.setNewBuildUnits(32);
            ngaUnitDetailsBlock.setType1Units(32);
            ngaUnitDetailsBlock.setNbWheelchairUnits(2);
            ngaUnitDetailsBlock.setGrossInternalArea(100);
            cloneBlock(paymentTester, Milestones);

            milestones = (ProjectMilestonesBlock) paymentTester.getSingleLatestBlockOfType(Milestones);
            milestones.getMilestoneByExternalId(3003).setClaimStatus(ClaimStatus.Claimed);

            cloneBlock(paymentTester, ProjectBlockType.GrantSource);
            GrantSourceBlock grantSource = (GrantSourceBlock) paymentTester.getSingleLatestBlockOfType(GrantSource);
            grantSource.setGrantValue(0L);
            grantSource.setRecycledCapitalGrantFundValue(28000L);

            projectRepository.save(paymentTester);

            projectToTestDeclinedPayments = pb
                    .createPopulatedTestProject("Project to test declined Payments", bucketProgramme, allBlocksTemplate,
                            TEST_ORG_ID_2, STATUS_ACTIVE);
            pb.approveProject(projectToTestDeclinedPayments);
            cloneBlock(projectToTestDeclinedPayments, Milestones);
            projectToTestPayments.getGrantSourceBlock().setGrantValue(9999L);
            projectRepository.save(projectToTestPayments);

            anotherProjectToTestDeclinedPayments = createPopulatedTestProject("Another project to test declined Payments",
                    bucketProgramme, allBlocksTemplate, TEST_ORG_ID_1, STATUS_ACTIVE);
            pb.approveProject(anotherProjectToTestDeclinedPayments);
            cloneBlock(anotherProjectToTestDeclinedPayments, Milestones);
            anotherProjectToTestDeclinedPayments.getGrantSourceBlock().setGrantValue(9999L);
            projectRepository.save(anotherProjectToTestDeclinedPayments);

            projectWithGrantToAuthorise = createProjectWithGrant("Project with Grant");
            secondProjectWithGrantToAuthorise = createProjectWithGrant("Second Project with Grant");
            thirdProjectWithGrantToAuthorise = createProjectWithGrant("Third Project with Grant");
            fourthProjectWithGrantToAuthorise = createProjectWithGrant("Fourth Project with Grant");
            fifthProjectWithGrantToAuthorise = createProjectWithGrant("Fifth Project with Grant");

            projectWithSupplementalPayment = createPopulatedTestProject("Project needing Supplemental Payment", bucketProgramme,
                    housingTemplate, TEST_ORG_ID_1, STATUS_ACTIVE);
            pb.generateProjectApprovalHistory(projectWithSupplementalPayment);
            milestonesBlock = (ProjectMilestonesBlock) projectWithSupplementalPayment.getSingleLatestBlockOfType(Milestones);
            projectWithSupplementalPayment.getGrantSourceBlock().setZeroGrantRequested(false);
            projectWithSupplementalPayment.getGrantSourceBlock().setGrantValue(500000L);
            for (Milestone milestone : milestonesBlock.getMilestones()) {
                if ("Start on site".equals(milestone.getSummary())) {
                    milestone.setClaimedGrant(250000L);
                    milestone.setClaimStatus(ClaimStatus.Approved);
                }
            }
            pb.approveProject(projectWithSupplementalPayment);
            cloneBlock(projectWithSupplementalPayment, GrantSource);
            GrantSourceBlock unappGrantSource = (GrantSourceBlock) projectWithSupplementalPayment
                    .getSingleLatestBlockOfType(GrantSource);
            unappGrantSource.setGrantValue(750001L);
            projectWithSupplementalPayment.setSubStatus(ApprovalRequested);
            projectRepository.save(projectWithSupplementalPayment);

            Project approvalRequestedProject = createPopulatedTestProject("Active: ApprovalRequested Project", bucketProgramme,
                    negotiatedRouteTemplate, TEST_ORG_ID_1, STATUS_ACTIVE);
            pb.approveProject(approvalRequestedProject);
            GrantSourceBlock grantSourceBlock2 = ((GrantSourceBlock) approvalRequestedProject
                    .getSingleBlockByType(ProjectBlockType.GrantSource));
            grantSourceBlock2.setZeroGrantRequested(false);
            grantSourceBlock2.setRecycledCapitalGrantFundValue(1200000L);
            cloneBlock(approvalRequestedProject, Milestones);
            cloneBlock(approvalRequestedProject, ProjectBlockType.NegotiatedGrant);
            approvalRequestedProject.setSubStatus(ApprovalRequested);
            projectRepository.save(approvalRequestedProject);

            Project approvalRequestedIncompleteProject = createPopulatedTestProject(
                    "Active: ApprovalRequested Incomplete Project", bucketProgramme, allBlocksTemplate, TEST_ORG_ID_1,
                    STATUS_ACTIVE);
            pb.approveProject(approvalRequestedIncompleteProject);
            cloneBlock(approvalRequestedIncompleteProject, ProjectBlockType.Details);
            cloneBlock(approvalRequestedIncompleteProject, Milestones);
            cloneBlock(approvalRequestedIncompleteProject, ProjectBlockType.CalculateGrant);
            cloneBlock(approvalRequestedIncompleteProject, ProjectBlockType.DeveloperLedGrant);
            cloneBlock(approvalRequestedIncompleteProject, ProjectBlockType.NegotiatedGrant);
            cloneBlock(approvalRequestedIncompleteProject, ProjectBlockType.IndicativeGrant);
            cloneBlock(approvalRequestedIncompleteProject, ProjectBlockType.GrantSource);
            cloneBlock(approvalRequestedIncompleteProject, ProjectBlockType.Questions);
            approvalRequestedIncompleteProject.setSubStatus(ApprovalRequested);
            projectRepository.save(approvalRequestedIncompleteProject);

            Project readyForRequestApproval = createPopulatedTestProject("Ready for request approval", bucketProgramme,
                    housingTemplate, TEST_ORG_ID_1, STATUS_ACTIVE);
            pb.approveProject(readyForRequestApproval);
            cloneBlock(readyForRequestApproval, Milestones);
            projectRepository.save(readyForRequestApproval);

            Project testingOddGrantSource = createPopulatedTestProject("Odd Grant Value", bucketProgramme, housingTemplate,
                    TEST_ORG_ID_1, STATUS_ACTIVE);
            testingOddGrantSource.getGrantSourceBlock().setZeroGrantRequested(false);
            testingOddGrantSource.getGrantSourceBlock().setGrantValue(100001L);
            testingOddGrantSource.getMilestonesBlock().getMilestones().forEach(m -> m.setClaimStatus(ClaimStatus.Approved));
            Milestone milestone = testingOddGrantSource.getMilestonesBlock().getMilestones().stream()
                    .filter(m -> m.getExternalId().equals(START_ON_SITE_ID)).findFirst().get();
            milestone.setClaimedGrant(50000L);
            milestone.setClaimStatus(ClaimStatus.Approved);
            pb.approveProject(testingOddGrantSource);
            cloneBlock(testingOddGrantSource, Milestones);
            milestone = testingOddGrantSource.getMilestonesBlock().getMilestones().stream()
                    .filter(m -> m.getExternalId().equals(COMPLETION_ID)).findFirst().get();
            milestone.setClaimStatus(ClaimStatus.Claimed);

            ProjectHistoryEntity history = new ProjectHistoryEntity(ProjectTransition.ApprovalRequested);
            history.setCreatedBy("testapproved@gla.com");
            testingOddGrantSource.getHistory().add(history);
            testingOddGrantSource.setSubStatus(ApprovalRequested);

            projectRepository.save(testingOddGrantSource);

            Project readyForBlockDelete = createPopulatedTestProject("Ready for block delete", bucketProgramme,
                    negotiatedRouteTemplate, TEST_ORG_ID_1, STATUS_ACTIVE);
            pb.approveProject(readyForBlockDelete);
            cloneBlock(readyForBlockDelete, Milestones);
            projectRepository.save(readyForBlockDelete);

            Project closedProject = createPopulatedTestProject("Closed Project", bucketProgramme, housingTemplate, TEST_ORG_ID_1,
                    STATUS_ClOSED);
            closedProject.setStatus(ProjectStatus.Closed);
            projectRepository.save(closedProject);

            Project zeroEligibleUnits = createPopulatedTestProject("Zero Eligible Units", bucketProgramme,
                    negotiatedRouteTemplate, TEST_ORG_ID_1, STATUS_ACTIVE);
            generateNegotiatedRouteProject(zeroEligibleUnits);
            projectRepository.save(zeroEligibleUnits);


            /*
            off the shelf project processing route project
            */
            Project offTheShelf = createPopulatedTestProject("Off the Shelf Project", mainsteamTestProg,
                    approvedProviderRouteTemplate, TEST_ORG_ID_1, STATUS_ACTIVE);
            MilestonesTemplateBlock milestoneTemplateBlock = (MilestonesTemplateBlock) offTheShelf.getTemplate()
                    .getSingleBlockByType(ProjectBlockType.Milestones);
            Set<ProcessingRoute> processingRoutes = milestoneTemplateBlock.getProcessingRoutes();
            ProjectMilestonesBlock milestoneBlock = offTheShelf.getMilestonesBlock();
            milestoneBlock.getMilestones().clear();
            for (ProcessingRoute p : processingRoutes) {
                if (p.getName().equals("Off the Shelf")) {
                    milestoneBlock.setProcessingRouteId(p.getId());
                    milestoneBlock.getMilestones()
                            .addAll(milestoneMapper.toProjectMilestones(p.getMilestones(), offTheShelf.getTemplate()));
                }
            }
            for (Milestone m : milestoneBlock.getMilestones()) {
                m.setMilestoneDate(LocalDate.of(2017, 5, 13));
                m.setMilestoneStatus(MilestoneStatus.ACTUAL);
            }
            pb.approveProject(offTheShelf);
            projectRepository.save(offTheShelf);

            createPopulatedTestProject("Auto Approval Project", bucketProgramme, autoApprovalTemplate, TEST_ORG_ID_1,
                    STATUS_COMPLETE);
            pb.approveProject(
                    createPopulatedTestProject("Active Auto Approval", bucketProgramme, autoApprovalTemplate, TEST_ORG_ID_1,
                            STATUS_ACTIVE));

            pb.approveProject(createPopulatedTestProject("Auto Approval Monetary Project", bucketProgramme,
                    autoApprovalMonetarySplitTemplate, TEST_ORG_ID_1, STATUS_ACTIVE));

            Project closedActiveProject = createPopulatedTestProject("Closed Active Project", bucketProgramme,
                    autoApprovalTemplate, TEST_ORG_ID_1, STATUS_ACTIVE);
            pb.approveProject(closedActiveProject);
            closedActiveProject.setStatus(ProjectStatus.Closed);
            closedActiveProject.setSubStatus(null);
            projectRepository.save(closedActiveProject);

            Project closedActiveProjectWithUnapprovedChanges = createPopulatedTestProject(
                    "Closed Active Project with Unapproved Changes", bucketProgramme, housingTemplate, TEST_ORG_ID_1,
                    STATUS_ACTIVE);
            pb.approveProject(closedActiveProjectWithUnapprovedChanges);
            DesignStandardsBlock designStandardsBlock = (DesignStandardsBlock) projectService
                    .getBlockAndLock(closedActiveProjectWithUnapprovedChanges,
                            closedActiveProjectWithUnapprovedChanges.getDesignStandardsBlock(), true);
            designStandardsBlock.setMeetingLondonHousingDesignGuide(false);
            designStandardsBlock.setReasonForNotMeetingDesignGuide("Design faulty.");
            designStandardsBlock.setLockDetails(null);
            closedActiveProjectWithUnapprovedChanges.setStatus(ProjectStatus.Closed);
            closedActiveProjectWithUnapprovedChanges.setSubStatus(null);
            projectRepository.save(closedActiveProjectWithUnapprovedChanges);
            lockDetailsRepository.deleteAllByProjectId(closedActiveProjectWithUnapprovedChanges.getId());

            pb.approveProject(createPopulatedTestProject("Active Auto Approval (Added Block)", bucketProgramme,
                    autoApprovalTemplateToAddBlockTo, TEST_ORG_ID_1, STATUS_ACTIVE));

            createPopulatedTestProject("Project With Hidden Blocks", bucketProgramme, hiddenBlocksTemplate, TEST_ORG_ID_1,
                    STATUS_COMPLETE);

            Project p = createPopulatedTestProject("Active for with new flag", bucketProgramme, housingTemplate, TEST_ORG_ID_1,
                    STATUS_ACTIVE);
            pb.approveProject(p);
            p.getDetailsBlock().setLastModified(null);
            p.getDetailsBlock().setBlockAppearsOnStatus("Active");
            p.getDetailsBlock().setBlockStatus(ProjectBlockStatus.UNAPPROVED);
            p.getDetailsBlock().setNew(true);
            projectRepository.save(p);

            pb.createProjectForClaimMilestone("Active for claim milestone", bucketProgramme, housingTemplate, true, null);

            Project statusForClaim = pb.createProjectForClaimMilestone("Active with status for claim milestone", bucketProgramme,
                    milestoneStatusTemplate, true, null);
            statusForClaim.getCalculateGrantBlock().setStartOnSiteMilestoneAuthorised(OffsetDateTime.now());
            statusForClaim.getCalculateGrantBlock().getTenureTypeAndUnitsEntries().stream()
                    .filter(e -> e.getTenureType().getTenureType().getName().equals(LONDON_AFFORDABLE_RENT))
                    .forEach(t -> t.setTotalUnitsAtStartOnSite(300));
            Milestone compMilestone = statusForClaim.getMilestonesBlock().getMilestoneByExternalId(COMPLETION_ID);
            compMilestone.setMilestoneStatus(MilestoneStatus.FORECAST);
            compMilestone.setMonetarySplit(100);
            compMilestone.setMilestoneDate(LocalDate.of(2017, 1, 10));
            Milestone sosMilestone = statusForClaim.getMilestonesBlock().getMilestoneByExternalId(START_ON_SITE_ID);
            sosMilestone.setMilestoneStatus(MilestoneStatus.ACTUAL);
            sosMilestone.setClaimStatus(ClaimStatus.Approved);
            sosMilestone.setMonetarySplit(0);
            sosMilestone.setMilestoneDate(LocalDate.of(2017, 1, 10));
            pb.approveProject(statusForClaim);
            projectRepository.save(statusForClaim);

            pb.createProjectForClaimMilestone("Active for exceeded claim", bucketProgramme, housingTemplate, false, null);
            pb.createProjectForClaimMilestone("Active for claim non-monetary milestone", bucketProgramme, housingTemplate, false,
                    null);

            Project activeReadyForPaymentAuth = pb
                    .createProjectForClaimMilestone("Active ready for request payment authorisation", bucketProgramme,
                            housingTemplate, true, ApprovalRequested);
            activeReadyForPaymentAuth.getDetailsBlock().setBlockStatus(ProjectBlockStatus.UNAPPROVED);
            activeReadyForPaymentAuth.getMilestonesBlock().setBlockStatus(ProjectBlockStatus.UNAPPROVED);
            projectRepository.save(activeReadyForPaymentAuth);
            pb.generateProjectApprovalHistory(activeReadyForPaymentAuth);

            pb.createProjectForClaimMilestone("Project to add block", bucketProgramme, housingTemplate, false, null);

            createProjectForCancelMilestone("Active for cancel claimed milestone", bucketProgramme, housingTemplate, true);

            Project activeAbandonPendingProject = createPopulatedTestProject("Active Abandon Pending", bucketProgramme,
                    housingTemplate, TEST_ORG_ID_1, STATUS_ACTIVE);
            pb.approveProject(activeAbandonPendingProject);
            activeAbandonPendingProject.setSubStatus(AbandonPending);

            Project completed = createPopulatedTestProject("Completed Project", bucketProgramme, housingTemplate, TEST_ORG_ID_1,
                    STATUS_ACTIVE);
            completed.getDetailsBlock().setLegacyProjectCode(222999);
            pb.approveProject(completed);
            completed.setStatus(ProjectStatus.Closed);
            completed.setSubStatus(Completed);

            Project custom = pb.createPopulatedTestProject("Custom Tenure Types", bucketProgramme, templateWithCustomTenureTypes,
                    TEST_ORG_ID_1, STATUS_ACTIVE);
            pb.approveProject(custom);
            ngaUnitDetailsBlock = (UnitDetailsBlock) custom.getSingleLatestBlockOfType(UnitDetails);
            ngaUnitDetailsBlock.setLastModified(environment.now());
            ngaUnitDetailsBlock.getTableEntries()
                    .add(createUnitDetailsTableEntry(custom, ngaUnitDetailsBlock, refDataService.getMarketType(2), 300));
            ngaUnitDetailsBlock.setNewBuildUnits(300);
            ngaUnitDetailsBlock.setType1Units(300);
            ngaUnitDetailsBlock.setNbWheelchairUnits(300);
            ngaUnitDetailsBlock.setGrossInternalArea(100);
            projectRepository.save(custom);

            // Add auto-approval project with questions block modified previous month
            OffsetDateTime previousMonth = OffsetDateTime.of(2019, 1, 1, 12, 12, 12, 12, ZoneOffset.UTC);

            Project autoApprovalWithQuestions = pb.createPopulatedTestProject("Auto Approval with Questions", bucketProgramme,
                    autoApprovalMonetarySplitTemplate, TEST_ORG_ID_1, STATUS_COMPLETE);
            pb.approveProject(autoApprovalWithQuestions);

            autoApprovalWithQuestions.getDetailsBlock().setLastModified(previousMonth);
            autoApprovalWithQuestions.getDetailsBlock().setBlockAppearsOnStatus("Active");
            autoApprovalWithQuestions.getDetailsBlock().setBlockStatus(ProjectBlockStatus.UNAPPROVED);
            autoApprovalWithQuestions.getDetailsBlock().setNew(true);

            // Add a new project question
            ProjectQuestionsBlock questionsBlock = (ProjectQuestionsBlock) autoApprovalWithQuestions
                    .getSingleBlockByType(ProjectBlockType.Questions);
            ProjectQuestion question = new ProjectQuestion();
            question.setTemplateQuestion(questionsBlock.getTemplateQuestions().stream().findFirst().get());
            question.setNew(true);
            question.setHidden(false);

            Set<ProjectQuestion> questions = new HashSet<>();
            questions.add(question);

            questionsBlock.setQuestions(questions);

            questionsBlock.setLastModified(previousMonth);
            questionsBlock.setBlockStatus(ProjectBlockStatus.UNAPPROVED);

            projectRepository.save(autoApprovalWithQuestions);

            createMinimalRisksBlockProjects();

        } catch (Exception e) {
            e.printStackTrace();
            // Don't allow failures when setting up test date to prevent server start up
            log.error("Error initialising test data", e);
        }
    }

    private void createMinimalRisksBlockProjects() {
        Template template = templateService.findByName("Risks Block For Small Grants");

        pb.createPopulatedTestProject("Minimal Risks Block Project 1", bucketProgramme, template, TEST_ORG_ID_1, STATUS_EMPTY);

        Project minRisksBlockActiveProject = pb.createPopulatedTestProject("Minimal Risks Block Active Project", bucketProgramme,
                template, TEST_ORG_ID_1, STATUS_EMPTY);
        pb.approveProject(minRisksBlockActiveProject);
    }

    private Project createProjectWithGrant(String name) {
        Project project = createPopulatedTestProject(name, bucketProgramme, housingTemplate, TEST_ORG_ID_1, STATUS_ACTIVE);
        project.getGrantSourceBlock().setZeroGrantRequested(false);
        project.getGrantSourceBlock().setGrantValue(25000L);
        pb.approveProject(project);
        return projectRepository.save(project);
    }

    private Project createPaymentProjectToAsses() {
        ProjectMilestonesBlock milestones;
        GrantSourceBlock grantSourceBlock;
        Project paymentProjectToAssess = pb
                .createPopulatedTestProject("Payment Project to assess", paymentTestProg, housingTemplate, TEST_ORG_ID_1,
                        STATUS_ASSESS);
        milestones = (ProjectMilestonesBlock) paymentProjectToAssess.getSingleLatestBlockOfType(ProjectBlockType.Milestones);
        for (Milestone milestone : milestones.getMilestones()) {
            if (milestone.getExternalId().equals(3003)) {
                milestone.setClaimStatus(ClaimStatus.Approved);
            }
        }
        milestones.setBlockStatus(ProjectBlockStatus.APPROVED);
        milestones.setApprovalTime(OffsetDateTime.now());

        paymentProjectToAssess.setStrategicProject(Boolean.TRUE);
        grantSourceBlock = (GrantSourceBlock) paymentProjectToAssess.getSingleLatestBlockOfType(GrantSource);
        grantSourceBlock.setDisposalProceedsFundValue(110000L);
        grantSourceBlock.setRecycledCapitalGrantFundValue(220000L);
        grantSourceBlock.setGrantValue(33000L);
        pb.initialiseCalcGrantTenure(paymentProjectToAssess);
        // pb.approveProject(paymentProjectToAssess);
        projectRepository.save(paymentProjectToAssess);
        return paymentProjectToAssess;
    }

    private Project createActivePaymentProject() {
        GrantSourceBlock grantSourceBlock;
        ProjectMilestonesBlock milestones;
        strategicProjectToTestPaymentRequests = pb
                .createPopulatedTestProject("Payment Project active", paymentTestProg, housingTemplate, TEST_ORG_ID_1,
                        STATUS_ACTIVE);

        strategicProjectToTestPaymentRequests.setStrategicProject(Boolean.TRUE);
        grantSourceBlock = (GrantSourceBlock) strategicProjectToTestPaymentRequests.getSingleLatestBlockOfType(GrantSource);
        milestones = (ProjectMilestonesBlock) strategicProjectToTestPaymentRequests
                .getSingleLatestBlockOfType(ProjectBlockType.Milestones);

        grantSourceBlock.setDisposalProceedsFundValue(110000L);
        grantSourceBlock.setRecycledCapitalGrantFundValue(220000L);
        grantSourceBlock.setGrantValue(33000L);
        for (Milestone milestone : milestones.getMilestones()) {
            if (milestone.getExternalId().equals(3003)) {
                milestone.setClaimStatus(ClaimStatus.Approved);
            }
        }
        milestones.setBlockStatus(ProjectBlockStatus.APPROVED);
        milestones.setApprovalTime(OffsetDateTime.now());
        pb.initialiseCalcGrantTenure(strategicProjectToTestPaymentRequests);
        pb.approveProject(strategicProjectToTestPaymentRequests);
        projectRepository.save(strategicProjectToTestPaymentRequests);
        return strategicProjectToTestPaymentRequests;
    }

    private Project createPopulatedTestProject(String name, Programme programme, Template template, Integer orgId, int status) {
        return pb.createPopulatedTestProject(name, programme, template, orgId, status);
    }

    private Project createTestLandProject(String projectName) {
        Project testLandProject = createPopulatedTestProject(projectName, bucketProgramme, landDisposal, GLA_HNL_ORG_ID,
                STATUS_COMPLETE);
        testLandProject.getProjectBudgetsBlock().setFromDate("2014/15");
        testLandProject.getProjectBudgetsBlock().setRevenue(500000L);
        testLandProject.getProjectBudgetsBlock().setCapital(1000000L);
        ProjectBudgetsAttachment attachment = new ProjectBudgetsAttachment();
        attachment.setFileName("test.txt");

        testLandProject.getProjectBudgetsBlock().getAttachments().add(attachment);
        createAnnualSpendDataForProject(testLandProject);
        createOutputsDataForProject(testLandProject);
        createReceiptsDataForProject(testLandProject);
        for (NamedProjectBlock namedProjectBlock : testLandProject.getProjectBlocks()) {
            namedProjectBlock.setLastModified(environment.now());
            namedProjectBlock.setModifiedBy("test.admin@gla.com");
        }
        projectRepository.save(testLandProject);
        return testLandProject;
    }

    private UnitDetailsTableEntry createUnitDetailsTableEntry(Project project, UnitDetailsBlock unitDetailsBlock,
            MarketType marketType) {
        return createUnitDetailsTableEntry(project, unitDetailsBlock, marketType, 12);
    }

    private UnitDetailsTableEntry createUnitDetailsTableEntry(Project project, UnitDetailsBlock unitDetailsBlock,
            MarketType marketType, Integer nbUnits) {
        UnitDetailsTableEntry unitDetailsTableEntry = new UnitDetailsTableEntry();
        unitDetailsTableEntry.setNbUnits(nbUnits);
        TemplateTenureType first = project.getTemplate().getTenureTypes().stream()
                .sorted(Comparator.comparingInt(TemplateTenureType::getDisplayOrder)).findFirst().get();
        unitDetailsTableEntry.setTenureId(first.getExternalId());
        unitDetailsTableEntry.setWeeklyServiceCharge(new BigDecimal(120));
        unitDetailsTableEntry.setNetWeeklyRent(new BigDecimal(1200));
        unitDetailsTableEntry.setType(UnitDetailsTableEntry.Type.Rent);
        unitDetailsTableEntry.setBlockId(unitDetailsBlock.getId());
        unitDetailsTableEntry.setProjectId(unitDetailsBlock.getProjectId());
        unitDetailsTableEntry.setMarketType(marketType);
        unitDetailsTableEntry.setUnitType(refDataService.getCategoryValue(9));
        unitDetailsTableEntry.setNbBeds(refDataService.getCategoryValue(3));
        if (marketType.getId().equals(2)) {
            unitDetailsTableEntry.setWeeklyMarketRent(new BigDecimal(1450));
        }
        return unitDetailsTableEntry;
    }

    private void generateNegotiatedRouteProject(Project project) {
        Set<ProjectTenureDetails> projectTenureDetailsEntries = project.getNegotiatedGrantBlock().getTenureTypeAndUnitsEntries();

        for (ProjectTenureDetails tt : projectTenureDetailsEntries) {
            switch (tt.getTenureType().getExternalId()) {
                case 4000: {
                    tt.setTotalUnits(0);
                    tt.setSupportedUnits(0);
                    tt.setGrantRequested(200000L);
                    tt.setTotalCost(1200000L);
                    break;
                }
                case 4001: {
                    tt.setTotalUnits(0);
                    tt.setSupportedUnits(0);
                    tt.setGrantRequested(260000L);
                    tt.setTotalCost(3200000L);
                    break;
                }
                case 4002: {
                    tt.setTotalUnits(0);
                    tt.setSupportedUnits(0);
                    tt.setGrantRequested(230000L);
                    tt.setTotalCost(930000L);
                    break;
                }
                case 4003: {
                    tt.setTotalUnits(0);
                    tt.setSupportedUnits(0);
                    tt.setGrantRequested(8900000L);
                    tt.setTotalCost(1207000L);
                    break;
                }
            }
            project.getNegotiatedGrantBlock().calculateTotals(tt);
        }
    }

    private void createOutputsDataForProject(Project persistedProject) {
        OutputType direct = outputConfigurationService.getOutputType("DIRECT");
        OutputType indMinorityStake = outputConfigurationService.getOutputType("IND_MINORITY_STAKE");
        OutputType indUnlocking = outputConfigurationService.getOutputType("IND_UNLOCKING");
        OutputType indOther = outputConfigurationService.getOutputType("IND_OTHER");

        OutputCategoryConfiguration outputConfig2 = outputConfigurationService.getOutputCategory(2);
        OutputCategoryConfiguration outputConfig11 = outputConfigurationService.getOutputCategory(11);
        OutputCategoryConfiguration outputConfig23 = outputConfigurationService.getOutputCategory(23);
        OutputCategoryConfiguration outputConfig24 = outputConfigurationService.getOutputCategory(24);
        OutputCategoryConfiguration outputConfig26 = outputConfigurationService.getOutputCategory(26);
        OutputCategoryConfiguration outputConfig30 = outputConfigurationService.getOutputCategory(30);
        OutputCategoryConfiguration outputConfig32 = outputConfigurationService.getOutputCategory(32);
        OutputCategoryConfiguration outputConfig35 = outputConfigurationService.getOutputCategory(35);

        List<OutputTableEntry> entries = new ArrayList<>();

        // 2012
        entries.add(new OutputTableEntry(persistedProject.getId(),
                persistedProject.getSingleBlockByType(ProjectBlockType.Outputs).getId(), outputConfig2, direct, 2012, 4,
                new BigDecimal(100), new BigDecimal(100)));

        // 2014
        entries.add(new OutputTableEntry(persistedProject.getId(),
                persistedProject.getSingleBlockByType(ProjectBlockType.Outputs).getId(), outputConfig2, direct, 2014, 6,
                new BigDecimal(100), new BigDecimal(100)));

        // 2016
        entries.add(new OutputTableEntry(persistedProject.getId(),
                persistedProject.getSingleBlockByType(ProjectBlockType.Outputs).getId(), outputConfig2, direct, 2016, 4,
                new BigDecimal(150), new BigDecimal(100)));
        entries.add(new OutputTableEntry(persistedProject.getId(),
                persistedProject.getSingleBlockByType(ProjectBlockType.Outputs).getId(), outputConfig11, direct, 2016, 4,
                new BigDecimal(200), new BigDecimal(250)));
        entries.add(new OutputTableEntry(persistedProject.getId(),
                persistedProject.getSingleBlockByType(ProjectBlockType.Outputs).getId(), outputConfig24, direct, 2016, 6,
                new BigDecimal(5), null));
        entries.add(new OutputTableEntry(persistedProject.getId(),
                persistedProject.getSingleBlockByType(ProjectBlockType.Outputs).getId(), outputConfig23, indUnlocking, 2016, 8,
                new BigDecimal("20.5"), new BigDecimal("20.5")));
        entries.add(new OutputTableEntry(persistedProject.getId(),
                persistedProject.getSingleBlockByType(ProjectBlockType.Outputs).getId(), outputConfig30, direct, 2016, 9, null,
                new BigDecimal(250)));
        entries.add(new OutputTableEntry(persistedProject.getId(),
                persistedProject.getSingleBlockByType(ProjectBlockType.Outputs).getId(), outputConfig32, indOther, 2016, 9,
                new BigDecimal(30), new BigDecimal(30)));
        entries.add(new OutputTableEntry(persistedProject.getId(),
                persistedProject.getSingleBlockByType(ProjectBlockType.Outputs).getId(), outputConfig26, indMinorityStake, 2017,
                3, new BigDecimal(500), null));
        entries.add(new OutputTableEntry(persistedProject.getId(),
                persistedProject.getSingleBlockByType(ProjectBlockType.Outputs).getId(), outputConfig35, direct, 2017, 3,
                new BigDecimal(3000000), null));

        outputTableEntryRepository.saveAll(entries);
    }

    private void createAnnualSpendDataForProject(Project persistedProject) {
        List<ProjectLedgerEntry> list = new ArrayList<>();

        // 2012
        list.add(new ProjectLedgerEntry(persistedProject, persistedProject.getSingleBlockByType(ProjectBlockType.ProjectBudgets),
                2012, 6,
                LedgerStatus.FORECAST, LedgerType.PAYMENT, SpendType.CAPITAL,
                5, "Direct Construction cost", new BigDecimal("100.00")));

        // ANNUAL BUDGET can be ignored here to test it has no effect
        list.add(new ProjectLedgerEntry(persistedProject, persistedProject.getSingleBlockByType(ProjectBlockType.ProjectBudgets),
                2016,
                LedgerType.BUDGET, SpendType.CAPITAL, new BigDecimal("145000.45")));
        list.add(new ProjectLedgerEntry(persistedProject, persistedProject.getSingleBlockByType(ProjectBlockType.ProjectBudgets),
                2016,
                LedgerType.BUDGET, SpendType.REVENUE, new BigDecimal("1995512.45")));

        // JUNE
        list.add(new ProjectLedgerEntry(persistedProject, persistedProject.getSingleBlockByType(ProjectBlockType.ProjectBudgets),
                2016, 6,
                LedgerStatus.FORECAST, LedgerType.PAYMENT, SpendType.CAPITAL,
                5, "Direct Construction cost", new BigDecimal("4500.45")));
        list.add(new ProjectLedgerEntry(persistedProject, persistedProject.getSingleBlockByType(ProjectBlockType.ProjectBudgets),
                2016, 6,
                LedgerStatus.FORECAST, LedgerType.PAYMENT, SpendType.CAPITAL,
                1, "Consultancy - Commissioned Report", new BigDecimal("-12300.12")));
        list.add(new ProjectLedgerEntry(persistedProject, persistedProject.getSingleBlockByType(ProjectBlockType.ProjectBudgets),
                2016, 6,
                LedgerStatus.FORECAST, LedgerType.PAYMENT, SpendType.REVENUE,
                1, "Consultancy - Commissioned Report", new BigDecimal("-6700.72")));

        // ACTUAL
        list.add(new ProjectLedgerEntry(persistedProject, persistedProject.getSingleBlockByType(ProjectBlockType.ProjectBudgets),
                2016, 6,
                LedgerStatus.ACTUAL, LedgerType.PAYMENT, SpendType.REVENUE,
                21, "Other Professional Fees", new BigDecimal("-1331.32")));

        list.add(new ProjectLedgerEntry(persistedProject, persistedProject.getSingleBlockByType(ProjectBlockType.ProjectBudgets),
                2016, 6,
                LedgerStatus.ACTUAL, LedgerType.PAYMENT, SpendType.CAPITAL,
                1, "Consultancy - Commissioned Report", new BigDecimal("-12589.99")));
        list.add(new ProjectLedgerEntry(persistedProject, persistedProject.getSingleBlockByType(ProjectBlockType.ProjectBudgets),
                2016, 5,
                LedgerStatus.ACTUAL, LedgerType.PAYMENT, SpendType.REVENUE,
                21, "Other Professional Fees", new BigDecimal("-13331.32")));

        list.add(new ProjectLedgerEntry(persistedProject, persistedProject.getSingleBlockByType(ProjectBlockType.ProjectBudgets),
                2016, 5,
                LedgerStatus.ACTUAL, LedgerType.PAYMENT, SpendType.CAPITAL,
                1, "Consultancy - Commissioned Report", new BigDecimal("-1589.99")));

        // AUGUST
        list.add(new ProjectLedgerEntry(persistedProject, persistedProject.getSingleBlockByType(ProjectBlockType.ProjectBudgets),
                2016, 8,
                LedgerStatus.FORECAST, LedgerType.PAYMENT, SpendType.CAPITAL,
                10, "External Audit Fees", new BigDecimal("-8900.12")));
        list.add(new ProjectLedgerEntry(persistedProject, persistedProject.getSingleBlockByType(ProjectBlockType.ProjectBudgets),
                2016, 8,
                LedgerStatus.FORECAST, LedgerType.PAYMENT, SpendType.REVENUE,
                1, "Consultancy - Commissioned Report", new BigDecimal("-124.02")));
        list.add(new ProjectLedgerEntry(persistedProject, persistedProject.getSingleBlockByType(ProjectBlockType.ProjectBudgets),
                2016, 8,
                LedgerStatus.FORECAST, LedgerType.PAYMENT, SpendType.CAPITAL,
                1, "Consultancy - Commissioned Report", new BigDecimal("-1455.12")));

        // FORECAST And Actuals CURRENT FINANCIAL YEAR
        int monthToUse = 4; // first month in year

        int currentActualYear = OffsetDateTime.now().getYear();
        int currentActualMonth = OffsetDateTime.now().getMonthValue();

        int endFinYear = currentActualMonth < 4 ? currentActualYear : currentActualYear + 1;
        int startOfCurrentFinancialYear = currentActualMonth > 4 ? currentActualYear : currentActualYear - 1;

        Integer previousYear = startOfCurrentFinancialYear - 1;

        list.add(new ProjectLedgerEntry(persistedProject, persistedProject.getSingleBlockByType(ProjectBlockType.ProjectBudgets),
                endFinYear, 3,
                LedgerStatus.FORECAST, LedgerType.PAYMENT, SpendType.CAPITAL,
                10, "External Audit Fees", new BigDecimal("-5600")));
        list.add(new ProjectLedgerEntry(persistedProject, persistedProject.getSingleBlockByType(ProjectBlockType.ProjectBudgets),
                endFinYear, 3,
                LedgerStatus.FORECAST, LedgerType.PAYMENT, SpendType.REVENUE,
                21, "Other Professional Fees", new BigDecimal("-5100")));
        list.add(new ProjectLedgerEntry(persistedProject, persistedProject.getSingleBlockByType(ProjectBlockType.ProjectBudgets),
                endFinYear, 3,
                LedgerStatus.FORECAST, LedgerType.PAYMENT, SpendType.CAPITAL,
                1, "Consultancy - Commissioned Report", new BigDecimal("-4400")));

        list.add(new ProjectLedgerEntry(persistedProject, persistedProject.getSingleBlockByType(ProjectBlockType.ProjectBudgets),
                previousYear, monthToUse,
                LedgerStatus.ACTUAL, LedgerType.PAYMENT, SpendType.CAPITAL,
                10, "External Audit Fees", new BigDecimal("-11200")));
        list.add(new ProjectLedgerEntry(persistedProject, persistedProject.getSingleBlockByType(ProjectBlockType.ProjectBudgets),
                previousYear, monthToUse,
                LedgerStatus.ACTUAL, LedgerType.PAYMENT, SpendType.REVENUE,
                21, "Other Professional Fees", new BigDecimal("-15100")));

        ProjectLedgerEntry ledgerEntry = new ProjectLedgerEntry(persistedProject,
                persistedProject.getSingleBlockByType(ProjectBlockType.ProjectBudgets), previousYear, monthToUse,
                LedgerStatus.ACTUAL, LedgerType.PAYMENT, SpendType.CAPITAL,
                1, "Consultancy - Commissioned Report", new BigDecimal("-43400"));
        list.add(populateLegerEntry(ledgerEntry, previousYear, monthToUse));

        list.add(new ProjectLedgerEntry(persistedProject, persistedProject.getSingleBlockByType(ProjectBlockType.ProjectBudgets),
                startOfCurrentFinancialYear, LedgerType.BUDGET, SpendType.REVENUE, new BigDecimal("1000000")));
        list.add(new ProjectLedgerEntry(persistedProject, persistedProject.getSingleBlockByType(ProjectBlockType.ProjectBudgets),
                startOfCurrentFinancialYear, LedgerType.BUDGET, SpendType.CAPITAL, new BigDecimal("2000000")));

        final Integer organisationId = persistedProject.getOrganisation().getId();
        list.forEach(p -> p.setOrganisationId(organisationId));
        financeService.save(list);
    }

    private void createReceiptsDataForProject(Project project) {
        List<ProjectLedgerEntry> list = new ArrayList<>();

        int currentYear = financialCalendar.currentYear();
        createReceipt(project, currentYear - 1, 4, LedgerStatus.ACTUAL, "NHS Bodies Contributions", new BigDecimal("-1800.00"));

        for (int year = currentYear; year < currentYear + 2; year++) {
            // APRIL
            createReceipt(project, year, 4, LedgerStatus.FORECAST, "NHS Bodies Contributions", new BigDecimal("1800.00"));
            createReceipt(project, year, 4, LedgerStatus.ACTUAL, "NHS Bodies Contributions", new BigDecimal("1000.00"));
            createReceipt(project, year, 4, LedgerStatus.ACTUAL, "NHS Bodies Contributions", new BigDecimal("500.00"));

            createReceipt(project, year, 4, LedgerStatus.FORECAST, "EU Grants", new BigDecimal("200.00"));
            // splitting this actuals to test they are summed and returning in 1 total entry (GLA-4265)
            createReceipt(project, year, 4, LedgerStatus.ACTUAL, "EU Grants", new BigDecimal("100.00"));
            createReceipt(project, year, 4, LedgerStatus.ACTUAL, "EU Grants", new BigDecimal("100.00"));
            createReceipt(project, year, 4, LedgerStatus.ACTUAL, "EU Grants", new BigDecimal("100.00"));

            // JUNE
            createReceipt(project, year, 6, LedgerStatus.FORECAST, "Capital Recpt-Property-on Completion",
                    new BigDecimal("300000.00"));
            createReceipt(project, year, 6, LedgerStatus.ACTUAL, "Capital Recpt-Property-on Completion",
                    new BigDecimal("40000.00"));

            // AUGUST
            createReceipt(project, year, 8, LedgerStatus.FORECAST, "Interest Receivable on Grant Recoveries",
                    new BigDecimal("50000.00"));
            createReceipt(project, year, 8, LedgerStatus.ACTUAL, "Interest Receivable on Grant Recoveries",
                    new BigDecimal("50430.29"));

            createReceipt(project, year, 8, LedgerStatus.FORECAST, "Other Public Sector Contributions",
                    new BigDecimal("30000.00"));
            createReceipt(project, year, 8, LedgerStatus.ACTUAL, "Other Public Sector Contributions", new BigDecimal("30000.00"));

            // OCTOBER
            createReceipt(project, year, 10, LedgerStatus.FORECAST, "General Fees & Charges", new BigDecimal("25000.00"));
            createReceipt(project, year, 10, LedgerStatus.ACTUAL, "General Fees & Charges", new BigDecimal("27590.15"));

            // JANUARY
            createReceipt(project, year + 1, 1, LedgerStatus.FORECAST, "Other Local Authority Contributions",
                    new BigDecimal("80000.00"));
            createReceipt(project, year + 1, 1, LedgerStatus.ACTUAL, "Other Local Authority Contributions",
                    new BigDecimal("75960.50"));

            createReceipt(project, year + 1, 1, LedgerStatus.FORECAST, "Unwind Discount on Finance Leases",
                    new BigDecimal("40000.00"));
            createReceipt(project, year + 1, 1, LedgerStatus.ACTUAL, "Unwind Discount on Finance Leases",
                    new BigDecimal("40000.00"));

            // FEBRUARY
            createReceipt(project, year + 1, 2, LedgerStatus.FORECAST, "EU Grants", new BigDecimal("10000.00"));
        }

        financeService.save(list);
    }

    private void createReceipt(Project project, int year, int month, LedgerStatus status, String categoryText, BigDecimal value) {
        FinanceCategory category = refDataService.getFinanceCategoryByText(categoryText);
        ProjectLedgerEntry entry = new ProjectLedgerEntry(project, project.getReceiptsBlock(), year, month, status,
                LedgerType.RECEIPT, null, category.getId(), value);
        populateLegerEntry(entry, year, month);
        financeService.save(entry);
    }

    private ProjectLedgerEntry populateLegerEntry(ProjectLedgerEntry entry, Integer year, Integer month) {
        entry.setVendorName("Bob's Staplers");
        entry.setTransactionDate("12/" + month + "/" + year);
        entry.setSapCategoryCode("W132");
        entry.setLedgerSource(LedgerSource.SAP);
        entry.setTransactionNumber("P11111-22222");
        return entry;
    }

    private void cloneBlock(Project project, ProjectBlockType type) {
        NamedProjectBlock singleBlockByType = project.getSingleBlockByType(type);
        NamedProjectBlock clone = singleBlockByType.cloneBlock("test.admin@gla.com", environment.now());
        project.addBlockToProject(clone);
        clone.setVersionNumber(2);

        project.getLatestProjectBlocks().remove(singleBlockByType);
        project.getLatestProjectBlocks().add(clone);

        singleBlockByType.setReportingVersion(true);
        singleBlockByType.setLatestVersion(false);

        clone.setReportingVersion(false);

        project.setSubStatus(UnapprovedChanges);

        projectRepository.save(project);

    }


    private Project createProjectForCancelMilestone(String projectName, Programme programme, Template template,
            boolean monetary) {
        Project project = pb.createProjectForClaimMilestone(projectName, programme, template, monetary, null);
        Milestone planningGrantedMilestone = getMilestoneByName(project, "Planning granted");
        planningGrantedMilestone.setClaimStatus(ClaimStatus.Pending);
        planningGrantedMilestone.setMonetary(false);

        Milestone completionMilestone = getMilestoneByName(project, "Completion");
        completionMilestone.setMilestoneStatus(MilestoneStatus.ACTUAL);
        completionMilestone.setMonetarySplit(0);

        getMilestoneByName(project, "Land acquired").setMonetarySplit(50);
        getMilestoneByName(project, "Land acquired").setClaimStatus(ClaimStatus.Approved);
        getMilestoneByName(project, "Land acquired").setClaimedGrant(500000L);
        getMilestoneByName(project, "Land acquired").setClaimedRcgf(25000L);
        getMilestoneByName(project, "Start on site").setMonetarySplit(50);

        projectRepository.save(project);
        return project;
    }

    private Milestone getMilestoneByName(Project project, String milestoneName) {
        ProjectMilestonesBlock milestonesBlock = project.getMilestonesBlock();
        return milestonesBlock.getMilestones().stream().filter(m -> m.getSummary().equals(milestoneName)).findAny().get();
    }

    public Project createUnapprovedProject(String projectTitle) {
        Project unapprovedProject = createPopulatedTestProject(projectTitle, bucketProgramme, allBlocksTemplate, TEST_ORG_ID_1,
                STATUS_ACTIVE);
        pb.approveProject(unapprovedProject);
        cloneBlock(unapprovedProject, ProjectBlockType.Details);
        cloneBlock(unapprovedProject, Milestones);
        cloneBlock(unapprovedProject, ProjectBlockType.CalculateGrant);
        cloneBlock(unapprovedProject, ProjectBlockType.DeveloperLedGrant);
        cloneBlock(unapprovedProject, ProjectBlockType.NegotiatedGrant);
        cloneBlock(unapprovedProject, ProjectBlockType.IndicativeGrant);
        cloneBlock(unapprovedProject, ProjectBlockType.GrantSource);
        cloneBlock(unapprovedProject, ProjectBlockType.DesignStandards);
        cloneBlock(unapprovedProject, ProjectBlockType.Questions);
        projectRepository.save(unapprovedProject);
        return unapprovedProject;
    }

    @Override
    public void addSupplementalData() {
    }

    @Override
    public void afterInitialisation() {
        // update template here so project is affected
        RisksTemplateBlock risks = new RisksTemplateBlock();
        risks.setBlockDisplayName("Risks And Issues");
        risks.setDisplayOrder(7);
        templateService.addBlock(autoApprovalTemplateToAddBlockTo.getId(), risks);
    }

}
