/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

class AdditionalQuestionsChangeReport {
  constructor($rootScope, $scope, ReferenceDataService, OrganisationGroupService, ProjectService) {
    this.showingLeft = !!this.data.left;
    this.showingRight = !!this.data.right;
    this.ProjectService = ProjectService;
    let leftSize = 0;
    let leftQuestionsSorted;
    // let leftAnswersSorted;
    // if(this.data.left != null && this.data.left.questionEntities!= null) {
    //   leftSize = this.data.left.questionEntities.length;
    //   leftQuestionsSorted = this.data.left.questionEntities.sort(this.sortQuestions);
      // leftAnswersSorted = this.sortAnswers(this.data.left.answers, this.getQuestionDisplayOrderMap(leftQuestionsSorted));
    // } else {
    //   leftSize=0;
      // leftQuestionsSorted = null;
    // }


    // let rightSize;
    // let rightQuestionsSorted;
    // let rightAnswersSorted;
    // if(this.data.right != null && this.data.right.questionEntities!= null) {
    //   rightSize = this.data.right.questionEntities.length;
    //   rightQuestionsSorted = this.data.right.questionEntities.sort(this.sortQuestions);
      // rightAnswersSorted = this.sortAnswers(
      //   this.data.right.answers,
      //   this.getQuestionDisplayOrderMap(rightQuestionsSorted));
    // } else {
    //   rightSize=0;
      // rightQuestionsSorted = null
    // }


    // var commonSize;
    // var bigger;
    // if(rightSize < leftSize) {
    //   commonSize = rightSize;
    //   bigger = {isRight : false, questions: leftQuestionsSorted, answers : leftAnswersSorted};
    // } else if(rightSize > leftSize){
    //   commonSize = leftSize;
    //   bigger = {isRight : true, questions: rightQuestionsSorted, answers : rightAnswersSorted};
    // } else {
    //   commonSize = leftSize
    // }
    // var arrayData = [];
    // for(var i=0 ; i< commonSize ; i++) {
    //   // if(this.ProjectService.hasParentCondition(rightQuestionsSorted[i])){
    //   //   debugger;
    //   // }
    //
    //   const item = this.generateItem(
    //     rightQuestionsSorted[i],
    //     this.showingLeft ? leftAnswersSorted[i] : null,
    //     this.showingRight ? rightAnswersSorted[i] : null)
    //   arrayData.push(item);
    // }
    // if(bigger) {
    //   for(var bIndex=commonSize ; bIndex < bigger.questions.length ; bIndex++) {
    //     var leftAnswer;
    //     var rightAnswer;
    //     if(bigger.isRight) {
    //       leftAnswer = null;
    //       rightAnswer = bigger.answers[bIndex];
    //     } else {
    //       leftAnswer = bigger.answers[bIndex];
    //       rightAnswer = null;
    //     }
    //     let question = bigger.questions[bIndex];
    //
    //     if(this.ProjectService.hasParentCondition(question)){
    //       let parentQuestion = _.find(bigger.questions, {question:{id:question.parentId}});
    //       debugger;
    //
    //     }
    //
    //     const extraItem = this.generateItem(
    //       bigger.questions[bIndex],
    //       this.showingLeft ? leftAnswer : null,
    //       this.showingRight ? rightAnswer : null);
    //     arrayData.push(extraItem);
    //   }
    // }
    let arrayData = [];
    let unionQuestions = _.unionBy(
      this.data.left ? this.data.left.questionEntities : [],
      this.data.right ? this.data.right.questionEntities : [], (item) => {
        return item.question.id;
      }).sort(this.sortQuestions);

    _.forEach(unionQuestions, (item)=>{
      let leftAnswer = null;
      let rightAnswer = null;
      let hasParentCondition = this.ProjectService.hasParentCondition(item);
      let parentAnswer = null;

      let conditionsMet = {
        left: false,
        right: false,
      };

      if(this.showingLeft){
        conditionsMet.left = true;
        if(hasParentCondition) {
          parentAnswer = _.find(this.data.left.answers, {questionId: item.parentId});
          if(parentAnswer){
            conditionsMet.left = (parentAnswer.answer === item.parentAnswerToMatch);
          } else {
            conditionsMet.left = false;
          }
        }
        leftAnswer = _.find(this.data.left.answers, {questionId: item.question.id});
      }

      parentAnswer =  null;

      if(this.showingRight){
        conditionsMet.right = true;
        if(hasParentCondition) {
          parentAnswer = _.find(this.data.right.answers, {questionId: item.parentId});
          if(parentAnswer){
            conditionsMet.right = (parentAnswer.answer === item.parentAnswerToMatch);
          } else {
            conditionsMet.right = false;
          }
        }
        rightAnswer = conditionsMet.right && _.find(this.data.right.answers, {questionId: item.question.id});
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

    var self = this;
    this.questionFilter = (value, index, array) => {
      if(value && value.isHidden){
        return false;
      } else if(self.ProjectService.hasParentCondition(value)){
        return self.ProjectService.isParentConditionMet(value, array);
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
      map[obj.question.id] = obj.displayOrder;
      return map;
    }, {});
  }

  //Assumes both answers are the same type
  generateItem(sourceQuestion, leftAnswerData, rightAnswerData, conditionMet) {

    let leftAnswer;
    let rightAnswer;
    let isFileUploadQuestion = false;
    if(sourceQuestion.question.answerType === 'FileUpload'){
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
      isFreeText: sourceQuestion.question.answerType === 'FreeText',
      displayOrder: sourceQuestion.displayOrder,
      label : sourceQuestion.question.text,
      left : left,
      right: right, //rightAnswerData ? {answer : rightAnswer} : (this.showingRight ? {} : undefined),
      questionId: sourceQuestion.question.id,
      isFileUploadQuestion: isFileUploadQuestion,
      changes: this.data.changes,
      isHidden: sourceQuestion.requirement === 'hidden'
    };
  }
  sortQuestions(entity1, entity2) {
    return entity1.displayOrder - entity2.displayOrder;
  }
}



AdditionalQuestionsChangeReport.$inject = ['$rootScope', '$scope', 'ReferenceDataService', 'OrganisationGroupService', 'ProjectService'];

angular.module('GLA')
  .component('additionalQuestionsChangeReport', {
    bindings: {
      data: '<'
    },
    templateUrl: 'scripts/pages/change-report/blocks/additionalQuestionsChangeReport.html',
    controller: AdditionalQuestionsChangeReport
  }
);
