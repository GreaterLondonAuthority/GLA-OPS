
/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

import DateUtil from '../../../../util/DateUtil';


class yearlyBudgetCapitalRevenueFundingCtrl{
  constructor($injector, $scope, ProjectService, ProjectFundingService) {
  }

  $onInit(){
    this.capital = this.type === 'capital' ? true : false;
    this.revenue = this.type === 'revenue' ? true : false;

    this.addYearLabels(this.yearlyData);
  }

  addYearLabels(yearlyData) {
    _.forEach(yearlyData.years, (year) => {
      year['yearLabel'] = DateUtil.toFinancialYearString(_.toNumber(year.year));
    });
  }

  showRow(row) {
    if(this.capital) {
      return _.some(row.capitalTotals, (key) => {
        return key!=null;
      });
    }
    if(this.revenue) {
      return _.some(row.revenueTotals, (key) => {
        return key!=null;
      });
    }
    return true;
  }
}

yearlyBudgetCapitalRevenueFundingCtrl.$inject = [];

angular.module('GLA')
  .component('yearlyBudgetCapitalRevenueFunding', {
  controller: yearlyBudgetCapitalRevenueFundingCtrl,
  bindings: {
    type: '<',
    yearlyData: '<',
    blockSessionStorage: '<'
  },
  templateUrl: 'scripts/pages/project/funding/yearly-budget-capital-revenue-funding/yearlyBudgetCapitalRevenueFunding.html'
});
