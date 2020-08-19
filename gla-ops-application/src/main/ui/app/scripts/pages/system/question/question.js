/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */


class QuestionCtrl {
  constructor($state) {
    this.$state = $state;
  }

  $onInit() {
    this.editable = this.question.editable;
    this.readOnly = true;
  }

  goBack() {
    this.$state.go('system-templates-questions');
  }

  edit() {
    this.$state.go('system-question-edit', {
      questionId: this.question.id,
      question: this.question
    });
  }

}

QuestionCtrl.$inject = ['$state'];

angular.module('GLA')
  .component('questionPage', {
    templateUrl: 'scripts/pages/system/question/question.html',
    bindings: {
      question: '<'
    },
    controller: QuestionCtrl
  });
