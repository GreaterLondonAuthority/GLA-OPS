/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

class ChangeReportFieldFiles {
  constructor(ReportService, $stateParams) {
    this.ReportService = ReportService;
    this.$stateParams = $stateParams;
  }

  $onInit(){
    this.data = this.data || {};

    this.displayMode = this.ReportService.getReportDisplayMode();

    // TODO remove this when styling is in place
    this.emptyField = [];

    // this.fields = angular.isArray(this.fields) ? this.fields : [this.fields];
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

    let value = this.ReportService.extractValue(field, data);
    if(field.format){
      value = this.ReportService.formatFieldValue(value, field.format);
    }
    return value || this.emptyField;
  }

  hasFieldChanges(field, file){
    let changes = this.changes || this.data.changes;
    let hasFieldChanged = changes && changes.hasFieldChanged(this.changeAttribute || field.field, field);
    this.hasChanges = this.hasChanges || hasFieldChanged;
    return hasFieldChanged;
  }
}

ChangeReportFieldFiles.$inject = ['ReportService', '$stateParams'];

angular.module('GLA')
  .component('changeReportFieldFiles', {
    bindings: {
      data: '<',
      field: '<',
      label: '<',
      sublabel: '<',
      formats: '<',
      changeAttribute: '<?',
      changes: '<?'
    },
    templateUrl: 'scripts/pages/change-report/change-report-field-files/changeReportFieldFiles.html',
    controller: ChangeReportFieldFiles
  });
