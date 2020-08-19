/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

function ReclaimInfoModal($uibModal, MilestonesService) {
  return {
    show: function (milestone, isMonetaryValue, showCancelReclaim) {
      return $uibModal.open({
        bindToController: true,
        controllerAs: '$ctrl',
        animation: false,
        templateUrl: 'scripts/pages/project/milestones/reclaimInfoModal/reclaimInfoModal.html',
        size: 'md',

        controller: ['$uibModalInstance', function($uibModalInstance) {
          this.milestone = milestone;
          this.isMonetaryValue = isMonetaryValue;
          this.showCancelReclaim = showCancelReclaim;

          this.cancelReclaim = function(/*milestone*/){
            $uibModalInstance.close({
              action: MilestonesService.claimActions.cancelReclaim
            });
          }
        }]
      });
    }
  };
}

ReclaimInfoModal.$inject = ['$uibModal', 'MilestonesService'];

angular.module('GLA')
  .service('ReclaimInfoModal', ReclaimInfoModal);
