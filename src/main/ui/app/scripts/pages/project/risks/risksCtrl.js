/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

import ProjectBlockCtrl from '../ProjectBlockCtrl';

class RisksCtrl extends ProjectBlockCtrl {
  constructor($state, ProjectService, riskCategories, project, $injector, $timeout, RisksService, RiskAndIssueModal, AddRiskActionModal, FeatureToggleService){
    super(project, $injector);

    this.$state = $state;
    this.ProjectService = ProjectService;
    this.RisksService = RisksService;
    this.$timeout = $timeout;
    this.blockData = this.projectBlock;
    this.riskCategories = riskCategories;

    this.RiskAndIssueModal = RiskAndIssueModal;
    this.AddRiskActionModal = AddRiskActionModal;

    this.overallRatings = RisksService.getOverallRatings();
    FeatureToggleService.isFeatureEnabled('ProjectRiskAndIssues').then(resp => {
      this.showRisksAndIssues = resp.data;
    });

  }
  $onInit() {
    this.overallRating = _.find(this.overallRatings, {id: this.blockData.rating});
    this.risks = this.RisksService.getRisks(this.blockData);
    this.issues = this.RisksService.getIssues(this.blockData);
  }

  onSaveData(releaseLock) {
    let data = {
      rating: this.overallRating ? this.overallRating.id : null,
      ratingExplanation: this.blockData.ratingExplanation,
      type: 'ProjectRisksBlock'
    };
    let p = this.RisksService.save(this.project.id, this.blockId, data, releaseLock);
    return this.addToRequestsQueue(p);
  }

  createNewRisk() {
    let modal = this.RiskAndIssueModal.show('Risk', this.riskCategories);
    modal.result.then(resp => {
      let data = _.clone(resp);
      data.type = 'Risk';
      return this.RisksService.postNewRiskOrIssue(this.project.id, this.blockId, data).then((resp)=>{

        let newRisksIds = _.differenceWith(_.map(resp.data.projectRiskAndIssues, 'id'), _.map(this.blockData.projectRiskAndIssues, 'id'), _.isEqual);

        _.forEach(newRisksIds, id => {this.blockSessionStorage.manageProjectRisksTablesState[id] = true});

        return this.refreshData();
      });
    });
  }

  editRisk(risk) {
    let modal = this.RiskAndIssueModal.show('Risk', this.riskCategories, _.clone(risk));
    modal.result.then(resp => {
      return this.RisksService.updateNewRiskOrIssue(this.project.id, this.blockId, resp).then(()=>{
        return this.refreshData();
      });
    });
  }

  deleteRisk(risk) {
    var modal = this.ConfirmationDialog.delete('Are you sure you want to delete the risk?');
    modal.result.then(resp => {
      return this.RisksService.deleteRiskOrIssue(this.project.id, this.blockId, risk.id).then(()=>{
        return this.refreshData();
      });
    });
  }


  closeRiskOrIssue(riskOrIssue){
    const modal = this.ConfirmationDialog.show({
      message: 'Change status to Closed',
      approveText: riskOrIssue.type === 'Risk'? 'CLOSE RISK' : 'CLOSE ISSUE',
      dismissText: 'CANCEL'
    });
    modal.result.then(() => {
      return this.RisksService.closeRiskOrIssue(this.project.id, this.blockId, riskOrIssue.id).then(()=>{
        return this.refreshData();
      });
    });
  }

  createNewIssue() {
    let modal = this.RiskAndIssueModal.show('Issue');
    modal.result.then( resp => {
      let data = _.clone(resp);
      data.type = 'Issue';
      // data.initialImpactRating = data.initialImpactRating ? data.initialImpactRating : '';
      return this.RisksService.postNewRiskOrIssue(this.project.id, this.blockId, data).then((resp)=>{
        let newRisksIds = _.differenceWith(_.map(resp.data.projectRiskAndIssues, 'id'), _.map(this.blockData.projectRiskAndIssues, 'id'), _.isEqual);

        _.forEach(newRisksIds, id => {this.blockSessionStorage.manageProjectIssuesTablesState[id] = true});

        this.refreshData();
      });
    });
  }

  editIssue(issue) {
    let modal = this.RiskAndIssueModal.show('Issue', false, _.clone(issue));
    modal.result.then(resp => {
      return this.RisksService.updateNewRiskOrIssue(this.project.id, this.blockId, resp).then(()=>{
        return this.refreshData();
      });
    });
  }

  deleteIssue(issue) {
    var modal = this.ConfirmationDialog.delete('Are you sure you want to delete the issue?');
    modal.result.then(resp => {
      return this.RisksService.deleteRiskOrIssue(this.project.id, this.blockId, issue.id).then(()=>{
        return this.refreshData();
      });
    });
  }

  addMitigation(risk) {

    let self = this;
    let modal = this.AddRiskActionModal.show('Mitigation');
    modal.result.then(function (resp) {
      let data = _.clone(resp);
      self.RisksService.postNewAction(self.project.id, self.blockId, risk.id, data).then(()=>{
        self.refreshData();
      });
    });
  }

  deleteMitigation(risk, mitigation) {
    let modal = this.ConfirmationDialog.delete('Are you sure you want to delete this mitigation and/or owner?');
    modal.result.then(() => {
      return this.RisksService.deleteAction(this.project.id, this.blockId, risk.id, mitigation.id).then(()=>{
        return this.refreshData();
      });
    });
  }

  addAction(issue) {
    let self = this;
    let modal = this.AddRiskActionModal.show('Action');
    modal.result.then(function (resp) {
      let data = _.clone(resp);
      self.RisksService.postNewAction(self.project.id, self.blockId, issue.id, data).then(()=>{
        self.refreshData();
      });
    });
  }
  deleteAction(issue, action) {
    let self = this;
    let modal = this.ConfirmationDialog.delete('Are you sure you want to delete this action and/or owner?');
    modal.result.then(function (resp) {
      let data = _.clone(resp);
      self.RisksService.deleteAction(self.project.id, self.blockId, issue.id, action.id).then(()=>{
        self.refreshData();
      });
    });
  }

  back() {
    if (this.readOnly) {
      this.returnToOverview();
    } else {
      this.onSaveData(true).then(()=>{
        this.returnToOverview(this.blockId);
      });
    }
  }

  submit() {
    //$timeout to fix autosafe=true after saving
    this.$timeout(()=>{
      this.$q.all(this.requestsQueue).then(results => {
        this.onSaveData(true).then(()=>{
          this.returnToOverview(this.blockId);
        });
      });
    });
  }

  autoSave() {
    this.onSaveData(false).then((resp)=>{
      this.blockData = resp.data;
    });
  }

  refreshData() {
    return this.ProjectService.getProjectBlock(this.project.id, this.blockData.id).then((resp)=>{
      this.blockData = resp.data;
      return this.$onInit();
    });
  }

}

RisksCtrl.$inject = ['$state', 'ProjectService', 'riskCategories', 'project', '$injector', '$timeout', 'RisksService', 'RiskAndIssueModal', 'AddRiskActionModal', 'FeatureToggleService'];

angular.module('GLA')
  .controller('RisksCtrl', RisksCtrl);
