/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.domain.template;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@JsonIgnoreProperties(ignoreUnknown = true, value = {"handler", "hibernateLazyInitializer"})
public class Question implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "question_seq_gen")
    @SequenceGenerator(name = "question_seq_gen", sequenceName = "question_seq", initialValue = 1000, allocationSize = 1)
    private Integer id;

    @Column(name = "external_key")
    private String externalKey;

    @Column(name = "text")
    private String text;

    @Column(name="answer_type")
    @Enumerated(EnumType.STRING)
    private AnswerType answerType;

    @Column(name="quantity")
    private Integer quantity;

    @Column(name="max_length")
    private Integer maxLength;

    @OneToMany(cascade = {CascadeType.ALL}, targetEntity = AnswerOption.class)
    @JoinColumn(name="question_id")
    private Set<AnswerOption> answerOptions;

    public Question() {}

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

    public Set<AnswerOption> getAnswerOptions() {
        return answerOptions;
    }

    public void setAnswerOptions(Set<AnswerOption> answerOptions) {
        this.answerOptions = answerOptions;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Question question = (Question) o;

        if (id != null ? !id.equals(question.id) : question.id != null) return false;
        if (text != null ? !text.equals(question.text) : question.text != null) return false;
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
        copy.setQuantity(this.getQuantity() );
        copy.setMaxLength(this.getMaxLength() );
        if(this.getAnswerOptions()!= null) {
            copy.setAnswerOptions(this.getAnswerOptions()
                    .stream()
                    .map(AnswerOption::copy)
                    .collect(Collectors.toSet()));
        } else {
            copy.setAnswerOptions(null);
        }
        return copy;
    }


}
