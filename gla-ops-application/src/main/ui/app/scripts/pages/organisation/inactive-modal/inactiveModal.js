/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */


function OrganisationInactiveModal($uibModal) {
  return {
    show: function () {
      return $uibModal.open({
        bindToController: true,
        controllerAs: '$ctrl',
        animation: false,
        templateUrl: 'scripts/pages/organisation/inactive-modal/inactiveModal.html',
        size: 'md',
        controller: function () {
          this.inactivateReasons = [{
            value:'Duplicate',
            label: 'Duplicate',
            requiresOrg: true
          }, {
            value:'ApprovedInError',
            label: 'Approved in error'
          }, {
            value:'Other',
            label: 'Other',
            requiresComment: true
          }];

          this.onReasonChange = () => {
            this.duplicateOrgId = null;
            this.duplicateOrgName = null;
          }

        },
        resolve: {

        }
      });
    }
  };
}

OrganisationInactiveModal.$inject = ['$uibModal'];

angular.module('GLA')
  .service('OrganisationInactiveModal', OrganisationInactiveModal);
