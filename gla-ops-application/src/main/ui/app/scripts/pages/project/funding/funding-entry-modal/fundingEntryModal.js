/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */


function FundingEntryModal($uibModal) {
  return {
    show: function (config) {
      return $uibModal.open({
        bindToController: true,
        controllerAs: '$ctrl',
        animation: false,
        templateUrl: 'scripts/pages/project/funding/funding-entry-modal/fundingEntryModal.html',
        size: 'md',
        controller: ['$uibModalInstance', function ($uibModalInstance) {

          this.addQuarterlyEntry = () => {
            $uibModalInstance.close({
              year: this.year.financialYear,
              quarter: this.data.selectedQuarter.sectionNumber,
              externalId: this.data.milestoneSelected ? this.data.milestoneSelected.id : this.data.spendCategorySelected.id,
              categoryDescription: this.data.milestoneSelected ? this.data.milestoneSelected.summary : this.data.spendCategorySelected.category,
              name: this.data.activityDescription,
              capitalValue: (this.data.spendType.type === 'CAPITAL' ? this.data.value : undefined),
              capitalMatchFundValue: (this.data.spendType.type === 'CAPITAL' ? this.data.matchFundValue : undefined),
              revenueValue: (this.data.spendType.type === 'REVENUE' ? this.data.value : undefined),
              revenueMatchFundValue: (this.data.spendType.type === 'REVENUE' ? this.data.matchFundValue : undefined)
            });
          };

          this.canAddOutput = () => {
            return this.data &&
              this.data.selectedQuarter &&
              (this.data.milestoneSelected || this.data.spendCategorySelected) &&
              this.data.activityDescription &&
              this.data.spendType &&
              (this.data.value || this.data.matchFundValue);
          };

          this.init = () => {
            _.assign(this, config);

            this.data = {
              activityDescription: this.allowActivityUpdate ? undefined : this.defaultActivityName
            };

            if(this.spendTypeOptions && this.spendTypeOptions.length == 1){
              this.data.spendType = this.spendTypeOptions[0];
            }

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
            }

            this.spendTypeOptions = spendTypeOptions;
            if(this.spendTypeOptions.length == 1){
              this.data.spendType = this.spendTypeOptions[0];
            }
          };

          this.init();
        }]
      });
    }
  }
}

FundingEntryModal.$inject = ['$uibModal'];

angular.module('GLA')
  .service('FundingEntryModal', FundingEntryModal);
