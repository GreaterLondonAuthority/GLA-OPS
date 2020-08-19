/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */


//TODO make generic cell component
class GrantTableController {

  tenureChange(t) {
    this.onTenureChange();
  }

  hasErrors(m, fieldName) {
    let errors = this.data.validationFailures || {};
    let rowErrors = errors[m.id];
    if (!rowErrors) {
      return false;
    }
    let el = _.find(rowErrors, {'name': fieldName});
    return !!el;
  }
}

GrantTableController.$inject = [];


angular.module('GLA')
  .component('grantTable', {
    bindings: {
      data: '<data',
      isReadonly: '<isReadonly',
      onTenureChange: '&'
    },
    controller: GrantTableController,
    templateUrl: 'scripts/pages/project/grant/grant-table/grantTable.html',
  });
