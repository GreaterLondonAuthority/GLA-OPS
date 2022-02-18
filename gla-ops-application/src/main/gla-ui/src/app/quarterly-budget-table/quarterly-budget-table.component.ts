import {Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges} from '@angular/core';
import {ProjectFundingService} from "../funding/project-funding.service";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {FundingActivitiesClaimModalComponent} from "../funding/funding-activities-claim-modal/funding-activities-claim-modal.component";
import {clone, filter, find, property, sumBy} from "lodash-es";
import {FundingClaimModalComponent} from "../funding/funding-claim-modal/funding-claim-modal.component";
import {ConfirmationDialogService} from "../shared/confirmation-dialog/confirmation-dialog.service";
import {FundingAllActivitiesClaimModalComponent} from "../funding/funding-all-activities-claim-modal/funding-all-activities-claim-modal.component";
import {DecimalPipe} from "@angular/common";
import {UserService} from '../user/user.service';
import {FundingActivitiesCancelModalComponent} from '../funding/funding-activities-cancel-modal/funding-activities-cancel-modal.component';
import {FeatureToggleService} from '../feature-toggle/feature-toggle.service';
import {FundingQuarterCancelModalComponent} from '../funding/funding-quarter-cancel-modal/funding-quarter-cancel-modal.component';

//TODO move to funding module
@Component({
  selector: 'gla-quarterly-budget-table',
  templateUrl: './quarterly-budget-table.component.html',
  styleUrls: ['./quarterly-budget-table.component.scss']
})
export class QuarterlyBudgetTableComponent implements OnInit, OnChanges {

  @Input() allProjectFunding: any
  @Input() blockSessionStorage: any
  @Input() readOnly: boolean

  @Input() blockId: number
  @Input() projectId: number
  @Input() hasMilestones: boolean
  @Input() hasCategories: boolean
  @Input() milestoneBlock: any
  @Input() allowActivityUpdate: boolean
  @Input() allowEvidenceUpload: boolean
  @Input() showCapitalGla: boolean
  @Input() showRevenueGla: boolean
  @Input() showCapitalOther: boolean
  @Input() showRevenueOther: boolean
  @Input() paymentsEnabled: boolean
  @Input() budgetSummaries: any
  @Input() capClaimedLabel: string
  @Input() capOtherLabel: string
  @Input() revClaimedLabel: string
  @Input() revOtherLabel: string
  @Input() monetaryValueScale: any
  @Input() canClaimActivity: boolean

  @Output() onActivityUpdate = new EventEmitter<any>()
  @Output() onDeleteActivity = new EventEmitter<any>()
  @Output() onShowEvidenceModal = new EventEmitter<any>()
  @Output() onClaimQuarter = new EventEmitter<any>()
  @Output() onCancelClaimedQuarter = new EventEmitter<any>()
  @Output() onAddEntry = new EventEmitter<any>()
  @Output() onPostApprovalClaimAction = new EventEmitter<any>()
  private oldActivityName: string;
  public yearlyData: any;
  public viewModes = [
    {
      name: 'activity-mode',
      label: 'All activity'
    },
    {
      name: 'quarter-mode',
      label: 'Quarterly totals'
    },
    {
      name: 'year-mode',
      label: 'Annual totals'
    }
  ];

  public selectedViewMode;
  digitInfo: string;
  cancelApprovedActivitiesFeature: boolean = false
  canCancelApprovedActivities: boolean = false
  hasReadOnlyPaymentActions: boolean = false

  constructor(private projectFundingService: ProjectFundingService,
              private ngbModal: NgbModal,
              private confirmationDialog: ConfirmationDialogService,
              private decimalPipe: DecimalPipe,
              private userService: UserService,
              private featureToggleService: FeatureToggleService) {
  }

  ngOnInit(): void {
    this.selectedViewMode = this.viewModes[0].name;
    if(!this.blockSessionStorage.toggleIcons){
      this.blockSessionStorage.toggleIcons = {};
    }
    this.digitInfo = this.monetaryValueScale != null? `1.${this.monetaryValueScale}-${this.monetaryValueScale}` : null;
    this.featureToggleService.isFeatureEnabled('CancelApprovedActivities').subscribe(enabled => {
      this.cancelApprovedActivitiesFeature = enabled
      if (enabled) {
        this.canCancelApprovedActivities = this.userService.hasPermission('payments.cancel.approval')
        this.hasReadOnlyPaymentActions = this.doExistReadOnlyActions()
      }
    })
  }

  ngOnChanges(changes: SimpleChanges): void {
    console.log('changes', changes);
  }

  doExistReadOnlyActions(): boolean {
    return this.canCancelApprovedActivities && this.doExistCancellablePayments()
  }

  doExistCancellablePayments(): boolean {
    for (let yearBreakdown of this.allProjectFunding.processedFundingByYear) {
      for (let section of yearBreakdown.mappedSections) {
        if (section.milestones) {
          for (let milestone of section.milestones) {
            for (let activity of milestone.activities) {
              if (activity.status === 'Paid') {
                return true
              }
            }
          }
        }
      }
    }
    return false
  }


  onActivityNameBlur(activity){
    this.isActivityValid(activity);
    var newActivityName = activity.name;
    if (newActivityName !== this.oldActivityName) {
      this.onBlurActivityInput(activity);
    }
  }
  onActivityNameFocus(activity){
    this.oldActivityName = activity.name;
  }

  isActivityValid(activity){
    //TODO doesn't return anything, just updates values
    return this.projectFundingService.isActivityValid(activity);
  }

  onBlurActivityInput(activity) {
    this.isActivityValid(activity);
    if(activity.name){
      // activity.name = activity.name || '';
      this.onActivityUpdate.emit(activity);
    } else {
      activity.name = activity.originalName;
    }
  }

  onEditEntry(activity) {
    activity.isEditWithModal = true;
    activity.allowActivityUpdate = this.allowActivityUpdate;
    this.onActivityUpdate.emit(activity);
  }

  evidenceLinkText(activity) {
    let hasAttachments = !!((activity || {}).attachments || []).length;
    if (this.readOnly) {
      return hasAttachments ? 'View Evidence' : null;
    } else {
      return hasAttachments ? 'Edit Evidence' : 'Add Evidence'
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

  showCancelPaymentModal(section: any, activity: any, isQuarter?: boolean) {
    const modal = isQuarter? this.ngbModal.open(FundingQuarterCancelModalComponent) : this.ngbModal.open(FundingActivitiesCancelModalComponent)
    modal.componentInstance.section = section
    modal.componentInstance.activity = activity;
    modal.componentInstance.showCapitalGla = this.showCapitalGla;
    modal.componentInstance.showRevenueGla = this.showRevenueGla;
    modal.componentInstance.showCapitalOther = this.showCapitalOther;
    modal.componentInstance.showRevenueOther = this.showRevenueOther;

    if (isQuarter) {
      let sections = (find(this.allProjectFunding.processedFundingByYear, {year: section.year}) || {}).mappedSections || [];
      let annualBudget = this.processBudgetSummariesFor(section.year)
      let budget = {
        capital: annualBudget.capitalValue.value || 0,
        revenue: annualBudget.revenueValue.value || 0
      } as any;
      budget.total = budget.capital + budget.revenue;
      let unclaimedGrant = {
        capital: (budget.capital - (sumBy(sections, property('claim.capitalGrant')) || 0)) || 0,
        revenue: (budget.revenue - (sumBy(sections, property('claim.revenueGrant')) || 0)) || 0
      } as any;
      unclaimedGrant.total = unclaimedGrant.capital + unclaimedGrant.revenue;

      modal.componentInstance.unclaimedGrant = unclaimedGrant
      modal.componentInstance.budget = budget
    }
    modal.result.then(result => {
      if (result.action === 'confirm') {
        this.onPostApprovalClaimAction.emit({claim: isQuarter? section.claim : activity.claim, action: 'cancel', reason: result.reason})
      }
    }, () => {})
  }

  showClaimModal(section, activity?) {
    if(!section.totalCapitalValue && !section.totalRevenueValue){
      //This can happen because of the delay to api call to update values
      return;
    }

    // let sections = this.yearBreakdown2.mappedSections;
    let sections = (find(this.allProjectFunding.processedFundingByYear, {year: section.year}) || {}).mappedSections || [];

    let annualBudget = this.processBudgetSummariesFor(section.year)
    let budget = {
      capital: annualBudget.capitalValue.value || 0,
      revenue: annualBudget.revenueValue.value || 0
    } as any;
    budget.total = budget.capital + budget.revenue;

    let unclaimedGrant = {
      capital: (budget.capital - (sumBy(sections, property('claim.capitalGrant')) || 0)) || 0,
      revenue: (budget.revenue - (sumBy(sections, property('claim.revenueGrant')) || 0)) || 0
    } as any;

    unclaimedGrant.total = unclaimedGrant.capital + unclaimedGrant.revenue;

    const modal = this.ngbModal.open(activity ? FundingActivitiesClaimModalComponent : FundingClaimModalComponent);
    modal.componentInstance.section = section;
    modal.componentInstance.activity = activity;
    modal.componentInstance.showCapitalGla = this.showCapitalGla;
    modal.componentInstance.showRevenueGla = this.showRevenueGla;
    modal.componentInstance.showCapitalOther = this.showCapitalOther;
    modal.componentInstance.showRevenueOther = this.showRevenueOther;
    modal.componentInstance.budget = budget;
    modal.componentInstance.unclaimedGrant = unclaimedGrant;
    modal.componentInstance.readOnly = this.readOnly;
    modal.componentInstance.canCancelApprovedActivities = this.canCancelApprovedActivities;
    modal.componentInstance.cancelApprovedActivitiesFeature = this.cancelApprovedActivitiesFeature

    modal.result.then((action) => {
        if(action === 'claim'){
          this.onClaimQuarter.emit({section: section, activity: activity});
        } else if (action === 'cancel'){
          this.onCancelClaimedQuarter.emit(section.claim);
        } else if (action == 'cancel-approved') {
          this.showCancelPaymentModal(section, activity, true)
        }
    }, ()=>{});
  }

  showClaimAllActivitiesModal(section, activity?) {
    if(!section.totalCapitalValue && !section.totalRevenueValue){
      //This can happen because of the delay to api call to update values
      return;
    }

    // let sections = this.yearBreakdown2.mappedSections;
    let sections = (find(this.allProjectFunding.processedFundingByYear, {year: section.year}) || {}).mappedSections || [];

    let annualBudget = this.processBudgetSummariesFor(section.year)
    let budget = {
      capital: annualBudget.capitalValue.value || 0,
      revenue: annualBudget.revenueValue.value || 0
    } as any;
    budget.total = budget.capital + budget.revenue;

    let unclaimedGrant = {
      capital: (budget.capital - (sumBy(sections, property('claim.capitalGrant')) || 0)) || 0,
      revenue: (budget.revenue - (sumBy(sections, property('claim.revenueGrant')) || 0)) || 0
    } as any;

    unclaimedGrant.total = unclaimedGrant.capital + unclaimedGrant.revenue;

    const modal = this.ngbModal.open(FundingAllActivitiesClaimModalComponent);
    modal.componentInstance.section = section;
    modal.componentInstance.activity = activity;
    modal.componentInstance.showCapitalGla = this.showCapitalGla;
    modal.componentInstance.showRevenueGla = this.showRevenueGla;
    modal.componentInstance.showCapitalOther = this.showCapitalOther;
    modal.componentInstance.showRevenueOther = this.showRevenueOther;
    modal.componentInstance.budget = budget;
    modal.componentInstance.unclaimedGrant = unclaimedGrant;
    modal.componentInstance.readOnly = this.readOnly;

    modal.result.then(action => {
      if(action === 'claim'){
        this.onClaimQuarter.emit({section: section, activity: activity});
      } else if (action === 'cancel'){
        this.onCancelClaimedQuarter.emit(section.claim);
      }
    });
  }

  cancelClaim(claim) {
    this.onCancelClaimedQuarter.emit(claim);
  }

  showCancelClaimModal(section, activity) {
    var modal = this.confirmationDialog.show({
      title: 'Cancel the ' + activity.name + ' for claim for ' + section.label + ' ' + section.year + '?',
      message: 'The cancelled activity will become Unclaimed.',
      dismissText: 'NO, KEEP CLAIM',
      approveText: 'YES, CANCEL CLAIM'

    });
    modal.result.then(() => {
      this.onCancelClaimedQuarter.emit(activity.claim);
    })
  }

  cancelAllActivitiesClaims(section) {
    let modal = this.confirmationDialog.show({
      title: 'Cancel the claim for ' + section.label + ' ' + section.year + '?',
      message: 'Cancelled activities will become Unclaimed.',
      approveText: 'YES, CANCEL CLAIM',
      dismissText: 'NO, KEEP CLAIM'
    });
    modal.result.then(() => {
      section.activities.forEach(act => {
        if(act.claim) {
          this.onCancelClaimedQuarter.emit(act.claim);
        }
      })
    })
  }

  isReadOnly(section, activity?){
    return this.readOnly || (!!section.claim && section.claim.claimStatus !== 'Withdrawn') || (activity && !!activity.claim && activity.claim.claimStatus !== 'Withdrawn');
  }

  activityCanBeDeleted(section, activity?) {
    return !this.readOnly && !section.claim && !(activity && !!activity.claim)
  }

  buildSectionClaimSummary(section) {
    let nbActivitiesClaimed = section.sectionClaimsSummary.nbActivitiesClaimed;
    let totalCapitalClaimed = section.sectionClaimsSummary.totalCapitalClaimed;
    let totalRevenueClaimed = section.sectionClaimsSummary.totalRevenueClaimed;
    let formattedTotalCapitalClaimed = this.decimalPipe.transform(totalCapitalClaimed, this.digitInfo);
    let formattedTotalRevenueClaimed = this.decimalPipe.transform(totalRevenueClaimed, this.digitInfo);

    if (totalCapitalClaimed && totalRevenueClaimed) {
      return 'Current claim for '+nbActivitiesClaimed+' activities with a GLA capital value of £'+formattedTotalCapitalClaimed+' and a GLA revenue value of £'+formattedTotalRevenueClaimed;
    }
    else if (totalCapitalClaimed) {
      return 'Current claim for '+nbActivitiesClaimed+' activities with a GLA capital value of £'+formattedTotalCapitalClaimed;
    }
    else if (totalRevenueClaimed) {
      return 'Current claim for '+nbActivitiesClaimed+' activities with a GLA revenue value of £'+formattedTotalRevenueClaimed;
    }
  }

  trackByKey(index: number, obj: any): string {
    return obj.id;
  };

  trackByYear(index: number, obj: any): string {
    return obj.year;
  };

  trackBySection(index: number, obj: any): string {
    return obj.label;
  };

  onViewModeChange(){
    this.blockSessionStorage.toggleIcons = {};
    this.allProjectFunding.processedFundingByYear.forEach((yb) => {
      yb.collapsed = (this.selectedViewMode === 'year-mode');
      yb.mappedSections.forEach(quarterSection => {
        quarterSection.collapsed = (this.selectedViewMode != 'activity-mode');
      });
    });
  }

  processBudgetSummariesFor(year) {
    let yearSummaryEntries = filter(this.budgetSummaries, {year:year});
    return {
      year: year,
      capitalValue: clone(find(yearSummaryEntries, {spendType: 'CAPITAL', matchFund: false, year: year})) || {
        blockId: this.blockId,
        ledgerType: 'BUDGET',
        projectId: this.projectId,
        spendType: 'CAPITAL',
        year: year
      },
      capitalMatchFundValue: clone(find(yearSummaryEntries, {spendType: 'CAPITAL', matchFund: true, year: year})) || {
        blockId: this.blockId,
        ledgerType: 'BUDGET',
        projectId: this.projectId,
        spendType: 'CAPITAL',
        category: 'MatchFund',
        year: year
      },
      revenueValue: clone(find(yearSummaryEntries, {spendType: 'REVENUE', matchFund: false, year: year})) || {
        blockId: this.blockId,
        ledgerType: 'BUDGET',
        projectId: this.projectId,
        spendType: 'REVENUE',
        year: year
      },
      revenueMatchFundValue: clone(find(yearSummaryEntries, {spendType: 'REVENUE', matchFund: true, year: year})) || {
        blockId: this.blockId,
        ledgerType: 'BUDGET',
        projectId: this.projectId,
        spendType: 'REVENUE',
        category: 'MatchFund',
        year: year
      }
    };
  }

  getPreviousYearItem(yearBreakdown: any, fundingTotalType: string) {
    if (!yearBreakdown.previousYearlyTotal) {
      return 'N/A'
    }
    return yearBreakdown.previousYearlyTotal[fundingTotalType].toLocaleString(undefined, {maximumFractionDigits: this.monetaryValueScale, minimumFractionDigits: this.monetaryValueScale})
  }
}

