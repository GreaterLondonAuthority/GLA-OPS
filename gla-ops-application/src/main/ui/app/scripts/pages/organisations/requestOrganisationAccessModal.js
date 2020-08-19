/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

'use strict';

function RequestOrganisationAccessModal($uibModal, $timeout, OrganisationService) {
  return {
    show: function () {
      return $uibModal.open({
        bindToController: true,
        controllerAs: '$ctrl',
        animation: false,
        templateUrl: 'scripts/pages/organisations/requestOrganisationAccessModal.html',
        size: 'md',
        controller: [function () {
          var ctrl = this;

          $timeout(function(){
            ctrl.focusSummary = true;
          },100)
        }]
      });
    }
  }
}

RequestOrganisationAccessModal.$inject = ['$uibModal', '$timeout', 'OrganisationService'];

angular.module('GLA')
  .service('RequestOrganisationAccessModal', RequestOrganisationAccessModal);
