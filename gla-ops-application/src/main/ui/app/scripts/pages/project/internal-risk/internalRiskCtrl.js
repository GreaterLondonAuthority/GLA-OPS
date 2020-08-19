/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

import DataUtil from '../../../util/DateUtil';

class InternalRiskCtrl {
  constructor($state, project, block, comments, template, ProjectBlockService, RisksService, CommentsService, UserService) {
    this.$state = $state;
    this.project = project;
    this.block = block;
    this.comments = comments;
    this.template = template;
    this.ProjectBlockService = ProjectBlockService;
    this.RisksService = RisksService;
    this.CommentsService = CommentsService;
    this.UserService = UserService;
  }

  $onInit() {
    this.editable = this.UserService.hasPermission('proj.edit.internal.blocks');
    this.readOnly = true;
    this.config = this.RisksService.getInternalRiskTemplateConfig(this.template);
    this.ratingList = this.config.ratingList;
    this.block.rating = _.find(this.ratingList, {id: (this.block.rating || {}).id}) || null;
    this.title = _.startCase(this.block.blockDisplayName.toLowerCase());
    this.noRatingLabel = this.RisksService.getInternalRiskNoRatingLabel();
    this.currentFinancialYear = this.project.programme.financialYear;
    let valueToUse = this.currentFinancialYear ? this.currentFinancialYear : DataUtil.getFinancialYear2(moment());
    this.financialYearLabel = DataUtil.toFinancialYearString(valueToUse);
    this.currentRiskAdjustedFigures = _.find(this.block.riskAdjustedFiguresList, {financialYear: valueToUse});
    if (!this.currentRiskAdjustedFigures) {
       this.currentRiskAdjustedFigures = {
         financialYear: valueToUse,
         starts: 0,
         completions: 0,
         grantSpend: 0,
       };

      this.block.riskAdjustedFiguresList.push(this.currentRiskAdjustedFigures);
    }
  }

  back() {
    this.$state.go('project-overview', {projectId: this.project.id}, {reload: true});
  }

  edit() {
    this.readOnly = false;
  }



  // save() {
  stopEditing() {
    this.ProjectBlockService.updateInternalBlock(this.project.id, this.block).then(() => {
      // this.back();
      this.readOnly = true;
    })
  }

  saveComment(comment) {
    this.CommentsService.saveInternalRiskComments(this.project.id, this.block.id, comment).then(() => {
      this.CommentsService.getInternalRiskComments(this.block.id).then(rsp => {
        this.comments = rsp.data.content;
      })
    });
  }

}

InternalRiskCtrl.$inject = ['$state', 'project', 'block', 'comments', 'template', 'ProjectBlockService', 'RisksService', 'CommentsService', 'UserService'];

angular.module('GLA')
  .controller('InternalRiskCtrl', InternalRiskCtrl);
