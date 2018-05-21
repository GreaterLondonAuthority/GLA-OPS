/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.service;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Service;
import uk.gov.london.ops.domain.project.Answer;
import uk.gov.london.ops.domain.project.Project;
import uk.gov.london.ops.domain.project.ProjectQuestionsBlock;
import uk.gov.london.ops.domain.template.AnswerOption;
import uk.gov.london.ops.domain.template.AnswerType;
import uk.gov.london.ops.domain.template.Question;
import uk.gov.london.ops.domain.template.Requirement;
import uk.gov.london.ops.exception.NotFoundException;
import uk.gov.london.ops.exception.ValidationException;
import uk.gov.london.ops.repository.ProjectRepository;
import uk.gov.london.ops.repository.QuestionRepository;
import uk.gov.london.ops.service.project.ProjectService;

import javax.annotation.PostConstruct;
import java.util.*;

/**
 * Service interface for managing the questions library.
 */
@Service
public class QuestionService {

    private static final int MIN_OPTIONS = 2;
    private static final int MAX_OPTIONS = 35;
    private static final int OPTION_MAX_LENGTH = 45;

    @Autowired
    ProjectService projectService;

    @Autowired
    QuestionRepository repository;

    @Autowired
    JdbcTemplate jdbcTemplate;

    private SimpleJdbcInsert jdbcInsertQuestion;


    @PostConstruct
    public void postInjection() {
        this.jdbcInsertQuestion = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("QUESTION")
                .usingGeneratedKeyColumns("id");

    }

    /**
     * Returns all questions in the library.
     */
    public List<Question> getAll() {
        return repository.findAll();
    }

    public Question getById(Integer id) {
        Question question = repository.findOne(id);
        if (question == null) {
            throw new NotFoundException("Unable to find question with id " + id);
        }
        return question;
    }

    public Question findById(Integer id) {
        return repository.findOne(id);
    }


    public Question getByExternalKey(final String externalKey) {
        final List<Question> questionsByExternalKey = repository.findByExternalKey(externalKey);
        return !CollectionUtils.isEmpty(questionsByExternalKey) ?
                questionsByExternalKey.get(0) : null;
    }



    public Question createDropDownQuestion(int id, String text, AnswerOption ... options) {
        Set<AnswerOption> optionsAsSet = new HashSet<>();
        if (options != null) Collections.addAll(optionsAsSet, options);
        return createQuestion(id, text, AnswerType.Dropdown, optionsAsSet, null, null, null);
    }

    public Question createFreeTextQuestion(int id, String text, Integer maxLength) {
        return createQuestion(id, text, AnswerType.FreeText, null, maxLength, null, null);
    }

    public Question createTextQuestion(int id, String text) {
        return createQuestion(id, text, AnswerType.Text, null, null, null, null);
    }

    public Question createFileUploadQuestion(int id, String text, Integer quantity) {
        return createQuestion(id, text, AnswerType.FileUpload, null, null, quantity, null);
    }

    public Question createNumericQuestion(int id, String text) {
        return createQuestion(id, text, AnswerType.Number, null, null, null, null);
    }

    public Question createDateQuestion(int id, String text) {
        return createQuestion(id, text, AnswerType.Date, null, null, null, null);
    }

    public Question createBasicQuestion(int id, String text, AnswerType answerType) {
        return createQuestion(id, text, answerType, null, null, null, null);
    }

    public Question createQuestion(Integer id, String text, AnswerType answerType, Set<AnswerOption> options, Integer maxLength, Integer quantity,  String externalKey) {


        if (options != null && !options.isEmpty()) {
            if (options.size() < MIN_OPTIONS || options.size() > MAX_OPTIONS) {
                throw new ValidationException("questions options have to be between "+MIN_OPTIONS+" and "+MAX_OPTIONS);
            }

            for (AnswerOption option : options) {
                if (option.getOption().length() > OPTION_MAX_LENGTH) {
                    throw new ValidationException("question option cannot be more than "+OPTION_MAX_LENGTH+" characters long!");
                }
            }
        }

        if (maxLength == null && AnswerType.FreeText.equals(answerType)) {
            maxLength = 2000;
        }


        if(id != null) {
            jdbcTemplate.update("insert into QUESTION (id,text,answer_type, external_key, max_length, quantity) VALUES (?,?,?,?, ? , ?)", id, text, answerType.name(), externalKey, maxLength, quantity);
        } else {
            id = insertNewQuestion(text, answerType.name(), externalKey);
        }


        if (options != null) {
            for (AnswerOption option : options) {
                jdbcTemplate.update("insert into QUESTION_ANSWER_OPTIONS (question_id, display_order, answer_options) VALUES (?,?,?)", id, option.getDisplayOrder(), option.getOption());
            }
        }

        return getById(id);
    }

    private int insertNewQuestion(final String text,  String answerType, String externalKey) {
        Map<String, Object> parameters = new HashedMap();
        parameters.put("text", text);
        parameters.put("answer_type", answerType);
        parameters.put("external_key", externalKey);
        Number key = jdbcInsertQuestion.executeAndReturnKey(new MapSqlParameterSource(parameters));
        return key.intValue();
    }

    public void flush() {
        repository.flush();
    }

    public void updateRequirement(Integer id, Requirement requirement) {
        jdbcTemplate.update("update template_question set requirement = ? where question_id = ?", requirement.name(), id);
    }

    /**
     * @param id question id
     * @param option option text
     * @param defaultUnanswered is set to true, this will update all the null or empty answers for that question with
     *                          the new option if the project is not in draft status.
     */
    public void addOption(Integer questionId, AnswerOption option, boolean defaultUnanswered) {
        Question question = repository.findOne(questionId);
        question.getAnswerOptions().add(option);
        repository.save(question);

        if (defaultUnanswered) {
            List<Project> projects = projectService.findAllForQuestionId(questionId);
            for (Project project: projects) {
                if (!Project.Status.Draft.equals(project.getStatus())) {
                    for (ProjectQuestionsBlock questionsBlock: project.getQuestionsBlocks()) {
                        if (questionsBlock.getTemplateQuestionByQuestionId(questionId) != null) {
                            Answer answer = questionsBlock.getAnswerByQuestionId(questionId);
                            if (answer != null && StringUtils.isEmpty(answer.getAnswer())) {
                                answer.setAnswer(option.getOption());
                                projectService.updateProject(project);
                            }
                            else if(answer == null) {
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
        Question question = repository.findOne(id);
        question.getAnswerOptions().removeIf(o -> o.getOption().equals(option));
        repository.save(question);

        jdbcTemplate.update("delete from answer where question_id = ? and answer = ?", id, option);

    }

}
