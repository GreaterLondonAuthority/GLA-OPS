/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */


class BudgetsSummaryReport {
  constructor(numberFilter) {
    this.numberFilter = numberFilter;
  }


  $onInit() {
    this.annualSpend = _.find(this.block.annualSpendSummaries, {year: this.financialYear});
    this.tiles = this.getTiles(this.annualSpend );
  }

  getTiles(annualSpend) {
    if(!annualSpend){
      return [];
    }
    let actualSpend = annualSpend.actualSpend || {};
    let totalForCurrentAndFutureMonths = annualSpend.totalForCurrentAndFutureMonths || {};
    let totals = annualSpend.totals || {};
    return [{
      name: `ACTUAL SPEND`,
      items: [{
        itemName: 'Capital',
        itemValue: this.numberOrDash(Math.abs(actualSpend.capitalActual)),
      }, {
        itemName: 'Revenue',
        itemValue: this.numberOrDash(Math.abs(actualSpend.revenueActual)),
      }]
    }, {
      name: `REMAINING FORECAST (CURRENT & FUTURE MONTHS)`,
      items: [{
        itemName: 'Capital',
        itemValue: this.numberOrDash(Math.abs(totalForCurrentAndFutureMonths.capitalForecast)),
      }, {
        itemName: 'Revenue',
        itemValue: this.numberOrDash(Math.abs(totalForCurrentAndFutureMonths.revenueForecast)),
      }]
    }, {
      name: `LEFT TO SPEND AGAINST ANNUAL BUDGET`,
      items: [{
        itemName: 'Capital',
        itemValue: this.numberOrDash(totals.leftToSpendCapitalInclCurrentMonth),
      }, {
        itemName: 'Revenue',
        itemValue: this.numberOrDash(totals.leftToSpendRevenueInclCurrentMonth),
      }]
    }, {
      name: `AVAILABLE TO FORECAST AGAINST ANNUAL BUDGET`,
      items: [{
        itemName: 'Capital',
        itemValue: this.numberOrDash(totals.availableToForecastCapital),
      }, {
        itemName: 'Revenue',
        itemValue: this.numberOrDash(totals.availableToForecastRevenue),
      }]
    }];
  }

  numberOrDash(number) {
    return number ? this.numberFilter(number) : '-';
  }
}

BudgetsSummaryReport.$inject = ['numberFilter'];

angular.module('GLA')
  .component('budgetsSummaryReport', {
    bindings: {
      block: '<',
      financialYear: '<',
    },
    templateUrl: 'scripts/pages/summary-report/budgetsSummaryReport.html',
    controller: BudgetsSummaryReport
  });
