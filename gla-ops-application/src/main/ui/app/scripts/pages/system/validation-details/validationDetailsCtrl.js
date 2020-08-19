/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */


class ValidationDetailsCtrl {
  constructor($state, ActuatorService) {
    this.$state = $state;
    this.ActuatorService = ActuatorService;
  }

  back() {
    this.$state.go('system');
  }
}

ValidationDetailsCtrl.$inject = ['$state', 'ActuatorService'];

angular.module('GLA')
  .component('validationDetailsPage', {
    templateUrl: 'scripts/pages/system/validation-details/validationDetails.html',
    bindings: {
      sysInfo: '<'
    },
    controller: ValidationDetailsCtrl
  });
