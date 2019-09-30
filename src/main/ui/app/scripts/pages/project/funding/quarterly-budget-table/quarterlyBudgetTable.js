/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

import '../funding-claim-modal/fundingClaimModal.js'

class quarterlyBudgetTableCtrl{
  constructor(ProjectFundingService, FundingClaimModal) {
    this.ProjectFundingService = ProjectFundingService;
    this.FundingClaimModal = FundingClaimModal;
  }

  $onInit() {
  }


  onActivityNameChange(activity){
    this.isActivityValid(activity);
  }
  onActivityNameBlur(activity){
    var newActivityName = activity.name;
    if (newActivityName !== this.oldActivityName) {
      this.onBlurActivityInput(activity);
    }
  }
  onActivityNameFocus(activity){
    this.oldActivityName = activity.name;
  }

  isActivityValid(activity){
    return this.ProjectFundingService.isActivityValid(activity);
  }
  onBlurActivityInput(activity) {
    this.isActivityValid(activity);
    if(activity.name){
      // activity.name = activity.name || '';
      this.onActivityUpdate({data: activity});
    } else {
      activity.name = activity.originalName;
    }
  }

  evidenceLinkText(activity) {
    let hasAttachments = !!((activity || {}).attachments || []).length;
    if (this.readOnly) {
      return hasAttachments ? 'View' : null;
    } else {
      return hasAttachments ? 'Edit' : 'Add'
    }
  }

  showClaimModal(section) {
    if(!section.totalCapitalValue && !section.totalRevenueValue){
      //This can happen because of the delay to api call to update values
      return;
    }

    let sections = this.yearBreakdown.mappedSections;

    let budget = {
      capital: this.annualBudget.capitalValue.value || 0,
      revenue: this.annualBudget.revenueValue.value || 0
    };
    budget.total = budget.capital + budget.revenue;

    let unclaimedGrant = {
      capital: (budget.capital - (_.sumBy(sections, _.property('claim.capitalGrant')) || 0)) || 0,
      revenue: (budget.revenue - (_.sumBy(sections, _.property('claim.revenueGrant')) || 0)) || 0
    };

   unclaimedGrant.total = unclaimedGrant.capital + unclaimedGrant.revenue;


    let modalData = {
      section: section,
      showCapitalGla: this.showCapitalGla,
      showRevenueGla: this.showRevenueGla,
      showCapitalOther: this.showCapitalOther,
      showRevenueOther: this.showRevenueOther,
      budget: budget,
      unclaimedGrant: unclaimedGrant,
      readOnly: this.readOnly
    };

    let modal = this.FundingClaimModal.show(modalData);
    modal.result.then(action => {
      if(action === 'claim'){
        this.onClaimQuarter({$event: section});
      } else if (action === 'cancel'){
        this.onCancelClaimedQuarter({ $event: section.claim});
      }

    });
  }

  isReadOnly(section){
    return this.readOnly || !!section.claim;
  }
}

quarterlyBudgetTableCtrl.$inject = ['ProjectFundingService', 'FundingClaimModal'];

angular.module('GLA')
  .component('quarterlyBudgetTable', {
  controller: quarterlyBudgetTableCtrl,
  bindings: {
    yearBreakdown: '<',
    blockSessionStorage: '<',
    readOnly: '<',
    onActivityUpdate: '&',
    onDeleteActivity: '&',
    onShowEvidenceModal: '&',
    onClaimQuarter: '&',
    onCancelClaimedQuarter: '&',
    hasMilestones: '<',
    hasCategories: '<',
    milestoneBlock: '<',
    allowActivityUpdate: '<',
    allowEvidenceUpload: '<',
    showCapitalGla: '<',
    showRevenueGla: '<',
    showCapitalOther: '<',
    showRevenueOther: '<',
    paymentsEnabled: '<',
    annualBudget: '<',
    capClaimedLabel: '<',
    capOtherLabel: '<',
    revClaimedLabel: '<',
    revOtherLabel: '<',
    monetaryValueScale: '<'
  },
  templateUrl: 'scripts/pages/project/funding/quarterly-budget-table/quarterlyBudgetTable.html'
});
