/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

import './grant-income-modal/grantIncomeModal';
import './grant-categories-table/grantCategoriesTable.js'

class RecoverableGrantSubmissionCtrl {
  constructor($state, $rootScope, $stateParams, organisation, block, annualSubmissionCategories, GrantIncomeModal, AnnualSubmissionService, LockingService, fYearFilter,ConfirmationDialog, $q) {
    this.$state = $state;
    this.$stateParams = $stateParams;
    this.$rootScope = $rootScope;
    this.GrantIncomeModal = GrantIncomeModal;
    this.AnnualSubmissionService = AnnualSubmissionService;
    this.LockingService = LockingService;
    this.fYearFilter = fYearFilter;
    this.ConfirmationDialog = ConfirmationDialog;
    this.$q = $q;

    this.organisation = organisation;
    this.block = block;
    this.annualSubmissionCategories = annualSubmissionCategories;
  }

  $onInit() {
    this.lockId = 'annualSubmissionBlock';
    this.grantSource =  this.block.grantType;
    this.isRcgf = this.block.grantType === 'RCGF';
    this.financialYear = this.block.financialYear;

    this.isInterestedAccumulatedSet = _.isNumber(this.block.interestedAccumulated);
    this.isTotalUnspentGrantSet = _.isNumber(this.block.totalUnspentGrant);

    this.availableCategoriesForGenerated = this.AnnualSubmissionService.getAvailableCategories(this.annualSubmissionCategories, this.grantSource, 'Generated', 'Actual', this.block.generatedEntries)
    this.availableCategoriesForSpent = this.AnnualSubmissionService.getAvailableCategories(this.annualSubmissionCategories, this.grantSource, 'Spent', 'Actual', this.block.spentEntries)
    this.requestsQueue = [];

    //TODO move editing/readonly handling outside this controller
    this.lockDetails = this.LockingService.getLockDetails(this.block);
    this.editable = this.LockingService.isBlockEditable(this.block);
    this.isLocked = this.LockingService.isLockedByCurrentUser(this.block);
    this.readOnly = this.editable && this.isLocked ? false : true;

    this.showCommentsForGenerated = this.$stateParams.showCommentsForGenerated;
    this.showCommentsForSpent = this.$stateParams.showCommentsForSpent;

    this.tiles =  this.AnnualSubmissionService.getBalanceTiles(this.block, this.organisation.annualSubmissions);

    if(this.isPreviousYearApproved()) {
      this.block.openingBalance = this.block.computedOpeningBalance;
    }
  }

    isPreviousYearApproved() {
      let previousYear = _.find(this.organisation.annualSubmissions, {financialYear: this.financialYear - 1});
      if(previousYear != null) {
        return previousYear.status === 'Approved';
      } else {
        return false;
      }
    }


  edit() {
    this.LockingService.lock(this.lockId, this.block.id).then(rsp => {
      this.isLocked = true;
      this.readOnly = false;
    }).then(()=>{
      // this.AnnualSubmissionService.getAnnualSubmission(this.annualSubmission.id).then(resp => {
      //   this.annualSubmission = resp.data;
      //   this.block = this.isRcgf ? this.annualSubmission.annualRcgf : this.annualSubmission.annualDpf;
      // });
    })
  }

  showGrantIncomeModal(entry, isSpent) {
    let availableCategories = isSpent? this.availableCategoriesForSpent : this.availableCategoriesForGenerated;
    let fYear = this.fYearFilter(this.financialYear);
    let title = isSpent? `Spent recoverable grant outgoing ${fYear}`: `Generated recoverable grant income ${fYear}`;
    let modal = this.GrantIncomeModal.show(entry, title, [{financialYear: this.financialYear, categories: availableCategories}]);
    modal.result.then((entry) => {
      this.$rootScope.showGlobalLoadingMask = true;
      let p = null;
      if (entry.id) {
        p = this.AnnualSubmissionService.updateAnnualSubmissionEntry(this.$stateParams.submissionId, this.block.id, entry)
      } else {
        p = this.AnnualSubmissionService.saveAnnualSubmissionEntry(this.$stateParams.submissionId, this.block.id, entry)
      }
      p.then(() => {
        this.$state.params.showCommentsForGenerated = this.showCommentsForGenerated;
        this.$state.params.showCommentsForSpent = this.showCommentsForSpent;
        return this.$state.go(this.$state.current, this.$state.params, {reload: true});
      });
    });
  }

  deleteRow(entry) {
    var modal = this.ConfirmationDialog.delete('Are you sure you want to delete this entry? All information will be permanently deleted.');
    modal.result.then(() =>{
      this.AnnualSubmissionService.deleteAnnualSubmissionEntry(this.$stateParams.submissionId, this.block.id, entry).then(()=> {
        return this.$state.go(this.$state.current, this.$state.params, {reload: true});
      });
    });
  }

  autoSave(){
    let data = angular.copy(this.block);
    delete data.totals;
    let p = this.AnnualSubmissionService.updateAnnualSubmissionBlock(this.$stateParams.submissionId, data);
    p = p.then(resp=>{
      this.block = resp.data;
    });
    this.requestsQueue.push(p);
  }

  showGrantDependantFields(){
    return this.block.template.rolloverEnabled && (this.block.totalUnspentGrant > 0 || this.block.interestedAccumulated > 0);
  }


  stopEditing() {
    if (this.isLocked) {
      this.$q.all(this.requestsQueue).then(()=>{
        this.LockingService.unlock(this.lockId, this.block.id).then(()=>{
          this.isLocked = false;
          this.goToOverview();
        });
      }).finally(()=>{
        this.requestsQueue = [];
      });
    } else {
      this.goToOverview();
    }
  }

  goToOverview() {
    this.$state.go('annual-submission', {
      orgId: this.organisation.id,
      annualSubmissionId: this.$stateParams.submissionId
    });
  }
}

RecoverableGrantSubmissionCtrl.$inject = ['$state', '$rootScope', '$stateParams', 'organisation', 'block', 'annualSubmissionCategories', 'GrantIncomeModal', 'AnnualSubmissionService', 'LockingService', 'fYearFilter', 'ConfirmationDialog', '$q'];

angular.module('GLA').controller('RecoverableGrantSubmissionCtrl', RecoverableGrantSubmissionCtrl);
