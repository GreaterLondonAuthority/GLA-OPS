/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

class LearningGrantTable {
  constructor(ProjectSkillsService, PaymentService, ClaimModal) {
    this.ProjectSkillsService = ProjectSkillsService;
    this.PaymentService = PaymentService;
    this.ClaimModal = ClaimModal;
  }

  $onInit() {
    this.paymentsExist = false;
    this.showColumns = {
      cumulativeEarnings: !this.isSupportAllocation,
      cumulativePayment: this.isAebProcured && !this.isSupportAllocation
    };

    let hiddenColumnCount = Object.keys(this.showColumns).filter(k => !this.showColumns[k]).length;
    this.visibleColumnCount = 8 - hiddenColumnCount;
    this.initialisePaymentData();
  }

  initialisePaymentData() {
    (this.data || []).forEach(entry => {
      if (!this.paymentsExist && entry.payments.length) {
        this.paymentsExist = true;
      }
      _.each(entry.payments, (payment) => {
        this.PaymentService.enrichPayment(payment);
      });
    });
  }

  $onChanges() {
    this.initialisePaymentData();
  }

  getScheduledPaymentStatus(entry) {
    if (entry.paymentStatus) {
      return entry.paymentStatus;
    }

    let scheduledPaymentStatus = null;

    if (entry) {
      scheduledPaymentStatus = 'Due ' + (moment(entry.paymentDate).format('DD/MM/YYYY'));
    }

    return scheduledPaymentStatus;
  }

  isEntryPaid(entry){
    return entry.payments && entry.payments.length > 0;
  }

  getToggleText() {
    return this.expandAll ? 'Expand all' : 'Collapse all';
  }

  collapseOrExpandRows() {
    (this.data || []).forEach(row => row.collapsed = !this.expandAll);
    this.expandAll = !this.expandAll;
  }

  onCollapseChange() {
    this.expandAll = !(_.some(this.data, e => !e.collapsed && e.payments && e.payments.length));
  }

  getStatusColumnText(entry) {
    if (!(this.isAebProcured || this.isAebGrant) || !entry) {
      return null;
    }

    if (entry.paymentStatus){
      return entry.paymentStatus;
    } else if (entry.claim) {
      return entry.claim.claimStatus;
    }

    if (!this.readOnly && entry.paymentDue) {
      return 'Claim';
    }

  }

  showStatusAsLink(entry){
    return this.getStatusColumnText(entry) === 'Claim'
      || this.getStatusColumnText(entry) === 'Claimed';
  }

  getPaymentType(payment) {
    if (payment.value < 0) {
      return 'Reclaim';
    }
    else if (payment.category.toUpperCase().indexOf('SUPPLEMENTARY') !== -1) {
      return 'Supplementary payment';
    }
    else {
      return 'Payment';
    }
  }

  showClaimModal(entry) {

    var title = '';
    if (entry.missedPayment) {
      title = `${!entry.claim ? '' : 'Claimed '} Missed Payment`;
    } else {
      title = `${this.isSupportAllocation ? 'Learner Support' : 'Delivery'} ${entry.claim ? entry.claim.claimStatus : 'Claim'} Payment`;
    }

    let config = {
      title: title,
      subtitle: `${this.ProjectSkillsService.monthName(entry.actualMonth)} (P${entry.period})`,
      claimableAmountTitle: 'Claim amount',
      claimableAmount: entry.paymentDue,
      isClaimed: !!entry.claim,
      readOnly: this.readOnly || (entry.claim || {}).claimStatus === 'Approved'
    };

    let claimRequest = {
      id: (entry.claim || {}).id,
      entityId: entry.originalId,
      projectId: this.project.id,
      blockId: this.blockId,
      year: entry.actualYear,
      claimTypePeriod: entry.actualMonth
    };

    let modal = this.ClaimModal.show(config, claimRequest);
    modal.result.then((result) => {
      let event = {event: entry};
      return (result === 'claim') ? this.onClaim(event) : this.onCancelClaim(event);
    });
  }

}

LearningGrantTable.$inject = ['ProjectSkillsService', 'PaymentService', 'ClaimModal'];

angular.module('GLA')
  .component('learningGrantTable', {
    controller: LearningGrantTable,
    bindings: {
      project: '<',
      blockId: '<',
      columns: '<',
      data: '<',
      isAebProcured: '<',
      isAebGrant: '<',
      isSupportAllocation: '<',
      readOnly: '<',
      claimable: '<',
      onClaim: '&',
      onCancelClaim: '&'
    },
    templateUrl: 'scripts/components/learning-grant-table/learningGrantTable.html'
  });

