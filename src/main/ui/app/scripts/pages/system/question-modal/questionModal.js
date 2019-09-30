/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */


function QuestionModal($uibModal, QuestionsService) {

  return {
    show: function (question, questions) {
      return $uibModal.open({
        bindToController: true,
        controllerAs: '$ctrl',
        animation: false,
        templateUrl: 'scripts/pages/system/question-modal/questionModal.html',
        size: 'md',
        resolve: {
        },
        controller: ['$uibModalInstance', function ($uibModalInstance) {
          this.requirements = [
            'optional',
            'mandatory',
            'hidden'
          ];
          this.question = angular.copy(question || {});
          this.question.displayOrder = ((_.maxBy(questions, 'displayOrder') || {}).displayOrder || 0) + 1,

          this.validateQuestion = () => {
            this.isExistingQuestion = _.some(questions, (question) => {
              return question.question.id === this.question.question.id;
            });
          },
          this.validateDisplayOrder = (displayOrder) => {
            this.isDuplicateDisplayOrder = _.some(questions, (question) =>{
                return question.displayOrder === displayOrder;
            });
          },
          this.onQuestionIdChange = (questionId) => {
              if(questionId) {
                this.loading = true;
                QuestionsService.getQuestion(questionId).then(rsp => {
                  this.question.question = rsp.data;

                  this.validateQuestion();
                  this.questionExists = true;
                }).catch(err => {
                  this.questionExists = false;
                }).finally(()=>{
                  this.loading = false;
                });
              }
              this.questionExists = false;
          },
          this.addQuestion = () => {
              this.$close(this.question)
          }
        }]
      });
    },

  };
}

QuestionModal.$inject = ['$uibModal', 'QuestionsService'];

angular.module('GLA')
  .service('QuestionModal', QuestionModal);
