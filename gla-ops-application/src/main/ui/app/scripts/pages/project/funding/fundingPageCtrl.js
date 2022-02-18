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
  constructor($injector, $rootScope, ProjectService, ProjectFundingService, ReferenceDataService, FileUploadModal, orderByFilter, FundingEntryModal, $location, $anchorScroll) {
    super($injector);
    this.FileUploadModal = FileUploadModal;
    this.ProjectService = ProjectService;
    this.ProjectFundingService = ProjectFundingService;
    this.ReferenceDataService = ReferenceDataService;
    this.orderByFilter = orderByFilter;
    this.FundingEntryModal = FundingEntryModal;
    this.$rootScope = $rootScope;
    this.$location = $location
    this.$anchorScroll = $anchorScroll;
  }

  $onInit() {
    super.$onInit();
    this.monetaryValueScaleToUse = this.projectBlock.monetaryValueScale != null ? this.projectBlock.monetaryValueScale : 0;

    this.capClaimedFunding = this.projectBlock.capClaimedFunding ? this.projectBlock.capClaimedFunding : 'GLA CAPITAL CONTRIBUTION £';
    this.capOtherFunding = this.projectBlock.capOtherFunding ? this.projectBlock.capOtherFunding : 'APPLICANT CAPITAL CONTRIBUTION £';
    this.revClaimedFunding = this.projectBlock.revClaimedFunding ? this.projectBlock.revClaimedFunding : 'GLA REVENUE CONTRIBUTION £';
    this.revOtherFunding = this.projectBlock.revOtherFunding ? this.projectBlock.revOtherFunding : 'APPLICANT REVENUE CONTRIBUTION £';

    this.wizardClaimLabel = this.projectBlock.wizardClaimLabel ? this.projectBlock.wizardClaimLabel : 'GLA Contribution £';
    this.wizardOtherLabel = this.projectBlock.wizardOtherLabel ? this.projectBlock.wizardOtherLabel : 'Applicant Contribution £';

    this.showCapitalGLA = this.projectBlock.showCapitalGLAFunding
    this.showRevenueGLA = this.projectBlock.showRevenueGLAFunding

    this.showCapitalOther = this.projectBlock.showCapitalOtherFunding
    this.showRevenueOther = this.projectBlock.showRevenueOtherFunding

    this.hasCapital = this.showCapitalGLA || this.showCapitalOther;
    this.hasRevenue = this.showRevenueGLA || this.showRevenueOther;
    this.hasGLA = this.showCapitalGLA || this.showRevenueGLA;
    this.canClaimActivity = this.projectBlock.canClaimActivity;

    this.maxEvidenceAttachments = this.projectBlock.maxEvidenceAttachments ? this.projectBlock.maxEvidenceAttachments : 2;

    let fundingBlock = _.find(this.template.blocksEnabled, {block: 'Funding'});
    this.defaultActivityName = (fundingBlock && fundingBlock.defaultActivityName) ? fundingBlock.defaultActivityName : 'Projected cost';
    this.allowActivityUpdate = fundingBlock.multipleBespokeActivitiesEnabled;
    this.allowEvidenceUpload = fundingBlock.budgetEvidenceAttachmentEnabled;

    this.milestones = this.projectMilestonesBlock.milestones || [];

    this.hasMilestones = this.milestones ? this.milestones && this.milestones.length : 0;

    if(this.projectBlock.categoriesExternalId) {
      this.ReferenceDataService.getConfigItemsByExternalId(this.projectBlock.categoriesExternalId).subscribe(categories => {
        this.categories = categories || [];
        this.hasCategories = this.categories ? this.categories && this.categories.length : 0;
      });
    }

    this.processSections(this.projectFunding);
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


  processSections(projectFunding) {
    this.projectFunding.allProjectFunding = projectFunding.allProjectFunding;
    let years = Object.keys(projectFunding.allProjectFunding.fundingByYear);
    let yearsBreakdowns = years.map(year => projectFunding.allProjectFunding.fundingByYear[year]);
    yearsBreakdowns = this.orderByFilter(yearsBreakdowns, ['year']);
    yearsBreakdowns.forEach(yb => {
      yb.mappedSections = this.ProjectFundingService.getMappedSections(yb, 4)
    });
    projectFunding.allProjectFunding.processedFundingByYear = yearsBreakdowns || [];
  }

  onAddQuarterlyBudget(data) {
    return this.ProjectFundingService.addQuarterlyEntry(this.project.id, this.blockId, data)
      .catch(this.ErrorService.apiValidationHandler());
  }

  onQuarterlyBudgetAdded(data) {
     this.ProjectFundingService.getProjectFunding(this.project.id, this.blockId).then((resp) => {
      this.refreshPageData(resp.data);
    });
  }

  onActivityUpdate(activity){
    if(activity.isEditWithModal) {
      this.allowActivityUpdate = activity.allowActivityUpdate;
      this.showFundingEntryModal(activity);
    } else {
      return this.onAddQuarterlyBudget(activity).then(() => {
        activity.originalName = activity.name;
        return this.ProjectFundingService.getProjectFunding(this.project.id,
          this.blockId).then((resp) => {
          this.refreshPageData(resp.data);
        });
      });
    }
  }

  onDeleteActivity(section, milestone, activity){
    var modal = this.ConfirmationDialog.delete('Are you sure you want to delete the expenditure?');
    modal.result.then(resp => {
      return this.ProjectFundingService.deleteActivity(this.project.id, this.blockId, activity.id).then(()=>{
        return this.ProjectFundingService.getProjectFunding(this.project.id, this.blockId).then((resp)=> {
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

  refreshPageData(projectFunding){
    this.projectFunding.claimed = projectFunding.claimed;
    this.projectFunding.claims = projectFunding.claims;

    this.projectFunding.validationFailures = projectFunding.validationFailures;
    this.projectFunding.yearBreakdown = projectFunding.yearBreakdown;
    this.projectFunding.allProjectFunding = projectFunding.allProjectFunding;
    this.processSections(projectFunding);
  }

  showEvidenceModal(activity) {
    activity.attachments = activity.attachments || [];

    let modalConfig = {
      orgId: this.project.organisation.id,
      readOnly: this.readOnly,
      fileIdColumn: 'fileId',
      programmeId: this.project.programmeId,
      projectId: this.project.id,
      blockId: this.blockId,
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
      return this.ProjectFundingService.getProjectFunding(this.project.id, this.projectBlock.id).then((resp) => {
        return this.refreshPageData(resp.data);
      });
    })
  }

  onPostApprovalClaimAction(event) {
    if (event.action === 'cancel' && event.claim.claimStatus === 'Approved') {
      return this.ProjectFundingService.editClaimStatuses(this.project.id, [event.claim.id], 'Withdrawn', event.reason).then(resp => {
        return this.ProjectFundingService.getProjectFunding(this.project.id, this.projectBlock.id).then((resp) => {
          return this.refreshPageData(resp.data);
        });
      })
    }
  }

  onClaimQuarter(data){
    let activityIds = [];
    data.activity ? activityIds.push(data.activity.id) : data.section.activities.forEach(act => {
      if(act.status === 'Claimable') {
        activityIds.push(act.id)
      }
    });
    return this.ProjectFundingService.claim(this.project.id, this.projectBlock.id, data.section.year, data.section.quarter, activityIds).then(resp => {
      return this.ProjectFundingService.getProjectFunding(this.project.id, this.projectBlock.id).then((resp) => {
        return this.refreshPageData(resp.data);
      });
    });
  }

  onCancelClaimedQuarter(claim){
    return this.ProjectFundingService.cancelClaim(this.project.id, this.projectBlock.id, claim.id).then(resp => {
      return this.ProjectFundingService.getProjectFunding(this.project.id, this.projectBlock.id).then((resp) => {
        return this.refreshPageData(resp.data);
      });
    });
  }

  showFundingEntryModal(activity){
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
      monetaryValueScale: this.monetaryValueScaleToUse,
      currentFinancialYear: this.currentFinancialYear,
      block: this.projectFunding
    };

    let modal = this.FundingEntryModal.show(config, activity);
    modal.result.then(data => {
      this.$rootScope.showGlobalLoadingMask = true;
      this.onAddQuarterlyBudget(data).then(rsp => {
        this.onQuarterlyBudgetAdded(rsp.data);
        this.$rootScope.showGlobalLoadingMask = false;
        // refresh data if activity was edited by modal
        if(activity.isEditWithModal) {
          data.originalName = data.name;
          return this.ProjectFundingService.getProjectFunding(this.project.id,
            this.blockId).then((resp) => {
            this.refreshPageData(resp.data);
          });
        }
      })
    })
  }
}

FundingPageCtrl.$inject = ['$injector','$rootScope', 'ProjectService', 'ProjectFundingService', 'ReferenceDataService', 'FileUploadModal', 'orderByFilter', 'FundingEntryModal', '$location', '$anchorScroll'];

angular.module('GLA')
  .component('projectFundingPage', {
  controller: FundingPageCtrl,
  bindings: {
    projectFunding: '<',
    projectMilestonesBlock: '<',
    project: '<',
    currentFinancialYear: '<',
    template: '<'
  },
  templateUrl: 'scripts/pages/project/funding/fundingPage.html'
});
