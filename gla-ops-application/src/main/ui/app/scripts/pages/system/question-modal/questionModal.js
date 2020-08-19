/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */


function QuestionModal($uibModal, QuestionsService) {

  return {
    show: function (question, questions, sections) {
      return $uibModal.open({
        bindToController: true,
        controllerAs: '$ctrl',
        animation: false,
        templateUrl: 'scripts/pages/system/question-modal/questionModal.html',
        size: 'md',
        resolve: {},
        controller: ['$uibModalInstance', function ($uibModalInstance) {
          this.sections = sections;
          this.requirements = [
            'optional',
            'mandatory',
            'hidden'
          ];
          this.originalQuestion = question;
          this.question = angular.copy(question || {});

          // If question already exists, update otherwise add
          if (this.question.question) {
            this.btnName = 'Update'
            this.questionExists = true;
            this.loading = false;
            this.isDuplicateDisplayOrder = false;
            this.isIdEditable = false;
            if (this.question.parentId) {
              this.parentQuestionValid = true;
              this.parentQuestionExists = true;
              this.isExistingParentQuestion = true;
              this.isParentQuestionLevelValid=true;
              this.isParentQuestionDifferent=true;

              if (this.question.parentAnswerToMatch === 'yes' || this.question.parentAnswerToMatch === 'no') {
                this.parentYesNoAnswerExists = true;
              } else {
                this.parentDropdownAnswerExists = true;
              }
            }
            if (this.question.sectionId) {
              this.selectedSection = (_.find(this.sections, {externalId: this.question.sectionId}));
            }
          } else {
            this.btnName = 'Add'
            this.isIdEditable = true;
            this.question.displayOrder = ((_.maxBy(questions, 'displayOrder') || {}).displayOrder || 0) + 1;
            this.parentQuestion = {};
          }

          this.questionsCount = questions.length;
          this.sectionsCount = sections.length;

          this.onSectionSelect = (section) => {
            this.selectedSection = section;
            this.question.sectionId = section ? section.externalId : this.sections[0].externalId;
            let sectionQuestions = questions.filter(q => q.sectionId === this.question.sectionId);
            this.question.displayOrder = ((_.maxBy(sectionQuestions, 'displayOrder') || {}).displayOrder || 0) + 1;
          };

          // If one section exists, all questions should belong to that section
          if (this.sectionsCount === 1) {
            this.onSectionSelect(sections[0]);
          }

          function getQuestionUpLevel(inQuestionId) {
            let returnLevel = 0
            questions.forEach(question => {
              if (question.question.id == inQuestionId) {
                if (question.parentId) {
                  returnLevel = 1 + getQuestionUpLevel(question.parentId)
                } else {
                  returnLevel = 1;
                }
              }
            });
            return returnLevel
          }
          function getQuestionDownLevel(inQuestionId) {
            let returnLevel = 0
            questions.forEach(question => {
              if (question.parentId == inQuestionId) {
                  returnLevel = 1 + getQuestionDownLevel(question.question.id)
              }
            });
            return returnLevel
          }

          this.validateParentQuestionLevel = (inQuestion) => {
            return getQuestionUpLevel(inQuestion.parentId) + getQuestionDownLevel(inQuestion.question.id) < 4
          },
            this.validateExistingQuestion = (inQuestion) => {
              return _.some(questions, (question) => {
                return question.question.id === inQuestion.id;
              });
            },
            this.validateDisplayOrder = (displayOrder) => {
              if (this.question.sectionId) {
                this.validateDisplayOrderInSection(displayOrder, this.question.sectionId)
              } else {
                this.isDuplicateDisplayOrder = _.some(questions, (question) => {
                  return question.displayOrder === displayOrder && question != this.originalQuestion;
                });
              }
            },
            this.validateDisplayOrderInSection = (displayOrder, sectionId) => {
              let sectionQuestions = questions.filter(q => q.sectionId === sectionId);
              this.isDuplicateDisplayOrderInSection = _.some(sectionQuestions, (question) => {
                return question.displayOrder === displayOrder && question != this.originalQuestion;
              });
            },
            this.onQuestionIdChange = (questionId) => {
              if (questionId) {
                this.loading = true;
                QuestionsService.getQuestion(questionId).then(rsp => {
                  this.question.question = rsp.data;
                  this.isExistingQuestion = this.validateExistingQuestion(this.question.question);
                  this.questionExists = true;

                }).catch(err => {
                  this.questionExists = false;
                }).finally(() => {
                  this.loading = false;
                });
              }
              this.questionExists = false;
            },
            this.onParentQuestionIdChange = (question) => {
              this.parentQuestionValid = false;
              this.parentQuestionExists = false;
              this.parentYesNoAnswerExists = false;
              this.parentDropdownAnswerExists = false;
              this.question.parentAnswerToMatch = undefined;
              this.isParentQuestionLevelValid = false;
              if (question.parentId) {
                this.loading = true;
                QuestionsService.getQuestion(question.parentId).then(rsp => {
                  this.parentQuestion = rsp.data;
                  this.isParentQuestionDifferent= question.question.id != question.parentId
                  this.isExistingParentQuestion = this.validateExistingQuestion(this.parentQuestion);
                  this.isParentQuestionLevelValid = this.validateParentQuestionLevel(question)
                  this.parentYesNoAnswerExists = this.parentQuestion.answerType == 'YesNo'
                  this.parentDropdownAnswerExists = this.parentQuestion.answerType == 'Dropdown'
                  this.parentQuestionValid = this.isExistingParentQuestion && this.isParentQuestionLevelValid
                                             && this.isParentQuestionDifferent
                                             && (this.parentYesNoAnswerExists || this.parentDropdownAnswerExists)

                  this.parentQuestionExists = true;
                }).catch(err => {
                  this.parentQuestionExists = false;
                }).finally(() => {
                  this.loading = false;
                });
              }
            },
            this.addButtonEnabled = () => {
              return this.question.question && this.question.question.id && this.question.question.text && this.question.displayOrder
                && this.question.requirement && !this.isExistingQuestion && !this.isDuplicateDisplayOrder
                && (!this.question.parentId || (this.parentQuestionValid && this.question.parentAnswerToMatch))
                && (!this.question.sectionId || (this.question.sectionId && !this.isDuplicateDisplayOrderInSection))
            },
            this.addQuestion = () => {
              this.$close(this.question)
            },
            this.onMultiSelectChange = (check, question) => {
              let sortedAnswers = _.sortBy(question.answerOptions, 'displayOrder')
              let answers = (sortedAnswers || []).filter(ao => !!ao.model);
              this.question.parentAnswerToMatch = answers.map(ao => ao.option).join(question.delimiter)
            }
        }]
      });
    },
  };
}

QuestionModal.$inject = ['$uibModal', 'QuestionsService'];

angular.module('GLA')
  .service('QuestionModal', QuestionModal);
