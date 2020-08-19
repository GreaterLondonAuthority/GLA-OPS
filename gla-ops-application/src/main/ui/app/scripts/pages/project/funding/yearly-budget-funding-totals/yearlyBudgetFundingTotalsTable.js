/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

class yearlyBudgetFundingTotalsTableCtrl{
  constructor($injector, $scope, ProjectService, ProjectFundingService) {
  }

  $onInit() {

  }
}

yearlyBudgetFundingTotalsTableCtrl.$inject = [];

angular.module('GLA')
  .component('yearlyBudgetFundingTotalsTable', {
  controller: yearlyBudgetFundingTotalsTableCtrl,
  bindings: {
    totals: '<',
    yearlyData: '<',
    totalBudget: '<',
    showCapitalGla: '<',
    showRevenueGla: '<',
    showCapitalOther: '<',
    showRevenueOther: '<',
    capClaimedLabel: '<',
    capOtherLabel: '<',
    revClaimedLabel: '<',
    revOtherLabel: '<',
  },
  templateUrl: 'scripts/pages/project/funding/yearly-budget-funding-totals/yearlyBudgetFundingTotalsTable.html'
});
