/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

import DateUtil from '../../util/DateUtil';

const EntryType = {
  DELIVERY: 'DELIVERY',
  SUPPORT: 'SUPPORT'
};

class LearningGrantSummaryReport {
  constructor(ReportService, TemplateService, ProjectSkillsService) {
    this.ReportService = ReportService;
    this.TemplateService = TemplateService;
    this.ProjectSkillsService = ProjectSkillsService;
  }

  $onInit() {
    let templateConfig = this.TemplateService.getBlockConfig(this.template, this.block);
    this.labels = this.ProjectSkillsService.getLabels(templateConfig);
    this.isAebProcured = this.ProjectSkillsService.isAebProcured(this.block);

    this.block.academicYear = DateUtil.toFinancialYearString(this.block.startYear);

    this.currentAllocation = _.find(this.block.allocations || [], {year: this.block.startYear});
    this.deliveryEntries = _.filter(this.block.learningGrantEntries, {type: EntryType.DELIVERY, academicYear: this.block.startYear});
    this.supportEntries = _.filter(this.block.learningGrantEntries, (s) => s.type === EntryType.SUPPORT && !!s.percentage && s.academicYear === this.block.startYear);
  }
}

LearningGrantSummaryReport.$inject = ['ReportService', 'TemplateService', 'ProjectSkillsService'];

angular.module('GLA')
  .component('learningGrantSummaryReport', {
    bindings: {
      block: '<',
      template: '<'
    },
    templateUrl: 'scripts/pages/summary-report/learningGrantSummaryReport.html',
    controller: LearningGrantSummaryReport
  });
