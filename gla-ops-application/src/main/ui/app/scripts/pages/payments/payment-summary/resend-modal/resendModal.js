/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

function ResendModal($uibModal) {
  return {



    show: function (payment) {

      return $uibModal.open({
        bindToController: true,
        controllerAs: '$ctrl',
        animation: false,
        templateUrl: 'scripts/pages/payments/payment-summary/resend-modal/resendModal.html',
        size: 'md',
        resolve: {},
        controller: [function () {
          this.payment = payment;
          this.response = {};
          this.response.wbsCode = payment.wbsCode;
          this.response.ceCode = payment.ceCode;
          this.response.companyName = payment.companyName;
          this.response.sapVendorId = payment.sapVendorId;
        }]
      });
    }
  };
}

ResendModal.$inject = ['$uibModal'];

angular.module('GLA')
  .service('ResendModal', ResendModal);
