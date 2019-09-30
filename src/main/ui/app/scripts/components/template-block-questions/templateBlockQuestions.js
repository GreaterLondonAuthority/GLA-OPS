/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

const gla = angular.module('GLA');

class TemplateBlockQuestions {

  constructor($state, QuestionModal, ConfirmationDialog) {
    this.$state = $state;
    this.QuestionModal = QuestionModal;
    this.ConfirmationDialog = ConfirmationDialog;
  }

  $onInit() {
    this.question = this.question || {answerOptions: [{}, {}]};
    this.questions = this.block.questions;
    this.refreshQuestions()

  }

  refreshQuestions() {
    this.sortedQuestions = _.groupBy(this.questions, 'sectionId');
    this.sections = Object.keys(this.sortedQuestions).map(sectionId => {
      return {
        id: sectionId,
        displayOrder: sectionId == 'undefined' ? -1 : (_.find(this.block.sections, {externalId: +sectionId}) || {}).displayOrder
      };
    });
    this.sortedSections =_.sortBy(this.sections, 'displayOrder');
  }

  refresh() {
    this.$state.reload();
  }

  getSectionText(sectionId) {
    if(sectionId == 'undefined'){
      return 'No section specified'
    }
    let section = (_.find(this.block.sections, {externalId: +sectionId}));
    return section.text;
  }


  showQuestionModal(question) {
    let modal = this.QuestionModal.show(question, this.questions);
    modal.result.then((question) => {
      if (question) {
        this.block.questions.push(question);
        this.refreshQuestions();
      } else {
        alert('Can\'t add to invalid template JSON');
      }

    });
  }

  deleteQuestion(question) {
    let modal = this.ConfirmationDialog.delete('Are you sure you want to delete question ' + question.question.id + '?');
    modal.result.then(() => {
      _.remove(this.questions, question);
      this.onRemoveQuestion(question, {event:this.block});
      this.refreshQuestions();

    });
  }
}

TemplateBlockQuestions.$inject = ['$state', 'QuestionModal', 'ConfirmationDialog'];

gla.component('templateBlockQuestions', {
  templateUrl: 'scripts/components/template-block-questions/templateBlockQuestions.html',
  controller: TemplateBlockQuestions,
  bindings: {
    block: '<',
    readOnly: '<',
    onRemoveQuestion: '&'
  },
});

