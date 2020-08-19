/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

'use strict';

import './password-strength-explanation/passwordStrengthExplanation.js';
import './password-modal/passwordModal.js';

var gla = angular.module('GLA');


gla.component('passwordStrength', {
  bindings: {
    password: '<password',
    isValid: '='
  },
  templateUrl: 'scripts/components/password-strength/passwordStrength.html',
  controller: PasswordStrengthCtrl
});


function PasswordStrengthCtrl(UserService, _, $timeout) {
  var ctrl = this;
  ctrl.ngClass = {};

  this.isValid = false;

  var config = {
    '0': {
      name: 'Weak',
      cls: 'password-strength-weak'
    },
    '1': {
      name: 'Fair',
      cls: 'password-strength-fair'
    },
    '2': {
      name: 'Good',
      cls: 'password-strength-good'
    },
    '3': {
      name: 'Strong',
      cls: 'password-strength-strong'
    },
    '4': {
      name: 'Very strong',
      cls: 'password-strength-very-strong'
    }
  };

  var lastRequestId = 0;

  this.$onChanges = function (changedParams) {
    ctrl.isValid = false;
    var requestId = ++lastRequestId;
    var password = _.get(changedParams, 'password.currentValue');

    //Show indication that password is being checked if it takes longer than 2 seconds
    var showLoadingIndicator = $timeout(function(){
      if(lastRequestId !== 0) {
        ctrl.strength = {name: 'checking...'}
      }
    }, 2000);

    if (password) {
      UserService.passwordStrength(password).then(function (rsp) {
        if(lastRequestId !== 0 && requestId == lastRequestId) {
          ctrl.strength = config[rsp.data];
          ctrl.isValid = +rsp.data > 1;
        }
        $timeout.cancel(showLoadingIndicator);
      });
    } else {
      lastRequestId = 0; //Cancel pending requests
      ctrl.strength = null;
      ctrl.isValid = false;
      $timeout.cancel(showLoadingIndicator);
    }
  };
}

PasswordStrengthCtrl.$inject = ['UserService', '_', '$timeout'];
