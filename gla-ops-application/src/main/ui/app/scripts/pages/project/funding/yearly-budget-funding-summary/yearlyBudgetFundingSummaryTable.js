/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
import DateUtil from '../../../../util/DateUtil';

class yearlyBudgetFundingSummaryTableCtrl{
  constructor($injector, $scope, ProjectService, ProjectFundingService) {
  }

  $onInit(){
    this.addYearLabels(this.yearlyData);
  }

  addYearLabels(yearlyData) {
    _.forEach(yearlyData, (year) => {
      year['yearLabel'] = DateUtil.toFinancialYearString(_.toNumber(year.year));
    });
  }
}

yearlyBudgetFundingSummaryTableCtrl.$inject = [];

angular.module('GLA')
  .component('yearlyBudgetFundingSummaryTable', {
  controller: yearlyBudgetFundingSummaryTableCtrl,
  bindings: {
    totals: '<',
    yearlyData: '<',
    blockSessionStorage: '<',
    showCapitalGla: '<',
    showRevenueGla: '<',
    showCapitalOther: '<',
    showRevenueOther: '<',
    capClaimedLabel: '<',
    capOtherLabel: '<',
    revClaimedLabel: '<',
    revOtherLabel: '<',
  },
  templateUrl: 'scripts/pages/project/funding/yearly-budget-funding-summary/yearlyBudgetFundingSummaryTable.html'
});
