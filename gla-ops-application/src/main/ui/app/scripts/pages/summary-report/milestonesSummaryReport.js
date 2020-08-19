/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */


class MilestonesSummaryReport {
  constructor(ReportService) {
    this.ReportService = ReportService;
  }


  $onInit() {

    let milestonesConfig = _.find(this.template.blocksEnabled, {block: 'Milestones'});
    this.processingRoutes = milestonesConfig.processingRoutes;
    this.processingRouteId = this.block.processingRouteId;

    if(!this.processingRouteId && milestonesConfig.defaultProcessingRoute){
      this.processingRouteId = milestonesConfig.defaultProcessingRoute.id;
    }

    if (this.processingRouteId) {
      this.processingRoute = _.find(milestonesConfig.processingRoutes, {
        id: this.processingRouteId
      });
    }

    this.evidenceDefaults = {
      textForAdded: 'Added'
    };
  }
}

MilestonesSummaryReport.$inject = ['ReportService'];

angular.module('GLA')
  .component('milestonesSummaryReport', {
    bindings: {
      block: '<',
      project: '<',
      template: '<'
    },
    templateUrl: 'scripts/pages/summary-report/milestonesSummaryReport.html',
    controller: MilestonesSummaryReport
  });
