/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

import DateUtil from '../../../util/DateUtil';

class ReceiptsChangeReport {
  constructor(ReportService) {
    this.ReportService = ReportService;
  }


  $onInit() {

    console.log('data', this.data);

    let comparableReceiptRows = this.getComparableReceiptRows(this.data.left, this.data.right);
    this.comparableReceiptGroups = this.ReportService.groupComparableRows(comparableReceiptRows, 'financialYear', true);

    this.comparableWbsCodeRows = this.getComparableWbsCodeRows(this.data.left, this.data.right);

    if(this.data.right){
      this.data.changes.addDeletions(comparableReceiptRows);
      this.data.changes.addDeletions(this.comparableWbsCodeRows);
    }


    const currentFinancialYear = this.data.context.currentFinancialYear;

    function getYear(comparableRow) {
      if (comparableRow) {
        return (comparableRow.left || comparableRow.right).financialYear;
      }
      return null;
    }

    this.wbsCodeFields = [
      {
        field: 'code',
        label: 'WBS CODE'
      }
    ];

    this.receiptsFields = [
      {
        field: 'totalForPastMonths.actual',
        label: 'ACTUAL £',
        format: 'number|2',
        hide(comparableRow){
          let year = getYear(comparableRow);
          return !year || year > currentFinancialYear;
        },
        changeAttribute: 'actual'
      },

      {
        field: 'totalForCurrentAndFutureMonths.forecast',
        label: 'REMAINING FORECAST £',
        format: 'number|2',
        hide(comparableRow){
          let year = getYear(comparableRow);
          return !year || year < currentFinancialYear;
        },
        changeAttribute: 'forecast'
      },

    ];


    // this.comparableOutputGroups = this.ReportService.groupComparableRows(comparableRows, 'financialYear', true);

  }

  getComparableReceiptRows(leftBlock, rightBlock) {
    let leftAndRightRows = [leftBlock, rightBlock].map(block => {
      return (block || {}).annualReceiptsSummaries || []
    });


    //Filter to identify left side tenure type row when you have the right side row
    let leftSideFilter = function (rightRow) {
      return {comparisonId: rightRow.comparisonId}
    };

    return this.ReportService.rowsToCompare(leftAndRightRows[0], leftAndRightRows[1], leftSideFilter);
  }

  getComparableWbsCodeRows(leftBlock, rightBlock) {
    let leftAndRightRows = [leftBlock, rightBlock].map(block => {
      return (block || {}).wbsCodes || []
    });


    //Filter to identify left side tenure type row when you have the right side row
    let leftSideFilter = function (rightRow) {
      return {comparisonId: rightRow.comparisonId}
    };

    return this.ReportService.rowsToCompare(leftAndRightRows[0], leftAndRightRows[1], leftSideFilter);
  }


  groupTitle(prefix, year) {
    let financialYear = DateUtil.toFinancialYearString(year);
    return `${prefix} ${financialYear}`;
  }
}

ReceiptsChangeReport.$inject = ['ReportService'];

angular.module('GLA')
  .component('receiptsChangeReport', {
    bindings: {
      data: '<'
    },
    templateUrl: 'scripts/pages/change-report/blocks/receiptsChangeReport.html',
    controller: ReceiptsChangeReport  });
