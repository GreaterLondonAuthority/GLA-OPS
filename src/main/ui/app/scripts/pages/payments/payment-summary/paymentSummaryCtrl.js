/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

import './reclaim-modal/reclaimModal.js'
import './resend-modal/resendModal.js'

class PaymentSummaryCtrl {
  constructor($stateParams, UserService, ReclaimModal, ResendModal, PaymentService, $state, dateFilter, numberFilter, currencyFilter, ConfirmationDialog, ToastrUtil, ErrorService) {
    this.$stateParams = $stateParams;
    this.UserService = UserService;
    this.ReclaimModal = ReclaimModal;
    this.ResendModal = ResendModal;
    this.PaymentService = PaymentService;
    this.$state = $state;
    this.dateFilter = dateFilter;
    this.numberFilter = numberFilter;
    this.currencyFilter = currencyFilter;
    this.ConfirmationDialog = ConfirmationDialog;
    this.ToastrUtil = ToastrUtil;
    this.ErrorService = ErrorService;
  }

  $onInit(){
    this.canViewSapVendorId = this.UserService.hasPermission('org.view.vendor.sap.id');
    this.payment = this.payment || _.find(this.paymentGroup.payments, {id: this.$stateParams.paymentId * 1});
    this.isDeclined = this.PaymentService.isDeclined(this.payment);
    this.isPending = this.PaymentService.isPending(this.payment);
    this.isAuthorised = this.PaymentService.isAuthorised(this.payment);
    this.source = this.PaymentService.getPaymentSource(this.payment);
    this.statusText = (this.isPending ? 'Pending' : (this.isAuthorised ? 'Authorised' : 'Declined'));
    this.declineReason = this.paymentGroup.declineReason ? this.paymentGroup.declineReason.displayValue : null;
    this.declineComments = this.paymentGroup.declineComments;

    this.resendable  = this.payment.resendable && this.UserService.hasPermission('payments.resend');
    let totalReclaimAmount = (this.reclaims || []).reduce(
      (total, reclaim) => {
        if(reclaim.ledgerStatus !== 'Declined' && !reclaim.interestPayment){
          total += reclaim.value;
        }
        return total;
      }, 0
    );
    this.allowedReclaimAmount = this.payment.value + totalReclaimAmount;

    (this.reclaims || []).forEach(r => {
      r._description = this.historyMessage(r);
    });

    this.showReclaimBtn = this.isReclaimEnabled &&
      this.UserService.hasPermission('payments.reclaim.create') &&
      this.isAuthorised &&
      this.project.statusType === 'Closed' &&
      this.allowedReclaimAmount > 0 && !this.payment.interestPayment;

    this.labels = {
      paymentTitle: this.getPaymentTitle(this.payment),
      reclaimValue: this.payment.interestPayment? 'Reclaim Interest:' : 'Reclaim Value:'
    };

    this.showMilestoneSection = this.payment.category !== 'Skills';
  }

  reclaim() {
    let modal = this.ReclaimModal.show(this.allowedReclaimAmount, this.source);
    modal.result.then((claimedAmount) => {
      return this.PaymentService.reclaim(this.payment.id, claimedAmount);
    }).then((data) => {
      this.$state.go('pending-payments', {
        'paymentId': data.id
      });
    }).catch(this.ErrorService.apiValidationHandler());
  }

  resendPayments() {
    let modal = this.ResendModal.show(this.payment);
    modal.result.then((wbsCode) => {
      return this.PaymentService.resend(this.payment.id, wbsCode);
    }).then((data) => {
      this.$state.go(this.$state.current, this.$stateParams, {reload: true});
      this.ToastrUtil.success(`Payment file sent`);
    }).catch(this.ErrorService.apiValidationHandler());
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

  getPaymentTitle(payment) {
    if (payment.reclaim) {
      return payment.interestPayment? 'Interest on Reclaim' : 'Reclaim Payment';
    } else {
      return 'Payment'
    }
  }
}

PaymentSummaryCtrl.$inject = ['$stateParams', 'UserService', 'ReclaimModal', 'ResendModal', 'PaymentService', '$state', 'dateFilter', 'numberFilter', 'currencyFilter', 'ConfirmationDialog', 'ToastrUtil', 'ErrorService'];


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
