/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.project.question;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import uk.gov.london.ops.project.template.domain.TemplateQuestion;

/**
 * Join table between a project questions block and a template question.
 */
@Entity(name = "project_block_question")
public class ProjectQuestion implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "project_block_question_seq_gen")
    @SequenceGenerator(name = "project_block_question_seq_gen", sequenceName = "project_block_question_seq",
            initialValue = 10000, allocationSize = 1)
    private Integer id;

    @ManyToOne(cascade = {})
    @JoinColumn(name = "question_id")
    private TemplateQuestion templateQuestion;

    @Column(name = "hidden")
    private boolean hidden;

    @Column(name = "is_new")
    private boolean isNew;

    public ProjectQuestion() {
    }

    public ProjectQuestion(TemplateQuestion templateQuestion) {
        this.templateQuestion = templateQuestion;
        this.hidden = templateQuestion.appearsOnStateTransition();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public TemplateQuestion getTemplateQuestion() {
        return templateQuestion;
    }

    public void setTemplateQuestion(TemplateQuestion templateQuestion) {
        this.templateQuestion = templateQuestion;
    }

    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    public boolean isNew() {
        return isNew;
    }

    public void setNew(boolean isNew) {
        this.isNew = isNew;
    }

    public ProjectQuestion copy() {
        ProjectQuestion copy = new ProjectQuestion();
        copy.setTemplateQuestion(this.getTemplateQuestion());
        copy.setHidden(this.isHidden());
        copy.setNew(this.isNew());
        return copy;
    }

}
