/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

import ExpandableRow from '../../../../components/expandableTable/ExpandableRow';
import NumberUtil from '../../../../util/NumberUtil';
import ForecastDataUtil from '../../../../util/ForecastDataUtil';

class ForecastMonthRowCtrl extends ExpandableRow {
  constructor($scope) {
    super();

    this.$scope = $scope;

    this.monthName = this.monthData.monthName;
    this.total = this.monthData.monthlyTotal;
    this.spendBreakdown = _.orderBy(this.monthData.spendBreakdown, ['spendCategory']);
  }

  /**
   * Returns a complete name for the month, from the shortened from the server
   * @returns {String} 'MMMM'
   */
  getMonthFullName(label) {
    return moment(label, 'MMM').format('MMMM');
  }

  getMonthNumber(label) {
    return moment(label, 'MMM').format('M');
  }

  /**
   * Convert a negative number to positive
   * @param {Number} value
   * @returns {Number} positive number
   */
  toPositiveNumber(value) {
    return (value < 0) ? Math.abs(value) : value;
  }

  /**
   * Format number to string with comma's and append CR
   * @see `NumberUtil.formatWithCommasAndCR()`
   */
  formatNumberWithCR(value, precision) {
    return NumberUtil.formatWithCommasAndCR(value, precision);
  }

  /**
   * Expand/collapse row handler
   */
  openClose() {
    if (this.spendBreakdown.length) {
      this.changeExpandedState();
    }
  }

  inputFocused(spend){
    spend.oldCapitalForecast = spend.capitalForecast;
    spend.oldRevenueForecast = spend.revenueForecast;
  }

  onForecastChange(event) {
    var data = event.data;
    data.month = this.getMonthNumber(this.monthData.monthName);

    this.onCellEdit({event: {data: data}});
  }

  onDelete(spend) {
    const date = moment(this.monthData.yearMonth, 'YYYYMM');
    const data = {
      entityType: ForecastDataUtil.getLedgerCodeBySpendObj(spend),
      month: eval(date.format('M')),
      categoryId: spend.categoryId
    }
    this.onDeleteSpend({event: {data: data}});
  }
}

ForecastMonthRowCtrl.$inject = ['$scope'];

angular.module('GLA')
  .component('forecastMonthRow', {
    bindings: {
      sessionStorage: '=',
      tableId: '@',
      rowId: '<',
      monthData: '=',
      onCellEdit: '&',
      onDeleteSpend: '&',
      onShowMetadata: '&',
      readOnly: '<'
    },
    templateUrl: 'scripts/pages/project/project-budget/forecast/forecastMonthRow.html',
    controller: ForecastMonthRowCtrl
  });
