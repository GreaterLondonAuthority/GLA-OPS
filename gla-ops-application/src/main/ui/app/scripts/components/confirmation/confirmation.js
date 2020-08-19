/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

class ConfirmationCtrl {
  constructor() {
  }
}

ConfirmationCtrl.$inject = ['$scope'];

angular.module('GLA')
  .component('confirmation', {
    bindings: {
      title: '@',
      text: '@',
    },
    transclude: true,
    templateUrl: 'scripts/components/confirmation/confirmation.html',
    controller: ConfirmationCtrl
  });
