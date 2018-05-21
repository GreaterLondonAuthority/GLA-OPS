/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

class ChangeReportField {
  constructor($rootScope, $scope, ReportService) {
    this.data = this.data || {};

    this.ReportService = ReportService;
    this.displayMode = this.ReportService.getReportDisplayMode();

    // TODO remove this when styling is in place
    this.emptyField = '-';

    this.fields = angular.isArray(this.fields) ? this.fields : [this.fields];
    this.formats = angular.isArray(this.formats) ? this.formats : [this.formats];

    if(this.formats && this.formats.length > 1 && this.formats.length !== this.fields.length){
      throw 'formats has to be 0 (no formatting), 1 (same formatting applied to all), or equal in length to ' + this.fields.length + ' (for 1 on 1 mapping)';
    }

    this.mappedFields = this.ReportService.mapFields(this.fields, this.formats);
  }
  getValue(data, field) {

    if(!data){
      return;
    }

    let value = this.ReportService.extractValue(field.field, data);
    if(field.format){
      value = this.ReportService.formatFieldValue(value, field.format);
    }
    return value || this.emptyField;
  }

  hasFieldChanges(field){
    let changes = this.changes || this.data.changes;
    let hasFieldChanged = changes && changes.hasFieldChanged(this.changeAttribute || field.field, this.data.right);
    this.hasChanges = this.hasChanges || hasFieldChanged;
    return hasFieldChanged;
  }
}

ChangeReportField.$inject = ['$rootScope', '$scope', 'ReportService', 'numberFilter', 'currencyFilter', 'dateFilter'];

angular.module('GLA')
  .component('changeReportField', {
    bindings: {
      data: '<',
      fields: '<',
      label: '<',
      sublabel: '<',
      formats: '<',
      changeAttribute: '<?',
      changes: '<?'
    },
    templateUrl: 'scripts/pages/change-report/change-report-field/changeReportField.html',
    controller: ChangeReportField
  });
