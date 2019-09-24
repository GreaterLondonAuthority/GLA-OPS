/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.domain.template;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import java.io.Serializable;
import java.util.Objects;

/**
 * Created by chris on 07/12/2016.
 */
@Entity(name = "question_answer_options")
public class AnswerOption implements Serializable {

    @Id()
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "answer_option_seq_gen")
    @SequenceGenerator(name = "answer_option_seq_gen", sequenceName = "answer_option_seq", initialValue = 1000, allocationSize = 1)
    private Integer id;

    @Column(name="display_order")
    private Integer displayOrder;

    @Column(name="answer_options")
    private String option;

    public AnswerOption() {
    }

    public AnswerOption(String option) {
        this.option = option;
    }

    public AnswerOption(Integer displayOrder, String option) {
        this.displayOrder = displayOrder;
        this.option = option;
    }

    @JsonIgnore
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }

    public String getOption() {
        return option;
    }

    public void setOption(String option) {
        this.option = option;
    }

    public AnswerOption copy() {
        final AnswerOption copy = new AnswerOption();
        copy.setId(this.getId());
        copy.setOption(this.getOption());
        copy.setDisplayOrder(this.getDisplayOrder());
        return copy;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AnswerOption that = (AnswerOption) o;
        return Objects.equals(option, that.option);
    }

    @Override
    public int hashCode() {
        return Objects.hash(option);
    }

}
