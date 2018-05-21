/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

'use strict';

InputCostBudgetCtrl.$inject = ['$scope', '$element', '$attrs'];

function InputCostBudgetCtrl($scope, $element, $attrs) {
  var ctrl = this;
  this.isSelected = (this.isSelected === true) ? true : false;
  this.readOnly = (this.readOnly === true) ? true : false;

  this.linkId = this.label.replace('£0 ', '').replace('(', '').replace(')', '').split(' ').join('-');

  this.onCheckboxChange = function() {
    if(ctrl.readOnly) return;
    this.value = !!ctrl.isSelected ? this.value : null;
  }

  this.onSelectedChange = function() {
    if(ctrl.readOnly) return;
    ctrl.isSelected = !ctrl.isSelected;
    this.value = !!ctrl.isSelected ? this.value : null;
  }
}

angular.module('GLA')
  .component('inputCostBudget', {
    templateUrl: 'scripts/components/input-cost-budget/inputCostBudget.html',
    bindings: {
      label: '@',
      isSelected: '=',
      value: '=',
      readOnly: '<?',
      hideValue: '<?',
      hideCheckbox: '<?'
    },
    controller: InputCostBudgetCtrl
  });
