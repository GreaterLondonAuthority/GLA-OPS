/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

import ExpandableRow from '../../../../components/expandableTable/ExpandableRow';
import NumberUtil from '../../../../util/NumberUtil';
import DateUtil from '../../../../util/DateUtil';
import OutputsUtil from '../OutputsUtil';

class ForecastCategoryRowCtrl extends ExpandableRow {
  constructor($scope, $element) {
    super();
    this.originalData = _.cloneDeep(this.data);
    this.category = this.data[0].config.category;
  }

  /**
   * Returns a name for the month, from the month value or the quarter
   * @returns {String} 'MMM' or 'QN'
   */
  getPeriodName(number) {
    if (this.periodType === 'Quarterly') {
      return 'Q'+(number < 4 ? 4 : (number-1)/3);
    }
    else {
      return moment(number, 'MM').format('MMM');
    }
  }

  /**
   * Returns the value from Id
   */
  getOutputTypeById(id) {
    return OutputsUtil.getOutputTypes()[id];
  }

  /**
   * Returns the unit label by Id
   */
  getUnitLabelById(id) {
    return OutputsUtil.getUnitConfig()[id].label;
  }

  /**
   * Returns the input decimal precision by Id
   */
  getUnitPrecision(id) {
    return OutputsUtil.getUnitConfig()[id].precision || 0;
  }

  /**
   * Evaluates if the month and year are after the today's date
   * @param {String} date 'MM' month
   * @param {String} date 'YYYY' year
   * @returns {Boolean}
   */
  isFutureDate(month, year) {
    const date = moment(`${month}/${year}`, 'MM/YYYY');
    const now = moment(); // TODO: this should be coming from the backend
    return date.isAfter(now);
  }

  /**
   * Format number to string with comma's and append CR
   * @see `NumberUtil.formatWithCommasAndCR()`
   */
  formatNumber(value, valueType) {
    const precision = this.getUnitPrecision(valueType);
    return value ? NumberUtil.formatWithCommas(value, precision) : '';
  }

  formatDifference(value, valueType) {
    const precision = this.getUnitPrecision(valueType);
    return `${value > 0 ? '+' : ''}${NumberUtil.formatWithCommas(value, precision)}`;
  }

  /**
   * Format number to string with comma's and append CR
   * @see `NumberUtil.formatWithCommasAndCR()`
   */
  formatNumberWithCR(value) {
    return NumberUtil.formatWithCommasAndCR(value);
  }

  /**
   * Expand/collapse row handler
   */
  openClose() {
    if (this.data.length) {
      this.changeExpandedState();
    }
  }

  onCellEditBlur(index) {
    if(this.data[index].forecast !== this.originalData[index].forecast ||
      this.data[index].actual !== this.originalData[index].actual) {
      this.onRowChanged({event:{data: this.data[index]}});
    }
  }
}

ForecastCategoryRowCtrl.$inject = ['$scope', '$element'];

angular
  .module('GLA')
  .component('forecastCategoryRow', {
    bindings: {
      sessionStorage: '=',
      tableId: '@',
      rowId: '<',
      data: '=',
      currentFinancialYear: '<',
      columnOffsets: '<',
      onRowChanged: '&',
      onDelete: '&',
      readOnly: '<',
      periodType: '<'
    },
    templateUrl: 'scripts/pages/project/outputs/forecast/forecastCategoryRow.html',
    controller: ForecastCategoryRowCtrl
  });
