/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

'use strict';

function ReclaimMilestoneModal($uibModal) {
  return {
    show: function (milestone, projectBlock) {
      return $uibModal.open({
        bindToController: true,
        controllerAs: '$ctrl',
        animation: false,
        templateUrl: 'scripts/pages/project/milestones/reclaimMilestoneModal/modal.html',
        size: 'md',
        resolve: {
          milestone: () => {
            return milestone;
          },
          projectBlock: () => {
            return projectBlock;
          }
        },

        controller: ['$uibModalInstance', 'MilestonesService', 'milestone', 'projectBlock', 'config', function ($uibModalInstance, MilestonesService, milestone, config) {
          var ctrl = this;

          ctrl.milestone = milestone;
          ctrl.projectBlock = projectBlock;

          ctrl.reclaimedRcgf = milestone.reclaimedRcgf;
          ctrl.rcgfUpTo = _.min([milestone.claimedRcgf || 0, projectBlock.availableToReclaimByType.RCGF + milestone.reclaimedRcgf]);
          ctrl.maxRcgf = _.max([ctrl.rcgfUpTo, milestone.reclaimedRcgf]);
          // ctrl.showReclaimRcgf = !(ctrl.milestone.claimedRcgf === 0 || (ctrl.reclaimedRcgf === 0 && ctrl.rcgfUpTo));
          ctrl.showReclaimRcgf = ctrl.maxRcgf !== 0;

          ctrl.reclaimedDpf = milestone.reclaimedDpf;
          ctrl.dpfUpTo = _.min([milestone.claimedDpf || 0, projectBlock.availableToReclaimByType.DPF + milestone.reclaimedDpf]);
          ctrl.maxDpf = _.max([ctrl.dpfUpTo, milestone.reclaimedDpf]);
          ctrl.showReclaimDpf = ctrl.maxDpf !== 0;

          ctrl.reclaimReason = milestone.reclaimReason;


          ctrl.claim = function(){
            var config = {
              reclaimedRcgf: ctrl.reclaimedRcgf,
              reclaimedDpf: ctrl.reclaimedDpf,
              reclaimReason: ctrl.reclaimReason
            };
            $uibModalInstance.close(config);
          };

          ctrl.cancelClaim = function(/*milestone*/){
            var config = {
            };
            $uibModalInstance.close(config);
          }
        }]
      });
    }
  };
}

ReclaimMilestoneModal.$inject = ['$uibModal'];

angular.module('GLA')
  .service('ReclaimMilestoneModal', ReclaimMilestoneModal);
