/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

import ProjectBlockCtrl from '../ProjectBlockCtrl';
import './funding-entry-modal/fundingEntryModal.js'

class FundingPageCtrl extends ProjectBlockCtrl {
  constructor($injector, $rootScope, ProjectService, ProjectFundingService, ReferenceDataService, FileUploadModal, orderByFilter, FundingEntryModal) {
    super($injector);
    this.FileUploadModal = FileUploadModal;
    this.ProjectService = ProjectService;
    this.ProjectFundingService = ProjectFundingService;
    this.ReferenceDataService = ReferenceDataService;
    this.orderByFilter = orderByFilter;
    this.FundingEntryModal = FundingEntryModal;
    this.$rootScope = $rootScope;
  }

  $onInit() {
    super.$onInit();
    this.monetaryValueScaleToUse = this.projectBlock.monetaryValueScale != null ? this.projectBlock.monetaryValueScale : 0;

    // Get configurable labels if exists, otherwise set to some default labels
    this.totalCapAvailableFunding = this.projectBlock.totalCapAvailableFunding
      ? this.projectBlock.totalCapAvailableFunding : 'Capital contribution you are requesting from GLA £';
    this.totalCapOtherFunding = this.projectBlock.totalCapOtherFunding
      ? this.projectBlock.totalCapOtherFunding : 'Capital applicant contribution you are providing £';
    this.totalRevAvailableFunding = this.projectBlock.totalRevAvailableFunding
      ? this.projectBlock.totalRevAvailableFunding : 'Revenue contribution you are requesting from GLA £';
    this.totalRevOtherFunding = this.projectBlock.totalRevOtherFunding
      ? this.projectBlock.totalRevOtherFunding : 'Revenue applicant contribution you are providing £';

    this.capClaimedFunding = this.projectBlock.capClaimedFunding ? this.projectBlock.capClaimedFunding : 'GLA CAPITAL CONTRIBUTION £';
    this.capOtherFunding = this.projectBlock.capOtherFunding ? this.projectBlock.capOtherFunding : 'APPLICANT CAPITAL CONTRIBUTION £';
    this.revClaimedFunding = this.projectBlock.revClaimedFunding ? this.projectBlock.revClaimedFunding : 'GLA REVENUE CONTRIBUTION £';
    this.revOtherFunding = this.projectBlock.revOtherFunding ? this.projectBlock.revOtherFunding : 'APPLICANT REVENUE CONTRIBUTION £';

    this.wizardClaimLabel = this.projectBlock.wizardClaimLabel ? this.projectBlock.wizardClaimLabel : 'GLA contribution £';
    this.wizardOtherLabel = this.projectBlock.wizardOtherLabel ? this.projectBlock.wizardOtherLabel : 'Applicant contribution £';


    this.showCapitalGLA = this.projectBlock.showCapitalGLAFunding
    this.showRevenueGLA = this.projectBlock.showRevenueGLAFunding

    this.showCapitalOther = this.projectBlock.showCapitalOtherFunding
    this.showRevenueOther = this.projectBlock.showRevenueOtherFunding

    this.hasCapital = this.showCapitalGLA || this.showCapitalOther;
    this.hasRevenue = this.showRevenueGLA || this.showRevenueOther;
    this.hasGLA = this.showCapitalGLA || this.showRevenueGLA;
    this.hasOther = this.showCapitalOther || this.showRevenueOther;
    this.canClaimActivity = this.projectBlock.canClaimActivity;

    if (this.hasGLA) {
      if (this.hasCapital && this.hasRevenue) {
        this.message = 'Enter the amount of GLA capital and/or revenue you are requesting for the year'
      } else if (this.hasCapital) {
        this.message = 'Enter the amount of GLA capital you are requesting for the year'
      } else {
        this.message = 'Enter the amount of GLA revenue you are requesting for the year'
      }
      if (this.hasOther) {
        this.message = this.message.concat(' and the amount of applicant funding being provided from other sources.');
      } else {
        this.message = this.message.concat('.');
      }

    } else {
      if (this.hasCapital && this.hasRevenue) {
        this.message = 'Enter the amount of capital and/or revenue applicant funding being provided.'
      } else if (this.hasCapital) {
        this.message = 'Enter the amount capital applicant funding being provided.'
      } else {
        this.message = 'Enter the amount revenue applicant funding being provided.'
      }
    }

    this.annualBudgetText = this.projectBlock.annualBudgetHelpText ? this.projectBlock.annualBudgetHelpText : this.message;

    this.totalBudgetViewOptions = [{
      label: 'Total budget',
      type: 'funding-totals'
    }, {
      label: 'Project funding summary',
      type: 'funding-summary'
    }, {
      label: 'Balance summary',
      type: 'balance-summary'
    }];
    if(this.hasCapital){
      this.totalBudgetViewOptions.push({
        label: 'Capital funding by Y & Q',
        type: 'capital-funding'
      });
    }
    if(this.hasRevenue){

      this.totalBudgetViewOptions.push({
        label: 'Revenue funding by Y & Q',
        type: 'revenue-funding'
      })
    }

    //   label: 'Quaterly claim summary',
    //   type: 'claim-summary'

    this.maxEvidenceAttachments = 2;

    this.selectedView = this.blockSessionStorage.selectedView || this.totalBudgetViewOptions[0];

    let fundingBlock = _.find(this.template.blocksEnabled, {block: 'Funding'});
    this.defaultActivityName = (fundingBlock && fundingBlock.defaultActivityName) ? fundingBlock.defaultActivityName : 'Projected cost';
    this.allowActivityUpdate = fundingBlock.multipleBespokeActivitiesEnabled;
    this.allowEvidenceUpload = fundingBlock.budgetEvidenceAttachmentEnabled;

    // this.projectFunding.yearlyData = this.ProjectFundingService.processBudgetSummaries(this.projectFunding.budgetSummaries);

    this.updateFundingBudgetSummaries(this.projectFunding);

    this.ProjectFundingService.addYearLabels(this.projectFunding.fundingTotalBudget.years);

    this.milestoneBlock = (_.find(this.project.projectBlocksSorted, {type: 'ProjectMilestonesBlock'})  || {});

    this.milestones = this.milestoneBlock.milestones || [];
    this.hasMilestones = this.milestones ? this.milestones && this.milestones.length : 0;

    if(this.projectBlock.categoriesExternalId) {
      this.ReferenceDataService.getConfigItemsByExternalId(this.projectBlock.categoriesExternalId).subscribe(categories => {
        this.categories = categories || [];
        this.hasCategories = this.categories ? this.categories && this.categories.length : 0;
      });
    }

    // let yearAvailableFrom = _.find(this.template.blocksEnabled, {block: 'Funding'}).yearAvailableFrom;

    // MSGLA-14 - Budget block allows users to enter an annual budget without selecting a year
    // let selectedYear = yearAvailableFrom > 0 ? this.currentFinancialYear + yearAvailableFrom : this.$stateParams.selectedYear || this.blockSessionStorage.selectedYear || this.currentFinancialYear;
    let selectedYear = this.$stateParams.selectedYear || this.blockSessionStorage.selectedYear || this.currentFinancialYear;

    if (selectedYear) {
      this.fromDateSelected = {
        financialYear: selectedYear
      }
    }
    this.selectYear(selectedYear);

    this.processSections(this.projectFunding);

    // this.projectFunding.yearAvailableFrom *= -1;
  }

  updateFundingBudgetSummaries(projectFunding){
    let filteredFundingBudgetSummaries;
    if(this.hasCapital && this.hasRevenue){
      filteredFundingBudgetSummaries = projectFunding.fundingBudgetSummaries;
    } else if(this.hasCapital){
      filteredFundingBudgetSummaries = _.filter(projectFunding.fundingBudgetSummaries, {spendType: 'CAPITAL'});
    } else if(this.hasRevenue){
      filteredFundingBudgetSummaries =_.filter(projectFunding.fundingBudgetSummaries, {spendType: 'REVENUE'});
    }

    // if(this.hasCapital && this.hasRevenue){
    //   filteredFundingBudgetSummaries = projectFunding.fundingBudgetSummaries;
    // } else if(this.hasGLA){
    //   if(this.hasCapital) {
    //     filteredFundingBudgetSummaries = _.filter(projectFunding.fundingBudgetSummaries, {spendType: 'CAPITAL', matchFund: false});
    //   } else {
    //     filteredFundingBudgetSummaries = _.filter(projectFunding.fundingBudgetSummaries, {spendType: 'REVENUE', matchFund: false});
    //   }
    // } else if(this.hasOther){
    //   if(this.hasCapital) {
    //     filteredFundingBudgetSummaries = _.filter(projectFunding.fundingBudgetSummaries, {spendType: 'CAPITAL', matchFund: true});
    //   } else {
    //     filteredFundingBudgetSummaries = _.filter(projectFunding.fundingBudgetSummaries, {spendType: 'REVENUE', matchFund: true});
    //   }
    // }

    this.sortedFundingBudgetSummaries = this.orderByFilter(filteredFundingBudgetSummaries, ['year', 'spendType']);
  }

  selectYear(year) {
    this.blockSessionStorage.selectedYear = year;

    let yearSummaryEntries = _.filter(this.projectFunding.budgetSummaries, {year:year});

    this.selectedYearData = {
      year: year,
      capitalValue: _.clone(_.find(yearSummaryEntries, {spendType: 'CAPITAL', matchFund: false, year: year})) || {
        blockId: this.blockId,
        ledgerType: 'BUDGET',
        projectId: this.project.id,
        spendType: 'CAPITAL',
        year: year
      },
      capitalMatchFundValue: _.clone(_.find(yearSummaryEntries, {spendType: 'CAPITAL', matchFund: true, year: year})) || {
        blockId: this.blockId,
        ledgerType: 'BUDGET',
        projectId: this.project.id,
        spendType: 'CAPITAL',
        category: 'MatchFund',
        year: year
      },
      revenueValue: _.clone(_.find(yearSummaryEntries, {spendType: 'REVENUE', matchFund: false, year: year})) || {
        blockId: this.blockId,
        ledgerType: 'BUDGET',
        projectId: this.project.id,
        spendType: 'REVENUE',
        year: year
      },
      revenueMatchFundValue: _.clone(_.find(yearSummaryEntries, {spendType: 'REVENUE', matchFund: true, year: year})) || {
        blockId: this.blockId,
        ledgerType: 'BUDGET',
        projectId: this.project.id,
        spendType: 'REVENUE',
        category: 'MatchFund',
        year: year
      }
    };
    // let orginalYear = _.find(this.projectFunding.yearlyData, {year: year});
    // this.selectedYearData = _.merge(defaultConfig, _.clone(orginalYear));

    // this.selectedYearData = _.clone(_.find(this.projectFunding.fundingTotalBudget.years, {year: year}))||{year:year};

    if(this.blockSessionStorage.jumpToEdit){
      this.blockSessionStorage.jumpToEdit = false;
      this.jumpTo('edit-jump-location')

    }
  }

  edit(){
    this.blockSessionStorage.jumpToEdit = true;
    super.edit();
  }

  changeSelectedView(selection) {
    this.blockSessionStorage.selectedView = selection;
  }

  onSelectYear(year) {
    this.$stateParams.selectedYear = year;
    this.$state.go(this.$state.current, this.$stateParams, {reload: true});
  }

  /**
   * Back
   */
  back(form) {
    if (this.readOnly || this.loading) {
      this.returnToOverview();
    } else {
      this.submit(form, false);
    }
  };

  /**
   * Submit
   */
  submit(form, validate) {
    return this.unlockBlock();
  }

  onBlurAnnualBudgetInput(entry) {


    let p;

    if (entry.value === '' || entry.value === undefined || entry.value === null) {
      if(entry.id){
        p = this.ProjectService.deleteLedgerEntry(this.project.id, this.blockId, entry.id);
      }
    } else {
      p = this.ProjectService.postLedgerEntry(this.project.id, this.blockId, entry);
    }
    if(p){
      p.then(() => {

        this.ProjectFundingService.getProjectFunding(this.project.id, this.blockId, this.blockSessionStorage.selectedYear).then((resp) => {
          this.projectFunding.budgetSummaries = resp.data.budgetSummaries;
          // this.projectFunding.yearlyData = this.ProjectFundingService.processBudgetSummaries(resp.data.budgetSummaries);
          this.projectFunding.fundingTotalBudget = resp.data.fundingTotalBudget;
          this.ProjectFundingService.addYearLabels(this.projectFunding.fundingTotalBudget.years);
          this.projectFunding.totals = resp.data.totals;
          this.projectFunding.validationFailures = resp.data.validationFailures;
          this.selectYear(this.blockSessionStorage.selectedYear);
          this.refreshPageData(resp.data);
          return resp.data;
        });
      });
    }
  }

  processSections(projectFunding) {
    projectFunding.yearBreakdown = projectFunding.yearBreakdown || {};
    projectFunding.yearBreakdown.year = projectFunding.yearBreakdown.year || this.blockSessionStorage.selectedYear;
    projectFunding.yearBreakdown.mappedSections = this.ProjectFundingService.getMappedSections(projectFunding.yearBreakdown, 4)
    projectFunding.fundingSummary.yearBreakdown = this.ProjectFundingService.mapProjectFundingSummary(projectFunding.populatedYears, projectFunding.fundingSummary.yearBreakdown);
    projectFunding.fundingByYearAndQuarter.years = this.ProjectFundingService.mapProjectFundingSummary(projectFunding.populatedYears, projectFunding.fundingByYearAndQuarter.years);
    this.refreshClaimErrors();
  }

  onAddQuarterlyBudget(data) {
    return this.ProjectFundingService.addQuarterlyEntry(this.project.id, this.blockId, data)
      .catch(this.ErrorService.apiValidationHandler());
  }

  onQuarterlyBudgetAdded(data) {
    // open record
    this.blockSessionStorage.sectionExpandState = this.blockSessionStorage.sectionExpandState || {};
    this.blockSessionStorage.fundingSummaryExpandState = this.blockSessionStorage.fundingSummaryExpandState || {};
    this.blockSessionStorage.capitalRevenueExpandState = this.blockSessionStorage.capitalRevenueExpandState || {};
    this.blockSessionStorage.sectionExpandState[data.year + '_' + data.quarter] = true;
    this.ProjectFundingService.getProjectFunding(this.project.id, this.blockId, this.blockSessionStorage.selectedYear).then((resp) => {
      this.refreshPageData(resp.data);
    });
  }

  onActivityUpdate(data){
    return this.onAddQuarterlyBudget(data).then(()=>{
      data.originalName = data.name;
      return this.ProjectFundingService.getProjectFunding(this.project.id, this.blockId, this.blockSessionStorage.selectedYear).then((resp)=>{
        this.refreshPageData(resp.data);
      });
    });
  }

  onDeleteActivity(section, milestone, activity){
    var modal = this.ConfirmationDialog.delete('Are you sure you want to delete the expenditure?');
    modal.result.then(resp => {
      return this.ProjectFundingService.deleteActivity(this.project.id, this.blockId, activity.id).then(()=>{
        return this.ProjectFundingService.getProjectFunding(this.project.id, this.blockId, this.blockSessionStorage.selectedYear).then((resp)=> {
          _.remove(milestone.activities, {id: activity.id});
          if(milestone.activities.length === 0){
            _.remove(section.milestones, milestone);
            section.hasMilestones = false;
          }
          this.refreshPageData(resp.data);
        });
      });
    });
  }

  refreshTotals(current, updated){
    updated = updated || {};
    current.total = updated.total;
    current.totalCapitalMatchFund = updated.totalCapitalMatchFund;
    current.totalCapitalValue = updated.totalCapitalValue;
    current.totalRevenueMatchFund = updated.totalRevenueMatchFund;
    current.totalRevenueValue = updated.totalRevenueValue;
  }

  refreshMappedSections(projectFunding){
    let currentMappedSection = this.projectFunding.yearBreakdown.mappedSections;
    projectFunding.yearBreakdown = projectFunding.yearBreakdown || {};
    projectFunding.yearBreakdown.year = projectFunding.yearBreakdown.year || this.blockSessionStorage.selectedYear;
    let updatedMappedSection = this.ProjectFundingService.getMappedSections(projectFunding.yearBreakdown, 4);
    for(let i = 0; i < 4; i++){
      _.merge(currentMappedSection[i], updatedMappedSection[i]);
      this.refreshTotals(currentMappedSection[i], updatedMappedSection[i]);
      _.forEach(currentMappedSection[i].milestones, currentMilestone => {
        let updatedMilestone = _.find(updatedMappedSection[i].milestones, {name: currentMilestone.name});
        this.refreshTotals(currentMilestone, updatedMilestone);
      });
    }


  }

  refreshClaimErrors(){
    this.claimErrors = [];
    this.projectFunding.yearBreakdown.mappedSections.forEach(section => {
      if(section.notClaimableReason){
        this.claimErrors.push(section.notClaimableReason);
      }
    });
    console.log('claim errors', this.claimErrors);
  }

  refreshYearBreakDownTotals(projectFunding){
    let yearBreakdown = projectFunding.yearBreakdown;
    this.refreshTotals(this.projectFunding.yearBreakdown, yearBreakdown);
    // this.projectFunding.yearBreakdown.total = yearBreakdown.total;
    // this.projectFunding.yearBreakdown.totalCapitalMatchFund = yearBreakdown.totalCapitalMatchFund;
    // this.projectFunding.yearBreakdown.totalCapitalValue = yearBreakdown.totalCapitalValue;
    // this.projectFunding.yearBreakdown.totalRevenueMatchFund = yearBreakdown.totalRevenueMatchFund;
    // this.projectFunding.yearBreakdown.totalRevenueValue = yearBreakdown.totalRevenueValue;

  }
  refreshPageData(projectFunding){

    this.refreshYearBreakDownTotals(projectFunding);
    this.refreshMappedSections(projectFunding);
    this.updateFundingBudgetSummaries(projectFunding);

    this.projectFunding.claimed = projectFunding.claimed;
    this.projectFunding.claims = projectFunding.claims;

    this.projectFunding.fundingByYearAndQuarter = projectFunding.fundingByYearAndQuarter;
    this.projectFunding.fundingSummary = projectFunding.fundingSummary;
    this.projectFunding.validationFailures = projectFunding.validationFailures;
    this.projectFunding.yearBreakdown = projectFunding.yearBreakdown;

    this.projectFunding.fundingByYearAndQuarter.years = this.ProjectFundingService.mapProjectFundingSummary(projectFunding.populatedYears, projectFunding.fundingByYearAndQuarter.years);

    this.ProjectFundingService.addYearLabels(projectFunding.fundingSummary.yearBreakdown);
    this.ProjectFundingService.addYearLabels(projectFunding.fundingByYearAndQuarter.years);
    this.processSections(projectFunding);
  }

  showEvidenceModal(activity) {
    activity.attachments = activity.attachments || [];

    let modalConfig = {
      orgId: this.project.organisation.id,
      readOnly: this.readOnly,
      fileIdColumn: 'fileId',
      attachments: activity.attachments,
      title: `Upload evidence for ${activity.name} activity`,
      maxEvidenceAttachments: this.maxEvidenceAttachments,

      onFileUploadComplete: (file) => {
        return this.ProjectFundingService.attachEvidence(this.project.id, this.projectBlock.id, activity.id, file.id).then(rsp => {
          activity.attachments = rsp.data;
          return activity.attachments;
        })
      },

      onDeleteFile: (file) => {
        return this.ProjectFundingService.deleteEvidence(this.project.id, this.projectBlock.id, activity.id, file.id).then(rsp => {
          _.remove(activity.attachments, {id: file.id});
          return activity.attachments;
        });
      }
    };

    let modal = this.FileUploadModal.show(modalConfig);
    modal.closed.then(()=>{
      return this.ProjectFundingService.getProjectFunding(this.project.id, this.projectBlock.id, this.blockSessionStorage.selectedYear).then((resp) => {
        return this.refreshPageData(resp.data);
      });
    })
  }

  onClaimQuarter(data){
    let activityIds = [];
    data.activity ? activityIds.push(data.activity.id) : data.section.activities.forEach(act => {
      if(act.status === 'Claimable') {
        activityIds.push(act.id)
      }
    });
    return this.ProjectFundingService.claim(this.project.id, this.projectBlock.id, data.section.year, data.section.quarter, activityIds).then(resp => {
      return this.ProjectFundingService.getProjectFunding(this.project.id, this.projectBlock.id, this.blockSessionStorage.selectedYear).then((resp) => {
        return this.refreshPageData(resp.data);
      });
    });
  }

  onCancelClaimedQuarter(claim){
    return this.ProjectFundingService.cancelClaim(this.project.id, this.projectBlock.id, claim.id).then(resp => {
      return this.ProjectFundingService.getProjectFunding(this.project.id, this.projectBlock.id, this.blockSessionStorage.selectedYear).then((resp) => {
        return this.refreshPageData(resp.data);
      });
    });
  }

  showFundingEntryModal(){
    let config = {
      year: this.fromDateSelected,
      milestones: this.milestones,
      showMilestones: this.projectBlock.showMilestones,
      categories: this.categories,
      showCategories: this.projectBlock.showCategories,
      allowActivityUpdate: this.allowActivityUpdate,
      defaultActivityName: this.defaultActivityName,
      showCapitalGla: this.showCapitalGLA,
      showRevenueGla: this.showRevenueGLA,
      showCapitalOther: this.showCapitalOther,
      showRevenueOther: this.showRevenueOther,
      wizardClaimLabel: this.wizardClaimLabel,
      wizardOtherLabel : this.wizardOtherLabel,
      monetaryValueScale: this.monetaryValueScaleToUse
    };
    let modal = this.FundingEntryModal.show(config);
    modal.result.then(data => {
      this.$rootScope.showGlobalLoadingMask = true;
      this.onAddQuarterlyBudget(data).then(rsp => {
        this.onQuarterlyBudgetAdded(rsp.data);
        this.$rootScope.showGlobalLoadingMask = false;
      })
    })
  }
}

FundingPageCtrl.$inject = ['$injector','$rootScope', 'ProjectService', 'ProjectFundingService', 'ReferenceDataService', 'FileUploadModal', 'orderByFilter', 'FundingEntryModal'];

angular.module('GLA')
  .component('projectFundingPage', {
  controller: FundingPageCtrl,
  bindings: {
    projectFunding: '<',
    project: '<',
    currentFinancialYear: '<',
    template: '<'
  },
  templateUrl: 'scripts/pages/project/funding/fundingPage.html'
});
