/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

'use strict';

var gla = angular.module('GLA');

gla.component('totalGrant', {
  bindings: {
    total: '<total',
    title: '@',
    isUnit: '@'
  },
  templateUrl: 'scripts/pages/project/grant/total-grant/totalGrant.html',
});
