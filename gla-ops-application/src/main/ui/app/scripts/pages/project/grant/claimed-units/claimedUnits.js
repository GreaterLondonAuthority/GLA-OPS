/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

'use strict';

var gla = angular.module('GLA');

gla.component('claimedUnits', {
  bindings: {
    startMilestone:'<',
    completionMilestone: '<',
    totalStartOnSite:'<',
    totalCompletion: '<',
    tenures: '<',
    total: '<',
    title: '@',
    isUnit: '@'
  },
  templateUrl: 'scripts/pages/project/grant/claimed-units/claimedUnits.html',
});
