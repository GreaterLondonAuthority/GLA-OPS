/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

class FundingClaimsTableCtrl {
  constructor(ProjectSkillsService) {
    this.ProjectSkillsService = ProjectSkillsService;
  }

}

FundingClaimsTableCtrl.$inject = ['ProjectSkillsService'];


angular.module('GLA')
.component('fundingClaimsTable', {
  controller: FundingClaimsTableCtrl,
  templateUrl: 'scripts/pages/project/funding-claims/funding-claims-table/fundingClaimsTable.html',
  bindings: {
    projectId: '<',
    blockId: '<',
    fundingClaimsEntries: '<',
    totals: '<',
    selectedPeriod: '<',
    selectedYear: '<',
    readOnly: '<',
    onEntryChange: '&'
  }
});
