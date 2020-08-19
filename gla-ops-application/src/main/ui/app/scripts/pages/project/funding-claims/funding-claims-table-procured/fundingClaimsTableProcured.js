/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

class FundingClaimsTableProcuredCtrl {
  constructor(ProjectSkillsService) {
    this.ProjectSkillsService = ProjectSkillsService;
  }

  $onInit() {
  }

  $onChanges() {
    this.fundingClaimsEntries.forEach(entry => {
      let allFundingEntries = [];
      this.contractTypes.forEach(type => {
        let fundingEntry = _.find(entry.contractTypeFundingEntries, {contractType: type.name});
        allFundingEntries.push(fundingEntry || {contractType: type.name});
      });
      entry.contractTypeFundingEntries.length = 0;
      allFundingEntries.forEach(e => entry.contractTypeFundingEntries.push(e));
      // entry.contractTypeFundingEntries = allFundingEntries;
    });
  }

  getTotalEntryByContractType(totals, name) {
    return _.find((totals || {}).contractTypeTotals, {contractType : name});
  }

  hasMoreThanOneContractType() {
    return this.contractTypes.length > 1;
  }
}

FundingClaimsTableProcuredCtrl.$inject = ['ProjectSkillsService'];


angular.module('GLA')
.component('fundingClaimsTableProcured', {
  controller: FundingClaimsTableProcuredCtrl,
  templateUrl: 'scripts/pages/project/funding-claims/funding-claims-table-procured/fundingClaimsTableProcured.html',
  bindings: {
    projectId: '<',
    blockId: '<',
    fundingClaimsEntries: '<',
    totals: '<',
    selectedPeriod: '<',
    selectedYear: '<',
    readOnly: '<',
    onEntryChange: '&',
    canEditForecast: '<',
    contractTypes: '<',
    flexibleAllocationThreshold: '<?'
  }
});
