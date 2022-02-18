/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.project.template;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.london.ops.audit.AuditService;
import uk.gov.london.ops.file.AttachmentFile;
import uk.gov.london.ops.file.FileCategory;
import uk.gov.london.ops.file.FileService;
import uk.gov.london.ops.framework.enums.Requirement;
import uk.gov.london.ops.framework.environment.Environment;
import uk.gov.london.ops.framework.exception.NotFoundException;
import uk.gov.london.ops.framework.exception.ValidationException;
import uk.gov.london.ops.framework.feature.Feature;
import uk.gov.london.ops.framework.feature.FeatureStatus;
import uk.gov.london.ops.project.Project;
import uk.gov.london.ops.project.ProjectService;
import uk.gov.london.ops.project.implementation.repository.QuestionRepository;
import uk.gov.london.ops.project.implementation.repository.TemplateRepository;
import uk.gov.london.ops.project.question.Answer;
import uk.gov.london.ops.project.question.ProjectQuestionsBlock;
import uk.gov.london.ops.project.question.ProjectQuestionsService;
import uk.gov.london.ops.project.state.ProjectStatus;
import uk.gov.london.ops.project.template.domain.*;
import uk.gov.london.ops.user.UserService;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.*;

import static org.apache.commons.io.FileUtils.ONE_MB;
import static uk.gov.london.common.GlaUtils.parseInt;
import static uk.gov.london.ops.framework.OPSUtils.currentUsername;

/**
 * Service interface for managing the questions library.
 */
@Service
public class QuestionService {

    private static final int MIN_OPTIONS = 2;
    private static final int MAX_OPTIONS = 35;
    private static final int OPTION_MAX_LENGTH = 100;
    private static final int TEXT_MAX_LENGTH = 2000;
    private static final int MAX_FILE_QUANTITY = 2;

    @Autowired
    AuditService auditService;

    @Autowired
    ProjectService projectService;

    @Autowired
    ProjectQuestionsService projectQuestionsService;

    @Autowired
    QuestionRepository repository;

    @Autowired
    TemplateRepository templateRepository;

    @Autowired
    FeatureStatus featureStatus;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    Environment environment;

    @Autowired
    FileService fileService;

    @Autowired
    UserService userService;

    @Value("${max.file.size}")
    int maxFileSize;

    public Page<Question> getAll(String questionText, String templateText, boolean enrich, Pageable pageable) {
        if (!StringUtils.isEmpty(questionText) && !StringUtils.isEmpty(templateText)) {
            throw new ValidationException("Can't search by question and template text at the same time");
        }

        Page<Question> questions;

        if (!StringUtils.isEmpty(templateText)) {
            questions = getAllByTemplateText(templateText, pageable);
        } else {
            questions = getAllByQuestionText(questionText, pageable);
        }

        if (enrich) {
            for (Question question : questions) {
                enrich(question);
            }
        }

        return questions;
    }

    public Page<Question> getAllByTemplateText(String templateText, Pageable pageable) {
        String textToSearch = (templateText == null ? "" : templateText);
        Integer templateId = parseInt(templateText);
        Set<Integer> questionIds = repository.findQuestionsByTemplateIdOrText(templateId == null ? -1 : templateId, textToSearch);
        return repository.findByIdIn(questionIds, pageable);
    }


    public Page<Question> getAllByQuestionText(String questionText, Pageable pageable) {
        String textToSearch = questionText == null ? "" : questionText;
        Integer questionId = parseInt(questionText);
        return repository.findAllByIdOrTextContainingIgnoreCase(questionId, textToSearch, pageable);
    }

    public Question getById(Integer questionId) {
        return getById(questionId, false);
    }

    public Question getById(Integer questionId, boolean enrich) {
        Question question = repository.findById(questionId).orElse(null);

        if (question == null) {
            throw new NotFoundException("Unable to find question with id " + questionId);
        }

        if (enrich) {
            enrich(question);
        }

        return question;
    }

    private void enrich(Question question) {
        userService.enrich(question);

        List<Template> templates = templateRepository.findAllForQuestion(question.getId());
        question.setTemplates(TemplateSummary.createFrom(templates));
        question.setNbProjectsUsedIn(projectQuestionsService.countByQuestion(question.getId()));
        question.setNbTemplatesUsedIn(templateRepository.countByQuestion(question.getId()));

        if (featureStatus.isEnabled(Feature.AllowChangeInUseQuestion)) {
            question.setEditInUseQuestionsFeatureEnabled(true);
        }
    }

    public Question findById(Integer id) {
        return repository.findById(id).orElse(null);
    }

    public Question save(Question question) {
        validateQuestion(question);
        question.setMaxLength(getTextMaxLength(question.getMaxLength(), question.getAnswerType()));
        return repository.save(question);
    }

    public Question create(Question question) {
        if (question.getId() == null) {
            throw new ValidationException("question ID is mandatory!");
        }

        if (repository.existsById(question.getId())) {
            throw new ValidationException("question with ID " + question.getId() + " already exists!");
        }

        return save(question);
    }

    public void update(Integer questionId, Question question) {

        if (!Objects.equals(questionId, question.getId())) {
            throw new ValidationException("cannot update the question ID!");
        }

        boolean changesToInUseAllowed = featureStatus.isEnabled(Feature.AllowChangeInUseQuestion);
        boolean isUseInProject = projectQuestionsService.countByQuestion(questionId) > 0;
        boolean inUseInMultipleTemplates = templateRepository.countByQuestion(questionId) > 1;

        if (!changesToInUseAllowed && isUseInProject) {
            throw new ValidationException("This question cannot be changed as it is already in use for a project");
        }

        if (!changesToInUseAllowed && inUseInMultipleTemplates) {
            throw new ValidationException("This question cannot be changed is in use on more than one template");
        }

        if (changesToInUseAllowed && (isUseInProject || inUseInMultipleTemplates)) {
            Question previousQuestion = repository.getOne(questionId);

            auditService.auditCurrentUserActivity(
                    String.format("Question %d text has been updated from \"%s\" to \"%s\"",
                            questionId, previousQuestion.getText(), question.getText()));

        }

        question.setModifiedOn(OffsetDateTime.now());
        save(question);
    }

    public void deleteQuestion(Integer questionId) {
        Question question = getById(questionId);

        if (!templateRepository.findAllForQuestion(questionId).isEmpty()) {
            throw new ValidationException("cannot delete a question used in a template!");
        }

        repository.delete(question);

        auditService.auditCurrentUserActivity(String.format("deleted question with ID %d", questionId));
    }

    public void createDropDownQuestion(int id, String text, Integer maxAnswers, String answersDelimiter,
            AnswerOption... options) {
        Set<AnswerOption> optionsAsSet = new HashSet<>();
        if (options != null) {
            Collections.addAll(optionsAsSet, options);
        }
        createQuestion(id, text, AnswerType.Dropdown, optionsAsSet, null, null, null, null, null, maxAnswers, answersDelimiter);
    }

    public void createTextQuestion(int id, String text) {
        createQuestion(id, text, AnswerType.Text, null, null, null, null);
    }

    public void createFileUploadQuestion(int id, String text, Integer quantity, Integer maxFileSize) {
        createQuestion(id, text, AnswerType.FileUpload, null, null, quantity, null, maxFileSize, null, null, null);
    }

    public void createBasicQuestion(int id, String text, AnswerType answerType) {
        createQuestion(id, text, answerType, null, null, null, null);
    }

    public void createQuestion(Integer id, String text, AnswerType answerType, Set<AnswerOption> options, Integer maxLength,
            Integer quantity, String externalKey) {
        createQuestion(id, text, answerType, options, maxLength, quantity, externalKey, null, null, null, null);
    }

    public Question createQuestion(Integer id,
            String text,
            AnswerType answerType,
            Set<AnswerOption> options,
            Integer maxLength,
            Integer quantity,
            String externalKey,
            Integer sizeInMb,
            Integer combinedFileSizeInMb,
            Integer maxAnswers,
            String answersDelimiter) {
        validateQuestion(id, text, answerType, options, maxAnswers, answersDelimiter);
        quantity = (quantity == null) ? MAX_FILE_QUANTITY : quantity;
        maxLength = getTextMaxLength(maxLength, answerType);
        jdbcTemplate.update(
                "insert into QUESTION (id, text, answer_type, external_key, max_length, quantity, created_on, max_upload_size, "
                        + "max_combined_upload_size, max_answers) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                id, text, answerType.name(), externalKey, maxLength, quantity,
                new java.sql.Date(Date.from(environment.now().toInstant()).getTime()), sizeInMb, combinedFileSizeInMb,
                maxAnswers);
        // not sure why creator has to be added separately to insert above,
        // but otherwise get transient instance save exception (GLA-26241)
        Question created = findById(id);
        try {
            created.setCreatedBy(currentUsername());
        } catch (Exception e) {
            e.printStackTrace();
        }
        repository.save(created);

        if (options != null) {
            for (AnswerOption option : options) {
                jdbcTemplate.update(
                        "insert into QUESTION_ANSWER_OPTIONS (question_id, display_order, answer_options) VALUES (?,?,?)",
                                id, option.getDisplayOrder(), option.getOption());
            }
        }

        return getById(id);
    }

    private void validateQuestion(Question question) {
        validateQuestion(question.getId(), question.getText(), question.getAnswerType(), question.getAnswerOptions(),
                question.getMaxAnswers(), question.getDelimiter());
    }

    public void validateQuestion(Integer id, String text, AnswerType answerType, Set<AnswerOption> options, Integer maxAnswers,
            String delimiter) {
        if (id == null) {
            throw new ValidationException("Question ID cannot be null!");
        }

        if (StringUtils.isEmpty(text)) {
            throw new ValidationException("Question text cannot be blank!");
        }

        if (answerType == null) {
            throw new ValidationException("Answer type cannot be null!");
        }

        if (options != null && !options.isEmpty()) {
            if (options.size() < MIN_OPTIONS || options.size() > MAX_OPTIONS) {
                throw new ValidationException("questions options have to be between " + MIN_OPTIONS + " and " + MAX_OPTIONS);
            }

            for (AnswerOption option : options) {
                if (option.getOption().length() > OPTION_MAX_LENGTH) {
                    throw new ValidationException(
                            "question option cannot be more than " + OPTION_MAX_LENGTH + " characters long!");
                }

                if (maxAnswers != null && delimiter != null && maxAnswers > 1 && option.getOption().contains(delimiter)) {
                    throw new ValidationException("question option cannot contain '" + delimiter + "'");
                }
            }
        }
    }

    private Integer getTextMaxLength(Integer maxLength, AnswerType answerType) {
        if (maxLength == null && AnswerType.FreeText.equals(answerType)) {
            maxLength = TEXT_MAX_LENGTH;
        }
        return maxLength;
    }

    public void flush() {
        repository.flush();
    }

    public void updateRequirement(Integer id, Requirement requirement) {
        jdbcTemplate.update("update template_question set requirement = ? where question_id = ?", requirement.name(), id);
    }

    /**
     * @param questionId question id
     * @param option option text
     * @param defaultUnanswered is set to true, this will update all the null or empty answers for that question with the new
     * option if the project is not in draft status.
     */
    public void addOption(Integer questionId, AnswerOption option, boolean defaultUnanswered) {
        Question question = repository.getOne(questionId);
        question.getAnswerOptions().add(option);
        repository.save(question);

        if (defaultUnanswered) {
            List<Project> projects = projectQuestionsService.findAllForQuestionId(questionId);
            for (Project project : projects) {
                if (!ProjectStatus.Draft.equals(project.getStatusType())) {
                    for (ProjectQuestionsBlock questionsBlock : project.getQuestionsBlocks()) {
                        if (questionsBlock.getTemplateQuestionByQuestionId(questionId) != null) {
                            Answer answer = questionsBlock.getAnswerByQuestionId(questionId);
                            if (answer != null && StringUtils.isEmpty(answer.getAnswer())) {
                                answer.setAnswer(option.getOption());
                                projectService.updateProject(project);
                            } else if (answer == null) {
                                answer = new Answer();
                                answer.setQuestion(question);
                                answer.setAnswer(option.getOption());
                                questionsBlock.getAnswers().add(answer);
                                projectService.updateProject(project);
                            }
                        }
                    }
                }

            }
        }
    }

    public void deleteOption(Integer id, String option) {
        Question question = repository.getOne(id);
        question.getAnswerOptions().removeIf(o -> o.getOption().equals(option));
        repository.save(question);

        jdbcTemplate.update("delete from answer where question_id = ? and answer = ?", id, option);
    }

    public AttachmentFile uploadFile(Integer questionId, Integer orgId, Integer programmeId, Integer projectId, Integer blockId,
            MultipartFile file) throws IOException {
        Question question = getById(questionId);

        Integer blockDisplayOrderOrId = blockId;
        if (question.getMaxCombinedUploadSizeInMb() != null) {
            ProjectQuestionsBlock questionsBlock = projectQuestionsService.getQuestionsBlock(blockId);
            blockDisplayOrderOrId = questionsBlock.getDisplayOrder();
            Answer answer = questionsBlock.getAnswerByQuestionId(questionId);
            long totalAttachmentsSize = answer != null ? answer.getTotalAttachmentsSize() : 0;
            long allowedFileSize = (question.getMaxCombinedUploadSizeInMb() * ONE_MB) - totalAttachmentsSize;
            if (file.getSize() > allowedFileSize) {
                throw new ValidationException(
                        "Combined file size exceeds the combined file size limit (" + (question.getMaxCombinedUploadSizeInMb())
                                + "MB)");
            }
        } else {
            long allowedFileSize =
                    (question.getMaxUploadSizeInMb() != null ? question.getMaxUploadSizeInMb() : maxFileSize) * ONE_MB;
            if (file.getSize() > allowedFileSize) {
                throw new ValidationException("File size cannot exceed " + (allowedFileSize / ONE_MB) + "Mb");
            }
        }

        String directory = buildDirectoryNameToStoreFile(programmeId, projectId, blockDisplayOrderOrId, questionId);
        return fileService.upload(orgId, file.getOriginalFilename(), file.getContentType(), file.getSize(),
                file.getInputStream(), FileCategory.Attachment, directory);
    }

    private String buildDirectoryNameToStoreFile(Integer programmeId, Integer projectId, Integer blockDisplayOrderOrId,
            Integer questionId) {
        return String.format("%d/%d/%d/%d", programmeId, projectId, blockDisplayOrderOrId, questionId);
    }

}
