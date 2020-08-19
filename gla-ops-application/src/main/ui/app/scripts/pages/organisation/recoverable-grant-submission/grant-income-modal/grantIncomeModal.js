/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

function GrantIncomeModal($uibModal, AnnualSubmissionService) {
  return {
    show: function (entry, title, years) {
      return $uibModal.open({
        bindToController: true,
        controllerAs: '$ctrl',
        animation: false,
        templateUrl: 'scripts/pages/organisation/recoverable-grant-submission/grant-income-modal/grantIncomeModal.html',
        size: 'md',
        controller: [function () {
          this.onYearSelect = (financialYear) => {
            if(!financialYear){
              return;
            }
            let year = _.find(this.years, {financialYear: financialYear});
            this.categories = year.categories;
            // don't do if editing a selection
            if(this.entry && !this.entry.id){
              this.entry.category = undefined;
              this.entry.value = undefined;
              // this.entry.comments = undefined;
            }
          }
          this.years = years;
          this.title = title;
          this.entry = angular.copy(entry || {});
          this.requiresYearSelection = years.length > 1;
          if(!this.requiresYearSelection){
            this.entry.financialYear = years[0].financialYear;
            this.onYearSelect(years[0].financialYear);
          }
          this.btnName = entry && entry.id ? 'UPDATE' : 'ADD';
        }]
      });
    }
  };
}

GrantIncomeModal.$inject = ['$uibModal', 'AnnualSubmissionService'];

angular.module('GLA')
  .service('GrantIncomeModal', GrantIncomeModal);
