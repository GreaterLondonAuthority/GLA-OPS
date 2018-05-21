/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

import NumberUtil from '../../util/NumberUtil';

class ForecastChangeCtrl {
  constructor($scope) {
    this.originalSpend = angular.copy(this.spend);
    this.originalValue = this.isCapital ? this.originalSpend.capitalForecast : this.originalSpend.revenueForecast;

    if(this.originalValue > 0) {
      this.isCR = true;
    } else if(this.originalValue < 0) {
      this.isCR = false;
      this.originalValue *= -1;
    } else {
      this.isEmpty = true;
    }
    this.value = this.originalValue;
  }

  keyDown($event) {
    if($event.keyCode === 13){
      $event.preventDefault();
      $event.stopPropagation();
      var target = $event.target;

      // do more here, like blur or other things
      target.blur();
    }
  }

  /**
   * Format number to string with comma's and append CR
   * @see `NumberUtil.formatWithCommasAndCR()`
   */
  formatNumberWithCR(value) {
    return NumberUtil.formatWithCommasAndCR(this.isCR ? value : (value *-1));
  }

  /**
   * change callback for revenue and capital.
   * change triggered on blur
   * triggers AnnualSpendCtrl->onAddSpendForecast as we are using the same
   * logic to update database
   * @param  {[type]}  newValue  new entered value
   * @param  {[type]}  oldValue  old value - this is a string snapshot of the value generated when the template renders.
   * @param  {[type]}  spend     [description]
   * @param  {Boolean} isCapital distiguishe between capital and revenu fields
   */
  changeValue() {
    // compare to old value
    if(this.value === this.originalValue) return;

    var type = this.isCR ? 'CREDIT' : 'EXPENDITURE';

    // build entryType value
    var entryType;

    entryType =
      this.isCapital ?
      'CAPITAL_' + type :
      'REVENUE_' + type;

    // callback to annualSpendForecast
    this.onCellEdit({
      event: {
        data: {
          entryType: entryType,
          forecastValue: this.value,
          categoryId: this.originalSpend.categoryId,
        }
      }
    });
  }
}

ForecastChangeCtrl.$inject = ['$scope'];

angular.module('GLA')
  .component('forecastChange', {
    bindings: {
      readOnly: '<',
      spend: '<',
      isCapital: '<',
      onCellFocus: '&',
      getCellFocus: '&',
      onCellEdit: '&'
    },
    templateUrl: 'scripts/components/forecast-change/forecastChange.html',
    controller: ForecastChangeCtrl,
  });
