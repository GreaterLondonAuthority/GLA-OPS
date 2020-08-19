/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */


function OrganisationRejectModal($uibModal) {
  return {
    show: function () {
      return $uibModal.open({
        bindToController: true,
        controllerAs: '$ctrl',
        animation: false,
        templateUrl: 'scripts/pages/organisation/rejectModal/modal.html',
        size: 'md',
        controller: function () {
          this.rejectionReasons = [{
            value:'Registered',
            label: 'Organisation is already registered'
          }, {
            value:'Ineligible',
            label: 'Organisation type is not eligible for funding under this department'
          }, {
            value:'UnableToVery',
            label: 'GLA were unable to verify organisation details'
          }, {
            value:'Other',
            label: 'Other',
            requiresComment: true
          }];

        },
        resolve: {

        }
      });
    }
  };
}

OrganisationRejectModal.$inject = ['$uibModal'];

angular.module('GLA')
  .service('OrganisationRejectModal', OrganisationRejectModal);
