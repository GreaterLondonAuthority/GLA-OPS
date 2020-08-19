/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

'use strict';

PasswordResetCtrl.$inject = ['$rootScope', '$scope', '$element', '$state', '$stateParams', '$log', 'UserService'];

function PasswordResetCtrl($rootScope, $scope, $element, $state, $stateParams, $log, UserService, showError) {
  var ctrl = this;
  this.isTokenValid = true;

  $scope.resetForm = {};

  // check validity of token
  UserService.checkPasswordResetToken($stateParams.userId, $stateParams.token)
    .then(function(resp) {
      if(resp.status === 200) {
        this.isTokenValid = true;
      }
    }.bind(this))
    .catch(function(err) {
      if(err.status === 400 ||
        err.status === 404) {
        $state.go('request-password-reset', {
          reasonError: 'Sorry, the password link has expired. Please enter your email address to receive a new link.'
        });
      } else {
        $log.debug(err);
        $state.go('request-password-reset', {
          reasonError: 'Sorry, an error has occurred with your link. Please try to reset your password again.'
        });
      }
    });

  /**
   * Submit handler
   */
  this.onSubmit = function() {
    if($scope.resetForm.$valid) {
      var data = {
        id: $stateParams.userId,
        token: $stateParams.token,
        password: $scope.formData.password
      };

      UserService.updateResetPassword($scope.formData.email, data)
        .then(function(resp) {
          $state.go('home', {
            reasonSuccess: 'You password was changed successfully.'
          });
        })
        .catch(function(err) {
          ctrl.errors = {};
          err.data.errors.map(function(i) {
            ctrl.errors[i.name] = i.description
          });
          $log.error(err)
        })
    }
  };

  /**
   * Cancel handler
   */
  this.onCancel = function() {
    $state.go('home');
  };
}

angular.module('GLA')
  .controller('PasswordResetCtrl', PasswordResetCtrl);
