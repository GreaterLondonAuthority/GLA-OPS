/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

import NumberUtil from '../../../../util/NumberUtil';
import DateUtil from '../../../../util/DateUtil';

class yearlyBudgetBalanceSummaryTableCtrl{
  constructor($injector, $scope, ProjectService, ProjectFundingService) {
  }

  $onInit() {

  }

  formatNumber(value) {
    return NumberUtil.formatWithCommas(value);
  }

  formatYear(value) {
    return DateUtil.toFinancialYearString(value);
  }

  spendTypeText(value) {
    return _.startCase(value.toLowerCase());
  }
}

yearlyBudgetBalanceSummaryTableCtrl.$inject = [];

angular.module('GLA')
  .component('yearlyBudgetBalanceSummaryTable', {
  controller: yearlyBudgetBalanceSummaryTableCtrl,
  bindings: {
    data: '<',
  },
  templateUrl: 'scripts/pages/project/funding/yearly-budget-balance-summary/yearlyBudgetBalanceSummaryTable.html'
});
