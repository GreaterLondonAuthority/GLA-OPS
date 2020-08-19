/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

function DeclinePaymentsDialog($uibModal, $timeout, _) {
  // const PAYMENT_OTHER_REASON_ID = 20;
  // const RECLAIM_OTHER_REASON_ID = 34;

  return {
    show(groupToDecline, paymentDeclineReason) {
      return $uibModal.open({
        bindToController: true,
        controllerAs: '$ctrl',
        animation: false,
        templateUrl: 'scripts/pages/payments/pending-payments/decline-dialog/declineDialog.html',
        size: 'confirm',
        controller: [
          function() {
            this.groupToDecline = groupToDecline;
            this.paymentDeclineReason = paymentDeclineReason;
            this.isReclaim = groupToDecline.payments[0].reclaim;

            // this.config = _.merge(defaultConfig, config);
            this.declinePayment = () => {
              if(!this.isFormValid()){
                return;
              }
              this.$close({
                id: this.groupToDecline.id,
                declineReason: angular.copy(this.declineReason),
                // {
                //   id: this.declineReason.id,
                //   catgory: this.declineReason.category,
                // },
                declineComments: this.declineComments
              });
            };


            this.isFormValid = () => {
              // let declineId = (this.declineReason || {}).id;
              // let isCommentRequired = (declineId === PAYMENT_OTHER_REASON_ID || declineId === RECLAIM_OTHER_REASON_ID);
              let declineReason = (this.declineReason || {}).displayValue;
              let isCommentRequired = (declineReason === 'Other');
              let declineId = (this.declineReason || {}).id;
              return declineId && (this.declineComments || !isCommentRequired);
            }
          }
        ]
      });
    }
  }
}

DeclinePaymentsDialog.$inject = ['$uibModal', '$timeout', '_'];

angular.module('GLA')
  .service('DeclinePaymentsDialog', DeclinePaymentsDialog);
