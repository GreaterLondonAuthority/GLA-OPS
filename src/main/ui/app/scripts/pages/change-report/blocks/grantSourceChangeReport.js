/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

class GrantSourceChangeReport {
  constructor($log, GrantSourceService) {
    this.config = GrantSourceService.getSourceVisibilityConfig(this.data.context.template);
    this.blockMetaData = GrantSourceService.getBlockMetaData(this.data.context.template);
    this.config = GrantSourceService.getAssociatedVisibilityConfig(this.data, this.config);
  }
}

GrantSourceChangeReport.$inject = ['$log', 'GrantSourceService'];

angular.module('GLA')
  .component('grantSourceChangeReport', {
    bindings: {
      data: '<'
    },
    templateUrl: 'scripts/pages/change-report/blocks/grantSourceChangeReport.html',
    controller: GrantSourceChangeReport  });
