/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

import ManageRiskAndIssueCtrl from '../manageRiskAndIssueCtrl'

class ManageProjectRiskCtrl extends ManageRiskAndIssueCtrl{
  constructor($injector){
    super($injector);
    this.blockSessionStorage.manageProjectRisksTablesState = this.blockSessionStorage.manageProjectRisksTablesState || [];
  }
}

ManageProjectRiskCtrl.$inject = ['$injector'];

angular.module('GLA')
  .component('manageProjectRisk', {
    bindings: {
      risks: '<',
      createNewRisk: '&',
      addMitigation: '&',
      deleteMitigation: '&',
      blockSessionStorage: '<',
      editRisk: '&',
      deleteRisk: '&',
      closeRisk: '&',
      readOnly: '<'
    },
    templateUrl: 'scripts/pages/project/risks/manage-project-risks/manageProjectRisk.html',
    controller: ManageProjectRiskCtrl
});
