/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.exception.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.london.ops.Environment;
import uk.gov.london.ops.domain.outputs.OutputConfigurationGroup;
import uk.gov.london.ops.domain.project.ProjectBlockType;
import uk.gov.london.ops.domain.template.*;
import uk.gov.london.ops.exception.NotFoundException;
import uk.gov.london.ops.exception.ValidationException;
import uk.gov.london.ops.repository.OutputConfigurationGroupRepository;
import uk.gov.london.ops.repository.TemplateDataRepository;
import uk.gov.london.ops.repository.TemplateRepository;
import uk.gov.london.ops.service.project.ProjectService;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

//import static uk.gov.london.ops.di.TemplateDataInitialiser.DEFAULT_CONFIG_GROUP_ID;
import static uk.gov.london.ops.domain.template.AnswerType.Dropdown;
import static uk.gov.london.ops.domain.template.AnswerType.YesNo;

@Service
public class TemplateService implements CRUDServiceInterface<Integer,Template> {

    Logger log = LoggerFactory.getLogger(getClass());

    private static final int BLOCK_NAME_MAX_SIZE = 40;
    static final int MAX_NB_QUESTION_BLOCKS = 50;
    private static final List<AnswerType> CONDITIONAL_ANSWER_TYPES = Arrays.asList(YesNo, Dropdown);

    @Autowired
    TemplateRepository templateRepository;

    @Autowired
    TemplateDataRepository templateDataRepository;

    @Autowired
    ContractService contractService;

    @Autowired
    QuestionService questionService;

    @Autowired
    ProjectService projectService;

    @Autowired
    UserService userService;

    @Autowired
    Environment environment;

    @Autowired
    JdbcTemplate jdbc;

    private Set<ProjectBlockType> allowedBlockTypesToAdd = new HashSet<>(Arrays.asList(
            ProjectBlockType.UnitDetails,
            ProjectBlockType.Questions,
            ProjectBlockType.Risks,
            ProjectBlockType.GrantSource
    ));

    @Autowired
    OutputConfigurationGroupRepository outputConfigurationGroupRepository;

    @Autowired
    ObjectMapper mapper;

    public List<Template> findAll() {
        return templateRepository.findAll();
    }

    //public List<Template> findAllByJsonNull() {
    //    return templateRepository.findAllByJsonNull();
    //}

    public String getTemplateJson(Integer id) throws IOException {
        Template template = templateRepository.findOne(id);

        if (template == null) {
            throw new NotFoundException();
        }

        //return template.getJson() != null ? template.getJson() : mapper.writeValueAsString(template);
        return mapper.writeValueAsString(template);
    }

    public Template find(Integer id) {
        Template template = templateRepository.findOne(id);

        if (template == null) {
            throw new NotFoundException();
        }

        return template;
    }

    public Template findByName(String name) {
        return templateRepository.findByName(name);
    }

    public Template create(String templateJson) throws IOException {
        Template template = mapper.readValue(templateJson, Template.class);

        if (template.getId() != null) {
            throw new ValidationException("cannot edit existing template!");
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
                    OutputConfigurationGroup group = outputConfigurationGroupRepository.getOne(-1);
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
                throw new ValidationException("Cannot create a template with more than "+MAX_NB_QUESTION_BLOCKS+" additional question blocks");
            }
            for (TemplateBlock  tb : blocksByType) {
                QuestionsTemplateBlock questionBlock = (QuestionsTemplateBlock) tb;

                int questionCount = questionBlock.getQuestions().size();
                int uniqueQuestions = questionBlock.getQuestions().stream().map(t -> t.getQuestion().getId()).collect(Collectors.toSet()).size();
                if (questionCount != uniqueQuestions) {
                    throw new ValidationException("Cannot create a template with duplicated questions");
                }

                for (TemplateQuestion question: questionBlock.getQuestions()) {
                    if (question.getParentId() != null) {
                        TemplateQuestion parentQuestion = questionBlock.getQuestionById(question.getParentId());
                        if (!CONDITIONAL_ANSWER_TYPES.contains(parentQuestion.getQuestion().getAnswerType())) {
                            throw new ValidationException("conditional questions can only be on YesNo or Dropdown!");
                        }
                    }
                }

                updateQuestionTemplates(questionBlock.getQuestions());
            }
        }



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

    public void updateJson(Template template) {
        try {
            String json = mapper.writeValueAsString(template);
            jdbc.update("UPDATE template SET json = ? WHERE id = ?", json, template.getId());
        } catch (JsonProcessingException e) {
            log.error("Could not serialise template to JSON, id="+template.getId(), e);
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

    private void validateTemplate(Template template) {
        for (TemplateBlock tb: template.getBlocksEnabled()) {
            if (tb.getBlockDisplayName() != null && tb.getBlockDisplayName().length() > BLOCK_NAME_MAX_SIZE) {
                throw new ValidationException("Cannot have a block name with a name longer thant "+BLOCK_NAME_MAX_SIZE+"! ("+tb.getBlockDisplayName()+")");
            }

            if (tb.getDisplayOrder() == null) {
                throw new ValidationException("Display order mandatory for block "+tb.getBlockDisplayName());
            }
        }
    }

    /**
     * Determines whether to load or crete template questions
     */
    Set<TemplateQuestion> updateQuestionTemplates(Set<TemplateQuestion> questions) {
        Set<TemplateQuestion> entities = new HashSet<>();
        if (questions != null) {
            for (TemplateQuestion templateQuestion: questions) {
                Question question;
                if (templateQuestion.getQuestion().getId() != null) {
                    question = questionService.findById(templateQuestion.getQuestion().getId());
                    if (question == null) {
                        HashSet<AnswerOption> options = new HashSet<>();
                        if (templateQuestion.getQuestion().getAnswerOptions()!=null) {
                            options.addAll(templateQuestion.getQuestion().getAnswerOptions());
                        }
                        question = questionService.createQuestion(templateQuestion.getQuestion().getId(),
                                templateQuestion.getQuestion().getText(), templateQuestion.getQuestion().getAnswerType(), options,
                                templateQuestion.getQuestion().getMaxLength(),  templateQuestion.getQuestion().getQuantity(), null);
                    }
                } else {
                    throw new ValidationException("Question IDs must be provided when uploading templates.");
                }

                TemplateQuestion newTemplateQuestion = new TemplateQuestion();
                newTemplateQuestion.setQuestion(question);
                newTemplateQuestion.setDisplayOrder(templateQuestion.getDisplayOrder());
                newTemplateQuestion.setRequirement(templateQuestion.getRequirement());
                newTemplateQuestion.setParentId(templateQuestion.getParentId());
                newTemplateQuestion.setParentAnswerToMatch(templateQuestion.getParentAnswerToMatch());
                newTemplateQuestion.setSectionId(templateQuestion.getSectionId());
                entities.add(newTemplateQuestion);
            }
        }
        return entities;
    }


    public void delete(Integer id) {
        try {
            templateRepository.delete(id);
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

        final Template template = templateRepository.findOne(templateId);
        if (template == null) {
            throw new ValidationException("Not found template with id " + templateId);
        }

        if (ProjectBlockType.Questions.equals(block.getBlock())) {
            setQuestionBlockData((QuestionsTemplateBlock) block);
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
        projectService.addBlockToProjectsByTemplate(template, savedBlock);
        return new AsyncResult<>(savedTemplate);
    }

    private void setQuestionBlockData(QuestionsTemplateBlock block) {
        final QuestionsTemplateBlock qtb = block;
        final Set<TemplateQuestion> templateQuestionSet = new HashSet<>();
        final Set<String> externalKeysUsedSet = new HashSet<>();
        for (final TemplateQuestion tq : qtb.getQuestions()) {
            String externalKey = tq.getQuestion().getExternalKey();
            if (externalKeysUsedSet.contains(externalKey)) {
                externalKey = "";
            } else {
                externalKeysUsedSet.add(externalKey);
            }
            final Question question = findOrCreateQuestion(
                    tq.getQuestion().getId(),
                    externalKey,
                    tq.getQuestion().getText(),
                    tq.getQuestion().getAnswerType(),
                    tq.getQuestion().getMaxLength(),
                    tq.getQuestion().getQuantity(),
                    tq.getQuestion().getAnswerOptions());
            final TemplateQuestion newTemplateQuestion = tq.copy();
            templateQuestionSet.add(newTemplateQuestion);
        }
        qtb.setQuestions(templateQuestionSet);
    }


    public Question findOrCreateQuestion(final Integer id,
                                         final String externalKey,
                                         final String text,
                                         final AnswerType answerType, final Integer maxLength,
                                         final Integer quantity,
                                         final Set<AnswerOption> answerOptions) {
        final Question savedQuestion = externalKey != null && !"".equals(externalKey) ?
                questionService.getByExternalKey(externalKey) : null;
        if(savedQuestion != null) {
            return savedQuestion;
        } else {
            final Question newQuestion = questionService.createQuestion(
                    id,
                    text,
                    answerType,
                    answerOptions,maxLength, quantity,
                    externalKey);
            questionService.flush();
            return newQuestion;
        }
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

    public void addQuestion(Integer templateId, int blockDisplayOrder, TemplateQuestion newTemplateQuestion) {
        Template template = find(templateId);

        TemplateBlock block = template.getSingleBlockByTypeAndDisplayOrder(ProjectBlockType.Questions, blockDisplayOrder);
        if (block == null) {
            throw new NotFoundException("could not find questions block with display order "+blockDisplayOrder);
        }

        int maxDisplayOrder = 0;
        QuestionsTemplateBlock questionsBlock = (QuestionsTemplateBlock) block;
        for (TemplateQuestion tq: questionsBlock.getQuestions()) {
            if (tq.getQuestion().getId().equals(newTemplateQuestion.getQuestion().getId())) {
                throw new ValidationException("question "+newTemplateQuestion.getQuestion().getId()+" already exists in template!");
            }
            maxDisplayOrder = Math.max(maxDisplayOrder, tq.getDisplayOrder());
        }

        // we want to force the question to be optional and to be displayed last
        newTemplateQuestion.setDisplayOrder(maxDisplayOrder+1);
        newTemplateQuestion.setRequirement(Requirement.optional);
        questionsBlock.getQuestions().add(newTemplateQuestion);

        template = save(template);

        questionsBlock = (QuestionsTemplateBlock) template.getSingleBlockByTypeAndDisplayOrder(ProjectBlockType.Questions, blockDisplayOrder);
        TemplateQuestion savedTemplateQuestion = questionsBlock.getQuestionById(newTemplateQuestion.getQuestion().getId());

        projectService.addQuestion(template, blockDisplayOrder, savedTemplateQuestion);
    }

    public void updateQuestionRequirement(Integer templateId, Integer blockDisplayOrder, Integer questionId, Requirement requirement) {
        Template template = find(templateId);

        TemplateBlock block = template.getSingleBlockByTypeAndDisplayOrder(ProjectBlockType.Questions, blockDisplayOrder);
        if (block == null) {
            throw new NotFoundException("could not find questions block with display order "+blockDisplayOrder);
        }

        QuestionsTemplateBlock questionsBlock = (QuestionsTemplateBlock) block;

        TemplateQuestion question = questionsBlock.getQuestionById(questionId);
        if (question == null) {
            throw new NotFoundException("could not find questions with id "+questionId);
        }

        question.setRequirement(requirement);

        save(template);
    }

    public void removeQuestion(Integer templateId, Integer blockDisplayOrder, Integer questionId) {
        Template template = find(templateId);

        TemplateBlock block = template.getSingleBlockByTypeAndDisplayOrder(ProjectBlockType.Questions, blockDisplayOrder);
        if (block == null) {
            throw new NotFoundException("could not find questions block with display order "+blockDisplayOrder);
        }

        QuestionsTemplateBlock questionsBlock = (QuestionsTemplateBlock) block;

        TemplateQuestion question = questionsBlock.getQuestionById(questionId);
        if (question == null) {
            throw new NotFoundException("could not find questions with id "+questionId);
        }

        questionsBlock.getQuestions().remove(question);

        save(template);

        projectService.removeQuestion(template, blockDisplayOrder, questionId);
    }

    public void updateAssociatedProjectsEnabled(Integer templateId, Boolean enabled) {
        Template template = find(templateId);
        template.setAssociatedProjectsEnabled(enabled);
        save(template);

        projectService.updateAssociatedProjectsEnabled(template, enabled);
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
        TemplateData data = templateDataRepository.findOne(templateId);
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
            throw new NotFoundException("could not find grant source block with display order "+blockDisplayOrder);
        }

        GrantSourceTemplateBlock grantBlock = (GrantSourceTemplateBlock) block;
        grantBlock.setGrantTotalText(text);

        save(template);
    }

    public void updateMilestoneDescriptionEnabled(Integer templateId, Integer blockDisplayOrder, boolean enabled) {
        Template template = find(templateId);

        TemplateBlock block = template.getSingleBlockByTypeAndDisplayOrder(ProjectBlockType.Milestones, blockDisplayOrder);
        if (block == null) {
            throw new NotFoundException("could not find milestones block with display order "+blockDisplayOrder);
        }

        MilestonesTemplateBlock milestonesBlock = (MilestonesTemplateBlock) block;
        milestonesBlock.setDescriptionEnabled(enabled);

        save(template);

        projectService.updateMilestoneDescriptionEnabled(template, blockDisplayOrder, enabled);
    }

    public void updateMilestoneAllowableEvidenceDocuments(Integer templateId, Integer blockDisplayOrder, Integer maxCount) {
        Template template = find(templateId);
        MilestonesTemplateBlock milestonesTemplateBlock = getMilestonesTemplateBlock(template, blockDisplayOrder);
        updateMilestoneAllowableEvidenceDocuments(template, milestonesTemplateBlock, maxCount, milestonesTemplateBlock.getEvidenceApplicability());
    }

    public void updateMilestoneAllowableEvidenceDocuments(Integer templateId, Integer blockDisplayOrder,  MilestonesTemplateBlock.EvidenceApplicability evidenceApplicability) {
        Template template = find(templateId);
        MilestonesTemplateBlock milestonesTemplateBlock = getMilestonesTemplateBlock(template, blockDisplayOrder);
        updateMilestoneAllowableEvidenceDocuments(template, milestonesTemplateBlock, milestonesTemplateBlock.getMaxEvidenceAttachments(), evidenceApplicability);
    }

    private MilestonesTemplateBlock getMilestonesTemplateBlock(Template template, Integer blockDisplayOrder) {
        TemplateBlock block = template.getSingleBlockByTypeAndDisplayOrder(ProjectBlockType.Milestones, blockDisplayOrder);
        if (block == null) {
            throw new NotFoundException("could not find milestones block with display order "+blockDisplayOrder);
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

        projectService.updateMilestoneEvidentialStatus(template , milestonesBlock.getDisplayOrder(), maxCount, evidenceApplicability);
    }

    public void updateMilestoneNaSelectable(Integer templateId, Integer blockDisplayOrder, Integer processingRouteId, Integer milestoneId, Boolean naSelectable) {
        Template template = find(templateId);

        TemplateBlock block = template.getSingleBlockByTypeAndDisplayOrder(ProjectBlockType.Milestones, blockDisplayOrder);
        if (block == null) {
            throw new NotFoundException("could not find milestones block with display order "+blockDisplayOrder);
        }

        MilestonesTemplateBlock milestonesBlock = (MilestonesTemplateBlock) block;
        ProcessingRoute processingRoute = milestonesBlock.getProcessingRoute(processingRouteId);
        if (processingRoute == null) {
            throw new NotFoundException("could not find processing route with id "+processingRouteId);
        }

        MilestoneTemplate milestoneTemplate = processingRoute.getMilestoneByExternalId(milestoneId);
        if (milestoneTemplate == null) {
            throw new NotFoundException("could not find template milestone with external id "+milestoneId);
        }
        milestoneTemplate.setNaSelectable(naSelectable);

        save(template);

        projectService.updateMilestoneNaSelectable(template, blockDisplayOrder, processingRouteId, milestoneId, naSelectable);
    }

}
