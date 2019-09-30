/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
function index(obj, i) {
  if(!obj){
    return;
  }
  return obj[i];
}
class ChangeReportCoodinates {
  constructor(ReportService) {
    this.ReportService = ReportService;
  }

  $onInit(){
    this.data = this.data || {};
    this.fields = angular.isArray(this.fields) ? this.fields : [this.fields];
    this.displayMode = this.ReportService.getReportDisplayMode();
    this.emptyField = '-';
  }

  getValue(data, field) {
    if(!data){
      return;
    }

    //function index(obj,i){return obj[i]};'a.b.etc'.split('.').reduce(index, {a:{b:{etc:123}}})
    return field.split('.').reduce(index, data) || this.emptyField;
  }

  hasFieldChanges(field){
    let hasFieldChanged = this.data.changes && this.data.changes.hasFieldChanged(field, this.data.right);
    this.hasChanges = this.hasChanges || hasFieldChanged;
    return hasFieldChanged;
  }
}

ChangeReportCoodinates.$inject = ['ReportService'];

angular.module('GLA')
  .component('changeReportCoodinates', {
    bindings: {
      data: '<',
      fields: '<',
      label: '<'
    },
    templateUrl: 'scripts/pages/change-report/change-report-coordinates/changeReportCoordinates.html',
    controller: ChangeReportCoodinates
  });
