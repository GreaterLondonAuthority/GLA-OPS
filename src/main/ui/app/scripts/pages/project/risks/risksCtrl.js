/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

import ProjectBlockCtrl from '../ProjectBlockCtrl';

class RisksCtrl extends ProjectBlockCtrl {
  constructor($state, ProjectService, ProjectBlockService, riskCategories, project, $injector, $timeout, RisksService, RiskAndIssueModal, AddRiskActionModal, FeatureToggleService){
    super($injector);

    this.$state = $state;
    this.ProjectService = ProjectService;
    this.RisksService = RisksService;
    this.$timeout = $timeout;
    this.riskCategories = riskCategories;
    this.RiskAndIssueModal = RiskAndIssueModal;
    this.AddRiskActionModal = AddRiskActionModal;
    this.FeatureToggleService = FeatureToggleService;
  }
  $onInit() {
    super.$onInit();
    this.blockData = this.projectBlock;
    this.projectMarkedCorporate = this.project.markedForCorporate;
    this.overallRatings = this.RisksService.getOverallRatings();
    this.FeatureToggleService.isFeatureEnabled('ProjectRiskAndIssues').then(resp => {
      this.showRisksAndIssues = resp.data;
    });
    this.refreshData();
  }

  onSaveData(releaseLock) {
    let data = {
      rating: this.overallRating ? this.overallRating.id : null,
      ratingExplanation: this.blockData.ratingExplanation,
      type: 'ProjectRisksBlock'
    };
    let p = this.ProjectBlockService.updateBlock(this.project.id, this.blockId, data, releaseLock);
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

    var text = 'Are you sure you want to delete the risk?'
    if (risk.markedForCorporateReporting) {
      text = 'Are you sure you want to delete a marked risk?<br/>' +
        'Deleting a marked risk will delete the risk and mitigations from corporate reporting';
    }

    var modal = this.ConfirmationDialog.delete(text);
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
    var text = 'Are you sure you want to delete the issue?'
    if (issue.markedForCorporateReporting) {
      text = 'Are you sure you want to delete a marked issue?<br/>' +
        'Deleting a marked issue will delete the issue and action from corporate reporting';
    }


    var modal = this.ConfirmationDialog.delete(text);
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

    var text = 'Are you sure you want to delete this mitigation and/or owner?'
    if (mitigation.markedForCorporateReporting) {
      text = 'Are you sure you want to delete a marked mitigation?<br/>' +
        'Deleting a marked mitigation will remove the mitigation from corporate reporting';
    }
    let modal = this.ConfirmationDialog.delete(text);
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
    var text = 'Are you sure you want to delete this action and/or owner?'
    if (action.markedForCorporateReporting) {
      text = 'Are you sure you want to delete a marked action?<br/>' +
        'Deleting a marked action will remove the action from corporate reporting';
    }
    let modal = this.ConfirmationDialog.delete(text);
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
    return this.$timeout(()=>{
      return this.$q.all(this.requestsQueue).then(results => {
        return this.onSaveData(true);
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
      this.overallRating = _.find(this.overallRatings, {id: this.blockData.rating});
      this.risks = this.RisksService.getRisks(this.blockData);
      this.risks.forEach(r => {
        r.projectId= this.project.id;
        r.blockId = this.blockId;
      });
      this.issues = this.RisksService.getIssues(this.blockData);
      this.issues.forEach(r => {
        r.projectId= this.project.id;
        r.blockId = this.blockId;
      });
    });
  }

}

RisksCtrl.$inject = ['$state', 'ProjectService', 'ProjectBlockService', 'riskCategories', 'project', '$injector', '$timeout', 'RisksService', 'RiskAndIssueModal', 'AddRiskActionModal', 'FeatureToggleService'];

angular.module('GLA')
  .controller('RisksCtrl', RisksCtrl);
