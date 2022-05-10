/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

import ProjectBlockCtrl from '../ProjectBlockCtrl';

class FundingClaimsCtrl extends ProjectBlockCtrl {
  constructor($scope, $state, $log, $injector, $timeout, ProjectBlockService, ProjectSkillsService, UserService, ContractTypeChangeModal) {
    super($injector);
    this.$log = $log;
    this.$state = $state;
    this.$timeout = $timeout;
    this.ProjectBlockService = ProjectBlockService;
    this.ProjectSkillsService = ProjectSkillsService;
    this.UserService = UserService;
    this.ContractTypeChangeModal = ContractTypeChangeModal;
    this.editFundingColumn = true;
  }

  $onInit() {
    super.$onInit();
    this.templateConfig = this.TemplateService.getBlockConfig(this.template, this.fundingClaims);
    this.allocationTypesConfig = this.ProjectSkillsService.getAllocationTypesConfig(this.template);

    this.labels = this.ProjectSkillsService.getLabels(this.templateConfig);
    this.isAebProcured = this.ProjectSkillsService.isAebProcured(this.fundingClaims);
    this.isAebGrant = this.ProjectSkillsService.isAebGrant(this.fundingClaims);

    this.showTotalAllocation = this.fundingClaims.numberOfYears > 1;
    this.academicYearFrom = this.fundingClaims.startYear;
    this.academicYearTo = this.fundingClaims.startYear + this.fundingClaims.numberOfYears - 1;
    this.showYearDropDown = this.academicYearFrom !== this.academicYearTo;

    this.currentDelivery = _.find(this.fundingClaims.allocations || [], {year: this.getSelectedYear(), type: 'Delivery'});
    this.currentCommunity = _.find(this.fundingClaims.allocations || [], {year: this.getSelectedYear(), type: 'Community'});
    this.currentInnovationFund = _.find(this.fundingClaims.allocations || [], {year: this.getSelectedYear(), type: 'InnovationFund'});
    this.currentResponseFundStrand1 = _.find(this.fundingClaims.allocations || [], {year: this.getSelectedYear(), type: 'ResponseFundStrand1'});
    this.currentLearningSupport = _.find(this.fundingClaims.allocations || [], {year: this.getSelectedYear(), type: 'LearnerSupport'});
    this.ofWhich = {collapsed: false};

    this.periods = this.templateConfig.periods;
    this.variation = this.fundingClaims.fundingClaimsVariations[0] || {};
    this.newAllocationText = this.templateConfig.newAllocationText || 'New total project allocation (£)';
    this.newAllocationRationaleText = this.templateConfig.newAllocationRationaleText || 'Please provide a brief rationale for your request';
    this.newAllocationInfoMessage = this.templateConfig.newAllocationInfoMessage || '';
    this.blockSessionStorage.hideFundingClaims = this.blockSessionStorage.hideFundingClaims != null ? this.blockSessionStorage.hideFundingClaims : false;
    this.blockSessionStorage.showFundingVariations = this.blockSessionStorage.showFundingVariations != null ? this.blockSessionStorage.showFundingVariations : false;

    if (this.blockSessionStorage.fundingClaimPeriodIndex == null) {
      this.blockSessionStorage.fundingClaimPeriodIndex = 0;
    }
    this.selectedPeriod = this.periods[this.blockSessionStorage.fundingClaimPeriodIndex];
    this.updateSelectedYear(this.getSelectedYear());
    this.setPeriodEditable(this.selectedPeriod);
  }

  initFromBlock(block){
    this.fundingClaims = block;
    this.fundingClaims.contractTypes = this.ProjectSkillsService.getAllContractTypes(block, this.templateConfig);
    this.activeContractTypes = this.ProjectSkillsService.getActiveContractTypes(block, this.templateConfig);
    this.selectedYearEntries = _.filter(this.fundingClaims.fundingClaimsEntries, {period: this.selectedPeriod.period, academicYear: this.fromDateSelected.financialYear})
    this.selectedYearEntries = _.sortBy(this.selectedYearEntries, 'displayOrder');
  }

  setPeriodEditable(period) {
    this.editFundingColumn = period.period != 14;
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
    this.setPeriodEditable(period);
    this.initFromBlock(this.fundingClaims);
  }

  isSelectedContractType() {
    if (this.fundingClaims.contractTypes ) {
      return _.filter(this.fundingClaims.contractTypes, {selected : true}).length > 0;
    }
    return false;
  }

  refreshData() {
    return this.ProjectSkillsService.getFundingClaimsBlock(this.project.id).then(rsp => this.initFromBlock(rsp.data));
  }


  onFundingClaimsEntryChange(entry) {
    return this.$q.all(this.requestsQueue).then(results => {
      let p = this.ProjectSkillsService.updateFundingClaimsBlockEntry(this.project.id, this.fundingClaims.id, entry);
      p.then(rsp => this.refreshData());
      return this.addToRequestsQueue(p);
    });
  }

  onVariationRequestedChanged() {
    if (this.fundingClaims.variationRequested && !this.fundingClaims.fundingClaimsVariations.length) {
      this.fundingClaims.fundingClaimsVariations.push(this.variation);
    }
    if (!this.fundingClaims.variationRequested && this.fundingClaims.fundingClaimsVariations.length) {
      _.remove(this.fundingClaims.fundingClaimsVariations, this.variation);
    }
  }

  getSelectedYear() {
    return this.blockSessionStorage.selectedYear || this.fundingClaims.startYear;
  }

  updateSelectedYear(year) {
    this.blockSessionStorage.selectedYear = year;
    this.fromDateSelected = this.fromDateSelected || {};
    this.fromDateSelected.financialYear = year;

    this.currentDelivery = _.find(this.fundingClaims.allocations || [], {year: year, type: 'Delivery'});
    this.currentCommunity = _.find(this.fundingClaims.allocations || [], {year: year, type: 'Community'});
    this.currentInnovationFund = _.find(this.fundingClaims.allocations || [], {year: year, type: 'InnovationFund'});
    this.currentResponseFundStrand1 = _.find(this.fundingClaims.allocations || [], {year: year, type: 'ResponseFundStrand1'});
    this.currentLearningSupport = _.find(this.fundingClaims.allocations || [], {year: year, type: 'LearnerSupport'});

    this.initFromBlock(this.fundingClaims);
  }

  save(releaseLock){
    return this.$timeout(() => {
      let p = this.$q.all(this.requestsQueue).then(results => {
        return this.ProjectBlockService.updateBlock(this.project.id, this.fundingClaims.id, this.fundingClaims, !!releaseLock);
      }).then(rsp => this.initFromBlock(rsp.data));
      return this.addToRequestsQueue(p);
    });
  }

  onContractTypeChange(lot) {
    if (!lot.selected && lot.original === true) {
      let modal = this.ContractTypeChangeModal.show(lot);
      modal.result.then((action) => {
        if (action === 'cancel') {
          lot.selected = lot.original;
        } else {
          this.save();
        }
      });
    } else {
      this.save();
    }
  }

  id(text){
    return (text || '').toLowerCase().replace(/ /g, '-')
  }

  submit() {
    return this.save(true);
  }
}

FundingClaimsCtrl.$inject = ['$scope', '$state', '$log', '$injector', '$timeout', 'ProjectBlockService', 'ProjectSkillsService', 'UserService', 'ContractTypeChangeModal'];

angular.module('GLA')
  .component('fundingClaims', {
    controller: FundingClaimsCtrl,
    bindings: {
      project: '<',
      fundingClaims: '<',
      template: '<',
      currentAcademicYear: '<'
    },
    templateUrl: 'scripts/pages/project/funding-claims/fundingClaims.html'
  });

