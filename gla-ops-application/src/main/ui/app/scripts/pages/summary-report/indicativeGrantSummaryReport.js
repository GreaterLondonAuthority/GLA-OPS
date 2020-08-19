/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */


class IndicativeGrantSummaryReport {
  constructor(GrantService) {
    this.GrantService = GrantService
  }


  $onInit() {
    this.data = this.GrantService.enhanceIndicativeBlock(this.block);
  }
}

IndicativeGrantSummaryReport.$inject = ['GrantService'];

angular.module('GLA')
  .component('indicativeGrantSummaryReport', {
    bindings: {
      block: '<',
    },
    templateUrl: 'scripts/pages/summary-report/indicativeGrantSummaryReport.html',
    controller: IndicativeGrantSummaryReport
  });
