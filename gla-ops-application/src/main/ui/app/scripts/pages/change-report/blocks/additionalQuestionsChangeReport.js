/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

class AdditionalQuestionsChangeReport {
  constructor(QuestionsService) {
    this.QuestionsService = QuestionsService;
  }

  $onInit(){
    this.showingLeft = !!this.data.left;
    this.showingRight = !!this.data.right;

    let arrayData = [];
    let unionQuestions = _.unionBy(
      this.data.left ? this.data.left.questions : [],
      this.data.right ? this.data.right.questions : [],
      (item) => item.id
    ).sort(this.sortQuestions);


    _.forEach(unionQuestions, (item)=>{
      let leftAnswer = null;
      let rightAnswer = null;
      let hasParentCondition = this.QuestionsService.hasParentCondition(item);
      let parentAnswer = null;

      let conditionsMet = {
        left: false,
        right: false,
      };

      if(this.showingLeft && this.questionExist(this.data.left.questions, item)){
        conditionsMet.left = true;
        if(hasParentCondition) {
          parentAnswer = _.find(this.data.left.answers, {questionId: item.parentId});
          if(parentAnswer){
            conditionsMet.left = (parentAnswer.answer === item.parentAnswerToMatch);
          } else {
            conditionsMet.left = false;
          }
        }
        leftAnswer = _.find(this.data.left.answers, {questionId: item.id});
      }

      parentAnswer =  null;

      if(this.showingRight && this.questionExist(this.data.right.questions, item)){
        conditionsMet.right = true;
        if(hasParentCondition) {
          parentAnswer = _.find(this.data.right.answers, {questionId: item.parentId});
          if(parentAnswer){
            conditionsMet.right = (parentAnswer.answer === item.parentAnswerToMatch);
          } else {
            conditionsMet.right = false;
          }
        }
        rightAnswer = conditionsMet.right && _.find(this.data.right.answers, {questionId: item.id});
      }

      if(conditionsMet.left || conditionsMet.right){
        arrayData.push(this.generateItem(
          item,
          leftAnswer,
          rightAnswer,
          conditionsMet
        ));
      }

    });

    this.questions = arrayData;

    let self = this;
    this.questionFilter = (value, index, array) => {
      if(value && value.isHidden){
        return false;
      } else if(self.QuestionsService.hasParentCondition(value)){
        return self.QuestionsService.isParentConditionMet(value, array);
      }else{
        return true;
      }
    }
  }

  sortAnswers(answers, qMap) {
    return answers.sort(function(a,b) {
      const aDisplayOrder = qMap[a.questionId]
      const bDisplayOrder = qMap[b.questionId]
      return aDisplayOrder - bDisplayOrder
    });
  }

  getQuestionDisplayOrderMap(arr) {
    return arr.reduce(function(map, obj) {
      map[obj.id] = obj.displayOrder;
      return map;
    }, {});
  }

  //Assumes both answers are the same type
  generateItem(sourceQuestion, leftAnswerData, rightAnswerData, conditionMet) {

    let leftAnswer;
    let rightAnswer;
    let isFileUploadQuestion = false;
    if(sourceQuestion.answerType === 'FileUpload'){
      isFileUploadQuestion = true;
    }

    if(leftAnswerData) {
      if(isFileUploadQuestion) {
        leftAnswer = leftAnswerData.fileAttachments;
      } else if(leftAnswerData.answer) {
        leftAnswer = leftAnswerData.answer
      } else {
        leftAnswer = leftAnswerData.numericAnswer
      }
    }

    if(rightAnswerData) {
      if(isFileUploadQuestion) {
        rightAnswer = rightAnswerData.fileAttachments;
      } else if(rightAnswerData.answer) {
        rightAnswer = rightAnswerData.answer
      } else {
        rightAnswer = rightAnswerData.numericAnswer
      }
    }

    let left, right;

    if(conditionMet.left){
      if(leftAnswerData) {
        left = {answer : leftAnswer};
      } else if(this.showingLeft) {
        left = {};
      }
    }

    if(conditionMet.right){
      if(rightAnswerData) {
        right = {answer : rightAnswer};
      } else if(this.showingRight) {
        right = {};
      }
    }

    return {
      className: 'question'+sourceQuestion.displayOrder,
      isFreeText: sourceQuestion.answerType === 'FreeText',
      displayOrder: sourceQuestion.displayOrder,
      label : sourceQuestion.text,
      left : left,
      right: right, //rightAnswerData ? {answer : rightAnswer} : (this.showingRight ? {} : undefined),
      questionId: sourceQuestion.id,
      isFileUploadQuestion: isFileUploadQuestion,
      changes: this.data.changes,
      isHidden: sourceQuestion.requirement === 'hidden'
    };
  }
  sortQuestions(entity1, entity2) {
    return entity1.displayOrder - entity2.displayOrder;
  }

  questionExist(questions, question){
    return !!_.find(questions, {id: (question || {}).id});
  }
}



AdditionalQuestionsChangeReport.$inject = ['QuestionsService'];

angular.module('GLA')
  .component('additionalQuestionsChangeReport', {
    bindings: {
      data: '<'
    },
    templateUrl: 'scripts/pages/change-report/blocks/additionalQuestionsChangeReport.html',
    controller: AdditionalQuestionsChangeReport
  }
);
