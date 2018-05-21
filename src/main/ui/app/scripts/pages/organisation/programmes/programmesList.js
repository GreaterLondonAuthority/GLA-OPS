/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

const DEFAULT_NUMBER_PROGRAMMES_SHOWN = 3;
class ProgrammesList {
  constructor($state) {
    this.showAll = false;
    this.showHowMany = DEFAULT_NUMBER_PROGRAMMES_SHOWN;
    this.$state = $state;
  }

  showMoreLessProgrammes() {
    this.showAll = !this.showAll;
    this.showHowMany = this.showAll ? this.org.programmes.length : DEFAULT_NUMBER_PROGRAMMES_SHOWN;
  }

  goToOrganisationProgramme(organisationId, programme) {
    this.$state.go('organisation-programme', {
      organisationId: organisationId,
      programmeId: programme.id,
      organisation: this.org,
      programme: programme
    });
  }

}

ProgrammesList.$inject = ['$state'];

angular.module('GLA')
  .component('programmesList', {
    bindings: {
      org: '<',
      refreshDetails: '&'
    },
    templateUrl: 'scripts/pages/organisation/programmes/programmesList.html',
    controller: ProgrammesList
  });
