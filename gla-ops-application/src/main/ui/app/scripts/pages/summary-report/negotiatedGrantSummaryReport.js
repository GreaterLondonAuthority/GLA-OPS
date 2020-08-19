/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */


class NegotiatedGrantSummaryReport {
  constructor(GrantService) {
    this.GrantService = GrantService
  }


  $onInit() {
    this.data = this.GrantService.sortTenureTypes(this.block);
  }
}

NegotiatedGrantSummaryReport.$inject = ['GrantService'];

angular.module('GLA')
  .component('negotiatedGrantSummaryReport', {
    bindings: {
      block: '<',
    },
    templateUrl: 'scripts/pages/summary-report/negotiatedGrantSummaryReport.html',
    controller: NegotiatedGrantSummaryReport
  });
