/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

import ExpandableRow from '../../../../components/expandableTable/ExpandableRow';
import NumberUtil from '../../../../util/NumberUtil';

class ReceiptsMonthRowCtrl extends ExpandableRow {
  constructor($scope, numberFilter) {
    super();
    this.$scope = $scope;
    this.numberFilter = numberFilter;
  }

  $onInit(){
    super.init();
    this.data.breakdown = _.orderBy(this.data.breakdown, ['category']);
    this.originalData = _.cloneDeep(this.data);
    this.monthName = this.data.monthName;
    this.total = this.data.monthlyTotal;
    this.breakdown = this.data.breakdown;
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
  formatNumberWithCR(value) {
    // return NumberUtil.formatWithCommasAndCR(value);
    let result = NumberUtil.formatWithCommas(value);
    return result? result: '';
  }

  /**
   * Expand/collapse row handler
   */
  openClose() {
    if (this.breakdown.length) {
      this.changeExpandedState();
    }
  }

  inputFocused(spend) {
    spend.oldCapitalForecast = spend.capitalForecast;
    spend.oldRevenueForecast = spend.revenueForecast;
  }

  /**
   * On cell edit handler
   * @param {number} index
   */
  onCellEditBlur(index) {
    if(this.data.breakdown[index].forecast !== this.originalData.breakdown[index].forecast) {
      let receiptDate = moment(this.data.yearMonth, 'YYYYMM');
      let data = this.data.breakdown[index];
      data.year = +(receiptDate.format('YYYY'));
      data.month = +(receiptDate.format('MM'));
      data.monthName = this.monthName;
      this.onEdit({event: {data: data}});
    }
  }

  number(number, decimals){
    if(number != null){
      return this.numberFilter(number, decimals || 0);
    }
  }
}

ReceiptsMonthRowCtrl.$inject = ['$scope', 'numberFilter'];

angular.module('GLA')
  .component('receiptsMonthRow', {
    bindings: {
      sessionStorage: '=',
      tableId: '@',
      rowId: '<',
      data: '=',
      onEdit: '&',
      onDelete: '&',
      onShowMetadata: '&',
      readOnly: '<'
    },
    templateUrl: 'scripts/pages/project/receipts/forecast/receiptsMonthRow.html',
    controller: ReceiptsMonthRowCtrl
  });
