/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

import ProjectBlockCtrl from '../ProjectBlockCtrl';

const EntryType = {
  DELIVERY: 'DELIVERY',
  SUPPORT: 'SUPPORT'
};

class LearningGrantCtrl extends ProjectBlockCtrl {
  constructor($scope, $state, $log, $injector, $timeout, ProjectBlockService, ProjectSkillsService, UserService) {
    super($injector);
    this.$log = $log;
    this.$state = $state;
    this.$timeout = $timeout;
    this.ProjectBlockService = ProjectBlockService;
    this.ProjectSkillsService = ProjectSkillsService;
    this.UserService = UserService;
  }

  $onInit(){
    super.$onInit();
    let templateConfig = this.TemplateService.getBlockConfig(this.template, this.learningGrant);
    this.allocationTypesConfig = this.ProjectSkillsService.getAllocationTypesConfig(this.template);
    this.labels = this.ProjectSkillsService.getLabels(templateConfig);
    this.isAebProcured = this.ProjectSkillsService.isAebProcured(this.projectBlock);
    this.isAebGrant = this.ProjectSkillsService.isAebGrant(this.projectBlock);
    this.isAebNsct = this.ProjectSkillsService.isAebNsct(this.projectBlock);
    this.showSupportAllocation = this.ProjectSkillsService.showSupportAllocation(this.projectBlock);

    this.showTotalAllocation = this.learningGrant.numberOfYears > 1;
    this.academicYearFrom = this.learningGrant.startYear;
    this.academicYearTo = this.learningGrant.startYear + this.learningGrant.numberOfYears - 1;
    this.showYearDropDown = this.academicYearFrom !== this.academicYearTo;
    this.updateSelectedYear(this.getSelectedYear());
    this.transformData(this.learningGrant);
    this.ofWhich = {collapsed: this.readOnly};
  }

  back() {
    this.returnToOverview();
  }

  onSelectYear(year) {
    this.updateSelectedYear(year);
    this.refreshData();
  }

  canEditDeliveryAllocation() {
    return !this.readOnly && this.learningGrant.isDeliveryAllocationEditable;
  }

  canEditSupportAllocation() {
    let hasPendingClaims = _.some(this.supportEntries, e => e.claim && e.claim.claimStatus !== 'Approved');
    return !this.readOnly && !hasPendingClaims;
  }

  refreshData(){
    return this.ProjectSkillsService.getLearningGrantBlock(this.project.id, this.learningGrant.id)
      .then(resp => {
        this.learningGrant = resp.data;
        this.transformData(this.learningGrant);
      });
  }

  onSaveData(releaseLock) {
    let p = this.ProjectSkillsService.updateLearningGrantBlock(this.project.id, this.learningGrant.id, this.getSelectedYear(), this.learningGrant, releaseLock);
    return this.addToRequestsQueue(p);
  }

  autoSave() {
    this.onSaveData(false).then(() => {
      return this.refreshData();
    });
  }

  transformData(block){
    (block.learningGrantEntries || []).forEach(summary => {
      summary.claim = _.find(block.claims || [], function(o) { return o.entityId === summary.originalId && o.claimStatus !== 'Approved'});
    });
    this.learningGrant = block;

    let selectedYear = this.getSelectedYear();

    this.deliveryEntries = _.filter(block.learningGrantEntries, {type: EntryType.DELIVERY, academicYear: selectedYear});
    this.supportEntries = _.filter(block.learningGrantEntries, (s) => s.type === EntryType.SUPPORT && !!s.percentage && s.academicYear === selectedYear);

    this.currentDelivery = _.find(block.allocations || [], {year: selectedYear, type: 'Delivery'}) || {};
    this.currentCommunity = _.find(block.allocations || [], {year: selectedYear, type: 'Community'}) || {};
    this.currentInnovationFund = _.find(block.allocations || [], {year: selectedYear, type: 'InnovationFund'}) || {};
    this.currentResponseFundStrand1 = _.find(block.allocations || [], {year: selectedYear, type: 'ResponseFundStrand1'}) || {};
    this.currentNationalSkillsFund = _.find(block.allocations || [], {year: selectedYear, type: 'NationalSkillsFund'}) || {};
    this.currentLearningSupport = _.find(block.allocations || [], {year: selectedYear, type: 'LearnerSupport'}) || {};
  }

  getSelectedYear(){
    return this.blockSessionStorage.selectedYear || this.learningGrant.startYear;
  }

  updateSelectedYear(year){
    this.blockSessionStorage.selectedYear = year;
    this.fromDateSelected = this.fromDateSelected || {};
    this.fromDateSelected.financialYear = year;

    let yearDeliveryAllocations = _.filter(this.learningGrant.learningGrantEntries, (s) => s.academicYear === year && s.type === 'DELIVERY' && s.percentage);
    let yearSupportAllocations = _.filter(this.learningGrant.learningGrantEntries, (s) => s.academicYear === year && s.type === 'SUPPORT' && s.percentage);
    let yearGrantAllocations = _.filter(this.learningGrant.learningGrantEntries, (s) => s.academicYear === year && s.percentage);
    this.isSelectedYearPercentageMissing = (this.learningGrant.grantType === 'AEB_GRANT' || this.learningGrant.profileAllocationType && this.learningGrant.profileAllocationType === 'AEB_GRANT') ? (yearGrantAllocations === undefined || yearGrantAllocations.length == 0)
      : (yearDeliveryAllocations === undefined  || yearSupportAllocations === undefined || yearDeliveryAllocations.length == 0 || yearSupportAllocations == 0);
  }

  submit() {
    return this.$timeout(() => {
      return this.$q.all(this.requestsQueue).then(results => {
        return this.onSaveData(true);
      });
    });
  }
}

LearningGrantCtrl.$inject = ['$scope', '$state', '$log', '$injector', '$timeout', 'ProjectBlockService', 'ProjectSkillsService', 'UserService'];

angular.module('GLA')
  .component('learningGrant', {
    controller: LearningGrantCtrl,
    bindings: {
      project: '<',
      learningGrant: '<',
      paymentsEnabled: '<',
      template: '<',
      currentAcademicYear: '<'
    },
    templateUrl: 'scripts/pages/project/learning-grant/learningGrant.html'
  });

