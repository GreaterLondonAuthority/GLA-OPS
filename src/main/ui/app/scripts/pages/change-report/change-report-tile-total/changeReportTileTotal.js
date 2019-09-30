/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

class ChangeReportTileTotal {
  constructor(ReportService) {
    this.ReportService = ReportService;
  }

  $onInit(){
    this.hasRightValues = !!this.data.right;
  }

  getValue(data, field) {
    return this.ReportService.getDisplayValue(field, 'currency', data);
  }

  hasFieldChanges(field){
    return this.changes && this.changes.hasFieldChanged(field, this.data.right);
  }
}

ChangeReportTileTotal.$inject = ['ReportService'];

angular.module('GLA')
  .component('changeReportTileTotal', {
    bindings: {
      data: '<',
      field: '<',
      heading: '<',
      format: '<',
      changes: '<?'
    },
    templateUrl: 'scripts/pages/change-report/change-report-tile-total/changeReportTileTotal.html',
    controller: ChangeReportTileTotal
  });
