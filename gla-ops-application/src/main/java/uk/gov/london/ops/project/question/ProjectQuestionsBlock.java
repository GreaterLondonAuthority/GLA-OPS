/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.project.question;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.persistence.CascadeType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import uk.gov.london.ops.framework.enums.Requirement;
import uk.gov.london.ops.framework.exception.ValidationException;
import uk.gov.london.ops.framework.jpa.Join;
import uk.gov.london.ops.framework.jpa.JoinData;
import uk.gov.london.ops.project.Project;
import uk.gov.london.ops.project.block.NamedProjectBlock;
import uk.gov.london.ops.project.block.ProjectBlockType;
import uk.gov.london.ops.project.block.ProjectDifference;
import uk.gov.london.ops.project.block.ProjectDifferences;
import uk.gov.london.ops.project.state.StateTransition;
import uk.gov.london.ops.project.template.domain.AnswerType;
import uk.gov.london.ops.project.template.domain.Question;
import uk.gov.london.ops.project.template.domain.QuestionsBlockSection;
import uk.gov.london.ops.project.template.domain.QuestionsTemplateBlock;
import uk.gov.london.ops.project.template.domain.TemplateBlock;
import uk.gov.london.ops.project.template.domain.TemplateQuestion;

/**
 * The Additional Questions block in a Project.
 *
 * @author Steve Leach
 */
@Entity(name = "questions_block")
@DiscriminatorValue("QUESTIONS")
@JoinData(sourceTable = "questions_block", sourceColumn = "id", targetTable = "project_block",
        targetColumn = "id", joinType = Join.JoinType.OneToOne,
        comment = "the questions block is a subclass of the project block and shares a common key")
public class ProjectQuestionsBlock extends NamedProjectBlock implements QuestionsBlock {

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true, targetEntity = Answer.class)
    @JoinColumn(name = "questions_block", nullable = false)
    private Set<Answer> answers = new HashSet<>();

    @JsonIgnore
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, targetEntity = ProjectQuestion.class)
    @JoinColumn(name = "project_block_id")
    private Set<ProjectQuestion> questions = new HashSet<>();

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, targetEntity = QuestionsBlockSection.class)
    @JoinColumn(name = "project_block_id")
    private Set<QuestionsBlockSection> sections = new HashSet<>();

    public ProjectQuestionsBlock() {}

    public ProjectQuestionsBlock(Project project) {
        super(project);
    }

    @Override
    public ProjectBlockType getBlockType() {
        return ProjectBlockType.Questions;
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

    public boolean hasAnswers() {
        return (getAnswers() != null && !getAnswers().isEmpty());
    }

    boolean isQuestionAnswered(Integer questionId) {
        Answer answer = getAnswerByQuestionId(questionId);
        return answer != null && !StringUtils.isEmpty(answer.getAnswerAsText());
    }

    @Override
    public boolean isComplete() {

        if (!isVisited()) {
            return false;
        }

        if (CollectionUtils.isEmpty(this.questions)) {
            // No questions, so can't be any mandatory ones that haven't been answered.
            return true;
        }

        Map<Integer, Answer> answerMap = questionToAnswerMap();

        for (ProjectQuestion projectQuestion : this.questions) {
            TemplateQuestion templateQuestion = projectQuestion.getTemplateQuestion();
            Question question = templateQuestion.getQuestion();
            if (!projectQuestion.isHidden() && Requirement.isRequired(templateQuestion.getRequirement())) {
                Answer answer = answerMap.get(templateQuestion.getQuestion().getId());

                if (templateQuestion.getParentId() != null) {
                    Answer parentAnswer = answerMap.get(templateQuestion.getParentId());
                    if (parentAnswer == null) {
                        return false; // if parent is unanswered and mandatory then block is incomplete
                    }
                    String parentAnswerToMatch = templateQuestion.getParentAnswerToMatch();
                    if (parentAnswerToMatch == null) {
                        return false; // shouldn't be possible
                    }
                    if (!parentAnswerToMatch.equals(parentAnswer.getAnswerAsText())) {
                        continue;
                    }
                }

                if (answer == null) {
                    return false;
                }

                if (AnswerType.Number.equals(question.getAnswerType()) && answer.getNumericAnswer() == null) {
                    return false;
                }

                if (AnswerType.FileUpload.equals(question.getAnswerType()) && answer.getFileAttachments().isEmpty()) {
                    return false;
                }

                if (!AnswerType.Number.equals(question.getAnswerType())
                        && !AnswerType.FileUpload.equals(question.getAnswerType())
                        && StringUtils.isEmpty(answer.getAnswer())) {
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    protected void generateValidationFailures() {
        // do nothing for now
    }

    /**
     * Merges new/updated answers into the project
     */
    public void merge(NamedProjectBlock block) {
        if (!this.getClass().isAssignableFrom(block.getClass())) {
            throw new ValidationException("Unable to merge due to incorrect block type");
        }

        ProjectQuestionsBlock questionsBlock = (ProjectQuestionsBlock) block;
        mergeAnswersFrom(questionsBlock);

        clearDependantAnswers();
    }

    public void clearDependantAnswers() {
        // remove any incorrectly answered dependant questions
        for (Answer answer : this.answers) {
            if (hasParent(answer) && !isParentConditionMet(answer)) {
                answer.clear();
            }
        }
    }

    private boolean hasParent(Answer answer) {
        return getTemplateQuestionByQuestionId(answer.getQuestionId()).hasParent();
    }

    private boolean isParentConditionMet(Answer answer) {
        TemplateQuestion templateQuestion = getTemplateQuestionByQuestionId(answer.getQuestionId());
        Integer parentQuestionId = templateQuestion.getParentId();
        TemplateQuestion parentTemplateQuestion = getTemplateQuestionByQuestionId(parentQuestionId);
        Answer parentAnswer = getAnswerByQuestionId(parentQuestionId);

        return (!parentTemplateQuestion.hasParent() || isParentConditionMet(parentAnswer))
                && Objects.equals(templateQuestion.getParentAnswerToMatch(), parentAnswer.getAnswerAsText());
    }

    /**
     * Returns a map of question IDs to answers.
     */
    private Map<Integer, Answer> questionToAnswerMap() {
        Map<Integer, Answer> answerMap;
        if (hasAnswers()) {
            answerMap = getAnswers().stream().collect(Collectors.toMap(Answer::getQuestionId, Function.identity()));
        } else {
            answerMap = Collections.EMPTY_MAP;
        }
        return answerMap;
    }

    public TemplateQuestion getTemplateQuestionByQuestionId(Integer questionId) {
        return getTemplateQuestions().stream().filter(tq -> tq.getQuestion().getId().equals(questionId)).findFirst().orElse(null);
    }

    @Override
    protected void initFromTemplateSpecific(TemplateBlock templateBlock) {
        QuestionsTemplateBlock qtb = (QuestionsTemplateBlock) templateBlock;
        for (TemplateQuestion tq : qtb.getQuestions()) {
            addTemplateQuestion(tq);
        }
        getSections().addAll(qtb.getSections().stream().map(QuestionsBlockSection::copy).collect(Collectors.toSet()));
    }

    protected void copyBlockContentInto(NamedProjectBlock t) {
        final ProjectQuestionsBlock target = (ProjectQuestionsBlock) t;
        copyDataFrom(target);
    }

    @Override
    protected void compareBlockSpecificContent(NamedProjectBlock other, ProjectDifferences differences) {
        ProjectQuestionsBlock otherQuestionsBlock = (ProjectQuestionsBlock) other;
        Map<Integer, Answer> thisAnswers = this.questionToAnswerMap();
        Map<Integer, Answer> otherAnswers = otherQuestionsBlock.questionToAnswerMap();

        for (Integer questionId : thisAnswers.keySet()) {
            // additions
            ProjectQuestion thisQuestion = this.getProjectQuestionByQuestionId(questionId);
            ProjectQuestion otherQuestion = otherQuestionsBlock.getProjectQuestionByQuestionId(questionId);
            if (!otherQuestionsBlock.isQuestionAnswered(questionId) || (otherQuestion != null && otherQuestion.isHidden()
                    && thisQuestion != null && !thisQuestion.isHidden())) {
                differences.add(new ProjectDifference(thisAnswers.get(questionId), ProjectDifference.DifferenceType.Addition));
            } else { // changes
                Answer thisAnswer = thisAnswers.get(questionId);
                Answer otherAnswer = otherAnswers.get(questionId);

                if (!Objects.equals(StringUtils.trimAllWhitespace(thisAnswer.getAnswerAsText()),
                        StringUtils.trimAllWhitespace(otherAnswer.getAnswerAsText()))) {
                    differences.add(new ProjectDifference(thisAnswer, ProjectDifference.DifferenceType.Change));
                }
            }
        }

        // deletions
        for (Integer questionId : otherAnswers.keySet()) {
            if (!this.isQuestionAnswered(questionId)) {
                differences.add(new ProjectDifference(otherAnswers.get(questionId), ProjectDifference.DifferenceType.Deletion));
            }
        }
    }

    @Override
    public boolean isBlockRevertable() {
        return true;
    }

    @Override
    public void handleStateTransitionSpecific(StateTransition stateTransition) {
        for (ProjectQuestion question : questions) {
            if (question.isHidden() && question.getTemplateQuestion().appearsOnState(stateTransition.getTo())) {
                question.setHidden(false);
                question.setNew(true);
            } else if (stateTransition.isClearNewLabel() && this.isComplete()) {
                question.setNew(false);
            }
        }
    }

    @Override
    public Boolean hasUpdates() {
        boolean response = false;
        for (ProjectQuestion projectQuestion : this.questions) {
            if (projectQuestion.isNew()) {
                Integer parentId = projectQuestion.getTemplateQuestion().getParentId();
                if (parentId != null) {
                    Answer answer = getAnswerByQuestionId(parentId);
                    if (answer != null && projectQuestion.getTemplateQuestion().getParentAnswerToMatch()
                            .equalsIgnoreCase(answer.getAnswerAsText())) {
                        response = true;
                    }
                } else {
                    response = true;
                }
            }
        }
        return response;
    }

}


