/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.project.internalblock;

import com.fasterxml.jackson.annotation.JsonIgnore;
import uk.gov.london.ops.framework.jpa.Join;
import uk.gov.london.ops.framework.jpa.JoinData;
import uk.gov.london.ops.project.question.Answer;
import uk.gov.london.ops.project.question.ProjectQuestion;
import uk.gov.london.ops.project.question.QuestionsBlock;
import uk.gov.london.ops.project.template.domain.InternalQuestionsTemplateBlock;
import uk.gov.london.ops.project.template.domain.InternalTemplateBlock;
import uk.gov.london.ops.project.template.domain.QuestionsBlockSection;
import uk.gov.london.ops.project.template.domain.TemplateQuestion;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Entity(name = "internal_questions_block")
@DiscriminatorValue("QUESTIONS")
@JoinData(sourceTable = "internal_questions_block", sourceColumn = "id", targetTable = "internal_project_block",
        targetColumn = "id", joinType = Join.JoinType.OneToOne,
        comment = "the internal questions block is a subclass of the internal project block and shares a common key")
public class InternalQuestionsBlock extends InternalProjectBlock implements QuestionsBlock {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "internal_questions_block_seq_gen")
    @SequenceGenerator(name = "internal_questions_block_seq_gen", sequenceName = "internal_questions_block_seq",
            initialValue = 10000, allocationSize = 1)
    private Integer id;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true, targetEntity = Answer.class)
    @JoinColumn(name = "internal_questions_block_id", nullable = false)
    private Set<Answer> answers = new HashSet<>();

    @JsonIgnore
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, targetEntity = ProjectQuestion.class)
    @JoinColumn(name = "internal_questions_block_id")
    private Set<ProjectQuestion> questions = new HashSet<>();

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, targetEntity = QuestionsBlockSection.class)
    @JoinColumn(name = "internal_questions_block_id")
    private Set<QuestionsBlockSection> sections = new HashSet<>();

    public InternalQuestionsBlock() {
        setType(InternalBlockType.Questions);
    }

    @Override
    public Integer getId() {
        return id;
    }

    @Override
    public Set<Answer> getAnswers() {
        return answers;
    }

    @Override
    public void setAnswers(Set<Answer> answers) {
        this.answers = answers;
    }

    @Override
    public Set<ProjectQuestion> getQuestions() {
        return questions;
    }

    @Override
    public void setQuestions(Set<ProjectQuestion> questions) {
        this.questions = questions;
    }

    @Override
    public Set<QuestionsBlockSection> getSections() {
        return sections;
    }

    @Override
    public void setSections(Set<QuestionsBlockSection> sections) {
        this.sections = sections;
    }

    @Override
    public InternalQuestionsBlock clone() {
        InternalQuestionsBlock clone = (InternalQuestionsBlock) super.clone();
        copyDataFrom(clone);
        return clone;
    }

    @Override
    public String merge(InternalProjectBlock updated) {
        InternalQuestionsBlock questionsBlock = (InternalQuestionsBlock) updated;
        mergeAnswersFrom(questionsBlock);
        return null;
    }

    @Override
    protected void initFromTemplateSpecific(InternalTemplateBlock templateBlock) {
        InternalQuestionsTemplateBlock iqtb = (InternalQuestionsTemplateBlock) templateBlock;
        for (TemplateQuestion tq : iqtb.getQuestions()) {
            addTemplateQuestion(tq);
        }
        getSections().addAll(iqtb.getSections().stream().map(QuestionsBlockSection::copy).collect(Collectors.toSet()));
    }

    public TemplateQuestion getTemplateQuestionByQuestionId(int id) {
        return getTemplateQuestions().stream().filter(q -> q.getQuestion().getId().equals(id)).findFirst().orElse(null);
    }
}
