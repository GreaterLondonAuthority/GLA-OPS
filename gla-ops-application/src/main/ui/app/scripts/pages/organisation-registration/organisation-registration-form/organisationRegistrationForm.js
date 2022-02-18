/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */


class OrganisationRegistrationForm {
  constructor($state, SessionService, OrganisationService, $scope) {
    this.$state = $state;
    this.SessionService = SessionService;
    this.OrganisationService = OrganisationService;
    this.$scope = $scope;
  }

  $onInit() {
    this.registration = this.SessionService.getOrgRegistration() || {};
    this.registration.org = this.registration.org || {
      address: {}
    };

    this.registration.navigationCircles = this.registration.navigationCircles || this.OrganisationService.getNavigationCircles();
    this.registration.navigationCircles.forEach(nc => nc.active = false);
    this.currentPageCircle = this.registration.navigationCircles[1];
    this.currentPageCircle.active = true;
  }

  onFormValidityChange(event){
      this.currentPageCircle.completed = event.isFormValid;
  }

  back(){
    this.SessionService.setOrgRegistration(this.registration);
    this.$state.go('organisation.registration-programme');
  }

  next(){
    this.SessionService.setOrgRegistration(this.registration);
    this.$state.go('organisation.registration-user');
  }
}

OrganisationRegistrationForm.$inject = ['$state', 'SessionService', 'OrganisationService', '$scope'];

angular.module('GLA')
  .component('organisationRegistrationFormPage', {
    controller: OrganisationRegistrationForm,
    bindings: {
      organisationTypes: '<',
      managingOrganisations: '<',
      legalStatuses: '<',
      organisationTemplates: '<'
    },
    templateUrl: 'scripts/pages/organisation-registration/organisation-registration-form/organisationRegistrationForm.html'
  });

