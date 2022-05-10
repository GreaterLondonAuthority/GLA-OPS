import DateUtil from '../../../../util/DateUtil';

/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */


function FundingEntryModal($uibModal) {
  return {
    show: function (config, activity) {
      return $uibModal.open({
        bindToController: true,
        controllerAs: '$ctrl',
        animation: false,
        templateUrl: 'scripts/pages/project/funding/funding-entry-modal/fundingEntryModal.html',
        size: 'md',
        controller: ['$uibModalInstance', function ($uibModalInstance) {

          this.onSelectYear = (year) => {
            this.data.selectedQuarter = null
          }

          this.addQuarterlyEntry = () => {
            if (this.data.id) {
              $uibModalInstance.close({
                id: this.data.id,
                year: this.data.selectedYear.financialYear,
                quarter: this.data.selectedQuarter.sectionNumber,
                externalId: this.data.milestoneSelected ? this.data.milestoneSelected.id : this.data.spendCategorySelected.id,
                categoryDescription: this.data.milestoneSelected ? this.data.milestoneSelected.summary : this.data.spendCategorySelected.category,
                name: this.data.activityDescription,
                capitalValue: this.data.capitalValue,
                capitalMatchFundValue: this.data.capitalMatchFundValue,
                revenueValue: this.data.revenueValue,
                revenueMatchFundValue: this.data.revenueMatchFundValue
              });
            } else {
              $uibModalInstance.close({
                year: this.data.selectedYear.financialYear,
                quarter: this.data.selectedQuarter.sectionNumber,
                externalId: this.data.milestoneSelected ? this.data.milestoneSelected.id : this.data.spendCategorySelected.id,
                categoryDescription: this.data.milestoneSelected ? this.data.milestoneSelected.summary : this.data.spendCategorySelected.category,
                name: this.data.activityDescription,
                capitalValue: this.data.capitalValue,
                capitalMatchFundValue: this.data.capitalMatchFundValue,
                revenueValue: this.data.revenueValue,
                revenueMatchFundValue: this.data.revenueMatchFundValue
              });
            }
          };

          this.canAddOutput = () => {
            if (this.data.id) {
              return (activity.allowActivityUpdate && this.data.activityDescription);
            } else {
              return this.data &&
                this.data.selectedYear &&
                this.data.selectedQuarter &&
                (this.data.milestoneSelected || this.data.spendCategorySelected)
                &&
                this.data.activityDescription &&
                (this.data.capitalValue || this.data.capitalMatchFundValue ||
                  this.data.revenueValue || this.data.revenueMatchFundValue);
            }
          };

          this.init = () => {
            _.assign(this, config);
            this.periodType = 'Quarterly';
            if (activity != null && activity.isEditWithModal) {
              this.titleText = 'Edit';
              let yearLabel = DateUtil.toFinancialYearString(activity.year);
              this.data = {
                id: activity.id,
                activityDescription: activity.originalName,
                selectedYear: {financialYear: activity.year, label: yearLabel},
                milestoneSelected: _.find(this.milestones, {summary: activity.categoryDescription}),
                spendCategorySelected: _.find(this.categories, {id: activity.externalId}),
                sectionNumber: activity.sectionNumber,
                capitalValue: activity.capitalValue,
                capitalMatchFundValue: activity.capitalMatchFundValue,
                revenueValue: activity.revenueValue,
                revenueMatchFundValue: activity.revenueMatchFundValue
              }
            } else {
              this.titleText = 'Add';
              this.data = {
                activityDescription: this.allowActivityUpdate ? undefined : this.defaultActivityName
              };
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
