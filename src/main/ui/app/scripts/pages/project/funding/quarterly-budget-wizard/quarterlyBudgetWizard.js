/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

class quarterlyBudgetWizardCtrl{
  constructor($injector, $scope, ProjectService, ProjectFundingService) {

  }

  $onInit() {
    this.resetWizard();
    this.periodType = 'Quarterly';
    let spendTypeOptions = [];
    if(this.showCapitalGla || this.showCapitalOther){
      spendTypeOptions.push({
        label: 'Capital spend',
        type: 'CAPITAL'
      });
    }
    if(this.showRevenueGla || this.showRevenueOther){
     spendTypeOptions.push({
       label: 'Revenue spend',
       type: 'REVENUE'
     });
    };

    this.spendTypeOptions = spendTypeOptions;
    if(this.spendTypeOptions.length == 1){
      this.data.spendType = this.spendTypeOptions[0];
    }

  }

  addQuarterlyEntry() {
    this.onAddQuarterlyBudget({
      data: {
        year: this.year.financialYear,
        quarter: this.data.selectedQuarter.sectionNumber,
        externalId: this.data.milestoneSelected ? this.data.milestoneSelected.id : this.data.spendCategorySelected.id,
        categoryDescription: this.data.milestoneSelected ? this.data.milestoneSelected.summary : this.data.spendCategorySelected.category,
        name: this.data.activityDescription,
        capitalValue: (this.data.spendType.type === 'CAPITAL' ? this.data.value : undefined),
        capitalMatchFundValue: (this.data.spendType.type === 'CAPITAL' ? this.data.matchFundValue : undefined),
        revenueValue: (this.data.spendType.type === 'REVENUE' ? this.data.value : undefined),
        revenueMatchFundValue: (this.data.spendType.type === 'REVENUE' ? this.data.matchFundValue : undefined)
      }
    }).then((resp)=>{
      this.resetWizard();
      this.onQuarterlyBudgetAdded({data:resp.data});
    });
  }


  resetWizard() {
    this.data = {
      activityDescription: this.allowActivityUpdate ? undefined : this.defaultActivityName
    };

    if(this.spendTypeOptions && this.spendTypeOptions.length == 1){
      this.data.spendType = this.spendTypeOptions[0];
    }
  }

  canAddOutput() {
    return this.data &&
      this.data.selectedQuarter &&
      (this.data.milestoneSelected || this.data.spendCategorySelected) &&
      this.data.activityDescription &&
      this.data.spendType &&
      (this.data.value || this.data.matchFundValue);
  }
}



quarterlyBudgetWizardCtrl.$inject = [];

angular.module('GLA')
  .component('quarterlyBudgetWizard', {
  controller: quarterlyBudgetWizardCtrl,
  bindings: {
    year: '<',
    milestones: '<',
    showMilestones: '<',
    categories: '<',
    showCategories: '<',
    onAddQuarterlyBudget: '&',
    onQuarterlyBudgetAdded: '&',
    allowActivityUpdate: '<',
    defaultActivityName: '<',
    showCapitalGla: '<',
    showRevenueGla: '<',
    showCapitalOther: '<',
    showRevenueOther: '<',
    wizardClaimLabel: '<',
    wizardOtherLabel: '<',
    monetaryValueScale: '<'
  },
  templateUrl: 'scripts/pages/project/funding/quarterly-budget-wizard/quarterlyBudgetWizard.html'
});
