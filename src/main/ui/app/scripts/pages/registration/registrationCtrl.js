/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */


class RegistrationCtrl {
  constructor($log, UserService, $state, $rootScope) {
    this.$log = $log;
    this.UserService = UserService;
    this.$state = $state;
    this.$rootScope = $rootScope;
    this.isFormValid = false;
  }

  submit() {
    this.$rootScope.showGlobalLoadingMask = true;
    this.UserService.registerUser(this.regData).then(() => {
      this.$state.go('confirm-user-created');
    }).catch(error => {
      this.errors = {};
      error.data.errors.forEach(e => this.errors[e.name] = e.description);
      this.$log.log(this.errors);
    }).finally(() => {
      this.$rootScope.showGlobalLoadingMask = false;
    });
  }

  onFormValidityChange(event) {
    this.isFormValid = event.isFormValid;
    this.regData = event.data;
  }
}

RegistrationCtrl.$inject = ['$log', 'UserService', '$state', '$rootScope'];


angular.module('GLA')
  .controller('RegistrationCtrl', RegistrationCtrl);
