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
class ChangeReportFieldLookup {
  constructor($rootScope, $scope, ReportService) {
    this.ReportService = ReportService;
    this.emptyField = '-';
    this.displayMode = ReportService.getReportDisplayMode();
    // take from left and right the value to look for in th collection: for example orgId
    let leftLookupValue = this.ReportService.extractValue(this.key, this.data.left);
    let rightLookupValue = this.ReportService.extractValue(this.key, this.data.right);

    // determin collectionKey (field in the collection that is used to find)
    this.collectionKey = this.collectionKey || 'id';

    // extract from collection the entry that matches the key/value
    this.lookupLeft = _.find(this.collection, {[this.collectionKey]:leftLookupValue}) || {};
    this.lookupRight = _.find(this.collection, {[this.collectionKey]:rightLookupValue}) || {};


    this.fields = angular.isArray(this.fields) ? this.fields : [this.fields];
    this.formats = angular.isArray(this.formats) ? this.formats : [this.formats];
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

  hasFieldChanges(fieldName){
    let hasFieldChanged = this.data.changes && this.data.changes.hasFieldChanged(this.changeAttribute || fieldName, this.data.right);
    this.hasChanges = this.hasChanges || hasFieldChanged;
    return hasFieldChanged;
  }
}

ChangeReportFieldLookup.$inject = ['$rootScope', '$scope', 'ReportService'];
angular.module('GLA')
  .component('changeReportFieldLookup', {
    bindings: {
      data: '<',
      fields: '<',
      label: '<',

      key: '<',
      collectionKey: '<',

      collection: '<'
    },
    templateUrl: 'scripts/pages/change-report/change-report-field-lookup/changeReportFieldLookup.html',
    controller: ChangeReportFieldLookup
  });
