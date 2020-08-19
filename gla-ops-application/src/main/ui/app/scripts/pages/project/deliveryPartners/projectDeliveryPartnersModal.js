/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

function ProjectDeliveryPartnerModal($uibModal) {
  return {
    show: function (deliveryPartner, templateConfig) {
      return $uibModal.open({
        bindToController: true,
        controllerAs: '$ctrl',
        animation: false,
        templateUrl: 'scripts/pages/project/deliveryPartners/projectDeliveryPartnersModal.html',
        size: 'md',
        resolve: {

        },
        controller: ['$uibModalInstance', function ($uibModalInstance) {
          this.deliveryPartner = angular.copy(deliveryPartner || {});
          this.templateConfig = templateConfig;
        }]
      });
    },

  };
}

ProjectDeliveryPartnerModal.$inject = ['$uibModal'];

angular.module('GLA')
.service('ProjectDeliveryPartnerModal', ProjectDeliveryPartnerModal);

