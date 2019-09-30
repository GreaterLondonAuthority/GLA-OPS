/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.domain.project.question;

import com.fasterxml.jackson.annotation.JsonIgnore;
import uk.gov.london.ops.domain.project.ComparableItem;
import uk.gov.london.ops.domain.template.AnswerType;
import uk.gov.london.ops.domain.template.Question;
import uk.gov.london.ops.file.AttachmentFile;

import javax.persistence.*;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Represents the answer to a template question
 * Created by chris on 28/09/2016.
 */
@Entity
public class Answer implements ComparableItem {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "answer_seq_gen")
    @SequenceGenerator(name = "answer_seq_gen", sequenceName = "answer_seq", initialValue = 1000, allocationSize = 1)
    private Integer id;

    @JsonIgnore
    @OneToOne(cascade = {})
    @JoinColumn(name="question_id", referencedColumnName="id")
    private Question question;

    @Column(name = "answer")
    private String answer;

    @Column(name = "numeric_answer")
    private Double numericAnswer;

    @OneToMany( fetch = FetchType.EAGER)
    @JoinTable(name = "answer_attachment",
            joinColumns = @JoinColumn(name = "answer_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "attachment_id", referencedColumnName = "id"))
    Set<AttachmentFile> fileAttachments = new HashSet<>();

    @Transient
    private Set<Integer> attachmentIds = new HashSet<>();

    @Transient
    private Integer questionId;

    public Answer() {
        // Empty
    }

    public Answer(Question question) {
        setQuestion(question);
    }

    public Answer(Integer id, Question question, String text, Double numeric) {
        this(question);
        setId(id);
        setAnswer(text);
        setNumericAnswer(numeric);
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return this.id;
    }

    public Question getQuestion() {
        return question;
    }

    public void setQuestion(Question question) {
        this.question = question;
    }

    public String getAnswer() {
        // TODO Update this
        if (this.getQuestion() != null && AnswerType.FileUpload.equals(this.getQuestion().getAnswerType()) && !fileAttachments.isEmpty()) {
            StringBuffer buffer = new StringBuffer();
            for (AttachmentFile fileAttachment : fileAttachments) {
                buffer.append(fileAttachment.getFileName() + ", ");
            }
            return buffer.toString();
        }
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public Double getNumericAnswer() {
        return numericAnswer;
    }

    public void setNumericAnswer(Double numericAnswer) {
        this.numericAnswer = numericAnswer;
    }

    public Set<AttachmentFile> getFileAttachments() {
        return fileAttachments;
    }

    public void setFileAttachments(Set<AttachmentFile> fileAttachments) {
        this.fileAttachments = fileAttachments;
    }

    public Integer getQuestionId() {
        // if we have real ID return it.
        if (question != null) {
            return question.getId();
        }
        // otherwise use the JSON id
        return questionId;
    }

    public void setQuestionId(Integer questionId) {
        this.questionId = questionId;
    }

    public String getAnswerAsText() {
        String response;
        switch (this.getQuestion().getAnswerType()) {
            case Number:
                response = numericAnswer == null ? null : numericAnswer.toString();
                break;
            case FileUpload:
                response = Arrays.toString(this.getAttachmentIds().stream().sorted().toArray());
                break;
            default:
                response = answer;
                break;
        }

        return response;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Answer answer1 = (Answer) o;

        if (id != null ? !id.equals(answer1.id) : answer1.id != null) return false;
        if (question != null ? !question.equals(answer1.question) : answer1.question != null) return false;
        if (answer != null ? !answer.equals(answer1.answer) : answer1.answer != null) return false;
        if (numericAnswer != null ? !numericAnswer.equals(answer1.numericAnswer) : answer1.numericAnswer != null)
            return false;
        return !(questionId != null ? !questionId.equals(answer1.questionId) : answer1.questionId != null);

    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (question != null ? question.hashCode() : 0);
        result = 31 * result + (answer != null ? answer.hashCode() : 0);
        result = 31 * result + (numericAnswer != null ? numericAnswer.hashCode() : 0);
        result = 31 * result + (questionId != null ? questionId.hashCode() : 0);
        return result;
    }

    public Set<Integer> getAttachmentIds() {
        if (attachmentIds.isEmpty()) {
            for (AttachmentFile fileAttachment : fileAttachments) {
                attachmentIds.add(fileAttachment.getId());
            }
        }
        return attachmentIds;
    }

    public void setAttachmentIds(Set<Integer> attachmentIds) {
        this.attachmentIds = attachmentIds;
    }

    public Answer copy() {
        final Answer copy = new Answer();
//        copy.setId(this.getId());
        copy.setQuestionId(this.getQuestionId());
        if(this.getQuestion() != null) {
            copy.setQuestion(this.getQuestion().copy());
        } else {
            this.setQuestion(null);
        }
        copy.setNumericAnswer(this.getNumericAnswer());
        copy.fileAttachments.addAll(this.fileAttachments);
        copy.setAnswer(this.getAnswer());
        return copy;
    }

    @Override
    public String getComparisonId() {
        return String.valueOf(this.getQuestion().getId());
    }

    public void clear() {
        this.getFileAttachments().clear();
        this.setNumericAnswer(null);
        this.setAnswer(null);
    }

}
