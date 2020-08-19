/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

class InternalQuestionsCtrl {
  constructor($state, project, block, ProjectBlockService, QuestionsService, UserService, $timeout) {
    this.$state = $state;
    this.project = project;
    this.block = block;
    this.ProjectBlockService = ProjectBlockService;
    this.QuestionsService = QuestionsService;
    this.UserService = UserService;
    this.$timeout = $timeout;
  }

  $onInit() {
    this.questions = this.QuestionsService.getQuestionsFromBlock(this.block);
    this.editable = this.UserService.hasPermission('proj.edit.internal.blocks');
    this.readOnly = true;
    this.showQuestions = true;
    this.title = _.startCase(this.block.blockDisplayName.toLowerCase());
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

}

InternalQuestionsCtrl.$inject = ['$state', 'project', 'block', 'ProjectBlockService', 'QuestionsService', 'UserService', '$timeout'];

angular.module('GLA')
  .controller('InternalQuestionsCtrl', InternalQuestionsCtrl);
