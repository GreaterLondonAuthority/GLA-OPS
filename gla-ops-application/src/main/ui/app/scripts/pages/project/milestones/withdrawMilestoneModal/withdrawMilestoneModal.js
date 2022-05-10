/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

function WithdrawMilestoneModal($uibModal, MilestonesService) {
  return {
    show: function (milestone, viewOnly, actionsEnabled) {
      return $uibModal.open({
        bindToController: true,
        controllerAs: '$ctrl',
        animation: false,
        templateUrl: 'scripts/pages/project/milestones/withdrawMilestoneModal/withdrawMilestoneModal.html',
        size: 'md',


        controller: ['$uibModalInstance', function($uibModalInstance) {
          var ctrl = this;
          this.actionsEnabled = actionsEnabled;
          this.viewOnly = viewOnly;
          this.milestone = milestone;
          ctrl.withdrawalReason = '';

          ctrl.cancelClaim = function(){
            var config = {
              withdrawalReason: ctrl.withdrawalReason,
            };
            $uibModalInstance.close(config);
          };

          ctrl.cancelWithdraw = function(){
            var config = {
              canclled: true,
            };
            $uibModalInstance.close(config);
          };
        }
        ]
      });
    }
  };
}

WithdrawMilestoneModal.$inject = ['$uibModal', 'MilestonesService'];

angular.module('GLA')
  .service('WithdrawMilestoneModal', WithdrawMilestoneModal);
