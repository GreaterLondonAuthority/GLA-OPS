/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */


class OrganisationRegistrationUser {
  constructor($state, SessionService, OrganisationService) {
    this.$state = $state;
    this.SessionService = SessionService;
    this.OrganisationService = OrganisationService;
  }

  $onInit() {
    this.registration = this.SessionService.getOrgRegistration() || {};
    this.registration.user = this.registration.user || {};
    this.registration.navigationCircles = this.registration.navigationCircles || this.OrganisationService.getNavigationCircles();
    this.registration.navigationCircles.forEach(nc => nc.active = false);
    this.currentPageCircle = this.registration.navigationCircles[2];
    this.currentPageCircle.active = true;
  }

  back(){
    this.$state.go('organisation.registration-form');
  }

  onUserFormValidityChange($event){
    this.isUserFormValid = $event.isFormValid;
    this.registration.user = $event.data;
    this.currentPageCircle.completed = this.isUserFormValid;
  }

  next(){
    this.errors = null;
    let data = this.OrganisationService.formModelToApiData(this.registration.org);
    data.userRegistration = this.registration.user;
    data.defaultProgrammeId = this.registration.programme? this.registration.programme.id : null;
    this.OrganisationService.createOrganisation(data).then(resp => {
      this.SessionService.setOrgRegistration(null);
      return this.$state.go('confirm-org-and-user-created');
    }).catch(err => {
      this.errors = {};
      err.data.errors.map(e => {
        this.errors[e.name] = e.description
      });
    })
  }
}

OrganisationRegistrationUser.$inject = ['$state', 'SessionService', 'OrganisationService'];

angular.module('GLA')
  .component('organisationRegistrationUserPage', {
    controller: OrganisationRegistrationUser,
    bindings: {
    },
    templateUrl: 'scripts/pages/organisation-registration/organisation-registration-user/organisationRegistrationUser.html'
  });

