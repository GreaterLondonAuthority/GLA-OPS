/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

const gla = angular.module('GLA');

class TemplateBlockQuestions {

  constructor($state, QuestionModal, SectionModal, ConfirmationDialog) {
    this.$state = $state;
    this.QuestionModal = QuestionModal;
    this.SectionModal = SectionModal;
    this.ConfirmationDialog = ConfirmationDialog;
  }

  $onInit() {
    this.question = this.question || {answerOptions: [{}, {}]};
    this.questions = this.block.questions;
    this.refreshQuestions();

    let ctrl = this;
  }

  $onChanges() {
    this.refreshQuestions();
  }

  sortQuestions() {
    this.questions = _.sortBy(this.questions, 'displayOrder');
  }

  isParentQuestion(id){
    let parentIds = this.questions.map(q => + q.parentId).filter(item => item !== undefined);
    return parentIds.indexOf(id) !== -1;
  }

  isSectionEmpty(section){
    return _.find(this.questions, {sectionId: section.externalId})?false:true;
  }

  refreshQuestions() {
    this.sortQuestions();
    this.sortedQuestions = _.groupBy(this.questions, 'sectionId');
    this.refreshSections();
  }

  refreshSections() {
    this.sections = this.block.sections || [];
    this.sortedSections =_.sortBy(this.sections, 'displayOrder');
  }

  updateSections(){
    this.block.sections = this.sortedSections;
    this.refreshSections();
  }

  updateQuestions(){
    this.block.questions = this.questions
    this.refreshQuestions()
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
    let existingQuestion = question ? _.find(this.questions, {question: question.question}) : null;
    let modal = this.QuestionModal.show(question, this.questions, this.sections);
    modal.result.then((question) => {
      if (question) {
        if(existingQuestion) {
          this.questions[this.questions.indexOf(existingQuestion)] = question;
          this.block.questions[this.block.questions.indexOf(existingQuestion)] = question;
        } else {
          this.questions.push(question);
          this.block.questions.push(question);
        }

        this.refreshQuestions();
      } else {
        alert('Can\'t add to invalid template JSON');
      }

    });
  }

  showSectionModal(section) {
    let existingSection = section ? _.find(this.sections, {displayOrder: section.displayOrder}) : null;
    let modal = this.SectionModal.show(section, this.sections);
    modal.result.then((section) => {
      if (section) {
        if(existingSection) {
          var i = this.sections.indexOf(existingSection);
          this.block.sections[i] = section;
        } else {
          this.block.sections.push(section);
          this.moveQuestionsToInitialSection(this.block.sections.length, section.externalId)
        }

        this.refreshQuestions();
      } else {
        alert('Can\'t add to invalid template JSON');
      }
    });
  }

  moveQuestionsToInitialSection(sectionsLength, sectionExternalId) {
    if(sectionsLength === 1) {
      this.questions.forEach(q => {
        q.sectionId = sectionExternalId;
      });
    }
  }

  deleteQuestion(question) {
    let modal = this.ConfirmationDialog.delete('Are you sure you want to delete question ' + question.question.id + '?');
    modal.result.then(() => {
      _.remove(this.questions, question);
      _.remove(this.block.questions, question);
      this.refreshQuestions();

    });
  }
  deleteSection(section) {
    let modal = this.ConfirmationDialog.show({
      message: 'Are you sure you want to delete ' + section.text + ' section?',
      title: 'Delete Section',
      approveText: 'DELETE',
      dismissText: 'KEEP'
    });
    modal.result.then(() => {
      _.remove(this.sections, section);
      _.remove(this.block.sections, section);
      this.refreshSections();

    });
  }

}

TemplateBlockQuestions.$inject = ['$state', 'QuestionModal', 'SectionModal', 'ConfirmationDialog'];

gla.component('templateBlockQuestions', {
  templateUrl: 'scripts/components/template-block-questions/templateBlockQuestions.html',
  controller: TemplateBlockQuestions,
  bindings: {
    block: '<',
    readOnly: '<'
  },
});

