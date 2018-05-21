/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

'use strict';

function ClaimMilestoneModal($uibModal) {
  return {
    // show: function (milestone, maxClaims, readOnly, grantValue, monetarySplitTitle, zeroGrantRequested, milestoneType, associatedProject) {
    show: function (milestone, config) {
      return $uibModal.open({
        bindToController: true,
        controllerAs: '$ctrl',
        animation: false,
        templateUrl: 'scripts/pages/project/milestones/claimMilestoneModal/modal.html',
        size: 'md',
        resolve: {
          milestone: () => {
            return milestone;
          },
          config: () => {
            return config;
          }
          // maxClaims: () => {
          //   return maxClaims;
          // },
          // readOnly: () => {
          //   return readOnly;
          // },
          // grantValue: () => {
          //   return grantValue;
          // },
          // monetarySplitTitle: () => {
          //   return monetarySplitTitle;
          // },
          // zeroGrantRequested: () => {
          //   return zeroGrantRequested;
          // },
          // milestoneType: () => {
          //   return milestoneType;
          // },
          // associatedProject: () => {
          //   return associatedProject;
          // }
        },

        // controller: ['$uibModalInstance', 'MilestonesService', 'milestone', 'maxClaims', 'readOnly', 'grantValue', 'monetarySplitTitle', 'zeroGrantRequested', 'milestoneType', function ($uibModalInstance, MilestonesService, milestone, maxClaims, readOnly, grantValue, monetarySplitTitle, zeroGrantRequested, milestoneType) {
        controller: ['$uibModalInstance', 'MilestonesService', 'milestone', 'config', function ($uibModalInstance, MilestonesService, milestone, config) {
          var ctrl = this;
          console.log('milestonel....', milestone);
          ctrl.isMonetaryValueType = config.milestoneType === 'MonetaryValue';
          ctrl.isMonetarySplitType = config.milestoneType === 'MonetarySplit';
          ctrl.isNonMonetarySplitType = config.milestoneType === 'NonMonetary';

          ctrl.milestone = milestone;
          ctrl.maxClaims = config.maxClaims || {};
          ctrl.readOnly = config.readOnly;
          ctrl.grantValue = config.grantValue;
          ctrl.zeroGrantRequested = config.zeroGrantRequested;
          ctrl.associatedProject = config.associatedProject;

          ctrl.isPending = ctrl.milestone.claimStatus === 'Pending';
          ctrl.isClaimed = ctrl.milestone.claimStatus === 'Claimed';
          ctrl.canClaim = ctrl.isPending && !ctrl.readOnly;
          ctrl.canCancel = ctrl.isClaimed && !ctrl.readOnly;

          ctrl.isPercentageMilestone = ctrl.milestone.monetary && ctrl.isMonetarySplitType && !ctrl.zeroGrantRequested && !ctrl.associatedProject;
          ctrl.isNonPercMonetaryMilestone = ctrl.milestone.monetary && ctrl.maxClaims.Grant > 0 && ctrl.isMonetaryValueType ;
          ctrl.isNonMonetaryMilestone = ctrl.maxClaims && (ctrl.maxClaims.RCGF || ctrl.maxClaims.DPF);

          ctrl.monetarySplitTitle = config.monetarySplitTitle;
          ctrl.grantName = `${config.monetarySplitTitle || ''} grant`.trim();

          ctrl.claimableGrant = (config.grantValue || 0) * milestone.monetarySplit/100;

          ctrl.claim = function(){
            let claimedGrant;
            if(ctrl.isPercentageMilestone){
              claimedGrant = ctrl.claimableGrant;
            } else if(ctrl.isNonPercMonetaryMilestone) {
              claimedGrant = ctrl.claimedGrant;
            }
            var config = {
              action: MilestonesService.claimActions.claim,
              claimedGrant: claimedGrant,
              claimedRcgf: ctrl.claimedRcgf,
              claimedDpf: ctrl.claimedDpf
            };
            $uibModalInstance.close(config);
          };

          ctrl.cancelClaim = function(/*milestone*/){
            var config = {
              action: MilestonesService.claimActions.cancel
            };
            $uibModalInstance.close(config);
          }
        }]
      });
    }
  };
}

ClaimMilestoneModal.$inject = ['$uibModal'];

angular.module('GLA')
  .service('ClaimMilestoneModal', ClaimMilestoneModal);
