/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */


class GrantSourceSummaryReport {
  constructor(GrantSourceService){
    this.GrantSourceService = GrantSourceService;
  }
  $onInit(){
    this.block = this.data.left;
    this.associatedProjectConfig = this.GrantSourceService.getAssociatedProjectConfig(this.data.left);
    this.ngClassConfig = {
      'hide-grant-sources': this.block.zeroGrantRequested || this.block.associatedProject,
      'hide-strategic-partnership-funding': !this.associatedProjectConfig.showMarker,
      'hide-strategic-partnership-funding-amount': !this.block.associatedProject,
    }
  }

}

GrantSourceSummaryReport.$inject = ['GrantSourceService'];

angular.module('GLA')
  .component('grantSourceSummaryReport', {
    bindings: {
      data: '<'
    },
    templateUrl: 'scripts/pages/summary-report/grantSourceSummaryReport.html',
    controller: GrantSourceSummaryReport
  });
