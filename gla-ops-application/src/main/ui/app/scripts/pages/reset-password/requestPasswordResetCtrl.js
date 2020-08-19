/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

'use strict';

RequestPasswordResetCtrl.$inject = ['$stateParams', '$scope', '$state', '$rootScope', 'UserService'];

function RequestPasswordResetCtrl($stateParams, $scope, $state, $rootScope, UserService) {
  var ctrl = this;

  $scope.reqResetForm = {};

  this.showReasonError = $stateParams.reasonError;

  this.isRequestSuccess = false;

  /**
   * Submit handler
   */
  this.onSubmit = function() {
    if($scope.reqResetForm.$valid) {
      UserService.requestPasswordReset($scope.formData.email)
        .then(function(resp) {
          this.isRequestSuccess = true;
        }.bind(this))
        .catch(function(err) {
          this.isRequestSuccess = true;
        }.bind(this));

      this.isRequestSuccess = true;
    }
  };

  /**
   * Cancel handler
   */
  this.onCancel = function() {
    $state.go('home');
  }
}

angular.module('GLA')
  .controller('RequestPasswordResetCtrl', RequestPasswordResetCtrl);
