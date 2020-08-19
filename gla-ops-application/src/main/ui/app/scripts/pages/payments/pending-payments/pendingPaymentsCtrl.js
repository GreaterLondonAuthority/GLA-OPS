/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

class PendingPaymentsCtrl {
  constructor($log, PaymentService, UserService, orderByFilter, $state, $stateParams, ToastrUtil, MessageModal, DeclinePaymentsDialog, $anchorScroll, $location, InterestDialog, ConfirmationDialog, NgbModal) {
    this.canViewSapVendorId = UserService.hasPermission('org.view.vendor.sap.id');
    this.PaymentService = PaymentService;
    this.orderByFilter = orderByFilter;
    this.$state = $state;
    this.$stateParams = $stateParams;
    this.UserService = UserService;
    this.ToastrUtil = ToastrUtil;
    this.MessageModal = MessageModal;
    this.DeclinePaymentsDialog = DeclinePaymentsDialog;
    this.$log = $log;
    this.$location = $location;
    this.$anchorScroll = $anchorScroll;
    this.$anchorScroll.yOffset = 45;
    this.InterestDialog = InterestDialog;
    this.ConfirmationDialog = ConfirmationDialog;
    this.NgbModal = NgbModal;
    this.canSetInterest = UserService.hasPermission('reclaim.payments.interest');
    this.username = UserService.currentUser().username;
  }

  $onInit() {
    this.isReverse = false;
    this.lastSortedColumn = null;
    this.canAuthoriseByManagingOrg = this.UserService.hasPermissionToAuthorise();
    this.sortBy('createdOn');
    let hash = this.$stateParams.paymentGroupId;
    if(!hash){
      hash = this.$stateParams.paymentId? `P${this.$stateParams.paymentId}` : null;
    }
    this.$location.hash(hash);
    this.$anchorScroll();
  }


  sortBy(columnName, maintainOrder) {

    // TODO update to sort inside group
    let isReverse = (!maintainOrder && this.lastSortedColumn === columnName ? this.isReverse : false);
    this.paymentGroups = this.orderByFilter(this.paymentGroups, [`this.payments[0].${columnName}`, 'this.payments[0].createdOn'], isReverse);
    this.lastSortedColumn = columnName;
    if(!maintainOrder){
      this.isReverse = !isReverse;
    }
  }

  showSapIdError(group) {

    let payment = _.find(group.payments, {paymentSourceDetails: {sendToSap: true}});
    if (payment && !payment.sapVendorId) {
      return true;
    }
    return false;
  }

  authorisePayments(groupToAuthorise) {
    console.log('groupToAuthorise', groupToAuthorise);
    this.PaymentService.authoriseGroup(groupToAuthorise.id).then(() =>{
      let paymentType = groupToAuthorise.payments[0].reclaim? 'Reclaim': 'Payment';
      this.ToastrUtil.success(`${paymentType} authorised`);
      _.remove(this.paymentGroups, group => group === groupToAuthorise);
    }).catch(err => {
      let description = 'Authorisation failed!';
      if (err && err.data && err.data.description) {
        description = err.data.description;
        let programmeId = this.getProgrammeIdForMissingRevenueWbsError(err);
        if(programmeId){
          description = `WBS Revenue code has not been provided. The WBS Revenue code must be added to the <a href="#programme/${programmeId}">programme associated template</a> by an OPS admin.`;
        }
      }
      this.ConfirmationDialog.warn(description);
    });
  }

  isMissingRevenueWbsCodeError(err){
    if (err && err.data && err.data.errors){
      let wbsError = _.find(err.data.errors, {name: 'REVENUE_WBS_CODE_MISSING'});
      if(wbsError){
        return true;
      }
    }
    return false;
  }

  getProgrammeIdForMissingRevenueWbsError(err){
    if(this.isMissingRevenueWbsCodeError(err)){
      let programmeField = _.find(err.data.errors, {description: 'programme id'});
      return (programmeField || {}).name;
    }
    return null;
  }

  declinePayments(groupToDecline) {
    let isReclaim = groupToDecline.payments[0].reclaim;
    let reasons = isReclaim? this.reclaimDeclineReason : this.paymentDeclineReason;

    let modal = this.DeclinePaymentsDialog.show(groupToDecline, reasons);
    modal.result.then((data) => {
      this.PaymentService.declineGroup(data.id, data).then(()=>{
        this.PaymentService.getPaymentGroups('PENDING').then((paymentGroups)=>{
          let paymentType = isReclaim? 'Reclaim': 'Payment';
          this.ToastrUtil.success(`${paymentType} declined`);
          this.paymentGroups = paymentGroups;
          this.sortBy(this.lastSortedColumn, true);
        });
      }).catch(err => {
        let errorMsg = 'Decline failed!';
        if (err && err.data && err.data.description) {
          errorMsg = err.data.description;
        }
        // `Payment cannot be authorised as the payment amount is greater than the remaining ${payment.source} balance`
        this.MessageModal.show({
          message: errorMsg
        });
        this.$log.error('failed:', err);
      });
    });
  }

  canAuthoriseGroup(group){
    let hasManagingOrganisationId = !! this.canAuthoriseByManagingOrg[group.payments[0].managingOrganisationId];
    return hasManagingOrganisationId;
  }

  /**
   * A user who tries to approve a payment requested by himself, won't be allowed to perform this action
   * @param {string} group A payment group needs to be approved
   * @returns {boolean} Returns true if is allow to authorise
   */
  isAllowToAuthorisePayments(group){
    let approvalRequestedBy = group.payments[0].modifiedBy ? group.payments[0].modifiedBy : group.payments[0].createdBy;
    let currentUsername = this.UserService.currentUser().username;
    return currentUsername !== approvalRequestedBy;
  }

  openInterestModal(group){
    // let interest = interest = group.payments[0].interest;


    let modal = this.InterestDialog.show(group);
    modal.result.then(intrests => {
      this.PaymentService.setInterests(intrests).then(()=>{
        this.ToastrUtil.success(`Interest updated`);
        this.$state.go(this.$state.current, this.$stateParams, {reload: true});
      });
    });
  }

  $onDestroy(){
    this.NgbModal.dismissAll();
  }
}

PendingPaymentsCtrl.$inject = ['$log', 'PaymentService', 'UserService', 'orderByFilter', '$state', '$stateParams', 'ToastrUtil', 'MessageModal', 'DeclinePaymentsDialog', '$anchorScroll', '$location', 'InterestDialog', 'ConfirmationDialog', 'NgbModal'];


angular.module('GLA')
  .component('pendingPayments', {
    templateUrl: 'scripts/pages/payments/pending-payments/pendingPayments.html',
    bindings: {
      paymentGroups: '<',
      paymentDeclineReason: '<',
      reclaimDeclineReason: '<'
    },
    controller: PendingPaymentsCtrl
  });
