/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */


class FundingClaimsSummaryReport {
  constructor(ReportService, TemplateService, ProjectSkillsService) {
    this.ReportService = ReportService;
    this.TemplateService = TemplateService;
    this.ProjectSkillsService = ProjectSkillsService;

  }

  $onInit() {
    this.templateConfig = this.TemplateService.getBlockConfig(this.template, this.block);
    this.contractTypes = this.ProjectSkillsService.getActiveContractTypes(this.block, this.templateConfig);
    this.periodsMap = this.ProjectSkillsService.getPeriodIdToTextMap(this.templateConfig.periods);

    this.isAebProcured = this.ProjectSkillsService.isAebProcured(this.block);
    this.isAebGrant = this.ProjectSkillsService.isAebGrant(this.block);

    this.periodTotals = this.ProjectSkillsService.getPeriodTotals(this.block.totals, this.contractTypes);
  }
}

FundingClaimsSummaryReport.$inject = ['ReportService', 'TemplateService', 'ProjectSkillsService'];

angular.module('GLA')
  .component('fundingClaimsSummaryReport', {
    bindings: {
      block: '<',
      template: '<'
    },
    templateUrl: 'scripts/pages/summary-report/fundingClaimsSummaryReport.html',
    controller: FundingClaimsSummaryReport
  });
