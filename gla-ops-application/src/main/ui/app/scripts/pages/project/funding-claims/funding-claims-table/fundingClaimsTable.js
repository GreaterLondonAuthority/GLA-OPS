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


  $onChanges(changes) {
    if (changes.fundingClaimsEntries) {
      this.fundingClaimsEntries = _.sortBy(this.fundingClaimsEntries, 'categoryId')
      this.fundingClaimsEntriesModel = [];
      this.fundingClaimsEntries.forEach(category => {
        if (!category.parentCategoryId) {
          category.subCategories = []
          this.fundingClaimsEntriesModel.push(category);
        } else {
          this.findParentCategory(this.fundingClaimsEntriesModel, category).subCategories.push(category)
        }
      });
    }
  }

   findParentCategory(fundingClaimsEntriesModel, category) {
     return _.find(fundingClaimsEntriesModel, {categoryId: category.parentCategoryId,
                                               period: category.period, academicYear: category.academicYear})
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
    onEntryChange: '&',
    canEditForecast: '<'
  }
});
