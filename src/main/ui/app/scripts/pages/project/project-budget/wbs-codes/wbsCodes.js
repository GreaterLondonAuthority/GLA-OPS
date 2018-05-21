/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

import WbsCodesCtrl from './wbsCodesCtrl.js';

var gla = angular.module('GLA');

gla.component('wbsCodes', {
  bindings: {
    codes: '<',
    type: '<?',
    max: '<?',
    readOnly: '<?',
    onWbsCodeModification: '&'
  },
  controller: WbsCodesCtrl,
  templateUrl: 'scripts/pages/project/project-budget/wbs-codes/wbsCodes.html'
});
