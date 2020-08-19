/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

import NumberUtil from '../../../util/NumberUtil';

class FinanceSummaryCtrl {
  constructor($scope) {
  }

  /**
   * Format number to string with comma's and append CR
   * @see `NumberUtil.formatWithCommasAndCR()`
   */
  formatNumber(value) {
    return NumberUtil.formatWithPoundAndCR(value);
  }
}

FinanceSummaryCtrl.$inject = ['$scope'];

angular
  .module('GLA')
  .component('financeSummary', {
    bindings: {
      summaryData: '<'
    },
    templateUrl: 'scripts/pages/project/project-budget/financeSummary.html',
    controller: FinanceSummaryCtrl
  });
