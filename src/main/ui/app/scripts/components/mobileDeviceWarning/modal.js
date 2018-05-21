/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

'use strict';

function MobileDeviceWarning($uibModal, $timeout) {
  return {
    show: function (parentThis, errorMsg) {
      return $uibModal.open({
        bindToController: true,
        controllerAs: '$ctrl',
        animation: false,
        templateUrl: 'scripts/components/mobileDeviceWarning/modal.html',
        size: 'md',
        controller: ['$uibModalInstance', function ($uibModalInstance) {
        }]
      });
    }
  }
}

MobileDeviceWarning.$inject = ['$uibModal', '$timeout'];

angular.module('GLA')
  .service('MobileDeviceWarning', MobileDeviceWarning);
