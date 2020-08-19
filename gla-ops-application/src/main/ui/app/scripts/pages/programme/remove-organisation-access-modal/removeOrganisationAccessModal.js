/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */


function RemoveOrganisationAccessModal($uibModal, OrganisationService) {

  return {
    show: function (organisation, orgName, templateName) {
      return $uibModal.open({
        bindToController: true,
        controllerAs: '$ctrl',
        animation: false,
        templateUrl: 'scripts/pages/programme/remove-organisation-access-modal/removeOrganisationAccessModal.html',
        size: 'md',
        resolve: {},
        controller: ['$uibModalInstance', function ($uibModalInstance) {
          this.templateName = templateName;
          this.organisationName = orgName;


            this.removeAccessToOrganisation = () => {
              this.$close(this.organisation)
            }
        }]
      });
    },

  };
}

RemoveOrganisationAccessModal.$inject = ['$uibModal', 'OrganisationService'];

angular.module('GLA').service('RemoveOrganisationAccessModal', RemoveOrganisationAccessModal);
