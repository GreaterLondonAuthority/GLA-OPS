/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.report.implementation;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;
import uk.gov.london.common.GlaUtils;
import uk.gov.london.ops.di.DataInitialiserModule;
import uk.gov.london.ops.di.builders.ProjectBuilder;
import uk.gov.london.ops.refdata.OutputCategoryConfiguration;
import uk.gov.london.ops.refdata.OutputConfigurationGroup;
import uk.gov.london.ops.domain.project.*;
import uk.gov.london.ops.domain.project.question.Answer;
import uk.gov.london.ops.domain.project.question.ProjectQuestionsBlock;
import uk.gov.london.ops.domain.template.*;
import uk.gov.london.ops.project.implementation.MilestoneMapper;
import uk.gov.london.ops.refdata.OutputConfigurationService;
import uk.gov.london.ops.report.LegacyIMSDataImporter;
import uk.gov.london.ops.report.Report;
import uk.gov.london.ops.repository.*;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.time.LocalDate;
import java.util.*;

import static uk.gov.london.ops.di.builders.OrganisationBuilder.GLA_HOUSING_AND_LAND_ORG_ID;
import static uk.gov.london.ops.di.builders.ProjectBuilder.STATUS_EMPTY;
import static uk.gov.london.ops.di.builders.TemplateBuilder.*;

@Transactional
@Component
public class ReportingDataInitialiser implements DataInitialiserModule {

    Logger log = LoggerFactory.getLogger(getClass());

    @Autowired MilestoneMapper milestoneMapper;
    @Autowired ProjectBuilder projectBuilder;
    @Autowired TemplateRepository templateRepository;
    @Autowired ProjectRepository projectRepository;
    @Autowired ProgrammeRepository programmeRepository;
    @Autowired ReportRepository reportRepository;
    @Autowired OutputTableEntryRepository outputTableEntryRepository;

    @Autowired
    private OutputConfigurationService outputConfigurationService;

    @Autowired
    private LegacyIMSDataImporter legacyIMSDataImporter;

    private Project approvedProviderRouteProject;
    private Project developerLedRouteProject;
    private Project negotiatedRouteProject;
    private Project indicativeProject;
    private Project smallProjectsAndEquipmentFundProject;
    private Project landDisposalProject;

    private Template landDisposal;


    public Project getApprovedProviderRouteProject() {
        return approvedProviderRouteProject;
    }

    public Project getDeveloperLedRouteProject() {
        return developerLedRouteProject;
    }

    public Project getNegotiatedRouteProject() {
        return negotiatedRouteProject;
    }

    public Project getIndicativeProject() {
        return indicativeProject;
    }

    public Project getSmallProjectsAndEquipmentFundProject() {
        return smallProjectsAndEquipmentFundProject;
    }

    public Project getLandDisposalProject() {
        return landDisposalProject;
    }

    @Override
    public String getName() {
        return "Reporting data initialiser";
    }

    @Override
    public void beforeInitialisation() {}

    @Override
    public void cleanupOldData() {}

    @Override
    public void addReferenceData() {
        OutputConfigurationGroup group = outputConfigurationService.getGroup(1001);

        OutputCategoryConfiguration cat = new OutputCategoryConfiguration(111, "Test Hidden", "Hidden", OutputCategoryConfiguration.InputValueType.NUMBER_OF);
        cat.setHidden(true);
        group.getCategories().add(cat);

        outputConfigurationService.save(cat);

        outputConfigurationService.save(group);
    }

    @Override
    public void addUsers() {}

    @Override
    public void addOrganisations() {}

    @Override
    public void addTemplates() {}

    @Override
    public void addProgrammes() {}

    @Override
    public void addProjects() {
        Programme mainsteamTestProg = programmeRepository.findByName("Mainstream housing programme test");

        Template approvedProviderRoute = templateRepository.findByName(APPROVED_PROVIDER_ROUTE_TEMPLATE_NAME);
        Template developerLedRoute = templateRepository.findByName(DEVELOPER_LED_ROUTE_TEMPLATE_NAME);
        Template negotiatedRoute = templateRepository.findByName(NEGOTIATED_ROUTE_TEMPLATE_NAME);
        Template indicativeRoute = templateRepository.findByName(INDICATIVE_TEMPLATE_NAME);
        Template smallProjectsAndEquipmentFund = templateRepository.findByName(SMALL_PROJECTS_AND_EQUIPMENT_FUND);
        Template enforced = templateRepository.findByName("Small Projects and Equipment Fund - Enforced");
        landDisposal = templateRepository.findByName("Land Disposal");

        createReportingViewTestProjects(mainsteamTestProg, approvedProviderRoute, developerLedRoute, negotiatedRoute, indicativeRoute, smallProjectsAndEquipmentFund, landDisposal, enforced);
    }

    @Override
    public void addSupplementalData() {

        legacyIMSDataImporter.importLegacyImsProject(this.getClass().getResourceAsStream("ims-legacy-projects.csv"));
        legacyIMSDataImporter.importLegacyImsReportedFigures(this.getClass().getResourceAsStream("ims-legacy-reported-figures.csv"));

        List<Report.Filter> programmeFilter = new ArrayList<>();
        programmeFilter.add(Report.Filter.Programme);

        Report costExceeded = new Report("Cost Exceeded", "Report for testing exceeded cost", "cost_exceeded" , false,  programmeFilter);
        reportRepository.save(costExceeded);

        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        try {
            Resource[] resources = resolver.getResources("classpath:**/testdata/reports/*.sql");
            List<Report.Filter> reportFilterList;
            for (Resource resource : resources) {
                Report report = new Report(resource.getFilename().replace(".sql", ""), null, IOUtils.toString(resource.getInputStream(), Charset.defaultCharset()));
                if (report.getName().toLowerCase().contains("external")) {
                    report.setExternal(true);
                    reportFilterList = report.getReportFiltersList();
                    reportFilterList.add(Report.Filter.Programme);
                    reportFilterList.add(Report.Filter.Borough);
                    reportFilterList.add(Report.Filter.ProjectStatus);
                    reportFilterList.add(Report.Filter.ProjectType);
                    reportFilterList.add(Report.Filter.Team);
                }
                if(report.getName().toLowerCase().contains("internal")) {
                    reportFilterList = report.getReportFiltersList();
                    reportFilterList.add(Report.Filter.Programme);
                    reportFilterList.add(Report.Filter.Team);

                }
                if(report.getName().toLowerCase().contains("corporate")) {
                    report.setExternal(true);
                    report.setDescription("Report about corporate projects");
                    reportFilterList = report.getReportFiltersList();
                    reportFilterList.add(Report.Filter.Programme);
                    reportFilterList.add(Report.Filter.Label);

                }
                reportRepository.save(report);
            }
        }
        catch (Exception e) {
            log.error("Error loading SQL reports", e);
        }
    }

    @Override
    public void afterInitialisation() {}

    private void createReportingViewTestProjects(Programme programme, Template... templates) {
        for (Template template : templates) {
            projectBuilder.createTestProject("e2e reporting view test project - " + template.getName(),  GLA_HOUSING_AND_LAND_ORG_ID, programme, template, STATUS_EMPTY);
        }

        approvedProviderRouteProject = projectRepository.findAllByTitle("e2e reporting view test project - "+APPROVED_PROVIDER_ROUTE_TEMPLATE_NAME).get(0);
        developerLedRouteProject = projectRepository.findAllByTitle("e2e reporting view test project - "+DEVELOPER_LED_ROUTE_TEMPLATE_NAME).get(0);
        negotiatedRouteProject = projectRepository.findAllByTitle("e2e reporting view test project - "+NEGOTIATED_ROUTE_TEMPLATE_NAME).get(0);
        indicativeProject = projectRepository.findAllByTitle("e2e reporting view test project - "+INDICATIVE_TEMPLATE_NAME).get(0);
        smallProjectsAndEquipmentFundProject = projectRepository.findAllByTitle("e2e reporting view test project - "+SMALL_PROJECTS_AND_EQUIPMENT_FUND).get(0);
        landDisposalProject = projectRepository.findAllByTitle("e2e reporting view test project - "+"Land Disposal").get(0);

        GrantSourceBlock grantSourceBlock = (GrantSourceBlock) approvedProviderRouteProject.getSingleBlockByType(ProjectBlockType.GrantSource);
        grantSourceBlock.setGrantValue(123l);
        generateApprovedProviderRouteTenureData(approvedProviderRouteProject);
        generateDeveloperLedTenureData(developerLedRouteProject);
        generateNegotiatedRouteProject(negotiatedRouteProject);
        generateIndicativeProjectTenureData(indicativeProject);

        GrantSourceBlock devGrantSource = (GrantSourceBlock) developerLedRouteProject.getSingleBlockByType(ProjectBlockType.GrantSource);
        devGrantSource.setRecycledCapitalGrantFundValue(456l);
        devGrantSource.setDisposalProceedsFundValue(789l);
        ((GrantSourceBlock)negotiatedRouteProject.getSingleBlockByType(ProjectBlockType.GrantSource)).setZeroGrantRequested(true);

        answerQuestions(approvedProviderRouteProject);
        answerQuestions(negotiatedRouteProject);
        answerAdditionalQuestions(negotiatedRouteProject);
        answerQuestions(developerLedRouteProject);
        answerQuestions(indicativeProject);

        createMilestones(approvedProviderRouteProject);
        createMilestones(negotiatedRouteProject);
        createMilestones(developerLedRouteProject);


        generateOutputs(smallProjectsAndEquipmentFundProject);
        generateOutputs(landDisposalProject);

        projectRepository.saveAll(Arrays.asList(approvedProviderRouteProject, developerLedRouteProject, negotiatedRouteProject, indicativeProject, smallProjectsAndEquipmentFundProject,landDisposalProject));
    }

    private void generateOutputs(Project project) {
        int currentMonth = LocalDate.now().getMonth().getValue();
        int currentQuarter = GlaUtils.getCurrentQuarter(currentMonth);
        int firstMonthInQuarter = GlaUtils.getFirstMonthInQuarter(currentQuarter);
        int year = LocalDate.now().getYear();
        Template template = project.getTemplate();
        OutputsTemplateBlock outputsTemplateBlock = (OutputsTemplateBlock) template.getSingleBlockByType(ProjectBlockType.Outputs);
        OutputCategoryConfiguration conf = outputsTemplateBlock.getOutputConfigurationGroup().getCategories().stream().sorted(Comparator.comparingInt(OutputCategoryConfiguration::getId)).findFirst().get();

        OutputsBlock outputsBlock = (OutputsBlock) project.getSingleBlockByType(ProjectBlockType.Outputs);
        Set<OutputTableEntry> outputTableEntries = new HashSet<>();

        for(int month=firstMonthInQuarter;month<firstMonthInQuarter+3;month++) {
            OutputTableEntry outputTableEntry = new OutputTableEntry(project.getId(), outputsBlock.getId(), conf,
                    outputConfigurationService.getOutputType("DIRECT"), year, month, BigDecimal.valueOf(5.0), BigDecimal.valueOf(10000.0));
            outputTableEntries.add(outputTableEntry);
        }

        if (outputsTemplateBlock.getOutputConfigurationGroup().getId().equals(1001)) {
            OutputCategoryConfiguration hidden = outputsTemplateBlock.getOutputConfigurationGroup().getCategories().stream().filter(c -> c.getId().equals(111)).findFirst().get();
            OutputTableEntry outputTableEntry = new OutputTableEntry(project.getId(), outputsBlock.getId(), hidden,
                    outputConfigurationService.getOutputType("DIRECT"), 2018, 1, BigDecimal.valueOf(5.0), BigDecimal.valueOf(10000.0));
            outputTableEntries.add(outputTableEntry);
        }
        outputsBlock.setTableData(outputTableEntries);
        outputTableEntryRepository.saveAll(outputTableEntries);
    }


    private void generateIndicativeProjectTenureData(Project project) {
        IndicativeGrantBlock indicativeGrantBlock = project.getIndicativeGrantBlock();
        for (ProjectTenureDetails tenure : indicativeGrantBlock.getTenureTypeAndUnitsEntries()) {
            for (IndicativeTenureValue indicativeTenureValue : tenure.getIndicativeTenureValues()) {
                Integer year = indicativeTenureValue.getYear();
                indicativeGrantBlock.calculateTotals(tenure);

                switch (tenure.getTenureType().getExternalId()) {
                    case (4001) : {
                        switch (year) {
                            case (2017) : indicativeTenureValue.setUnits(17); break;
                            case (2018) : indicativeTenureValue.setUnits(18); break;
                            case (2019) : indicativeTenureValue.setUnits(19); break;
                        }
                        break;
                    }
                    case (4002) : {
                        switch (year) {
                            case (2017) : indicativeTenureValue.setUnits(171); break;
                            case (2018) : indicativeTenureValue.setUnits(181); break;
                            case (2019) : indicativeTenureValue.setUnits(191); break;
                        }
                    }
                }
            }
        }
    }

    private void answerAdditionalQuestions(Project negotiatedRouteProject) {
        List<ProjectQuestionsBlock> questionsBlocks = negotiatedRouteProject.getQuestionsBlocks();
        ProjectQuestionsBlock questionsBlock=null;

        for (ProjectQuestionsBlock block : questionsBlocks) {
            if (block.getBlockDisplayName().contains("dditional")) {
                questionsBlock = block;
            }
        }

        questionsBlock.getAnswers().clear();
        for (TemplateQuestion templateQuestion : questionsBlock.getTemplateQuestions()) {
            Answer answer = new Answer();
            answer.setQuestion(templateQuestion.getQuestion());
            answer.setQuestionId(templateQuestion.getQuestion().getId());
            switch (answer.getQuestion().getId()) {
                case 524:
                    answer.setNumericAnswer(25.0); break;
                case 511:
                    answer.setNumericAnswer(17.0); break;
                case 512:
                    answer.setNumericAnswer(6.0); break;
                case 513:
                    answer.setAnswer("Homeless families"); break;
                case 514:
                    answer.setNumericAnswer(3.0); break;
                case 525:
                    answer.setNumericAnswer(12225.0); break;
                case 526:
                    answer.setNumericAnswer(9878.0); break;
                case 527:
                    answer.setNumericAnswer(120003.2); break;
                case 528:
                    answer.setNumericAnswer(2323231.0); break;

            }
            if (answer.getNumericAnswer() != null || answer.getAnswer() != null) {
                questionsBlock.getAnswers().add(answer);
            }
        }
    }

    private void createMilestones(Project project) {
        ProjectMilestonesBlock milestonesBlock = (ProjectMilestonesBlock) project.getSingleBlockByType(ProjectBlockType.Milestones);
        MilestonesTemplateBlock singleBlockByType = (MilestonesTemplateBlock) project.getTemplate().getSingleBlockByType(ProjectBlockType.Milestones);
        Set<ProcessingRoute> processingRoutes = singleBlockByType.getProcessingRoutes();
        for (ProcessingRoute processingRoute : processingRoutes) {
            if ("Acquisition of Home & Works".equals(processingRoute.getName())) {
                milestonesBlock.setProcessingRouteId(processingRoute.getId());
                milestonesBlock.getMilestones().addAll(milestoneMapper.toProjectMilestones(processingRoute.getMilestones(), project.getTemplate()));
                for (Milestone milestone : milestonesBlock.getMilestones()) {
                    if (milestone.getExternalId() != null) {
                        switch (milestone.getExternalId()) {
                            case 3001:
                                milestone.setMilestoneDate(LocalDate.of(2012, 12, 12));
                                milestone.setMilestoneStatus(MilestoneStatus.ACTUAL);
                                break;
                            case 3003:
                                milestone.setMilestoneDate(LocalDate.of(2016, 12, 12));
                                milestone.setMilestoneStatus(MilestoneStatus.ACTUAL);
                                break;
                            case 3004:
                                milestone.setMilestoneDate(LocalDate.of(2070, 12, 12));
                                milestone.setMilestoneStatus(MilestoneStatus.FORECAST);
                                break;
                            case 3005:
                                milestone.setMilestoneDate(LocalDate.of(2082, 12, 12));
                                milestone.setMilestoneStatus(MilestoneStatus.FORECAST);
                                break;
                            case 3006:
                                milestone.setMilestoneDate(LocalDate.of(2092, 12, 12));
                                milestone.setMilestoneStatus(MilestoneStatus.FORECAST);
                                break;
                        }
                    }
                }
            }
        }
    }
    private void answerQuestions(Project project) {
        List<ProjectQuestionsBlock> questionsBlocks = project.getQuestionsBlocks();
        ProjectQuestionsBlock questionsBlock=null;

        for (ProjectQuestionsBlock block : questionsBlocks) {
            if (!block.getBlockDisplayName().contains("dditional")) {
                questionsBlock = block;
            }
        }

        questionsBlock.getAnswers().clear();
        for (TemplateQuestion templateQuestion : questionsBlock.getTemplateQuestions()) {
            Answer answer = new Answer();
            answer.setQuestion(templateQuestion.getQuestion());
            answer.setQuestionId(templateQuestion.getQuestion().getId());
            switch (answer.getQuestion().getId()) {
                case 530:
                    answer.setNumericAnswer(25.0); break;
                case 501:
                    answer.setAnswer("Rent to save"); break;
                case 502:
                    answer.setAnswer("Not required"); break;
                case 503:
                    answer.setAnswer("Land identified"); break;
                case 504:
                    answer.setAnswer("Yes, all units"); break;
                case 505:
                    answer.setNumericAnswer(12225.0); break;
                case 506:
                    answer.setAnswer("Yes"); break;
                case 507:
                    answer.setAnswer("No"); break;
                case 509:
                    answer.setNumericAnswer(23.0); break;
                case 519:
                    answer.setNumericAnswer(12.0); break;
                case 520:
                    answer.setNumericAnswer(8.0); break;
                case 521:
                    answer.setNumericAnswer(23.0); break;
                case 522:
                    answer.setNumericAnswer(233.0); break;
                case 529:
                    answer.setAnswer("Contracting process not yet begun"); break;
            }
            if (answer.getNumericAnswer() != null || answer.getAnswer() != null) {
                questionsBlock.getAnswers().add(answer);
            }
        }
    }

    private void generateDeveloperLedTenureData(Project project) {
        Set<ProjectTenureDetails> projectTenureDetailsEntries = project.getDeveloperLedGrantBlock().getTenureTypeAndUnitsEntries();
        project.getDeveloperLedGrantBlock().setAffordableCriteriaMet(true);
        for (ProjectTenureDetails tt : projectTenureDetailsEntries) {
            switch (tt.getTenureType().getExternalId()) {
                case 4000: {
                    tt.setAdditionalAffordableUnits(12);
                    tt.setS106Units(10);
                    tt.setTotalCost(1200000L);
                    break;
                }
                case 4001: {
                    tt.setAdditionalAffordableUnits(5);
                    tt.setS106Units(0);
                    tt.setTotalCost(15000L);
                    break;
                }
                case 4002: {
                    tt.setAdditionalAffordableUnits(50);
                    tt.setS106Units(45);
                    tt.setTotalCost(900000L);
                    break;
                }
                case 4003: {
                    tt.setAdditionalAffordableUnits(1);
                    tt.setS106Units(1);
                    tt.setTotalCost(124000L);
                    break;
                }
            }
            project.getDeveloperLedGrantBlock().calculateTotals(tt);
        }
    }

    private void generateApprovedProviderRouteTenureData(Project project) {
        Set<ProjectTenureDetails> projectTenureDetailsEntries = project.getCalculateGrantBlock().getTenureTypeAndUnitsEntries();
        for (ProjectTenureDetails tt : projectTenureDetailsEntries) {
            switch (tt.getTenureType().getExternalId()) {
                case 4000: {
                    tt.setTotalUnits(120);
                    tt.setS106Units(10);
                    tt.setTotalCost(1200000L);
                    break;
                }
                case 4001: {
                    tt.setTotalUnits(5);
                    tt.setS106Units(0);
                    tt.setTotalCost(15000L);
                    break;
                }
                case 4002: {
                    tt.setTotalUnits(50);
                    tt.setS106Units(45);
                    tt.setTotalCost(900000L);
                    break;
                }
                case 4003: {
                    tt.setTotalUnits(1);
                    tt.setS106Units(1);
                    tt.setTotalCost(124000L);
                    break;
                }
            }
            project.getCalculateGrantBlock().calculateTotals(tt);
        }
    }

    private void generateNegotiatedRouteProject(Project project) {
        Set<ProjectTenureDetails> projectTenureDetailsEntries = project.getNegotiatedGrantBlock().getTenureTypeAndUnitsEntries();

        for (ProjectTenureDetails tt : projectTenureDetailsEntries) {
            switch (tt.getTenureType().getExternalId()) {
                case 4000: {
                    tt.setTotalUnits(120);
                    tt.setSupportedUnits(10);
                    tt.setGrantRequested(200000L);
                    tt.setTotalCost(1200000L);
                    break;
                }
                case 4001: {
                    tt.setTotalUnits(34);
                    tt.setSupportedUnits(12);
                    tt.setGrantRequested(260000L);
                    tt.setTotalCost(3200000L);
                    break;
                }
                case 4002: {
                    tt.setTotalUnits(11);
                    tt.setSupportedUnits(2);
                    tt.setGrantRequested(230000L);
                    tt.setTotalCost(930000L);
                    break;
                }
                case 4003: {
                    tt.setTotalUnits(23);
                    tt.setSupportedUnits(12);
                    tt.setGrantRequested(8900000L);
                    tt.setTotalCost(1207000L);
                    break;
                }
            }
            project.getNegotiatedGrantBlock().calculateTotals(tt);
        }
    }

}
