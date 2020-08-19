/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

class OutputsCostSummaryReport {
  constructor() {
  }

  $onInit() {
  }

}

OutputsCostSummaryReport.$inject = [];

angular.module('GLA')
  .component('outputsCostSummaryReport', {
    bindings: {
      block: '<',
      project: '<',
      template: '<'
    },
    templateUrl: 'scripts/pages/summary-report/outputsCostSummaryReport.html',
    controller: OutputsCostSummaryReport
  });
