/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */


function GiveOrganisationAccessModal($uibModal, OrganisationService) {

  return {
    show: function (organisation, organisationsWithAccess, templateName, managingOrganisationId) {
      return $uibModal.open({
        bindToController: true,
        controllerAs: '$ctrl',
        animation: false,
        templateUrl: 'scripts/pages/programme/give-organisation-access-modal/giveOrganisationAccessModal.html',
        size: 'md',
        resolve: {},
        controller: ['$uibModalInstance', function ($uibModalInstance) {
          this.organisation = angular.copy(organisation || {});
          this.templateName = templateName;
          this.organisationsWithAccess = organisationsWithAccess;
          this.managingOrganisationId = managingOrganisationId;

          this.validateOrganisation = (orgId) => {
            return _.some(this.organisationsWithAccess, (organisation) => {
              return organisation.organisationId == orgId;
            });
          },
          this.validateMO = (orgId) => {
            return this.managingOrganisationId == orgId;
          },
          this.onOrganisationIdChange = (organisationId) => {
              if (organisationId) {
                this.loading = true;
                OrganisationService.getDetails(organisationId).then(rsp => {
                  this.organisation = rsp.data;
                  this.organisationExists = true;
                  this.isProgrammeMO = this.validateMO(organisationId);
                  this.isDuplicateOrganisation = this.validateOrganisation(organisationId);
                }).catch(err => {
                  this.organisationExists = false;
                }).finally(() => {
                  this.loading = false;
                });
              }
              this.organisationExists = false;
            },

            this.giveAccessButtonEnabled = () => {
              return this.organisationExists && this.organisation.id && this.organisation.name && !this.isDuplicateOrganisation && !this.isProgrammeMO;
            },
            this.giveAccessToOrganisation = () => {
              this.$close(this.organisation)
            }
        }]
      });
    },

  };
}

GiveOrganisationAccessModal.$inject = ['$uibModal', 'OrganisationService'];

angular.module('GLA').service('GiveOrganisationAccessModal', GiveOrganisationAccessModal);
