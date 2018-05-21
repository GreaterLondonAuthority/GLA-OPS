/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.domain.template;

import uk.gov.london.ops.domain.project.ProjectBlockType;
import uk.gov.london.ops.util.jpajoins.Join;
import uk.gov.london.ops.util.jpajoins.JoinData;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by chris on 19/12/2016.
 */
@Entity
@DiscriminatorValue("QUESTIONS")
public class QuestionsTemplateBlock extends TemplateBlock {

    @JoinData(joinType = Join.JoinType.Complex, sourceTable = "template_block"
              , comment = "Join from template_block to template_question via join table TEMPLATE_BLOCK_QUESTION  ")
    @OneToMany(cascade = CascadeType.ALL)
    @JoinTable(
            name = "TEMPLATE_BLOCK_QUESTION",
            joinColumns = @JoinColumn(name = "template_block_id"),
            inverseJoinColumns = @JoinColumn(name = "question_id")
    )
    private Set<TemplateQuestion> questions = new HashSet<>();

    @JoinData(joinType = Join.JoinType.OneToMany, sourceTable = "template_block", targetColumn = "id", targetTable = "questions_block_section", comment = "")
    @OneToMany(cascade = CascadeType.ALL, targetEntity = QuestionsBlockSection.class)
    @JoinColumn(name = "template_block_id")
    private Set<QuestionsBlockSection> sections = new HashSet<>();

    public QuestionsTemplateBlock() {
        super(ProjectBlockType.Questions);
    }

    public QuestionsTemplateBlock(String blockName) {
        super(blockName);
    }

    public QuestionsTemplateBlock(Integer displayOrder, ProjectBlockType block) {
        super(displayOrder, block);
    }

    public QuestionsTemplateBlock(Integer displayOrder, ProjectBlockType block, String blockDisplayName) {
        super(displayOrder, block, blockDisplayName);
    }

    @Override
    public void updateCloneFromBlock(TemplateBlock clone) {
        super.updateCloneFromBlock(clone);
        QuestionsTemplateBlock cloned = (QuestionsTemplateBlock) clone;

        for (TemplateQuestion question : questions) {
            TemplateQuestion clonedQuestion = new TemplateQuestion(question.getDisplayOrder(), question.getQuestion(), question.getRequirement());
            cloned.getQuestions().add(clonedQuestion);
        }

        for (QuestionsBlockSection section: sections) {
            QuestionsBlockSection clonedSection = new QuestionsBlockSection(section.getExternalId(), section.getDisplayOrder(), section.getText());
            cloned.getSections().add(clonedSection);
        }

    }

    public Set<TemplateQuestion> getQuestions() {
        return questions;
    }

    public void setQuestions(Set<TemplateQuestion> questions) {
        this.questions = questions;
    }

    public Set<QuestionsBlockSection> getSections() {
        return sections;
    }

    public void setSections(Set<QuestionsBlockSection> sections) {
        this.sections = sections;
    }

    public TemplateQuestion getQuestionById(Integer id) {
        return questions.stream().filter(tq -> tq.getQuestion().getId().equals(id)).findFirst().orElse(null);
    }

}
