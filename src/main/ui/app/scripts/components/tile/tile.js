/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

'use strict';

var gla = angular.module('GLA');



tileCtrl.$inject = ['$scope'];

gla.component('tile', {
  bindings: {
    items: '<?'
  },
  templateUrl: 'scripts/components/tile/tile.html',
  transclude: true,
  controller: tileCtrl
});

function tileCtrl($scope) {
  $scope.isNull = _.isNull;
}

