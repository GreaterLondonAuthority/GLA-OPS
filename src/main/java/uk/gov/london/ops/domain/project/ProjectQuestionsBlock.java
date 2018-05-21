/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.domain.project;

import org.springframework.util.StringUtils;
import uk.gov.london.ops.domain.attachment.AttachmentFile;
import uk.gov.london.ops.domain.template.*;
import uk.gov.london.ops.exception.ValidationException;
import uk.gov.london.ops.spe.SimpleProjectExportConfig;
import uk.gov.london.ops.util.jpajoins.Join;
import uk.gov.london.ops.util.jpajoins.JoinData;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static uk.gov.london.ops.spe.SimpleProjectExportConstants.ReportPrefix;

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

    @OneToMany(fetch = FetchType.EAGER, cascade = {}, targetEntity = TemplateQuestion.class)
    @JoinTable(
            name = "PROJECT_BLOCK_QUESTION",
            joinColumns = @JoinColumn(name = "project_block_id"),
            inverseJoinColumns = @JoinColumn(name = "question_id")
    )
    private Set<TemplateQuestion> questionEntities = new HashSet<>();

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
        if (answers == null) {
            return Collections.emptySet();
        }
        return answers;
    }

    public boolean hasAnswers() {
        return (getAnswers() != null && !getAnswers().isEmpty());
    }

    @Override
    public boolean isComplete() {

        if (!isVisited()) {
            return false;
        }

        Set<TemplateQuestion> templateQuestions = this.getQuestionEntities();
        if (templateQuestions == null || templateQuestions.isEmpty()) {
            // No questions, so can't be any mandatory ones that haven't been answered.
            return true;
        }

        Map<Integer, Answer> answerMap = questionToAnswerMap();

        for (TemplateQuestion templateQuestion : templateQuestions) {
            Question question = templateQuestion.getQuestion();
            if (Requirement.isRequired(templateQuestion.getRequirement())) {
                Answer answer = answerMap.get(templateQuestion.getQuestion().getId());

                if (templateQuestion.getParentId() != null) {
                    Answer parentAnswer = answerMap.get(templateQuestion.getParentId());
                    if (!templateQuestion.getParentAnswerToMatch().equals(parentAnswer.getAnswerAsText())) {
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

        Set<TemplateQuestion> questions = this.getQuestionEntities();
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
        Template template = getProject().getTemplate();
        QuestionsTemplateBlock qtb = (QuestionsTemplateBlock) template.getSingleBlockByTypeAndDisplayOrder(ProjectBlockType.Questions, this.getDisplayOrder());
        // remove any incorrectly answered dependant questions
        for (Answer answer : this.answers) {
            TemplateQuestion templateQuestion = qtb.getQuestionById(answer.getQuestionId());
            Integer parentId = templateQuestion.getParentId();
            if (parentId != null) {
                Answer parentQuestion = getAnswerByQuestionId(parentId);
                if (templateQuestion.getParentAnswerToMatch() != null && !templateQuestion.getParentAnswerToMatch().equals(parentQuestion.getAnswerAsText())) {
                    answer.getFileAttachments().clear();
                    answer.setNumericAnswer(null);
                    answer.setAnswer(null);
                }
            }
        }
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

    public Set<TemplateQuestion> getQuestionEntities() {
        return questionEntities;
    }

    public void setQuestionEntities(Set<TemplateQuestion> questionEntities) {
        this.questionEntities = questionEntities;
    }

    public Set<QuestionsBlockSection> getSections() {
        return sections;
    }

    public void setSections(Set<QuestionsBlockSection> sections) {
        this.sections = sections;
    }

    public TemplateQuestion getTemplateQuestionByQuestionId(Integer questionId) {
        return questionEntities.stream().filter(tq -> tq.getQuestion().getId().equals(questionId)).findFirst().orElse(null);
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
        getQuestionEntities().addAll(qtb.getQuestions());
        getSections().addAll(qtb.getSections().stream().map(QuestionsBlockSection::copy).collect(Collectors.toSet()));
    }

    protected void copyBlockContentInto(NamedProjectBlock t) {
        final ProjectQuestionsBlock target = (ProjectQuestionsBlock) t;
        if (this.getAnswers() != null) {
            target.setAnswers(this.getAnswers().stream()
                    .map(Answer::copy)
                    .collect(Collectors.toSet()));
        } else {
            target.setAnswers(null);
        }

        if (this.questionEntities != null) {
            target.setQuestionEntities(this.getQuestionEntities().stream()
                    .map(TemplateQuestion::copy)
                    .collect(Collectors.toSet()));
        } else {
            target.setQuestionEntities(null);
        }

        if (this.sections != null) {
            target.setSections(this.getSections().stream()
                    .map(QuestionsBlockSection::copy)
                    .collect(Collectors.toSet()));
        }

    }

    @Override
    public boolean allowMultipleVersions() {
        return true;
    }

    @Override
    protected void compareBlockSpecificContent(NamedProjectBlock other, ProjectDifferences differences) {
        ProjectQuestionsBlock otherProjectQuestionsBlock = (ProjectQuestionsBlock) other;

        Set<Answer> otherAnswersCopy = new HashSet<>();
        otherAnswersCopy.addAll(otherProjectQuestionsBlock.getAnswers());



        for (Answer answer : answers) {
            Answer otherAnswer = otherProjectQuestionsBlock.getAnswerByQuestionId(answer.getQuestionId());
            otherAnswersCopy.remove(otherAnswer);
            if (otherAnswer != null) {
                if (!Objects.equals(StringUtils.trimAllWhitespace(answer.getAnswerAsText()), StringUtils.trimAllWhitespace(otherAnswer.getAnswerAsText()))) {
                    differences.add(new ProjectDifference(answer));
                }
            } else {
                differences.add(new ProjectDifference(answer));
            }
        }

        for (Answer answer : otherAnswersCopy) {
            differences.add(new ProjectDifference(answer, ProjectDifference.DifferenceType.Deletion));

        }



    }
}


