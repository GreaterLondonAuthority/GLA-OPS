/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

import DateUtil from '../../../util/DateUtil';

const DEFAULT_NUMBER_PROGRAMMES_SHOWN = 3;
class RecoverableGrant {
  constructor($state, UserService, FeatureToggleService) {
    this.$state = $state;
    this.UserService = UserService;
    this.FeatureToggleService = FeatureToggleService;
  }

  $onInit(){
    this.showAll = false;
    this.showHowMany = DEFAULT_NUMBER_PROGRAMMES_SHOWN;
    this.FeatureToggleService.isFeatureEnabled('CreateAnnualReturn').then(resp => {
      this.showCreateAnnualReturnButton = resp.data;
    });

    let currentYear = DateUtil.getFinancialYear2(moment());
    let years = _.map(this.org.annualSubmissions, 'financialYear');
    this.canCreateNewAnnualSubmission = this.UserService.hasPermission('annual.submission.create', this.org.id) && this.remainingYears.length;
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

RecoverableGrant.$inject = ['$state', 'UserService', 'FeatureToggleService'];

angular.module('GLA')
  .component('recoverableGrant', {
    bindings: {
      org: '<',
      remainingYears: '<'
    },
    templateUrl: 'scripts/pages/organisation/recoverable-grant/recoverableGrant.html',
    controller: RecoverableGrant
  });
