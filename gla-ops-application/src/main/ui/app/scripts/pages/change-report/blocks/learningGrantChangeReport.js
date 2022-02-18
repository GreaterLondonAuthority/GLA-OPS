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
    this.ofWhich = {collapsed: false};
    this.isAebProcured = this.ProjectSkillsService.isAebProcured(block);
    this.isAebGrant = this.ProjectSkillsService.isAebGrant(block);
    [this.data.left, this.data.right].forEach(block => {
      if(block) {
        block.academicYear = DateUtil.toFinancialYearString(block.startYear);
        let startYearAllocation = _.find(block.allocations || [], {year: block.startYear});
        block.allocation = startYearAllocation.allocation;
        block.communityAllocation = startYearAllocation.communityAllocation;
        block.learnerSupportAllocation = startYearAllocation.learnerSupportAllocation;

        let deliveryAllocation = _.find(block.allocations || [], {year: block.startYear, type: 'Delivery'}) || {};
        block.allocation = deliveryAllocation.allocation;
        let communityAllocation = _.find(block.allocations || [], {year: block.startYear, type: 'Community'}) || {};
        block.communityAllocation = communityAllocation.allocation;
        let innovationFund = _.find(block.allocations || [], {year: block.startYear, type: 'InnovationFund'}) || {};
        block.innovationFundAllocation = innovationFund.allocation;
        let responseFundStrand1 = _.find(block.allocations || [], {year: block.startYear, type: 'ResponseFundStrand1'}) || {};
        block.responseFundStrand1Allocation = responseFundStrand1.allocation;
        if (this.isAebProcured) {
          let learnerSupportAllocation = _.find(block.allocations || [], {year: block.startYear, type: 'LearnerSupport'}) || {};
          block.learnerSupportAllocation = learnerSupportAllocation.allocation;
        }
      }
    });
    let templateConfig = this.TemplateService.getBlockConfig(this.data.context.template, block);
    this.labels = this.ProjectSkillsService.getLabels(templateConfig);
    this.labels = this.ProjectSkillsService.getLabels(templateConfig);
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
