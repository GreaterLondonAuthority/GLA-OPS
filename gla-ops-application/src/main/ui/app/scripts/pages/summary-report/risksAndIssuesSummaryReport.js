/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */


class RisksAndIssuesSummaryReport {
  constructor(RisksService, FeatureToggleService) {
    this.overallRatings = RisksService.getOverallRatings();
    this.RisksService = RisksService;
    this.FeatureToggleService = FeatureToggleService;
  }


  $onInit() {
    this.blockData = this.data;
    // TODO is there a way to move this to routes?
    this.FeatureToggleService.isFeatureEnabled('ProjectRiskAndIssues').subscribe(resp => {
      this.showRisksAndIssues = resp;
    });
    this.hasRisks = false;
    this.hasIssues = false;
    this.overallRating = _.find(this.overallRatings, {id: this.blockData.rating});
    this.risks = this.RisksService.getRisks(this.blockData);
    this.issues = this.RisksService.getIssues(this.blockData);

    this.hasRisks = !!_.filter(this.risks, risk => risk.status !== 'Closed').length;
    this.hasIssues = !!_.filter(this.issues, issue => issue.status !== 'Closed').length;
  }
}

RisksAndIssuesSummaryReport.$inject = ['RisksService', 'FeatureToggleService'];

angular.module('GLA')
  .component('risksAndIssuesSummaryReport', {
    bindings: {
      data: '<',
    },
    templateUrl: 'scripts/pages/summary-report/risksAndIssuesSummaryReport.html',
    controller: RisksAndIssuesSummaryReport
  });
