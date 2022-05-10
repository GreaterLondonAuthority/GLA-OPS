/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

class InternalQuestionsCtrl {
  constructor($state, project, template, block, comments, ProjectBlockService, QuestionsService, UserService, CommentsService, $timeout) {
    this.$state = $state;
    this.project = project;
    this.template = template;
    this.block = block;
    this.comments = comments;
    this.ProjectBlockService = ProjectBlockService;
    this.QuestionsService = QuestionsService;
    this.UserService = UserService;
    this.CommentsService = CommentsService;
    this.$timeout = $timeout;
  }

  $onInit() {
    this.questions = this.QuestionsService.getQuestionsFromBlock(this.block);
    this.config = this.QuestionsService.getInternalQuestionTemplateConfig(this.template, this.block.displayOrder);
    this.editable = this.UserService.hasPermission('proj.edit.internal.blocks');
    this.readOnly = true;
    this.showQuestions = true;
    this.title = _.startCase(this.block.blockDisplayName.toLowerCase());
    this.infoMessage = this.block.infoMessage
  }

  back() {
    this.$state.go('project-overview', {projectId: this.project.id}, {reload: true});
  }

  edit() {
    this.readOnly = false;
    this.refreshQuestions();
  }

  stopEditing() {
    this.block.answers = this.QuestionsService.getAnswers(this.questions);
    this.ProjectBlockService.updateInternalBlock(this.project.id, this.block).then(() => {
      this.readOnly = true;
      this.refreshQuestions();
    })
  }

  refreshQuestions(){
    this.showQuestions = false;
    this.$timeout(()=>{
      this.showQuestions = true;
    });
  }

  saveComment(comment) {
    const INTERNAL_QUESTION_BLOCK_TYPE  = 'internalQuestionBlock';
    this.CommentsService.saveInternalComments(this.project.id, this.block.id,INTERNAL_QUESTION_BLOCK_TYPE, comment).then(() => {
      this.CommentsService.getInternalComments(this.block.id, INTERNAL_QUESTION_BLOCK_TYPE).then(rsp => {
        this.comments = rsp.data.content;
      })
    });
  }

}

InternalQuestionsCtrl.$inject = ['$state', 'project', 'template', 'block', 'comments','ProjectBlockService', 'QuestionsService', 'UserService', 'CommentsService', '$timeout'];

angular.module('GLA')
  .controller('InternalQuestionsCtrl', InternalQuestionsCtrl);
