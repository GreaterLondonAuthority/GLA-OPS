/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

import './questionFileUpload.js'

class Questions {
  constructor(QuestionsService) {
    this.QuestionsService = QuestionsService;
  }

  $onInit(){
  }

  isVisible(question) {
    return this.QuestionsService.isQuestionVisible(question, this.questions);
  }

  updateSectionVisibility(){
    this.QuestionsService.updateSectionVisibility(this.questions);
  }

  onMultiSelectChange(check, question) {
    let answers = (question.answerOptions || []).filter(ao => !!ao.model);
    question.answer = answers.map(ao => ao.label).join(question.delimiter);
    this.updateSectionVisibility();
  }

  formatDropdownAnswer(question){
    if(!question || !question.answer){
      return null;
    }
    return question.answer.split(question.delimiter).join(question.delimiter + ' ');
  }
}

Questions.$inject = ['QuestionsService'];

angular.module('GLA')
  .component('glaQuestions', {
    templateUrl: 'scripts/components/questions/questions.html',
    bindings: {
      questions: '<',
      block: '<',
      project: '<',
      readOnly: '<'
    },
    controller: Questions

  });
