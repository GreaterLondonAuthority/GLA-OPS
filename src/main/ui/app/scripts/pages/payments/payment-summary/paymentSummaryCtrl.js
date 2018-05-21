/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

import './reclaim-modal/reclaimModal.js'

class PaymentSummaryCtrl {
  constructor($stateParams, UserService, ReclaimModal, PaymentService, $state, dateFilter, numberFilter, currencyFilter) {
    this.canViewSapVendorId = UserService.hasPermission('org.view.vendor.sap.id');
    this.PaymentService = PaymentService;
    this.dateFilter = dateFilter;
    this.numberFilter = numberFilter;
    this.currencyFilter = currencyFilter;
    this.$state = $state;
    this.payment = this.payment || _.find(this.paymentGroup.payments, {id: $stateParams.paymentId * 1});
    this.isDeclined = PaymentService.isDeclined(this.payment);
    this.isPending = PaymentService.isPending(this.payment);
    this.isAuthorised = PaymentService.isAuthorised(this.payment);
    this.source = this.payment.ledgerType === 'PAYMENT' ? 'Grant' : this.PaymentService.getPaymentSource(this.payment);
    this.statusText = (this.isPending ? 'Pending' : (this.isAuthorised ? 'Authorised' : 'Declined'));
    this.declineReason = this.paymentGroup.declineReason ? this.paymentGroup.declineReason.displayValue : null;
    this.declineComments = this.paymentGroup.declineComments;
    this.ReclaimModal = ReclaimModal;

    let totalReclaimAmount = (this.reclaims || []).reduce(
      (total, reclaim) => {
        if(reclaim.ledgerStatus !== 'Declined'){
          total += reclaim.value;
        }
        return total;
      }, 0
    );
    this.allowedReclaimAmount = this.payment.value + totalReclaimAmount;

    (this.reclaims || []).forEach(r => {
      r._description = this.historyMessage(r);
    });

    console.log('reclaims', this.reclaims, this.allowedReclaimAmount);
    this.showReclaimBtn = this.isReclaimEnabled &&
      UserService.hasPermission('payments.reclaim.create') &&
      this.isAuthorised &&
      this.project.status === 'Closed' &&
      this.allowedReclaimAmount > 0;
  }


  reclaim() {
    let modal = this.ReclaimModal.show(this.allowedReclaimAmount, this.source);
    modal.result.then((claimedAmount) => {
      return this.PaymentService.reclaim(this.payment.id, claimedAmount);
    }).then((data) => {
      this.$state.go('pending-payments', {
        'paymentId': data.id
      });
    });
  }


  historyMessage(p) {
    let actionName = null;
    let actionDate = null;
    let actionedBy = null;
    let reclaimTitle = null;
    let reclaimValue = this.currencyFilter(p.value, '£');

    if(this.PaymentService.isAuthorised(p)){
      actionName = 'authorised';
      actionDate = p.authorisedOn;
      actionedBy = p.authorisor;
      reclaimTitle = 'Reclaim';
    }else if(this.PaymentService.isDeclined(p)){
      actionName = 'declined';
      actionDate = p.modifiedOn;
      actionedBy = p.lastModifierName;
      reclaimTitle = 'Reclaim';
    }else if(this.PaymentService.isPending(p)){
      actionName = 'created';
      actionDate = p.createdOn;
      actionedBy = p.creator;
      reclaimTitle = 'Pending reclaim';
    }

    let formattedActionDate = this.dateFilter(actionDate, 'dd/MM/yyyy \'at\' HH:mm');

    return `${formattedActionDate} ${reclaimTitle} of ${reclaimValue} ${actionName} by ${actionedBy}`
  }
}

PaymentSummaryCtrl.$inject = ['$stateParams', 'UserService', 'ReclaimModal', 'PaymentService', '$state', 'dateFilter', 'numberFilter', 'currencyFilter'];


angular.module('GLA')
  .component('paymentSummary', {
    templateUrl: 'scripts/pages/payments/payment-summary/paymentSummary.html',
    bindings: {
      paymentGroup: '<',
      payment: '<',
      milestone: '<',
      project: '<',
      reclaims: '<',
      isReclaimEnabled: '<',
      originalPayment: '<'
    },
    controller: PaymentSummaryCtrl
  });
