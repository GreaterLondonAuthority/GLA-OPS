/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

const gla = angular.module('GLA');

class MilestoneClaimStatus {
  constructor(ConfirmationDialog, ReclaimInfoModal) {
    this.ConfirmationDialog = ConfirmationDialog;
    this.ReclaimInfoModal = ReclaimInfoModal;
  }

  showReclaimInfoIcon(milestone) {
    return this.readOnly && (this.milestone.reclaimed || this.milestone.isReclaimed);
  }

  openReclaimInfoModal(milestone) {
    if (milestone.isReclaimed) {
      this.ReclaimInfoModal.show(milestone, this.milestoneType === 'MonetaryValue');
    } else if (milestone.reclaimed) {
      this.ConfirmationDialog.info('A repayment/reclaim has been made against this milestone.')
    }
  }
}

MilestoneClaimStatus.$inject = ['ConfirmationDialog', 'ReclaimInfoModal'];



gla.component('milestoneClaimStatus', {
  bindings: {
    milestone: '<',
    readOnly: '<',
    milestoneType: '<',
    openClaimMilestoneModal: '&'
  },
  controller: MilestoneClaimStatus,
  templateUrl: 'scripts/components/milestone-claim-status/milestoneClaimStatus.html'
});
