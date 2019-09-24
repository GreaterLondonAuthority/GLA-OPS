/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */


class InputCostBudgetCtrl {
  constructor() {
  }

  $onInit() {
    this.isSelected = (this.isSelected === true) ? true : false;
    this.readOnly = (this.readOnly === true) ? true : false;

    this.linkId = this.label.replace('£0 ', '').replace('(', '').replace(')', '').split(' ').join('-');
  }

  onCheckboxChange() {
    if (this.readOnly) return;
    this.value = !!this.isSelected ? this.value : null;
  }

  onSelectedChange() {
    if (this.readOnly) return;
    this.isSelected = !this.isSelected;
    this.value = !!this.isSelected ? this.value : null;
  }
}

InputCostBudgetCtrl.$inject = [];

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
