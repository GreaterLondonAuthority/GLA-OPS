/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.domain.project.question;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import uk.gov.london.ops.domain.Requirement;
import uk.gov.london.ops.domain.project.*;
import uk.gov.london.ops.domain.template.*;
import uk.gov.london.ops.file.AttachmentFile;
import uk.gov.london.ops.framework.exception.ValidationException;
import uk.gov.london.ops.framework.jpa.Join;
import uk.gov.london.ops.framework.jpa.JoinData;
import uk.gov.london.ops.project.implementation.spe.SimpleProjectExportConfig;
import uk.gov.london.ops.service.project.state.StateTransition;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static uk.gov.london.ops.project.implementation.spe.SimpleProjectExportConstants.ReportPrefix;

/**
 * The Additional Questions block in a Project.
 *
 * @author Steve Leach
 */
@Entity(name = "questions_block")
@DiscriminatorValue("QUESTIONS")
@JoinData(sourceTable = "questions_block", sourceColumn = "id", targetTable = "project_block", targetColumn = "id", joinType = Join.JoinType.OneToOne,
        comment = "the questions block is a subclass of the project block and shares a common key")
public class ProjectQuestionsBlock extends NamedProjectBlock {

    public static final int SHORT_TEXT_LENGTH = 80;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true, targetEntity = Answer.class)
    @JoinColumn(name = "questions_block", nullable = false)
    private Set<Answer> answers = new HashSet<>();

    @JsonIgnore
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, targetEntity = ProjectQuestion.class)
    @JoinColumn(name = "project_block_id")
    private Set<ProjectQuestion> questions = new HashSet<>();

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, targetEntity = QuestionsBlockSection.class)
    @JoinColumn(name = "project_block_id")
    private Set<QuestionsBlockSection> sections = new HashSet<>();

    public ProjectQuestionsBlock() {

    }

    public ProjectQuestionsBlock(Project project) {
        super(project);
    }

    @Override
    public ProjectBlockType getBlockType() {
        return ProjectBlockType.Questions;
    }

    public Set<Answer> getAnswers() {
        return answers;
    }

    public boolean hasAnswers() {
        return (getAnswers() != null && !getAnswers().isEmpty());
    }

    boolean isQuestionAnswered(Integer questionId) {
        Answer answer = getAnswerByQuestionId(questionId);
        return answer != null && !StringUtils.isEmpty(answer.getAnswerAsText());
    }

    @Override
    public boolean isComplete() {

        if (!isVisited()) {
            return false;
        }

        if (CollectionUtils.isEmpty(this.questions)) {
            // No questions, so can't be any mandatory ones that haven't been answered.
            return true;
        }

        Map<Integer, Answer> answerMap = questionToAnswerMap();

        for (ProjectQuestion projectQuestion : this.questions) {
            TemplateQuestion templateQuestion = projectQuestion.getTemplateQuestion();
            Question question = templateQuestion.getQuestion();
            if (!projectQuestion.isHidden() && Requirement.isRequired(templateQuestion.getRequirement())) {
                Answer answer = answerMap.get(templateQuestion.getQuestion().getId());

                if (templateQuestion.getParentId() != null) {
                    Answer parentAnswer = answerMap.get(templateQuestion.getParentId());
                    if (parentAnswer == null) {
                        return false; // if parent is unanswered and mandatory then block is incomplete
                    }
                    String parentAnswerToMatch = templateQuestion.getParentAnswerToMatch();
                    if (parentAnswerToMatch == null) {
                        return false; // shouldn't be possible
                    }
                    if (!parentAnswerToMatch.equals(parentAnswer.getAnswerAsText())) {
                        continue;
                    }
                }

                if (answer == null)
                    return false;

                if (AnswerType.Number.equals(question.getAnswerType()) && answer.getNumericAnswer() == null)
                    return false;

                if (AnswerType.FileUpload.equals(question.getAnswerType()) && answer.getFileAttachments().isEmpty())
                    return false;

                if (!AnswerType.Number.equals(question.getAnswerType()) &&
                        !AnswerType.FileUpload.equals(question.getAnswerType()) &&
                        StringUtils.isEmpty(answer.getAnswer()))
                    return false;
            }
        }

        return true;
    }

    @Override
    protected void generateValidationFailures() {
        // do nothing for now
    }

    /**
     * Merges new/updated answers into the project
     */
    public void merge(NamedProjectBlock block) {
        if (!this.getClass().isAssignableFrom(block.getClass())) {
            throw new ValidationException("Unable to merge due to incorrect block type");
        }

        ProjectQuestionsBlock questionsBlock = (ProjectQuestionsBlock) block;
        Set<Answer> updatedAnswers = questionsBlock.getAnswers();

        Set<TemplateQuestion> questions = this.getTemplateQuestions();
        Set<Answer> to = getAnswers();
        if (updatedAnswers == null || updatedAnswers.isEmpty()) {
            return;
        }

        for (Answer fromAnswer : updatedAnswers) {

            Answer toAnswer = getAnswerByQuestionId(to, fromAnswer.getQuestionId());

            Question question = getQuestionByQuestionId(questions, fromAnswer.getQuestionId());

            if (toAnswer == null) {
                toAnswer = new Answer();
                if (question != null) {
                    toAnswer.setQuestion(question);
                } else {
                    throw new ValidationException("answer", "Unable to find question matching: " + fromAnswer.getQuestionId());
                }
                to.add(toAnswer);
            }

            if (!AnswerType.FileUpload.equals(question.getAnswerType())) {

                if (AnswerType.Number.equals(toAnswer.getQuestion().getAnswerType())) {
                    toAnswer.setNumericAnswer(fromAnswer.getNumericAnswer());
                } else {
                    toAnswer.setAnswer(fromAnswer.getAnswer());
                }
                validateAnswer(toAnswer);
            } else {

                if (!fromAnswer.getAttachmentIds().isEmpty()) {
                    for (Integer fileId : fromAnswer.getAttachmentIds()) {
                        AttachmentFile file = new AttachmentFile();
                        file.setId(fileId);
                        toAnswer.getFileAttachments().add(file);
                    }
                }

                // remove any pre-existing attachments that aren't in the new list of IDs
                toAnswer.getFileAttachments().removeIf(fileEntry -> !fromAnswer.getAttachmentIds().contains(fileEntry.getId()));
            }

        }


        clearDependantAnswers();

    }

    public void clearDependantAnswers() {
        // remove any incorrectly answered dependant questions
        for (Answer answer : this.answers) {
            if (hasParent(answer) && !isParentConditionMet(answer)) {
                answer.clear();
            }
        }
    }

    private boolean hasParent(Answer answer) {
        return getTemplateQuestionByQuestionId(answer.getQuestionId()).hasParent();
    }

    private boolean isParentConditionMet(Answer answer) {
        TemplateQuestion templateQuestion = getTemplateQuestionByQuestionId(answer.getQuestionId());
        Integer parentQuestionId = templateQuestion.getParentId();
        TemplateQuestion parentTemplateQuestion = getTemplateQuestionByQuestionId(parentQuestionId);
        Answer parentAnswer = getAnswerByQuestionId(parentQuestionId);

        return (!parentTemplateQuestion.hasParent() || isParentConditionMet(parentAnswer))
                && Objects.equals(templateQuestion.getParentAnswerToMatch(), parentAnswer.getAnswerAsText());
    }

    /**
     * Returns a map of question IDs to answers.
     */
    private Map<Integer, Answer> questionToAnswerMap() {
        Map<Integer, Answer> answerMap;
        if (hasAnswers()) {
            answerMap = getAnswers().stream().collect(Collectors.toMap(Answer::getQuestionId, Function.identity()));
        } else {
            answerMap = Collections.EMPTY_MAP;
        }
        return answerMap;
    }

    public void validateAnswer(Answer answer) {
        // answers can be optional
        if (answer.getAnswer() == null || answer.getAnswer().length() == 0) {
            return;
        }
        switch (answer.getQuestion().getAnswerType()) {
            case Date:
                validateDate(answer.getAnswer());
                break;
            case YesNo:
                validateBoolean(answer.getAnswer());
                break;
            case Text:
                validateText(answer.getAnswer(), SHORT_TEXT_LENGTH);
                break;
            case FreeText:
                validateText(answer.getAnswer(), answer.getQuestion().getMaxLength());
                break;
        }
    }

    static void validateText(String answer, int maxLenth) {
        if (answer.length() > maxLenth) {
            throw new ValidationException("answer", String.format("The maximum length for the answer field is %d characters.", maxLenth));
        }

        // TODO: check that short text answers don't contain newlines
    }

    static void validateBoolean(String answer) {
        if (!(answer.equalsIgnoreCase("yes") || answer.equalsIgnoreCase("no"))) {
            throw new ValidationException("answer", "Acceptable values for answer are: Yes/No only");
        }
    }

    static void validateDate(String answer) {
        try {
            LocalDate.parse(answer);
        } catch (DateTimeParseException e) {
            throw new ValidationException("answer", "Unable to format the given value as a date.");
        }
    }

    public Answer getAnswerByQuestionId(int questionId) {
        return getAnswerByQuestionId(answers, questionId);
    }

    public static Answer getAnswerByQuestionId(Set<Answer> answers, int questionId) {
        if (answers == null) {
            return null;
        }
        for (Answer answer : answers) {
            if (answer.getQuestionId().equals(questionId)) {
                return answer;
            }
        }
        return null;
    }

    public static Question getQuestionByQuestionId(Set<TemplateQuestion> templateQuestions, int questionId) {
        for (TemplateQuestion templateQuestion : templateQuestions) {
            if (templateQuestion.getQuestion().getId().equals(questionId)) {
                return templateQuestion.getQuestion();
            }
        }
        return null;
    }

    public void setAnswers(Set<Answer> answers) {
        this.answers = answers;
    }

    public Set<ProjectQuestion> getQuestions() {
        return questions;
    }

    public void setQuestions(Set<ProjectQuestion> questions) {
        this.questions = questions;
    }

    public ProjectQuestion getProjectQuestionByQuestionId(Integer questionId) {
        return getQuestions().stream().filter(pq -> pq.getTemplateQuestion().getQuestion().getId().equals(questionId)).findFirst().orElse(null);
    }

    @JsonIgnore
    public Set<TemplateQuestion> getTemplateQuestions() {
        return questions.stream().map(ProjectQuestion::getTemplateQuestion).collect(Collectors.toSet());
    }

    @JsonProperty(value = "questions", access = JsonProperty.Access.READ_ONLY)
    public Set<ProjectQuestionModel> getEnrichedQuestions() {
        Set<ProjectQuestionModel> models = new HashSet<>();
        for (ProjectQuestion projectQuestion: questions) {
            if (!projectQuestion.isHidden()) {
                TemplateQuestion templateQuestion = projectQuestion.getTemplateQuestion();
                Question question = templateQuestion.getQuestion();
                ProjectQuestionModel model = new ProjectQuestionModel();
                model.setId(question.getId());
                model.setText(question.getText());
                model.setAnswerType(question.getAnswerType());
                model.setQuantity(question.getQuantity());
                model.setMaxUploadSizeInMb(question.getMaxUploadSizeInMb());
                model.setMaxLength(question.getMaxLength());
                model.setAnswerOptions(question.getAnswerOptions());
                model.setDisplayOrder(templateQuestion.getDisplayOrder());
                model.setRequirement(templateQuestion.getRequirement());
                model.setParentId(templateQuestion.getParentId());
                model.setParentAnswerToMatch(templateQuestion.getParentAnswerToMatch());
                model.setSectionId(templateQuestion.getSectionId());
                model.setNewQuestion(projectQuestion.isNew());
                models.add(model);
            }
        }
        return models;
    }

    public Set<QuestionsBlockSection> getSections() {
        return sections;
    }

    public void setSections(Set<QuestionsBlockSection> sections) {
        this.sections = sections;
    }

    public TemplateQuestion getTemplateQuestionByQuestionId(Integer questionId) {
        return getTemplateQuestions().stream().filter(tq -> tq.getQuestion().getId().equals(questionId)).findFirst().orElse(null);
    }

    @Override
    public Map<String, Object> simpleDataExtract(SimpleProjectExportConfig config) {
        Map<String, Object> map = new HashMap<>();
        for (Answer answer : answers) {
            map.put(buildDataExtractKey(answer.getQuestion()), answer.getAnswer() != null ? answer.getAnswer() : answer.getNumericAnswer());
        }
        return map;
    }

    private String buildDataExtractKey(Question question) {
        return ReportPrefix.q_ + (StringUtils.isEmpty(question.getExternalKey()) ? ("q" + question.getId()) : question.getExternalKey());
    }

    @Override
    protected void initFromTemplateSpecific(TemplateBlock templateBlock) {
        QuestionsTemplateBlock qtb = (QuestionsTemplateBlock) templateBlock;
        for (TemplateQuestion tq: qtb.getQuestions()) {
            addTemplateQuestion(tq);
        }
        getSections().addAll(qtb.getSections().stream().map(QuestionsBlockSection::copy).collect(Collectors.toSet()));
    }

    public void addTemplateQuestion(TemplateQuestion templateQuestion) {
        this.questions.add(new ProjectQuestion(templateQuestion));
    }

    protected void copyBlockContentInto(NamedProjectBlock t) {
        final ProjectQuestionsBlock target = (ProjectQuestionsBlock) t;
        if (this.getAnswers() != null) {
            target.setAnswers(this.getAnswers().stream()
                    .map(Answer::copy)
                    .collect(Collectors.toSet()));
        }

        if (this.questions != null) {
            target.setQuestions(this.getQuestions().stream()
                    .map(ProjectQuestion::copy)
                    .collect(Collectors.toSet()));
        } else {
            target.setQuestions(null);
        }


        if (this.sections != null) {
            target.setSections(this.getSections().stream()
                    .map(QuestionsBlockSection::copy)
                    .collect(Collectors.toSet()));
        }

    }

    @Override
    protected void compareBlockSpecificContent(NamedProjectBlock other, ProjectDifferences differences) {
        ProjectQuestionsBlock otherQuestionsBlock = (ProjectQuestionsBlock) other;

        Map<Integer, Answer> thisAnswers = this.questionToAnswerMap();
        Map<Integer, Answer> otherAnswers = otherQuestionsBlock.questionToAnswerMap();

        for (Integer questionId: thisAnswers.keySet()) {

            // additions
            ProjectQuestion thisQuestion = this.getProjectQuestionByQuestionId(questionId);
            ProjectQuestion otherQuestion = otherQuestionsBlock.getProjectQuestionByQuestionId(questionId);
            if (!otherQuestionsBlock.isQuestionAnswered(questionId) || (otherQuestion != null && otherQuestion.isHidden() && thisQuestion != null && !thisQuestion.isHidden())) {
                differences.add(new ProjectDifference(thisAnswers.get(questionId), ProjectDifference.DifferenceType.Addition));
            }

            // changes
            else {
                Answer thisAnswer = thisAnswers.get(questionId);
                Answer otherAnswer = otherAnswers.get(questionId);

                if (!Objects.equals(StringUtils.trimAllWhitespace(thisAnswer.getAnswerAsText()), StringUtils.trimAllWhitespace(otherAnswer.getAnswerAsText()))) {
                    differences.add(new ProjectDifference(thisAnswer, ProjectDifference.DifferenceType.Change));
                }
            }
        }

        // deletions
        for (Integer questionId: otherAnswers.keySet()) {
            if (!this.isQuestionAnswered(questionId)) {
                differences.add(new ProjectDifference(otherAnswers.get(questionId), ProjectDifference.DifferenceType.Deletion));
            }
        }
    }

    @Override
    public boolean isBlockRevertable() {
        return true;
    }

    @Override
    public void handleStateTransitionSpecific(StateTransition stateTransition) {
        for (ProjectQuestion question: questions) {
            if (question.isHidden() && question.getTemplateQuestion().appearsOnState(stateTransition.getTo())) {
                question.setHidden(false);
                question.setNew(true);
            }
            else if (stateTransition.isClearNewLabel() && this.isComplete()) {
                question.setNew(false);
            }
        }
    }

    @Override
    public boolean hasUpdates() {
        for (ProjectQuestion projectQuestion: this.questions) {
            if (projectQuestion.isNew()) {
                Integer parentId = projectQuestion.getTemplateQuestion().getParentId();
                if (parentId != null) {
                    Answer answer = getAnswerByQuestionId(parentId);
                    if (answer != null && projectQuestion.getTemplateQuestion().getParentAnswerToMatch().equalsIgnoreCase(answer.getAnswerAsText())) {
                        return true;
                    }
                } else {
                    return true;
                }
            }
        }
        return false;
    }

}


