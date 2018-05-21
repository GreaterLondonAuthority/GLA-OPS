/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

'use strict';

var gla = angular.module('GLA');

gla.component('passwordStrengthExplanation', {
  bindings:{
    tabIndex: '@?'
  },
  templateUrl: 'scripts/components/password-strength/password-strength-explanation/passwordStrengthExplanation.html',
  controller: PasswordStrengthExplanationCtrl
});


function PasswordStrengthExplanationCtrl(PasswordModal) {
  this.showModal = function(){
    PasswordModal.show();
  }
}

PasswordStrengthExplanationCtrl.$inject = ['PasswordModal'];
