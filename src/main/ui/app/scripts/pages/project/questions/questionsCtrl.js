/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

import ProjectBlockCtrl from '../ProjectBlockCtrl';

class QuestionsCtrl extends ProjectBlockCtrl {
  constructor(ProjectService, FileUploadErrorModal, moment, project, $injector, ConfirmationDialog, template){
    super(project, $injector);
    this.ProjectService = ProjectService;
    this.moment = moment;
    this.FileUploadErrorModal = FileUploadErrorModal;
    this.ConfirmationDialog = ConfirmationDialog;
    this.template = template;

    this.uploadParams = {
      orgId: project.organisation.id
    };

    this.projectId = project.id;


    this.blockData = this.projectBlock;
    this.questions = this.blockData.questionEntities;

    this.sections = {};
      _.forEach(this.blockData.sections, section => {
        this.sections[section.externalId] = section;
      });

    this.questions = _
      .chain(this.questions)
      .sortBy('displayOrder')
      .map(item => {
        item.question.isOptional = (item.requirement === 'optional');
        item.question.isHidden = (item.requirement === 'hidden');
        item.question.parentAnswerToMatch = item.parentAnswerToMatch;
        item.question.parentId = item.parentId;


        // if(item.parentId && item.sectionId) {
        //   item.question.parentSectionId = _.find(this.questions, {question:{id:item.parentId}}).sectionId;
        // }



        if(item.sectionId){
          let section = this.sections[item.sectionId];
          if(!section.alreadyUsed) {
            item.question.isFirstInSection = true;
            section.alreadyUsed = true;
            item.question.section = section;
          }

        }


        if(item.question.parentId){
          if(item.sectionId){

            let parentSectionId = _.find(this.questions, {question:{id:item.parentId}}).sectionId;
            item.question.subQuestion = parentSectionId === item.sectionId;

          } else {
            item.question.subQuestion = true;
          }
        }
        return item.question;
      })
      .value();

    // sort and parse Dropdown options
    _.each(this.questions, question => {
      if (question.answerType.toLowerCase() === 'dropdown') {
        question.answerOptions = _
          .chain(question.answerOptions)
          .sortBy('displayOrder')
          .map(answer => {
            return answer.option
          })
          .value();

      }
    });

    // populate answers
    _.each(this.blockData.answers, answer => {
      _.each(this.questions, question => {
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
          }
        }

      });
    });

    this.isVisible = (question) => {
      if(question && question.isHidden){
        return false;
      }else if(this.ProjectService.hasParentCondition(question)){
        return this.ProjectService.isParentConditionMet(question, this.questions);
      }else{
        return true;
      }
    }
  }




  back() {
    if (this.readOnly) {
      this.returnToOverview();
    } else {
      this.onSaveData();
    }
  }

  /**
   * Form submit handler
   */
  onSaveData() {
    if (this.readOnly) {
      this.returnToOverview();
    } else {
      var answers = [];
      _.each(this.questions, question => {
        var answer = {};
        answer.questionId = question.id;
        if(this.ProjectService.hasParentCondition(question) && !this.ProjectService.isParentConditionMet(question, this.questions)){
          answer.answer = undefined;
          answer.numericAnswer = undefined;
          answer.IdattachmentIds = undefined;
          answer.IdanswerId = undefined;
        } else {
          if (question.answerType.toLowerCase() === 'dropdown' && !question.answer) {
            question.answer = '';
          }

        // if (question.answer || question.answer === '' || _.isNumber(question.numericAnswer)) {



          if (question.answerType === 'Number' && _.isNumber(question.numericAnswer)) {
            answer.numericAnswer = question.numericAnswer;
          }



          answer.answer = question.answer;


          // TODO: this should move to a util
          // if date... format to YYYY-MM-DD
          if (question.answerType.toLowerCase() === 'date') {
            if (answer.answer && !this.moment(answer.answer, 'YYYY-MM-DD', true).isValid()) {
              answer.answer = this.moment(answer.answer, 'DD/MM/YYYY', true).format('YYYY-MM-DD');
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

      this.blockData.answers = answers;
      // save
      this.ProjectService.updateProjectAnswers(this.project.id, this.blockId, this.blockData)
        .then(resp => {
          this.returnToOverview(this.blockId);
        }, (resp)=> {
          this.ConfirmationDialog.warn(
            _.map(resp.data.errors, (error)=>error.description).join(', ')
          );
        })
        .catch(this.$log.error);
    }
  }

}

QuestionsCtrl.$inject = ['ProjectService', 'FileUploadErrorModal', 'moment', 'project', '$injector', 'ConfirmationDialog', 'template'];

angular.module('GLA')
  .controller('QuestionsCtrl', QuestionsCtrl);
