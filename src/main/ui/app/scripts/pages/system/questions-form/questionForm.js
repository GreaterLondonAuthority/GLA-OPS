/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */


class QuestionForm {
  constructor($state, $stateParams, QuestionsService, ConfirmationDialog) {
    this.$state = $state;
    this.$stateParams = $stateParams;
    this.QuestionsService = QuestionsService;
    this.ConfirmationDialog = ConfirmationDialog;
  }

  $onInit() {
    this.question = this.question || {answerOptions: [{}, {}]};
    this.answerTypes = [
      'Number',
      'YesNo',
      'FreeText',
      'Date',
      'Text',
      'FileUpload',
      'Dropdown'
    ]
  }

  addNewDropdownOption() {
    this.question.answerOptions.push({});
  }


  deleteDropdownOption(index) {
    this.question.answerOptions.splice(index, 1);
  }

  prepareQuestionForRequest(question) {
    if (question.answerType !== 'Dropdown') {
      delete question.answerOptions;
    }

    if (question.answerType !== 'FileUpload') {
      delete question.quantity;
    }

    if (question.answerType !== 'FreeText') {
      delete question.maxLength;
    }

    if (question.answerType === 'Dropdown') {
      question.answerOptions = this.getValidOptions(question);
      let i=1;
      question.answerOptions.forEach(ao => ao.displayOrder = i++);
    }

  }

  save() {
    this.prepareQuestionForRequest(this.question);
    console.log('saving', this.question);
    this.QuestionsService.saveQuestion(this.question, this.$stateParams.questionId).then(() => {
      this.goBack();
    }).catch(err => {
      let errMessage = (err.data || {}).description || 'Failed to save';
      this.ConfirmationDialog.warn(errMessage);
    });
  }


  goBack() {
    this.$state.go('system-templates-questions');
  }

  isQuestionValid(question){
    this.hasDuplicate = false;
    if(!question.text || !question.answerType){
      return false;
    }

    if (question.answerType === 'Dropdown' && this.getValidOptions(question).length < 2) {
      this.hasDuplicate = true;
      return false;

    }

    return true;
  }

  getValidOptions(question){
    let optionsWithText = _.filter(question.answerOptions, ao => !!ao.option);
    return _.uniqBy(optionsWithText, 'option');
  }

}

QuestionForm.$inject = ['$state', '$stateParams', 'QuestionsService', 'ConfirmationDialog'];

angular.module('GLA')
  .component('questionForm', {
    templateUrl: 'scripts/pages/system/questions-form/questionForm.html',
    bindings: {
      question: '<?'
    },
    controller: QuestionForm
  });
