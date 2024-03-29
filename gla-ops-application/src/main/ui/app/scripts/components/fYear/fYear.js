/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */


const gla = angular.module('GLA');

function FYear() {
  return function (year) {
    return year + '/' + (++year).toString().substr(2,2)
  }
}

FYear.$inject = [];

angular.module('GLA').filter('fYear', FYear);
