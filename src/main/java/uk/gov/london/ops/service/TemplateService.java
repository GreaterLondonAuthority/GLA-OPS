/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.service;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.exception.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.london.common.skills.SkillsGrantType;
import uk.gov.london.ops.Environment;
import uk.gov.london.ops.audit.AuditService;
import uk.gov.london.ops.domain.Requirement;
import uk.gov.london.ops.framework.portableentity.PortableEntityProvider;
import uk.gov.london.ops.framework.portableentity.SanitiseComponent;
import uk.gov.london.ops.domain.project.*;
import uk.gov.london.ops.domain.project.funding.FundingBlock;
import uk.gov.london.ops.domain.project.question.Answer;
import uk.gov.london.ops.domain.project.question.ProjectQuestionsBlock;
import uk.gov.london.ops.domain.project.state.ProjectStatus;
import uk.gov.london.ops.domain.project.subcontracting.DeliverableType;
import uk.gov.london.ops.domain.template.*;
import uk.gov.london.ops.framework.exception.NotFoundException;
import uk.gov.london.ops.framework.exception.ValidationException;
import uk.gov.london.ops.payment.FinanceService;
import uk.gov.london.ops.payment.ProjectLedgerEntry;
import uk.gov.london.ops.refdata.*;
import uk.gov.london.ops.repository.*;
import uk.gov.london.ops.service.project.TemplateProjectService;
import uk.gov.london.ops.web.model.FundingBlockFlags;
import uk.gov.london.ops.web.model.FundingSpendingTypeFlags;
import uk.gov.london.ops.web.model.UpdateStateModelRequest;

import javax.persistence.EntityManager;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import static uk.gov.london.common.GlaUtils.parseInt;
import static uk.gov.london.ops.di.TemplateDataInitialiser.DEFAULT_CONFIG_GROUP_ID;
import static uk.gov.london.ops.domain.template.AnswerType.Dropdown;
import static uk.gov.london.ops.domain.template.AnswerType.YesNo;
import static uk.gov.london.ops.payment.ProjectLedgerEntry.MATCH_FUND_CATEGORY;

@Service
@Transactional
public class TemplateService implements CRUDServiceInterface<Integer, Template>, PortableEntityProvider {

    Logger log = LoggerFactory.getLogger(getClass());

    private static final int BLOCK_NAME_MAX_SIZE = 40;
    static final int MAX_NB_QUESTION_BLOCKS = 50;
    private static final List<AnswerType> CONDITIONAL_ANSWER_TYPES = Arrays.asList(YesNo, Dropdown);

    @Autowired
    TemplateRepository templateRepository;

    @Autowired
    TemplateSummaryRepository templateSummaryRepository;

    @Autowired
    TemplateDataRepository templateDataRepository;

    @Autowired
    OutputCategoryAssumptionRepository outputCategoryAssumptionRepository;

    @Autowired
    FinanceService financeService;

    @Autowired
    ProjectRepository projectRepository;

    @Autowired
    AuditService auditService;

    @Autowired
    ContractService contractService;

    @Autowired
    OutputConfigurationService outputConfigurationService;

    @Autowired
    ProgrammeService programmeService;

    @Autowired
    QuestionService questionService;

    @Autowired
    RefDataService refDataService;

    @Autowired
    TemplateProjectService templateProjectService;

    @Autowired
    UserService userService;

    @Autowired
    Environment environment;

    @Autowired
    RiskRatingService riskRatingService;

    @Autowired
    EntityManager entityManager;

    @Autowired
    JdbcTemplate jdbc;

    @Autowired
    ObjectMapper mapper;

    @Autowired
    SanitiseComponent sanitiseComponent;

    private Set<ProjectBlockType> allowedBlockTypesToAdd = new HashSet<>(Arrays.asList(
            ProjectBlockType.UnitDetails,
            ProjectBlockType.Questions,
            ProjectBlockType.Risks,
            ProjectBlockType.GrantSource,
            ProjectBlockType.Funding,
            ProjectBlockType.FundingClaims,
            ProjectBlockType.ProgressUpdates,
            ProjectBlockType.Subcontracting
    ));

    public List<Template> findAll() {
        return templateRepository.findAll();
    }

    //public List<Template> findAllByJsonNull() {
    //    return templateRepository.findAllByJsonNull();
    //}

    public String getTemplateJson(Integer id, boolean sanitise) throws IOException {
        Template template = templateRepository.findById(id).orElse(null);

        if (template == null) {
            throw new NotFoundException();
        }

        if (Template.TemplateStatus.Draft.equals(template.getStatus())) {
            return template.getJson();
        }

        return getTemplateAsJsonString(template, sanitise);
    }

    public String getTemplateAsJsonString(Template template, boolean sanitise) throws JsonProcessingException, IOException {
        Template sanitised = null;
        if (sanitise) {
            // call to fully initialise template  - don't remove or will get lazy load issues.
            mapper.writeValueAsString(template);
            entityManager.detach(template);
            List<Class> excludedClasses = Arrays.asList(
                    Contract.class, OutputConfigurationGroup.class, MarketType.class, Question.class, Template.class, TenureType.class);

            sanitised = (Template) sanitiseComponent.sanitise(template, excludedClasses);
            sanitised.setName("TBC");
            sanitised.setAuthor("TBC");
            sanitised.setCreatedBy(null);
            sanitised.setCreatedOn(null);

        }

        //return template.getJson() != null ? template.getJson() : mapper.writeValueAsString(template);
        return mapper.writeValueAsString(sanitise ? sanitised : template);
    }

    public Template find(Integer id) {
        Template template = templateRepository.findById(id).orElse(null);

        if (template == null) {
            throw new NotFoundException("Could not find template with ID " + id);
        }

        return template;
    }

    public Template sanitise(Template template) {
        sanitiseIds(template);
        template.setName("TBC");
        template.setAuthor("TBC");
        template.setCreatedBy(null);
        template.setCreatedOn(null);
        return template;
    }

    void sanitiseIds(Object object) {
        if (object != null) {
            sanitiseIds(object, object.getClass().getDeclaredFields());
            sanitiseIds(object, object.getClass().getSuperclass().getDeclaredFields());
        }
    }

    void sanitiseIds(Object object, Field[] fields) {
        for (Field field : fields) {
            if (!java.lang.reflect.Modifier.isStatic(field.getModifiers()) && field.getAnnotationsByType(JsonIgnore.class).length == 0) {
                try {
                    field.setAccessible(true);
                    if (field.getName().equals("id") && !skipIdSanitisation(object)) {
                        field.set(object, null);
                    } else if (field.get(object) instanceof Collection) {
                        Collection collection = (Collection) field.get(object);
                        for (Object item : collection) {
                            sanitiseIds(item);
                        }
                    } else if (field.get(object) != null
                            && field.get(object).getClass().getName().startsWith("uk.gov.london.ops")
                            // TODO : replace below with !skipIdSanitisation(field.get(object))
                            && !(field.get(object) instanceof Contract)
                            && !(field.get(object) instanceof OutputConfigurationGroup)
                            && !(field.get(object) instanceof MarketType)
                            && !(field.get(object) instanceof Question)
                            && !(field.get(object) instanceof Template)
                            && !(field.get(object) instanceof TenureType)) {
                        sanitiseIds(field.get(object));
                    }
                } catch (IllegalAccessException e) {
                    log.error("could not nullify field on class " + object.getClass().getName(), e);
                }
            }
        }
    }

    private boolean skipIdSanitisation(Object object) {
        return object instanceof Contract
                || object instanceof FundingClaimCategory
                || object instanceof MarketType
                || object instanceof OutputConfigurationGroup
                || object instanceof Question
                || object instanceof TenureType;
    }

    public Template findByName(String name) {
        return templateRepository.findByName(name);
    }

    public Template create(String templateJson) throws IOException {
        Template template = null;
        try {
            template = mapper.readValue(templateJson, Template.class);
        } catch (IOException e) {
            throw new ValidationException("Unable to parse JSON Request: " + e.getMessage());
        }

        if (template.getId() != null) {
            throw new ValidationException("cannot edit existing template!");
        }

        if (template.getStateModel() == null) {
            throw new ValidationException("No state model is present!");
        }
        template.setCreatedBy(userService.currentUser().getUsername());
        template.setCreatedOn(environment.now());

        return save(template);
    }

    public Template save(Template template) {
        validateTemplate(template);

        // if any blocks are not numbered
        boolean anyMissingDisplayOrders = false;
        for (TemplateBlock templateBlock : template.getBlocksEnabled()) {
            if (templateBlock.getDisplayOrder() == null) {
                anyMissingDisplayOrders = true;
                if (templateBlock.getBlockDisplayName() != null && templateBlock.getBlockDisplayName().length() > 40) {
                    throw new ValidationException("Block title cannot contain more than 40 characters");
                }

            }
        }

        // init tenure template types and associate market types
        if (template.getTenureTypes() != null) {
            for (TemplateTenureType templateTenureType : template.getTenureTypes()) {
                for (TemplateTenureTypeMarketType templateTenureTypeMarketType : templateTenureType.getTemplateTenureTypeMarketTypes()) {
                    templateTenureTypeMarketType.setTemplateTenureType(templateTenureType);
                    Integer id = templateTenureTypeMarketType.getMarketType().getId();
                    MarketType inited = refDataService.getMarketType(id);
                    if (inited == null) {
                        throw new ValidationException("Market Type with ID" + id + " not found");
                    }
                    templateTenureTypeMarketType.setMarketType(inited);
                }
            }
        }


        // then renumber all blocks
        if (anyMissingDisplayOrders) {
            int currentMax = 1;
            for (TemplateBlock templateBlock : template.getBlocksEnabled()) {
                templateBlock.setDisplayOrder(currentMax++);
            }
        }
        if (template.isBlockPresent(ProjectBlockType.Outputs)) {
            Set<TemplateBlock> blocksByType = template.getBlocksByType(ProjectBlockType.Outputs);
            for (TemplateBlock templateBlock : blocksByType) {
                OutputsTemplateBlock outputsTemplateBlock = (OutputsTemplateBlock) templateBlock;
                if (outputsTemplateBlock.getOutputConfigurationGroup() == null) {
                    OutputConfigurationGroup group = outputConfigurationService.getGroup(DEFAULT_CONFIG_GROUP_ID);
                    outputsTemplateBlock.setOutputConfigurationGroup(group);
                }
            }

        }
        if (template.isBlockPresent(ProjectBlockType.Milestones)) {

            MilestonesTemplateBlock milestonesTemplateBlock = (MilestonesTemplateBlock) template.getSingleBlockByType(ProjectBlockType.Milestones);
            for (MilestoneTemplate milestoneTemplate : milestonesTemplateBlock.getAllMilestones()) {
                // milestones are monetary by default
                if (milestoneTemplate.getMonetary() == null) {
                    milestoneTemplate.setMonetary(true);
                }
            }
        }
        if (template.isBlockPresent(ProjectBlockType.Questions)) {
            Set<TemplateBlock> blocksByType = template.getBlocksByType(ProjectBlockType.Questions);
            if (blocksByType.size() > MAX_NB_QUESTION_BLOCKS) {
                throw new ValidationException("Cannot create a template with more than " + MAX_NB_QUESTION_BLOCKS + " additional question blocks");
            }
            for (TemplateBlock tb : blocksByType) {
                QuestionsTemplateBlock questionBlock = (QuestionsTemplateBlock) tb;

                int questionCount = questionBlock.getQuestions().size();
                int uniqueQuestions = questionBlock.getQuestions().stream().map(t -> t.getQuestion().getId()).collect(Collectors.toSet()).size();
                if (questionCount != uniqueQuestions) {
                    throw new ValidationException("Cannot create a template with duplicated questions");
                }

                createOrLoadAndValidateQuestions(questionBlock.getQuestions());

                for (TemplateQuestion question : questionBlock.getQuestions()) {
                    if (question.getParentId() != null) {
                        TemplateQuestion parentQuestion = questionBlock.getQuestionById(question.getParentId());
                        if (!CONDITIONAL_ANSWER_TYPES.contains(parentQuestion.getQuestion().getAnswerType())) {
                            throw new ValidationException("conditional questions can only be on YesNo or Dropdown!");
                        }
                    }
                }
            }
        }

        // update the blocks json before saving the blocks for the first time
        updateTemplateBlocksJson(template);

        // generates any IDs first
        templateRepository.save(template);

        if (template.isBlockPresent(ProjectBlockType.Milestones)) {

            MilestonesTemplateBlock milestonesTemplateBlock = (MilestonesTemplateBlock) template.getSingleBlockByType(ProjectBlockType.Milestones);
            Set<ProcessingRoute> processingRoutes = milestonesTemplateBlock.getProcessingRoutes();
            for (ProcessingRoute processingRoute : processingRoutes) {
                if (processingRoute.getExternalId() == null || processingRoute.getExternalId() == 0) {
                    processingRoute.setExternalId(processingRoute.getId());
                }
            }
        }

        Template savedTemplate = templateRepository.save(template);

        updateJson(template);

        return savedTemplate;
    }

    private void updateTemplateBlocksJson(Template template) {
        for (TemplateBlock templateBlock : template.getBlocksEnabled()) {
            try {
                if (ProjectBlockType.FundingClaims.equals(templateBlock.getBlock())) { // TODO : replace this with something like "isJsonBlockData" ?
                    templateBlock.setBlockData(mapper.writeValueAsString(templateBlock));
                }
            } catch (JsonProcessingException e) {
                log.error("Could not serialise template block to JSON, type=" + templateBlock.getBlock(), e);
            }
        }
    }

    public void updateJson(Template template) {
        try {
            String json = mapper.writeValueAsString(template);
            jdbc.update("UPDATE template SET json = ? WHERE id = ?", json, template.getId());
        } catch (JsonProcessingException e) {
            log.error("Could not serialise template to JSON, id=" + template.getId(), e);
        }
    }

    public Template cloneTemplate(Template template, String newName) {

        String username = userService.currentUser().getUsername();

        if (StringUtils.isEmpty(newName)) {
            throw new ValidationException("Cloned template name must be specified");
        }

        if (newName.equalsIgnoreCase(template.getName())) {
            throw new ValidationException("Cloned template name must be different from the original template name");
        }

        Template clone = template.cloneTemplate(newName);
        clone.setAuthor(username);
        clone.setCreatedBy(username);
        clone.setCreatedOn(environment.now());
        save(clone);
        return clone;

    }

    void validateTemplate(Template template) {

        if (template.getNumberOfProjectAllowedPerOrg() != null && template.getNumberOfProjectAllowedPerOrg() < 1) {
            throw new ValidationException("Maximum number of projects needs to be 1 or more or empty (Indicating no limit)");
        }

        for (TemplateBlock tb : template.getBlocksEnabled()) {
            if (tb.getBlockDisplayName() != null && tb.getBlockDisplayName().length() > BLOCK_NAME_MAX_SIZE) {
                throw new ValidationException("Cannot have a block name with a name longer thant " + BLOCK_NAME_MAX_SIZE + "! (" + tb.getBlockDisplayName() + ")");
            }

            if (tb.getDisplayOrder() == null) {
                throw new ValidationException("Display order mandatory for block " + tb.getBlockDisplayName());
            }
        }

        if (CollectionUtils.isNotEmpty(template.getTenureTypes())) {
            for (TemplateTenureType templateTenureType : template.getTenureTypes()) {
                if (templateTenureType.getTenureType() == null || templateTenureType.getTenureType().getId() == null) {
                    throw new ValidationException("need to specify an external ID in the template tenure type!");
                }

                Integer id = templateTenureType.getTenureType().getId();
                TenureType tenureType = refDataService.getTenureType(id);

                if (tenureType == null) {
                    throw new ValidationException("Tenure Type used by this template is not recognised, create it prior to using in template: " + templateTenureType.getTenureType().getName());
                }

                boolean defaultTenure = tenureType.getMarketTypes() != null && !tenureType.getMarketTypes().isEmpty();
                boolean templateTenureTypes = templateTenureType.getTemplateTenureTypeMarketTypes() != null
                        && !templateTenureType.getTemplateTenureTypeMarketTypes().isEmpty();

                if (!defaultTenure && !templateTenureTypes) {
                    throw new ValidationException("Template " + template.getName() +
                            " No market types specified for new tenure type with display order of: " +
                            templateTenureType.getDisplayOrder());
                }
            }
        }

        if (template.isBlockPresent(ProjectBlockType.Funding)) {
            FundingTemplateBlock fundingBlock = (FundingTemplateBlock) template.getSingleBlockByType(ProjectBlockType.Funding);

            boolean catsEnabled = fundingBlock.getShowCategories() == null ? false : fundingBlock.getShowCategories();
            boolean milestonesEnabled = fundingBlock.getShowMilestones() == null ? false : fundingBlock.getShowMilestones();
            if (catsEnabled && milestonesEnabled) {
                throw new ValidationException("Unable to configure template with both categories and milestones configured.");
            }

            if (!catsEnabled && !milestonesEnabled) {
                throw new ValidationException("Unable to configure template with neither categories nor milestones configured.");
            }

            if (catsEnabled && fundingBlock.getCategoriesExternalId() == null) {
                throw new ValidationException("Unable to configure template with categories enabled but no cat list specified.");

            }

            if (!Boolean.TRUE.equals(fundingBlock.getShowCapitalGLAFunding()) &&
                    !Boolean.TRUE.equals(fundingBlock.getShowRevenueGLAFunding()) &&
                    !Boolean.TRUE.equals(fundingBlock.getShowCapitalOtherFunding()) &&
                    !Boolean.TRUE.equals(fundingBlock.getShowRevenueOtherFunding()) && fundingBlock.getFundingSpendType() == null ) {
                throw new ValidationException("Template " + template.getName() +
                        " has a funding block but no funding type is enabled.");
            }
        }

        if (template.isBlockPresent(ProjectBlockType.Details)) {
            DetailsTemplate detailsConfig = template.getDetailsConfig();

            if (detailsConfig == null) {
                throw new ValidationException("DetailsTemplate mandatory if project details added.");
            }

            Requirement boroughRequirement = detailsConfig.getBoroughRequirement();
            Requirement wardRequirement = detailsConfig.getWardIdRequirement();
            if (boroughRequirement != null) {
                boolean valid = true;
                if (wardRequirement != null) {
                    switch (wardRequirement) {
                        case mandatory:
                            if (!boroughRequirement.equals(Requirement.mandatory)) {
                                valid = false;
                            }
                            break;
                        case optional:
                            if (boroughRequirement.equals(Requirement.hidden)) {
                                valid = false;
                            }
                            break;
                        case hidden:
                            break;
                    }
                }
                if (!valid) {
                    throw new ValidationException("Cannot create or update a template where ward requirement is more restrictive than Borough requirement");
                }
            }
        }

        if (template.isBlockPresent(ProjectBlockType.LearningGrant)) {
            LearningGrantTemplateBlock learningGrantTemplateBlock = (LearningGrantTemplateBlock) template.getSingleBlockByType(ProjectBlockType.LearningGrant);
            if (learningGrantTemplateBlock.getGrantType() == null) {
                List<String> typeNames = Arrays.stream(SkillsGrantType.values()).map(SkillsGrantType::name).collect(Collectors.toList());
                throw new ValidationException("Template " + template.getName() + " has a learning grant block but no grantType is set. Valid values are " + String.join(", ", typeNames));
            }
        }

        // Validate the flags for columns for an output block
        Set<OutputsTemplateBlock> outputsBlocks = (Set) template.getBlocksByType(ProjectBlockType.Outputs);
        if (null != outputsBlocks || !outputsBlocks.isEmpty()) {
            for (OutputsTemplateBlock outputBlock : outputsBlocks) {
                if (outputBlock.getShowValueColumn() == null) {
                    throw new ValidationException("Template " + template.getName()
                            + " has an outputs block but no showValueColumn is set. Valid values are true or false.");
                }

                if (outputBlock.getShowOutputTypeColumn() == null) {
                    throw new ValidationException("Template " + template.getName()
                            + " has an outputs block but no showOutputTypeColumn is set. Valid values are true or false.");
                }
            }
        }
    }

    /**
     * Determines whether to load or crete template questions
     */
    private void createOrLoadAndValidateQuestions(Set<TemplateQuestion> questions) {
        if (questions != null) {
            for (TemplateQuestion templateQuestion : questions) {
                Question providedQuestion = templateQuestion.getQuestion();
                if (providedQuestion.getId() != null) {
                    Question questionFromDB = questionService.findById(providedQuestion.getId());
                    if (questionFromDB == null) {
                        HashSet<AnswerOption> options = new HashSet<>();
                        if (templateQuestion.getQuestion().getAnswerOptions() != null) {
                            options.addAll(templateQuestion.getQuestion().getAnswerOptions());
                        }
                        questionFromDB = questionService.createQuestion(
                                providedQuestion.getId(),
                                providedQuestion.getText(),
                                providedQuestion.getAnswerType(),
                                options,
                                providedQuestion.getMaxLength(),
                                providedQuestion.getQuantity(),
                                null,  providedQuestion.getMaxUploadSizeInMb());
                    } else {
                        validateProvidedQuestionAgainstExisting(providedQuestion, questionFromDB);
                    }
                    templateQuestion.setQuestion(questionFromDB);
                } else {
                    throw new ValidationException("Question IDs must be provided when uploading templates.");
                }
            }
        }
    }

    void validateProvidedQuestionAgainstExisting(Question providedQuestion, Question existingQuestion) {
        if (StringUtils.isNotEmpty(providedQuestion.getText()) && !StringUtils.equalsIgnoreCase(existingQuestion.getText().trim(), providedQuestion.getText().trim())) {
            throw new ValidationException(String.format("provided text for question %d does not match the existing one!", providedQuestion.getId()));
        }

        if (providedQuestion.getAnswerType() != null && !Objects.equals(existingQuestion.getAnswerType(), providedQuestion.getAnswerType())) {
            throw new ValidationException(String.format("provided answer type for question %d does not match the existing one!", providedQuestion.getId()));
        }
    }

    public void updateWarningMessage(Integer id, String message) {

        Template template = templateRepository.findById(id).orElse(null);
        if (template == null) {
            throw new ValidationException("Unable to find requested template");
        }
        template.setWarningMessage(message);
        templateRepository.save(template);

    }


    private Page<TemplateSummary> getTemplatesByTemplateText(String templateText, List<Template.TemplateStatus> selectedTemplateStatuses, Pageable pageable) {
        templateText = templateText == null ? "" : templateText;
        Integer templateId = parseInt(templateText);
        if (selectedTemplateStatuses == null) {
            return templateSummaryRepository.findAllByIdOrNameContainingIgnoreCase(templateId, templateText, pageable);
        } else {
            return templateSummaryRepository.findAllByIdOrNameContainingIgnoreCaseAndTemplateStatusIn(templateId, templateText, selectedTemplateStatuses, pageable);
        }
    }

    private Page<TemplateSummary> getTemplatesByProgrammeText(String programmeText, Pageable pageable) {
        programmeText = programmeText == null ? "" : programmeText.toLowerCase();
        Integer programmeId = parseInt(programmeText) == null ? -1 : parseInt(programmeText);
        Set<Integer> templateIds = templateSummaryRepository.findAllByProgrammeIdOrName(programmeId, programmeText);
        return templateSummaryRepository.findByIdIn(templateIds, pageable);
    }

    public void delete(Integer id) {
        try {
            templateRepository.deleteById(id);
        } catch (DataIntegrityViolationException e) {
            if (e.getCause() instanceof ConstraintViolationException) {
                ConstraintViolationException cause = (ConstraintViolationException) e.getCause();
                throw new RuntimeException(String.format("Referential integrity error deleting template %d : %s",
                        id, cause.getConstraintName()));
            } else {
                throw e;
            }
        }
    }

    @Async
    @Transactional

    public Future<Template> addBlock(final int templateId, final TemplateBlock block) throws InterruptedException {
        if (!getAllowedBlockTypesToAdd().contains(block.getBlock())) {
            throw new ValidationException("Not allowed block type " + block.getBlock().name());
        }

        final Template template = templateRepository.findById(templateId).orElse(null);
        if (template == null) {
            throw new ValidationException("Not found template with id " + templateId);
        }

        if (ProjectBlockType.Questions.equals(block.getBlock())) {
            createOrLoadAndValidateQuestions(((QuestionsTemplateBlock) block).getQuestions());
//            setQuestionBlockData((QuestionsTemplateBlock) block);
        }

        if (template.getCloneOfTemplateId() != null) {
            template.setCloneModified(true);
        }

        template.addNextBlock(block);
        Template savedTemplate = save(template);
        Set<TemplateBlock> savedBlockSet = template.getBlocksByType(block.getBlock());
        TemplateBlock savedBlock = savedBlockSet.stream()
                .sorted(((o1, o2) -> o1.getId().equals(o2.getId()) ? 0 : o1.getId() > o2.getId() ? -1 : 1))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Block wasn't added"));
        templateProjectService.addBlockToProjectsByTemplate(template, savedBlock);
        return new AsyncResult<>(savedTemplate);
    }

//    private void setQuestionBlockData(QuestionsTemplateBlock block) {
//        final QuestionsTemplateBlock qtb = block;
//        final Set<TemplateQuestion> templateQuestionSet = new HashSet<>();
//        final Set<String> externalKeysUsedSet = new HashSet<>();
//        for (final TemplateQuestion tq : qtb.getQuestions()) {
//            String externalKey = tq.getQuestion().getExternalKey();
//            if (externalKeysUsedSet.contains(externalKey)) {
//                externalKey = "";
//            } else {
//                externalKeysUsedSet.add(externalKey);
//            }
//            final Question question = findOrCreateQuestion(
//                    tq.getQuestion().getId(),
//                    externalKey,
//                    tq.getQuestion().getText(),
//                    tq.getQuestion().getAnswerType(),
//                    tq.getQuestion().getMaxLength(),
//                    tq.getQuestion().getQuantity(),
//                    tq.getQuestion().getAnswerOptions());
//            final TemplateQuestion newTemplateQuestion = tq.copy();
//            templateQuestionSet.add(newTemplateQuestion);
//        }
//        qtb.setQuestions(templateQuestionSet);
//    }
//
//
//    public Question findOrCreateQuestion(final Integer id,
//                                         final String externalKey,
//                                         final String text,
//                                         final AnswerType answerType, final Integer maxLength,
//                                         final Integer quantity,
//                                         final Set<AnswerOption> answerOptions) {
//        final Question savedQuestion = externalKey != null && !"".equals(externalKey) ?
//                questionService.getByExternalKey(externalKey) : null;
//        if (savedQuestion != null) {
//            return savedQuestion;
//        } else {
//            final Question newQuestion = questionService.createQuestion(
//                    id,
//                    text,
//                    answerType,
//                    answerOptions, maxLength, quantity,
//                    externalKey);
//            questionService.flush();
//            return newQuestion;
//        }
//    }

    // TODO : fix integration test to make this async
//    @Async
    @Transactional
    public Future<Template> addInternalBlock(int templateId, InternalTemplateBlock block) {
        Template template = find(templateId);
        template.getInternalBlocks().add(block);
        save(template);
        templateProjectService.addInternalBlock(template, block);
        return new AsyncResult<>(template);
    }

    public void setTemplateContract(Integer templateId, Integer contractId) {
        Template template = find(templateId);
        Contract contract = contractService.find(contractId);
        template.setContract(contract);
        save(template);
    }

    public void removeTemplateContract(Integer templateId) {
        Template template = find(templateId);
        template.setContract(null);
        save(template);
    }

    public Set<ProjectBlockType> getAllowedBlockTypesToAdd() {
        return allowedBlockTypesToAdd;
    }

    public void addSection(Integer templateId, int blockDisplayOrder, QuestionsBlockSection section) {
        Template template = find(templateId);

        QuestionsTemplateBlock block = (QuestionsTemplateBlock) template.getSingleBlockByTypeAndDisplayOrder(ProjectBlockType.Questions, blockDisplayOrder);
        if (block == null) {
            throw new NotFoundException("could not find questions block with display order " + blockDisplayOrder);
        }

        if (section == null) {
            throw new ValidationException("Section wasn't specified.");
        } else {
            if (section.getText() == null || section.getExternalId() == null) {
                throw new ValidationException("Text and External ID must be specified when adding a new section.");
            }
        }

        for (QuestionsBlockSection existingSection : block.getSections()) {
            if (existingSection.getExternalId().equals(section.getExternalId())) {
                throw new ValidationException("Unable to add new section with existing external ID");
            }
        }

        if (section.getDisplayOrder() == null) {
            double currentMax = block.getSections().stream().mapToDouble(QuestionsBlockSection::getDisplayOrder).max().orElse(0.0);

            section.setDisplayOrder(currentMax + 1.0);
        }
        block.getSections().add(section);
        this.save(template);

        templateProjectService.addSection(template, blockDisplayOrder, section);
    }

    public void addQuestion(Integer templateId, int blockDisplayOrder, TemplateQuestion newTemplateQuestion) {
        Template template = find(templateId);

        TemplateBlock block = template.getSingleBlockByTypeAndDisplayOrder(ProjectBlockType.Questions, blockDisplayOrder);
        if (block == null) {
            throw new NotFoundException("could not find questions block with display order " + blockDisplayOrder);
        }

        double maxDisplayOrder = 0.0;
        QuestionsTemplateBlock questionsBlock = (QuestionsTemplateBlock) block;
        for (TemplateQuestion tq : questionsBlock.getQuestions()) {
            if (tq.getQuestion().getId().equals(newTemplateQuestion.getQuestion().getId())) {
                throw new ValidationException("question " + newTemplateQuestion.getQuestion().getId() + " already exists in template!");
            }
            maxDisplayOrder = Math.max(maxDisplayOrder, tq.getDisplayOrder());
        }


        if (newTemplateQuestion.getSectionId() != null) {
            if (questionsBlock.getSections().stream().noneMatch(m -> m.getExternalId().equals(newTemplateQuestion.getSectionId()))) {
                throw new ValidationException("Unable to create question as associated section was not found.");
            }
        }

        // we want to force the question to be optional and to be displayed last
        Double newDisplayOrderFromRequest = newTemplateQuestion.getDisplayOrder();
        newTemplateQuestion.setDisplayOrder(newDisplayOrderFromRequest != null ? newDisplayOrderFromRequest : maxDisplayOrder + 1.0);

        if (newTemplateQuestion.getRequirement() == null) {
            newTemplateQuestion.setRequirement(Requirement.optional);
        }
        questionsBlock.getQuestions().add(newTemplateQuestion);


        template = save(template);

        questionsBlock = (QuestionsTemplateBlock) template.getSingleBlockByTypeAndDisplayOrder(ProjectBlockType.Questions, blockDisplayOrder);
        TemplateQuestion savedTemplateQuestion = questionsBlock.getQuestionById(newTemplateQuestion.getQuestion().getId());

        templateProjectService.addQuestion(template, blockDisplayOrder, savedTemplateQuestion);
    }

    public void updateQuestionRequirement(Integer templateId, Integer blockDisplayOrder, Integer questionId, Requirement requirement) {
        Template template = find(templateId);
        TemplateQuestion question = getTemplateQuestion(template, blockDisplayOrder, questionId);
        question.setRequirement(requirement);
        save(template);
    }

    public void updateQuestionDisplayOrder(Integer templateId, Integer blockDisplayOrder, Integer questionId, Double newDisplayOrder) {
        Template template = find(templateId);
        TemplateQuestion question = getTemplateQuestion(template, blockDisplayOrder, questionId);
        question.setDisplayOrder(newDisplayOrder);
        save(template);
    }

    public void addQuestionToSection(Integer templateId, Integer blockDisplayOrder, Integer questionId, Integer sectionId) {
        Template template = find(templateId);
        TemplateQuestion question = getTemplateQuestion(template, blockDisplayOrder, questionId);
        question.setSectionId(sectionId);
        save(template);
    }

    private TemplateQuestion getTemplateQuestion(Template template, Integer blockDisplayOrder, Integer questionId) {

        TemplateBlock block = template.getSingleBlockByTypeAndDisplayOrder(ProjectBlockType.Questions, blockDisplayOrder);
        if (block == null) {
            throw new NotFoundException("could not find questions block with display order " + blockDisplayOrder);
        }

        QuestionsTemplateBlock questionsBlock = (QuestionsTemplateBlock) block;

        TemplateQuestion question = questionsBlock.getQuestionById(questionId);
        if (question == null) {
            throw new NotFoundException("could not find questions with id " + questionId);
        }

        return question;
    }

    public void removeQuestion(Integer templateId, Integer blockDisplayOrder, Integer questionId) {
        Template template = find(templateId);

        TemplateBlock block = template.getSingleBlockByTypeAndDisplayOrder(ProjectBlockType.Questions, blockDisplayOrder);
        if (block == null) {
            throw new NotFoundException("could not find questions block with display order " + blockDisplayOrder);
        }

        QuestionsTemplateBlock questionsBlock = (QuestionsTemplateBlock) block;

        TemplateQuestion question = questionsBlock.getQuestionById(questionId);
        if (question == null) {
            throw new NotFoundException("could not find questions with id " + questionId);
        }

        questionsBlock.getQuestions().remove(question);

        save(template);

        templateProjectService.removeQuestion(template, blockDisplayOrder, questionId);
    }

    public void updateAssociatedProjectsEnabled(Integer templateId, Boolean enabled) {
        Template template = find(templateId);
        template.setAssociatedProjectsEnabled(enabled);
        save(template);

        templateProjectService.updateAssociatedProjectsEnabled(template, enabled);
    }

    public void updateMilestoneDescriptionHintText(Integer templateId, String text) {
        Template template = find(templateId);
        template.setMilestoneDescriptionHintText("null".equals(text) ? null : text);
        save(template);
    }

    public void updateStartOnSiteRestrictionText(Integer templateId, String text) {
        Template template = find(templateId);
        template.setStartOnSiteRestrictionText("null".equals(text) ? null : text);
        save(template);
    }

    public void updateMilestoneType(Integer templateId, Template.MilestoneType milestoneType) {
        Template template = find(templateId);
        template.setMilestoneType(milestoneType);
        save(template);
    }

    public TemplateData getTemplateData(Integer templateId) {
        TemplateData data = templateDataRepository.getOne(templateId);
        log.debug("Found {}", data.getName());
        return data;
    }

    public String getTemplateDataJson(Integer templateId) throws JsonProcessingException {
        return mapper.writeValueAsString(getTemplateData(templateId));
    }

    public void updateGrantTotalText(Integer templateId, Integer blockDisplayOrder, String text) {
        Template template = find(templateId);

        TemplateBlock block = template.getSingleBlockByTypeAndDisplayOrder(ProjectBlockType.GrantSource, blockDisplayOrder);
        if (block == null) {
            throw new NotFoundException("could not find grant source block with display order " + blockDisplayOrder);
        }

        GrantSourceTemplateBlock grantBlock = (GrantSourceTemplateBlock) block;
        grantBlock.setGrantTotalText(text);

        save(template);
    }

    public void updateMilestoneDescriptionEnabled(Integer templateId, Integer blockDisplayOrder, boolean enabled) {
        Template template = find(templateId);

        TemplateBlock block = template.getSingleBlockByTypeAndDisplayOrder(ProjectBlockType.Milestones, blockDisplayOrder);
        if (block == null) {
            throw new NotFoundException("could not find milestones block with display order " + blockDisplayOrder);
        }

        MilestonesTemplateBlock milestonesBlock = (MilestonesTemplateBlock) block;
        milestonesBlock.setDescriptionEnabled(enabled);

        save(template);

        templateProjectService.updateMilestoneDescriptionEnabled(template, blockDisplayOrder, enabled);
    }

    public void updateMilestoneAllowableEvidenceDocuments(Integer templateId, Integer blockDisplayOrder, Integer maxCount) {
        Template template = find(templateId);
        MilestonesTemplateBlock milestonesTemplateBlock = getMilestonesTemplateBlock(template, blockDisplayOrder);
        updateMilestoneAllowableEvidenceDocuments(template, milestonesTemplateBlock, maxCount, milestonesTemplateBlock.getEvidenceApplicability());
    }

    public void updateMilestoneAllowableEvidenceDocuments(Integer templateId, Integer blockDisplayOrder, MilestonesTemplateBlock.EvidenceApplicability evidenceApplicability) {
        Template template = find(templateId);
        MilestonesTemplateBlock milestonesTemplateBlock = getMilestonesTemplateBlock(template, blockDisplayOrder);
        updateMilestoneAllowableEvidenceDocuments(template, milestonesTemplateBlock, milestonesTemplateBlock.getMaxEvidenceAttachments(), evidenceApplicability);
    }

    private MilestonesTemplateBlock getMilestonesTemplateBlock(Template template, Integer blockDisplayOrder) {
        TemplateBlock block = template.getSingleBlockByTypeAndDisplayOrder(ProjectBlockType.Milestones, blockDisplayOrder);
        if (block == null) {
            throw new NotFoundException("could not find milestones block with display order " + blockDisplayOrder);
        }

        return (MilestonesTemplateBlock) block;
    }

    private void updateMilestoneAllowableEvidenceDocuments(Template template, MilestonesTemplateBlock milestonesBlock, Integer maxCount, MilestonesTemplateBlock.EvidenceApplicability evidenceApplicability) {


        Integer maxEvidenceAttachments = milestonesBlock.getMaxEvidenceAttachments();
        if (maxEvidenceAttachments != null && maxCount < maxEvidenceAttachments) {
            throw new ValidationException("Unable to reduce the number of permitted evidence attachments.");
        }

        MilestonesTemplateBlock.EvidenceApplicability existingApplicability = milestonesBlock.getEvidenceApplicability();
        if (existingApplicability != null && !existingApplicability.equals(MilestonesTemplateBlock.EvidenceApplicability.NOT_APPLICABLE) && !existingApplicability.equals(evidenceApplicability)) {
            throw new ValidationException("Unable to change the evidence rules.");

        }

        milestonesBlock.setMaxEvidenceAttachments(maxCount);
        milestonesBlock.setEvidenceApplicability(evidenceApplicability);
        save(template);

        templateProjectService.updateMilestoneEvidentialStatus(template, milestonesBlock.getDisplayOrder(), maxCount, evidenceApplicability);
    }

    public void updateMilestoneNaSelectable(Integer templateId, Integer blockDisplayOrder, Integer processingRouteId, Integer milestoneId, Boolean naSelectable) {
        Template template = find(templateId);

        TemplateBlock block = template.getSingleBlockByTypeAndDisplayOrder(ProjectBlockType.Milestones, blockDisplayOrder);
        if (block == null) {
            throw new NotFoundException("could not find milestones block with display order " + blockDisplayOrder);
        }

        MilestonesTemplateBlock milestonesBlock = (MilestonesTemplateBlock) block;
        ProcessingRoute processingRoute = milestonesBlock.getProcessingRoute(processingRouteId);
        if (processingRoute == null) {
            throw new NotFoundException("could not find processing route with id " + processingRouteId);
        }

        MilestoneTemplate milestoneTemplate = processingRoute.getMilestoneByExternalId(milestoneId);
        if (milestoneTemplate == null) {
            throw new NotFoundException("could not find template milestone with external id " + milestoneId);
        }
        milestoneTemplate.setNaSelectable(naSelectable);

        save(template);

        templateProjectService.updateMilestoneNaSelectable(template, blockDisplayOrder, processingRouteId, milestoneId, naSelectable);
    }

    public void updateBlockDisplayName(Integer templateId, Integer blockDisplayOrder, ProjectBlockType blockType, String oldName, String newName) {
        Template template = find(templateId);

        TemplateBlock block = template.getSingleBlockByTypeAndDisplayOrder(blockType, blockDisplayOrder);
        if (block == null) {
            throw new NotFoundException("could not find block with type " + blockType + " and display order " + blockDisplayOrder);
        }

        if (block.getBlockDisplayName() != null && !StringUtils.equals(oldName, block.getBlockDisplayName())) {
            throw new ValidationException("old name does not match the current name: '" + block.getBlockDisplayName() + "'!");
        }

        block.setBlockDisplayName(newName);

        save(template);

        templateProjectService.updateBlockDisplayName(template, blockDisplayOrder, blockType, newName);
    }

    public void replaceQuestion(Integer templateId, Integer blockDisplayOrder, Integer oldQuestionId, Integer newQuestionId) {
        Template template = find(templateId);

        TemplateBlock block = template.getSingleBlockByTypeAndDisplayOrder(ProjectBlockType.Questions, blockDisplayOrder);
        if (block == null) {
            throw new NotFoundException("could not find questions block with display order " + blockDisplayOrder);
        }

        QuestionsTemplateBlock questionsBlock = (QuestionsTemplateBlock) block;

        TemplateQuestion templateQuestion = questionsBlock.getQuestionById(oldQuestionId);
        if (templateQuestion == null) {
            throw new NotFoundException("could not find questions with id " + oldQuestionId);
        }

        Question oldQuestion = questionService.getById(oldQuestionId);
        Question newQuestion = questionService.getById(newQuestionId);

        if (!oldQuestion.canBeReplacedWith(newQuestion)) {
            throw new ValidationException("questions incompatible, cannot replace");
        }

        templateQuestion.setQuestion(newQuestion);
        for (TemplateQuestion tq : questionsBlock.getQuestionsByParentId(oldQuestionId)) {
            tq.setParentId(newQuestionId);
        }

        save(template);

        templateProjectService.replaceQuestion(template, blockDisplayOrder, oldQuestionId, newQuestion);

        auditService.auditCurrentUserActivity(String.format("%s template question %d replaced with %d", template.getName(), oldQuestionId, newQuestionId));
    }

    public void makeMonetary(Integer templateId, Integer milestoneId) {
        Template template = find(templateId);

        MilestonesTemplateBlock block = (MilestonesTemplateBlock) template.getSingleBlockByType(ProjectBlockType.Milestones);
        if (block == null) {
            throw new NotFoundException("could not find milestones block");
        }

        List<Project> allByTemplate = templateProjectService.findAllByTemplate(template);
        boolean anyNotApplicableForMonetary = allByTemplate.stream()
                .anyMatch(p -> !((p.getStatusType().equals(ProjectStatus.Draft))
                        || (p.getStatusType().equals(ProjectStatus.Submitted))
                        || (p.getStatusType().equals(ProjectStatus.Assess))));
        if (anyNotApplicableForMonetary) {
            throw new ValidationException("Unable to update this template as it contains projects of status Returned/Active/Closed.");
        }

        boolean milestoneUpdated = false;
        for (ProcessingRoute processingRoute : block.getProcessingRoutes()) {
            for (MilestoneTemplate milestoneTemplate : processingRoute.getMilestones()) {
                if (milestoneTemplate.getExternalId().equals(milestoneId)) {
                    milestoneTemplate.setMonetary(true);
                    milestoneUpdated = true;
                }
            }
        }

        if (!milestoneUpdated) {
            throw new ValidationException("Unable to find milestone on this template with the specified external ID");
        }

        templateRepository.save(template);

        for (Project project : allByTemplate) {
            Milestone milestoneByExternalId = project.getMilestonesBlock().getMilestoneByExternalId(milestoneId);
            if (milestoneByExternalId != null) {
                milestoneByExternalId.setMonetary(true);
                milestoneByExternalId.setMonetarySplit(0);
                templateProjectService.updateProject(project);
            }
        }


    }

    public void setZeroUnitsAllowedForIndicativeBlock(Integer templateID) {
        Template template = find(templateID);

        IndicativeGrantTemplateBlock block = (IndicativeGrantTemplateBlock) template.getSingleBlockByType(ProjectBlockType.IndicativeGrant);
        if (block == null) {
            throw new NotFoundException("could not find indicative block");
        }

        block.setAllowZeroUnits(true);
        templateRepository.save(template);

    }

    public void replaceTenureType(Integer templateId, Integer oldTenureTypeId, Integer newTenureTypeId) {
        Template template = find(templateId);

        if (template.getTenureTypes() == null || template.getTenureTypes().size() > 1) {
            throw new ValidationException("template must have 1 tenure type!");
        }

        TemplateTenureType templateTenureType = template.getTenureTypes().iterator().next();

        if (!templateTenureType.getExternalId().equals(oldTenureTypeId)) {
            throw new ValidationException("template does not have tenure type " + oldTenureTypeId);
        }

        TenureType oldTenureType = templateTenureType.getTenureType();

        TenureType newTenureType = refDataService.getTenureType(newTenureTypeId);
        if (newTenureType == null) {
            throw new NotFoundException("tenure type with id " + newTenureTypeId + " not found!");
        }

        if (!CollectionUtils.isEqualCollection(oldTenureType.getMarketTypes(), newTenureType.getMarketTypes())) {
            throw new ValidationException("old and new tenure types must have the same markets!");
        }

        templateTenureType.setTenureType(newTenureType);

        save(template);

        templateProjectService.replaceTenureType(template, oldTenureTypeId, newTenureTypeId);

        auditService.auditCurrentUserActivity(String.format("%s template tenure type %d replaced with %d", template.getName(), oldTenureTypeId, newTenureTypeId));
    }

    public void updateFundingType(Integer templateId, FundingSpendingTypeFlags flags, boolean deleteBlockData) {
        Template template = find(templateId);

        if (!template.isBlockPresent(ProjectBlockType.Funding)) {
            throw new ValidationException("Funding block not present on this template");
        }

        if (flags == null) {
            throw new ValidationException("FundingSpendingTypeFlags is mandatory, values are showCapitalGLA, showRevenueOther etc");
        }

        FundingTemplateBlock fundingBlock = (FundingTemplateBlock) template.getSingleBlockByType(ProjectBlockType.Funding);
        List<Project> allByTemplate = templateProjectService.findAllByTemplate(template);
        for (Project project : allByTemplate) {
            if (ProjectStatus.Active.equals(project.getStatusType()) || ProjectStatus.Closed.equals(project.getStatusType())) {
                throw new ValidationException("Unable to update this template as it contains active projects.");
            }
        }

        boolean deleteGLACapital = false;
        boolean deleteOtherCapital = false;
        boolean deleteGLARevenue = false;
        boolean deleteOtherRevenue = false;

        if (flags.showCapitalGLA != null) {
            if (fundingBlock.getShowCapitalGLAFunding() != flags.showCapitalGLA) {
                deleteGLACapital = !flags.showCapitalGLA;
                fundingBlock.setShowCapitalGLAFunding(flags.showCapitalGLA);
            }
        }
        if (flags.showCapitalOther != null) {
            if (fundingBlock.getShowCapitalOtherFunding() != flags.showCapitalOther) {
                deleteOtherCapital = !flags.showCapitalOther;
                fundingBlock.setShowCapitalOtherFunding(flags.showCapitalOther);
            }
            fundingBlock.setShowRevenueOtherFunding(flags.showCapitalOther);
        }
       
        if (flags.showRevenueGLA != null) {
            if (fundingBlock.getShowRevenueGLAFunding() != flags.showRevenueGLA) {
                deleteGLARevenue = !flags.showRevenueGLA;
                fundingBlock.setShowRevenueGLAFunding(flags.showRevenueGLA);
            }
        }
        if (flags.showRevenueOther != null) {
            if (fundingBlock.getShowRevenueOtherFunding() != flags.showRevenueOther) {
                deleteOtherRevenue = !flags.showRevenueOther;
                fundingBlock.setShowRevenueOtherFunding(flags.showRevenueOther);
            }
            fundingBlock.setShowRevenueOtherFunding(flags.showRevenueOther);
        }
       

        auditService.auditCurrentUserActivity(String.format("Updated template with ID: %d to use %s for the funding block", template.getId(), flags));
        save(template);


        for (Project project : allByTemplate) {
            updateProjectSpend(project, flags, deleteBlockData, deleteGLACapital, deleteOtherCapital, deleteGLARevenue, deleteOtherRevenue);
        }
    }

    private boolean updateProjectSpend(Project project, FundingSpendingTypeFlags flags, boolean deleteBlockData, boolean deleteGLACapital, boolean deleteOtherCapital, boolean deleteGLARevenue, boolean deleteOtherRevenue ) {
        boolean projectModified = false;
        FundingBlock projectBlock = (FundingBlock) project.getSingleLatestBlockOfType(ProjectBlockType.Funding);
        updateProjectWithNewFlags(project, flags);
        templateProjectService.updateProject(project);

        if (deleteBlockData) {
            List<ProjectLedgerEntry> allByBlockId = financeService.findAllByBlockId(projectBlock.getId());

            for (ProjectLedgerEntry ledgerEntry : allByBlockId) {
                boolean delete = false;
                if (deleteGLACapital && ledgerEntry.getSpendType().equals(SpendType.CAPITAL) && ledgerEntry.getCategory() == null) {
                    delete = true;
                }
                if (deleteOtherCapital && ledgerEntry.getSpendType().equals(SpendType.CAPITAL) && MATCH_FUND_CATEGORY.equals(ledgerEntry.getCategory())) {
                    delete = true;
                }

                if (deleteGLARevenue && ledgerEntry.getSpendType().equals(SpendType.REVENUE) && ledgerEntry.getCategory() == null) {
                    delete = true;
                }
                if (deleteOtherRevenue && ledgerEntry.getSpendType().equals(SpendType.REVENUE) && MATCH_FUND_CATEGORY.equals(ledgerEntry.getCategory())) {
                    delete = true;
                }

                if (delete) {
                    financeService.delete(ledgerEntry);
                    log.warn("deleted: " + ledgerEntry.getId());
                    projectModified = true;
                }
            }
            if (projectModified) {

                auditService.auditCurrentUserActivity(
                        String.format("Project with id: %d had entries deleted from the Funding Block", project.getId()));
                projectRepository.save(project);
            }
        }
        return projectModified;
    }

    private void updateProjectWithNewFlags(Project project, FundingSpendingTypeFlags flags) {
        FundingBlock fundingBlock = project.getFundingBlock();
        if (flags.showCapitalGLA != null) {
            fundingBlock.setShowCapitalGLAFunding(flags.showCapitalGLA);
        }
        if (flags.showCapitalOther != null) {
            fundingBlock.setShowCapitalOtherFunding(flags.showCapitalOther);
        }
        if (flags.showRevenueGLA != null) {
            fundingBlock.setShowRevenueGLAFunding(flags.showRevenueGLA);
        }
        if (flags.showRevenueOther != null) {
            fundingBlock.setShowRevenueOtherFunding(flags.showRevenueOther);
        }
    }

    public void updateDetailsTemplate(Integer templateId, DetailsTemplate details) {
        Template template = find(templateId);
        template.setDetailsConfig(details);
        validateTemplate(template);
        save(template);
    }

    public Page<TemplateSummary> getTemplateSummaries(String templateText, String programmeText, List<Template.TemplateStatus> selectedTemplateStatuses, Pageable pageable) {
        if (!StringUtils.isEmpty(templateText) && !StringUtils.isEmpty(programmeText)) {
            throw new ValidationException("Can't search by template and programme text at the same time");
        }

        Page<TemplateSummary> templates;
        if (StringUtils.isEmpty(templateText) && StringUtils.isEmpty(programmeText)) {
            templates = selectedTemplateStatuses == null ? templateSummaryRepository.findAll(pageable) : templateSummaryRepository.findAllByTemplateStatusIn(selectedTemplateStatuses, pageable);
        } else if (!StringUtils.isEmpty(templateText)) {
            templates = getTemplatesByTemplateText(templateText, selectedTemplateStatuses, pageable);
        } else {
            templates = getTemplatesByProgrammeText(programmeText, pageable);
        }

        for (TemplateSummary template : templates) {
            template.setProgrammes(programmeService.getProgrammesByTemplate(template.getId()));
        }
        return templates;
    }

    public Integer createDraft(String templateJson) {
        Template toSave = new Template();
        return saveDraft(toSave, templateJson);
    }

    public void saveDraft(Integer toSave, String templateJson) {
        Template template = templateRepository.getOne(toSave);
        saveDraft(template, templateJson);
    }

    protected Integer saveDraft(Template toSave, String templateJson) {

        try {
            Template template = mapper.readValue(templateJson, Template.class);
            template.setStatus(Template.TemplateStatus.Draft);
            if (template.getStateModel() == null) {
                throw new ValidationException("No state model is present!");
            }
            templateJson = mapper.writeValueAsString(template);
            validateTemplate(template);
            toSave.setJson(templateJson);
            toSave.setName(template.getName());
            toSave.setAuthor(template.getAuthor());
            toSave.setStatus(Template.TemplateStatus.Draft);
            toSave.setStateModel(template.getStateModel());
            toSave.setCreatedBy(userService.currentUser().getUsername());
            toSave.setCreatedOn(environment.now());
            toSave.setNumberOfProjectAllowedPerOrg(template.getNumberOfProjectAllowedPerOrg());
            templateRepository.save(toSave);
            return toSave.getId();
        } catch (IOException e) {
            throw new ValidationException("Unable to parse JSON Request: " + e.getMessage());
        }


    }

    public Template inflateTemplateFromJson(Template template) {
        if (!template.getBlocksEnabled().isEmpty()) {
            return template;
        }

        if (!Template.TemplateStatus.Draft.equals(template.getStatus())) {
            throw new ValidationException("Can only inflate draft templates");
        }

        String json = template.getJson();
        try {
            Template inflated = mapper.readValue(json, Template.class);
            inflated.cloneIntoTemplate(template, template.getName());
            template.setStatus(Template.TemplateStatus.Active);
            return save(template);
        } catch (IOException e) {
            throw new ValidationException("Unable to parse JSON Request: " + e.getMessage());
        }
    }

    /**
     * @deprecated use updateFundingBlockFlags() instead
     */
    @Deprecated
    public void updateEnforceFundingBalance(Integer templateId, Boolean enforce) {
        Template template = find(templateId);

        FundingTemplateBlock templateFundingBlock = (FundingTemplateBlock) template.getSingleBlockByType(ProjectBlockType.Funding);

        if (templateFundingBlock == null) {
            throw new NotFoundException("could not find funding block");
        }

        templateFundingBlock.setFundingBalanceEnforced(enforce);

        save(template);

        templateProjectService.updateEnforceFundingBalance(template, enforce);
    }

    public void updateStateModel(Integer templateId, UpdateStateModelRequest updateStateModelRequest) {
        Template template = find(templateId);
        template.setStateModel(updateStateModelRequest.getStateModel());
        save(template);
        templateProjectService.updateStateModel(template, updateStateModelRequest);
    }

    public void updateShowMilestoneStatus(Integer templateId, Integer displayOrder, Boolean shown) {
        Template template = find(templateId);
        MilestonesTemplateBlock milestonesTemplateBlock = getMilestonesTemplateBlock(template, displayOrder);
        milestonesTemplateBlock.setShowMilestoneStatus(shown);
        save(template);
    }

    /**
     * Updates flags on the funding block.
     * <p>
     * GLA-21493
     *
     * @author Steve Leach
     */
    public void updateFundingBlockFlags(Integer templateId, FundingBlockFlags flags) {
        Template template = find(templateId);

        FundingTemplateBlock templateFundingBlock = (FundingTemplateBlock) template.getSingleBlockByType(ProjectBlockType.Funding);

        if (templateFundingBlock == null) {
            throw new NotFoundException("Could not find funding block on template " + templateId);
        }

        if (flags.enforceFundingBalance != null) {
            templateFundingBlock.setFundingBalanceEnforced(flags.enforceFundingBalance);
        }

        if (flags.budgetEvidenceAttachmentEnabled != null) {
            templateFundingBlock.setBudgetEvidenceAttachmentEnabled(flags.budgetEvidenceAttachmentEnabled);
        }

        if (flags.multipleBespokeActivitiesEnabled != null) {
            templateFundingBlock.setMultipleBespokeActivitiesEnabled(flags.multipleBespokeActivitiesEnabled);
        }

        save(template);

        if (flags.enforceFundingBalance != null) {
            templateProjectService.updateEnforceFundingBalance(template, flags.enforceFundingBalance);
        }
    }

    public void updateBaselineForOutputsBlock(Integer templateId, Boolean showBaseline) {
        Template template = find(templateId);

        OutputsTemplateBlock outputsTemplateBlock = (OutputsTemplateBlock) template.getSingleBlockByType(ProjectBlockType.Outputs);

        outputsTemplateBlock.setShowBaselines(showBaseline);
        save(template);

    }

    public void updateAssumptionsForOutputsBlock(Integer templateId, Boolean showAssumptions) {
        Template template = find(templateId);

        OutputsTemplateBlock outputsTemplateBlock = (OutputsTemplateBlock) template.getSingleBlockByType(ProjectBlockType.Outputs);

        outputsTemplateBlock.setShowAssumptions(showAssumptions);
        save(template);

    }

    public void migrateQuestionProgressUpdateBlock(Integer templateId, int displayOrder, Integer questionId) {

        Template template = find(templateId);
        List<Project> projectsForTemplate = projectRepository.findAllByTemplate(template);

        boolean useExistingBlock;
        ProgressUpdateBlock newBlock;
        int existingBlockDisplayOrder = -1;

        for (Project project : projectsForTemplate) {
            useExistingBlock = true;
            List<NamedProjectBlock> allQuestionBlocks = project.getBlocksByTypeAndDisplayOrder(ProjectBlockType.Questions, displayOrder);

            for (NamedProjectBlock block : allQuestionBlocks) {
                ProjectQuestionsBlock questionsBlock = (ProjectQuestionsBlock) block;
                Answer questionsAnswer = questionsBlock.getAnswerByQuestionId(questionId);

                if (questionsAnswer != null) {

                    // TODO delete question block if migration was successfully in prod
                    if (questionsBlock.getQuestions().size() - 1 == 0) {
                        questionsBlock.setHidden(true);
                    } else {
                        questionsBlock.getTemplateQuestionByQuestionId(questionId).setRequirement(Requirement.hidden);
                    }

                    if (useExistingBlock) {
                        newBlock = (ProgressUpdateBlock) project.getSingleLatestBlockOfType(ProjectBlockType.ProgressUpdates);
                        if (StringUtils.isNotEmpty(newBlock.getProgressUpdate())) continue;
                        existingBlockDisplayOrder = newBlock.getDisplayOrder();
                        useExistingBlock = false;
                    } else {
                        newBlock = new ProgressUpdateBlock();
                    }
                    newBlock.setDisplayOrder(existingBlockDisplayOrder);

                    newBlock.setBlockType(ProjectBlockType.ProgressUpdates);
                    newBlock.setVersionNumber(questionsBlock.getVersionNumber());
                    newBlock.setProgressUpdate(questionsAnswer.getAnswerAsText());

                    newBlock.setApprovalTime(questionsBlock.getApprovalTime());
                    newBlock.setApproverUsername(questionsBlock.getApproverUsername());
                    newBlock.setApprovedByName(questionsBlock.getApprovedByName());
                    newBlock.setApprovedOnStatus(questionsBlock.getApprovedOnStatus());
                    newBlock.setLastModified(questionsBlock.getLastModified());
                    newBlock.setBlockStatus(questionsBlock.getBlockStatus());

                    newBlock.setProject(project);

                    if (questionsBlock.isReportingVersion()) {
                        newBlock.setReportingVersion(true);
                    } else {
                        newBlock.setReportingVersion(false);
                    }

                    if (questionsBlock.isLatestVersion()) {
                        newBlock.setLatestVersion(true);
                        project.addBlockToProject(newBlock);
                    } else {
                        newBlock.setLatestVersion(false);
                        project.getProjectBlocks().add(newBlock);
                    }

                    projectRepository.save(project);
                }
            }

        }

    }

    public void migrateContactDetails(Integer templateId, Integer questionsBlockDisplayOrder, Integer contactNameQuestionId, Integer contactEmailQuestionId) {
        Template template = find(templateId);
        templateProjectService.migrateContactDetails(template, questionsBlockDisplayOrder, contactNameQuestionId, contactEmailQuestionId);
    }

    public void updateAssessmentsAffectedByCategoryChange(OutputConfigurationGroup group, String oldName, String newName) {
        Set<Template> allUsingConfigGroup = templateRepository.findAllUsingConfigGroup(group.getId());

        templateProjectService.updateAssessmentsAffectedByCategoryChange(allUsingConfigGroup, oldName, newName);

    }

    public void updateInfoMessage(Integer templateId, Integer displayOrder, String infoMessage) {
        Template template = find(templateId);
        TemplateBlock block = template.getSingleBlockByDisplayOrder(displayOrder);

        if (null == block) {
            throw new NotFoundException("Could not find template block for display order " + displayOrder);
        }
        block.setInfoMessage(infoMessage);
        templateProjectService.updateInfoMessage(template, displayOrder, infoMessage);

        templateRepository.save(template);
    }

    public void updateRiskAdjustedFiguresFlag(Integer templateID, boolean enabled) {
        Template template = find(templateID);

        InternalRiskTemplateBlock block = template.getInternalRiskBlock();

        block.setRiskAdjustedFiguresFlag(enabled);
        templateProjectService.updateRiskAdjustedFiguresFlag(template, enabled);

        templateRepository.save(template);

    }

    public RiskRating addRiskRating(Integer templateId, RiskRating riskRating) {
        Template template = find(templateId);

        RiskRating newRiskRating = riskRatingService.create(riskRating);

        InternalRiskTemplateBlock block = template.getInternalRiskBlock();
        block.getRatingList().add(riskRating);

        templateRepository.save(template);

        return newRiskRating;
    }

    public void updateRiskRating(Integer templateId, Integer riskRatingId, RiskRating updatedRiskRating) {
        Template template = find(templateId);

        InternalRiskTemplateBlock block = template.getInternalRiskBlock();
        RiskRating existingRiskRating = block.getRating(riskRatingId);
        existingRiskRating.setDescription(updatedRiskRating.getDescription());
        existingRiskRating.setDisplayOrder(updatedRiskRating.getDisplayOrder());

        templateRepository.save(template);
    }

    public void deleteRiskRating(Integer templateId, Integer riskRatingId) {
        Template template = find(templateId);

        RiskRating riskRating = riskRatingService.find(riskRatingId);

        if (templateProjectService.isRiskRatingUsed(riskRating.getId())) {
            throw new ValidationException("Cannot delete a risk rating used in projects");
        } else {
            InternalRiskTemplateBlock block = template.getInternalRiskBlock();
            block.getRatingList().remove(riskRating);
            riskRatingService.delete(riskRating);
        }

        templateRepository.save(template);
    }

    public Map<String, String> getAvailableDeliverableTypes(Integer templateId) {
        Template template = find(templateId);
        TemplateBlock singleBlockByType = template.getSingleBlockByType(ProjectBlockType.Subcontracting);
        Map<String, String> map = null;
        if (singleBlockByType != null) {
            Set<DeliverableType> availableDeliverableTypes = ((SubcontractingTemplateBlock) singleBlockByType).getAvailableDeliverableTypes();
            map = availableDeliverableTypes
                    .stream().collect(Collectors.toMap(DeliverableType::name, DeliverableType::getDescription));
        }
        return map;
    }

    public void replacePaymentSources(Integer templateId, int displayOrder, String paymentSources) {
        Template template = find(templateId);

        if (paymentSources.isEmpty() || StringUtils.isBlank(paymentSources)) {
            throw new ValidationException("PaymentSources are mandatory, values are: Grant, DPF, RCGF, ESF, MOPAC");
        }

        if (template == null) {
            throw new ValidationException("Not found template with id " + templateId);
        }

        TemplateBlock templateBlock = template.getSingleBlockByDisplayOrder(displayOrder);
        if (templateBlock == null) {
            throw new ValidationException("Template block not present on this template");
        }

        templateBlock.setPaymentSourcesString(paymentSources);
        templateProjectService.replacePaymentSources(template, displayOrder, paymentSources);
        save(template);

    }

    public void updateFundingBlockMonetaryValueScale(Integer templateId, Integer monetaryValueScale) {
        Template template = find(templateId);
        FundingTemplateBlock templateFundingBlock = (FundingTemplateBlock) template.getSingleBlockByType(ProjectBlockType.Funding);

        if (templateFundingBlock == null) {
            throw new NotFoundException("Could not find funding block on template " + templateId);
        }

        templateFundingBlock.setMonetaryValueScale(monetaryValueScale);
        templateProjectService.updateFundingBlockMonetaryValueScale(template, monetaryValueScale);
        save(template);
    }

    @Override
    public boolean canHandleEntity(String className) {
        return Template.class.getSimpleName().equals(className);
    }

    @Override
    public String sanitize(String className, Integer id) throws IOException {
        if (Template.class.getSimpleName().equals(className)) {
            return getTemplateJson(id, true);
        }
        return null;
    }

    @Override
    public void persist(String className, String json) {
    }
}
