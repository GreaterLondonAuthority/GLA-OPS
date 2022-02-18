/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.project.template.domain;

import java.util.List;
import java.util.stream.Collectors;
import uk.gov.london.ops.framework.JSONUtils;
import uk.gov.london.ops.framework.jpa.Join;
import uk.gov.london.ops.framework.jpa.JoinData;
import uk.gov.london.ops.project.internalblock.InternalBlockType;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@DiscriminatorValue("QUESTIONS")
public class InternalQuestionsTemplateBlock extends InternalTemplateBlock implements TemplateQuestionsBlockInterface {

    @JoinData(joinType = Join.JoinType.Complex, sourceTable = "internal_template_block", sourceColumn = "-",
            comment = "Join from internal_template_block to template_question via join table internal_template_block_question")
    @OneToMany(cascade = CascadeType.ALL)
    @JoinTable(
            name = "internal_template_block_question",
            joinColumns = @JoinColumn(name = "internal_template_block_id"),
            inverseJoinColumns = @JoinColumn(name = "question_id")
    )
    private Set<TemplateQuestion> questions = new HashSet<>();

    @JoinData(joinType = Join.JoinType.ManyToOne, sourceTable = "internal_template_block", sourceColumn = "id",
            targetColumn = "internal_template_block_id", targetTable = "questions_block_section", comment = "")
    @OneToMany(cascade = CascadeType.ALL, targetEntity = QuestionsBlockSection.class)
    @JoinColumn(name = "internal_template_block_id")
    private Set<QuestionsBlockSection> sections = new HashSet<>();

    @Transient
    private boolean showComments = false;

    public InternalQuestionsTemplateBlock() {
        super(InternalBlockType.Questions);
    }

    public InternalQuestionsTemplateBlock(Integer displayOrder) {
        super(InternalBlockType.Questions, displayOrder);
    }

    public Set<TemplateQuestion> getQuestions() {
        return questions;
    }

    public void setQuestions(Set<TemplateQuestion> questions) {
        this.questions = questions;
    }

    public boolean isShowComments() {
        return showComments;
    }

    public void setShowComments(boolean showComments) {
        this.showComments = showComments;
    }

    @Override
    public Set<QuestionsBlockSection> getSections() {
        return sections;
    }

    public void setSections(Set<QuestionsBlockSection> sections) {
        this.sections = sections;
    }

    @PostLoad
    void loadBlockData() {
        InternalQuestionsTemplateBlock data = JSONUtils.fromJSON(this.blockData, InternalQuestionsTemplateBlock.class);
        if (data != null) {
            this.setShowComments(data.isShowComments());
        }
    }

    @Override
    public InternalQuestionsTemplateBlock clone() {
        InternalQuestionsTemplateBlock clone = (InternalQuestionsTemplateBlock) super.clone();
        clone.setShowComments(this.showComments);
        for (TemplateQuestion question : questions) {
            TemplateQuestion copy = question.copy();
            clone.getQuestions().add(copy);
        }
        for (QuestionsBlockSection section : sections) {
            QuestionsBlockSection clonedSection = new QuestionsBlockSection(section.getExternalId(), section.getDisplayOrder(),
                    section.getText());
            clone.getSections().add(clonedSection);
        }
        return clone;
    }

    @Override
    public List<TemplateBlockCommand> getTemplateBlockCommands() {
        List<TemplateBlockCommand> globalCommands = super.getTemplateBlockCommands().stream().collect(Collectors.toList());
        globalCommands.add(TemplateBlockCommand.REMOVE_QUESTION);
        return globalCommands;
    }
}
