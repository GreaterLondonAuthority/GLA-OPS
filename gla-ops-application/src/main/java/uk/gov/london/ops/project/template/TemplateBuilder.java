/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.project.template;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.london.ops.framework.enums.Requirement;
import uk.gov.london.ops.framework.environment.Environment;
import uk.gov.london.ops.project.block.ProjectBlockType;
import uk.gov.london.ops.project.implementation.di.TemplateDataInitialiser;
import uk.gov.london.ops.project.implementation.repository.QuestionRepository;
import uk.gov.london.ops.project.state.StateModel;
import uk.gov.london.ops.project.template.domain.*;
import uk.gov.london.ops.refdata.OutputConfigurationGroup;
import uk.gov.london.ops.refdata.TenureType;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static uk.gov.london.ops.refdata.TenureType.*;
import static uk.gov.london.ops.user.UserBuilder.DATA_INITIALISER_USER;

/**
 * Factory class for building Organisation and Users entities, primarily for testing.
 *
 * @author Steve Leach
 */
@Component
public class TemplateBuilder {

    Logger log = LoggerFactory.getLogger(getClass());

    public enum Directorate {
        Land, Housing
    }

    public static final String TEST_HOUSING_TEMPLATE_NAME = "Test Housing Template";
    public static final String TEST_HOUSING_TEMPLATE_WITH_MILESTONE = "Housing Milestone with status template";
    public static final String APPROVED_PROVIDER_ROUTE_TEMPLATE_NAME = "Approved Provider Route";
    public static final String DEVELOPER_LED_ROUTE_TEMPLATE_NAME = "Developer-Led Route";
    public static final String NEGOTIATED_ROUTE_TEMPLATE_NAME = "Negotiated Route";
    public static final String NEGOTIATED_ROUTE_LEGACY_TEMPLATE_NAME = "Negotiated Route - Legacy Shared Ownership";
    public static final String INDICATIVE_TEMPLATE_NAME = "Indicative";
    public static final String AFFORDABLE_HOUSING_GRANT_AGREEMENT = "Affordable Housing Grant Agreement";
    public static final String BOROUGH_INTERVENTION_AGREEMENT = "Borough Intervention Agreement";
    public static final String DEVELOPMENT_FACILITY_AGREEMENT = "Development Facility Agreement";
    public static final String HOUSING_ZONES_GENERAL = "Housing Zones General";
    public static final String MHC_REVOLVING_FUND = "MHC: Revolving Fund";
    public static final String AUTO_APPROVAL_TEMPLATE_NAME = "Auto Approval Template";
    public static final String AUTO_APPROVAL_TEMPLATE_ADD_NAME = "Auto Approval Template (Add Blocks)";
    public static final String HIDDEN_BLOCKS_TEMPLATE_NAME = "Hidden Blocks Template";
    public static final String SMALL_PROJECTS_AND_EQUIPMENT_FUND = "Small Projects and Equipment Fund";
    public static final String SMALL_PROJECTS_AND_EQUIPMENT_FUND_ALL = "Small Projects and Equipment Fund With All Grant Types";
    public static final String LEGACY_CARE_AND_SUPPORT = "Legacy Care & Support";
    public static final String LONDON_HOUSING_BANK = "London Housing Bank";
    public static final String LEGACY_MHC_15_18 = "Legacy MHC 15-18";
    public static final String LEGACY_CARE_AND_SUPPORT_TEMPLATE = "Legacy Care & Support";
    public static final String LONDON_HOUSING_BANK_TEMPLATE = "London Housing Bank";
    public static final String LEGACY_MHC_15_18_TEMPLATE = "Legacy MHC 15-18";
    public static final String MULTI_ASSESSMENT_TEMPLATE_NAME = "Minimal Multi Assessment Template";
    public static final String MOPAC_TEMPLATE_NAME = "MOPAC: London Crime Prevention Fund";
    public static final String MOVE_ON_TEMPLATE_NAME = "Move-on";
    public static final String CONTRACT_OFFER_AND_ACCEPTANCE_TEMPLATE = "Contract Offer And Acceptance Workflow";

    @Autowired
    TemplateServiceImpl templateService;
    @Autowired
    QuestionRepository questionRepository;
    @Autowired
    Environment environment;

    List<Integer> mandatoryQuestions = Arrays.asList(141);

    public Template createOrUpdateTestTemplate(String title, Directorate directorate, boolean showMilestones,
            Integer... questions) {
        return createOrUpdateTestTemplate(title, null, directorate, showMilestones, questions);
    }

    public Template createOrUpdateTestTemplate(String name, String warningMsg, Directorate directorate, boolean showMilestones,
            Integer... questions) {
        try {
            Template template = findOrCreateTemplate(name);
            template.setStatus(Template.TemplateStatus.Active);
            template.addNextBlock(ProjectBlockType.Details);
            template.setDetailsConfig(createDetailsTemplate(directorate, false));

            if (questions.length > 0) {
                template.addNextBlock(ProjectBlockType.Questions);
                addTemplateQuestions((QuestionsTemplateBlock) template.getSingleBlockByType(ProjectBlockType.Questions),
                        questions);
            }

            if (showMilestones) {
                addTemplateMilestones(template, directorate);
            }

            if (directorate.equals(Directorate.Housing)) {
                template.addNextBlock(ProjectBlockType.CalculateGrant);
                template.addNextBlock(ProjectBlockType.DesignStandards);
                template.addNextBlock(ProjectBlockType.GrantSource);
                addTenureTypes(template);
            } else if (directorate.equals(Directorate.Land)) {
                template.addNextBlock(ProjectBlockType.ProjectBudgets);
                template.addNextBlock(ProjectBlockType.Outputs);
                template.addNextBlock(ProjectBlockType.Receipts);
                template.addNextBlock(ProjectBlockType.Risks);

                OutputsTemplateBlock outputs = (OutputsTemplateBlock) template.getSingleBlockByType(ProjectBlockType.Outputs);
                OutputConfigurationGroup outputConfigurationGroup = new OutputConfigurationGroup();
                outputConfigurationGroup.setId(TemplateDataInitialiser.DEFAULT_CONFIG_GROUP_ID);
                outputs.setOutputConfigurationGroup(outputConfigurationGroup);
                outputs.setShowValueColumn(true);
                outputs.setShowOutputTypeColumn(true);
            }

            if (warningMsg != null) {
                template.setWarningMessage(warningMsg);
            }

            Template saved = templateService.save(template);

            updateProcessingRoutesWithExternalId(saved);

            saved = templateService.save(template);
            log.info("Created template {} : {}", template.getId(), template.getName());

            return saved;
        } catch (RuntimeException e) {
            log.error("Error creating test template " + name, e);
            return null;
        }
    }

    public Template findOrCreateTemplate(String name) {
        for (Template t : templateService.findAll()) {
            if (t.getName().equals(name) && t.getAuthor().equals(DATA_INITIALISER_USER)) {
                return t;
            }
        }
        return createTemplate(name);
    }

    public Template createTemplate(String name) {
        Template template = new Template();
        template.setName(name);
        template.setAuthor(DATA_INITIALISER_USER);
        template.setCreatedBy(DATA_INITIALISER_USER);
        template.setCreatedOn(environment.now());
        template.setStateModel(StateModel.ChangeControlled);
        return template;
    }

    public DetailsTemplate createDetailsTemplate(Directorate directorate, boolean hideIrrelevantFields) {
        DetailsTemplate dt = new DetailsTemplate();

        if (Directorate.Land.equals(directorate)) {
            dt.setAddressRequirement(Requirement.optional);
            dt.setBoroughRequirement(Requirement.mandatory);
            dt.setPostcodeRequirement(Requirement.optional);
            dt.setCoordsRequirement(Requirement.mandatory);
            dt.setMaincontactRequirement(Requirement.mandatory);
            dt.setMaincontactemailRequirement(Requirement.mandatory);
            dt.setSecondaryContactRequirement(Requirement.optional);
            dt.setSecondaryContactEmailRequirement(Requirement.optional);
            dt.setInterestRequirement(Requirement.optional);
            dt.setSiteOwnerRequirement(Requirement.mandatory);
            dt.setProjectManagerRequirement(Requirement.optional);
            dt.setSiteStatusRequirement(Requirement.mandatory);
            dt.setLegacyProjectCodeRequirement(Requirement.optional);
            dt.setDevelopmentLiabilityOrganisationRequirement(Requirement.optional);
            dt.setPostCompletionLiabilityOrganisationRequirement(Requirement.optional);
        } else {
            dt.setAddressRequirement(Requirement.mandatory);
            dt.setBoroughRequirement(Requirement.mandatory);
            dt.setPostcodeRequirement(Requirement.optional);
            dt.setCoordsRequirement(Requirement.mandatory);
            dt.setMaincontactRequirement(Requirement.mandatory);
            dt.setMaincontactemailRequirement(Requirement.mandatory);
            dt.setSecondaryContactRequirement(Requirement.optional);
            dt.setSecondaryContactEmailRequirement(Requirement.optional);
            dt.setInterestRequirement(Requirement.hidden);
            dt.setSiteOwnerRequirement(Requirement.hidden);
            dt.setProjectManagerRequirement(Requirement.hidden);
            dt.setSiteStatusRequirement(Requirement.hidden);
            dt.setLegacyProjectCodeRequirement(Requirement.hidden);
            dt.setDevelopmentLiabilityOrganisationRequirement(Requirement.optional);
            dt.setPostCompletionLiabilityOrganisationRequirement(Requirement.optional);
        }

        return dt;
    }

    public void addTenureTypes(Template template) {
        Set<TemplateTenureType> tts = new HashSet<>();
        int displayOrder = 0;
        TemplateTenureType type;

        if (!template.isBlockPresent(ProjectBlockType.IndicativeGrant)) {
            type = new TemplateTenureType(new TenureType(LONDON_AFFORDABLE_RENT_TENURE_ID, LONDON_AFFORDABLE_RENT));
            type.setDisplayOrder(displayOrder++);
            type.setTariffRate(25000);
            tts.add(type);
        }

        type = new TemplateTenureType(new TenureType(LONDON_LIVING_RENT_TENURE_ID, LONDON_LIVING_RENT));
        type.setDisplayOrder(displayOrder++);
        type.setTariffRate(10000);
        tts.add(type);

        type = new TemplateTenureType(new TenureType(LONDON_SHARED_OWNERSHIP_TENURE_ID, LONDON_SHARED_OWNERSHIP));
        type.setDisplayOrder(displayOrder++);
        type.setTariffRate(22000);
        tts.add(type);

        if (!template.isBlockPresent(ProjectBlockType.IndicativeGrant)) {
            type = new TemplateTenureType(new TenureType(OTHER_AFFORDABLE_TENURE_ID, OTHER_AFFORDABLE));
            type.setDisplayOrder(displayOrder++);
            type.setTariffRate(0);
            tts.add(type);
        }
        template.setTenureTypes(tts);
    }

    public void addTemplateQuestions(QuestionsTemplateBlock block, Integer... questions) {
        double count = 0.0;
        for (Integer questionId : questions) {
            TemplateQuestion tq = new TemplateQuestion();
            tq.setHelpText("This is an informative text about this question. More details: [OPS](https://ops.london.gov.uk)");
            block.getQuestions().add(tq);
            if (mandatoryQuestions.contains(questionId)) {
                tq.setRequirement(Requirement.mandatory);
            } else {
                tq.setRequirement(Requirement.optional);
            }
            Question q = questionRepository.getOne(questionId);
            if (q == null) {
                log.warn("Unable to locate question {}", questionId);
            } else {
                tq.setQuestion(q);
            }
            tq.setDisplayOrder(count);
            count += 1.0;
        }
    }

    public void updateProcessingRoutesWithExternalId(Template template) {
        Set<TemplateBlock> blocksByType = template.getBlocksByType(ProjectBlockType.Milestones);
        for (TemplateBlock templateBlock : blocksByType) {
            MilestonesTemplateBlock mtb = (MilestonesTemplateBlock) templateBlock;
            Set<ProcessingRoute> processingRoutes = mtb.getProcessingRoutes();
            for (ProcessingRoute processingRoute : processingRoutes) {
                processingRoute.setExternalId(processingRoute.getId());
            }
        }
    }

    public void addTemplateMilestones(Template template, Directorate directorate) {
        template.addNextBlock(ProjectBlockType.Milestones);

        // set up milestones for this template
        HashSet<MilestoneTemplate> milestoneSet = new HashSet<>();

        if (directorate.equals(Directorate.Housing)) {
            template.setAllowMonetaryMilestones(true);
            milestoneSet.add(createHousingMilestone(3000, "Land acquired", 0, 0, false, Requirement.mandatory));
            milestoneSet.add(createHousingMilestone(3001, "Planning granted", 1, 0, false, Requirement.mandatory));
            MilestoneTemplate milestone = createHousingMilestone(3003, "Start on site", 2, 50, true, Requirement.mandatory);
            milestoneSet.add(milestone);
            MilestoneTemplate completion = createHousingMilestone(3004, "Completion", 3, 50, true, Requirement.mandatory);
            milestoneSet.add(completion);
        } else if (directorate.equals(Directorate.Land)) {
            template.setAllowMonetaryMilestones(false);
            template.setMonetarySplitTitle("GRANT PAYMENT %");
            milestoneSet.add(createLandMilestone(5, "First stage approval", 0, 0, false, Requirement.mandatory));
            milestoneSet.add(createLandMilestone(6, "Final approval", 1, 0, false, Requirement.mandatory));
            milestoneSet.add(createLandMilestone(7, "Mayoral approval", 2, 0, false, Requirement.mandatory));
            milestoneSet.add(createLandMilestone(8, "Marketing commenced", 3, 0, false, Requirement.mandatory));
            milestoneSet.add(createLandMilestone(9, "Consideration agreed", 4, 0, false, Requirement.mandatory));
            milestoneSet.add(createLandMilestone(10, "Contract Exchanged", 5, 0, false, Requirement.mandatory));
            milestoneSet.add(createLandMilestone(11, "Contract Complete", 6, 0, false, Requirement.mandatory));
        }

        ProcessingRoute processingRoute = new ProcessingRoute();
        processingRoute.setName(ProcessingRoute.DEFAULT_PROCESSING_ROUTE_NAME);
        processingRoute.setExternalId(ProcessingRoute.DEFAULT_PROCESSING_ROUTE_EXT_ID);
        processingRoute.setDisplayOrder(1);
        processingRoute.setMilestones(milestoneSet);

        Set<ProcessingRoute> processingRoutes = new HashSet<>();
        processingRoutes.add(processingRoute);

        MilestonesTemplateBlock milestonesTemplateBlock = (MilestonesTemplateBlock) template
                .getSingleBlockByType(ProjectBlockType.Milestones);
        milestonesTemplateBlock.setAutoCalculateMilestoneState(true);
        milestonesTemplateBlock.setProcessingRoutes(processingRoutes);
    }

    private MilestoneTemplate createHousingMilestone(Integer externalId, String title, int displayOrder, int split,
            boolean keyEvent, Requirement requirement) {
        return createMilestone(externalId, title, displayOrder, true, split, keyEvent, false, requirement);
    }

    private MilestoneTemplate createLandMilestone(Integer externalId, String title, int displayOrder, int split, boolean keyEvent,
            Requirement requirement) {
        return createMilestone(externalId, title, displayOrder, false, split, keyEvent, true, requirement);
    }

    private MilestoneTemplate createMilestone(Integer externalId, String title, int displayOrder, boolean isMonetary, int split,
            boolean keyEvent, boolean naSelectable, Requirement requirement) {
        MilestoneTemplate milestoneTemplate = new MilestoneTemplate();
        milestoneTemplate.setExternalId(externalId);
        milestoneTemplate.setSummary(title);
        milestoneTemplate.setDisplayOrder(displayOrder);
        milestoneTemplate.setRequirement(requirement);
        milestoneTemplate.setMonetarySplit(split);
        milestoneTemplate.setMonetary(isMonetary);
        milestoneTemplate.setKeyEvent(keyEvent);
        milestoneTemplate.setNaSelectable(naSelectable);
        return milestoneTemplate;
    }

    @Transactional
    public Template loadJsonTemplate(Resource templateResource) throws IOException {
        ObjectMapper mapper = new ObjectMapper();

        Template template = mapper.readValue(templateResource.getInputStream(), Template.class);

        template.setCreatedBy(DATA_INITIALISER_USER);
        template.setCreatedOn(environment.now());

        templateService.save(template);

        return template;
    }
}
