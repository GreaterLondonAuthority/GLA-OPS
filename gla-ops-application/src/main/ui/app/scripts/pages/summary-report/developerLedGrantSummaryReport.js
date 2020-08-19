/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */


class DeveloperLedGrantSummaryReport {
  constructor(GrantService) {
    this.GrantService = GrantService
  }


  $onInit() {
    this.data = this.GrantService.sortTenureTypes(this.block);
    this.affordableCriteriaMet = this.GrantService.getYesNoAnswer(this.data.affordableCriteriaMet);
  }
}

DeveloperLedGrantSummaryReport.$inject = ['GrantService'];

angular.module('GLA')
  .component('developerLedGrantSummaryReport', {
    bindings: {
      block: '<',
    },
    templateUrl: 'scripts/pages/summary-report/developerLedGrantSummaryReport.html',
    controller: DeveloperLedGrantSummaryReport
  });
