/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.project.question;

import java.util.Set;
import uk.gov.london.ops.domain.Requirement;
import uk.gov.london.ops.project.template.domain.AnswerOption;
import uk.gov.london.ops.project.template.domain.AnswerType;

public class ProjectQuestionModel {

    private Integer id;
    private String text;
    private AnswerType answerType;
    private Integer maxUploadSizeInMb;
    private Integer maxCombinedUploadSizeInMb;
    private Integer quantity;
    private Integer maxLength;
    private Integer maxAnswers;
    private String delimiter;
    private Set<AnswerOption> answerOptions;
    private Double displayOrder;
    private Requirement requirement;
    private String helpText;
    private Integer parentId;
    private String parentAnswerToMatch;
    private Integer sectionId;
    private boolean newQuestion;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public AnswerType getAnswerType() {
        return answerType;
    }

    public void setAnswerType(AnswerType answerType) {
        this.answerType = answerType;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Integer getMaxLength() {
        return maxLength;
    }

    public void setMaxLength(Integer maxLength) {
        this.maxLength = maxLength;
    }

    public Integer getMaxAnswers() {
        return maxAnswers;
    }

    public void setMaxAnswers(Integer maxAnswers) {
        this.maxAnswers = maxAnswers;
    }

    public String getDelimiter() {
        return delimiter;
    }

    public void setDelimiter(String delimiter) {
        this.delimiter = delimiter;
    }

    public Set<AnswerOption> getAnswerOptions() {
        return answerOptions;
    }

    public void setAnswerOptions(Set<AnswerOption> answerOptions) {
        this.answerOptions = answerOptions;
    }

    public Double getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(Double displayOrder) {
        this.displayOrder = displayOrder;
    }

    public Requirement getRequirement() {
        return requirement;
    }

    public void setRequirement(Requirement requirement) {
        this.requirement = requirement;
    }

    public String getHelpText() {
        return helpText;
    }

    public void setHelpText(String helpText) {
        this.helpText = helpText;
    }

    public Integer getParentId() {
        return parentId;
    }

    public void setParentId(Integer parentId) {
        this.parentId = parentId;
    }

    public String getParentAnswerToMatch() {
        return parentAnswerToMatch;
    }

    public void setParentAnswerToMatch(String parentAnswerToMatch) {
        this.parentAnswerToMatch = parentAnswerToMatch;
    }

    public Integer getSectionId() {
        return sectionId;
    }

    public void setSectionId(Integer sectionId) {
        this.sectionId = sectionId;
    }

    public boolean isNewQuestion() {
        return newQuestion;
    }

    public void setNewQuestion(boolean newQuestion) {
        this.newQuestion = newQuestion;
    }

    public Integer getMaxUploadSizeInMb() {
        return maxUploadSizeInMb;
    }

    public void setMaxUploadSizeInMb(Integer maxUploadSizeInMb) {
        this.maxUploadSizeInMb = maxUploadSizeInMb;
    }

    public Integer getMaxCombinedUploadSizeInMb() {
        return maxCombinedUploadSizeInMb;
    }

    public void setMaxCombinedUploadSizeInMb(Integer maxCombinedUploadSizeInMb) {
        this.maxCombinedUploadSizeInMb = maxCombinedUploadSizeInMb;
    }

}
