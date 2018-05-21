/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

'use strict';

var gla = angular.module('GLA');

gla.component('grantTable', {
  bindings: {
    data: '<data',
    isReadonly: '<isReadonly',
    onTenureChange: '&'
  },
  controller: GrantTableController,
  templateUrl: 'scripts/pages/project/grant/grant-table/grantTable.html',
});

//TODO make generic cell component
function GrantTableController(){
  this.tenureChange = function(t){
    t.totalUnits = t.totalUnits || 0;
    t.s106Units = t.s106Units || 0;
    t.totalCost = t.totalCost || 0;

    t.grantRequested = t.grantRequested || 0;
    t.supportedUnits = t.supportedUnits || 0;

    t.additionalAffordableUnits = t.additionalAffordableUnits || 0;
    t.additionalAffordableUnits = t.additionalAffordableUnits || 0;

    t.units = t.units || 0;

    this.onTenureChange();
  };

  this.hasErrors = function (m, fieldName) {
    var errors = this.data.validationFailures || {};
    var rowErrors = errors[m.id];
    if (!rowErrors) {
      return false;
    }
    var el = _.find(rowErrors, {'name': fieldName});
    return !!el;
  };
}

GrantTableController.$inject = [];
