/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */


class OrganisationRegistrationProgramme {
  constructor($state, SessionService, OrganisationService, $scope) {
    this.$state = $state;
    this.SessionService = SessionService;
    this.OrganisationService = OrganisationService;
    this.$scope = $scope;
  }

  $onInit() {
    this.registration = this.SessionService.getOrgRegistration() || {};
    this.registration.navigationCircles = this.registration.navigationCircles || this.OrganisationService.getNavigationCircles();
    this.currentPageCircle = this.registration.navigationCircles[0];
    this.registration.navigationCircles.forEach(nc => nc.active = false);
    this.currentPageCircle.active = true;
    this.$scope.$watch('$ctrl.registration.programme', programme => {
      this.currentPageCircle.completed = !!programme;
    });

    this.groupedProgrammes = _.groupBy(_.filter(this.programmes, p => !!p.managingOrganisationId), 'managingOrganisationId')
    this.managingOrganisationsWithProgrammes = [];

    this.managingOrganisations.forEach(org => {
      if(org.registrationAllowed) {
        let organisation = angular.copy(org);
        organisation.programmes = this.groupedProgrammes[org.id] || [];
        this.managingOrganisationsWithProgrammes.push(organisation);
      }
    });
  }

  onProgrammeSelect(programme, managingOrganisation){
    this.registration.programme = programme;
    this.registration.managingOrganisation = _.pick(managingOrganisation, ['id', 'name']);
    this.currentPageCircle.completed = true;
    this.next();
  }

  back(){
    this.$state.go('registration-type');
  }

  next(){
    this.SessionService.setOrgRegistration(this.registration);
    this.$state.go('organisation.registration-form');
  }
}

OrganisationRegistrationProgramme.$inject = ['$state', 'SessionService', 'OrganisationService', '$scope'];

angular.module('GLA')
  .component('organisationRegistrationProgrammePage', {
    controller: OrganisationRegistrationProgramme,
    bindings: {
      programmes: '<',
      managingOrganisations: '<'
    },
    templateUrl: 'scripts/pages/organisation-registration/organisation-registration-programme/organisationRegistrationProgramme.html'
  });

