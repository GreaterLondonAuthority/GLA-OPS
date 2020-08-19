/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

class ChangeReportTableSeparator {
  constructor($rootScope, $scope) {
  }

}

ChangeReportTableSeparator.$inject = ['$rootScope', '$scope'];

angular.module('GLA')
  .component('changeReportTableSeparator', {
    bindings: {
      hasRightValues: '<'
    },
    templateUrl: 'scripts/pages/change-report/change-report-table-separator/changeReportTableSeparator.html',
    controller: ChangeReportTableSeparator
  });
