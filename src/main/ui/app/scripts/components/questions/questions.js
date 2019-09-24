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

  isVisible(question) {
    if(question && question.isHidden){
      return false;
    }else if(this.QuestionsService.hasParentCondition(question)){
      return this.QuestionsService.isParentConditionMet(question, this.questions);
    }else{
      return true;
    }
  }
}

Questions.$inject = ['QuestionsService'];

angular.module('GLA')
  .component('questions', {
    templateUrl: 'scripts/components/questions/questions.html',
    bindings: {
      questions: '<',
      project: '<',
      readOnly: '<'
    },
    controller: Questions

  });
