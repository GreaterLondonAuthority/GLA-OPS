/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */


function ReclaimModal($uibModal) {
  return {
    show: function (allowedReclaimAmount, paymentSource) {
      return $uibModal.open({
        bindToController: true,
        controllerAs: '$ctrl',
        animation: false,
        templateUrl: 'scripts/pages/payments/payment-summary/reclaim-modal/reclaimModal.html',
        size: 'md',
        resolve: {},
        controller: [function () {
          this.source = paymentSource;
          this.allowedReclaimAmount = allowedReclaimAmount;
        }]
      });
    }
  };
}

ReclaimModal.$inject = ['$uibModal'];

angular.module('GLA')
  .service('ReclaimModal', ReclaimModal);
