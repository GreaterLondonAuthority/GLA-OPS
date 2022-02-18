/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.project.template;

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
import uk.gov.london.ops.audit.AuditService;
import uk.gov.london.ops.contracts.ContractEntity;
import uk.gov.london.ops.contracts.ContractModel;
import uk.gov.london.ops.contracts.ContractService;
import uk.gov.london.ops.framework.enums.Requirement;
import uk.gov.london.ops.framework.environment.Environment;
import uk.gov.london.ops.framework.exception.NotFoundException;
import uk.gov.london.ops.framework.exception.ValidationException;
import uk.gov.london.ops.framework.portableentity.PortableEntityProvider;
import uk.gov.london.ops.framework.portableentity.SanitiseComponent;
import uk.gov.london.ops.payment.FinanceService;
import uk.gov.london.ops.payment.ProjectLedgerEntry;
import uk.gov.london.ops.payment.SpendType;
import uk.gov.london.ops.programme.ProgrammeService;
import uk.gov.london.ops.project.Project;
import uk.gov.london.ops.project.ProjectService;
import uk.gov.london.ops.project.block.NamedProjectBlock;
import uk.gov.london.ops.project.block.ProgressUpdateBlock;
import uk.gov.london.ops.project.block.ProjectBlockType;
import uk.gov.london.ops.project.deliverypartner.DeliverableType;
import uk.gov.london.ops.project.funding.FundingBlock;
import uk.gov.london.ops.project.grant.AffordableHomesOfWhichCategory;
import uk.gov.london.ops.project.implementation.repository.*;
import uk.gov.london.ops.project.internalblock.InternalBlockType;
import uk.gov.london.ops.project.milestone.Milestone;
import uk.gov.london.ops.project.question.Answer;
import uk.gov.london.ops.project.question.ProjectQuestionsBlock;
import uk.gov.london.ops.project.risk.RiskRatingService;
import uk.gov.london.ops.project.skills.AllocationType;
import uk.gov.london.ops.project.state.ProjectStatus;
import uk.gov.london.ops.project.state.StateModel;
import uk.gov.london.ops.project.template.domain.*;
import uk.gov.london.ops.refdata.*;
import uk.gov.london.ops.service.CRUDServiceInterface;

import javax.persistence.EntityManager;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import static uk.gov.london.common.GlaUtils.parseInt;
import static uk.gov.london.ops.framework.OPSUtils.currentUsername;
import static uk.gov.london.ops.framework.enums.Requirement.mandatory;
import static uk.gov.london.ops.framework.enums.Requirement.optional;
import static uk.gov.london.ops.payment.ProjectLedgerEntry.MATCH_FUND_CATEGORY;
import static uk.gov.london.ops.project.implementation.di.TemplateDataInitialiser.DEFAULT_CONFIG_GROUP_ID;
import static uk.gov.london.ops.project.template.domain.AnswerType.Dropdown;
import static uk.gov.london.ops.project.template.domain.AnswerType.YesNo;

@Service
@Transactional
public class TemplateServiceImpl implements TemplateService, CRUDServiceInterface<Integer, Template>, PortableEntityProvider {

    Logger log = LoggerFactory.getLogger(getClass());

    private static final int BLOCK_NAME_MAX_SIZE = 50;
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
    RefDataServiceImpl refDataService;

    @Autowired
    ProjectService projectService;

    @Autowired
    TemplateProjectService templateProjectService;

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

    public List<Template> findAll() {
        return templateRepository.findAll();
    }

    public Template get(Integer id) {
        return templateRepository.getOne(id);
    }

    public String getTemplateJson(Integer id, boolean sanitise) throws IOException {
        Template template = templateRepository.findById(id).orElse(null);
        if (template == null) {
            throw new NotFoundException();
        }
        if (Template.TemplateStatus.Draft.equals(template.getStatus())) {
            return template.getJson();
        }
        if (template.getContractId() != null) {
            ContractModel contractModel = contractService.find(template.getContractId());
            template.setContract(contractModel);
        }

        template.setProjectsCount(projectRepository.findAllIdByTemplateId(id).length);

        return getTemplateAsJsonString(template, sanitise);
    }

    public String getTemplateAsJsonString(Template template, boolean sanitise) throws IOException {
        Template sanitised = null;
        if (sanitise) {
            // call to fully initialise template  - don't remove or will get lazy load issues.
            mapper.writeValueAsString(template);
            entityManager.detach(template);
            List<Class> excludedClasses = Arrays.asList(
                    ContractEntity.class, OutputConfigurationGroup.class, FundingClaimCategory.class, MarketType.class,
                    Question.class,
                    Template.class, TenureType.class);

            sanitised = (Template) sanitiseComponent.sanitise(template, excludedClasses);
            if (template.getStatus() != Template.TemplateStatus.Active) {
                sanitised.setName("TBC");
                sanitised.setAuthor("TBC");
            }
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
            if (!java.lang.reflect.Modifier.isStatic(field.getModifiers())
                    && field.getAnnotationsByType(JsonIgnore.class).length == 0) {
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
                            && !(field.get(object) instanceof ContractEntity)
                            && !(field.get(object) instanceof OutputConfigurationGroup)
                            && !(field.get(object) instanceof MarketType)
                            && !(field.get(object) instanceof Question)
                            && !(field.get(object) instanceof Template)
                            && !(field.get(object) instanceof FundingClaimCategory)
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
        return object instanceof ContractEntity
                || object instanceof FundingClaimCategory
                || object instanceof MarketType
                || object instanceof OutputConfigurationGroup
                || object instanceof Question
                || object instanceof TenureType
                || object instanceof FundingClaimCategory;
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
        template.setCreatedBy(currentUsername());
        template.setCreatedOn(environment.now());
        template.setJson(templateJson);

        return save(template);
    }

    public Template save(Template template) {
        validateTemplate(template);

        // if any blocks are not numbered
        boolean anyMissingDisplayOrders = false;
        for (TemplateBlock templateBlock : template.getBlocksEnabled()) {
            if (templateBlock.getDisplayOrder() == null) {
                anyMissingDisplayOrders = true;
            }
        }

        // init tenure template types and associate market types
        if (template.getTenureTypes() != null) {
            for (TemplateTenureType templateTenureType : template.getTenureTypes()) {
                for (TemplateTenureTypeMarketType templateTenureTypeMarketType : templateTenureType
                        .getTemplateTenureTypeMarketTypes()) {
                    templateTenureTypeMarketType.setTemplateTenureType(templateTenureType);
                    Integer id = templateTenureTypeMarketType.getMarketType().getId();
                    MarketType inited = refDataService.getMarketType(id);
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
            setMilestonesTemplateBlockDefaults(template);
        }

        List<TemplateQuestionsBlockInterface> questionsBlocks = template.getBlocksByType(TemplateQuestionsBlockInterface.class);
        for (TemplateQuestionsBlockInterface questionBlock : questionsBlocks) {
            createOrLoadAndValidateQuestions(questionBlock.getQuestions());
        }

        // update the blocks json before saving the blocks for the first time
        updateTemplateBlocksJson(template);
        updateInternalTemplateBlocksJson(template);

        // generates any IDs first
        templateRepository.save(template);

        if (template.isBlockPresent(ProjectBlockType.Milestones)) {
            MilestonesTemplateBlock milestonesTemplateBlock = (MilestonesTemplateBlock) template
                    .getSingleBlockByType(ProjectBlockType.Milestones);
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

    private void setMilestonesTemplateBlockDefaults(Template template) {
        MilestonesTemplateBlock milestonesTemplateBlock = (MilestonesTemplateBlock) template
                .getSingleBlockByType(ProjectBlockType.Milestones);
        for (MilestoneTemplate milestoneTemplate : milestonesTemplateBlock.getAllMilestones()) {
            // milestones are monetary by default
            if (milestoneTemplate.getMonetary() == null) {
                milestoneTemplate.setMonetary(true);
            }
        }
    }

    private void updateTemplateBlocksJson(Template template) {
        for (TemplateBlock templateBlock : template.getBlocksEnabled()) {
            try {
                if (templateBlock.shouldSaveBlockData()) {
                    templateBlock.setBlockData(mapper.writeValueAsString(templateBlock));
                }
            } catch (JsonProcessingException e) {
                log.error("Could not serialise template block to JSON, type=" + templateBlock.getBlock(), e);
            }
        }
    }

    void updateInternalTemplateBlocksJson(Template template) {
        for (InternalTemplateBlock internalTemplateBlock : template.getInternalBlocks()) {
            try {
                internalTemplateBlock.setBlockData(mapper.writeValueAsString(internalTemplateBlock));
            } catch (JsonProcessingException e) {
                log.error("Could not serialise internal template block to JSON, type=" + internalTemplateBlock.getType(), e);
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

        String username = currentUsername();

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
        if (template.getStateModel() == null) {
            throw new ValidationException("No state model is present!");
        }

        if (template.getNumberOfProjectAllowedPerOrg() != null && template.getNumberOfProjectAllowedPerOrg() < 1) {
            throw new ValidationException("Maximum number of projects needs to be 1 or more or empty (Indicating no limit)");
        }

        for (TemplateBlock tb : template.getBlocksEnabled()) {
            if (tb.getBlockDisplayName() != null && tb.getBlockDisplayName().length() > BLOCK_NAME_MAX_SIZE) {
                throw new ValidationException(
                        "Cannot have a block name with a name longer than " + BLOCK_NAME_MAX_SIZE + "! (" + tb
                                .getBlockDisplayName() + ")");
            }

            if (tb.getDisplayOrder() == null) {
                throw new ValidationException("Display order mandatory for block " + tb.getBlockDisplayName());
            }
        }

        if (template.getBlocksEnabled() != null && !template.getBlocksEnabled().isEmpty()) {
            Set<Integer> uniqueDisplayOrders = template.getBlocksEnabled().stream().map(block -> block.getDisplayOrder())
                    .collect(Collectors.toSet());
            if (uniqueDisplayOrders.size() != template.getBlocksEnabled().size()) {
                throw new ValidationException("Display order has to be unique in each block");
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
                    throw new ValidationException(
                            "Tenure Type used by this template is not recognised, create it prior to using in template: "
                                    + templateTenureType.getTenureType().getName());
                }

                boolean defaultTenure = tenureType.getMarketTypes() != null && !tenureType.getMarketTypes().isEmpty();
                boolean templateTenureTypes = templateTenureType.getTemplateTenureTypeMarketTypes() != null
                        && !templateTenureType.getTemplateTenureTypeMarketTypes().isEmpty();

                if (!defaultTenure && !templateTenureTypes) {
                    throw new ValidationException("Template " + template.getName()
                            + " No market types specified for new tenure type with display order of: "
                            + templateTenureType.getDisplayOrder());
                }
            }
        }

        if (template.isBlockPresent(ProjectBlockType.Funding)) {
            FundingTemplateBlock fundingBlock = (FundingTemplateBlock) template.getSingleBlockByType(ProjectBlockType.Funding);

            if (fundingBlock.getFundingSpendType() == null) {
                throw new ValidationException("Unable to configure template with funding spend type on Funding Block not set.");
            }

            boolean catsEnabled = fundingBlock.getShowCategories() == null ? false : fundingBlock.getShowCategories();
            boolean milestonesEnabled = fundingBlock.getShowMilestones() == null ? false : fundingBlock.getShowMilestones();
            if (catsEnabled && milestonesEnabled) {
                throw new ValidationException("Unable to configure template with both categories and milestones configured.");
            }

            if (!catsEnabled && !milestonesEnabled) {
                throw new ValidationException("Unable to configure template with neither categories nor milestones configured.");
            }

            if (catsEnabled && fundingBlock.getCategoriesExternalId() == null) {
                throw new ValidationException(
                        "Unable to configure template with categories enabled but no category group id specified.");

            }

            if (!Boolean.TRUE.equals(fundingBlock.getShowCapitalGLAFunding())
                    && !Boolean.TRUE.equals(fundingBlock.getShowRevenueGLAFunding())
                    && !Boolean.TRUE.equals(fundingBlock.getShowCapitalOtherFunding())
                    && !Boolean.TRUE.equals(fundingBlock.getShowRevenueOtherFunding())
                    && fundingBlock.getFundingSpendType() == null) {
                throw new ValidationException("Template " + template.getName()
                        + " has a funding block but no funding type is enabled.");
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
                if (mandatory.equals(wardRequirement)) {
                    if (!boroughRequirement.equals(mandatory)) {
                        valid = false;
                    }
                } else if (optional.equals(wardRequirement)) {
                    if (boroughRequirement.equals(Requirement.hidden)) {
                        valid = false;
                    }
                }
                if (!valid) {
                    throw new ValidationException(
                            "Cannot create or update a template where ward requirement is more restrictive than Borough requirement");
                }
            }
        }

        if (template.isBlockPresent(ProjectBlockType.LearningGrant)) {
            LearningGrantTemplateBlock learningGrantTemplateBlock = (LearningGrantTemplateBlock) template
                    .getSingleBlockByType(ProjectBlockType.LearningGrant);
            if (learningGrantTemplateBlock.getGrantType() == null) {
                List<String> typeNames = Arrays.stream(SkillsGrantType.values()).map(SkillsGrantType::name)
                        .collect(Collectors.toList());
                throw new ValidationException("Template " + template.getName()
                        + " has a learning grant block but no grantType is set. Valid values are " + String
                        .join(", ", typeNames));
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

                if (outputBlock.getOutputConfigurationGroup() != null
                        && outputBlock.getOutputConfigurationGroup().getId() != null) {
                    OutputConfigurationGroup outputConfigurationGroup = outputConfigurationService
                            .findGroup(outputBlock.getOutputConfigurationGroup().getId());
                    if (outputConfigurationGroup == null) {
                        throw new ValidationException("Output configuration group with id "
                                + outputBlock.getOutputConfigurationGroup().getId() + " not found");
                    }
                }
            }
        }

        if (template.getTenureTypes() != null) {
            for (TemplateTenureType templateTenureType : template.getTenureTypes()) {
                for (TemplateTenureTypeMarketType templateTenureTypeMarketType : templateTenureType
                        .getTemplateTenureTypeMarketTypes()) {
                    Integer id = templateTenureTypeMarketType.getMarketType().getId();
                    MarketType inited = refDataService.getMarketType(id);
                    if (inited == null) {
                        throw new ValidationException("Market Type with ID" + id + " not found");
                    }
                }
            }
        }

        List<TemplateQuestionsBlockInterface> questionsBlocks = template.getBlocksByType(TemplateQuestionsBlockInterface.class);
        if (questionsBlocks.size() > MAX_NB_QUESTION_BLOCKS) {
            throw new ValidationException(
                    "Cannot create a template with more than " + MAX_NB_QUESTION_BLOCKS + " additional question blocks");
        }
        for (TemplateQuestionsBlockInterface questionBlock : questionsBlocks) {
            int questionCount = questionBlock.getQuestions().size();
            int uniqueQuestions = questionBlock.getQuestions().stream().map(t -> t.getQuestion().getId())
                    .collect(Collectors.toSet()).size();
            if (questionCount != uniqueQuestions) {
                throw new ValidationException("Cannot create a template with duplicated questions");
            }

            validateTemplateQuestions(questionBlock.getQuestions());

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

    /**
     * Determines whether to load or crete template questions
     */
    private void createOrLoadAndValidateQuestions(Set<TemplateQuestion> questions) {
        validateTemplateQuestions(questions);
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
                                null,
                                providedQuestion.getMaxUploadSizeInMb(),
                                providedQuestion.getMaxCombinedUploadSizeInMb(),
                                providedQuestion.getMaxAnswers(),
                                providedQuestion.getDelimiter());
                    }
                    templateQuestion.setQuestion(questionFromDB);
                }
            }
        }
    }


    /**
     * Validates template questions
     */
    private void validateTemplateQuestions(Set<TemplateQuestion> questions) {
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
                        questionService.validateQuestion(providedQuestion.getId(), providedQuestion.getText(),
                                providedQuestion.getAnswerType(), options, providedQuestion.getMaxAnswers(),
                                providedQuestion.getDelimiter());

                    } else {
                        validateProvidedQuestionAgainstExisting(providedQuestion, questionFromDB);
                    }
                } else {
                    throw new ValidationException("Question IDs must be provided when uploading templates.");
                }
            }
        }
    }

    void validateProvidedQuestionAgainstExisting(Question providedQuestion, Question existingQuestion) {
        if (StringUtils.isNotEmpty(providedQuestion.getText()) && !StringUtils
                .equalsIgnoreCase(existingQuestion.getText().trim(), providedQuestion.getText().trim())) {
            throw new ValidationException(
                    String.format("provided text for question %d does not match the existing one!", providedQuestion.getId()));
        }

        if (providedQuestion.getAnswerType() != null && !Objects
                .equals(existingQuestion.getAnswerType(), providedQuestion.getAnswerType())) {
            throw new ValidationException(String.format("provided answer type for question %d does not match the existing one!",
                    providedQuestion.getId()));
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


    private Page<TemplateSummary> getTemplatesByTemplateText(String templateText,
            List<Template.TemplateStatus> selectedTemplateStatuses, Pageable pageable) {
        templateText = templateText == null ? "" : templateText;
        Integer templateId = parseInt(templateText);
        if (selectedTemplateStatuses == null) {
            return templateSummaryRepository.findAllByIdOrNameContainingIgnoreCase(templateId, templateText, pageable);
        } else {
            return templateSummaryRepository
                    .findAllByIdOrNameContainingIgnoreCaseAndTemplateStatusIn(templateId, templateText, selectedTemplateStatuses,
                            pageable);
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

    @Transactional
    public String reorderBlock(final int templateId, ProjectBlockType blockType, int blockDisplayOrder, int newDisplayOrder)
            throws InterruptedException {
        final Template template = templateRepository.findById(templateId).orElse(null);

        TemplateBlock block = template.getSingleBlockByTypeAndDisplayOrder(blockType, blockDisplayOrder);
        jdbc.update("update template_block tb set display_order = ? where tb.id = ?", newDisplayOrder, block.getId());

        int projectUpdates = templateProjectService
                .updateBlockDisplayOrder(template.getId(), blockType, blockDisplayOrder, newDisplayOrder);

        return projectUpdates + "project blocks updated";
    }

    @Async
    @Transactional
    public Future<Template> addBlockAsync(final int templateId, final TemplateBlock block) throws InterruptedException {
        Template savedTemplate = addBlock(templateId, block);
        return new AsyncResult<>(savedTemplate);
    }

    public Template addBlock(final int templateId, final TemplateBlock block) {
        final Template template = templateRepository.findById(templateId).orElse(null);

        if (ProjectBlockType.Questions.equals(block.getBlock())) {
            createOrLoadAndValidateQuestions(((QuestionsTemplateBlock) block).getQuestions());
        }

        if (template.getCloneOfTemplateId() != null) {
            template.setCloneModified(true);
        }

        template.addBlockWithDisplayOrder(block);
        Template savedTemplate = save(template);
        templateProjectService.addBlockToProjectsByTemplate(template, block);
        return savedTemplate;
    }

    @Transactional
    public void addInternalBlock(int templateId, InternalTemplateBlock block) {
        Template template = find(templateId);
        template.getInternalBlocks().add(block);
        save(template);
        InternalTemplateBlock savedBlock = template
                .getInternalBlockByTypeAndDisplayOrder(block.getType(), block.getDisplayOrder());
        templateProjectService.addInternalBlock(template, savedBlock);
    }

    public void setTemplateContract(Integer templateId, Integer contractId) {
        Template template = find(templateId);
        ContractModel contractModel = contractService.find(contractId);
        template.setContract(contractModel);
        template.setContractId(contractId);
        save(template);
    }

    public void removeTemplateContract(Integer templateId) {
        Template template = find(templateId);
        template.setContract(null);
        template.setContractId(null);
        save(template);
    }

    public void addInternalQuestionSection(Integer templateId, int blockDisplayOrder, QuestionsBlockSection section) {
        Template template = find(templateId);

        InternalQuestionsTemplateBlock block = (InternalQuestionsTemplateBlock) template
                .getInternalBlockByTypeAndDisplayOrder(InternalBlockType.Questions, blockDisplayOrder);
        addSectionToBlock(template, block, blockDisplayOrder, section);
        templateProjectService.addSectionToInternalBlock(template, blockDisplayOrder, section);
    }

    public void addSection(Integer templateId, int blockDisplayOrder, QuestionsBlockSection section) {
        Template template = find(templateId);

        QuestionsTemplateBlock block = (QuestionsTemplateBlock) template
                .getSingleBlockByTypeAndDisplayOrder(ProjectBlockType.Questions, blockDisplayOrder);
        addSectionToBlock(template, block, blockDisplayOrder, section);
        templateProjectService.addSection(template, blockDisplayOrder, section);
    }

    private void validateSection(QuestionsBlockSection section, TemplateQuestionsBlockInterface block) {
        TemplateQuestion parentQuestion = section.getParentId() != null ? block.getQuestionById(section.getParentId()) : null;
        if (block != null && parentQuestion != null && Objects.equals(parentQuestion.getSectionId(), section.getExternalId())) {
            throw new ValidationException(
                    "Unable to add a new section because section parent question cannot belong to same section "
                            + section.getExternalId());
        }
    }

    private void addSectionToBlock(Template template, TemplateQuestionsBlockInterface block, int blockDisplayOrder,
            QuestionsBlockSection section) {
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
            double currentMax = block.getSections().stream().mapToDouble(QuestionsBlockSection::getDisplayOrder).max()
                    .orElse(0.0);

            section.setDisplayOrder(currentMax + 1.0);
        }
        validateSection(section, block);
        block.getSections().add(section);
        this.save(template);
    }

    public void addQuestion(Integer templateId, int blockDisplayOrder, TemplateQuestion newTemplateQuestion) {
        Template template = find(templateId);

        TemplateBlock block = template.getSingleBlockByTypeAndDisplayOrder(ProjectBlockType.Questions, blockDisplayOrder);
        if (block == null) {
            throw new NotFoundException("could not find questions block with display order " + blockDisplayOrder);
        }

        addQuestionToTemplateQuestionBlock((TemplateQuestionsBlockInterface) block, newTemplateQuestion);

        template = save(template);

        QuestionsTemplateBlock questionsBlock = (QuestionsTemplateBlock) template
                .getSingleBlockByTypeAndDisplayOrder(ProjectBlockType.Questions, blockDisplayOrder);
        TemplateQuestion savedTemplateQuestion = questionsBlock.getQuestionById(newTemplateQuestion.getQuestion().getId());

        templateProjectService.addQuestion(template, blockDisplayOrder, savedTemplateQuestion);
    }

    public void addQuestionToInternalBlock(Integer templateId, Integer displayOrder, TemplateQuestion newTemplateQuestion) {
        Template template = find(templateId);
        InternalTemplateBlock block = template.getInternalBlockByTypeAndDisplayOrder(InternalBlockType.Questions, displayOrder);
        if (block == null) {
            throw new NotFoundException("could not find questions block with display order " + displayOrder);
        }
        addQuestionToTemplateQuestionBlock((TemplateQuestionsBlockInterface) block, newTemplateQuestion);
        template = save(template);
        InternalQuestionsTemplateBlock savedBlock = (InternalQuestionsTemplateBlock) template
                .getInternalBlockByTypeAndDisplayOrder(InternalBlockType.Questions, displayOrder);
        TemplateQuestion savedTemplateQuestion = savedBlock.getQuestionById(newTemplateQuestion.getQuestion().getId());
        templateProjectService.addQuestionToInternalBlock(template, displayOrder, savedTemplateQuestion);
    }

    private void addQuestionToTemplateQuestionBlock(TemplateQuestionsBlockInterface questionsBlock, TemplateQuestion question) {
        double maxDisplayOrder = 0.0;
        for (TemplateQuestion tq : questionsBlock.getQuestions()) {
            if (tq.getQuestion().getId().equals(question.getQuestion().getId())) {
                throw new ValidationException("question " + question.getQuestion().getId() + " already exists in template!");
            }
            maxDisplayOrder = Math.max(maxDisplayOrder, tq.getDisplayOrder());
        }

        if (question.getSectionId() != null) {
            if (questionsBlock.getSections().stream().noneMatch(m -> m.getExternalId().equals(question.getSectionId()))) {
                throw new ValidationException("Unable to create question as associated section was not found.");
            }
        }

        // we want to force the question to be optional and to be displayed last
        Double newDisplayOrderFromRequest = question.getDisplayOrder();
        question.setDisplayOrder(newDisplayOrderFromRequest != null ? newDisplayOrderFromRequest : maxDisplayOrder + 1.0);

        if (question.getRequirement() == null) {
            question.setRequirement(optional);
        }
        questionsBlock.getQuestions().add(question);
    }

    public void updateQuestionRequirement(Integer templateId, Integer blockDisplayOrder, Integer questionId,
            Requirement requirement) {
        Template template = find(templateId);
        TemplateQuestion question = getTemplateQuestion(template, blockDisplayOrder, questionId);
        question.setRequirement(requirement);
        save(template);
    }

    public void updateQuestionHelpText(Integer templateId, Integer blockDisplayOrder, Integer questionId, String helpText) {
        Template template = find(templateId);
        TemplateQuestion question = getTemplateQuestion(template, blockDisplayOrder, questionId);
        if (helpText == null || helpText.trim().isEmpty()) {
            question.setHelpText(null);
        } else {
            question.setHelpText(helpText);
        }
        save(template);
        auditService.auditCurrentUserActivity(
                String.format("Template: %d, question block display order: %d, question id: %d, help text changed.",
                        templateId, blockDisplayOrder, questionId));
    }

    public void updateQuestionDisplayOrder(Integer templateId, Integer blockDisplayOrder, Integer questionId,
            Double newDisplayOrder, boolean internal) {
        Template template = find(templateId);
        TemplateQuestion question = getTemplateQuestion(template, blockDisplayOrder, questionId, internal);
        question.setDisplayOrder(newDisplayOrder);
        save(template);
    }

    public void addQuestionToSection(Integer templateId, Integer blockDisplayOrder, Integer questionId, Integer sectionId) {
        Template template = find(templateId);
        TemplateQuestion question = getTemplateQuestion(template, blockDisplayOrder, questionId);
        question.setSectionId(sectionId);
        save(template);
    }

    public void addQuestionToInternalSection(Integer templateId, Integer blockDisplayOrder, Integer questionId,
            Integer sectionId) {
        Template template = find(templateId);
        TemplateQuestion question = getTemplateQuestion(template, blockDisplayOrder, questionId, true);
        question.setSectionId(sectionId);
        save(template);
    }

    public void removeQuestionFromSection(Integer templateId, Integer blockDisplayOrder, Integer questionId) {
        Template template = find(templateId);
        TemplateQuestion question = getTemplateQuestion(template, blockDisplayOrder, questionId);
        question.setSectionId(null);
        save(template);
    }

    public void removeQuestionFromInternalSection(Integer templateId, Integer blockDisplayOrder, Integer questionId) {
        Template template = find(templateId);
        TemplateQuestion question = getTemplateQuestion(template, blockDisplayOrder, questionId, true);
        question.setSectionId(null);
        save(template);
    }

    private TemplateQuestion getTemplateQuestion(Template template, Integer blockDisplayOrder, Integer questionId) {
        return getTemplateQuestion(template, blockDisplayOrder, questionId, false);
    }

    public TemplateQuestion getTemplateQuestion(Template template, Integer blockDisplayOrder, Integer questionId,
            boolean internal) {

        Object block;
        if (internal) {
            block = template.getInternalBlockByTypeAndDisplayOrder(InternalBlockType.Questions,
                    blockDisplayOrder);
        } else {
            block = template.getSingleBlockByTypeAndDisplayOrder(ProjectBlockType.Questions, blockDisplayOrder);
        }
        if (block == null) {
            throw new NotFoundException("could not find questions block with display order " + blockDisplayOrder);
        }

        TemplateQuestionsBlockInterface questionsBlock = (TemplateQuestionsBlockInterface) block;

        TemplateQuestion question = questionsBlock.getQuestionById(questionId);
        if (question == null) {
            throw new NotFoundException("could not find questions with id " + questionId);
        }

        return question;
    }

    public void removeQuestion(Integer templateId, Integer blockDisplayOrder, Integer questionId, String infoMessage) {
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
        auditService.auditCurrentUserActivity(
                String.format("Template: %d, block display order: %d, question id: %d, question removed with reason: %s",
                        templateId, blockDisplayOrder, questionId, infoMessage));
        templateProjectService.removeQuestion(template, blockDisplayOrder, questionId);
    }

    public void removeInternalQuestion(Integer templateId, Integer blockDisplayOrder, Integer questionId, String infoMessage) {
        Template template = find(templateId);

        InternalTemplateBlock block = template
                .getInternalBlockByTypeAndDisplayOrder(InternalBlockType.Questions, blockDisplayOrder);
        if (block == null) {
            throw new NotFoundException("could not find internal questions block with display order "
                    + blockDisplayOrder);
        }

        InternalQuestionsTemplateBlock questionsBlock = (InternalQuestionsTemplateBlock) block;

        TemplateQuestion question = questionsBlock.getQuestionById(questionId);
        if (question == null) {
            throw new NotFoundException("could not find questions with id " + questionId);
        }

        questionsBlock.getQuestions().remove(question);
        save(template);
        auditService.auditCurrentUserActivity(
                String.format("Template: %d, internal block display order: %d, question id: %d, question removed with reason: %s",
                        templateId, blockDisplayOrder, questionId, infoMessage));
        templateProjectService.removeInternalQuestion(template, blockDisplayOrder, questionId);
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
        updateMilestoneAllowableEvidenceDocuments(template, milestonesTemplateBlock, maxCount,
                milestonesTemplateBlock.getEvidenceApplicability());
    }

    public void updateMilestoneAllowableEvidenceDocuments(Integer templateId, Integer blockDisplayOrder,
            MilestonesTemplateBlock.EvidenceApplicability evidenceApplicability) {
        Template template = find(templateId);
        MilestonesTemplateBlock milestonesTemplateBlock = getMilestonesTemplateBlock(template, blockDisplayOrder);
        updateMilestoneAllowableEvidenceDocuments(template, milestonesTemplateBlock,
                milestonesTemplateBlock.getMaxEvidenceAttachments(), evidenceApplicability);
    }

    private void updateMilestoneAllowableEvidenceDocuments(Template template, MilestonesTemplateBlock milestonesBlock,
            Integer maxCount, MilestonesTemplateBlock.EvidenceApplicability evidenceApplicability) {

        Integer maxEvidenceAttachments = milestonesBlock.getMaxEvidenceAttachments();
        if (maxEvidenceAttachments != null && maxCount < maxEvidenceAttachments) {
            throw new ValidationException("Unable to reduce the number of permitted evidence attachments.");
        }

        MilestonesTemplateBlock.EvidenceApplicability existingApplicability = milestonesBlock.getEvidenceApplicability();
        if (existingApplicability != null && !existingApplicability
                .equals(MilestonesTemplateBlock.EvidenceApplicability.NOT_APPLICABLE) && !existingApplicability
                .equals(evidenceApplicability)) {
            throw new ValidationException("Unable to change the evidence rules.");

        }

        milestonesBlock.setMaxEvidenceAttachments(maxCount);
        milestonesBlock.setEvidenceApplicability(evidenceApplicability);
        save(template);

        templateProjectService
                .updateMilestoneEvidentialStatus(template, milestonesBlock.getDisplayOrder(), maxCount, evidenceApplicability);
    }


    private MilestonesTemplateBlock getMilestonesTemplateBlock(Template template, Integer blockDisplayOrder) {
        TemplateBlock block = template.getSingleBlockByTypeAndDisplayOrder(ProjectBlockType.Milestones, blockDisplayOrder);
        if (block == null) {
            throw new NotFoundException("could not find milestones block with display order " + blockDisplayOrder);
        }

        return (MilestonesTemplateBlock) block;
    }

    public void updateBlockDisplayName(Integer templateId, Integer blockDisplayOrder, String blockType, String oldName,
            String newName, boolean isInternalBlock) {

        validateBlockType(blockType, isInternalBlock);

        Template template = find(templateId);
        if (isInternalBlock) {
            updateInternalBlockDisplayName(template, blockDisplayOrder, blockType, oldName, newName);
        } else {
            updateTemplateBlockDisplayName(template, blockDisplayOrder, blockType, oldName, newName);
        }
        save(template);
        templateProjectService.updateBlockDisplayName(template, blockDisplayOrder, blockType, newName, isInternalBlock);
    }

    void validateBlockType(String blockType, boolean isInternalBlock) {
        try {
            Object anyBlockType = isInternalBlock ? InternalBlockType.valueOf(blockType) : ProjectBlockType.valueOf(blockType);
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Invalid blockType provided for the block : " + blockType);
        }
    }

    void updateTemplateBlockDisplayName(Template template, Integer blockDisplayOrder, String blockType, String oldName,
            String newName) {
        TemplateBlock block = template
                .getSingleBlockByTypeAndDisplayOrder(ProjectBlockType.valueOf(blockType), blockDisplayOrder);
        validateTemplateBlockNotNull(block, blockType, blockDisplayOrder);
        validateBlockDisplayName(block.getBlockDisplayName(), oldName);
        block.setBlockDisplayName(newName);
    }

    void updateInternalBlockDisplayName(Template template, Integer blockDisplayOrder, String blockType,
            String oldName, String newName) {
        InternalTemplateBlock block = template.getInternalBlockByTypeAndDisplayOrder(InternalBlockType.valueOf(blockType),
                blockDisplayOrder);
        validateTemplateBlockNotNull(block, blockType, blockDisplayOrder);
        validateBlockDisplayName(block.getBlockDisplayName(), oldName);
        block.setBlockDisplayName(newName);
    }

    void validateTemplateBlockNotNull(Object block, String blockType, Integer blockDisplayOrder) {
        if (block == null) {
            throw new ValidationException(
                    "could not find block with type " + blockType + " and display order " + blockDisplayOrder);
        }
    }

    void validateBlockDisplayName(String blockDisplayName, String oldName) {
        if (blockDisplayName != null && !StringUtils.equals(oldName, blockDisplayName)) {
            throw new ValidationException("old name does not match the current name: '" + blockDisplayName + "'!");
        }
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
        auditService.auditCurrentUserActivity(
                String.format("%s template question %d replaced with %d", template.getName(), oldQuestionId, newQuestionId));
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
            throw new ValidationException(
                    "Unable to update this template as it contains projects of status Returned/Active/Closed.");
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
                projectService.updateProject(project);
            }
        }


    }

    public void setZeroUnitsAllowedForIndicativeBlock(Integer templateID) {
        Template template = find(templateID);

        IndicativeGrantTemplateBlock block = (IndicativeGrantTemplateBlock) template
                .getSingleBlockByType(ProjectBlockType.IndicativeGrant);
        if (block == null) {
            throw new NotFoundException("could not find indicative block");
        }

        block.setAllowZeroUnits(true);
        templateRepository.save(template);

    }

    public Set<Integer> getTemplatesUsingTenureType(Integer tenureType) {
        return templateRepository.getUsagesOfTenureType(tenureType);

    }

    public void replaceMarketType(Integer tenureType, Integer oldMarketType, Integer newMarketType) {

        TenureType existingTenure = refDataService.getTenureType(tenureType);

        Optional<MarketType> first = existingTenure.getMarketTypes().stream()
                .filter(m -> m.getId().equals(oldMarketType))
                .findFirst();
        if (!first.isPresent()) {
            throw new ValidationException("No matching market type found for tenure type: " + tenureType);
        }

        MarketType marketType = refDataService.getMarketType(newMarketType);
        if (marketType == null) {
            throw new ValidationException("No replacement market type found with ID: " + newMarketType);

        }

        existingTenure.getMarketTypes().remove(first.get());
        existingTenure.getMarketTypes().add(marketType);
        refDataService.updateTenureType(existingTenure);

        auditService.auditCurrentUserActivity(String.format("Replaced the market type for tenure %d", tenureType));

    }

    public void replaceTenureType(Integer templateId, Integer oldTenureTypeId, Integer newTenureTypeId,
            boolean updateActiveProjects) {
        Template template = find(templateId);

        TemplateTenureType templateTenureType = template.getTenureTypes().stream()
                .filter(t -> t.getExternalId().equals(oldTenureTypeId))
                .findFirst().orElse(null);

        if (templateTenureType == null) {
            throw new ValidationException("template does not have tenure type " + oldTenureTypeId);
        }

        if (!updateActiveProjects) {
            List<Project> allByTemplate = templateProjectService.findAllByTemplate(template);
            for (Project project : allByTemplate) {
                if (project.getStatusType().equals(ProjectStatus.Active)) {
                    throw new ValidationException(
                            "Unable to modify tenure type as template contains active projects: P" + project.getId());
                }
            }
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
        auditService.auditCurrentUserActivity(String.format("%s template tenure type %d replaced with %d",
                template.getName(), oldTenureTypeId, newTenureTypeId));
    }

    public void updateFundingType(Integer templateId, FundingSpendingTypeFlags flags, boolean deleteBlockData) {
        Template template = find(templateId);

        if (!template.isBlockPresent(ProjectBlockType.Funding)) {
            throw new ValidationException("Funding block not present on this template");
        }

        if (flags == null) {
            throw new ValidationException(
                    "FundingSpendingTypeFlags is mandatory, values are showCapitalGLA, showRevenueOther etc");
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

        auditService.auditCurrentUserActivity(
                String.format("Updated template with ID: %d to use %s for the funding block", template.getId(), flags));
        save(template);

        for (Project project : allByTemplate) {
            updateProjectSpend(project, flags, deleteBlockData, deleteGLACapital, deleteOtherCapital, deleteGLARevenue,
                    deleteOtherRevenue);
        }
    }

    private boolean updateProjectSpend(Project project, FundingSpendingTypeFlags flags, boolean deleteBlockData,
            boolean deleteGLACapital, boolean deleteOtherCapital, boolean deleteGLARevenue, boolean deleteOtherRevenue) {
        boolean projectModified = false;
        FundingBlock projectBlock = (FundingBlock) project.getSingleLatestBlockOfType(ProjectBlockType.Funding);
        updateProjectWithNewFlags(project, flags);
        projectService.updateProject(project);

        if (deleteBlockData) {
            List<ProjectLedgerEntry> allByBlockId = financeService.findAllByBlockId(projectBlock.getId());

            for (ProjectLedgerEntry ledgerEntry : allByBlockId) {
                boolean delete = false;
                if (deleteGLACapital && ledgerEntry.getSpendType().equals(SpendType.CAPITAL)
                        && ledgerEntry.getCategory() == null) {
                    delete = true;
                }
                if (deleteOtherCapital && ledgerEntry.getSpendType().equals(SpendType.CAPITAL) && MATCH_FUND_CATEGORY
                        .equals(ledgerEntry.getCategory())) {
                    delete = true;
                }

                if (deleteGLARevenue && ledgerEntry.getSpendType().equals(SpendType.REVENUE)
                        && ledgerEntry.getCategory() == null) {
                    delete = true;
                }
                if (deleteOtherRevenue && ledgerEntry.getSpendType().equals(SpendType.REVENUE) && MATCH_FUND_CATEGORY
                        .equals(ledgerEntry.getCategory())) {
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

    public void updateProjectDetailsTemplate(Integer templateId, ProjectDetailsTemplateBlock details) {
        Template template = find(templateId);
        ProjectDetailsTemplateBlock projectDetailsTemplateBlock = (ProjectDetailsTemplateBlock) template.getSingleBlockByType(ProjectBlockType.Details);
        if(details.getMaxBoroughs() != null && details.getMaxBoroughs() > 0) {
            projectDetailsTemplateBlock.setMaxBoroughs(details.getMaxBoroughs());
        } else {
            throw new ValidationException("Cannot update maxBoroughs with " + details.getMaxBoroughs() + ". At least 1 borough needed.");
        }
        if(details.getAllocationQuestion() != null && !details.getAllocationQuestion().trim().isEmpty()) {
            projectDetailsTemplateBlock.setAllocationQuestion(details.getAllocationQuestion());
        } else {
            throw new ValidationException("Cannot update allocationQuestion label with an empty text.");
        }
        validateTemplate(template);
        save(template);
    }

    public List<TemplateSummary> getTemplatesSummaries() {
        return templateSummaryRepository.findAll().stream()
                .sorted(Comparator.comparingInt(TemplateSummary::getId))
                .collect(Collectors.toList());
    }

    public Page<TemplateSummary> getTemplateSummaries(String templateText, String programmeText,
            List<Template.TemplateStatus> selectedTemplateStatuses, Pageable pageable) {
        if (!StringUtils.isEmpty(templateText) && !StringUtils.isEmpty(programmeText)) {
            throw new ValidationException("Can't search by template and programme text at the same time");
        }

        Page<TemplateSummary> templates;
        if (StringUtils.isEmpty(templateText) && StringUtils.isEmpty(programmeText)) {
            templates = selectedTemplateStatuses == null ? templateSummaryRepository.findAll(pageable)
                    : templateSummaryRepository.findAllByTemplateStatusIn(selectedTemplateStatuses, pageable);
        } else if (!StringUtils.isEmpty(templateText)) {
            templates = getTemplatesByTemplateText(templateText, selectedTemplateStatuses, pageable);
        } else {
            templates = getTemplatesByProgrammeText(programmeText, pageable);
        }

        for (TemplateSummary template : templates) {
            template.setProgrammes(programmeService.getProgrammeDetailsSummariesByTemplate(template.getId()));
        }
        return templates;
    }

    public Integer createDraft(String templateJson) {
        Template toSave = new Template();
        return updateDraftTemplate(toSave, templateJson);
    }

    public void validateTemplate(String templateJson) {
        try {
            Template template = mapper.readValue(templateJson, Template.class);
            template.setStatus(Template.TemplateStatus.Draft);
            if (template.getStateModel() == null) {
                throw new ValidationException("No state model is present!");
            }
            validateTemplate(template);
        } catch (IOException e) {
            throw new ValidationException("Unable to parse JSON Request: " + e.getMessage());
        }
    }

    public void updateTemplate(Integer toSave, String templateJson) {
        Template template = templateRepository.getOne(toSave);
        Integer[] projectIds = projectRepository.findAllIdByTemplateId(template.getId());
        if (projectIds.length > 0) {
            throw new ValidationException("Can't update template because it is used in projects: " + projectIds);
        }
        if (template.getStatus() == Template.TemplateStatus.Active) {
            updateActiveTemplate(template, templateJson);
        } else {
            updateDraftTemplate(template, templateJson);
        }
    }

    public void updateDraftTemplate(Integer toSave, String templateJson) {
        Template template = templateRepository.getOne(toSave);
        updateDraftTemplate(template, templateJson);
    }

    protected Integer updateDraftTemplate(Template toSave, String templateJson) {
        copyValuesFromTemplateJson(toSave, templateJson);
        toSave.setStatus(Template.TemplateStatus.Draft);
        templateRepository.save(toSave);
        return toSave.getId();
    }

    protected Integer updateActiveTemplate(Template existingTemplate, String newTemplateJson) {
        copyValuesFromTemplateJson(existingTemplate, newTemplateJson);
        save(existingTemplate);
        return existingTemplate.getId();
    }

    public Integer updateInUseTemplate(Integer id, String newTemplateJson) throws JsonProcessingException {
        Template existingTemplate = templateRepository.getOne(id);
        Template newTemplate = mapper.readValue(newTemplateJson, Template.class);
        existingTemplate.setProgrammeAllocation(newTemplate.isProgrammeAllocation());
        return save(existingTemplate).getId();
    }

    private void copyValuesFromTemplateJson(Template toSave, String templateJson) {
        try {
            Template templateFromJson = mapper.readValue(templateJson, Template.class);
            templateFromJson.setStatus(Template.TemplateStatus.Draft);
            validateTemplate(templateFromJson);
            templateJson = mapper.writeValueAsString(templateFromJson);
            toSave.setJson(templateJson);
            toSave.setName(templateFromJson.getName());
            toSave.setAuthor(templateFromJson.getAuthor());
            toSave.setStateModel(templateFromJson.getStateModel());
            toSave.setCreatedBy(currentUsername());
            toSave.setCreatedOn(environment.now());
            toSave.setNumberOfProjectAllowedPerOrg(templateFromJson.getNumberOfProjectAllowedPerOrg());
            if (toSave.getId() != null && toSave.getStatus() == Template.TemplateStatus.Active) {
                //Clear existing data and copy values from template json
                templateFromJson.cloneIntoTemplate(toSave, templateFromJson.getName());
            }
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

        FundingTemplateBlock templateFundingBlock = (FundingTemplateBlock) template
                .getSingleBlockByType(ProjectBlockType.Funding);

        if (templateFundingBlock == null) {
            throw new NotFoundException("Could not find funding block on template " + templateId);
        }

        if (flags.budgetEvidenceAttachmentEnabled != null) {
            templateFundingBlock.setBudgetEvidenceAttachmentEnabled(flags.budgetEvidenceAttachmentEnabled);
        }

        if (flags.multipleBespokeActivitiesEnabled != null) {
            templateFundingBlock.setMultipleBespokeActivitiesEnabled(flags.multipleBespokeActivitiesEnabled);
        }

        save(template);

    }

    public void updateBaselineForOutputsBlock(Integer templateId, Boolean showBaseline) {
        Template template = find(templateId);

        OutputsTemplateBlock outputsTemplateBlock = (OutputsTemplateBlock) template
                .getSingleBlockByType(ProjectBlockType.Outputs);

        outputsTemplateBlock.setShowBaselines(showBaseline);
        save(template);

    }

    public void updateAssumptionsForOutputsBlock(Integer templateId, Boolean showAssumptions) {
        Template template = find(templateId);

        OutputsTemplateBlock outputsTemplateBlock = (OutputsTemplateBlock) template
                .getSingleBlockByType(ProjectBlockType.Outputs);

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
            List<NamedProjectBlock> allQuestionBlocks = project
                    .getBlocksByTypeAndDisplayOrder(ProjectBlockType.Questions, displayOrder);

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
                        if (StringUtils.isNotEmpty(newBlock.getProgressUpdate())) {
                            continue;
                        }
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

                    newBlock.setReportingVersion(questionsBlock.isReportingVersion());

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

    public void migrateContactDetails(Integer templateId, Integer questionsBlockDisplayOrder, Integer contactNameQuestionId,
            Integer contactEmailQuestionId) {
        Template template = find(templateId);
        templateProjectService
                .migrateContactDetails(template, questionsBlockDisplayOrder, contactNameQuestionId, contactEmailQuestionId);
    }

    private void updateBlockInfoMessage(Integer templateId, Integer displayOrder, String infoMessage, boolean internal) {
        if (internal) {
            updateInternalBlockInfoMessage(templateId, displayOrder, infoMessage);
        } else {
            updateInfoMessage(templateId, displayOrder, infoMessage);
        }
    }

    private void updateInfoMessage(Integer templateId, Integer displayOrder, String infoMessage) {
        Template template = find(templateId);
        TemplateBlock block = template.getSingleBlockByDisplayOrder(displayOrder);

        if (null == block) {
            throw new NotFoundException("Could not find template block for display order " + displayOrder);
        }
        block.setInfoMessage(infoMessage);
        templateProjectService.updateInfoMessage(template, displayOrder, infoMessage);

        templateRepository.save(template);
    }

    private void updateInternalBlockInfoMessage(Integer templateId, Integer displayOrder, String infoMessage) {
        Template template = find(templateId);
        InternalTemplateBlock block = template.getSingleInternalBlockByDisplayOrder(displayOrder);

        if (null == block) {
            throw new NotFoundException("Could not find template block for display order " + displayOrder);
        }
        block.setInfoMessage(infoMessage);
        templateProjectService.updateInternalBlockInfoMessage(template, displayOrder, infoMessage);

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
        TemplateBlock singleBlockByType = template.getSingleBlockByType(ProjectBlockType.DeliveryPartners);
        Map<String, String> map = null;
        if (singleBlockByType != null) {
            Set<DeliverableType> availableDeliverableTypes = ((DeliveryPartnersTemplateBlock) singleBlockByType)
                    .getAvailableDeliverableTypes();
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
        FundingTemplateBlock templateFundingBlock = (FundingTemplateBlock) template
                .getSingleBlockByType(ProjectBlockType.Funding);

        if (templateFundingBlock == null) {
            throw new NotFoundException("Could not find funding block on template " + templateId);
        }

        templateFundingBlock.setMonetaryValueScale(monetaryValueScale);
        templateProjectService.updateFundingBlockMonetaryValueScale(template, monetaryValueScale);
        save(template);
    }

    public void createProcessingRoute(Integer templateId, Integer displayOrder, ProcessingRoute processingRoute) {
        Template template = templateRepository.findById(templateId).orElse(null);
        if (template == null) {
            throw new ValidationException("Could not find template with id " + templateId);
        }

        if (template.getStatus() != Template.TemplateStatus.Active) {
            throw new ValidationException("Template id " + templateId + " is not active");
        }

        TemplateBlock block = template.getSingleBlockByTypeAndDisplayOrder(ProjectBlockType.Milestones, displayOrder);
        if (block == null) {
            throw new ValidationException("Could not find milestones block for template id " + templateId);
        }

        if (processingRoute.getId() != null) {
            throw new ValidationException("Cannot edit existing processing route!");
        }

        MilestonesTemplateBlock milestonesBlock = (MilestonesTemplateBlock) block;
        milestonesBlock.getProcessingRoutes().add(processingRoute);

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

    public Set<StateModel> getAvailableStateModelsForManagingOrgIds(Set<Integer> managingOrgIds) {
        return templateRepository.getAvailableStateModelsForManagingOrgIds(managingOrgIds);
    }

    public void disassociateBlock(Integer templateId, Integer blockId) {
        Template template = templateRepository.getOne(templateId);
        TemplateBlock templateBlock = template.getBlockById(blockId);
        templateBlock.setDetachedTemplateId(templateId);
        Integer[] allByTemplate = templateProjectService.findAllIdByTemplate(template);

        templateProjectService.disassociateBlock(allByTemplate, templateBlock.getDisplayOrder());

        templateRepository.copyTemplateIdToDetachedID(templateId, templateBlock.getDisplayOrder());
        auditService.auditCurrentUserActivity(
                String.format("Removed block %d (%s) from template %d", blockId, templateBlock.getBlock().name(), templateId));
    }

    public void disassociateInternalBlock(Integer templateId, Integer blockId) {
        Template template = templateRepository.getOne(templateId);
        InternalTemplateBlock templateBlock = template.getInternalBlockById(blockId);
        templateBlock.setDetachedTemplateId(templateId);
        Integer[] allByTemplate = templateProjectService.findAllIdByTemplate(template);

        templateProjectService.disassociateInternalBlock(allByTemplate, templateBlock.getDisplayOrder());

        templateRepository.copyTemplateIdToDetachedIDForInternalBlock(templateId, templateBlock.getDisplayOrder());
        auditService.auditCurrentUserActivity(
                String.format("Removed internal block %d (%s) from template %d",
                        blockId, templateBlock.getBlockDisplayName(), templateId));
    }

    public void reattachDisassociateBlock(Integer templateId, Integer blockId) {

        templateRepository.reattachRemovedBlock(templateId, blockId);
        Template template = templateRepository.getOne(templateId);

        TemplateBlock templateBlock = template.getBlockById(blockId);
        Integer[] allByTemplate = templateProjectService.findAllIdByTemplate(template);

        templateProjectService.reattachRemovedBlock(allByTemplate, templateBlock.getDisplayOrder());

        auditService.auditCurrentUserActivity(
                String.format("Reattached removed block %d (%s) from template %d", blockId, templateBlock.getBlock().name(),
                        templateId));

    }

    public void reattachDisassociateInternalBlock(Integer templateId, Integer blockId) {

        templateRepository.reattachRemovedInternalBlock(templateId, blockId);
        Template template = templateRepository.getOne(templateId);

        InternalTemplateBlock templateBlock = template.getInternalBlockById(blockId);
        Integer[] allByTemplate = templateProjectService.findAllIdByTemplate(template);

        templateProjectService.reattachRemovedInternalBlock(allByTemplate, templateBlock.getDisplayOrder());

        auditService.auditCurrentUserActivity(
                String.format("Reattached removed block %d (%s) from template %d", blockId, templateBlock.getBlockDisplayName(),
                        templateId));

    }

    public void updateFundingVariationsEnabled(Integer templateId, Integer displayOrder, boolean enabled) {
        Template template = find(templateId);
        FundingClaimsTemplateBlock fundingClaimsTemplateBlock = getFundingClaimTemplateBlock(template, displayOrder);
        fundingClaimsTemplateBlock.setFundingVariationsEnabled(enabled);
        save(template);
    }

    public void updateCanClaimActivity(Integer templateId, Integer displayOrder, boolean canClaimActivity) {
        Template template = find(templateId);
        TemplateBlock templateBlock = template.getSingleBlockByTypeAndDisplayOrder(ProjectBlockType.Funding, displayOrder);
        if (templateBlock == null || !(templateBlock instanceof FundingTemplateBlock)) {
            throw new ValidationException("Template doesn't contain funding block at display order " + displayOrder);
        }

        FundingTemplateBlock fundingTemplateBlock = (FundingTemplateBlock) templateBlock;

        fundingTemplateBlock.setCanClaimActivity(canClaimActivity);

        List<Project> allByTemplate = templateProjectService.findAllByTemplate(template);
        for (Project project : allByTemplate) {
            updateCanClaimActivity(project, displayOrder, canClaimActivity);
        }
        save(template);
    }

    public void updateShowComments(Integer templateId, Integer displayOrder, boolean showComments) {
        Template template = find(templateId);
        InternalTemplateBlock templateBlock = template
                .getInternalBlockByTypeAndDisplayOrder(InternalBlockType.Questions, displayOrder);

        if (templateBlock == null || !(templateBlock instanceof InternalQuestionsTemplateBlock)) {
            throw new ValidationException("Template doesn't contain question block at display order " + displayOrder);
        }

        InternalQuestionsTemplateBlock questionTemplateBlock = (InternalQuestionsTemplateBlock) templateBlock;
        questionTemplateBlock.setShowComments(showComments);

        save(template);
    }

    public void updateProjectSubmissionReminder(Integer templateId, boolean projectSubmissionReminder) {
        Template template = find(templateId);
        template.setProjectSubmissionReminder(projectSubmissionReminder);
        save(template);
    }

    public void updateShowOtherAffordableQuestion(Integer templateId, ProjectBlockType grantBlockType, boolean enable) {
        Template template = find(templateId);
        TemplateBlock templateBlock = template.getSingleBlockByType(grantBlockType);

        if (templateBlock == null || !(templateBlock instanceof BaseGrantTemplateBlock)) {
            throw new ValidationException("Template doesn't contain " + grantBlockType);
        }

        BaseGrantTemplateBlock grantTemplateBlock = (BaseGrantTemplateBlock) templateBlock;
        grantTemplateBlock.setShowOtherAffordableQuestion(enable);

        save(template);
    }

    private void updateCanClaimActivity(Project project, Integer displayOrder, boolean canClaimActivity) {
        List<NamedProjectBlock> blocks = project.getBlocksByTypeAndDisplayOrder(ProjectBlockType.Funding, displayOrder);
        for (NamedProjectBlock block : blocks) {
            FundingBlock fundingBlock = (FundingBlock) block;

            if (canClaimActivity && fundingBlock.getClaimed().size() > 0) {
                throw new ValidationException(String.format("Project %s contains pending claims", project.getId()));
            }

            if (!canClaimActivity && fundingBlock.getClaims().size() > 0) {
                throw new ValidationException(String.format("Project %s contains claims", project.getId()));
            }

            fundingBlock.setCanClaimActivity(canClaimActivity);
        }
        projectRepository.save(project);
    }

    public void updateFundingClaimPeriod(Integer templateId, int displayOrder, Integer periodId, String periodText) {
        Template template = find(templateId);
        FundingClaimsTemplateBlock fundingClaimsTemplateBlock = getFundingClaimTemplateBlock(template, displayOrder);
        fundingClaimsTemplateBlock.setPeriods(updatePeriodText(fundingClaimsTemplateBlock.getPeriods(), periodId, periodText));
        save(template);
    }

    private FundingClaimsTemplateBlock getFundingClaimTemplateBlock(Template template, int displayOrder) {
        TemplateBlock templateBlock = template.getSingleBlockByTypeAndDisplayOrder(ProjectBlockType.FundingClaims, displayOrder);
        if (templateBlock == null || !(templateBlock instanceof FundingClaimsTemplateBlock)) {
            throw new ValidationException("Template doesn't contain funding claims block at display order " + displayOrder);
        }
        return (FundingClaimsTemplateBlock) templateBlock;
    }

    private List<FundingClaimPeriod> updatePeriodText(List<FundingClaimPeriod> fundingClaimPeriod, Integer periodId,
            String periodText) {
        fundingClaimPeriod.forEach(p -> {
            if (p.getPeriod().equals(periodId)) {
                p.setText(periodText);
            }
        });
        return fundingClaimPeriod;
    }

    public void addFundingClaimCategory(Integer templateId, Integer blockDisplayOrder, FundingClaimCategory category) {
        Template template = find(templateId);
        FundingClaimsTemplateBlock fundingClaimsTemplateBlock = getFundingClaimTemplateBlock(template, blockDisplayOrder);
        fundingClaimsTemplateBlock.getCategories().add(category);
        save(template);
        templateProjectService.addFundingClaimCategory(template, fundingClaimsTemplateBlock, category);
    }

    public void addFundingClaimSubCategory(Integer templateId, Integer blockDisplayOrder, Integer categoryId, FundingClaimCategory subcategory) {
        Template template = find(templateId);
        FundingClaimsTemplateBlock fundingClaimsTemplateBlock = getFundingClaimTemplateBlock(template, blockDisplayOrder);
        FundingClaimCategory category = fundingClaimsTemplateBlock.getCategory(categoryId);
        if(category != null) {
            Set<FundingClaimCategory> subcategories = category.getSubCategories();
            if(subcategories.stream().anyMatch(entry -> entry.getId().equals(subcategory.getId()))) {
                throw new ValidationException("There is already one subcategory with id " + subcategory.getId());
            }
            if(subcategories.stream().anyMatch(entry -> entry.getDisplayOrder().equals(subcategory.getDisplayOrder()))) {
                throw new ValidationException("There is already one subcategory with display order " + subcategory.getDisplayOrder());
            }
            subcategories.add(subcategory);
            save(template);
            templateProjectService.addFundingClaimSubCategory(template, fundingClaimsTemplateBlock, category, subcategory);
        } else {
            throw new ValidationException("Template doesn't contain funding claim category with id " + categoryId);
        }
    }

    public void updateFundingClaimCategoryDisplayOrder(Integer templateId, Integer blockDisplayOrder, Integer categoryId,
            Integer newDisplayOrder) {
        Template template = find(templateId);
        FundingClaimsTemplateBlock fundingClaimsTemplateBlock = getFundingClaimTemplateBlock(template, blockDisplayOrder);
        FundingClaimCategory category = fundingClaimsTemplateBlock.getCategory(categoryId);
        if (category == null) {
            throw new NotFoundException("Cannot find category with ID " + categoryId);
        }
        category.setDisplayOrder(newDisplayOrder);
        save(template);
        templateProjectService.updateFundingClaimCategoryDisplayOrder(template, categoryId, newDisplayOrder);
    }

    private LearningGrantTemplateBlock getLearningGrantTemplateBlock(Template template, int displayOrder) {
        TemplateBlock templateBlock = template.getSingleBlockByTypeAndDisplayOrder(ProjectBlockType.LearningGrant, displayOrder);
        if (templateBlock == null || !(templateBlock instanceof LearningGrantTemplateBlock)) {
            throw new ValidationException("Template doesn't contain learning grant block at display order " + displayOrder);
        }
        return (LearningGrantTemplateBlock) templateBlock;
    }

    public void addLearningGrantAllocationType(Integer templateId, Integer blockDisplayOrder, AllocationType allocationType) {
        Template template = find(templateId);
        LearningGrantTemplateBlock learningGrantTemplateBlock = getLearningGrantTemplateBlock(template, blockDisplayOrder);
        if (learningGrantTemplateBlock.getAllocationTypes().contains(allocationType)) {
            throw new ValidationException("Could not add allocation type because already exists.");
        }
        learningGrantTemplateBlock.getAllocationTypes().add(allocationType);
        save(template);
        templateProjectService.addLearningGrantAllocationType(template, learningGrantTemplateBlock, allocationType);
    }

    public Set<Integer> getSubmissionReminderTemplates() {
        return templateRepository.findAllByTemplateServiceReminder(true);
    }

    public List<BlockUsage> getBlockUsage(ProjectBlockType projectBlockType, InternalBlockType internalBlockType) {
        String sql = null;
        if (projectBlockType != null) {
            sql = "select distinct t.id, t.name, p.id, p.name, o.name "
                    + "from template_block tb "
                    + "inner join template t on t.id = tb.template_id "
                    + "inner join programme_template pb on pb.template_id = t.id "
                    + "inner join programme p on p.id = pb.programme_id "
                    + "inner join organisation o on o.id = p.managing_organisation_id "
                    + "where block = '" + projectBlockType + "'";
        } else if (internalBlockType != null) {
            sql = "select distinct t.id, t.name, p.id, p.name, o.name "
                    + "from internal_template_block tb "
                    + "inner join template t on t.id = tb.template_id "
                    + "inner join programme_template pb on pb.template_id = t.id "
                    + "inner join programme p on p.id = pb.programme_id "
                    + "inner join organisation o on o.id = p.managing_organisation_id "
                    + "where type = '" + internalBlockType + "'";
        }

        if (sql != null) {
            return jdbc.query(sql, (resultSet, i) -> new BlockUsage(resultSet.getInt(1), resultSet.getString(2),
                    resultSet.getInt(3), resultSet.getString(4), resultSet.getString(5)));
        } else {
            return Collections.emptyList();
        }
    }

    public void deleteMilestone(Integer templateId, Integer blockDisplayOrder, Integer processingRouteId, Integer milestoneId,
            boolean updateActiveProjects) {
        Template template = find(templateId);
        ProcessingRoute processingRoute = getProcessingRoute(template, blockDisplayOrder, processingRouteId);
        processingRoute.getMilestones().removeIf(m -> m.getExternalId().equals(milestoneId));
        save(template);
        templateProjectService
                .deleteMilestone(template, blockDisplayOrder, processingRoute.getId(), milestoneId, updateActiveProjects);
    }

    public void addMilestone(Integer templateId, Integer blockDisplayOrder, Integer processingRouteId,
            MilestoneTemplate milestoneTemplate, boolean updateActiveProjects) {
        Template template = find(templateId);
        ProcessingRoute processingRoute = getProcessingRoute(template, blockDisplayOrder, processingRouteId);
        processingRoute.getMilestones().add(milestoneTemplate);
        save(template);
        templateProjectService
                .addMilestone(template, blockDisplayOrder, processingRoute.getId(), milestoneTemplate, updateActiveProjects);
    }

    private ProcessingRoute getProcessingRoute(Template template, Integer blockDisplayOrder, Integer processingRouteId) {
        TemplateBlock block = template.getSingleBlockByTypeAndDisplayOrder(ProjectBlockType.Milestones, blockDisplayOrder);
        if (block == null) {
            throw new NotFoundException("could not find milestones block with display order " + blockDisplayOrder);
        }

        MilestonesTemplateBlock milestonesBlock = (MilestonesTemplateBlock) block;
        ProcessingRoute processingRoute = milestonesBlock.getProcessingRoutes().stream()
                .filter(pr -> processingRouteId.equals(pr.getExternalId())).findFirst().orElse(null);
        if (processingRoute == null) {
            throw new NotFoundException("could not find processing route with id " + processingRouteId);
        }

        return processingRoute;
    }

    public void specifyOfWhichCategories(Integer templateId, Integer blockDisplayOrder,
            List<AffordableHomesOfWhichCategory> items) {
        Template template = find(templateId);
        TemplateBlock block =
                template.getSingleBlockByTypeAndDisplayOrder(ProjectBlockType.AffordableHomes, blockDisplayOrder);
        if (block == null) {
            throw new NotFoundException("could not find AffordableHomes block with display order " + blockDisplayOrder);
        }

        AffordableHomesTemplateBlock affTemplate = (AffordableHomesTemplateBlock) block;
        List<AffordableHomesOfWhichCategory> existing = new ArrayList<>(affTemplate.getOfWhichCategories());
        affTemplate.setOfWhichCategories(items);

        save(template);
        templateProjectService.updateOWhichCategories(template, blockDisplayOrder, existing, items);
    }

    public void updateCompletionOnlyAvailability(Integer templateId, Integer blockDisplayOrder,
            boolean completionOnly) {
        Template template = find(templateId);
        TemplateBlock block =
                template.getSingleBlockByTypeAndDisplayOrder(ProjectBlockType.AffordableHomes, blockDisplayOrder);
        if (block == null) {
            throw new NotFoundException("could not find AffordableHomes block with display order " + blockDisplayOrder);
        }

        AffordableHomesTemplateBlock affTemplate = (AffordableHomesTemplateBlock) block;
        affTemplate.setCompletionOnlyAvailable(completionOnly);

        save(template);
        templateProjectService.updateCompletionOnlyAvailability(template, blockDisplayOrder, completionOnly);
    }

    public Template performCommand(TemplateBlockCommand command, Integer templateId, boolean internalBlock, Integer displayOrder,
            String payloadString)
            throws IOException {
        CommandPayload payload = null;
        BlockData blockData = null;
        Template template = get(templateId);
        Commandable blockById = internalBlock ? template.getSingleInternalBlockByDisplayOrder(displayOrder)
                : template.getSingleBlockByDisplayOrder(displayOrder);
        if (StringUtils.isNotEmpty(payloadString)) {
            payload = mapper.readValue(payloadString, CommandPayload.class);
            blockData = payload.getBlockData();
        }
        if (command.isGlobal()) {
            switch (command) {
                case REMOVE_BLOCK:
                    if (internalBlock) {
                        this.disassociateInternalBlock(templateId, blockById.getId());
                    } else {
                        this.disassociateBlock(templateId, blockById.getId());
                    }
                    break;
                case UPDATE_DISPLAY_NAME:
                    this.updateBlockDisplayName(templateId, displayOrder, blockData.getBlockType(), blockData.getBlockOldName(),
                            blockData.getBlockNewName(), internalBlock);
                    break;
                case UPDATE_INFO_MESSAGE:
                    this.updateBlockInfoMessage(templateId, displayOrder, blockData.getInfoMessage(), internalBlock);
                    break;
                default:
                    break;
            }
        } else {
            switch (command) {
                case REMOVE_QUESTION:
                    if (internalBlock) {
                        this.removeInternalQuestion(templateId, displayOrder, blockData.getQuestionId(),
                                blockData.getInfoMessage());
                    } else {
                        this.removeQuestion(templateId, displayOrder, blockData.getQuestionId(), blockData.getInfoMessage());
                    }
                    break;
                case EDIT_MILESTONES:
                    this.updateMilestonesBlockMilestone(template, displayOrder, blockData.getProcessingRouteId(),
                            blockData.getMilestoneExternalId(), blockData.getMilestoneSummary(),
                            blockData.getMilestoneNaSelectable(), blockData.getMilestoneDisplayOrder(),
                            blockData.getMilestoneRequirement());
                    break;
                case EDIT_LEARNING_GRANT_LABELS:
                    this.updateLearningGrantBlockLabels(template, blockData.getLearningGrantLabels());
                    break;
                case EDIT_USER_DEFINED_OUTPUT_BLOCK:
                    this.updateUserDefinedOutputTemplateBlock(template.getId(), blockData.getUserDefinedOutputTemplateBlock());
                    break;
                default:
                    break;
            }
        }
        templateRepository.flush();
        entityManager.detach(template);
        return get(templateId);
    }

    void updateMilestonesBlockMilestone(Template template, Integer blockDisplayOrder, Integer processingRouteId,
            Integer milestoneExternalId, String milestoneSummary, Boolean naSelectable,
            Integer milestoneDisplayOrder, Requirement milestoneRequirement) {

        if (!Template.TemplateStatus.Active.equals(template.getStatus())) {
            throw new ValidationException("Can not update, template status is not Active " + template.getId());
        }

        TemplateBlock block = template.getSingleBlockByType(ProjectBlockType.Milestones);
        if (block == null) {
            throw new ValidationException("could not find milestones block in the template " + template.getId());
        }

        MilestonesTemplateBlock milestonesBlock = (MilestonesTemplateBlock) block;

        ProcessingRoute processingRoute = milestonesBlock.getProcessingRouteByExternalId(processingRouteId);
        if (processingRoute == null) {
            throw new ValidationException("could not find processing route with id " + processingRouteId);
        }

        MilestoneTemplate milestoneTemplate;
        if (milestoneExternalId != null) {
            milestoneTemplate = processingRoute.getMilestoneByExternalId(milestoneExternalId);
        } else {
            milestoneTemplate = processingRoute.getMilestoneBySummary(milestoneSummary);
        }
        if (milestoneTemplate == null) {
            throw new ValidationException("could not find template milestone with external id " + milestoneExternalId);
        }
        milestoneTemplate.setDisplayOrder(milestoneDisplayOrder);
        milestoneTemplate.setRequirement(milestoneRequirement);
        milestoneTemplate.setNaSelectable(naSelectable);

        save(template);

        templateProjectService.updateMilestonesBlockMilestone(template, blockDisplayOrder, processingRouteId, milestoneExternalId,
                milestoneSummary,
                milestoneDisplayOrder, milestoneRequirement, naSelectable);
        auditService.auditCurrentUserActivity(
                String.format("Template: %d, milestone block, changed milestone table entry with id %d. Display order: %d,"
                                + " requirement: %s, na selectable: %b",
                        template.getId(), milestoneExternalId, milestoneDisplayOrder, milestoneRequirement.name(), naSelectable));
    }

    void updateLearningGrantBlockLabels(Template template, LearningGrantLabels labels) {
        if (!Template.TemplateStatus.Active.equals(template.getStatus())) {
            throw new ValidationException("Can not update, template status is not Active " + template.getId());
        }

        TemplateBlock block = template.getSingleBlockByType(ProjectBlockType.LearningGrant);
        if (block == null) {
            throw new ValidationException("could not find Learning Grant block in the template " + template.getId());
        }

        LearningGrantTemplateBlock lgbTemplate = (LearningGrantTemplateBlock) block;
        lgbTemplate.setProfileTitle(labels.getProfileTitle());
        lgbTemplate.setAllocationTitle(labels.getAllocationTitle());
        lgbTemplate.setCumulativeAllocationTitle(labels.getCumulativeAllocationTitle());
        lgbTemplate.setCumulativeEarningsTitle(labels.getCumulativeEarningsTitle());
        lgbTemplate.setCumulativePaymentTitle(labels.getCumulativePaymentTitle());
        lgbTemplate.setPaymentDueTitle(labels.getPaymentDueTitle());

        save(template);
        auditService.auditCurrentUserActivity(String.format(
                "Template: %d, learning grant block, changed table column labels.", template.getId()));
    }

    public void updateDetailsTemplateConsortiumRequirements(Integer templateId, DetailsTemplate details) {
        Template template = find(templateId);
        template.getDetailsConfig().setDevelopmentLiabilityOrganisationRequirement(
                details.getDevelopmentLiabilityOrganisationRequirement());
        template.getDetailsConfig().setPostCompletionLiabilityOrganisationRequirement(
                details.getPostCompletionLiabilityOrganisationRequirement());
        validateTemplate(template);
        save(template);
        templateProjectService.updateDetailsTemplateConsortiumRequirements(template, details);
    }

    public void updateUserDefinedOutputTemplateBlock(Integer templateId, UserDefinedOutputTemplateBlock templateBlock) {
        Template template = find(templateId);
        UserDefinedOutputTemplateBlock existingBlock = (UserDefinedOutputTemplateBlock) template
                .getSingleBlockByType(ProjectBlockType.UserDefinedOutput);
        existingBlock.mergeConfig(templateBlock);
        save(template);
    }

    public void updateProjectObjectivesTemplateBlock(Integer templateId, ProjectObjectivesTemplateBlock templateBlock) {
        Template template = find(templateId);
        ProjectObjectivesTemplateBlock existingBlock = (ProjectObjectivesTemplateBlock) template
                .getSingleBlockByType(ProjectBlockType.ProjectObjectives);
        existingBlock.mergeConfig(templateBlock);
        save(template);
    }

    public void updateProjectElementsTemplateBlock(Integer templateId, ProjectElementsTemplateBlock templateBlock) {
        Template template = find(templateId);
        ProjectElementsTemplateBlock existingBlock = (ProjectElementsTemplateBlock) template
                .getSingleBlockByType(ProjectBlockType.ProjectElements);
        existingBlock.mergeConfig(templateBlock);
        save(template);
    }

    public void updateOtherFundingTemplateBlock(Integer templateId, OtherFundingTemplateBlock templateBlock) {
        Template template = find(templateId);
        OtherFundingTemplateBlock existingBlock = (OtherFundingTemplateBlock) template
                .getSingleBlockByType(ProjectBlockType.OtherFunding);
        existingBlock.mergeConfig(templateBlock);
        save(template);
    }
}
