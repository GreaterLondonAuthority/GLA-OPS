/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

import FinancialYearCtrl from './financialYearCtrl.js';

var gla = angular.module('GLA');

gla.component('financialYear', {
  require: {
    // ngModel: 'ngModel'
  },
  bindings: {
    back: '<?',
    forward: '<?',
    from: '<?',
    to: '<?',
    selectedYear: '=',
    onSelect: '&?',
    currentFinancialYearConst: '<',
    populatedYears: '<'
  },
  controller: FinancialYearCtrl,
  templateUrl: 'scripts/components/financial-year/financialYear.html'
});
