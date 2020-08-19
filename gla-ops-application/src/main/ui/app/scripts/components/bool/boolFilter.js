/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

const gla = angular.module('GLA');


function BoolFilter() {
  return function (val) {
    if (_.isBoolean(val)) {
      return val ? 'Yes' : 'No'
    }
    return 'Not provided'
  }
}

BoolFilter.$inject = [];

angular.module('GLA').filter('bool', BoolFilter);
