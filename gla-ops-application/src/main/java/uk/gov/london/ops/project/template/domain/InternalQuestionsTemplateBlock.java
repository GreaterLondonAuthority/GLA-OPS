/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.project.template.domain;

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

    @Override
    public Set<QuestionsBlockSection> getSections() {
        return null;
    }

    @Override
    public InternalQuestionsTemplateBlock clone() {
        InternalQuestionsTemplateBlock clone = (InternalQuestionsTemplateBlock) super.clone();
        for (TemplateQuestion question : questions) {
            TemplateQuestion copy = question.copy();
            clone.getQuestions().add(copy);
        }
        return clone;
    }

}
