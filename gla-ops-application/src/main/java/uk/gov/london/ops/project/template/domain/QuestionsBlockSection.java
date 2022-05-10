/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.project.template.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;

@Entity(name = "questions_block_section")
public class QuestionsBlockSection implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "questions_block_section_seq_gen")
    @SequenceGenerator(name = "questions_block_section_seq_gen", sequenceName = "questions_block_section_seq",
            initialValue = 10000, allocationSize = 1)
    private Integer id;

    @Column(name = "external_id")
    private Integer externalId;

    @Column(name = "display_order")
    private Double displayOrder;

    @Column(name = "text")
    private String text;

    @Column(name = "parent_id")
    private Integer parentId;

    @Column(name = "parent_answer_to_match")
    private String parentAnswerToMatch;

    public QuestionsBlockSection() {
    }

    public QuestionsBlockSection(Integer externalId, Double displayOrder, String text, Integer parentId, String parentAnswerToMatch) {
        this.externalId = externalId;
        this.displayOrder = displayOrder;
        this.text = text;
        this.parentId = parentId;
        this.parentAnswerToMatch = parentAnswerToMatch;
    }

    public QuestionsBlockSection(Integer externalId, Double displayOrder, String text) {
        this(externalId, displayOrder, text, null, null);
    }

    public QuestionsBlockSection(Integer externalId, Integer displayOrder, String text) {
        this(externalId, new Double(displayOrder), text);
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getExternalId() {
        return externalId;
    }

    public void setExternalId(Integer externalId) {
        this.externalId = externalId;
    }

    public Double getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(Double displayOrder) {
        this.displayOrder = displayOrder;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
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

    @JsonIgnore
    public boolean hasParent() {
        return parentId != null;
    }

    public QuestionsBlockSection copy() {
        QuestionsBlockSection copy = new QuestionsBlockSection();
        copy.setExternalId(this.getExternalId());
        copy.setDisplayOrder(this.getDisplayOrder());
        copy.setText(this.getText());
        copy.setParentId(this.getParentId());
        copy.setParentAnswerToMatch(this.getParentAnswerToMatch());
        return copy;
    }

}
