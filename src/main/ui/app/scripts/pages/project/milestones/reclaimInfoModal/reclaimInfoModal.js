/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

function ReclaimInfoModal($uibModal) {
  return {
    show: function (milestone) {
      return $uibModal.open({
        bindToController: true,
        controllerAs: '$ctrl',
        animation: false,
        templateUrl: 'scripts/pages/project/milestones/reclaimInfoModal/reclaimInfoModal.html',
        size: 'md',

        controller: ['$uibModalInstance', function($uibModalInstance) {
          this.milestone = milestone;
        }]
      });
    }
  };
}

ReclaimInfoModal.$inject = ['$uibModal'];

angular.module('GLA')
  .service('ReclaimInfoModal', ReclaimInfoModal);
