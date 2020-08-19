/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */


class CalculateGrantSummaryReport {
  constructor(GrantService) {
    this.GrantService = GrantService
  }


  $onInit() {
    this.data = this.GrantService.sortTenureTypes(this.block);
  }
}

CalculateGrantSummaryReport.$inject = ['GrantService'];

angular.module('GLA')
  .component('calculateGrantSummaryReport', {
    bindings: {
      block: '<',
    },
    templateUrl: 'scripts/pages/summary-report/calculateGrantSummaryReport.html',
    controller: CalculateGrantSummaryReport
  });
