/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

import ProjectBlockCtrl from '../ProjectBlockCtrl';

class FundingClaimsCtrl extends ProjectBlockCtrl {
  constructor($scope, $state, $log, $injector, $timeout, ProjectBlockService, ProjectSkillsService, UserService) {
    super($injector);
    this.$log = $log;
    this.$state = $state;
    this.$timeout = $timeout;
    this.ProjectBlockService = ProjectBlockService;
    this.ProjectSkillsService = ProjectSkillsService;
    this.UserService = UserService;
  }


  $onInit() {
    super.$onInit();
    let templateConfig = this.TemplateService.getBlockConfig(this.template, this.fundingClaims);
    this.labels = this.ProjectSkillsService.getLabels(templateConfig);
    this.isAebProcured = this.ProjectSkillsService.isAebProcured(this.learningGrant);
    this.isAebGrant = this.ProjectSkillsService.isAebGrant(this.learningGrant);

    this.showTotalAllocation = this.learningGrant.numberOfYears > 1;
    this.academicYearFrom = this.learningGrant.startYear;
    this.academicYearTo = this.learningGrant.startYear + this.learningGrant.numberOfYears - 1;
    this.showYearDropDown = this.academicYearFrom !== this.academicYearTo;

    this.currentAllocation = _.find(this.learningGrant.allocations || [], {year: this.getSelectedYear()});
    this.periods = templateConfig.periods;
    this.variation = this.fundingClaims.fundingClaimsVariations[0] || {};
    this.newAllocationText = templateConfig.newAllocationText || 'New total project allocation (£)';
    this.newAllocationRationaleText = templateConfig.newAllocationRationaleText || 'Please provide a brief rationale for your request';
    this.newAllocationInfoMessage = templateConfig.newAllocationInfoMessage || '';
    this.blockSessionStorage.showFundingClaims = this.blockSessionStorage.showFundingClaims != null ? this.blockSessionStorage.showFundingClaims : true;
    this.blockSessionStorage.showFundingVariations = this.blockSessionStorage.showFundingVariations != null ? this.blockSessionStorage.showFundingVariations : false;
    this.updateSelectedYear(this.getSelectedYear());


    if (this.blockSessionStorage.fundingClaimPeriodIndex == null) {
      this.blockSessionStorage.fundingClaimPeriodIndex = 0;
    }
    this.selectedPeriod = this.periods[this.blockSessionStorage.fundingClaimPeriodIndex];
  }

  back() {
    this.returnToOverview();
  }

  onSelectYear(year) {
    this.updateSelectedYear(year);
    this.refreshData();
  }

  onSelectFundingClaimPeriod(period) {
    this.blockSessionStorage.fundingClaimPeriodIndex = this.periods.indexOf(period);
  }

  refreshData() {
    return this.ProjectSkillsService.getFundingClaimsBlock(this.project.id)
      .then(resp => {
        this.fundingClaims = resp.data;
      });
  }

  autoSave() {
    this.onSaveData(false).then(() => {
      return this.refreshData();
    });
  }

  onFundingClaimsEntryChange(entry) {
    this.updatedFundingClaimsEntry = entry;
    this.autoSave();
  }

  onVariationRequestedChanged() {
    if (this.fundingClaims.variationRequested && !this.fundingClaims.fundingClaimsVariations.length) {
      this.fundingClaims.fundingClaimsVariations.push(this.variation);
    }
    if (!this.fundingClaims.variationRequested && this.fundingClaims.fundingClaimsVariations.length) {
      _.remove(this.fundingClaims.fundingClaimsVariations, this.variation);
    }

    console.log('this.fundingClaims.fundingClaimsVariations', this.fundingClaims.fundingClaimsVariations)
  }

  onSaveData(releaseLock) {
    let p = this.ProjectSkillsService.updateFundingClaimsBlockEntry(this.project.id, this.fundingClaims.id, this.updatedFundingClaimsEntry);
    return this.addToRequestsQueue(p);
  }

  getSelectedYear() {
    return this.blockSessionStorage.selectedYear || this.learningGrant.startYear;
  }

  updateSelectedYear(year) {
    this.blockSessionStorage.selectedYear = year;
    this.fromDateSelected = this.fromDateSelected || {};
    this.fromDateSelected.financialYear = year;
    this.currentAllocation = _.find(this.learningGrant.allocations || [], {year: year});
  }

  submit() {
    return this.$timeout(() => {
      return this.$q.all(this.requestsQueue).then(results => {
        return this.ProjectBlockService.updateBlock(this.project.id, this.fundingClaims.id, this.fundingClaims, true);
      });
    });
  }
}

ProjectBlockCtrl.$inject = ['$scope', '$state', '$log', '$injector', '$timeout', 'ProjectBlockService', 'ProjectSkillsService', 'UserService'];

angular.module('GLA')
  .component('fundingClaims', {
    controller: FundingClaimsCtrl,
    bindings: {
      project: '<',
      fundingClaims: '<',
      learningGrant: '<',
      template: '<',
      currentAcademicYear: '<'
    },
    templateUrl: 'scripts/pages/project/funding-claims/fundingClaims.html'
  });

