/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

import '../funding-all-activities-claim-modal/fundingAllActivitiesClaimModal.js'
import '../funding-activities-claim-modal/fundingActivitiesClaimModal.js'
import '../funding-claim-modal/fundingClaimModal.js'

class quarterlyBudgetTableCtrl{
  constructor(ProjectFundingService, FundingClaimModal, FundingActivitiesClaimModal, FundingAllActivitiesClaimModal, currencyFilter, ConfirmationDialog) {
    this.ProjectFundingService = ProjectFundingService;
    this.FundingClaimModal = FundingClaimModal;
    this.FundingActivitiesClaimModal = FundingActivitiesClaimModal;
    this.FundingAllActivitiesClaimModal = FundingAllActivitiesClaimModal;
    this.currencyFilter = currencyFilter;
    this.ConfirmationDialog = ConfirmationDialog;
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

  moreThanOneUnclaimed(section) {
    let noOfClaimableActivities = 0;
    if(section.activities != null && section.activities.length > 0) {
      section.activities.forEach(activity => {
        if (activity.status === 'Claimable') {
          noOfClaimableActivities++;
        }
      });
    }
    return ((section.activities.length - noOfClaimableActivities) > 1);
  }

  moreThanOneClaimed(section) {
    let noOfClaimedActivities = 0, noOfClaimableActivities =0;
    if(section.activities != null && section.activities.length > 0) {
      section.activities.forEach(activity => {
        if (activity.status === 'Claimed') {
          noOfClaimedActivities++;
        }
        else if (activity.status === 'Claimable') {
          noOfClaimableActivities++;
        }
      });
    }
    return (noOfClaimableActivities ==0 && noOfClaimedActivities >1);
  }
  canClaimActivities(section) {
    let claimable = false;
    if(section.activities != null && section.activities.length >= 2) {
      section.activities.forEach(activity => {
        if (activity.status === 'Claimable') {
          claimable = true;
        }
        if (activity.status === 'Claimed') {
          claimable = false;
        }
      });
    }
    return claimable;
  }

  showClaimModal(section, activity) {
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
      activity: activity,
      showCapitalGla: this.showCapitalGla,
      showRevenueGla: this.showRevenueGla,
      showCapitalOther: this.showCapitalOther,
      showRevenueOther: this.showRevenueOther,
      budget: budget,
      unclaimedGrant: unclaimedGrant,
      readOnly: this.readOnly
    };

    let modal = activity ? this.FundingActivitiesClaimModal.show(modalData) : this.FundingClaimModal.show(modalData);
    modal.result.then(action => {
      if(action === 'claim'){
        this.onClaimQuarter({$event: {section: section, activity: activity}});
      } else if (action === 'cancel'){
        this.onCancelClaimedQuarter({ $event: section.claim});
      }

    });
  }

  showClaimAllActivitiesModal(section, activity) {
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
      activity: activity,
      showCapitalGla: this.showCapitalGla,
      showRevenueGla: this.showRevenueGla,
      showCapitalOther: this.showCapitalOther,
      showRevenueOther: this.showRevenueOther,
      budget: budget,
      unclaimedGrant: unclaimedGrant,
      readOnly: this.readOnly
    };

    this.FundingAllActivitiesClaimModal.show(modalData).result.then(action => {
      if(action === 'claim'){
        this.onClaimQuarter({$event: {section: section, activity: activity}});
      } else if (action === 'cancel'){
        this.onCancelClaimedQuarter({ $event: section.claim});
      }
    });
  }

  cancelClaim(claim) {
    this.onCancelClaimedQuarter({$event: claim});
  }

  showCancelClaimModal(section, activity) {
    var modal = this.ConfirmationDialog.show({
      title: 'Cancel the ' + activity.name + ' for claim for ' + section.label + ' ' + section.year + '?',
      message: 'The cancelled activity will become Unclaimed.',
      dismissText: 'NO, KEEP CLAIM',
      approveText: 'YES, CANCEL CLAIM'

    });
    modal.result.then(() => {
      this.onCancelClaimedQuarter({$event: activity.claim});
    })
  }

  cancelAllActivitiesClaims(section) {
    var modal = this.ConfirmationDialog.show({
      title: 'Cancel the claim for ' + section.label + ' ' + section.year + '?',
      message: 'Cancelled activities will become Unclaimed.',
      approveText: 'YES, CANCEL CLAIM',
      dismissText: 'NO, KEEP CLAIM'
    });
    modal.result.then(() => {
      section.activities.forEach(act => {
        this.onCancelClaimedQuarter({$event: act.claim});
      })
    })
  }

  isReadOnly(section, activity){
    return this.readOnly || !!section.claim || (activity && !!activity.claim);
  }

  buildSectionClaimSummary(section) {
    let nbActivitiesClaimed = section.sectionClaimsSummary.nbActivitiesClaimed;
    let totalCapitalClaimed = section.sectionClaimsSummary.totalCapitalClaimed;
    let totalRevenueClaimed = section.sectionClaimsSummary.totalRevenueClaimed;
    let formattedTotalCapitalClaimed = this.currencyFilter(totalCapitalClaimed, '£', this.monetaryValueScale);
    let formattedTotalRevenueClaimed = this.currencyFilter(totalRevenueClaimed, '£', this.monetaryValueScale);

    if (totalCapitalClaimed && totalRevenueClaimed) {
      return 'Current claim for '+nbActivitiesClaimed+' activities with a GLA capital value of '+formattedTotalCapitalClaimed+' and a GLA revenue value of '+formattedTotalRevenueClaimed;
    }
    else if (totalCapitalClaimed) {
      return 'Current claim for '+nbActivitiesClaimed+' activities with a GLA capital value of '+formattedTotalCapitalClaimed;
    }
    else if (totalRevenueClaimed) {
      return 'Current claim for '+nbActivitiesClaimed+' activities with a GLA revenue value of '+formattedTotalRevenueClaimed;
    }
  }
}

quarterlyBudgetTableCtrl.$inject = ['ProjectFundingService', 'FundingClaimModal', 'FundingActivitiesClaimModal', 'FundingAllActivitiesClaimModal', 'currencyFilter', 'ConfirmationDialog'];

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
    monetaryValueScale: '<',
    canClaimActivity: '<'
  },
  templateUrl: 'scripts/pages/project/funding/quarterly-budget-table/quarterlyBudgetTable.html'
});
