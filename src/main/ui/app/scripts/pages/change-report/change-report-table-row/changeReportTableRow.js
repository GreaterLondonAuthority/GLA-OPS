/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

class ChangeReportTableRow {
  constructor(ReportService) {
    this.ReportService = ReportService;
  }
  getValue(data, field) {
    return this.ReportService.getDisplayValue(field.field, field.format, data, field.defaultValue);
  }

  fileURL(data, field) {
    if(field.format && field.format === 'file'){
      let row = data || {};
      let fileId = row.fileId;
      if (fileId){
        return `/api/v1/file/${fileId}`;
      }
    }
    return null;
  }

  hasFieldChanges(field){
    let params = [field.changeAttribute || field.field, this.row.right];
    if(field.changeAttribute && _.isFunction(field.changeAttribute)){
      params = [field.changeAttribute(this.row.right, this.row)];
    }
    return this.changes && this.changes.hasFieldChanged(...params);
  }

  fieldVisible(field){
    if(field.hide && _.isFunction(field.hide)){
      return !field.hide(this.row);
    }
    return true
  }
}

ChangeReportTableRow.$inject = ['ReportService'];

angular.module('GLA')
  .component('changeReportTableRow', {
    bindings: {
      heading: '<',
      row: '<',
      fields: '<',
      changes: '<?'
    },
    templateUrl: 'scripts/pages/change-report/change-report-table-row/changeReportTableRow.html',
    controller: ChangeReportTableRow
  });
