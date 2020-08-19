/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

'use strict';

function FileDeleteConfirmationModal($uibModal, $timeout) {
  return {
    show: function (parentThis, attachment) {
      return $uibModal.open({
        bindToController: true,
        controllerAs: '$ctrl',
        animation: false,
        templateUrl: 'scripts/pages/project/project-budget/fileDeleteConfirmationModal.html',
        size: 'md',
        controller: ['$uibModalInstance', function ($uibModalInstance) {
          var ctrl = this;
          ctrl.attachment = attachment;

          $timeout(function(){
            ctrl.focusSummary = true;
          },100)
        }]
      });
    }
  }
}

FileDeleteConfirmationModal.$inject = ['$uibModal', '$timeout'];

angular.module('GLA')
  .service('FileDeleteConfirmationModal', FileDeleteConfirmationModal);
