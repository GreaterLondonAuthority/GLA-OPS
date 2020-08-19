/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

const gla = angular.module('GLA');


function AbsFilter() {
  return function (val) {
    return Math.abs(val);
  }
}

AbsFilter.$inject = [];

angular.module('GLA').filter('abs', AbsFilter);
