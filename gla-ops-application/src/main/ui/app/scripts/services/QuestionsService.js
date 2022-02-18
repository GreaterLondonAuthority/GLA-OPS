/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

QuestionsService.$inject = ['$http', 'config'];

const INTERNAL_QUESTION_BLOCK_TYPE = 'Questions';

function QuestionsService($http, config) {
  return {

    getQuestions(page, question, template) {
      let cfg = {
        params: {
          question: question,
          template: template,
          page: page,
          size: 50,
          sort: 'id,asc',
          enrich: true
        }
      };

      return $http.get(`${config.basePath}/questions`, cfg)
    },

    getQuestion(id) {
      return $http.get(`${config.basePath}/questions/${id}?enrich=true`)
    },

    getInternalQuestionTemplateConfig(template, displayOrder){
      return _.find(template.internalBlocks, {type: INTERNAL_QUESTION_BLOCK_TYPE, displayOrder: displayOrder});
    },

    saveQuestion(question, questionId) {
      if (questionId) {
        return $http.put(`${config.basePath}/questions/${questionId}`, question)
      } else {
        return $http.post(`${config.basePath}/questions`, question)
      }
    },

    isSectionConditionMet(question, questions) {
      if (question.section && question.section.parentId) {
        let parentQuestion = _.find(questions, {id: question.section.parentId});
        // recursion here as we might have multiple levels of conditional questions
        let parentAnswerMatch = false;
        if(parentQuestion.answer) {
          const parentAnswers = parentQuestion.answer.split(parentQuestion.delimiter);
          const childAnswersToMatch = question.section.parentAnswerToMatch.split(parentQuestion.delimiter);
          const intersectionOfAnswers = parentAnswers.filter(value => childAnswersToMatch.indexOf(value) !== -1);
          parentAnswerMatch = intersectionOfAnswers.length >= 1;
        }
        return this.isSectionConditionMet(parentQuestion, questions) &&
          this.isParentConditionMet(parentQuestion, questions) &&
          parentAnswerMatch;
      } else {
        return true;
      }
    },

    isParentConditionMet(question, questions) {
      let sectionConditionMet = this.isSectionConditionMet(question, questions);

      if (!question.parentId) {
        return sectionConditionMet;
      }

      let parentQuestion = _.find(questions, {id: question.parentId});
      // recursion here as we might have multiple levels of conditional questions
      let parentAnswerMatch = false;
      if(parentQuestion.answer) {
        const parentAnswers = parentQuestion.answer.split(parentQuestion.delimiter);
        const childAnswersToMatch = question.parentAnswerToMatch.split(parentQuestion.delimiter);
        const intersectionOfAnswers = parentAnswers.filter(value => childAnswersToMatch.indexOf(value) !== -1);
        parentAnswerMatch = intersectionOfAnswers.length >= 1;
      }
      return this.isParentConditionMet(parentQuestion, questions) && parentAnswerMatch;
    },

    isQuestionVisible(question, questions) {
      if (question && question.isHidden) {
        return false;
      } else {
        return this.isParentConditionMet(question, questions);
      }
    },

    getQuestionsFromBlock(blockData) {
      let questions = blockData.questions || [];
      let sections = {};

      _.forEach(blockData.sections, section => {
        sections[section.externalId] = section;
      });

      questions = _
        .chain(questions)
        .map(item => {
            if (item.sectionId) {
              let section = sections[item.sectionId];
              item.section = section;
              item.sectionDisplayOrder = (section.displayOrder * 10000) + item.displayOrder;
            } else {
              item.sectionDisplayOrder = item.displayOrder;
            }
            return item;
          }
        )
        .sortBy('sectionDisplayOrder')
        .map(item => {
          item.isOptional = (item.requirement === 'optional');
          item.isHidden = (item.requirement === 'hidden');
          item.helpText = item.helpText;
          item.parentAnswerToMatch = item.parentAnswerToMatch;
          item.parentId = item.parentId;

          if (item.parentId) {
            if (item.sectionId) {

              let parentSectionId = _.find(questions, {id: item.parentId}).sectionId;
              item.subQuestion = parentSectionId === item.sectionId;

            } else {
              item.subQuestion = true;
            }
          }
          return item;
        })
        .value();

      function getMultiSelectSelectedAnswers(allQuestionsAnswers, question){
        let answerObj = _.find(allQuestionsAnswers, {questionId: question.id});
        let answers = (answerObj && answerObj.answer)? answerObj.answer.split(question.delimiter) : [];
        return answers;
      }

      // sort and parse Dropdown options
      _.each(questions, question => {
        if (question.answerType.toLowerCase() === 'dropdown') {
          if (question.maxAnswers > 1) {
            if (question.answer == '') {
              question.answer = null;
            }
            question.answerOptions = _
              .chain(question.answerOptions)
              .sortBy('displayOrder')
              .map(answer => {
                return {
                  label: answer.option,
                  model: getMultiSelectSelectedAnswers(blockData.answers, question).indexOf(answer.option) !== -1
                }
              })
              .value();
          } else {
            question.answerOptions = _
              .chain(question.answerOptions)
              .sortBy('displayOrder')
              .map(answer => {
                return answer.option
              })
              .value();
          }

        }
      });

      // populate answers
      _.each(blockData.answers, answer => {
        _.each(questions, question => {
          if (question.id === answer.questionId) {
            if (_.isNumber(answer.numericAnswer)) {
              question.numericAnswer = answer.numericAnswer;
            } else {
              question.answer = answer.answer;
            }
            if (question.answerType === 'FileUpload') {
              question.attachments = [];
              question.answerId = answer.id;
              question.fileAttachments = answer.fileAttachments;
              question.totalAttachmentsSize = answer.totalAttachmentsSize || 0;
            }
          }
        });
      });

      this.updateSectionVisibility(questions);
      return questions;
    },

    updateSectionVisibility(questions){
      questions = questions || [];
      questions.forEach(q => {
        if(q.section){
          q.section.alreadyUsed = false;
        }
      });

      questions.forEach(q => {
        q.isFirstInSection = false;
        if (q.sectionId) {
          if (!q.section.alreadyUsed && this.isQuestionVisible(q, questions)) {
            q.isFirstInSection = true;
            q.section.alreadyUsed = true;
          }
        }
      });
    },

    /**
     * Extracts answers from questions
     * @param questions array returned by this.getQuestionsFromBlock(block)
     * @returns {*}
     */
    getAnswers(questions) {
      var answers = [];
      _.each(questions, question => {
        var answer = {};
        answer.questionId = question.id;
        if (!this.isParentConditionMet(question, questions)) {
          answer.answer = undefined;
          answer.numericAnswer = undefined;
          answer.IdattachmentIds = undefined;
          answer.IdanswerId = undefined;
        } else {
          if (question.answerType.toLowerCase() === 'dropdown' && !question.answer) {
            question.answer = null;
          }

          // if (question.answer || question.answer === '' || _.isNumber(question.numericAnswer)) {

          if (question.answerType === 'Number' && _.isNumber(question.numericAnswer)) {
            answer.numericAnswer = question.numericAnswer;
          }

          answer.answer = question.answer;
          // TODO: this should move to a util
          // if date... format to YYYY-MM-DD
          if (question.answerType.toLowerCase() === 'date') {
            if (answer.answer && !moment(answer.answer, 'YYYY-MM-DD', true).isValid()) {
              answer.answer = moment(answer.answer, 'DD/MM/YYYY', true).format('YYYY-MM-DD');
            }
          }
          if (question.answerType === 'FileUpload') {
            answer.attachmentIds = [];
            answer.answerId = question.answerId;
            _.forEach(question.fileAttachments, (fileAttachment) => {
              answer.attachmentIds.push(fileAttachment.id);
            });
            _.forEach(question.attachments, (attachement) => {
              answer.attachmentIds.push(attachement.fileId);
            });
          }
        }
        answers.push(answer);

      });
      return answers;
    },

    getQuestionUpLevel(inQuestionId, questions) {
      let returnLevel = 0
      questions.forEach(question => {
        if (question.question.id == inQuestionId) {
          if (question.parentId) {
            returnLevel = 1 + this.getQuestionUpLevel(question.parentId, questions)
          } else {
            returnLevel = 1;
          }
        }
      });
      return returnLevel
    },

    getQuestionDownLevel(inQuestionId, questions) {
      let returnLevel = 0
      questions.forEach(question => {
        if (question.parentId == inQuestionId) {
          returnLevel = 1 + this.getQuestionDownLevel(question.question.id, questions)
        }
      });
      return returnLevel
    }
  };
}

angular.module('GLA')
  .service('QuestionsService', QuestionsService);
