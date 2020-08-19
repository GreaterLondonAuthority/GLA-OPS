/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

class ChangeReportTileRow {
  constructor(ReportService) {
    this.ReportService = ReportService;
  }
  getValue(data, field) {
    return this.ReportService.getDisplayValue(field.field, field.format, data);
  }

  getHeading(data){
    return this.ReportService.getDisplayValue(this.headingField, null, data);
  }

  getDescription(data){
    return this.ReportService.getDisplayValue(this.descriptionField, null, data);
  }

  hasFieldChanges(field){
    let changeAttribute = field.changeAttribute || field.field;
    if(field.changeAttribute && _.isFunction(field.changeAttribute)){
      changeAttribute= field.changeAttribute(this.row.right);
    }
    return this.changes && this.changes.hasFieldChanged(changeAttribute, this.row.right);
  }
}

ChangeReportTileRow.$inject = ['ReportService'];

angular.module('GLA')
  .component('changeReportTileRow', {
    bindings: {
      headingField: '<',
      descriptionField: '<?',
      row: '<',
      fields: '<',
      changes: '<?'
    },
    templateUrl: 'scripts/pages/change-report/change-report-tiles/change-report-tile-row/changeReportTileRow.html',
    controller: ChangeReportTileRow
  });
