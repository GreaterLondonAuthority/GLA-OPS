/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

import CollapsibleReportCtr from '../../controllers/collapsibleReportCtrl.js';


class SummaryReportsPageCtrl extends CollapsibleReportCtr{
  constructor($injector, OutputsService, GlaReportService) {
    super($injector);
    this.OutputsService = OutputsService;
    this.GlaReportService = GlaReportService;
  }

  $onInit() {
    this.ReportService.setFullWidth();
    this.GlaReportService.setFullWidth();
    this.showApprovedOnly = !this.$stateParams.showUnapproved;
    this.blocksToCompare = this.ReportService.getBlocksToCompareForSummaryReport(this.project, this.template, this.currentFinancialYear);

    let supportedBlockType = [
      'ProjectDetailsBlock',
      'DesignStandardsBlock',
      'ProjectQuestionsBlock',
      'OutputsBlock',
      'ProjectRisksBlock',
      'ProjectBudgetsBlock',
      'ProjectMilestonesBlock',
      'CalculateGrantBlock',
      'DeveloperLedGrantBlock',
      'NegotiatedGrantBlock',
      'IndicativeGrantBlock',
      'GrantSourceBlock',
      'FundingBlock',
      'LearningGrantBlock',
      'FundingClaimsBlock',
      'OutputsCostsBlock',
      'DeliveryPartnersBlock',
      'AffordableHomesBlock'
    ];

    this.blocksToCompare = this.blocksToCompare.filter(block => supportedBlockType.indexOf(block.type) !== -1);

    this.outputSummaries = this.OutputsService.outputSummaries(this.projectBlock);

    this.latestComment = undefined;
    _.forEach(this.projectHistory, (item) => {
      this.latestComment = this.latestComment || (item.comments ? item : undefined);
    });
    if (this.latestComment) {
      this.transitionMap = this.ProjectService.getTransitionMap();
      this.blocksToCompare.push({
        type: 'ProjectHistory',
        blockDisplayName: 'Project History',
        blockDisplayCls: 'project-history-block',
        id: 'projecthistoryId',
        expanded: true,
        latestComment: this.latestComment
      });
    }
  }


  onBack() {
    this.$state.go('project-overview', {
      projectId: this.project.id,
    }, {
      reload: true
    });
  }

  onShowApprovedOnlyChange() {
    this.$stateParams.showUnapproved = !this.showApprovedOnly;
    this.$state.go(this.$state.current, this.$stateParams, {reload: true});
  }
}

SummaryReportsPageCtrl.$inject = ['$injector', 'OutputsService', 'GlaReportService'];


angular.module('GLA')
  .component('summaryReportPage', {
    templateUrl: 'scripts/pages/summary-report/summaryReportPage.html',
    bindings: {
      project: '<',
      template: '<',
      currentFinancialYear: '<',
      projectHistory: '<'
    },
    controller: SummaryReportsPageCtrl
  });
