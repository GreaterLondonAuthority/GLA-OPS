/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.project.question;

import static uk.gov.london.ops.project.question.AnswerValidatorKt.validateAnswer;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import uk.gov.london.ops.file.AttachmentFile;
import uk.gov.london.ops.framework.exception.ValidationException;
import uk.gov.london.ops.project.template.domain.AnswerType;
import uk.gov.london.ops.project.template.domain.Question;
import uk.gov.london.ops.project.template.domain.QuestionsBlockSection;
import uk.gov.london.ops.project.template.domain.TemplateQuestion;

public interface QuestionsBlock {

    Set<Answer> getAnswers();

    void setAnswers(Set<Answer> answers);

    Set<ProjectQuestion> getQuestions();

    void setQuestions(Set<ProjectQuestion> questions);

    Set<QuestionsBlockSection> getSections();

    void setSections(Set<QuestionsBlockSection> sections);

    default void addTemplateQuestion(TemplateQuestion templateQuestion) {
        getQuestions().add(new ProjectQuestion(templateQuestion));
    }

    default ProjectQuestion getProjectQuestionByQuestionId(Integer questionId) {
        return getQuestions().stream()
                .filter(pq -> pq.getTemplateQuestion().getQuestion().getId().equals(questionId))
                .findFirst()
                .orElse(null);
    }

    default Question getQuestionByQuestionId(Integer questionId) {
        for (TemplateQuestion templateQuestion : getTemplateQuestions()) {
            if (templateQuestion.getQuestion().getId().equals(questionId)) {
                return templateQuestion.getQuestion();
            }
        }
        return null;
    }

    default Answer getAnswerByQuestionId(Integer questionId) {
        if (getAnswers() == null) {
            return null;
        }
        for (Answer answer : getAnswers()) {
            if (answer.getQuestionId().equals(questionId)) {
                return answer;
            }
        }
        return null;
    }

    @JsonIgnore
    default Set<TemplateQuestion> getTemplateQuestions() {
        return getQuestions().stream().map(ProjectQuestion::getTemplateQuestion).collect(Collectors.toSet());
    }

    @JsonProperty(value = "questions", access = JsonProperty.Access.READ_ONLY)
    default Set<ProjectQuestionModel> getEnrichedQuestions() {
        Set<ProjectQuestionModel> models = new HashSet<>();
        for (ProjectQuestion projectQuestion : getQuestions()) {
            if (!projectQuestion.isHidden()) {
                TemplateQuestion templateQuestion = projectQuestion.getTemplateQuestion();
                Question question = templateQuestion.getQuestion();
                ProjectQuestionModel model = new ProjectQuestionModel();
                model.setId(question.getId());
                model.setText(question.getText());
                model.setAnswerType(question.getAnswerType());
                model.setQuantity(question.getQuantity());
                model.setMaxUploadSizeInMb(question.getMaxUploadSizeInMb());
                model.setMaxCombinedUploadSizeInMb(question.getMaxCombinedUploadSizeInMb());
                model.setMaxLength(question.getMaxLength());
                model.setMaxAnswers(question.getMaxAnswers());
                model.setDelimiter(question.getDelimiter());
                model.setAnswerOptions(question.getAnswerOptions());
                model.setDisplayOrder(templateQuestion.getDisplayOrder());
                model.setRequirement(templateQuestion.getRequirement());
                model.setHelpText(templateQuestion.getHelpText());
                model.setParentId(templateQuestion.getParentId());
                model.setParentAnswerToMatch(templateQuestion.getParentAnswerToMatch());
                model.setSectionId(templateQuestion.getSectionId());
                model.setNewQuestion(projectQuestion.isNew());
                models.add(model);
            }
        }
        return models;
    }

    default void mergeAnswersFrom(QuestionsBlock other) {
        Set<Answer> updatedAnswers = other.getAnswers();

        Set<Answer> to = getAnswers();
        if (updatedAnswers == null || updatedAnswers.isEmpty()) {
            return;
        }

        for (Answer fromAnswer : updatedAnswers) {

            Answer toAnswer = getAnswerByQuestionId(fromAnswer.getQuestionId());

            Question question = getQuestionByQuestionId(fromAnswer.getQuestionId());

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
    }

    default void copyDataFrom(QuestionsBlock target) {
        if (this.getAnswers() != null) {
            target.setAnswers(this.getAnswers().stream()
                    .map(Answer::copy)
                    .collect(Collectors.toSet()));
        }

        if (this.getQuestions() != null) {
            target.setQuestions(this.getQuestions().stream()
                    .map(ProjectQuestion::copy)
                    .collect(Collectors.toSet()));
        } else {
            target.setQuestions(null);
        }

        if (this.getSections() != null) {
            target.setSections(this.getSections().stream()
                    .map(QuestionsBlockSection::copy)
                    .collect(Collectors.toSet()));
        }
    }

}
