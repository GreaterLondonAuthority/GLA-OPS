/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.framework;

import uk.gov.london.ops.project.question.Answer;
import uk.gov.london.ops.project.template.domain.AnswerOption;
import uk.gov.london.ops.project.template.domain.Question;
import uk.gov.london.ops.project.template.domain.TemplateQuestion;

import java.util.Set;

/**
 * Created by dieppa on 21/04/17.
 */
public class ProjectUtil {



    public static boolean checkAnswerEquals(Answer a, Answer copy) {
        return
//                a.getId().equals(copy.getId()) &&
                a.getAnswer().equals(copy.getAnswer())
                        && a.getNumericAnswer().equals(copy.getNumericAnswer())
                        && a.getQuestionId().equals(copy.getQuestionId())
                        && checkQuestionsEquals(a.getQuestion(), copy.getQuestion());
    }

    public static boolean checkAnswerOptionFilter(
            final AnswerOption ao,
            final Set<AnswerOption> set) {
        return set.stream().filter(a->
                a.getId().equals(ao.getId())
                        && a.getDisplayOrder().equals(ao.getDisplayOrder())
                        && a.getOption().equals(ao.getOption()))
                .count() == 1;
    }

    public static boolean checkQuestionsEquals(final Question expected,
                                               final Question actual) {
        boolean result = expected.getId().equals(actual.getId())
                && expected.getExternalKey().equals(actual.getExternalKey())
                && expected.getAnswerType().equals(actual.getAnswerType())
                && expected.getText().equals(actual.getText());

        return result &&
                actual.getAnswerOptions().stream()
                        .filter(ao-> checkAnswerOptionFilter(
                                ao,
                                expected.getAnswerOptions()))
                        .count() == expected.getAnswerOptions().size();
    }





    public static boolean checkTemplateQuestionEquals(
            final TemplateQuestion tq,
            final TemplateQuestion copy) {
        return tq.getId().equals(copy.getId())
                && tq.getDisplayOrder().equals(copy.getDisplayOrder())
                && tq.getRequirement().equals(copy.getRequirement())
                && tq.getQuestion().getId().equals(copy.getQuestion().getId())
                && checkQuestionsEquals(tq.getQuestion(), copy.getQuestion());
    }
}
