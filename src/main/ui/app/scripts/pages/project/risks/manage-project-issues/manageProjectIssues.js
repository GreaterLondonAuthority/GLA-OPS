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
    this.blockSessionStorage.manageProjectIssuesTablesState = this.blockSessionStorage.manageProjectIssuesTablesState || [];
    this.issueImpactLevelsDisplayMap = this.RisksService.getIssueImpactLevelsDisplayMap();
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
      readOnly: '<'
    },
    templateUrl: 'scripts/pages/project/risks/manage-project-issues/manageProjectIssues.html',
    controller: ManageProjectIssueCtrl
});
