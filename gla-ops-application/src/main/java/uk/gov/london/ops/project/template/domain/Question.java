/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.project.template.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.london.ops.framework.OpsEntity;

import javax.persistence.*;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static uk.gov.london.ops.project.template.domain.AnswerType.Dropdown;

@Entity
@JsonIgnoreProperties(ignoreUnknown = true, value = {"handler", "hibernateLazyInitializer"})
public class Question implements OpsEntity<Integer> {

    @Id
    private Integer id;

    @Column(name = "external_key")
    private String externalKey;

    @Column(name = "text")
    private String text;

    @Column(name = "answer_type")
    @Enumerated(EnumType.STRING)
    private AnswerType answerType;

    @Column(name = "quantity")
    private Integer quantity;

    @Column(name = "max_upload_size")
    private Integer maxUploadSizeInMb;

    @Column(name = "max_combined_upload_size")
    private Integer maxCombinedUploadSizeInMb;

    @Column(name = "max_length")
    private Integer maxLength;

    @Column(name = "max_answers")
    private Integer maxAnswers = 1;

    @Column(name = "created_by")
    private String createdBy;

    @Transient
    private String creatorName;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Column(name = "created_on", updatable = false)
    private OffsetDateTime createdOn;

    @Column(name = "modified_by")
    private String modifiedBy;

    @Transient
    private String modifierName;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Column(name = "modified_on")
    private OffsetDateTime modifiedOn;

    @OneToMany(cascade = {CascadeType.ALL}, targetEntity = AnswerOption.class)
    @JoinColumn(name = "question_id")
    private Set<AnswerOption> answerOptions;

    @Transient
    private List<TemplateSummary> templates;

    @Transient
    private int nbProjectsUsedIn;

    @Transient
    private int nbTemplatesUsedIn;

    @Transient
    @JsonIgnore
    private boolean editInUseQuestionsFeatureEnabled;

    @Transient
    private String delimiter = ";";

    public Question() {
    }

    public Question(Integer id, String text, AnswerType answerType) {
        this.id = id;
        this.text = text;
        this.answerType = answerType;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getExternalKey() {
        return externalKey;
    }

    public void setExternalKey(String externalKey) {
        this.externalKey = externalKey;
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

    @Override
    public String getCreatedBy() {
        return createdBy;
    }

    @Override
    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    @Override
    public String getCreatorName() {
        return creatorName;
    }

    @Override
    public void setCreatorName(String creatorName) {
        this.creatorName = creatorName;
    }

    @Override
    public OffsetDateTime getCreatedOn() {
        return createdOn;
    }

    @Override
    public void setCreatedOn(OffsetDateTime createdOn) {
        this.createdOn = createdOn;
    }

    @Override
    public String getModifiedBy() {
        return modifiedBy;
    }

    @Override
    public void setModifiedBy(String modifiedBy) {
        this.modifiedBy = modifiedBy;
    }

    @Override
    public String getModifierName() {
        return modifierName;
    }

    @Override
    public void setModifierName(String modifierName) {
        this.modifierName = modifierName;
    }

    @Override
    public OffsetDateTime getModifiedOn() {
        return modifiedOn;
    }

    @Override
    public void setModifiedOn(OffsetDateTime modifiedOn) {
        this.modifiedOn = modifiedOn;
    }

    public Set<AnswerOption> getAnswerOptions() {
        return answerOptions;
    }

    public void setAnswerOptions(Set<AnswerOption> answerOptions) {
        this.answerOptions = answerOptions;
    }

    public List<TemplateSummary> getTemplates() {
        return templates;
    }

    public void setTemplates(List<TemplateSummary> templates) {
        this.templates = templates;
    }

    public int getNbProjectsUsedIn() {
        return nbProjectsUsedIn;
    }

    public void setNbProjectsUsedIn(int nbProjectsUsedIn) {
        this.nbProjectsUsedIn = nbProjectsUsedIn;
    }

    public int getNbTemplatesUsedIn() {
        return nbTemplatesUsedIn;
    }

    public void setNbTemplatesUsedIn(int nbTemplatesUsedIn) {
        this.nbTemplatesUsedIn = nbTemplatesUsedIn;
    }

    public boolean isEditable() {
        return editInUseQuestionsFeatureEnabled || (nbProjectsUsedIn == 0 && nbTemplatesUsedIn <= 1);
    }

    public boolean isEditInUseQuestionsFeatureEnabled() {
        return editInUseQuestionsFeatureEnabled;
    }

    public void setEditInUseQuestionsFeatureEnabled(boolean editInUseQuestionsFeatureEnabled) {
        this.editInUseQuestionsFeatureEnabled = editInUseQuestionsFeatureEnabled;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Question question = (Question) o;

        if (id != null ? !id.equals(question.id) : question.id != null) {
            return false;
        }
        if (text != null ? !text.equals(question.text) : question.text != null) {
            return false;
        }
        return answerType == question.answerType;

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
        return maxAnswers == null ? 1 : maxAnswers;
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

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (text != null ? text.hashCode() : 0);
        result = 31 * result + (answerType != null ? answerType.hashCode() : 0);
        return result;
    }

    public Question copy() {
        final Question copy = new Question();
        copy.setId(this.getId());
        copy.setText(this.getText());
        copy.setExternalKey(this.getExternalKey());
        copy.setAnswerType(this.getAnswerType());
        copy.setMaxUploadSizeInMb(this.getMaxUploadSizeInMb());
        copy.setMaxCombinedUploadSizeInMb(this.getMaxCombinedUploadSizeInMb());
        copy.setQuantity(this.getQuantity());
        copy.setMaxLength(this.getMaxLength());
        copy.setMaxAnswers(this.getMaxAnswers());
        copy.setDelimiter(this.getDelimiter());
        if (this.getAnswerOptions() != null) {
            copy.setAnswerOptions(this.getAnswerOptions()
                    .stream()
                    .map(AnswerOption::copy)
                    .collect(Collectors.toSet()));
        } else {
            copy.setAnswerOptions(null);
        }
        return copy;
    }

    public boolean canBeReplacedWith(Question otherQuestion) {
        if (!Objects.equals(this.answerType, otherQuestion.answerType)) {
            return false;
        }

        return !Dropdown.equals(this.answerType) || Objects.equals(this.answerOptions, otherQuestion.answerOptions);
    }

}
