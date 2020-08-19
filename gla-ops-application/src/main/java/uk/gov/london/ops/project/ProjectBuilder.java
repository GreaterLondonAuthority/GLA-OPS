/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.project;

import static uk.gov.london.ops.organisation.OrganisationBuilder.MOPAC_TEST_ORG_1;
import static uk.gov.london.ops.organisation.OrganisationBuilder.SKILLS_TEST_ORG_1;
import static uk.gov.london.ops.organisation.OrganisationBuilder.TEST_ORG_ID_1;
import static uk.gov.london.ops.payment.LedgerType.BUDGET;
import static uk.gov.london.ops.payment.LedgerType.PAYMENT;
import static uk.gov.london.ops.payment.ProjectLedgerEntry.MATCH_FUND_CATEGORY;
import static uk.gov.london.ops.payment.SpendType.CAPITAL;
import static uk.gov.london.ops.payment.SpendType.REVENUE;
import static uk.gov.london.ops.project.block.ProjectBlockType.CalculateGrant;
import static uk.gov.london.ops.project.block.ProjectBlockType.DeliveryPartners;
import static uk.gov.london.ops.project.block.ProjectBlockType.Funding;
import static uk.gov.london.ops.project.block.ProjectBlockType.GrantSource;
import static uk.gov.london.ops.project.block.ProjectBlockType.Milestones;
import static uk.gov.london.ops.project.block.ProjectBlockType.Outputs;
import static uk.gov.london.ops.project.block.ProjectBlockType.ProjectObjectives;
import static uk.gov.london.ops.project.block.ProjectBlockType.Risks;
import static uk.gov.london.ops.project.block.ProjectBlockType.UserDefinedOutput;
import static uk.gov.london.ops.user.UserBuilder.DATA_INITIALISER_USER;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.london.ops.file.AttachmentFile;
import uk.gov.london.ops.file.FileService;
import uk.gov.london.ops.framework.Environment;
import uk.gov.london.ops.framework.exception.ValidationException;
import uk.gov.london.ops.organisation.OrganisationGroupService;
import uk.gov.london.ops.organisation.OrganisationService;
import uk.gov.london.ops.organisation.model.OrganisationGroup;
import uk.gov.london.ops.payment.FinanceService;
import uk.gov.london.ops.payment.LedgerStatus;
import uk.gov.london.ops.payment.ProjectLedgerEntry;
import uk.gov.london.ops.payment.SpendType;
import uk.gov.london.ops.programme.domain.Programme;
import uk.gov.london.ops.project.block.DesignStandardsBlock;
import uk.gov.london.ops.project.block.NamedProjectBlock;
import uk.gov.london.ops.project.block.ProjectBlockType;
import uk.gov.london.ops.project.block.ProjectDetailsBlock;
import uk.gov.london.ops.project.deliverypartner.Deliverable;
import uk.gov.london.ops.project.deliverypartner.DeliverableType;
import uk.gov.london.ops.project.deliverypartner.DeliveryPartner;
import uk.gov.london.ops.project.deliverypartner.DeliveryPartnersBlock;
import uk.gov.london.ops.project.funding.FundingActivity;
import uk.gov.london.ops.project.funding.FundingActivityGroup;
import uk.gov.london.ops.project.funding.FundingBlock;
import uk.gov.london.ops.project.grant.BaseGrantBlock;
import uk.gov.london.ops.project.grant.CalculateGrantBlock;
import uk.gov.london.ops.project.grant.DeveloperLedGrantBlock;
import uk.gov.london.ops.project.grant.GrantSourceBlock;
import uk.gov.london.ops.project.grant.IndicativeGrantBlock;
import uk.gov.london.ops.project.grant.IndicativeTenureValue;
import uk.gov.london.ops.project.grant.NegotiatedGrantBlock;
import uk.gov.london.ops.project.grant.ProjectTenureDetails;
import uk.gov.london.ops.project.implementation.mapper.MilestoneMapper;
import uk.gov.london.ops.project.implementation.repository.FundingActivityGroupRepository;
import uk.gov.london.ops.project.implementation.repository.ProjectHistoryRepository;
import uk.gov.london.ops.project.implementation.repository.ProjectRepository;
import uk.gov.london.ops.project.milestone.Milestone;
import uk.gov.london.ops.project.milestone.MilestoneStatus;
import uk.gov.london.ops.project.milestone.ProjectMilestonesBlock;
import uk.gov.london.ops.project.outputs.OutputsBlock;
import uk.gov.london.ops.project.outputs.OutputsCostsBlock;
import uk.gov.london.ops.project.question.Answer;
import uk.gov.london.ops.project.question.ProjectQuestionsBlock;
import uk.gov.london.ops.project.repeatingentity.OtherFundingBlock;
import uk.gov.london.ops.project.repeatingentity.ProjectObjective;
import uk.gov.london.ops.project.repeatingentity.ProjectObjectivesBlock;
import uk.gov.london.ops.project.repeatingentity.UserDefinedOutput;
import uk.gov.london.ops.project.repeatingentity.UserDefinedOutputBlock;
import uk.gov.london.ops.project.risk.ProjectRiskAndIssue;
import uk.gov.london.ops.project.risk.ProjectRisksBlock;
import uk.gov.london.ops.project.state.ProjectStatus;
import uk.gov.london.ops.project.template.domain.AnswerType;
import uk.gov.london.ops.project.template.domain.FundingTemplateBlock;
import uk.gov.london.ops.project.template.domain.GrantSourceTemplateBlock;
import uk.gov.london.ops.project.template.domain.MilestoneTemplate;
import uk.gov.london.ops.project.template.domain.MilestonesTemplateBlock;
import uk.gov.london.ops.project.template.domain.ProcessingRoute;
import uk.gov.london.ops.project.template.domain.Template;
import uk.gov.london.ops.project.template.domain.TemplateQuestion;
import uk.gov.london.ops.refdata.Borough;
import uk.gov.london.ops.refdata.CategoryValue;
import uk.gov.london.ops.refdata.ConfigurableListItem;
import uk.gov.london.ops.refdata.RefDataService;
import uk.gov.london.ops.user.UserBuilder;

/**
 * Factory class for building Project entities, primarily for testing.
 *
 * @author Steve Leach
 */
@Component
public class ProjectBuilder {

    Logger log = LoggerFactory.getLogger(getClass());

    // Initial status of projects; related to but slightly different to project state model.
    // Should probably use an enum, but I need to use ">=" and haven't figured out how to do that with enums yet
    public static int STATUS_EMPTY = 0;
    public static int STATUS_PARTIAL = 1;
    public static int STATUS_COMPLETE = 2;
    public static int STATUS_SUBMITTED = 3;
    public static int STATUS_WITHDRAWN = 4;
    public static int STATUS_RESUBMITTED = 5;
    public static int STATUS_ASSESS = 6;
    public static int STATUS_RETURNED = 7;
    public static int STATUS_ACTIVE = 8;
    public static int STATUS_ClOSED = 9;

    @Autowired
    FileService fileService;

    @Autowired
    ProjectService projectService;

    @Autowired
    FundingActivityGroupRepository fundingActivityGroupRepository;

    @Autowired
    ProjectRepository projectRepository;

    @Autowired
    OrganisationService organisationService;

    @Autowired
    OrganisationGroupService organisationGroupService;

    @Autowired
    ProjectHistoryRepository projectHistoryRepository;

    @Autowired
    FinanceService financeService;

    @Autowired
    RefDataService refDataService;

    @Autowired
    MilestoneMapper milestoneMapper;

    @Autowired
    UserBuilder userBuilder;

    @Autowired
    Environment environment;

    /**
     * Creates and saves a test project.
     */
    public Project createTestProject(String title, int orgId, Programme programme, Template template, int initialStatus) {
        return createTestProject(title, orgId, programme, template, null, initialStatus);
    }

    public Project createTestProject(String title, int orgId, Programme programme, Template template,
            OrganisationGroup group, int initialStatus) {
        if (template == null) {
            log.warn("Cannot create a project with null template: " + title);
            return null;
        }
        try {
            if (orgId == TEST_ORG_ID_1) {
                userBuilder.withLoggedInUser("test.glaops@gmail.com");
            } else if (orgId == MOPAC_TEST_ORG_1) {
                userBuilder.withLoggedInUser("mopac.rp@gla.com");
            } else if (orgId == SKILLS_TEST_ORG_1) {
                userBuilder.withLoggedInUser("skillsapproved@gla.com");
            } else {
                userBuilder.withLoggedInUser(DATA_INITIALISER_USER);
            }

            Project project = setupTestProject(title, orgId, programme, template, group);
            project = projectService.createProject(project);
            if (group != null) {
                project.getDetailsBlock().setDevelopingOrganisationId(group.getLeadOrganisationId());
            }
            if (initialStatus >= STATUS_PARTIAL && initialStatus <= STATUS_ACTIVE) {
                populateProjectDetails(project);
            }
            if (title.contains("Kit")) {
                project.getDetailsBlock().setLegacyProjectCode(21346);
            }
            project.setEnriched(true);
            projectRepository.save(project);
            return project;
        } catch (RuntimeException e) {
            log.error("Error creating test project " + title, e);
        }
        return null;
    }

    /**
     * Sets up, but doesn't save, a test project.
     */
    public Project setupTestProject(String title, int orgId, Programme programme, Template template, OrganisationGroup group) {
        Project project = new Project();
        project.setTitle(title);
        project.setProgramme(programme);
        project.setTemplate(template);
        project.setOrganisation(organisationService.findOne(orgId));
        project.setOrgSelected(true);
        project.setCreatedBy(DATA_INITIALISER_USER);
        project.setCreatedOn(OffsetDateTime.now().minus(15, ChronoUnit.DAYS));

        if (group != null) {
            project.setOrganisationGroupId(group.getId());
        }

        return project;
    }

    public void populateProjectDetails(Project project) {

        ProjectDetailsBlock detailsBlock = project.getDetailsBlock();
        detailsBlock.setAddress("1 The High Street");
        detailsBlock.setBorough("Camden");
        Borough camden = refDataService.findBoroughByName("Camden");
        detailsBlock.setWardId(camden.getWards().get(0).getId());
        detailsBlock.setPostcode("EC1 4RJ");
        detailsBlock.setCoordX("123456");
        detailsBlock.setCoordY("323456");
        detailsBlock.setInterest("Interest ");
        detailsBlock.setProjectManager("Bob T Builder");
        detailsBlock.setSiteStatus("Operational");
        detailsBlock.setSiteOwner("Mr M Smith");
        detailsBlock.setDescription("Planned development of a 21 houses.");
        detailsBlock.setMainContact("User Alpha");
        detailsBlock.setMainContactEmail("user.alpha@gla.org");
    }

    public Project createPopulatedTestProject(String title, Programme prog, Template template, int org, int initialStatus) {
        return this.createPopulatedTestProject(title, prog, template, org, 0, initialStatus);
    }

    public Project createPopulatedTestProject(String title, Programme prog, Template template, int org, int orgGroupId,
            int initialStatus) {
        OrganisationGroup orgGroup = orgGroupId == 0 ? null : organisationGroupService.findByGroupId(orgGroupId);
        Project project = createTestProject(title, org, prog, template, orgGroup, initialStatus);
        if (initialStatus >= STATUS_SUBMITTED) {
            populateAndSubmitProject(project);
        } else {
            populateAllProjectData(project);

        }
        saveProjectWithHistoricLastUpdate(project);
        return project;
    }

    private void saveProjectWithHistoricLastUpdate(Project project) {
        project.setLastModified(OffsetDateTime.now().minus(1 + new Random().nextInt(15), ChronoUnit.DAYS));
        projectRepository.saveAndFlush(project);
    }

    private void populateAndSubmitProject(Project submitted) {
        populateAllProjectData(submitted);

        submitted.setStatus(ProjectStatus.Submitted);

        // don't change this or tests will break
        OffsetDateTime dateTime = LocalDateTime.parse("2016-10-04 12:30", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
                .atZone(ZoneId.systemDefault()).toOffsetDateTime();

        // as the "Created" history transition is timestamped on the creation date time, we need this to mock the data
        ProjectHistory projectCreationTransition = projectHistoryRepository
                .findAllByProjectIdOrderByCreatedOnDesc(submitted.getId()).get(0);
        projectCreationTransition.setCreatedOn(dateTime.minus(4, ChronoUnit.DAYS));
        projectHistoryRepository.save(projectCreationTransition);

        ProjectHistory projectHistory = new ProjectHistory();

        projectHistory.setProjectId(submitted.getId());
        projectHistory.setTransition(ProjectHistory.Transition.Submitted);
        projectHistory.setComments("Submitted");
        projectHistory.setCreatedOn(dateTime.minus(3, ChronoUnit.DAYS));
        projectHistory.setCreatedBy("test.admin@gla.com");
        projectHistoryRepository.save(projectHistory);

        projectHistory = new ProjectHistory();
        projectHistory.setProjectId(submitted.getId());
        projectHistory.setTransition(ProjectHistory.Transition.Withdrawn);
        projectHistory.setComments("Returned to Draft");
        projectHistory.setCreatedOn(dateTime.minus(2, ChronoUnit.DAYS));
        projectHistory.setCreatedBy("test.admin@gla.com");
        projectHistoryRepository.save(projectHistory);

        projectHistory = new ProjectHistory();
        projectHistory.setProjectId(submitted.getId());
        projectHistory.setTransition(ProjectHistory.Transition.Submitted);
        projectHistory.setComments("Submitted Again");
        projectHistory.setCreatedOn(dateTime.minus(1, ChronoUnit.DAYS));
        projectHistory.setCreatedBy("user.alpha@gla.org");
        projectHistoryRepository.save(projectHistory);
    }

    private void populateAllProjectData(Project project) {
        populateProjectDetails(project);

        List<NamedProjectBlock> milestonesBlocks = project.getBlocksByType(Milestones);

        if (milestonesBlocks.size() > 0) {
            populateMilestonesBlock(project, milestonesBlocks);
        }

        BaseGrantBlock tenure = project.getIndicativeGrantBlock();
        tenure = tenure == null ? project.getNegotiatedGrantBlock() : tenure;
        tenure = tenure == null ? project.getCalculateGrantBlock() : tenure;
        tenure = tenure == null ? project.getDeveloperLedGrantBlock() : tenure;

        if (tenure != null) {
            populateTenureBlock(tenure);
        }

        DesignStandardsBlock designStandardsBlock = project.getDesignStandardsBlock();
        if (designStandardsBlock != null) {
            populateDesignStandardsBlock(designStandardsBlock);
        }

        GrantSourceBlock grantSourceBlock = (GrantSourceBlock) project.getSingleLatestBlockOfType(GrantSource);
        if (grantSourceBlock != null) {
            populateGrantSource(grantSourceBlock);
        }

        List<NamedProjectBlock> questionBlocks = project.getBlocksByType(ProjectBlockType.Questions);
        if (questionBlocks.size() > 0) {
            populateQuestionsBlock(questionBlocks);
        }

        ProjectRisksBlock risksBlock = (ProjectRisksBlock) project.getSingleLatestBlockOfType(Risks);
        if (risksBlock != null) {
            populateRisksBlock(risksBlock);
        }

        OutputsBlock outputsBlock = (OutputsBlock) project.getSingleLatestBlockOfType(Outputs);
        if (outputsBlock != null) {
            populateOutputsBlock(outputsBlock);
        }

        if (project.getFundingBlock() != null) {
            populateFundingBlock(project);
        }

        DeliveryPartnersBlock deliveryPartnersBlock = (DeliveryPartnersBlock) project
                .getSingleLatestBlockOfType(DeliveryPartners);
        if (deliveryPartnersBlock != null) {
            populateDeliveryPartnersBlock(deliveryPartnersBlock);
            if (deliveryPartnersBlock.getDeliveryPartners() != null && deliveryPartnersBlock.isShowDeliverables()) {
                populateDeliverables(deliveryPartnersBlock);
            }
        }

        OutputsCostsBlock outputsCosts = project.getOutputsCostsBlock();
        if (outputsCosts != null) {
            populateOutputCostsBlock(outputsCosts);
        }

        OtherFundingBlock otherFundingBlock = project.getOtherFundingBlock();
        if (otherFundingBlock != null) {
            populateOtherFundingBlock(otherFundingBlock);
        }

        ProjectObjectivesBlock projectObjectivesBlock = (ProjectObjectivesBlock) project
                .getSingleLatestBlockOfType(ProjectObjectives);
        if (projectObjectivesBlock != null) {
            populateProjectObjectivesBlock(projectObjectivesBlock);
        }

        UserDefinedOutputBlock userDefinedOutputBlock = (UserDefinedOutputBlock) project
                .getSingleLatestBlockOfType(UserDefinedOutput);
        if (userDefinedOutputBlock != null) {
            populateUserDefinedOutputBlock(userDefinedOutputBlock);
        }
    }

    private void populateUserDefinedOutputBlock(UserDefinedOutputBlock userDefinedOutputBlock) {
        UserDefinedOutput userDefinedOutput = new UserDefinedOutput();
        userDefinedOutput.setOutputName("Output1");
        userDefinedOutput.setDeliveryAmount("100");
        userDefinedOutput.setMonitorOfOutput("Test reason one");

        userDefinedOutputBlock.getUserDefinedOutputs().add(userDefinedOutput);

        UserDefinedOutput userDefinedOutputTwo = new UserDefinedOutput();
        userDefinedOutputTwo.setOutputName("Output two");
        userDefinedOutputTwo.setDeliveryAmount("200");
        userDefinedOutputTwo.setMonitorOfOutput("Test reason two");
        userDefinedOutputBlock.getUserDefinedOutputs().add(userDefinedOutputTwo);
        userDefinedOutputBlock.setLastModified(OffsetDateTime.now());
    }

    private void populateProjectObjectivesBlock(ProjectObjectivesBlock projectObjectivesBlock) {
        ProjectObjective objective = new ProjectObjective();
        objective.setTitle("title");
        objective.setSummary("summary");
        projectObjectivesBlock.getProjectObjectives().add(objective);
        projectObjectivesBlock.setLastModified(OffsetDateTime.now());
    }

    private void populateOtherFundingBlock(OtherFundingBlock otherFundingBlock) {
        otherFundingBlock.setHasFundingPartners(Boolean.FALSE);
        otherFundingBlock.setLastModified(OffsetDateTime.now());
    }

    private void populateOutputCostsBlock(OutputsCostsBlock outputsCosts) {
        outputsCosts.setLastModified(OffsetDateTime.now());
        outputsCosts.setAdvancePayment(new BigDecimal(100000));
        outputsCosts.setTotalProjectSpend(new BigDecimal(200000));
        outputsCosts.selectRecoveryOutput(201);
    }

    private void populateOutputsBlock(OutputsBlock outputsBlock) {
        outputsBlock.setLastModified(OffsetDateTime.now());
    }

    private void populateDeliveryPartnersBlock(DeliveryPartnersBlock block) {
        block.setHasDeliveryPartners(true);
        block.setQuestion2(true);
        block.setQuestion3(true);
        block.setQuestion4(true);
        block.setQuestion5(true);
        DeliveryPartner deliveryPartner = new DeliveryPartner("test org");
        deliveryPartner.setIdentifier(1234);
        deliveryPartner.setOrganisationType("type");
        deliveryPartner.setContractValue(new BigDecimal(100.00));
        deliveryPartner.setRole("role");
        block.getDeliveryPartners().add(deliveryPartner);
        block.setLastModified(OffsetDateTime.now());

    }

    private void populateDeliverables(DeliveryPartnersBlock block) {
        DeliveryPartner deliveryPartner = block.getDeliveryPartners().get(0);
        Deliverable deliverable = new Deliverable();
        deliveryPartner.getDeliverables().add(deliverable);

        deliverable.setFee(new BigDecimal(100.00));
        deliverable.setValue(new BigDecimal(1000.00));
        deliverable.setQuantity(100);
        deliverable.setDeliverableType(DeliverableType.ADULT_EDUCATION_BUDGET);
        deliverable.setComments("Test comments");
    }

    private void populateMilestonesBlock(Project project, List<NamedProjectBlock> milestonesBlocks) {
        for (NamedProjectBlock namedProjectBlock : milestonesBlocks) {
            ProjectMilestonesBlock milestonesBlock = (ProjectMilestonesBlock) namedProjectBlock;
            milestonesBlock.setLastModified(environment.now());

            MilestonesTemplateBlock milestonesTemplate = (MilestonesTemplateBlock) project.getTemplate()
                    .getSingleBlockByType(Milestones);

            if (milestonesTemplate.getProcessingRoutes() != null && milestonesTemplate.getProcessingRoutes().size() > 0) {
                Optional<ProcessingRoute> first = milestonesTemplate.getProcessingRoutes()
                        .stream().filter(pr -> pr.getName().equalsIgnoreCase("Land & Development")).findFirst();

                ProcessingRoute processingRoute;
                if (first.isPresent()) {
                    processingRoute = first.get();
                } else {
                    processingRoute = milestonesTemplate.getProcessingRoutes()
                            .stream().filter(pr -> pr.getName().equals("default"))
                            .findFirst().orElse(milestonesTemplate.getProcessingRoutes().iterator().next());
                }
                milestonesBlock.setProcessingRouteId(processingRoute.getId());
                milestonesBlock.getMilestones().clear();
                milestonesBlock.getMilestones()
                        .addAll(milestoneMapper.toProjectMilestones(processingRoute.getMilestones(), project.getTemplate()));
            }

            int count = 2;
            for (Milestone milestone : milestonesBlock.getMilestones()) {
                // milestone.setMilestoneDate(LocalDate.now().minus(100, ChronoUnit.DAYS));
                milestone.setMilestoneDate(LocalDate.of(2017, 5, 10));
                //if (milestone.getMonetary() && count > 0) {
                //    milestone.setMonetarySplit(50);
                //    count--;
                //} else {
                //    milestone.setMonetarySplit(0);
                //}
                milestone.setMilestoneStatus(MilestoneStatus.ACTUAL);
            }


        }
    }

    private void populateTenureBlock(BaseGrantBlock tenure) {
        tenure.setLastModified(environment.now());
        ProjectTenureDetails units = tenure.getTenureTypeAndUnitsEntriesSorted().get(0);

        tenure.calculateTotals(units);
        if (tenure instanceof CalculateGrantBlock) {
            units.setTotalUnits(300);
            units.setTotalCost(2500000L);
            units.setS106Units(20);
        } else if (tenure instanceof NegotiatedGrantBlock) {
            NegotiatedGrantBlock negotiatedGrantBlock = (NegotiatedGrantBlock) tenure;
            negotiatedGrantBlock.setJustification("Justy");
            units.setGrantRequested(1200000L);
            units.setSupportedUnits(12);
            units.setTotalUnits(300);
            units.setTotalCost(2500000L);
        } else if (tenure instanceof DeveloperLedGrantBlock) {
            DeveloperLedGrantBlock block = (DeveloperLedGrantBlock) tenure;
            block.setAffordableCriteriaMet(true);
            units.setS106Units(20);
            units.setAdditionalAffordableUnits(12);
            units.setTotalCost(2500000L);
        } else if (tenure instanceof IndicativeGrantBlock) {
            IndicativeGrantBlock block = (IndicativeGrantBlock) tenure;
            Set<IndicativeTenureValue> indicativeTenureValues = units.getIndicativeTenureValues();
            for (IndicativeTenureValue indicativeTenureValue : indicativeTenureValues) {

                switch (indicativeTenureValue.getYear()) {
                    case 2017:
                        indicativeTenureValue.setUnits(12);
                        break;
                    case 2018:
                        indicativeTenureValue.setUnits(22);
                        break;
                    case 2019:
                        indicativeTenureValue.setUnits(31);
                        break;
                    default:
                        throw new RuntimeException("Unrecognised indicative tenure year: " + indicativeTenureValue.getYear());
                }
            }

        }
    }

    private void populateDesignStandardsBlock(DesignStandardsBlock block) {
        block.setMeetingLondonHousingDesignGuide(true);
        block.setLastModified(environment.now());
    }

    private void populateGrantSource(GrantSourceBlock block) {
        block.setZeroGrantRequested(!((GrantSourceTemplateBlock) block.getProject().getTemplate()
                .getSingleBlockByType(GrantSource)).isNilGrantHidden());
        block.setLastModified(environment.now());
    }

    private void populateQuestionsBlock(List<NamedProjectBlock> questionBlocks) {
        for (NamedProjectBlock block : questionBlocks) {
            ProjectQuestionsBlock questionsBlock = (ProjectQuestionsBlock) block;
            Set<TemplateQuestion> questionEntities = questionsBlock.getTemplateQuestions();
            Set<Answer> answers = questionsBlock.getAnswers();
            questionsBlock.setLastModified(OffsetDateTime.now());
            for (TemplateQuestion questionEntity : questionEntities) {
                Answer ans = new Answer();
                ans.setQuestion(questionEntity.getQuestion());

                if (AnswerType.FreeText.equals(questionEntity.getQuestion().getAnswerType())) {
                    ans.setAnswer(
                            "Some really quite long text, asdiansodnoa ind oasindoaksndo aknso dkasndokasndokn aasdasd asd asd");
                } else if (AnswerType.Text.equals(questionEntity.getQuestion().getAnswerType())) {
                    ans.setAnswer("Some no quite so long text");
                } else if (AnswerType.YesNo.equals(questionEntity.getQuestion().getAnswerType())) {
                    ans.setAnswer("yes");
                } else if (AnswerType.Date.equals(questionEntity.getQuestion().getAnswerType())) {
                    ans.setAnswer("2015-12-12");
                } else if (AnswerType.Number.equals(questionEntity.getQuestion().getAnswerType())) {
                    ans.setNumericAnswer(1244.1);
                } else if (AnswerType.Dropdown.equals(questionEntity.getQuestion().getAnswerType())) {
                    ans.setAnswer(questionEntity.getQuestion().getAnswerOptions().iterator().next().getOption());
                } else if (AnswerType.FileUpload.equals(questionEntity.getQuestion().getAnswerType())) {
                    AttachmentFile file = new AttachmentFile();
                    file.setFileName("test.pdf");
                    file.setContentType("applciation/pdf");
                    file.setCreatedOn(OffsetDateTime.now());
                    try {
                        fileService.save(file, new ByteArrayInputStream(new byte[]{1, 2, 3, 4}));
                    } catch (IOException e) {
                        throw new ValidationException("Unable to persist file");
                    }
                    ans.getFileAttachments().add(file);
                }
                answers.add(ans);
            }
        }
    }

    private void populateRisksBlock(ProjectRisksBlock risksBlock) {
        risksBlock.setLastModified(environment.now());
        risksBlock.setRating(1);
        risksBlock.setRatingExplanation("No significant risk to report at the moment, project is only in feasibility stage");

        ProjectRiskAndIssue risk = new ProjectRiskAndIssue();
        risk.setTitle("Risk 1");
        risk.setDescription("Risk Description");
        risk.setInitialImpactRating(3);
        risk.setInitialProbabilityRating(3);
        risk.setResidualImpactRating(2);
        risk.setResidualProbabilityRating(2);
        risk.setRiskCategory(new CategoryValue(22, null, null, null));
        risksBlock.getProjectRiskAndIssues().add(risk);

        ProjectRiskAndIssue issue = new ProjectRiskAndIssue();
        issue.setTitle("Issue 1");
        issue.setDescription("Issue Description");
        issue.setInitialImpactRating(1);
        issue.setType(ProjectRiskAndIssue.Type.Issue);
        issue.setRiskCategory(new CategoryValue(24, null, null, null));
        risksBlock.getProjectRiskAndIssues().add(issue);
    }

    private void populateFundingBlock(Project project) {
        FundingBlock block = project.getFundingBlock();
        block.setStartYear(2009);
        block.setYearAvailableTo(15);

        String text1 = null;
        String text2 = null;
        Integer extID1 = null;
        Integer extID2 = null;

        if (block.getShowMilestones()) {
            ProcessingRoute processingRoute = ((MilestonesTemplateBlock) project.getTemplate().getSingleBlockByType(Milestones))
                    .getProcessingRoute(project.getMilestonesBlock().getProcessingRouteId());
            Set<String> names = processingRoute.getMilestones().stream().map(MilestoneTemplate::getSummary)
                    .collect(Collectors.toSet());
            if (names.remove("Start on site")) {
                text1 = "Start on site";
                extID1 = project.getMilestonesBlock().getMilestoneBySummary(text1).getExternalId();
            }

            if (names.remove("Construction")) {
                text1 = "Construction";
                extID1 = project.getMilestonesBlock().getMilestoneBySummary(text1).getExternalId();
            }

            if (names.remove("Planning permission")) {
                text2 = "Planning permission";
                extID1 = project.getMilestonesBlock().getMilestoneBySummary(text2).getExternalId();

            }
            if (text2 == null && names.remove("Planning granted")) {
                text2 = "Planning granted";
                extID1 = project.getMilestonesBlock().getMilestoneBySummary(text2).getExternalId();
            }

            if (text2 == null && names.remove("Completion")) {
                text2 = "Completion";
                extID1 = project.getMilestonesBlock().getMilestoneBySummary(text2).getExternalId();
            }

            if (text2 == null && names.remove("Contractor Procurement")) {
                text2 = "Contractor Procurement";
                extID1 = project.getMilestonesBlock().getMilestoneBySummary(text2).getExternalId();
            }


        } else {
            FundingTemplateBlock singleBlockByType = (FundingTemplateBlock) project.getTemplate().getSingleBlockByType(Funding);
            List<ConfigurableListItem> items = refDataService
                    .getConfigurableListItemsByExtID(singleBlockByType.getCategoriesExternalId())
                    .stream().sorted(Comparator.comparingInt(ConfigurableListItem::getDisplayOrder)).collect(Collectors.toList());
            if (items.size() > 0) {
                text1 = items.get(0).getCategory();
                extID1 = items.get(0).getId();
            }

            if (items.size() > 1) {
                text2 = items.get(1).getCategory();
                extID2 = items.get(1).getId();
            }
        }

        if (text1 == null || text2 == null) {
            log.error("Unable to configure funding block for project: " + project.getTitle());
            return;
        }

        if (block.getShowCapitalGLAFunding()) {
            financeService.save(new ProjectLedgerEntry(project, block, 2015, BUDGET, CAPITAL, new BigDecimal(1000.00)));
            financeService.save(new ProjectLedgerEntry(project, block, 2017, BUDGET, CAPITAL, new BigDecimal(1000.00)));
        }

        if (block.getShowRevenueGLAFunding()) {
            financeService.save(new ProjectLedgerEntry(project, block, 2017, BUDGET, REVENUE, new BigDecimal(30.00)));
            createActivity(project, block, 2017, 1, REVENUE, "Completion of external courtyard works", extID1, text1,
                    new BigDecimal("100.00"), null);
        }

        if (block.getShowCapitalOtherFunding()) {
            financeService.save(new ProjectLedgerEntry(project, block, 2015, BUDGET, CAPITAL, new BigDecimal(500.00),
                    MATCH_FUND_CATEGORY));
            financeService.save(new ProjectLedgerEntry(project, block, 2017, BUDGET, CAPITAL, new BigDecimal(200.00),
                    MATCH_FUND_CATEGORY));
        }

        if (block.getShowRevenueOtherFunding()) {
            financeService
                    .save(new ProjectLedgerEntry(project, block, 2017, BUDGET, REVENUE, new BigDecimal(0), MATCH_FUND_CATEGORY));
            createActivity(project, block, 2017, 3, REVENUE, "Preferred contractor appointed ", extID2, text2, null,
                    new BigDecimal("100.00"));
        }

        if (block.getShowCapitalOtherFunding() || block.getShowCapitalGLAFunding()) {
            createActivity(project, block, 2015, 1, CAPITAL, "Tender list identified", extID2, text2,
                    block.getShowCapitalGLAFunding() ? new BigDecimal("1000.00") : null,
                    block.getShowCapitalOtherFunding() ? new BigDecimal("500.00") : null);
            createActivity(project, block, 2017, 1, CAPITAL, "Tender list identified", extID2, text2,
                    block.getShowCapitalGLAFunding() ? new BigDecimal("1000.00") : null,
                    block.getShowCapitalOtherFunding() ? new BigDecimal("500.00") : null);
            createActivity(project, block, 2017, 1, CAPITAL, "Tender invitations issued", extID2, text2,
                    block.getShowCapitalGLAFunding() ? new BigDecimal("2000.00") : null,
                    block.getShowCapitalOtherFunding() ? new BigDecimal("1000.00") : null);
        }

        projectRepository.save(project);


    }

    private void createActivity(Project project, FundingBlock block, Integer year, Integer quarter, SpendType spendType,
            String name, Integer milestoneId, String milestoneName, BigDecimal value, BigDecimal matchFundValue) {
        FundingActivity activity = new FundingActivity(block.getId(), year, quarter, name, milestoneId, milestoneName);

        if (value != null) {
            ProjectLedgerEntry mainLedgerEntry = new ProjectLedgerEntry(project, block, year, PAYMENT, spendType, value);
            mainLedgerEntry.setLedgerStatus(LedgerStatus.FORECAST);
            mainLedgerEntry.setQuarter(quarter);
            mainLedgerEntry.setExternalId(milestoneId);
            mainLedgerEntry.setSubCategory(milestoneName);
            mainLedgerEntry = financeService.save(mainLedgerEntry);
            activity.getLedgerEntries().add(mainLedgerEntry);
        }

        if (matchFundValue != null) {
            ProjectLedgerEntry matchFundLedgerEntry = new ProjectLedgerEntry(project, block, year, PAYMENT, spendType,
                    matchFundValue, MATCH_FUND_CATEGORY);
            matchFundLedgerEntry.setLedgerStatus(LedgerStatus.FORECAST);
            matchFundLedgerEntry.setQuarter(quarter);
            matchFundLedgerEntry.setExternalId(milestoneId);
            matchFundLedgerEntry.setSubCategory(milestoneName);
            matchFundLedgerEntry = financeService.save(matchFundLedgerEntry);
            activity.getLedgerEntries().add(matchFundLedgerEntry);
        }

        FundingActivityGroup actGroup = fundingActivityGroupRepository
                .findByBlockIdAndYearAndQuarter(block.getId(), year, quarter);
        if (actGroup == null) {
            actGroup = new FundingActivityGroup(block.getId(), year, quarter);
        }
        actGroup.getActivities().add(activity);
        fundingActivityGroupRepository.save(actGroup);
    }

    public void approveProject(Project project) {
        project.setStatus(ProjectStatus.Active);
        OffsetDateTime now = environment.now();
        project.setFirstApproved(project.getCreatedOn());
        for (NamedProjectBlock block : project.getProjectBlocks()) {
            if (block.getBlockStatus().equals(NamedProjectBlock.BlockStatus.UNAPPROVED)) {
                block.approve("test.admin@gla.com", now);
                block.setLockDetails(null);
            }
        }
        updateBlockVisibility(project);
        projectService.initialiseWithSkillsData(project);
        if (!project.getStateModel().isApprovalRequired()) {
            ProjectHistory projectHistory = new ProjectHistory();
            projectHistory.setProjectId(project.getId());
            projectHistory.setTransition(ProjectHistory.Transition.Approved);
            projectHistory.setComments("Approved");
            projectHistory.setDescription("Project saved to active");
            projectHistory.setCreatedOn(project.getFirstApproved());
            projectHistory.setCreatedBy("user.alpha@gla.org");
            projectHistoryRepository.save(projectHistory);
        }
        projectRepository.save(project);
    }

    private void updateBlockVisibility(Project project) {
        project.getProjectBlocks().stream()
                .filter(block -> Objects.equals(project.getStatusName(), block.getBlockAppearsOnStatus()))
                .forEach(block -> {
                    block.setNew(true);
                    block.setHidden(false);
                    block.setLastModified(null);
                    project.getLatestProjectBlocks().add(block);
                });
    }

    public void initialiseCalcGrantTenure(Project project) {
        int count = 1;

        CalculateGrantBlock block = (CalculateGrantBlock) project.getSingleLatestBlockOfType(CalculateGrant);

        for (ProjectTenureDetails tenureTypeAndUnits : block.getTenureTypeAndUnitsEntriesSorted()) {
            tenureTypeAndUnits.setTotalUnits(count * 10);
            tenureTypeAndUnits.setS106Units(count * 2);
            tenureTypeAndUnits.setTotalCost(count * 45000L);
            count++;


        }
        projectRepository.save(project);
    }

    public void generateProjectApprovalHistory(Project project) {
        ProjectHistory projectHistory = new ProjectHistory();
        projectHistory.setProjectId(project.getId());
        projectHistory.setTransition(ProjectHistory.Transition.ApprovalRequested);
        projectHistory.setComments("Approval Requested");
        projectHistory.setCreatedOn(environment.now().minus(2, ChronoUnit.DAYS));
        projectHistory.setCreatedBy("test.admin@gla.com");
        projectHistoryRepository.save(projectHistory);
    }

    public void cloneBlock(Project project, ProjectBlockType type) {
        NamedProjectBlock singleBlockByType = project.getSingleBlockByType(type);
        NamedProjectBlock clone = singleBlockByType.cloneBlock("test.admin@gla.com", environment.now());
        project.addBlockToProject(clone);
        clone.setVersionNumber(2);

        project.getLatestProjectBlocks().remove(singleBlockByType);
        singleBlockByType.setLatestVersion(false);
        project.getLatestProjectBlocks().add(clone);

        singleBlockByType.setReportingVersion(true);

        clone.setReportingVersion(false);

        projectRepository.save(project);

    }

}
