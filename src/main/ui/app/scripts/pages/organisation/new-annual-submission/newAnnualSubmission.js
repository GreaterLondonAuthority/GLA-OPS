/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
import DateUtil from '../../../util/DateUtil';

class NewAnnualSubmissionCtrl {
  constructor($rootScope, $state, $stateParams, AnnualSubmissionService) {
    let envVars = $rootScope.envVars;
    this.selectedYear = null;
    this.$state = $state;
    this.AnnualSubmissionService = AnnualSubmissionService;
    this.orgId = $stateParams.orgId;
  }

  $onInit() {
  }

  createNew(){
    this.AnnualSubmissionService.createNewAnnualSubmission(
      this.orgId,
      this.selectedYear.financialYear
    ).then((resp)=>{
      let annualSubmissionId = resp.data.id;
      this.$state.go('annual-submission', {orgId: this.orgId, annualSubmissionId: annualSubmissionId})
    });
  }
}

NewAnnualSubmissionCtrl.$inject = ['$rootScope', '$state', '$stateParams', 'AnnualSubmissionService'];


angular.module('GLA')
  .component('newAnnualSubmission', {
    templateUrl: 'scripts/pages/organisation/new-annual-submission/newAnnualSubmission.html',
    bindings: {
      years : '<',
      organisation: '<',
      remainingYears: '<'
    },

    controller: NewAnnualSubmissionCtrl
  });
