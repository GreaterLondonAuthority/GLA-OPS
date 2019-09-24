/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

function VersionHistoryModal($uibModal) {
  return {
    show: function (versionHistory, project) {
      return $uibModal.open({
        bindToController: true,
        controllerAs: '$ctrl',
        animation: false,
        templateUrl: 'scripts/components/version-history-modal/versionHistoryModal.html',
        size: 'lg',
        controller: [function () {
          this.versionHistory = versionHistory || [];
          this.autoApproval = !project.stateModel.approvalRequired;
          this.actionedByTitle = this.autoApproval ? 'Saved by' : 'Actioned by';
          this.versionText = (historyItem, isFirstItem) => {

            if (this.autoApproval) {
              return isFirstItem ? 'Current version' : `Version ${historyItem.blockVersion}`;
            } else if(historyItem.status === 'UNAPPROVED'){
              return 'Unapproved version';
            } else {
              return historyItem.approvedOnStatus? `${historyItem.approvedOnStatus} approved v${historyItem.blockVersion}` : `Approved v${historyItem.blockVersion}`;
            }
          }
        }]
      });
    }
  }


}

VersionHistoryModal.$inject = ['$uibModal'];

angular.module('GLA')
  .service('VersionHistoryModal', VersionHistoryModal);
