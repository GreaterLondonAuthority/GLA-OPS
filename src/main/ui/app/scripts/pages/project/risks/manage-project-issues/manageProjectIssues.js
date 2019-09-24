/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

import ManageRiskAndIssueCtrl from '../manageRiskAndIssueCtrl'

class ManageProjectIssueCtrl extends ManageRiskAndIssueCtrl{
  constructor($injector){
    super($injector);
  }

  $onInit(){
    this.blockSessionStorage = this.blockSessionStorage || {};
    this.blockSessionStorage.manageProjectIssuesTablesState = this.blockSessionStorage.manageProjectIssuesTablesState || [];
    this.issueImpactLevelsDisplayMap = this.RisksService.getIssueImpactLevelsDisplayMap();
  }

  onIssueMarkCorporateChange(issue) {
    this.blockSessionStorage.manageProjectIssuesTablesState[issue.id] = true;
    issue.actions.forEach(a => { a.markedForCorporateReporting = issue.markedForCorporateReporting } );
    this.updateIssue(issue)
  }



  updateIssue(issue) {
    return this.RisksService.updateNewRiskOrIssue(issue.projectId, issue.blockId, issue).then(()=>{
      return;
    });
  }
}

ManageProjectIssueCtrl.$inject = ['$injector'];

angular.module('GLA')
  .component('manageProjectIssues', {
    bindings: {
      issues: '<',
      createNewIssue: '&',
      addAction: '&',
      deleteAction: '&',
      blockSessionStorage: '<',
      editIssue: '&',
      deleteIssue: '&',
      closeIssue: '&',
      readOnly: '<',
      disableHideClosedFilter: '<',
      header: '<',
      projectMarkedCorporate: '<',
      subheader: '<'
    },
    templateUrl: 'scripts/pages/project/risks/manage-project-issues/manageProjectIssues.html',
    controller: ManageProjectIssueCtrl
});
