/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.project.template.domain;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.persistence.CascadeType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;
import uk.gov.london.ops.framework.jpa.Join;
import uk.gov.london.ops.framework.jpa.JoinData;
import uk.gov.london.ops.project.block.ProjectBlockType;

/**
 * Created by chris on 19/12/2016.
 */
@Entity
@DiscriminatorValue("QUESTIONS")
public class QuestionsTemplateBlock extends TemplateBlock implements TemplateQuestionsBlockInterface {

    @JoinData(joinType = Join.JoinType.Complex, sourceTable = "template_block", sourceColumn = "-",
            comment = "Join from template_block to template_question via join table TEMPLATE_BLOCK_QUESTION  ")
    @OneToMany(cascade = CascadeType.ALL)
    @JoinTable(
            name = "TEMPLATE_BLOCK_QUESTION",
            joinColumns = @JoinColumn(name = "template_block_id"),
            inverseJoinColumns = @JoinColumn(name = "question_id")
    )
    private Set<TemplateQuestion> questions = new HashSet<>();

    @JoinData(joinType = Join.JoinType.ManyToOne, sourceTable = "questions_block_section", sourceColumn = "template_block_id",
            targetColumn = "id", targetTable = "template_block", comment = "")
    @OneToMany(cascade = CascadeType.ALL, targetEntity = QuestionsBlockSection.class)
    @JoinColumn(name = "template_block_id")
    private Set<QuestionsBlockSection> sections = new HashSet<>();

    public QuestionsTemplateBlock() {
        super(ProjectBlockType.Questions);
    }

    public QuestionsTemplateBlock(String blockName) {
        super(blockName);
    }

    public QuestionsTemplateBlock(Integer displayOrder) {
        super(displayOrder, ProjectBlockType.Questions);
    }

    public QuestionsTemplateBlock(Integer displayOrder, ProjectBlockType block, String blockDisplayName) {
        super(displayOrder, block, blockDisplayName);
    }

    @Override
    public void updateCloneFromBlock(TemplateBlock clone) {
        super.updateCloneFromBlock(clone);
        QuestionsTemplateBlock cloned = (QuestionsTemplateBlock) clone;

        for (TemplateQuestion question : questions) {
            TemplateQuestion copy = question.copy();
            cloned.getQuestions().add(copy);
        }

        for (QuestionsBlockSection section : sections) {
            QuestionsBlockSection clonedSection = new QuestionsBlockSection(section.getExternalId(), section.getDisplayOrder(),
                    section.getText());
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

    public List<TemplateQuestion> getQuestionsByParentId(Integer parentId) {
        return questions.stream().filter(tq -> parentId.equals(tq.getParentId())).collect(Collectors.toList());
    }

}
