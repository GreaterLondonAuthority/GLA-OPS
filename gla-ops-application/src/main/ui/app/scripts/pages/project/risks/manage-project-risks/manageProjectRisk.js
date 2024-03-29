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
  }

  $onInit(){
    this.columnCount = 6 + ((this.projectMarkedCorporate?1:0) + (this.risksBlockConfig.showResidualRiskRatings?1:0))
    this.blockSessionStorage = this.blockSessionStorage || {};
    this.blockSessionStorage.manageProjectRisksTablesState = this.blockSessionStorage.manageProjectRisksTablesState || [];
  }


  onMarkCorporateChange(risk) {
    this.blockSessionStorage.manageProjectRisksTablesState[risk.id] = true;
    risk.actions.forEach(a => { a.markedForCorporateReporting = risk.markedForCorporateReporting } );
    this.updateRisk(risk)
  }



  updateRisk(risk) {
    return this.RisksService.updateNewRiskOrIssue(risk.projectId, risk.blockId, risk).then(()=>{

    });
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
      editAction: '&',
      deleteRisk: '&',
      closeRisk: '&',
      readOnly: '<',
      disableHideClosedFilter: '<',
      header: '<',
      projectMarkedCorporate: '<',
      subheader: '<',
      risksBlockConfig: '<'
    },
    templateUrl: 'scripts/pages/project/risks/manage-project-risks/manageProjectRisk.html',
    controller: ManageProjectRiskCtrl
});
