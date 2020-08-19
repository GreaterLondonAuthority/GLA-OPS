/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

import './grant-income-modal/grantIncomeModal';
import './grant-categories-table/grantCategoriesTable.js'


class RecoverableGrantSubmissionForecastCtrl {
  constructor($state, $rootScope, $stateParams, organisation, block, annualSubmissionCategories, AnnualSubmissionService, LockingService, fYearFilter, GrantIncomeModal, ConfirmationDialog, $q) {
    this.$state = $state;
    this.$stateParams = $stateParams;
    this.$rootScope = $rootScope;
    this.AnnualSubmissionService = AnnualSubmissionService;
    this.annualSubmissionCategories = annualSubmissionCategories;
    this.LockingService = LockingService;
    this.$q = $q;
    this.fYearFilter = fYearFilter;

    this.GrantIncomeModal = GrantIncomeModal;
    this.ConfirmationDialog = ConfirmationDialog;

    this.lockId = 'annualSubmissionBlock';
    this.organisation = organisation;

    this.grantSource =  block.grantType;
    this.block = block;
    this.financialYear = block.financialYear;

    block.unspentGrantYear1 = block.unspentGrantYear1 || 0;
    block.unspentGrantYear2 = block.unspentGrantYear2 || 0;
    block.unspentGrantYear3 = block.unspentGrantYear3 || 0;

    this.totalEntries = 0;
    this.totalCategories = 0;
    this.yearlyCategories = _.map(block.yearBreakdown, (year)=>{
      let clone = _.clone(year);
      clone.categories = this.AnnualSubmissionService.getAvailableCategories(this.annualSubmissionCategories, this.grantSource, 'Spent', 'Forecast', year.entries);
      this.totalCategories += (clone.categories ? clone.categories.length : 0);
      this.totalEntries += (year.entries ? year.entries.length : 0);

      return clone;
    });

    this.requestsQueue = [];
  }

  $onInit() {

    this.lockDetails = this.LockingService.getLockDetails(this.block);
    this.editable = this.LockingService.isBlockEditable(this.block);
    this.isLocked = this.LockingService.isLockedByCurrentUser(this.block);
    this.readOnly = this.editable && this.isLocked ? false : true;

  }

  showGrantIncomeModal(entry, isSpent) {
    let title = 'Forecast spend';

    let modal = this.GrantIncomeModal.show(entry, title, this.yearlyCategories);
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

  edit() {
    this.LockingService.lock(this.lockId, this.block.id).then(rsp => {
      this.isLocked = true;
      this.readOnly = false;
    }).then(()=>{

    })
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
      this.block.unspentGrantTotal = resp.data.unspentGrantTotal;
      this.block.unspentGrantYear1 = this.block.unspentGrantYear1 || 0;
      this.block.unspentGrantYear2 = this.block.unspentGrantYear2 || 0;
      this.block.unspentGrantYear3 = this.block.unspentGrantYear3 || 0;
    });

    this.requestsQueue.push(p);
    return p;
  }

  stopEditing() {
    if (this.isLocked) {
      this.$q.all(this.requestsQueue).then(()=>{
        this.autoSave().then(()=> {
          this.LockingService.unlock(this.lockId, this.block.id).then(()=>{
            this.isLocked = false;
            this.goToOverview();
          });
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

  showAllEntriesFn() {
    _.forEach(this.block.yearBreakdown, (year) => {
        year.expanded = this.showAllEntries;
      }
    );
  }
}

RecoverableGrantSubmissionForecastCtrl.$inject = ['$state', '$rootScope', '$stateParams', 'organisation', 'block', 'annualSubmissionCategories', 'AnnualSubmissionService', 'LockingService', 'fYearFilter', 'GrantIncomeModal', 'ConfirmationDialog', '$q'];

angular.module('GLA').controller('RecoverableGrantSubmissionForecastCtrl', RecoverableGrantSubmissionForecastCtrl);
