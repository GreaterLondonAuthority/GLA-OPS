/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.project.template.domain;

import java.util.Set;

public interface TemplateQuestionsBlockInterface extends TemplateBlockInterface {

    Set<TemplateQuestion> getQuestions();

    Set<QuestionsBlockSection> getSections();

    default TemplateQuestion getQuestionById(Integer id) {
        return getQuestions().stream().filter(tq -> tq.getQuestion().getId().equals(id)).findFirst().orElse(null);
    }

}
