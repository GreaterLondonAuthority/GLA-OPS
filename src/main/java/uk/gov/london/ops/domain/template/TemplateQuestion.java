/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.domain.template;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.commons.lang3.StringUtils;
import uk.gov.london.ops.domain.Requirement;
import uk.gov.london.ops.service.project.state.ProjectState;
import uk.gov.london.ops.framework.jpa.Join;
import uk.gov.london.ops.framework.jpa.JoinData;

import javax.persistence.*;
import java.io.Serializable;

@Entity(name="template_question")
public class TemplateQuestion implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "template_question_seq_gen")
    @SequenceGenerator(name = "template_question_seq_gen", sequenceName = "template_question_seq", initialValue = 1000, allocationSize = 1)
    private Integer id;

    @Column(name = "display_order")
    private Double displayOrder;

    @ManyToOne(cascade = {})
    @JoinColumn(name = "question_id")
    private Question question;

    @Column(name="requirement")
    @Enumerated(EnumType.STRING)
    private Requirement requirement;

    @Column(name = "parent_id")
    @JoinData(targetTable = "template_question", targetColumn = "question_id", joinType = Join.JoinType.OneToOne,
            comment = "For child questions this indicates the parent, which would be shown if parent_answer_to_match was matched")
    private Integer parentId;

    @Column(name = "parent_answer_to_match")
    private String parentAnswerToMatch;

    @Column(name = "section_id")
    private Integer sectionId;

    @Column(name = "appears_on_status")
    private String appearsOnStatus;

    @Column(name = "appears_on_sub_status")
    private String appearsOnSubStatus;

    public TemplateQuestion() {}

    public TemplateQuestion(final Double displayOrder,
                            final Question question,
                            final Requirement requirement) {
        this.displayOrder = displayOrder;
        this.question = question;
        this.requirement = requirement;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Double getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(Double displayOrder) {
        this.displayOrder = displayOrder;
    }

    public Question getQuestion() {
        return question;
    }

    public void setQuestion(Question question) {
        this.question = question;
    }

    public Requirement getRequirement() {
        return requirement;
    }

    public void setRequirement(Requirement requirement) {
        this.requirement = requirement;
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

    public String getAppearsOnStatus() {
        return appearsOnStatus;
    }

    public void setAppearsOnStatus(String appearsOnStatus) {
        this.appearsOnStatus = appearsOnStatus;
    }

    public String getAppearsOnSubStatus() {
        return appearsOnSubStatus;
    }

    public void setAppearsOnSubStatus(String appearsOnSubStatus) {
        this.appearsOnSubStatus = appearsOnSubStatus;
    }

    @JsonIgnore
    public ProjectState getAppearsOnState() {
        return new ProjectState(appearsOnStatus, appearsOnSubStatus);
    }

    public TemplateQuestion copy() {
        final TemplateQuestion copy =  new TemplateQuestion();
        copy.setId(this.getId());
        copy.setDisplayOrder(this.getDisplayOrder());
        copy.setRequirement(this.getRequirement());
        copy.setParentAnswerToMatch(this.getParentAnswerToMatch());
        copy.setParentId(this.getParentId());
        copy.setSectionId(this.getSectionId());
        copy.setAppearsOnStatus(this.getAppearsOnStatus());
        copy.setAppearsOnSubStatus(this.getAppearsOnSubStatus());
        if(this.getQuestion() != null) {
            copy.setQuestion(this.question.copy());
        } else {
            copy.setQuestion(null);
        }
        return copy;
    }

    public boolean appearsOnStateTransition() {
        return StringUtils.isNotEmpty(appearsOnStatus);
    }

    @JsonIgnore
    public boolean hasParent() {
        return parentId != null;
    }

    public boolean appearsOnState(ProjectState state) {
        return appearsOnStateTransition() && state.equals(getAppearsOnState());
    }

}
