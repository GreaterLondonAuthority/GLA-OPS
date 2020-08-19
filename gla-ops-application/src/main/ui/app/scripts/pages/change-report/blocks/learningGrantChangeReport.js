/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

import DateUtil from '../../../util/DateUtil';

class LearningGrantChangeReport {
  constructor(ProjectSkillsService, TemplateService) {
    this.ProjectSkillsService = ProjectSkillsService;
    this.TemplateService = TemplateService;

  }

  $onInit(){
    let block = this.data.left || this.data.right;
    [this.data.left, this.data.right].forEach(block => {
      if(block) {
        block.academicYear = DateUtil.toFinancialYearString(block.startYear);
        let startYearAllocation = _.find(block.allocations || [], {year: block.startYear});
        block.allocation = startYearAllocation.allocation;
        block.communityAllocation = startYearAllocation.communityAllocation;
        block.learnerSupportAllocation = startYearAllocation.learnerSupportAllocation;
      }
    });
    let templateConfig = this.TemplateService.getBlockConfig(this.data.context.template, block);
    this.labels = this.ProjectSkillsService.getLabels(templateConfig);
    this.isAebProcured = this.ProjectSkillsService.isAebProcured(block);
    this.isAebGrant = this.ProjectSkillsService.isAebGrant(block);
    this.labels = this.ProjectSkillsService.getLabels(templateConfig);

    console.log('labels', this.labels)
    console.log('data', this.data);
  }
}

LearningGrantChangeReport.$inject = ['ProjectSkillsService', 'TemplateService'];

angular.module('GLA')
  .component('learningGrantChangeReport', {
    bindings: {
      data: '<'
    },
    templateUrl: 'scripts/pages/change-report/blocks/learningGrantChangeReport.html',
    controller: LearningGrantChangeReport  });
