/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

class TotalBoxCtrl {
  constructor($scope) {
  }
}

TotalBoxCtrl.$inject = ['$scope'];

angular.module('GLA')
  .component('totalBox', {
    bindings: {
      title: '<',
      description: '<',
      leftLabel: '<',
      leftValue: '<',
      rightLabel: '<',
      rightValue: '<',
      totalValue: '<',
      showTotalValue: '<'
    },
    templateUrl: 'scripts/components/total-box/totalBox.html',
    controller: TotalBoxCtrl
  });
