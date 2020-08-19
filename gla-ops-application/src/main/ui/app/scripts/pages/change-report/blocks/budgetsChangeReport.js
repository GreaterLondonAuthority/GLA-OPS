/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

import DateUtil from '../../../util/DateUtil';

class ProjectBudgetsChangeReport {
  constructor(ReportService, Util) {
    this.ReportService = ReportService;
    this.NumberUtil = Util.Number;
  }


  $onInit() {
    this.title = this.getTitle();

    this.comparableAttachmentRows = this.getComparableAttachmentRows(this.data.left, this.data.right);

    let comparableBudgetRows = this.getComparableBudgetGroups(this.data.left, this.data.right);
    this.comparableBudgetGroups = this.ReportService.groupComparableRows(comparableBudgetRows, 'financialYear', true);

    this.totalsToCompare = this.getComparableBudgetTotals(this.data.left, this.data.right);

    this.lifeTimeSpendSummaries = this.getComparableLifeTimeSpendSummaries(this.data.left, this.data.right);

    this.comparableAnnualSpendRows = this.getComparableAnnualSpendRows(this.data.left, this.data.right);

    if(this.data.right){
      this.data.changes.addDeletions(this.comparableAttachmentRows);
      this.data.changes.addDeletions(comparableBudgetRows);
      this.data.changes.addDeletions(this.totalsToCompare);
      this.data.changes.addDeletions(this.lifeTimeSpendSummaries);
      this.data.changes.addDeletions(this.comparableAnnualSpendRows);
    }


    this.attachmentsFields = [
      {
        field: 'fileName',
        label: 'DOCUMENT NAME'
      },

      {
        field: 'documentType',
        label: 'DOCUMENT TYPE'
      },

      {
        field: 'createdOn',
        label: 'UPLOAD DATE',
        format: 'date'
      },

      {
        field: 'creatorName',
        label: 'UPLOADED BY'
      }

    ];


    const currentFinancialYear = this.data.context.currentFinancialYear;

    function getYear(comparableRow) {
      if (comparableRow) {
        return (comparableRow.left || comparableRow.right).financialYear;
      }
      return null;
    }

    let NumberUtil = this.NumberUtil;

    function formatWithCommasAndCR (fieldName){
      return row => row ? NumberUtil.formatWithCommasAndCR(row[fieldName]) || 0 : null;
    }

    this.budgetsFields = [
      {
        field: 'spendType',
        label: 'SPEND TYPE',
        format(row){
          return row ? _.startCase(_.toLower(row.spendType)) : null;
        }
      },

      {
        field: 'forecastValue',
        label: 'REMAINING FORECAST £',
        format: formatWithCommasAndCR('forecastValue'),
        hide(comparableRow){
          let year = getYear(comparableRow);
          return !year || year < currentFinancialYear;
        }
      },

      {
        field: 'actualValue',
        label: 'ACTUAL SPEND £',
        format: formatWithCommasAndCR('actualValue'),
        hide(comparableRow){
          let year = getYear(comparableRow);
          return !year || year > currentFinancialYear;
        }
      },

      {
        field: 'remainingForecastAndActuals',
        label: 'REMAINING FORECAST + ACTUAL SPEND £',
        format: formatWithCommasAndCR('remainingForecastAndActuals'),
        hide(comparableRow){
          let year = getYear(comparableRow);
          return !year || year !== currentFinancialYear;
        }
      }

    ];


    this.totalsFields = [
      {
        field: '',
        label: 'SPEND TYPE',
        format(row){
          return 'Capital and Revenue';
        }
      },

      {
        field: 'forecastValueTotal',
        label: 'REMAINING FORECAST £',
        format: formatWithCommasAndCR('forecastValueTotal')
      },

      {
        field: 'actualValueTotal',
        label: 'ACTUAL SPEND £',
        format: formatWithCommasAndCR('actualValueTotal')
      },

      {
        field: 'remainingForecastAndActualsTotal',
        label: 'REMAINING FORECAST + ACTUAL SPEND £',
        format: formatWithCommasAndCR('remainingForecastAndActualsTotal')
      }
    ];


    this.lifeTimeSpendSummariesFields = [
      {
        label: 'Capital',
        field: 'capital',
        format: 'currency',
        changeAttribute(row){
          return row? `totals:${row.capitalField}`: null;
        }
      },
      {
        label: 'Revenue',
        field: 'revenue',
        format: 'currency',
        changeAttribute(row){
          return row? `totals:${row.revenueField}`: null;
        }
      }
    ];

    this.annualSpendFields = [
      {
        label: 'Capital',
        field: 'capital',
        format: 'currency',
        changeAttribute(row){
          return row? `${row.capitalField}`: null;
        }
      },
      {
        label: 'Revenue',
        field: 'revenue',
        format: 'currency',
        changeAttribute(row){
          return row? `${row.revenueField}`: null;
        }
      }
    ];
  }

  getTitle() {
    let allYears = [this.data.left, this.data.right].reduce((allYears, block) => {
      block = block || {};
      allYears.push(block.toFinancialYear);
      allYears.push(block.fromFinancialYear);
      return allYears;
    }, []);
    let startYear = _.min(allYears);
    let endYear = _.max(allYears);

    let title = 'Approved lifetime budget';

    if (startYear) {
      title += ' ' + DateUtil.toFinancialYearString(startYear);
    }

    if (endYear && startYear != endYear) {
      title += ' - ' + DateUtil.toFinancialYearString(endYear);
    }
    return title;
  }

  getComparableBudgetGroups(leftBlock, rightBlock) {
    let leftAndRightBlock = [leftBlock, rightBlock].map(block => {
      if (block && block.projectBudgetsYearlySummary) {
        return block.projectBudgetsYearlySummary.summaryEntries || [];
      }
      return [];
    });


    //Filter to identify left side tenure type row when you have the right side row
    let leftSideFilter = function (rightRow) {
      return {comparisonId: rightRow.comparisonId}
    };

    return this.ReportService.rowsToCompare(leftAndRightBlock[0], leftAndRightBlock[1], leftSideFilter);
  }

  getComparableAttachmentRows(leftBlock, rightBlock) {
    let leftAndRightBlock = [leftBlock, rightBlock].map(block => {
      return (block || {}).attachments || [];
    });


    //Filter to identify left side tenure type row when you have the right side row
    let leftSideFilter = function (rightRow) {
      return {comparisonId: rightRow.comparisonId}
    };

    return this.ReportService.rowsToCompare(leftAndRightBlock[0], leftAndRightBlock[1], leftSideFilter);
  }


  getComparableAnnualSpendRows(leftBlock, rightBlock) {
    let leftAndRightBlock = [leftBlock, rightBlock].map(block => {
      return (block || {}).annualSpendSummaries || [];
    });


    //Filter to identify left side tenure type row when you have the right side row
    let leftSideFilter = function (rightRow) {
      return {comparisonId: rightRow.comparisonId}
    };


    let tiles = [
      {
        title: 'LEFT TO SPEND AGAINST ANNUAL BUDGET',
        description: 'Annual budget minus actual spend (excluding the current month)',
        capitalField: 'leftToSpendCapitalInclCurrentMonth',
        revenueField: 'leftToSpendRevenueInclCurrentMonth'
      },
      {
        title: 'AVAILABLE TO FORECAST AGAINST ANNUAL BUDGET',
        description: 'Annual budget minus total remaining forecast and actual spend',
        capitalField: 'availableToForecastCapital',
        revenueField: 'availableToForecastRevenue'
      },
    ];

    let comparableRows = this.ReportService.rowsToCompare(leftAndRightBlock[0], leftAndRightBlock[1], leftSideFilter);

    comparableRows.forEach(row => {
      row.tiles = [];
      tiles.forEach(tile => {
        let leftAndRightTile = [row.left, row.right].map(annualSpendSummary => {
          if (annualSpendSummary) {
            tile = angular.copy(tile);
            tile.capital = annualSpendSummary.totals[tile.capitalField] || 0;
            tile.revenue = annualSpendSummary.totals[tile.revenueField] || 0;
            tile.comparisonId = annualSpendSummary.comparisonId;
            return tile;
          }
          return null;
        });
        row.tiles.push({
          left: leftAndRightTile[0],
          right: leftAndRightTile[1]
        });
      });
    });
    return comparableRows;
  }

  getComparableBudgetTotals(leftBlock, rightBlock) {

    let leftAndRightBlock = [leftBlock, rightBlock].map(block => {
      if (block && block.projectBudgetsYearlySummary) {
        return block.projectBudgetsYearlySummary.projectBudgetsAllYearSummary;
      }
      return null;
    });


    let totalsRow = {
      left: leftAndRightBlock[0],
      right: leftAndRightBlock[1]
    };

    return (totalsRow.left || totalsRow.right) ? [totalsRow] : [];
  }

  getComparableLifeTimeSpendSummaries(leftBlock, rightBlock) {

    let leftAndRightBlock = [leftBlock, rightBlock].map(block => {
      return (block || {}).totals;
    });


    let tiles = [
      {
        title: 'AVAILABLE TO FORECAST',
        description: 'Lifetime budget minus total remaining forecast and actual spend',
        capitalField: 'availableToForecastCapital',
        revenueField: 'availableToForecastRevenue'
      },
      {
        title: 'LEFT TO SPEND',
        description: 'Lifetime budget minus the actual spend',
        capitalField: 'leftToSpendOnProjectCapital',
        revenueField: 'leftToSpendOnProjectRevenue'
      },
      {
        title: 'APPROVED PROJECT FORECAST',
        description: 'Amount of lifetime budget remaining after subtracting total forecast and actual spent',
        capitalField: 'approvedProjectForecastCapital',
        revenueField: 'approvedProjectForecastRevenue'
      },
      {
        title: 'UNAPPROVED PROJECT FORECAST',
        description: 'Amount by which actual spend + future forecast exceeds lifetime budget',
        capitalField: 'unapprovedProjectForecastCapital',
        revenueField: 'unapprovedProjectForecastRevenue'
      }
    ];

    let rows = [];
    tiles.forEach(tile => {
      let leftAndRightTile = leftAndRightBlock.map(blockTotals => {
        if (blockTotals) {
          tile = angular.copy(tile);
          tile.capital = blockTotals[tile.capitalField] || 0;
          tile.revenue = blockTotals[tile.revenueField] || 0;
          tile.comparisonId = blockTotals.comparisonId;
          return tile;
        }
        return null;
      });
      rows.push({
        left: leftAndRightTile[0],
        right: leftAndRightTile[1]
      });
    });
    return rows;
  }

  groupTitle(prefix, year) {
    let financialYear = DateUtil.toFinancialYearString(year);
    return `${prefix} ${financialYear}`;
  }
}

ProjectBudgetsChangeReport.$inject = ['ReportService', 'Util'];

angular.module('GLA')
  .component('budgetsChangeReport', {
    bindings: {
      data: '<'
    },
    templateUrl: 'scripts/pages/change-report/blocks/budgetsChangeReport.html',
    controller: ProjectBudgetsChangeReport
  });
