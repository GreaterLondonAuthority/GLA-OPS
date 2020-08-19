/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

'use strict';

function PasswordModal($uibModal) {
  return {
    show: function () {
      return $uibModal.open({
        bindToController: true,
        controllerAs: '$ctrl',
        animation: false,
        templateUrl: 'scripts/components/password-strength/password-modal/passwordModal.html',
        size: 'md',
        controller: [function () {
        }]
      });
    }
  }
}

PasswordModal.$inject = ['$uibModal'];

angular.module('GLA').service('PasswordModal', PasswordModal);



